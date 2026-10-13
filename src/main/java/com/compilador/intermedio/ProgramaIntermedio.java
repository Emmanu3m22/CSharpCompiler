package com.compilador.intermedio;

import java.util.List;

/**
 * Representación intermedia completa de un programa:
 * - Lista de expresiones aritméticas analizadas (con infijo, prefijo, posfijo y resultado)
 * - Lista consolidada de todos los cuádruplos del programa en orden
 * - Entorno de valores final tras la evaluación secuencial
 */
public record ProgramaIntermedio(
        List<ResultadoExpresion> expresiones,
        List<Cuadruplo> cuadruplos,
        EntornoValores entorno
) {
    public boolean estaVacio() {
        return expresiones.isEmpty() && cuadruplos.isEmpty();
    }

    public int totalExpresiones() {
        return expresiones.size();
    }

    public int totalCuadruplos() {
        return cuadruplos.size();
    }
}
