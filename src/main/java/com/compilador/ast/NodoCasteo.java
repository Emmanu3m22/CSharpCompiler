package com.compilador.ast;

public class NodoCasteo extends Nodo {
    private final String tipo;
    private final Nodo expresion;

    public NodoCasteo(String tipo, Nodo expresion, int linea, int columna) {
        super(linea, columna);
        this.tipo = tipo;
        this.expresion = expresion;
    }

    public String getTipo() {
        return tipo;
    }

    public Nodo getExpresion() {
        return expresion;
    }

    @Override
    public String toString() {
        return "Casteo((" + tipo + ") " + expresion + ")";
    }

    @Override
    public <T> T accept(NodoVisitor<T> visitor) {
        return visitor.visitar(this);
    }
}
