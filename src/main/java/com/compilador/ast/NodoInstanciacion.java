package com.compilador.ast;

import java.util.List;

public class NodoInstanciacion extends Nodo {
    private final String tipo;
    private final List<Nodo> argumentos;
    private final Nodo indiceArreglo;
    private final boolean esArreglo;

    public NodoInstanciacion(String tipo, List<Nodo> argumentos, int linea, int columna) {
        super(linea, columna);
        this.tipo = tipo;
        this.argumentos = argumentos;
        this.esArreglo = false;
        this.indiceArreglo = null;
    }

    public NodoInstanciacion(String tipo, Nodo indiceArreglo, int linea, int columna) {
        super(linea, columna);
        this.tipo = tipo;
        this.indiceArreglo = indiceArreglo;
        this.esArreglo = true;
        this.argumentos = null;
    }

    public String getTipo() {
        return tipo;
    }

    public List<Nodo> getArgumentos() {
        return argumentos;
    }

    public Nodo getIndiceArreglo() {
        return indiceArreglo;
    }

    public boolean isEsArreglo() {
        return esArreglo;
    }

    @Override
    public String toString() {
        if (esArreglo) {
            return "NewArray(" + tipo + "[" + (indiceArreglo != null ? indiceArreglo : "") + "])";
        } else {
            StringBuilder sb = new StringBuilder();
            sb.append("New(").append(tipo).append("(");
            if (argumentos != null) {
                for (int i = 0; i < argumentos.size(); i++) {
                    sb.append(argumentos.get(i).toString());
                    if (i < argumentos.size() - 1) {
                        sb.append(", ");
                    }
                }
            }
            sb.append("))");
            return sb.toString();
        }
    }

    @Override
    public <T> T accept(NodoVisitor<T> visitor) {
        return visitor.visitar(this);
    }
}
