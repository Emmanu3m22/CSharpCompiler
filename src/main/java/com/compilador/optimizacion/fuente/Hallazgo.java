package com.compilador.optimizacion.fuente;

/**
 * Representa un fragmento de código muerto identificado y eliminado durante un pase.
 *
 * @param linea           línea de inicio del código eliminado en el archivo fuente (1-based)
 * @param tipo            clasificación de la regla que detectó el código muerto
 * @param codigoEliminado fragmento de texto o sentencia eliminada
 * @param motivo          explicación pedagógica de por qué se considera código muerto
 */
public record Hallazgo(
        int linea,
        Tipo tipo,
        String codigoEliminado,
        String motivo
) {
    public enum Tipo {
        INALCANZABLE("Código Inalcanzable"),
        CONDICION_CONSTANTE("Condición Constante"),
        VARIABLE_SIN_USO("Variable Sin Uso"),
        ALMACENAMIENTO_SOBRESCRITO("Almacenamiento Sobrescrito"),
        OTRO("Optimización");

        private final String etiqueta;

        Tipo(String etiqueta) {
            this.etiqueta = etiqueta;
        }

        public String getEtiqueta() {
            return etiqueta;
        }
    }
}
