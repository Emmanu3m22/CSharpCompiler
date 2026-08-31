package com.compilador.ast;

/**
 * Nodo del AST que representa un comando/instrucción especial.
 * Ejemplo: Console.WriteLine("Hola"); o return x;
 */
public class NodoComando extends Nodo {

    /** Nombre del comando (ej: "Console.WriteLine", "return") */
    private final String comando;

    /** Argumento del comando (puede ser null) */
    private final Nodo argumento;

    public NodoComando(String comando, Nodo argumento, int linea, int columna) {
        super(linea, columna);
        this.comando = comando;
        this.argumento = argumento;
    }

    public String getComando() {
        return comando;
    }

    public Nodo getArgumento() {
        return argumento;
    }

    @Override
    public String toString() {
        if (argumento != null) {
            return "Comando(" + comando + ", " + argumento + ")";
        }
        return "Comando(" + comando + ")";
    }
}
