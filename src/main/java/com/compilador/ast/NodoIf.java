package com.compilador.ast;

/**
 * Nodo del AST que representa una estructura condicional if/else.
 * Ejemplo: if (x > 5) { ... } else { ... }
 */
public class NodoIf extends Nodo {

    /** Expresión de condición */
    private final Nodo condicion;

    /** Bloque que se ejecuta si la condición es verdadera */
    private final Nodo bloqueThen;

    /** Bloque que se ejecuta si la condición es falsa (puede ser null) */
    private final Nodo bloqueElse;

    public NodoIf(Nodo condicion, Nodo bloqueThen, Nodo bloqueElse, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.bloqueThen = bloqueThen;
        this.bloqueElse = bloqueElse;
    }

    public Nodo getCondicion() {
        return condicion;
    }

    public Nodo getBloqueThen() {
        return bloqueThen;
    }

    public Nodo getBloqueElse() {
        return bloqueElse;
    }

    @Override
    public String toString() {
        if (bloqueElse != null) {
            return "If(" + condicion + ", " + bloqueThen + ", Else(" + bloqueElse + "))";
        }
        return "If(" + condicion + ", " + bloqueThen + ")";
    }
}
