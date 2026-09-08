package com.compilador.ast;

/**
 * Nodo del AST que representa un ciclo do-while.
 * Ejemplo: do { x = x + 1; } while (x < 10);
 */
public class NodoDoWhile extends Nodo {

    /** Cuerpo del ciclo (bloque de sentencias) */
    private final Nodo cuerpo;

    /** Expresión de condición del ciclo */
    private final Nodo condicion;

    public NodoDoWhile(Nodo cuerpo, Nodo condicion, int linea, int columna) {
        super(linea, columna);
        this.cuerpo = cuerpo;
        this.condicion = condicion;
    }

    public Nodo getCuerpo() {
        return cuerpo;
    }

    public Nodo getCondicion() {
        return condicion;
    }

    @Override
    public String toString() {
        return "DoWhile(" + cuerpo + ", " + condicion + ")";
    }
}
