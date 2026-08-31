package com.compilador.semantic;

/**
 * Representa una entrada en la tabla de símbolos.
 * Almacena información sobre una variable o identificador declarado.
 */
public class Simbolo {

    private final String nombre;
    private final TipoDato tipo;
    private final int lineaDeclaracion;
    private final int columnaDeclaracion;
    private String ambito;
    private boolean inicializado;

    public Simbolo(String nombre, TipoDato tipo, int lineaDeclaracion, int columnaDeclaracion) {
        this.nombre = nombre;
        this.tipo = tipo;
        this.lineaDeclaracion = lineaDeclaracion;
        this.columnaDeclaracion = columnaDeclaracion;
        this.ambito = "global";
        this.inicializado = false;
    }

    public String getNombre() {
        return nombre;
    }

    public TipoDato getTipo() {
        return tipo;
    }

    public int getLineaDeclaracion() {
        return lineaDeclaracion;
    }

    public int getColumnaDeclaracion() {
        return columnaDeclaracion;
    }

    public String getAmbito() {
        return ambito;
    }

    public void setAmbito(String ambito) {
        this.ambito = ambito;
    }

    public boolean isInicializado() {
        return inicializado;
    }

    public void setInicializado(boolean inicializado) {
        this.inicializado = inicializado;
    }

    @Override
    public String toString() {
        return "Simbolo{" +
                "nombre='" + nombre + '\'' +
                ", tipo=" + tipo +
                ", linea=" + lineaDeclaracion +
                ", ambito='" + ambito + '\'' +
                ", inicializado=" + inicializado +
                '}';
    }
}
