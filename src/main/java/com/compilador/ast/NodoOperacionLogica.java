package com.compilador.ast;

/**
 * Nodo del AST que representa una operación lógica binaria.
 * Ejemplo: x > 5 && y < 10, a || b
 */
public class NodoOperacionLogica extends Nodo {

    /** Operando izquierdo */
    private final Nodo izquierdo;

    /** Operador lógico (&&, ||) */
    private final String operador;

    /** Operando derecho */
    private final Nodo derecho;

    public NodoOperacionLogica(Nodo izquierdo, String operador, Nodo derecho,
                                int linea, int columna) {
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
        return "Logica(" + izquierdo + " " + operador + " " + derecho + ")";
    }
}
