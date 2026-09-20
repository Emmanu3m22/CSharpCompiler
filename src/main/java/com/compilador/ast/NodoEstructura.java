package com.compilador.ast;

public class NodoEstructura extends Nodo {
    private final String tipoEstructura; // "using", "namespace", "class", "interface"
    private final String identificador;

    public NodoEstructura(String tipoEstructura, String identificador, int linea, int columna) {
        super(linea, columna);
        this.tipoEstructura = tipoEstructura;
        this.identificador = identificador;
    }

    public String getTipoEstructura() {
        return tipoEstructura;
    }

    public String getIdentificador() {
        return identificador;
    }

    @Override
    public String toString() {
        if (identificador != null && !identificador.isEmpty()) {
            return "Estructura(" + tipoEstructura + " " + identificador + ")";
        }
        return "Estructura(" + tipoEstructura + ")";
    }
}
