package com.compilador.ast;

/**
 * Nodo del AST que representa un literal booleano.
 * Ejemplo: true, false
 */
public class NodoBooleano extends Nodo {

    /** Valor booleano */
    private final boolean valor;

    public NodoBooleano(boolean valor, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
    }

    public boolean getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return "Booleano(" + valor + ")";
    }
}
