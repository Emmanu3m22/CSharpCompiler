package com.compilador.errores;

/**
 * Representa un error sintáctico detectado durante el análisis.
 * Contiene información sobre el token encontrado, lo esperado, la línea y la columna.
 */
public class ErrorSintactico {

    private final String tokenEncontrado;
    private final String tokenEsperado;
    private final String mensaje;
    private final int linea;
    private final int columna;

    public ErrorSintactico(String tokenEncontrado, String tokenEsperado,
                            String mensaje, int linea, int columna) {
        this.tokenEncontrado = tokenEncontrado;
        this.tokenEsperado = tokenEsperado;
        this.mensaje = mensaje;
        this.linea = linea;
        this.columna = columna;
    }

    public String getTokenEncontrado() {
        return tokenEncontrado;
    }

    public String getTokenEsperado() {
        return tokenEsperado;
    }

    public String getMensaje() {
        return mensaje;
    }

    public int getLinea() {
        return linea;
    }

    public int getColumna() {
        return columna;
    }

    @Override
    public String toString() {
        return "Error Sintáctico en línea " + linea + ", columna " + columna
                + ": se encontró '" + tokenEncontrado + "'"
                + (tokenEsperado != null ? ", se esperaba '" + tokenEsperado + "'" : "")
                + " - " + mensaje;
    }
}
