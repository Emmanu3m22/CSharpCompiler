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

    /** Límite de repeticiones de la cadena de pases (evita ciclos infinitos). */
    private static final int MAX_RONDAS = 5;

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
        List<String> advertencias = new ArrayList<>();
        String actualEnmascarado = codigoOriginal;
        String actualDespues = codigoOriginal;

        // Los pases pueden habilitarse entre sí (p. ej. plegar constantes deja variables muertas),
        // por eso se repite la cadena hasta que ninguna etapa produzca cambios.
        for (int ronda = 1; ronda <= MAX_RONDAS; ronda++) {
            boolean huboCambios = false;

            for (PaseOptimizacionFuente pase : pases) {
                CodigoFuente fuenteActual = new CodigoFuente(actualEnmascarado);
                long t0 = System.nanoTime();
                EtapaOptimizacion etapa;
                try {
                    etapa = pase.aplicar(fuenteActual);
                } catch (Throwable e) {
                    advertencias.add("El pase '" + pase.nombre() + "' falló y se omitió: " + e);
                    continue;
                }
                long dt = System.nanoTime() - t0;
                if (etapa == null) continue;

                // Un pase nunca debe dejar el programa con más errores que antes
                if (etapa.hayCambios()) {
                    int antes = ValidadorSintaxis.contarErrores(actualEnmascarado);
                    int despues = ValidadorSintaxis.contarErrores(etapa.codigoEnmascarado());
                    if (despues > antes) {
                        advertencias.add("El pase '" + pase.nombre()
                                + "' produjo código inválido y se revirtió.");
                        continue;
                    }
                }

                // En rondas adicionales solo interesan las etapas que cambian algo
                if (ronda > 1 && !etapa.hayCambios()) continue;

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
                if (etapaAjustada.hayCambios()) huboCambios = true;
            }

            if (!huboCambios) break;
        }

        // Verificar el texto que realmente se entregaría (colapsado, sin comentarios).
        // Si tiene más errores que el original, la optimización se rechaza por completo.
        VerificacionOptimizado verificacion = VerificadorCodigoOptimizado.verificar(actualDespues);
        if (!verificacion.valido()) {
            int erroresOriginal = VerificadorCodigoOptimizado.verificar(codigoOriginal).totalErrores();
            if (verificacion.totalErrores() > erroresOriginal) {
                advertencias.add("Se descartó la optimización: el código optimizado tiene "
                        + verificacion.totalErrores() + " error(es) que el original no tenía.");
                return new ResultadoOptimizacionFuente(
                        codigoOriginal, codigoOriginal, codigoOriginal, null,
                        List.of(), false, null,
                        System.nanoTime() - inicioTotal,
                        advertencias, verificacion, actualDespues);
            }
        }

        long tiempoTotal = System.nanoTime() - inicioTotal;

        // Intentar parsear el código enmascarado final para generar el AST limpio
        NodoPrograma astLimpio = null;
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(actualEnmascarado.getBytes(StandardCharsets.UTF_8));
            Analizador parser = new Analizador(bais);
            astLimpio = parser.programa();
        } catch (Throwable e) {
            advertencias.add("No se pudo generar el AST limpio: " + e.getClass().getSimpleName());
        }

        return new ResultadoOptimizacionFuente(
                codigoOriginal,
                actualDespues,
                actualEnmascarado,
                astLimpio,
                etapas,
                false,
                null,
                tiempoTotal,
                advertencias,
                verificacion,
                null
        );
    }
}
