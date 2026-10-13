package com.compilador.intermedio;

import java.util.List;

/**
 * Contiene toda la información intermedia generada para una expresión aritmética:
 * - Ubicación en código fuente (línea)
 * - Destino (si se asignó a una variable)
 * - Notación infija (reconstruida con formato limpio)
 * - Notación prefija (notación polaca)
 * - Notación posfija (notación polaca inversa)
 * - Lista de cuádruplos generados con temporales
 * - Resultado evaluado (si fue posible calcularlo)
 * - Mensaje de error de evaluación (si no fue posible calcularlo)
 * - Traza de la pila durante la evaluación posfija
 */
public record ResultadoExpresion(
        int linea,
        String destino,
        String infijo,
        String prefijo,
        String posfijo,
        List<Cuadruplo> cuadruplos,
        Valor resultado,
        String errorEvaluacion,
        List<String> trazaPila
) {
    public boolean esEvaluable() {
        return errorEvaluacion == null && resultado != null;
    }

    public String resultadoOErrorTexto() {
        if (esEvaluable()) {
            return destino != null ? destino + " = " + resultado : resultado.toString();
        }
        return errorEvaluacion != null ? "No evaluable: " + errorEvaluacion : "No evaluable";
    }
}
