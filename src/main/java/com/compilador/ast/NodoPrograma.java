package com.compilador.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Nodo raíz del AST que representa un programa completo.
 * Contiene una lista de sentencias (declaraciones, asignaciones, comandos, etc.).
 */
public class NodoPrograma extends Nodo {

    private final List<Nodo> sentencias;

    public NodoPrograma(int linea, int columna) {
        super(linea, columna);
        this.sentencias = new ArrayList<>();
    }

    public NodoPrograma() {
        this(0, 0);
    }

    public void agregarSentencia(Nodo sentencia) {
        sentencias.add(sentencia);
    }

    public List<Nodo> getSentencias() {
        return sentencias;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Programa[\n");
        for (Nodo s : sentencias) {
            sb.append("  ").append(s.toString()).append("\n");
        }
        sb.append("]");
        return sb.toString();
    }
}
