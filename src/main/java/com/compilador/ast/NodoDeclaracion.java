package com.compilador.ast;

/**
 * Nodo del AST que representa una declaración de variable.
 * Ejemplo: int x; o int x = 5;
 */
public class NodoDeclaracion extends Nodo {

    /** Tipo de dato declarado (int, float, bool, string, etc.) */
    private final String tipoDato;

    /** Nombre de la variable */
    private final String identificador;

    /** Expresión de inicialización (puede ser null si no se inicializa) */
    private final Nodo inicializacion;

    public NodoDeclaracion(String tipoDato, String identificador, Nodo inicializacion,
                           int linea, int columna) {
        super(linea, columna);
        this.tipoDato = tipoDato;
        this.identificador = identificador;
        this.inicializacion = inicializacion;
    }

    public String getTipoDato() {
        return tipoDato;
    }

    public String getIdentificador() {
        return identificador;
    }

    public Nodo getInicializacion() {
        return inicializacion;
    }

    @Override
    public String toString() {
        if (inicializacion != null) {
            return "Declaracion(" + tipoDato + " " + identificador + " = " + inicializacion + ")";
        }
        return "Declaracion(" + tipoDato + " " + identificador + ")";
    }
}
