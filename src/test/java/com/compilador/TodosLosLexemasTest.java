package com.compilador;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

import java.io.StringReader;

public class TodosLosLexemasTest {

    private Analizador crearParser(String codigo) {
        return new Analizador(new StringReader(codigo));
    }

    @Test
    @DisplayName("Verifica todas las palabras reservadas y tipos de datos")
    void testPalabrasReservadasYTipos() throws Exception {
        String codigo = "int float double bool string char void byte sbyte short ushort uint long ulong decimal object " +
                        "struct interface enum namespace protected switch case default foreach break continue this base using try catch finally throw " +
                        "if else while for do return true false class public private static new null Console.WriteLine";

        Analizador parser = crearParser(codigo);

        String[] lexemasEsperados = {
            "int", "float", "double", "bool", "string", "char", "void", "byte", "sbyte", "short", "ushort", "uint", "long", "ulong", "decimal", "object",
            "struct", "interface", "enum", "namespace", "protected", "switch", "case", "default", "foreach", "break", "continue", "this", "base", "using", "try", "catch", "finally", "throw",
            "if", "else", "while", "for", "do", "return", "true", "false", "class", "public", "private", "static", "new", "null", "Console.WriteLine"
        };

        for (String esperado : lexemasEsperados) {
            Token t = parser.getNextToken();
            assertEquals(esperado, t.image, "Fallo al reconocer la palabra reservada: " + esperado);
        }
    }

    @Test
    @DisplayName("Verifica todos los operadores")
    void testOperadores() throws Exception {
        String codigo = "+= -= *= /= %= + - * / % = == != <= >= < > && || ! ++ -- ^ : _";

        Analizador parser = crearParser(codigo);

        String[] operadoresEsperados = {
            "+=", "-=", "*=", "/=", "%=", "+", "-", "*", "/", "%", "=", "==", "!=", "<=", ">=", "<", ">", "&&", "||", "!", "++", "--", "^", ":", "_"
        };

        for (String esperado : operadoresEsperados) {
            Token t = parser.getNextToken();
            assertEquals(esperado, t.image, "Fallo al reconocer el operador: " + esperado);
        }
    }

    @Test
    @DisplayName("Verifica todos los delimitadores")
    void testDelimitadores() throws Exception {
        String codigo = "( ) { } [ ] ; , .";

        Analizador parser = crearParser(codigo);

        String[] delimitadoresEsperados = {
            "(", ")", "{", "}", "[", "]", ";", ",", "."
        };

        for (String esperado : delimitadoresEsperados) {
            Token t = parser.getNextToken();
            assertEquals(esperado, t.image, "Fallo al reconocer el delimitador: " + esperado);
        }
    }

    @Test
    @DisplayName("Verifica comentarios de línea, bloque y documentación")
    void testComentarios() throws Exception {
        String codigo = "int // comentario linea\n" +
                        "/// comentario doc\n" +
                        "/* comentario bloque */ float";

        Analizador parser = crearParser(codigo);

        Token t1 = parser.getNextToken();
        assertEquals("int", t1.image);

        Token t2 = parser.getNextToken();
        assertEquals("float", t2.image);
    }

    @Test
    @DisplayName("Verifica literales e identificadores")
    void testLiteralesEIdentificadores() throws Exception {
        String codigo = "123 45.67 \"Hola Mundo\" 'a' miVariable_99";

        Analizador parser = crearParser(codigo);

        assertEquals("123", parser.getNextToken().image);
        assertEquals("45.67", parser.getNextToken().image);
        assertEquals("\"Hola Mundo\"", parser.getNextToken().image);
        assertEquals("'a'", parser.getNextToken().image);
        assertEquals("miVariable_99", parser.getNextToken().image);
    }
}
