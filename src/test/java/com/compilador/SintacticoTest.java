package com.compilador;

import com.compilador.ast.*;

import java.io.StringReader;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del analizador sintáctico.
 * Verifica que las estructuras gramaticales se reconozcan correctamente
 * y generen los nodos AST esperados.
 */
public class SintacticoTest {

    /**
     * Helper para parsear un string de código fuente y obtener el AST.
     */
    private NodoPrograma parsear(String codigo) throws ParseException {
        Analizador parser = new Analizador(new StringReader(codigo));
        return parser.programa();
    }

    @Test
    @DisplayName("Parsea programa vacío")
    void testProgramaVacio() throws Exception {
        NodoPrograma prog = parsear("");
        assertNotNull(prog);
        assertEquals(0, prog.getSentencias().size());
    }

    @Test
    @DisplayName("Parsea declaración simple: int x;")
    void testDeclaracionSimple() throws Exception {
        NodoPrograma prog = parsear("int x;");
        assertEquals(1, prog.getSentencias().size());

        Nodo n = prog.getSentencias().get(0);
        assertInstanceOf(NodoDeclaracion.class, n);

        NodoDeclaracion decl = (NodoDeclaracion) n;
        assertEquals("int", decl.getTipoDato());
        assertEquals("x", decl.getIdentificador());
        assertNull(decl.getInicializacion());
    }

    @Test
    @DisplayName("Parsea declaración con inicialización: int x = 5;")
    void testDeclaracionConInicializacion() throws Exception {
        NodoPrograma prog = parsear("int x = 5;");
        assertEquals(1, prog.getSentencias().size());

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        assertEquals("int", decl.getTipoDato());
        assertEquals("x", decl.getIdentificador());
        assertNotNull(decl.getInicializacion());
        assertInstanceOf(NodoNumero.class, decl.getInicializacion());
    }

