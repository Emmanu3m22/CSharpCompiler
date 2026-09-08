package com.compilador.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo del AST que representa una estructura switch.
 * Ejemplo:
 *   switch (x) {
 *       case 1: ... break;
 *       case 2: ... break;
 *       default: ... break;
 *   }
 */
public class NodoSwitch extends Nodo {

    /** Expresión que se evalúa en el switch */
    private final Nodo expresion;

    /** Lista de casos (case y default) */
    private final List<NodoCaso> casos;

    public NodoSwitch(Nodo expresion, List<NodoCaso> casos, int linea, int columna) {
        super(linea, columna);
        this.expresion = expresion;
        this.casos = casos;
    }

    public Nodo getExpresion() {
        return expresion;
    }

    public List<NodoCaso> getCasos() {
        return casos;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Switch(");
        sb.append(expresion).append(", [\n");
        for (NodoCaso caso : casos) {
            sb.append("  ").append(caso.toString()).append("\n");
        }
        sb.append("])");
        return sb.toString();
    }
}
