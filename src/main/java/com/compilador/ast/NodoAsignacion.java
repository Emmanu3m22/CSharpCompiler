package com.compilador.ast;

/**
 * Nodo del AST que representa una asignación: identificador = expresión;
 * Ejemplo: x = 5 + 3;
 */
public class NodoAsignacion extends Nodo {

    /** Nombre de la variable a la que se asigna */
    private final String identificador;

    /** Expresión cuyo valor se asigna */
    private final Nodo expresion;

    public NodoAsignacion(String identificador, Nodo expresion, int linea, int columna) {
        super(linea, columna);
        this.identificador = identificador;
        this.expresion = expresion;
    }

    public String getIdentificador() {
        return identificador;
    }

    public Nodo getExpresion() {
        return expresion;
    }

    @Override
    public String toString() {
        return "Asignacion(" + identificador + " = " + expresion + ")";
    }
}