    @Test
    @DisplayName("Parsea declaración float con decimal: float pi = 3.14;")
    void testDeclaracionFloat() throws Exception {
        NodoPrograma prog = parsear("float pi = 3.14;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        assertEquals("float", decl.getTipoDato());
        assertEquals("pi", decl.getIdentificador());

        NodoNumero num = (NodoNumero) decl.getInicializacion();
        assertEquals("3.14", num.getValor());
        assertTrue(num.esDecimal());
    }

    @Test
    @DisplayName("Parsea asignación: x = 10;")
    void testAsignacion() throws Exception {
        NodoPrograma prog = parsear("x = 10;");
        assertEquals(1, prog.getSentencias().size());

        NodoAsignacion asig = (NodoAsignacion) prog.getSentencias().get(0);
        assertEquals("x", asig.getIdentificador());
        assertInstanceOf(NodoNumero.class, asig.getExpresion());
    }

    @Test
    @DisplayName("Parsea expresión aritmética: x = 5 + 3;")
    void testExpresionAritmetica() throws Exception {
        NodoPrograma prog = parsear("x = 5 + 3;");

        NodoAsignacion asig = (NodoAsignacion) prog.getSentencias().get(0);
        assertInstanceOf(NodoOperacion.class, asig.getExpresion());

        NodoOperacion op = (NodoOperacion) asig.getExpresion();
        assertEquals("+", op.getOperador());
        assertInstanceOf(NodoNumero.class, op.getIzquierdo());
        assertInstanceOf(NodoNumero.class, op.getDerecho());
    }

    @Test
    @DisplayName("Parsea precedencia de operadores: x = 2 + 3 * 4;")
    void testPrecedenciaOperadores() throws Exception {
        NodoPrograma prog = parsear("x = 2 + 3 * 4;");

        NodoAsignacion asig = (NodoAsignacion) prog.getSentencias().get(0);
        NodoOperacion suma = (NodoOperacion) asig.getExpresion();

        // La suma debe estar en la raíz (menor precedencia)
        assertEquals("+", suma.getOperador());

        // El lado derecho debe ser la multiplicación (mayor precedencia)
        assertInstanceOf(NodoOperacion.class, suma.getDerecho());
        NodoOperacion mult = (NodoOperacion) suma.getDerecho();
        assertEquals("*", mult.getOperador());
    }

    @Test
    @DisplayName("Parsea expresión con paréntesis: x = (2 + 3) * 4;")
    void testExpresionConParentesis() throws Exception {
        NodoPrograma prog = parsear("x = (2 + 3) * 4;");

        NodoAsignacion asig = (NodoAsignacion) prog.getSentencias().get(0);
        NodoOperacion mult = (NodoOperacion) asig.getExpresion();

        // La multiplicación debe estar en la raíz
        assertEquals("*", mult.getOperador());

        // El lado izquierdo debe ser la agrupación
        assertInstanceOf(NodoAgrupacion.class, mult.getIzquierdo());
    }

    @Test
    @DisplayName("Parsea declaración string: string msg = \"Hola\";")
    void testDeclaracionString() throws Exception {
        NodoPrograma prog = parsear("string msg = \"Hola\";");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        assertEquals("string", decl.getTipoDato());
        assertInstanceOf(NodoCadena.class, decl.getInicializacion());

        NodoCadena cadena = (NodoCadena) decl.getInicializacion();
        assertEquals("Hola", cadena.getValor());
    }

    @Test
    @DisplayName("Parsea declaración bool: bool activo = true;")
    void testDeclaracionBool() throws Exception {
        NodoPrograma prog = parsear("bool activo = true;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        assertEquals("bool", decl.getTipoDato());
        assertInstanceOf(NodoBooleano.class, decl.getInicializacion());

        NodoBooleano bool = (NodoBooleano) decl.getInicializacion();
        assertTrue(bool.getValor());
    }

    @Test
    @DisplayName("Parsea Console.WriteLine(42);")
    void testConsoleWriteLine() throws Exception {
        NodoPrograma prog = parsear("Console.WriteLine(42);");
        assertEquals(1, prog.getSentencias().size());

        NodoComando cmd = (NodoComando) prog.getSentencias().get(0);
        assertEquals("Console.WriteLine", cmd.getComando());
        assertInstanceOf(NodoNumero.class, cmd.getArgumento());
    }

    @Test
    @DisplayName("Parsea múltiples sentencias")
    void testMultiplesSentencias() throws Exception {
        String codigo = "int x = 5;\nint y = 10;\nx = x + y;\nConsole.WriteLine(x);";
        NodoPrograma prog = parsear(codigo);
        assertEquals(4, prog.getSentencias().size());
    }

    @Test
    @DisplayName("Error de sintaxis lanza ParseException")
    void testErrorSintaxis() {
        // Un programa mal formado debe lanzar ParseException
        assertThrows(ParseException.class, () -> {
            parsear("int = ;");
        });
    }

    @Test
    @DisplayName("Parsea expresión con resta: x = 10 - 3;")
    void testResta() throws Exception {
        NodoPrograma prog = parsear("x = 10 - 3;");

        NodoAsignacion asig = (NodoAsignacion) prog.getSentencias().get(0);
        NodoOperacion op = (NodoOperacion) asig.getExpresion();
        assertEquals("-", op.getOperador());
    }

    @Test
    @DisplayName("Parsea expresión con división: x = 10 / 2;")
    void testDivision() throws Exception {
        NodoPrograma prog = parsear("x = 10 / 2;");

        NodoAsignacion asig = (NodoAsignacion) prog.getSentencias().get(0);
        NodoOperacion op = (NodoOperacion) asig.getExpresion();
        assertEquals("/", op.getOperador());
    }

    @Test
    @DisplayName("Parsea expresión con módulo: x = 10 % 3;")
    void testModulo() throws Exception {
        NodoPrograma prog = parsear("x = 10 % 3;");

        NodoAsignacion asig = (NodoAsignacion) prog.getSentencias().get(0);
        NodoOperacion op = (NodoOperacion) asig.getExpresion();
        assertEquals("%", op.getOperador());
    }

    @Test
    @DisplayName("Parsea declaraciones de todos los tipos")
    void testTodosLosTipos() throws Exception {
        String codigo = "int a;\nfloat b;\ndouble c;\nbool d;\nstring e;\nchar f;";
        NodoPrograma prog = parsear(codigo);
        assertEquals(6, prog.getSentencias().size());

        String[] tipos = {"int", "float", "double", "bool", "string", "char"};
        for (int i = 0; i < tipos.length; i++) {
            NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(i);
            assertEquals(tipos[i], decl.getTipoDato());
        }
    }
}
