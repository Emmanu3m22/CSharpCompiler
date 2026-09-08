package com.compilador.ast;

/**
 * Nodo del AST que representa un ciclo while.
 * Ejemplo: while (x < 10) { x = x + 1; }
 */
public class NodoWhile extends Nodo {

    /** Expresión de condición del ciclo */
    private final Nodo condicion;

    /** Cuerpo del ciclo (bloque de sentencias) */
    private final Nodo cuerpo;

    public NodoWhile(Nodo condicion, Nodo cuerpo, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.cuerpo = cuerpo;
    }

    public Nodo getCondicion() {
        return condicion;
    }

    public Nodo getCuerpo() {
        return cuerpo;
    }

    @Override
    public String toString() {
        return "While(" + condicion + ", " + cuerpo + ")";
    }
}
