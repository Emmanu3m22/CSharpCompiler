package com.compilador.optimizacion.fuente;

import com.compilador.ast.NodoPrograma;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado global del pipeline de optimización de código fuente.
 *
 * Si el código optimizado resultó inválido, la optimización se <b>rechaza</b>:
 * {@link #codigoFinal()} es el código original, {@link #rechazada()} es true,
 * {@link #codigoRechazado()} conserva el candidato descartado y {@link #verificacion()}
 * explica qué errores tenía.
 */
public record ResultadoOptimizacionFuente(
        String codigoOriginal,
        String codigoFinal,
        String codigoEnmascaradoFinal,
        NodoPrograma astLimpio,
        List<EtapaOptimizacion> etapas,
        boolean omitida,
        String motivoOmision,
        long tiempoTotalNanos,
        List<String> advertencias,
        VerificacionOptimizado verificacion,
        String codigoRechazado
) {
    public ResultadoOptimizacionFuente {
        etapas = etapas == null ? List.of() : List.copyOf(etapas);
        advertencias = advertencias == null ? List.of() : List.copyOf(advertencias);
        verificacion = verificacion == null ? VerificacionOptimizado.sinErrores() : verificacion;
    }

    /** Constructor con advertencias, sin verificación (compatibilidad). */
    public ResultadoOptimizacionFuente(String codigoOriginal, String codigoFinal, String codigoEnmascaradoFinal,
                                       NodoPrograma astLimpio, List<EtapaOptimizacion> etapas, boolean omitida,
                                       String motivoOmision, long tiempoTotalNanos, List<String> advertencias) {
        this(codigoOriginal, codigoFinal, codigoEnmascaradoFinal, astLimpio, etapas, omitida,
                motivoOmision, tiempoTotalNanos, advertencias, null, null);
    }

    /** Constructor sin advertencias (compatibilidad). */
    public ResultadoOptimizacionFuente(String codigoOriginal, String codigoFinal, String codigoEnmascaradoFinal,
                                       NodoPrograma astLimpio, List<EtapaOptimizacion> etapas, boolean omitida,
                                       String motivoOmision, long tiempoTotalNanos) {
        this(codigoOriginal, codigoFinal, codigoEnmascaradoFinal, astLimpio, etapas, omitida,
                motivoOmision, tiempoTotalNanos, List.of(), null, null);
    }

    public static ResultadoOptimizacionFuente omitida(String codigo, String motivo) {
        return new ResultadoOptimizacionFuente(codigo, codigo, codigo, null,
                List.of(), true, motivo, 0L);
    }

    /** @return true si se descartó la optimización porque el código resultante era inválido. */
    public boolean rechazada() {
        return codigoRechazado != null;
    }

    /** @return true si hay un código optimizado válido que se puede guardar o usar. */
    public boolean aplicable() {
        return !omitida && !rechazada() && verificacion.valido();
    }

    public int totalEliminaciones() {
        int total = 0;
        for (EtapaOptimizacion etapa : etapas) {
            total += etapa.totalEliminaciones();
        }
        return total;
    }

    public List<Hallazgo> todosLosHallazgos() {
        List<Hallazgo> todos = new ArrayList<>();
        for (EtapaOptimizacion etapa : etapas) {
            todos.addAll(etapa.hallazgos());
        }
        return List.copyOf(todos);
    }

    public double tiempoTotalMilisegundos() {
        return tiempoTotalNanos / 1_000_000.0;
    }
}
