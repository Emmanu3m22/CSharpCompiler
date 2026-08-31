package com.compilador.ast;

/**
 * Nodo del AST que representa una operación aritmética binaria.
 * Ejemplo: 5 + 3, x * y, a - b
 */
public class NodoOperacion extends Nodo {

    /** Operando izquierdo */
    private final Nodo izquierdo;

    /** Operador (+, -, *, /) */
    private final String operador;

    /** Operando derecho */
    private final Nodo derecho;

    public NodoOperacion(Nodo izquierdo, String operador, Nodo derecho, int linea, int columna) {
        super(linea, columna);
        this.izquierdo = izquierdo;
        this.operador = operador;
        this.derecho = derecho;
    }

    public Nodo getIzquierdo() {
        return izquierdo;
    }

    public String getOperador() {
        return operador;
    }

    public Nodo getDerecho() {
        return derecho;
    }

    @Override
    public String toString() {
        return "Operacion(" + izquierdo + " " + operador + " " + derecho + ")";
    }
}
