package com.compilador.ast;

/**
 * Nodo del AST que representa una asignación: identificador = expresión;
 * Ejemplo: x = 5 + 3;
 */
public class NodoAsignacion extends Nodo {

    /** Expresión que recibe el valor asignado. */
    private final Nodo destino;

    /** Expresión cuyo valor se asigna */
    private final Nodo expresion;

    public NodoAsignacion(Nodo destino, Nodo expresion, int linea, int columna) {
        super(linea, columna);
        this.destino = destino;
        this.expresion = expresion;
    }

    /** Constructor compatible con clientes que aún asignan a una variable simple. */
    public NodoAsignacion(String identificador, Nodo expresion, int linea, int columna) {
        this(new NodoIdentificador(identificador, linea, columna), expresion, linea, columna);
    }

    public Nodo getDestino() {
        return destino;
    }

    public String getIdentificador() {
        return destino instanceof NodoIdentificador
                ? ((NodoIdentificador) destino).getNombre()
                : null;
    }

    public Nodo getExpresion() {
        return expresion;
    }

    @Override
    public String toString() {
        return "Asignacion(" + destino + " = " + expresion + ")";
    }

    @Override
    public <T> T accept(NodoVisitor<T> visitor) {
        return visitor.visitar(this);
    }
}
