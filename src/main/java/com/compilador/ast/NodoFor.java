package com.compilador.ast;

/**
 * Nodo del AST que representa un ciclo for.
 * Ejemplo: for (int i = 0; i < 10; i++) { ... }
 */
public class NodoFor extends Nodo {

    /** Inicialización del ciclo (puede ser null) */
    private final Nodo inicializacion;

    /** Condición del ciclo (puede ser null) */
    private final Nodo condicion;

    /** Actualización del ciclo (puede ser null) */
    private final Nodo actualizacion;

    /** Cuerpo del ciclo (bloque de sentencias) */
    private final Nodo cuerpo;

    public NodoFor(Nodo inicializacion, Nodo condicion, Nodo actualizacion,
                   Nodo cuerpo, int linea, int columna) {
        super(linea, columna);
        this.inicializacion = inicializacion;
        this.condicion = condicion;
        this.actualizacion = actualizacion;
        this.cuerpo = cuerpo;
    }

    public Nodo getInicializacion() {
        return inicializacion;
    }

    public Nodo getCondicion() {
        return condicion;
    }

    public Nodo getActualizacion() {
        return actualizacion;
    }

    public Nodo getCuerpo() {
        return cuerpo;
    }

    @Override
    public String toString() {
        return "For(" + inicializacion + "; " + condicion + "; " + actualizacion + ", " + cuerpo + ")";
    }
}
