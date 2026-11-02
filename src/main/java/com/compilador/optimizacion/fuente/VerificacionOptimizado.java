package com.compilador.optimizacion.fuente;

import java.util.List;

/**
 * Resultado de verificar un texto de código: errores léxicos, sintácticos y semánticos
 * ya redactados de forma legible. Las líneas son las del texto verificado.
 */
public record VerificacionOptimizado(List<ErrorVerificacion> errores) {

    /**
     * @param fase       "Léxico", "Sintáctico" o "Semántico"
     * @param linea      línea dentro del texto verificado (0 si se desconoce)
     * @param columna    columna dentro del texto verificado (0 si se desconoce)
     * @param titulo     resumen corto del error
     * @param mensaje    descripción del problema
     * @param sugerencia cómo corregirlo (puede estar vacía)
     */
    public record ErrorVerificacion(String fase, int linea, int columna,
                                    String titulo, String mensaje, String sugerencia) {
        public String comoTexto() {
            String ubic = linea > 0 ? "línea " + linea + ", columna " + columna : "sin ubicación";
            return "[" + fase + "] (" + ubic + ") " + titulo + ": " + mensaje;
        }
    }

    public VerificacionOptimizado {
        errores = errores == null ? List.of() : List.copyOf(errores);
    }

    public static VerificacionOptimizado sinErrores() {
        return new VerificacionOptimizado(List.of());
    }

    public boolean valido() {
        return errores.isEmpty();
    }

    public int totalErrores() {
        return errores.size();
    }
}
