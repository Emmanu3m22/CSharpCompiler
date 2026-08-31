package com.compilador.ast;

/**
 * Nodo del AST que representa una expresión entre paréntesis (agrupación).
 * Ejemplo: (5 + 3) en la expresión x = (5 + 3) * 2
 */
public class NodoAgrupacion extends Nodo {

    /** La expresión contenida dentro de los paréntesis */
    private final Nodo expresion;

    public NodoAgrupacion(Nodo expresion, int linea, int columna) {
        super(linea, columna);
        this.expresion = expresion;
    }

    public Nodo getExpresion() {
        return expresion;
    }

    @Override
    public String toString() {
        return "Agrupacion(" + expresion + ")";
    }
}
