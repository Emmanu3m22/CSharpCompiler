package com.compilador.semantic;

/**
 * Representa un error semántico detectado durante el análisis.
 * Ejemplos: variable no declarada, tipos incompatibles, variable ya declarada.
 */
public class ErrorSemantico {

    private final String mensaje;
    private final int linea;
    private final int columna;
    private final String tipoError;

    public ErrorSemantico(String mensaje, String tipoError, int linea, int columna) {
        this.mensaje = mensaje;
        this.tipoError = tipoError;
        this.linea = linea;
        this.columna = columna;
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

    public String getTipoError() {
        return tipoError;
    }

    @Override
    public String toString() {
        return "Error Semántico en línea " + linea + ", columna " + columna
                + " [" + tipoError + "]: " + mensaje;
    }
}
