package com.compilador.optimizacion.fuente;

import com.compilador.Analizador;
import com.compilador.ast.NodoPrograma;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Pase de optimización para la eliminación de código muerto.
 *
 * Orquesta las reglas {@link ReglaCodigoMuerto} sobre un mismo AST:
 * <ul>
 *   <li>R1 {@link ReglaInalcanzable}: sentencias tras return, break, continue o throw.</li>
 *   <li>R2/R3 {@link ReglaCondicionConstante}: ramas muertas por condiciones constantes.</li>
 *   <li>R4 {@link ReglaVariablesSinUso}: variables que nunca se leen (punto fijo).</li>
 * </ul>
 */
public class EliminadorCodigoMuerto implements PaseOptimizacionFuente {

    private final List<ReglaCodigoMuerto> reglas;

    public EliminadorCodigoMuerto() {
        this(List.of(new ReglaInalcanzable(), new ReglaCondicionConstante(), new ReglaVariablesSinUso()));
    }

    public EliminadorCodigoMuerto(List<ReglaCodigoMuerto> reglas) {
        this.reglas = List.copyOf(reglas);
    }

    @Override
    public String nombre() {
        return "Eliminación de Código Muerto";
    }

    @Override
    public String descripcion() {
        return "Detecta y elimina código inalcanzable tras terminadores, condiciones constantes y variables sin uso posterior.";
    }

    @Override
    public EtapaOptimizacion aplicar(CodigoFuente fuente) {
        if (fuente == null || fuente.getTexto().trim().isEmpty()) {
            return EtapaOptimizacion.sinCambios(nombre(), descripcion(), "", 0L);
        }

        long t0 = System.nanoTime();
        List<Hallazgo> hallazgos = new ArrayList<>();
        RangosMarcados rangos = new RangosMarcados();

        NodoPrograma ast = parsear(fuente.getTexto());
        if (ast != null) {
            for (ReglaCodigoMuerto regla : reglas) {
                regla.aplicar(ast, fuente, hallazgos, rangos);
            }
        }

        long dt = System.nanoTime() - t0;
        if (rangos.isEmpty()) {
            return new EtapaOptimizacion(
                    nombre(), descripcion(),
                    fuente.getTexto(),
                    fuente.colapsar(List.of()),
                    fuente.getTexto(),
                    List.of(), List.of(), dt);
        }

        List<RangoTexto> normalizados = CodigoFuente.normalizarRangos(rangos.comoLista());
        return new EtapaOptimizacion(
                nombre(), descripcion(),
                fuente.getTexto(),
                fuente.colapsar(normalizados),
                fuente.enmascarar(normalizados),
                hallazgos, normalizados, dt);
    }

    private static NodoPrograma parsear(String codigo) {
        try {
            Analizador parser = new Analizador(new ByteArrayInputStream(codigo.getBytes(StandardCharsets.UTF_8)));
            return parser.programa();
        } catch (Throwable e) {
            return null;
        }
    }
}
