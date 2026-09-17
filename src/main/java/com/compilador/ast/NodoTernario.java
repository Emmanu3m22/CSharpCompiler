package com.compilador.ast;

/**
 * Representa una expresión condicional (operador ternario).
 * condicion ? verdadero : falso
 */
public class NodoTernario extends Nodo {

    private Nodo condicion;
    private Nodo verdadero;
    private Nodo falso;

    public NodoTernario(Nodo condicion, Nodo verdadero, Nodo falso, int linea, int columna) {
        super(linea, columna);
        this.condicion = condicion;
        this.verdadero = verdadero;
        this.falso = falso;
    }

    public Nodo getCondicion() {
        return condicion;
    }

    public Nodo getVerdadero() {
        return verdadero;
    }

    public Nodo getFalso() {
        return falso;
    }

    @Override
    public String toString() {
        return "Ternario(" + condicion + " ? " + verdadero + " : " + falso + ")";
    }
}
