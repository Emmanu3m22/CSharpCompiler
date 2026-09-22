package com.compilador.ast;

import java.util.List;

public class NodoLlamadaMetodo extends Nodo {
    private final String identificador;
    private final List<Nodo> argumentos;

    public NodoLlamadaMetodo(String identificador, List<Nodo> argumentos, int linea, int columna) {
        super(linea, columna);
        this.identificador = identificador;
        this.argumentos = argumentos;
    }

    public String getIdentificador() {
        return identificador;
    }

    public List<Nodo> getArgumentos() {
        return argumentos;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("LlamadaMetodo(").append(identificador).append("(");
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

    @Override
    public <T> T accept(NodoVisitor<T> visitor) {
        return visitor.visitar(this);
    }
}
