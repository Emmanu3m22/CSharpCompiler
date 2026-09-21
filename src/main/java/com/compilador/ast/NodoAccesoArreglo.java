package com.compilador.ast;

public class NodoAccesoArreglo extends Nodo {
    private final String identificador;
    private final Nodo indice;

    public NodoAccesoArreglo(String identificador, Nodo indice, int linea, int columna) {
        super(linea, columna);
        this.identificador = identificador;
        this.indice = indice;
    }

    public String getIdentificador() {
        return identificador;
    }

    public Nodo getIndice() {
        return indice;
    }

    @Override
    public String toString() {
        return "AccesoArreglo(" + identificador + "[" + indice + "])";
    }
}
