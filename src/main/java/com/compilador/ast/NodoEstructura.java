package com.compilador.ast;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NodoEstructura extends Nodo {
    private final String tipoEstructura; // "using", "namespace", "class", "interface"
    private final String identificador;
    private final NodoBloque cuerpo;
    private final List<ParametroMetodo> parametros;

    public NodoEstructura(String tipoEstructura, String identificador, int linea, int columna) {
        this(tipoEstructura, identificador, null, Collections.emptyList(), linea, columna);
    }

    public NodoEstructura(String tipoEstructura, String identificador, NodoBloque cuerpo,
                          int linea, int columna) {
        this(tipoEstructura, identificador, cuerpo, Collections.emptyList(), linea, columna);
    }

    public NodoEstructura(String tipoEstructura, String identificador, NodoBloque cuerpo,
                          List<ParametroMetodo> parametros, int linea, int columna) {
        super(linea, columna);
        this.tipoEstructura = tipoEstructura;
        this.identificador = identificador;
        this.cuerpo = cuerpo;
        this.parametros = parametros == null
                ? Collections.emptyList()
                : Collections.unmodifiableList(new ArrayList<>(parametros));
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

    public List<ParametroMetodo> getParametros() {
        return parametros;
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
