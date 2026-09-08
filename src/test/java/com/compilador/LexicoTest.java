package com.compilador;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

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
    @DisplayName("Reconoce números enteros negativos")
    void testNumeroEnteroNegativo() throws Exception {
        Analizador parser = crearParser("-42;");
        Token t = parser.getNextToken();
        assertEquals("-42", t.image);
    }

    @Test
    @DisplayName("Reconoce números decimales")
    void testNumeroDecimal() throws Exception {
        Analizador parser = crearParser("3.14;");
        Token t = parser.getNextToken();
        assertEquals("3.14", t.image);
    }

    @Test
    @DisplayName("Reconoce números decimales negativos")
    void testNumeroDecimalNegativo() throws Exception {
        Analizador parser = crearParser("-3.14;");
        Token t = parser.getNextToken();
        assertEquals("-3.14", t.image);
    }

    @Test
    @DisplayName("Mantiene separada la resta entre identificadores")
    void testRestaEntreIdentificadores() throws Exception {
        Analizador parser = crearParser("a - b;");

        assertEquals("a", parser.getNextToken().image);
        assertEquals("-", parser.getNextToken().image);
        assertEquals("b", parser.getNextToken().image);
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
    @DisplayName("Reconoce la estructura léxica de un arreglo")
    void testArreglo() throws Exception {
        Analizador parser = crearParser("int[] numeros = new int[5];");
        String[] lexemasEsperados = {
                "int", "[", "]", "numeros", "=", "new", "int", "[", "5", "]", ";"
        };

        for (String esperado : lexemasEsperados) {
            assertEquals(esperado, parser.getNextToken().image);
        }
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
    @DisplayName("Agrupa identificadores con símbolos inválidos")
    void testIdentificadorInvalidoCompleto() throws Exception {
        Analizador parser = crearParser("act$ivo");
        Token t = parser.getNextToken();

        assertEquals("act$ivo", t.image);
        assertEquals(AnalizadorConstants.IDENTIFICADOR_INVALIDO, t.kind);
    }

    @Test
    @DisplayName("Agrupa identificadores con varios símbolos inválidos")
    void testIdentificadorConVariosSimbolosInvalidos() throws Exception {
        Analizador parser = crearParser("resu@#ltado");
        Token t = parser.getNextToken();

        assertEquals("resu@#ltado", t.image);
        assertEquals(AnalizadorConstants.IDENTIFICADOR_INVALIDO, t.kind);
    }

    @Test
    @DisplayName("Agrupa cadenas con símbolos inválidos")
    void testCadenaInvalidaCompleta() throws Exception {
        Analizador parser = crearParser("\"Resul$tado: \"");
        Token t = parser.getNextToken();

        assertEquals("\"Resul$tado: \"", t.image);
        assertEquals(AnalizadorConstants.CADENA_INVALIDA, t.kind);
    }

    @Test
    @DisplayName("Agrupa cadenas con arroba y numeral inválidos")
    void testCadenaConVariosSimbolosInvalidos() throws Exception {
        Analizador parser = crearParser("\"Resu@#ltado: \"");
        Token t = parser.getNextToken();

        assertEquals("\"Resu@#ltado: \"", t.image);
        assertEquals(AnalizadorConstants.CADENA_INVALIDA, t.kind);
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
