package com.compilador.ast;

/**
 * Nodo que representa la creación de un arreglo con tamaño fijo.
 */
public class NodoArreglo extends Nodo {

    private final String tipoElemento;
    private final String tamaño;

    public NodoArreglo(String tipoElemento, String tamaño, int linea, int columna) {
        super(linea, columna);
        this.tipoElemento = tipoElemento;
        this.tamaño = tamaño;
    }

    public String getTipoElemento() {
        return tipoElemento;
    }

    public String getTamaño() {
        return tamaño;
    }

    @Override
    public String toString() {
        return "Arreglo(" + tipoElemento + ", tamaño=" + tamaño + ")";
    }
}