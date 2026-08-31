package com.compilador.errores;

/**
 * Representa un error léxico detectado durante el análisis.
 * Contiene información sobre el carácter inválido, la línea y la columna.
 */
public class ErrorLexico {

    private final String lexema;
    private final String mensaje;
    private final int linea;
    private final int columna;

    public ErrorLexico(String lexema, String mensaje, int linea, int columna) {
        this.lexema = lexema;
        this.mensaje = mensaje;
        this.linea = linea;
        this.columna = columna;
    }

    public String getLexema() {
        return lexema;
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
        return "Error Léxico en línea " + linea + ", columna " + columna
                + ": '" + lexema + "' - " + mensaje;
    }
}
