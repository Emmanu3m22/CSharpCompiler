package com.compilador.ast;

/**
 * Nodo del AST que representa un identificador (nombre de variable, función, etc.).
 * Ejemplo: x, miVariable, contador
 */
public class NodoIdentificador extends Nodo {

    /** Nombre del identificador */
    private final String nombre;

    public NodoIdentificador(String nombre, int linea, int columna) {
        super(linea, columna);
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    @Override
    public String toString() {
        return "Identificador(" + nombre + ")";
    }
}
