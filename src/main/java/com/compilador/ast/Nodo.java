package com.compilador.ast;

/**
 * Clase base abstracta para todos los nodos del Árbol Sintáctico Abstracto (AST).
 * Cada nodo guarda la línea y columna donde apareció en el código fuente,
 * para poder reportar errores con ubicación precisa.
 */
public abstract class Nodo {

    /** Línea en el código fuente donde inicia este nodo */
    private int linea;

    /** Columna en el código fuente donde inicia este nodo */
    private int columna;

    public Nodo() {
        this.linea = 0;
        this.columna = 0;
    }

    public Nodo(int linea, int columna) {
        this.linea = linea;
        this.columna = columna;
    }

    public int getLinea() {
        return linea;
    }

    public void setLinea(int linea) {
        this.linea = linea;
    }

    public int getColumna() {
        return columna;
    }

    public void setColumna(int columna) {
        this.columna = columna;
    }

    /**
     * Método para aceptar un visitante (patrón Visitor).
     * Será usado por el AnalizadorSemantico para recorrer el AST.
     */
    public abstract String toString();
}
