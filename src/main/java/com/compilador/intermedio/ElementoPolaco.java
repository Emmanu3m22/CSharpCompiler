package com.compilador.intermedio;

/**
 * Elemento de una expresión en notación polaca (prefija o posfija).
 *
 * Se guarda como lista de elementos tipados —y no solo como texto— para que el
 * evaluador con pila pueda distinguir el operador "-" del literal "-5", o un
 * átomo complejo como "Math.Max(a, b)" (que contiene espacios).
 */
public record ElementoPolaco(String texto, Clase clase) {

    public enum Clase {
        OPERADOR_BINARIO,
        OPERADOR_UNARIO,
        CONSTANTE,
        VARIABLE,
        EXTERNO
    }

    public boolean esOperador() {
        return clase == Clase.OPERADOR_BINARIO || clase == Clase.OPERADOR_UNARIO;
    }

    @Override
    public String toString() {
        return texto;
    }
}
