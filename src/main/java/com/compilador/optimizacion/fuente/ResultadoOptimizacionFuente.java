package com.compilador.optimizacion.fuente;

import com.compilador.ast.NodoPrograma;
import java.util.ArrayList;
import java.util.List;

/**
 * Resultado global del pipeline de optimización de código fuente.
 */
public record ResultadoOptimizacionFuente(
        String codigoOriginal,
        String codigoFinal,
        String codigoEnmascaradoFinal,
        NodoPrograma astLimpio,
        List<EtapaOptimizacion> etapas,
        boolean omitida,
        String motivoOmision,
        long tiempoTotalNanos
) {
    public ResultadoOptimizacionFuente {
        etapas = etapas == null ? List.of() : List.copyOf(etapas);
    }

    public static ResultadoOptimizacionFuente omitida(String codigo, String motivo) {
        return new ResultadoOptimizacionFuente(codigo, codigo, codigo, null,
                List.of(), true, motivo, 0L);
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
