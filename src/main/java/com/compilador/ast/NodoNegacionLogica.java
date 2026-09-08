package com.compilador.ast;

/**
 * Nodo del AST que representa una negación lógica.
 * Ejemplo: !activo, !(x > 5)
 */
public class NodoNegacionLogica extends Nodo {

    /** La expresión que se niega */
    private final Nodo expresion;

    public NodoNegacionLogica(Nodo expresion, int linea, int columna) {
        super(linea, columna);
        this.expresion = expresion;
    }

    public Nodo getExpresion() {
        return expresion;
    }

    @Override
    public String toString() {
        return "Negacion(!" + expresion + ")";
    }
}
