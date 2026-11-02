package com.compilador.optimizacion.fuente;

import java.util.Collections;
import java.util.List;

/**
 * Representa el resultado de la ejecución de una etapa o pase de optimización sobre el código fuente.
 * Contiene el código antes y después, la versión enmascarada para las siguientes fases,
 * los hallazgos explicados y los rangos de caracteres eliminados.
 */
public record EtapaOptimizacion(
        String nombre,
        String descripcion,
        String codigoAntes,
        String codigoDespues,
        String codigoEnmascarado,
        List<Hallazgo> hallazgos,
        List<RangoTexto> rangosEliminados,
        long tiempoNanos
) {
    public EtapaOptimizacion {
        hallazgos = hallazgos == null ? List.of() : List.copyOf(hallazgos);
        rangosEliminados = rangosEliminados == null ? List.of() : List.copyOf(rangosEliminados);
    }

    public static EtapaOptimizacion sinCambios(String nombre, String descripcion, String codigo, long nanos) {
        return new EtapaOptimizacion(nombre, descripcion, codigo, codigo, codigo,
                List.of(), List.of(), nanos);
    }

    public boolean hayCambios() {
        return !hallazgos.isEmpty() || !rangosEliminados.isEmpty();
    }

    public int totalEliminaciones() {
        return hallazgos.size();
    }

    public double tiempoMilisegundos() {
        return tiempoNanos / 1_000_000.0;
    }
}
