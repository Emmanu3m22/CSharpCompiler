package com.compilador.gui;

/**
 * Códigos de ejemplo precargados para que el usuario pueda probar
 * el compilador sin tener que escribir código desde cero.
 *
 * Cada ejemplo tiene un nombre descriptivo y el código fuente.
 */
public final class Ejemplos {

    private Ejemplos() {} // No instanciable

    /** Nombres de los ejemplos disponibles */
    public static final String[] NOMBRES = {
        "Hola Mundo",
        "Variables y operaciones",
        "Expresiones complejas",
        "Errores de ejemplo",
        "Programa vacío"
    };

    /** Código fuente de cada ejemplo (misma posición que NOMBRES) */
    public static final String[] CODIGOS = {
        // ── Hola Mundo ──
            """
            // Programa básico: Hola Mundo
            string saludo = "Hola mundo";
            Console.WriteLine(saludo);
            """,

        // ── Variables y operaciones ──
        "// Declaraciones con diferentes tipos\n" +
        "int x = 10;\n" +
        "int y = 20;\n" +
        "float resultado = 0.0;\n" +
        "\n" +
        "// Operación aritmética\n" +
        "resultado = x + y * 2;\n" +
        "\n" +
        "// Salida por consola\n" +
        "Console.WriteLine(resultado);\n",

        // ── Expresiones complejas ──
        "// Operaciones con paréntesis y múltiples tipos\n" +
        "int a = 5;\n" +
        "int b = 3;\n" +
        "int c = (a + b) * 2;\n" +
        "double d = 3.14;\n" +
        "\n" +
        "// Booleanos\n" +
        "bool activo = true;\n" +
        "bool inactivo = false;\n" +
        "\n" +
        "// Cadena con caracteres especiales\n" +
        "string msg = \"Resultado: \";\n" +
        "Console.WriteLine(msg);\n" +
        "Console.WriteLine(c);\n",

        // ── Errores de ejemplo ──
        "// Este código tiene errores intencionales\n" +
        "int x = 10;\n" +
        "int x = 20;\n" +            // Redeclaración
        "y = 5;\n" +                  // Variable no declarada
        "Console.WriteLine(x);\n",

        // ── Programa vacío ──
        "// Programa sin sentencias\n"
    };

    /**
     * Retorna el número de ejemplos disponibles.
     */
    public static int cantidad() {
        return NOMBRES.length;
    }

    /**
     * Retorna el código del ejemplo en la posición indicada.
     */
    public static String getCodigo(int indice) {
        if (indice >= 0 && indice < CODIGOS.length) {
            return CODIGOS[indice];
        }
        return "";
    }

    /**
     * Retorna el nombre del ejemplo en la posición indicada.
     */
    public static String getNombre(int indice) {
        if (indice >= 0 && indice < NOMBRES.length) {
            return NOMBRES[indice];
        }
        return "Desconocido";
    }
}
