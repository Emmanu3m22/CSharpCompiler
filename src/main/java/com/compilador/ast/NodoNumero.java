package com.compilador.ast;

/**
 * Nodo del AST que representa un literal numérico (entero o decimal).
 * Ejemplo: 42, 3.14
 */
public class NodoNumero extends Nodo {

    /** Valor del número como string (para preservar precisión) */
    private final String valor;

    public NodoNumero(String valor, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
    }

    public String getValor() {
        return valor;
    }

    /**
     * Retorna true si el número contiene punto decimal.
     */
    public boolean esDecimal() {
        return valor.contains(".");
    }

    @Override
    public String toString() {
        return "Numero(" + valor + ")";
    }
}
