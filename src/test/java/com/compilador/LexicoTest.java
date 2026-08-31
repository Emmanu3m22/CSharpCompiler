package com.compilador;

import com.compilador.ast.*;
import com.compilador.errores.*;

import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del analizador léxico.
 * Verifica que los tokens se reconozcan correctamente.
 */
public class LexicoTest {

    /**
     * Helper para crear un parser a partir de un string de código fuente.
     */
    private Analizador crearParser(String codigo) {
        return new Analizador(new StringReader(codigo));
    }

    @Test
    @DisplayName("Reconoce números enteros")
    void testNumeroEntero() throws Exception {
        Analizador parser = crearParser("42;");
        // Obtener el primer token
        Token t = parser.getNextToken();
        assertEquals("42", t.image);
    }

    @Test
    @DisplayName("Reconoce números decimales")
    void testNumeroDecimal() throws Exception {
        Analizador parser = crearParser("3.14;");
        Token t = parser.getNextToken();
        assertEquals("3.14", t.image);
    }

    @Test
    @DisplayName("Reconoce identificadores")
    void testIdentificador() throws Exception {
        Analizador parser = crearParser("miVariable;");
        Token t = parser.getNextToken();
        assertEquals("miVariable", t.image);
    }

    @Test
    @DisplayName("Reconoce palabras reservadas int")
    void testPalabraReservadaInt() throws Exception {
        Analizador parser = crearParser("int x;");
        Token t = parser.getNextToken();
        assertEquals("int", t.image);
    }

    @Test
    @DisplayName("Reconoce palabras reservadas float")
    void testPalabraReservadaFloat() throws Exception {
        Analizador parser = crearParser("float y;");
        Token t = parser.getNextToken();
        assertEquals("float", t.image);
    }

    @Test
    @DisplayName("Reconoce palabras reservadas bool")
    void testPalabraReservadaBool() throws Exception {
        Analizador parser = crearParser("bool activo;");
        Token t = parser.getNextToken();
        assertEquals("bool", t.image);
    }

    @Test
    @DisplayName("Reconoce cadenas de texto")
    void testCadena() throws Exception {
        Analizador parser = crearParser("\"Hola mundo\";");
        Token t = parser.getNextToken();
        assertEquals("\"Hola mundo\"", t.image);
    }

    @Test
    @DisplayName("Reconoce operadores aritméticos")
    void testOperadores() throws Exception {
        Analizador parser = crearParser("+ - * / %");
        assertEquals("+", parser.getNextToken().image);
        assertEquals("-", parser.getNextToken().image);
        assertEquals("*", parser.getNextToken().image);
        assertEquals("/", parser.getNextToken().image);
        assertEquals("%", parser.getNextToken().image);
    }

    @Test
    @DisplayName("Reconoce operadores de comparación")
    void testOperadoresComparacion() throws Exception {
        Analizador parser = crearParser("== != < > <= >=");
        assertEquals("==", parser.getNextToken().image);
        assertEquals("!=", parser.getNextToken().image);
        assertEquals("<", parser.getNextToken().image);
        assertEquals(">", parser.getNextToken().image);
        assertEquals("<=", parser.getNextToken().image);
        assertEquals(">=", parser.getNextToken().image);
    }

    @Test
    @DisplayName("Reconoce delimitadores")
    void testDelimitadores() throws Exception {
        Analizador parser = crearParser("( ) { } [ ] ; , .");
        assertEquals("(", parser.getNextToken().image);
        assertEquals(")", parser.getNextToken().image);
        assertEquals("{", parser.getNextToken().image);
        assertEquals("}", parser.getNextToken().image);
        assertEquals("[", parser.getNextToken().image);
        assertEquals("]", parser.getNextToken().image);
        assertEquals(";", parser.getNextToken().image);
        assertEquals(",", parser.getNextToken().image);
        assertEquals(".", parser.getNextToken().image);
    }

    @Test
    @DisplayName("Ignora espacios en blanco y saltos de línea")
    void testEspaciosEnBlanco() throws Exception {
        Analizador parser = crearParser("   int   \n\t  x   ;");
        assertEquals("int", parser.getNextToken().image);
        assertEquals("x", parser.getNextToken().image);
        assertEquals(";", parser.getNextToken().image);
    }

    @Test
    @DisplayName("Ignora comentarios de línea")
    void testComentarioLinea() throws Exception {
        Analizador parser = crearParser("int // esto es un comentario\n x;");
        assertEquals("int", parser.getNextToken().image);
        assertEquals("x", parser.getNextToken().image);
    }

    @Test
    @DisplayName("Reconoce true y false como tokens")
    void testBooleanos() throws Exception {
        Analizador parser = crearParser("true false");
        assertEquals("true", parser.getNextToken().image);
        assertEquals("false", parser.getNextToken().image);
    }

    @Test
    @DisplayName("Reconoce Console.WriteLine como un solo token")
    void testConsoleWriteLine() throws Exception {
        Analizador parser = crearParser("Console.WriteLine(42);");
        assertEquals("Console.WriteLine", parser.getNextToken().image);
    }

    @Test
    @DisplayName("Detecta carácter no reconocido como ERROR_LEXICO")
    void testErrorLexico() throws Exception {
        Analizador parser = crearParser("@");
        Token t = parser.getNextToken();
        // El token ERROR_LEXICO captura caracteres inválidos
        assertEquals("@", t.image);
    }

    @Test
    @DisplayName("Registra la línea y columna correctas del token")
    void testPosicionToken() throws Exception {
        Analizador parser = crearParser("int x;");
        Token t = parser.getNextToken();
        assertEquals(1, t.beginLine);
        assertEquals(1, t.beginColumn);
    }
}
