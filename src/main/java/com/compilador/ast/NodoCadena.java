package com.compilador.ast;

/**
 * Nodo del AST que representa un literal de cadena de texto.
 * Ejemplo: "Hola mundo"
 */
public class NodoCadena extends Nodo {

    /** Valor de la cadena (sin las comillas) */
    private final String valor;

    public NodoCadena(String valor, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    @Override
    public String toString() {
        return "Cadena(\"" + valor + "\")";
    }
}
