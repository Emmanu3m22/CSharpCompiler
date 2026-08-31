package com.compilador.ast;

/**
 * Nodo del AST que representa una operación relacional.
 * Ejemplo: x > 5, a == b, c != d
 */
public class NodoOperacionRelacional extends Nodo {

    /** Operando izquierdo */
    private final Nodo izquierdo;

    /** Operador relacional (==, !=, <, >, <=, >=) */
    private final String operador;

    /** Operando derecho */
    private final Nodo derecho;

    public NodoOperacionRelacional(Nodo izquierdo, String operador, Nodo derecho,
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
        return "Relacional(" + izquierdo + " " + operador + " " + derecho + ")";
    }
}
