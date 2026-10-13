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
        "Polaca y Cuádruplos",
        "Hola Mundo",
        "Variables y operaciones",
        "Expresiones complejas",
        "Errores de ejemplo",
        "Programa vacío"
    };

    /** Código fuente de cada ejemplo (misma posición que NOMBRES) */
    public static final String[] CODIGOS = {
        // ── Polaca y Cuádruplos (Múltiples expresiones complejas) ──
        """
        // Múltiples expresiones aritméticas complejas
        int a = 5;
        int b = 3;
        int c = 4;
        int d = 10;
        double e = 2.5;

        // 1. Precedencia mixta con agrupación: (5+3)*4 - 10/2 = 27
        int r1 = (a + b) * c - d / 2;

        // 2. Anidamiento profundo y módulo: 5 * (3 + 4 * (10 - 5)) % 7 = 115 % 7 = 3
        int r2 = a * (b + c * (d - a)) % 7;

        // 3. Aritmética mixta con decimales: (5 + 2.5) * (4 - 3) / 2 = 3.75
        double r3 = (a + e) * (c - b) / 2;

        // 4. División entera compuesta: ((5 - 3) * (4 + 10)) / (3 + 1) = 28 / 4 = 7
        int r4 = ((a - b) * (c + d)) / (b + 1);

        // 5. Asignación compuesta sobre resultado previo: 27 + 3 * 2 = 33
        r1 += r2 * 2;

        Console.WriteLine(r1);
        Console.WriteLine(r2);
        Console.WriteLine(r3);
        Console.WriteLine(r4);
        """,

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
