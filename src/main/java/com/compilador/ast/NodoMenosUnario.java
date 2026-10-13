package com.compilador.ast;

/**
 * Nodo del AST que representa el operador menos unario aplicado a una expresión.
 * Ejemplo: -a, -(b + c)
 *
 * Nota: el menos aplicado directamente a un literal (-5) se sigue representando
 * como un NodoNumero negativo para conservar el comportamiento existente.
 */
public class NodoMenosUnario extends Nodo {

    /** Expresión que se niega aritméticamente */
    private final Nodo expresion;

    public NodoMenosUnario(Nodo expresion, int linea, int columna) {
        super(linea, columna);
        this.expresion = expresion;
    }

    public Nodo getExpresion() {
        return expresion;
    }

    @Override
    public <T> T accept(NodoVisitor<T> visitor) {
        return visitor.visitar(this);
    }

    @Override
    public String toString() {
        return "MenosUnario(" + expresion + ")";
    }
}
