package com.compilador.ast;

/**
 * Nodo del AST que representa una operación de incremento o decremento.
 * Ejemplo: x++, i--
 */
public class NodoIncremento extends Nodo {

    /** Nombre de la variable */
    private final String identificador;

    /** Operador (++ o --) */
    private final String operador;

    public NodoIncremento(String identificador, String operador, int linea, int columna) {
        super(linea, columna);
        this.identificador = identificador;
        this.operador = operador;
    }

    public String getIdentificador() {
        return identificador;
    }

    public String getOperador() {
        return operador;
    }

    @Override
    public String toString() {
        return "Incremento(" + identificador + operador + ")";
    }
}
