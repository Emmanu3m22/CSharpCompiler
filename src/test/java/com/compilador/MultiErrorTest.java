package com.compilador;

import java.io.StringReader;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Test para verificar que múltiples errores sintácticos se detectan en un solo análisis.
 */
public class MultiErrorTest {

    @Test
    @DisplayName("Detecta múltiples errores sintácticos en sentencias separadas")
    void testMultiplesErroresSintacticos() throws Exception {
        // 3 sentencias con errores separados por punto y coma
        String codigo = "int = ;\n" +       // error 1: falta identificador
                         "float = ;\n" +      // error 2: falta identificador
                         "double = ;\n";      // error 3: falta identificador

        Analizador parser = new Analizador(new StringReader(codigo));
        parser.programa();

        System.out.println("Errores sintácticos encontrados: " + parser.getErroresSintacticos().size());
        for (var error : parser.getErroresSintacticos()) {
            System.out.println("  - " + error);
        }

        assertTrue(parser.tieneErrores(), "Debe tener errores");
        assertTrue(parser.getErroresSintacticos().size() >= 3,
            "Debe detectar al menos 3 errores, encontró: " + parser.getErroresSintacticos().size());
    }

    @Test
    @DisplayName("Detecta errores mixtos en distintas construcciones")
    void testErroresMixtos() throws Exception {
        String codigo = "int x = ;\n" +           // error: falta expresión
                         "if () { }\n" +            // error: falta condición en if
                         "int y = 5;\n";            // sentencia válida

        Analizador parser = new Analizador(new StringReader(codigo));
        parser.programa();

        System.out.println("Errores mixtos encontrados: " + parser.getErroresSintacticos().size());
        for (var error : parser.getErroresSintacticos()) {
            System.out.println("  - " + error);
        }

        assertTrue(parser.getErroresSintacticos().size() >= 2,
            "Debe detectar al menos 2 errores, encontró: " + parser.getErroresSintacticos().size());
    }

    @Test
    @DisplayName("Detecta errores dentro de bloques")
    void testErroresDentroDeBloques() throws Exception {
        String codigo = "if (true) {\n" +
                         "    int = ;\n" +          // error dentro del bloque
                         "    float = ;\n" +        // error dentro del bloque
                         "}\n";

        Analizador parser = new Analizador(new StringReader(codigo));
        parser.programa();

        System.out.println("Errores dentro de bloques: " + parser.getErroresSintacticos().size());
        for (var error : parser.getErroresSintacticos()) {
            System.out.println("  - " + error);
        }

        assertTrue(parser.getErroresSintacticos().size() >= 2,
            "Debe detectar al menos 2 errores dentro del bloque, encontró: " + parser.getErroresSintacticos().size());
    }

    @Test
    @DisplayName("Errores en diferentes tipos de sentencias se acumulan")
    void testErroresVariados() throws Exception {
        String codigo = "int = ;\n" +                // error: falta id en declaración
                         "x = ;\n" +                  // error: falta expresión en asignación
                         "Console.WriteLine();\n" +   // error: falta argumento
                         "int z = 5;\n";              // válida

        Analizador parser = new Analizador(new StringReader(codigo));
        parser.programa();

        System.out.println("Errores variados: " + parser.getErroresSintacticos().size());
        for (var error : parser.getErroresSintacticos()) {
            System.out.println("  - " + error);
        }

        assertTrue(parser.getErroresSintacticos().size() >= 2,
            "Debe detectar múltiples errores, encontró: " + parser.getErroresSintacticos().size());
    }

    @Test
    @DisplayName("Detecta los 3 errores específicos del usuario")
    void testErroresDelUsuario() throws Exception {
        String codigo = 
            "int cantidad = 5;\n" +
            "double precio = 12.5;\n" +
            "double total = cantidad * precio\n" + // ERROR 1: falta ;
            "if total > 50.0 )\n" +                // ERROR 2: falta (
            "{\n" +
            "    Console.WriteLine(\"Aplica descuento\");\n" +
            "else\n" +                             // ERROR 3: falta } antes del else
            "{\n" +
            "    Console.WriteLine(\"No aplica descuento\");\n" +
            "}\n";

        Analizador parser = new Analizador(new StringReader(codigo));
        parser.programa();

        System.out.println("Errores del usuario encontrados: " + parser.getErroresSintacticos().size());
        for (var error : parser.getErroresSintacticos()) {
            System.out.println("  - " + error);
        }

        assertTrue(parser.getErroresSintacticos().size() >= 3,
            "Debe detectar al menos 3 errores, encontró: " + parser.getErroresSintacticos().size());
    }
}
