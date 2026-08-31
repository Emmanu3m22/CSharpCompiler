package com.compilador.semantic;

/**
 * Enumeración de los tipos de datos que maneja el lenguaje.
 * Se usará en la tabla de símbolos y en la validación semántica.
 */
public enum TipoDato {
    INT("int"),
    FLOAT("float"),
    DOUBLE("double"),
    BOOL("bool"),
    STRING("string"),
    CHAR("char"),
    VOID("void"),
    DESCONOCIDO("desconocido");

    private final String nombre;

    TipoDato(String nombre) {
        this.nombre = nombre;
    }

    public String getNombre() {
        return nombre;
    }

    /**
     * Convierte un string del código fuente al tipo de dato correspondiente.
     */
    public static TipoDato desdeString(String tipo) {
        for (TipoDato t : values()) {
            if (t.nombre.equalsIgnoreCase(tipo)) {
                return t;
            }
        }
        return DESCONOCIDO;
    }

    @Override
    public String toString() {
        return nombre;
    }
}
