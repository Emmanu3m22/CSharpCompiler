package com.compilador.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo del AST que representa un bloque de código delimitado por llaves.
 * Ejemplo: { int x = 5; x = x + 1; }
 */
public class NodoBloque extends Nodo {

    /** Lista de sentencias dentro del bloque */
    private final List<Nodo> sentencias;

    public NodoBloque(int linea, int columna) {
        super(linea, columna);
        this.sentencias = new ArrayList<>();
    }

    public void agregarSentencia(Nodo sentencia) {
        sentencias.add(sentencia);
    }

    public List<Nodo> getSentencias() {
        return sentencias;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Bloque[\n");
        for (Nodo s : sentencias) {
            sb.append("  ").append(s.toString()).append("\n");
        }
        sb.append("]");
        return sb.toString();
    }
}
