package com.compilador.ast;

public class NodoEstructura extends Nodo {
    private final String tipoEstructura; // "using", "namespace", "class", "interface"
    private final String identificador;
    private final NodoBloque cuerpo;

    public NodoEstructura(String tipoEstructura, String identificador, int linea, int columna) {
        this(tipoEstructura, identificador, null, linea, columna);
    }

    public NodoEstructura(String tipoEstructura, String identificador, NodoBloque cuerpo,
                          int linea, int columna) {
        super(linea, columna);
        this.tipoEstructura = tipoEstructura;
        this.identificador = identificador;
        this.cuerpo = cuerpo;
    }

    public String getTipoEstructura() {
        return tipoEstructura;
    }

    public String getIdentificador() {
        return identificador;
    }

    public NodoBloque getCuerpo() {
        return cuerpo;
    }

    @Override
    public String toString() {
        if (identificador != null && !identificador.isEmpty()) {
            return "Estructura(" + tipoEstructura + " " + identificador + ")";
        }
        return "Estructura(" + tipoEstructura + ")";
    }

    @Override
    public <T> T accept(NodoVisitor<T> visitor) {
        return visitor.visitar(this);
    }
}
