package com.compilador;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import com.compilador.ast.*;

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

    // ═══════════════════════════════════════════════════════════════
    // Tests existentes (declaraciones, asignaciones, expresiones)
    // ═══════════════════════════════════════════════════════════════

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
    @DisplayName("Parsea declaración de arreglo: int[] numeros = new int[5];")
    void testDeclaracionArreglo() throws Exception {
        NodoPrograma prog = parsear("int[] numeros = new int[5];");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        assertEquals("int[]", decl.getTipoDato());
        assertEquals("numeros", decl.getIdentificador());
        assertInstanceOf(NodoArreglo.class, decl.getInicializacion());

        NodoArreglo arreglo = (NodoArreglo) decl.getInicializacion();
        assertEquals("int", arreglo.getTipoElemento());
        assertEquals("5", arreglo.getTamaño());
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
    @DisplayName("Parsea declaración float negativo: float neg = -3.14;")
    void testDeclaracionFloatNegativo() throws Exception {
        NodoPrograma prog = parsear("float neg = -3.14;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        assertEquals("float", decl.getTipoDato());
        assertEquals("neg", decl.getIdentificador());

        NodoNumero num = (NodoNumero) decl.getInicializacion();
        assertEquals("-3.14", num.getValor());
        assertTrue(num.esDecimal());
    }

    @Test
    @DisplayName("Parsea declaración int negativo: int neg = -5;")
    void testDeclaracionIntNegativoMenosCinco() throws Exception {
        NodoPrograma prog = parsear("int neg = -5;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        assertEquals("int", decl.getTipoDato());
        assertEquals("neg", decl.getIdentificador());

        NodoNumero num = (NodoNumero) decl.getInicializacion();
        assertEquals("-5", num.getValor());
        assertFalse(num.esDecimal());
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

    // ═══════════════════════════════════════════════════════════════
    // Tests de expresiones lógicas y relacionales
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Parsea expresión relacional: x > 5")
    void testExpresionRelacionalMayor() throws Exception {
        NodoPrograma prog = parsear("bool r = x > 5;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        assertInstanceOf(NodoOperacionRelacional.class, decl.getInicializacion());

        NodoOperacionRelacional rel = (NodoOperacionRelacional) decl.getInicializacion();
        assertEquals(">", rel.getOperador());
        assertInstanceOf(NodoIdentificador.class, rel.getIzquierdo());
        assertInstanceOf(NodoNumero.class, rel.getDerecho());
    }

    @Test
    @DisplayName("Parsea expresión relacional: x < 10")
    void testExpresionRelacionalMenor() throws Exception {
        NodoPrograma prog = parsear("bool r = x < 10;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        NodoOperacionRelacional rel = (NodoOperacionRelacional) decl.getInicializacion();
        assertEquals("<", rel.getOperador());
    }

    @Test
    @DisplayName("Parsea expresión relacional: x <= 10")
    void testExpresionRelacionalMenorIgual() throws Exception {
        NodoPrograma prog = parsear("bool r = x <= 10;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        NodoOperacionRelacional rel = (NodoOperacionRelacional) decl.getInicializacion();
        assertEquals("<=", rel.getOperador());
    }

    @Test
    @DisplayName("Parsea expresión relacional: x >= 10")
    void testExpresionRelacionalMayorIgual() throws Exception {
        NodoPrograma prog = parsear("bool r = x >= 10;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        NodoOperacionRelacional rel = (NodoOperacionRelacional) decl.getInicializacion();
        assertEquals(">=", rel.getOperador());
    }

    @Test
    @DisplayName("Parsea expresión de igualdad: x == 5")
    void testExpresionIgualdad() throws Exception {
        NodoPrograma prog = parsear("bool r = x == 5;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        NodoOperacionRelacional rel = (NodoOperacionRelacional) decl.getInicializacion();
        assertEquals("==", rel.getOperador());
    }

    @Test
    @DisplayName("Parsea expresión de diferencia: x != 5")
    void testExpresionDiferente() throws Exception {
        NodoPrograma prog = parsear("bool r = x != 5;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        NodoOperacionRelacional rel = (NodoOperacionRelacional) decl.getInicializacion();
        assertEquals("!=", rel.getOperador());
    }

    @Test
    @DisplayName("Parsea expresión lógica AND: x > 5 && y < 10")
    void testExpresionLogicaAnd() throws Exception {
        NodoPrograma prog = parsear("bool r = x > 5 && y < 10;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        assertInstanceOf(NodoOperacionLogica.class, decl.getInicializacion());

        NodoOperacionLogica logica = (NodoOperacionLogica) decl.getInicializacion();
        assertEquals("&&", logica.getOperador());

        // Izquierdo: x > 5
        assertInstanceOf(NodoOperacionRelacional.class, logica.getIzquierdo());
        // Derecho: y < 10
        assertInstanceOf(NodoOperacionRelacional.class, logica.getDerecho());
    }

    @Test
    @DisplayName("Parsea expresión lógica OR: a || b")
    void testExpresionLogicaOr() throws Exception {
        NodoPrograma prog = parsear("bool r = a || b;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        assertInstanceOf(NodoOperacionLogica.class, decl.getInicializacion());

        NodoOperacionLogica logica = (NodoOperacionLogica) decl.getInicializacion();
        assertEquals("||", logica.getOperador());
    }

    @Test
    @DisplayName("Parsea negación lógica: !activo")
    void testNegacionLogica() throws Exception {
        NodoPrograma prog = parsear("bool r = !activo;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        assertInstanceOf(NodoNegacionLogica.class, decl.getInicializacion());

        NodoNegacionLogica neg = (NodoNegacionLogica) decl.getInicializacion();
        assertInstanceOf(NodoIdentificador.class, neg.getExpresion());
    }

    @Test
    @DisplayName("Precedencia: && tiene mayor precedencia que ||")
    void testPrecedenciaLogica() throws Exception {
        // a || b && c  debe ser  a || (b && c)
        NodoPrograma prog = parsear("bool r = a || b && c;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        NodoOperacionLogica or = (NodoOperacionLogica) decl.getInicializacion();
        assertEquals("||", or.getOperador());

        // El lado derecho debe ser el AND
        assertInstanceOf(NodoOperacionLogica.class, or.getDerecho());
        NodoOperacionLogica and = (NodoOperacionLogica) or.getDerecho();
        assertEquals("&&", and.getOperador());
    }

    @Test
    @DisplayName("Precedencia: relacionales > aritméticos")
    void testPrecedenciaRelacionalAritmetica() throws Exception {
        // x + 1 > 5  debe ser  (x + 1) > 5
        NodoPrograma prog = parsear("bool r = x + 1 > 5;");

        NodoDeclaracion decl = (NodoDeclaracion) prog.getSentencias().get(0);
        NodoOperacionRelacional rel = (NodoOperacionRelacional) decl.getInicializacion();
        assertEquals(">", rel.getOperador());

        // El lado izquierdo debe ser la suma
        assertInstanceOf(NodoOperacion.class, rel.getIzquierdo());
        NodoOperacion suma = (NodoOperacion) rel.getIzquierdo();
        assertEquals("+", suma.getOperador());
    }

    // ═══════════════════════════════════════════════════════════════
    // Tests de bloques de código
    // ═══════════════════════════════════════════════════════════════

    // Nota: los bloques se prueban indirectamente a través de if, while, for, etc.

    // ═══════════════════════════════════════════════════════════════
    // Tests de if / else
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Parsea if simple: if (x > 5) { ... }")
    void testIfSimple() throws Exception {
        String codigo = "if (x > 5) { Console.WriteLine(x); }";
        NodoPrograma prog = parsear(codigo);
        assertEquals(1, prog.getSentencias().size());

        NodoIf nodoIf = (NodoIf) prog.getSentencias().get(0);
        assertNotNull(nodoIf.getCondicion());
        assertNotNull(nodoIf.getBloqueThen());
        assertNull(nodoIf.getBloqueElse());

        // Verificar que la condición es x > 5
        assertInstanceOf(NodoOperacionRelacional.class, nodoIf.getCondicion());

        // Verificar el bloque then
        NodoBloque bloqueThen = (NodoBloque) nodoIf.getBloqueThen();
        assertEquals(1, bloqueThen.getSentencias().size());
    }

    @Test
    @DisplayName("Parsea if-else: if (x > 0) { ... } else { ... }")
    void testIfElse() throws Exception {
        String codigo = "if (x > 0) { x = 1; } else { x = 0; }";
        NodoPrograma prog = parsear(codigo);

        NodoIf nodoIf = (NodoIf) prog.getSentencias().get(0);
        assertNotNull(nodoIf.getCondicion());
        assertNotNull(nodoIf.getBloqueThen());
        assertNotNull(nodoIf.getBloqueElse());

        // Verificar bloque then
        NodoBloque bloqueThen = (NodoBloque) nodoIf.getBloqueThen();
        assertEquals(1, bloqueThen.getSentencias().size());

        // Verificar bloque else
        NodoBloque bloqueElse = (NodoBloque) nodoIf.getBloqueElse();
        assertEquals(1, bloqueElse.getSentencias().size());
    }

    @Test
    @DisplayName("Parsea if-else if-else")
    void testIfElseIfElse() throws Exception {
        String codigo = "if (x > 0) { x = 1; } else if (x == 0) { x = 0; } else { x = -1; }";
        NodoPrograma prog = parsear(codigo);

        NodoIf nodoIf = (NodoIf) prog.getSentencias().get(0);
        assertNotNull(nodoIf.getBloqueThen());

        // El else debe ser otro NodoIf (else if)
        assertInstanceOf(NodoIf.class, nodoIf.getBloqueElse());

        NodoIf elseIf = (NodoIf) nodoIf.getBloqueElse();
        assertNotNull(elseIf.getBloqueThen());
        assertNotNull(elseIf.getBloqueElse()); // el else final
        assertInstanceOf(NodoBloque.class, elseIf.getBloqueElse());
    }

    @Test
    @DisplayName("Parsea if con múltiples sentencias en el bloque")
    void testIfMultiplesSentencias() throws Exception {
        String codigo = "if (activo) { int x = 5; int y = 10; Console.WriteLine(x); }";
        NodoPrograma prog = parsear(codigo);

        NodoIf nodoIf = (NodoIf) prog.getSentencias().get(0);
        NodoBloque bloque = (NodoBloque) nodoIf.getBloqueThen();
        assertEquals(3, bloque.getSentencias().size());
    }

    @Test
    @DisplayName("Parsea if con condición lógica compuesta")
    void testIfCondicionCompuesta() throws Exception {
        String codigo = "if (x > 0 && x < 100) { x = 50; }";
        NodoPrograma prog = parsear(codigo);

        NodoIf nodoIf = (NodoIf) prog.getSentencias().get(0);
        assertInstanceOf(NodoOperacionLogica.class, nodoIf.getCondicion());

        NodoOperacionLogica logica = (NodoOperacionLogica) nodoIf.getCondicion();
        assertEquals("&&", logica.getOperador());
    }

    // ═══════════════════════════════════════════════════════════════
    // Tests de while
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Parsea while simple: while (x < 10) { ... }")
    void testWhileSimple() throws Exception {
        String codigo = "while (x < 10) { x = x + 1; }";
        NodoPrograma prog = parsear(codigo);
        assertEquals(1, prog.getSentencias().size());

        NodoWhile nodoWhile = (NodoWhile) prog.getSentencias().get(0);
        assertNotNull(nodoWhile.getCondicion());
        assertNotNull(nodoWhile.getCuerpo());

        // Verificar condición
        assertInstanceOf(NodoOperacionRelacional.class, nodoWhile.getCondicion());
        NodoOperacionRelacional rel = (NodoOperacionRelacional) nodoWhile.getCondicion();
        assertEquals("<", rel.getOperador());

        // Verificar cuerpo
        NodoBloque cuerpo = (NodoBloque) nodoWhile.getCuerpo();
        assertEquals(1, cuerpo.getSentencias().size());
    }

    @Test
    @DisplayName("Parsea while con bloque vacío")
    void testWhileBloqueVacio() throws Exception {
        String codigo = "while (true) { }";
        NodoPrograma prog = parsear(codigo);

        NodoWhile nodoWhile = (NodoWhile) prog.getSentencias().get(0);
        NodoBloque cuerpo = (NodoBloque) nodoWhile.getCuerpo();
        assertEquals(0, cuerpo.getSentencias().size());
    }

    @Test
    @DisplayName("Parsea while con múltiples sentencias")
    void testWhileMultiplesSentencias() throws Exception {
        String codigo = "while (i < 5) { Console.WriteLine(i); i = i + 1; }";
        NodoPrograma prog = parsear(codigo);

        NodoWhile nodoWhile = (NodoWhile) prog.getSentencias().get(0);
        NodoBloque cuerpo = (NodoBloque) nodoWhile.getCuerpo();
        assertEquals(2, cuerpo.getSentencias().size());
    }

    // ═══════════════════════════════════════════════════════════════
    // Tests de do-while
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Parsea do-while: do { ... } while (condicion);")
    void testDoWhile() throws Exception {
        String codigo = "do { x = x + 1; } while (x < 10);";
        NodoPrograma prog = parsear(codigo);
        assertEquals(1, prog.getSentencias().size());

        NodoDoWhile nodoDoWhile = (NodoDoWhile) prog.getSentencias().get(0);
        assertNotNull(nodoDoWhile.getCuerpo());
        assertNotNull(nodoDoWhile.getCondicion());

        // Verificar cuerpo
        NodoBloque cuerpo = (NodoBloque) nodoDoWhile.getCuerpo();
        assertEquals(1, cuerpo.getSentencias().size());

        // Verificar condición
        assertInstanceOf(NodoOperacionRelacional.class, nodoDoWhile.getCondicion());
    }

    @Test
    @DisplayName("Parsea do-while con múltiples sentencias")
    void testDoWhileMultiplesSentencias() throws Exception {
        String codigo = "do { Console.WriteLine(i); i = i + 1; } while (i < 100);";
        NodoPrograma prog = parsear(codigo);

        NodoDoWhile nodoDoWhile = (NodoDoWhile) prog.getSentencias().get(0);
        NodoBloque cuerpo = (NodoBloque) nodoDoWhile.getCuerpo();
        assertEquals(2, cuerpo.getSentencias().size());
    }

    // ═══════════════════════════════════════════════════════════════
    // Tests de for
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Parsea for clásico con declaración: for (int i = 0; i < 10; i++) { ... }")
    void testForConDeclaracion() throws Exception {
        String codigo = "for (int i = 0; i < 10; i++) { Console.WriteLine(i); }";
        NodoPrograma prog = parsear(codigo);
        assertEquals(1, prog.getSentencias().size());

        NodoFor nodoFor = (NodoFor) prog.getSentencias().get(0);
        assertNotNull(nodoFor.getInicializacion());
        assertNotNull(nodoFor.getCondicion());
        assertNotNull(nodoFor.getActualizacion());
        assertNotNull(nodoFor.getCuerpo());

        // Verificar inicialización (declaración)
        assertInstanceOf(NodoDeclaracion.class, nodoFor.getInicializacion());
        NodoDeclaracion decl = (NodoDeclaracion) nodoFor.getInicializacion();
        assertEquals("int", decl.getTipoDato());
        assertEquals("i", decl.getIdentificador());

        // Verificar condición
        assertInstanceOf(NodoOperacionRelacional.class, nodoFor.getCondicion());

        // Verificar actualización (incremento)
        assertInstanceOf(NodoIncremento.class, nodoFor.getActualizacion());
        NodoIncremento inc = (NodoIncremento) nodoFor.getActualizacion();
        assertEquals("i", inc.getIdentificador());
        assertEquals("++", inc.getOperador());
    }

    @Test
    @DisplayName("Parsea for con asignación en inicialización: for (i = 0; i < 5; i++) { ... }")
    void testForConAsignacion() throws Exception {
        String codigo = "for (i = 0; i < 5; i++) { x = x + i; }";
        NodoPrograma prog = parsear(codigo);

        NodoFor nodoFor = (NodoFor) prog.getSentencias().get(0);
        assertInstanceOf(NodoAsignacion.class, nodoFor.getInicializacion());
    }

    @Test
    @DisplayName("Parsea for con decremento: for (int i = 10; i > 0; i--) { ... }")
    void testForConDecremento() throws Exception {
        String codigo = "for (int i = 10; i > 0; i--) { Console.WriteLine(i); }";
        NodoPrograma prog = parsear(codigo);

        NodoFor nodoFor = (NodoFor) prog.getSentencias().get(0);
        assertInstanceOf(NodoIncremento.class, nodoFor.getActualizacion());

        NodoIncremento dec = (NodoIncremento) nodoFor.getActualizacion();
        assertEquals("--", dec.getOperador());
    }

    @Test
    @DisplayName("Parsea for con partes vacías: for (;;) { ... }")
    void testForVacio() throws Exception {
        String codigo = "for (;;) { x = 1; }";
        NodoPrograma prog = parsear(codigo);

        NodoFor nodoFor = (NodoFor) prog.getSentencias().get(0);
        assertNull(nodoFor.getInicializacion());
        assertNull(nodoFor.getCondicion());
        assertNull(nodoFor.getActualizacion());
        assertNotNull(nodoFor.getCuerpo());
    }

    @Test
    @DisplayName("Parsea for con asignación compuesta en actualización")
    void testForConAsignacionCompuesta() throws Exception {
        String codigo = "for (int i = 0; i < 100; i += 2) { Console.WriteLine(i); }";
        NodoPrograma prog = parsear(codigo);

        NodoFor nodoFor = (NodoFor) prog.getSentencias().get(0);
        // La asignación compuesta se descompone en asignación simple
        assertInstanceOf(NodoAsignacion.class, nodoFor.getActualizacion());
    }

    // ═══════════════════════════════════════════════════════════════
    // Tests de incremento / decremento
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Parsea incremento: x++;")
    void testIncremento() throws Exception {
        NodoPrograma prog = parsear("x++;");
        assertEquals(1, prog.getSentencias().size());

        NodoIncremento inc = (NodoIncremento) prog.getSentencias().get(0);
        assertEquals("x", inc.getIdentificador());
        assertEquals("++", inc.getOperador());
    }

    @Test
    @DisplayName("Parsea decremento: contador--;")
    void testDecremento() throws Exception {
        NodoPrograma prog = parsear("contador--;");
        assertEquals(1, prog.getSentencias().size());

        NodoIncremento dec = (NodoIncremento) prog.getSentencias().get(0);
        assertEquals("contador", dec.getIdentificador());
        assertEquals("--", dec.getOperador());
    }

    // ═══════════════════════════════════════════════════════════════
    // Tests de asignaciones compuestas
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Parsea asignación compuesta: x += 5;")
    void testAsignacionSumaCompuesta() throws Exception {
        NodoPrograma prog = parsear("x += 5;");
        assertEquals(1, prog.getSentencias().size());

        NodoAsignacion asig = (NodoAsignacion) prog.getSentencias().get(0);
        assertEquals("x", asig.getIdentificador());

        // Se descompone en: x = x + 5
        assertInstanceOf(NodoOperacion.class, asig.getExpresion());
        NodoOperacion op = (NodoOperacion) asig.getExpresion();
        assertEquals("+", op.getOperador());
    }

    @Test
    @DisplayName("Parsea asignación compuesta: y -= 3;")
    void testAsignacionRestaCompuesta() throws Exception {
        NodoPrograma prog = parsear("y -= 3;");

        NodoAsignacion asig = (NodoAsignacion) prog.getSentencias().get(0);
        NodoOperacion op = (NodoOperacion) asig.getExpresion();
        assertEquals("-", op.getOperador());
    }

    @Test
    @DisplayName("Parsea asignación compuesta: z *= 2;")
    void testAsignacionMultCompuesta() throws Exception {
        NodoPrograma prog = parsear("z *= 2;");

        NodoAsignacion asig = (NodoAsignacion) prog.getSentencias().get(0);
        NodoOperacion op = (NodoOperacion) asig.getExpresion();
        assertEquals("*", op.getOperador());
    }

    @Test
    @DisplayName("Parsea asignación compuesta: a /= 4;")
    void testAsignacionDivCompuesta() throws Exception {
        NodoPrograma prog = parsear("a /= 4;");

        NodoAsignacion asig = (NodoAsignacion) prog.getSentencias().get(0);
        NodoOperacion op = (NodoOperacion) asig.getExpresion();
        assertEquals("/", op.getOperador());
    }

    @Test
    @DisplayName("Parsea asignación compuesta: b %= 3;")
    void testAsignacionModCompuesta() throws Exception {
        NodoPrograma prog = parsear("b %= 3;");

        NodoAsignacion asig = (NodoAsignacion) prog.getSentencias().get(0);
        NodoOperacion op = (NodoOperacion) asig.getExpresion();
        assertEquals("%", op.getOperador());
    }

    // ═══════════════════════════════════════════════════════════════
    // Tests de break y continue
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Parsea break dentro de while")
    void testBreak() throws Exception {
        String codigo = "while (true) { break; }";
        NodoPrograma prog = parsear(codigo);

        NodoWhile nodoWhile = (NodoWhile) prog.getSentencias().get(0);
        NodoBloque cuerpo = (NodoBloque) nodoWhile.getCuerpo();
        assertEquals(1, cuerpo.getSentencias().size());

        NodoComando cmd = (NodoComando) cuerpo.getSentencias().get(0);
        assertEquals("break", cmd.getComando());
        assertNull(cmd.getArgumento());
    }

    @Test
    @DisplayName("Parsea continue dentro de for")
    void testContinue() throws Exception {
        String codigo = "for (int i = 0; i < 10; i++) { continue; }";
        NodoPrograma prog = parsear(codigo);

        NodoFor nodoFor = (NodoFor) prog.getSentencias().get(0);
        NodoBloque cuerpo = (NodoBloque) nodoFor.getCuerpo();
        assertEquals(1, cuerpo.getSentencias().size());

        NodoComando cmd = (NodoComando) cuerpo.getSentencias().get(0);
        assertEquals("continue", cmd.getComando());
    }

    // ═══════════════════════════════════════════════════════════════
    // Tests de anidamiento
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Parsea if anidado dentro de while")
    void testIfDentroDeWhile() throws Exception {
        String codigo = "while (x < 100) { if (x > 50) { Console.WriteLine(x); } x = x + 1; }";
        NodoPrograma prog = parsear(codigo);

        NodoWhile nodoWhile = (NodoWhile) prog.getSentencias().get(0);
        NodoBloque cuerpo = (NodoBloque) nodoWhile.getCuerpo();
        assertEquals(2, cuerpo.getSentencias().size());

        // Primera sentencia: if
        assertInstanceOf(NodoIf.class, cuerpo.getSentencias().get(0));
        // Segunda sentencia: asignación
        assertInstanceOf(NodoAsignacion.class, cuerpo.getSentencias().get(1));
    }

    @Test
    @DisplayName("Parsea for dentro de if")
    void testForDentroDeIf() throws Exception {
        String codigo = "if (activo) { for (int i = 0; i < 5; i++) { Console.WriteLine(i); } }";
        NodoPrograma prog = parsear(codigo);

        NodoIf nodoIf = (NodoIf) prog.getSentencias().get(0);
        NodoBloque bloque = (NodoBloque) nodoIf.getBloqueThen();
        assertEquals(1, bloque.getSentencias().size());
        assertInstanceOf(NodoFor.class, bloque.getSentencias().get(0));
    }

    @Test
    @DisplayName("Parsea while dentro de while (anidamiento doble)")
    void testWhileDentroDeWhile() throws Exception {
        String codigo = "while (x < 10) { while (y < 10) { y = y + 1; } x = x + 1; }";
        NodoPrograma prog = parsear(codigo);

        NodoWhile whileExterno = (NodoWhile) prog.getSentencias().get(0);
        NodoBloque cuerpoExterno = (NodoBloque) whileExterno.getCuerpo();
        assertEquals(2, cuerpoExterno.getSentencias().size());

        assertInstanceOf(NodoWhile.class, cuerpoExterno.getSentencias().get(0));
    }

    // ═══════════════════════════════════════════════════════════════
    // Tests de programas completos (integración)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Parsea programa completo con declaraciones, ciclo y condicional")
    void testProgramaCompleto() throws Exception {
        String codigo =
            "int suma = 0;\n" +
            "for (int i = 1; i <= 10; i++) {\n" +
            "    suma += i;\n" +
            "}\n" +
            "if (suma > 50) {\n" +
            "    Console.WriteLine(suma);\n" +
            "} else {\n" +
            "    Console.WriteLine(0);\n" +
            "}\n";

        NodoPrograma prog = parsear(codigo);
        assertEquals(3, prog.getSentencias().size());

        assertInstanceOf(NodoDeclaracion.class, prog.getSentencias().get(0));
        assertInstanceOf(NodoFor.class, prog.getSentencias().get(1));
        assertInstanceOf(NodoIf.class, prog.getSentencias().get(2));
    }

    @Test
    @DisplayName("Parsea programa con do-while y break condicional")
    void testProgramaDoWhileConBreak() throws Exception {
        String codigo =
            "int x = 0;\n" +
            "do {\n" +
            "    x = x + 1;\n" +
            "    if (x == 5) {\n" +
            "        break;\n" +
            "    }\n" +
            "} while (x < 10);\n";

        NodoPrograma prog = parsear(codigo);
        assertEquals(2, prog.getSentencias().size());

        assertInstanceOf(NodoDeclaracion.class, prog.getSentencias().get(0));
        assertInstanceOf(NodoDoWhile.class, prog.getSentencias().get(1));

        NodoDoWhile doWhile = (NodoDoWhile) prog.getSentencias().get(1);
        NodoBloque cuerpo = (NodoBloque) doWhile.getCuerpo();
        assertEquals(2, cuerpo.getSentencias().size()); // asignación + if
    }

    @Test
    @DisplayName("Parsea programa con negación lógica en condición")
    void testProgramaNegacionEnCondicion() throws Exception {
        String codigo = "if (!terminado) { Console.WriteLine(\"En progreso\"); }";
        NodoPrograma prog = parsear(codigo);

        NodoIf nodoIf = (NodoIf) prog.getSentencias().get(0);
        assertInstanceOf(NodoNegacionLogica.class, nodoIf.getCondicion());
    }

    @Test
    @DisplayName("Parsea programa con condición OR compleja")
    void testCondicionOrCompleja() throws Exception {
        String codigo = "if (x == 0 || y == 0 || z == 0) { Console.WriteLine(\"cero\"); }";
        NodoPrograma prog = parsear(codigo);

        NodoIf nodoIf = (NodoIf) prog.getSentencias().get(0);
        // x == 0 || y == 0 || z == 0 se asocia a la izquierda
        assertInstanceOf(NodoOperacionLogica.class, nodoIf.getCondicion());
    }

    // ═══════════════════════════════════════════════════════════════
    // Tests de Switch
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Parsea switch con varios cases")
    void testSwitchConCases() throws Exception {
        String codigo = "switch (x) { case 1: Console.WriteLine(1); break; case 2: Console.WriteLine(2); break; }";
        NodoPrograma prog = parsear(codigo);
        
        assertEquals(1, prog.getSentencias().size());
        assertInstanceOf(NodoSwitch.class, prog.getSentencias().get(0));
        
        NodoSwitch nodoSwitch = (NodoSwitch) prog.getSentencias().get(0);
        assertInstanceOf(NodoIdentificador.class, nodoSwitch.getExpresion());
        
        assertEquals(2, nodoSwitch.getCasos().size());
        
        NodoCaso caso1 = nodoSwitch.getCasos().get(0);
        assertFalse(caso1.isEsDefault());
        assertEquals("1", ((NodoNumero) caso1.getValor()).getValor());
        assertEquals(2, caso1.getSentencias().size()); // WriteLine y break
    }

    @Test
    @DisplayName("Parsea switch con default")
    void testSwitchConDefault() throws Exception {
        String codigo = "switch (x) { case 1: break; default: x = 0; break; }";
        NodoPrograma prog = parsear(codigo);
        
        NodoSwitch nodoSwitch = (NodoSwitch) prog.getSentencias().get(0);
        assertEquals(2, nodoSwitch.getCasos().size());
        
        NodoCaso casoDefault = nodoSwitch.getCasos().get(1);
        assertTrue(casoDefault.isEsDefault());
        assertNull(casoDefault.getValor());
        assertEquals(2, casoDefault.getSentencias().size()); // asignacion y break
    }

    @Test
    @DisplayName("Parsea switch con fall-through (case vacío)")
    void testSwitchFallThrough() throws Exception {
        String codigo = "switch (x) { case 1: case 2: break; }";
        NodoPrograma prog = parsear(codigo);
        
        NodoSwitch nodoSwitch = (NodoSwitch) prog.getSentencias().get(0);
        assertEquals(2, nodoSwitch.getCasos().size());
        
        NodoCaso caso1 = nodoSwitch.getCasos().get(0);
        assertEquals("1", ((NodoNumero) caso1.getValor()).getValor());
        assertEquals(0, caso1.getSentencias().size()); // sin sentencias
        
        NodoCaso caso2 = nodoSwitch.getCasos().get(1);
        assertEquals("2", ((NodoNumero) caso2.getValor()).getValor());
        assertEquals(1, caso2.getSentencias().size()); // break
    }
}
