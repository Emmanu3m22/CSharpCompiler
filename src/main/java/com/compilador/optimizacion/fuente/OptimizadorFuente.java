package com.compilador.optimizacion.fuente;

import com.compilador.Analizador;
import com.compilador.ast.NodoPrograma;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Orquestador del pipeline de optimizaciones a nivel de código fuente.
 *
 * Encadena múltiples pases independientes. Cada pase toma la versión del código
 * generada por el pase anterior, produce su bitácora de cambios y genera
 * la versión enmascarada para el siguiente.
 *
 * Al final, parsea la versión enmascarada para entregar un {@link NodoPrograma} limpio
 * conservando las líneas originales intactas para las fases posteriores (semántico,
 * código intermedio).
 */
public class OptimizadorFuente {

    private final List<PaseOptimizacionFuente> pases;

    public OptimizadorFuente() {
        this.pases = new ArrayList<>();
    }

    /**
     * Instancia configurada con los pases de optimización oficiales.
     * Aquí se registrarán los futuros pases (operaciones aritméticas, redundancias, ciclos).
     */
    public static OptimizadorFuente crearPorDefecto() {
        OptimizadorFuente opt = new OptimizadorFuente();
        opt.agregarPase(new EliminadorCodigoMuerto());
        return opt;
    }

    public OptimizadorFuente agregarPase(PaseOptimizacionFuente pase) {
        if (pase != null) {
            pases.add(pase);
        }
        return this;
    }

    public List<PaseOptimizacionFuente> getPases() {
        return List.copyOf(pases);
    }

    /**
     * Ejecuta la cadena de optimizaciones sobre el código fuente.
     */
    public ResultadoOptimizacionFuente optimizar(String codigoOriginal) {
        if (codigoOriginal == null || codigoOriginal.trim().isEmpty()) {
            return new ResultadoOptimizacionFuente(
                    codigoOriginal != null ? codigoOriginal : "",
                    codigoOriginal != null ? codigoOriginal : "",
                    codigoOriginal != null ? codigoOriginal : "",
                    null,
                    List.of(),
                    false,
                    null,
                    0L
            );
        }

        long inicioTotal = System.nanoTime();
        List<EtapaOptimizacion> etapas = new ArrayList<>();
        String actualEnmascarado = codigoOriginal;
        String actualDespues = codigoOriginal;

        for (PaseOptimizacionFuente pase : pases) {
            CodigoFuente fuenteActual = new CodigoFuente(actualEnmascarado);
            long t0 = System.nanoTime();
            EtapaOptimizacion etapa = pase.aplicar(fuenteActual);
            long dt = System.nanoTime() - t0;

            // Aseguramos que el tiempo medido sea fiel si el pase no lo calculó internamente
            EtapaOptimizacion etapaAjustada = new EtapaOptimizacion(
                    etapa.nombre(),
                    etapa.descripcion(),
                    actualDespues,
                    etapa.codigoDespues(),
                    etapa.codigoEnmascarado(),
                    etapa.hallazgos(),
                    etapa.rangosEliminados(),
                    etapa.tiempoNanos() > 0 ? etapa.tiempoNanos() : dt
            );

            etapas.add(etapaAjustada);
            actualEnmascarado = etapaAjustada.codigoEnmascarado();
            actualDespues = etapaAjustada.codigoDespues();
        }

        long tiempoTotal = System.nanoTime() - inicioTotal;

        // Intentar parsear el código enmascarado final para generar el AST limpio
        NodoPrograma astLimpio = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(actualEnmascarado.getBytes(StandardCharsets.UTF_8));
            Analizador parser = new Analizador(bais);
            astLimpio = parser.programa();
        } catch (Throwable e) {
            // Si el parser falla (por ejemplo por fragmentos atípicos), queda astLimpio = null
        }

        return new ResultadoOptimizacionFuente(
                codigoOriginal,
                actualDespues,
                actualEnmascarado,
                astLimpio,
                etapas,
                false,
                null,
                tiempoTotal
        );
    }
}
