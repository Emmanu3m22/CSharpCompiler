package com.compilador.ast;

import java.util.List;

/**
 * Nodo del AST que representa un caso dentro de un switch.
 * Puede ser un "case valor:" o un "default:".
 *
 * Ejemplo:
 *   case 1:
 *       Console.WriteLine("uno");
 *       break;
 *
 *   default:
 *       Console.WriteLine("otro");
 *       break;
 */
public class NodoCaso extends Nodo {

    /** Expresión del caso (null si es default) */
    private final Nodo valor;

    /** Si es true, este caso es el "default" */
    private final boolean esDefault;

    /** Lista de sentencias dentro del caso */
    private final List<Nodo> sentencias;

    public NodoCaso(Nodo valor, boolean esDefault, List<Nodo> sentencias, int linea, int columna) {
        super(linea, columna);
        this.valor = valor;
        this.esDefault = esDefault;
        this.sentencias = sentencias;
    }

    public Nodo getValor() {
        return valor;
    }

    public boolean isEsDefault() {
        return esDefault;
    }

    public List<Nodo> getSentencias() {
        return sentencias;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        if (esDefault) {
            sb.append("Default(");
        } else {
            sb.append("Case(").append(valor).append(", ");
        }
        sb.append("[");
        for (int i = 0; i < sentencias.size(); i++) {
            if (i > 0) sb.append(", ");
            sb.append(sentencias.get(i));
        }
        sb.append("])");
        return sb.toString();
    }
}
