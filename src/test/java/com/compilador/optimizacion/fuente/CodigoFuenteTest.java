package com.compilador.optimizacion.fuente;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class CodigoFuenteTest {

    @Test
    @DisplayName("Delimita sentencia simple con punto y coma")
    void testSentenciaSimple() {
        String codigo = "int x = 10 + 20;\nConsole.WriteLine(x);";
        CodigoFuente cf = new CodigoFuente(codigo);

        int idx = cf.buscarIndiceToken(1, 1);
        assertEquals(0, idx);

        RangoTexto rango = cf.obtenerRangoSentencia(idx);
        assertNotNull(rango);

        String extraido = codigo.substring(rango.inicio(), rango.fin()).trim();
        assertEquals("int x = 10 + 20;", extraido);
    }

    @Test
    @DisplayName("Ignora punto y coma dentro de literales de cadena")
    void testPuntoYComaEnCadena() {
        String codigo = "string s = \"hola; mundo;\";\nConsole.WriteLine(s);";
        CodigoFuente cf = new CodigoFuente(codigo);

        int idx = cf.buscarIndiceToken(1, 1);
        RangoTexto rango = cf.obtenerRangoSentencia(idx);
        assertNotNull(rango);

        String extraido = codigo.substring(rango.inicio(), rango.fin()).trim();
        assertEquals("string s = \"hola; mundo;\";", extraido);
    }

    @Test
    @DisplayName("Delimita sentencia if con else completo")
    void testDelimitarIfElse() {
        String codigo = "if (x > 0) {\n    Console.WriteLine(1);\n} else {\n    Console.WriteLine(2);\n}\nint z = 3;";
        CodigoFuente cf = new CodigoFuente(codigo);

        int idxIf = cf.buscarIndiceToken(1, 1);
        RangoTexto rango = cf.obtenerRangoSentencia(idxIf);
        assertNotNull(rango);

        String colapsado = cf.colapsar(List.of(rango));
        assertFalse(colapsado.contains("if (x > 0)"));
        assertFalse(colapsado.contains("else"));
        assertTrue(colapsado.contains("int z = 3;"));
    }

    @Test
    @DisplayName("Enmascarado reemplaza con espacios conservando saltos de línea")
    void testEnmascaradoPreservaLineas() {
        String codigo = "int a = 1;\nint b = 2;\nint c = 3;";
        CodigoFuente cf = new CodigoFuente(codigo);

        // Enmascarar la línea 2 ("int b = 2;\n")
        int idxB = cf.buscarIndiceToken(2, 1);
        RangoTexto rB = cf.obtenerRangoSentencia(idxB);
        assertNotNull(rB);

        String enmascarado = cf.enmascarar(List.of(rB));
        String[] lineas = enmascarado.split("\r?\n");
        assertEquals(3, lineas.length);
        assertEquals("int a = 1;", lineas[0].trim());
        assertEquals("", lineas[1].trim()); // queda vacía (solo espacios)
        assertEquals("int c = 3;", lineas[2].trim());
    }

    @Test
    @DisplayName("Quitar comentarios de una línea preservando la instrucción previa")
    void testQuitarComentariosLinea() {
        String codigo = """
                // Encabezado
                int x = 10; // asigna diez
                int y = 20;
                """;
        String limpio = CodigoFuente.quitarComentarios(codigo);
        assertFalse(limpio.contains("//"));
        assertTrue(limpio.contains("int x = 10;"));
        assertTrue(limpio.contains("int y = 20;"));
    }

    @Test
    @DisplayName("Quitar comentarios multilínea de bloque")
    void testQuitarComentariosBloque() {
        String codigo = """
                /*
                 * Bloque explicativo
                 */
                int z = /* inline */ 30;
                """;
        String limpio = CodigoFuente.quitarComentarios(codigo);
        assertFalse(limpio.contains("/*"));
        assertFalse(limpio.contains("*/"));
        assertTrue(limpio.contains("int z = 30;"));
    }

    @Test
    @DisplayName("Preserva secuencias de comentarios dentro de cadenas y verbatim")
    void testPreservarComentariosEnLiterales() {
        String codigo = """
                string s1 = "hola // no es comentario /* tampoco */";
                string s2 = @"verbatim // con salto
                y comillas";
                char c = '/';
                """;
        String limpio = CodigoFuente.quitarComentarios(codigo);
        assertTrue(limpio.contains("\"hola // no es comentario /* tampoco */\""));
        assertTrue(limpio.contains("@\"verbatim // con salto"));
        assertTrue(limpio.contains("char c = '/';"));
    }

    @Test
    @DisplayName("colapsar() elimina rangos y comentarios simultáneamente")
    void testColapsarRetiraComentarios() {
        String codigo = """
                // Comentario de inicio
                int a = 1;
                int muerto = 2; // se eliminara
                int b = 3;
                """;
        CodigoFuente cf = new CodigoFuente(codigo);
        int idxMuerto = cf.buscarIndiceToken(3, 1);
        RangoTexto rango = cf.obtenerRangoSentencia(idxMuerto);

        String colapsado = cf.colapsar(List.of(rango));
        assertFalse(colapsado.contains("//"));
        assertFalse(colapsado.contains("int muerto = 2;"));
        assertTrue(colapsado.contains("int a = 1;"));
        assertTrue(colapsado.contains("int b = 3;"));
    }
}
