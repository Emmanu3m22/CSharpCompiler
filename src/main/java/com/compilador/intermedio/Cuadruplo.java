package com.compilador.intermedio;

/**
 * Cuádruplo de código intermedio: (operador, arg1, arg2, resultado).
 *
 * Operadores soportados:
 * <ul>
 *   <li>Binarios: {@code + - * / %} → {@code resultado = arg1 op arg2}</li>
 *   <li>Unario {@value #NEGACION} → {@code resultado = -arg1} (arg2 es null)</li>
 *   <li>Asignación {@value #ASIGNACION} → {@code resultado = arg1} (arg2 es null)</li>
 * </ul>
 *
 * Es inmutable: los pases de optimización producen cuádruplos nuevos en lugar
 * de modificar los existentes.
 */
public record Cuadruplo(String operador, Operando arg1, Operando arg2, Operando resultado) {

    public static final String ASIGNACION = "=";
    public static final String NEGACION = "neg";

    /** Texto usado en tablas/consola para un argumento vacío. */
    public static final String VACIO = "—";

    public static Cuadruplo binario(String operador, Operando arg1, Operando arg2, Operando resultado) {
        return new Cuadruplo(operador, arg1, arg2, resultado);
    }

    public static Cuadruplo negacion(Operando arg1, Operando resultado) {
        return new Cuadruplo(NEGACION, arg1, null, resultado);
    }

    public static Cuadruplo asignacion(Operando valor, Operando destino) {
        return new Cuadruplo(ASIGNACION, valor, null, destino);
    }

    public boolean esAsignacion() {
        return ASIGNACION.equals(operador);
    }

    public boolean esNegacion() {
        return NEGACION.equals(operador);
    }

    public boolean esBinario() {
        return !esAsignacion() && !esNegacion();
    }

    public String arg1Texto() {
        return arg1 == null ? VACIO : arg1.texto();
    }

    public String arg2Texto() {
        return arg2 == null ? VACIO : arg2.texto();
    }

    public String resultadoTexto() {
        return resultado == null ? VACIO : resultado.texto();
    }

    @Override
    public String toString() {
        return "(" + operador + ", " + arg1Texto() + ", " + arg2Texto() + ", " + resultadoTexto() + ")";
    }
}
