package com.compilador;

import com.compilador.ast.*;
import com.compilador.semantic.*;

import java.io.StringReader;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.DisplayName;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas del analizador semántico.
 * Verifica validaciones de tipos, ámbitos y declaraciones.
 */
public class SemanticoTest {

    /**
     * Helper: parsea código y ejecuta análisis semántico.
     */
    private AnalizadorSemantico analizar(String codigo) throws Exception {
        Analizador parser = new Analizador(new StringReader(codigo));
        NodoPrograma ast = parser.programa();
        AnalizadorSemantico semantico = new AnalizadorSemantico();
        semantico.analizar(ast);
        return semantico;
    }

    @Test
    @DisplayName("Declaración válida no genera errores")
    void testDeclaracionValida() throws Exception {
        AnalizadorSemantico sem = analizar("int x = 5;");
        assertFalse(sem.tieneErrores());
        assertTrue(sem.getTablaSimbolos().existe("x"));
    }

    @Test
    @DisplayName("Múltiples declaraciones válidas")
    void testMultiplesDeclaraciones() throws Exception {
        String codigo = "int x = 5;\nfloat y = 3.14;\nstring nombre = \"Juan\";";
        AnalizadorSemantico sem = analizar(codigo);
        assertFalse(sem.tieneErrores());
        assertTrue(sem.getTablaSimbolos().existe("x"));
        assertTrue(sem.getTablaSimbolos().existe("y"));
        assertTrue(sem.getTablaSimbolos().existe("nombre"));
    }

    @Test
    @DisplayName("Variable redeclarada genera error")
    void testRedeclaracion() throws Exception {
        String codigo = "int x = 5;\nint x = 10;";
        AnalizadorSemantico sem = analizar(codigo);
        assertTrue(sem.tieneErrores());

        List<ErrorSemantico> errores = sem.getErrores();
        assertEquals(1, errores.size());
        assertTrue(errores.get(0).getMensaje().contains("ya fue declarada"));
    }

    @Test
    @DisplayName("Asignación a variable no declarada genera error")
    void testVariableNoDeclarada() throws Exception {
        String codigo = "x = 10;";
        AnalizadorSemantico sem = analizar(codigo);
        assertTrue(sem.tieneErrores());

        List<ErrorSemantico> errores = sem.getErrores();
        assertEquals(1, errores.size());
        assertTrue(errores.get(0).getMensaje().contains("no ha sido declarada"));
    }

    @Test
    @DisplayName("Asignación a variable declarada no genera error")
    void testAsignacionValida() throws Exception {
        String codigo = "int x;\nx = 10;";
        AnalizadorSemantico sem = analizar(codigo);
        assertFalse(sem.tieneErrores());
    }

    @Test
    @DisplayName("Tabla de símbolos registra el tipo correcto")
    void testTipoEnTabla() throws Exception {
        String codigo = "int edad = 25;\nfloat precio = 9.99;\nbool activo = true;";
        AnalizadorSemantico sem = analizar(codigo);

        Simbolo sEdad = sem.getTablaSimbolos().buscar("edad");
        assertNotNull(sEdad);
        assertEquals(TipoDato.INT, sEdad.getTipo());

        Simbolo sPrecio = sem.getTablaSimbolos().buscar("precio");
        assertNotNull(sPrecio);
        assertEquals(TipoDato.FLOAT, sPrecio.getTipo());

        Simbolo sActivo = sem.getTablaSimbolos().buscar("activo");
        assertNotNull(sActivo);
        assertEquals(TipoDato.BOOL, sActivo.getTipo());
    }

    @Test
    @DisplayName("Declaración con inicialización marca variable como inicializada")
    void testInicializacion() throws Exception {
        String codigo = "int x = 5;";
        AnalizadorSemantico sem = analizar(codigo);

        Simbolo s = sem.getTablaSimbolos().buscar("x");
        assertNotNull(s);
        assertTrue(s.isInicializado());
    }

    @Test
    @DisplayName("Declaración sin inicialización marca variable como no inicializada")
    void testSinInicializacion() throws Exception {
        String codigo = "int x;";
        AnalizadorSemantico sem = analizar(codigo);

        Simbolo s = sem.getTablaSimbolos().buscar("x");
        assertNotNull(s);
        assertFalse(s.isInicializado());
    }

    @Test
    @DisplayName("Programa sin sentencias no genera errores")
    void testProgramaVacio() throws Exception {
        AnalizadorSemantico sem = analizar("");
        assertFalse(sem.tieneErrores());
    }

    @Test
    @DisplayName("TipoDato.desdeString funciona correctamente")
    void testTipoDatoDesdeString() {
        assertEquals(TipoDato.INT, TipoDato.desdeString("int"));
        assertEquals(TipoDato.FLOAT, TipoDato.desdeString("float"));
        assertEquals(TipoDato.BOOL, TipoDato.desdeString("bool"));
        assertEquals(TipoDato.STRING, TipoDato.desdeString("string"));
        assertEquals(TipoDato.DESCONOCIDO, TipoDato.desdeString("xyz"));
    }

    @Test
    @DisplayName("Tabla de símbolos maneja ámbitos")
    void testAmbitos() {
        TablaSimbolos tabla = new TablaSimbolos();
        assertEquals("global", tabla.getAmbitoActual());

        // Registrar en global
        Simbolo sGlobal = new Simbolo("x", TipoDato.INT, 1, 1);
        assertTrue(tabla.registrar(sGlobal));

        // Entrar a nuevo ámbito
        tabla.entrarAmbito("funcion1");
        assertEquals("funcion1", tabla.getAmbitoActual());

        // La variable global debe ser visible
        assertTrue(tabla.existe("x"));

        // Registrar otra variable en el nuevo ámbito
        Simbolo sLocal = new Simbolo("y", TipoDato.FLOAT, 5, 1);
        assertTrue(tabla.registrar(sLocal));

        // Salir del ámbito
        tabla.salirAmbito();
        assertEquals("global", tabla.getAmbitoActual());

        // La variable global sigue visible
        assertTrue(tabla.existe("x"));
    }

    @Test
    @DisplayName("Llamada con cantidad exacta de argumentos no genera error")
    void testLlamadaConArgumentosExactos() throws Exception {
        String codigo = "class C { void S(int x, int y) {} void T() { S(1, 2); } }";

        AnalizadorSemantico sem = analizar(codigo);

        assertFalse(sem.tieneErrores(), sem.getErrores().toString());
    }

    @Test
    @DisplayName("Llamada con argumentos de más genera error")
    void testLlamadaConArgumentosDeMas() throws Exception {
        String codigo = "class C { void S(int x) {} void T() { S(1, 2); } }";

        AnalizadorSemantico sem = analizar(codigo);

        assertTrue(sem.tieneErrores());
        assertEquals("ARGUMENTOS_DE_MAS", sem.getErrores().get(0).getTipoError());
    }

    @Test
    @DisplayName("Llamada con argumentos de menos genera error")
    void testLlamadaConArgumentosDeMenos() throws Exception {
        String codigo = "class C { void S(int x, int y) {} void T() { S(1); } }";

        AnalizadorSemantico sem = analizar(codigo);

        assertTrue(sem.tieneErrores());
        assertEquals("ARGUMENTOS_DE_MENOS", sem.getErrores().get(0).getTipoError());
    }

    @Test
    @DisplayName("La omisión de un parámetro opcional no genera error")
    void testLlamadaConParametroOpcional() throws Exception {
        String codigo = "class C { void S(int x, int y = 2) {} void T() { S(1); } }";

        AnalizadorSemantico sem = analizar(codigo);

        assertFalse(sem.tieneErrores(), sem.getErrores().toString());
    }

    @Test
    @DisplayName("Una función puede llamarse antes de su declaración")
    void testLlamadaAntesDeDeclaracion() throws Exception {
        String codigo = "class C { void T() { S(1); } void S(int x) {} }";

        AnalizadorSemantico sem = analizar(codigo);

        assertFalse(sem.tieneErrores(), sem.getErrores().toString());
    }

    // ═══════════════════════════════════════════════════════════════
    // Tests de la regla: Variable No Declarada (uso en expresiones)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Uso de variable no declarada en expresión aritmética genera error")
    void testVariableNoDeclaradaEnExpresion() throws Exception {
        // z no existe, x sí
        String codigo = "int x = 5;\nint y = x + z;";
        AnalizadorSemantico sem = analizar(codigo);

        assertTrue(sem.tieneErrores());
        List<ErrorSemantico> errores = sem.getErrores();
        assertEquals(1, errores.size());
        assertEquals("NO_DECLARADA", errores.get(0).getTipoError());
        assertTrue(errores.get(0).getMensaje().contains("z"));
    }

    @Test
    @DisplayName("Uso de variables declaradas en expresión aritmética no genera error")
    void testVariablesDeclaradasEnExpresionAritmetica() throws Exception {
        String codigo = "int a = 3;\nint b = 4;\nint c = a + b;";
        AnalizadorSemantico sem = analizar(codigo);

        assertFalse(sem.tieneErrores(), sem.getErrores().toString());
    }

    @Test
    @DisplayName("Variable no declarada en condición de if genera error")
    void testVariableNoDeclaradaEnCondicionIf() throws Exception {
        // condicion usa 'x' que no fue declarada
        String codigo = "if (x > 5) { int y = 1; }";
        AnalizadorSemantico sem = analizar(codigo);

        assertTrue(sem.tieneErrores());
        assertTrue(sem.getErrores().stream()
                .anyMatch(e -> e.getTipoError().equals("NO_DECLARADA")
                        && e.getMensaje().contains("x")));
    }

    @Test
    @DisplayName("Variable declarada antes del if es válida en su condición")
    void testVariableDeclaradaEnCondicionIf() throws Exception {
        String codigo = "int x = 10;\nif (x > 5) { int y = 1; }";
        AnalizadorSemantico sem = analizar(codigo);

        assertFalse(sem.tieneErrores(), sem.getErrores().toString());
    }

    @Test
    @DisplayName("Variable no declarada en condición de while genera error")
    void testVariableNoDeclaradaEnWhile() throws Exception {
        String codigo = "while (contador < 10) { int x = 1; }";
        AnalizadorSemantico sem = analizar(codigo);

        assertTrue(sem.tieneErrores());
        assertTrue(sem.getErrores().stream()
                .anyMatch(e -> e.getTipoError().equals("NO_DECLARADA")
                        && e.getMensaje().contains("contador")));
    }

    @Test
    @DisplayName("Variable no declarada en cuerpo de while genera error")
    void testVariableNoDeclaradaEnCuerpoWhile() throws Exception {
        String codigo = "int i = 0;\nwhile (i < 5) { Console.WriteLine(resultado); i = i + 1; }";
        AnalizadorSemantico sem = analizar(codigo);

        assertTrue(sem.tieneErrores());
        assertTrue(sem.getErrores().stream()
                .anyMatch(e -> e.getTipoError().equals("NO_DECLARADA")
                        && e.getMensaje().contains("resultado")));
    }

    @Test
    @DisplayName("Incremento de variable no declarada genera error")
    void testIncrementoVariableNoDeclarada() throws Exception {
        String codigo = "x++;";
        AnalizadorSemantico sem = analizar(codigo);

        assertTrue(sem.tieneErrores());
        assertEquals(1, sem.getErrores().size());
        assertEquals("NO_DECLARADA", sem.getErrores().get(0).getTipoError());
        assertTrue(sem.getErrores().get(0).getMensaje().contains("x"));
    }

    @Test
    @DisplayName("Decremento de variable declarada no genera error")
    void testDecrementoVariableDeclarada() throws Exception {
        String codigo = "int i = 10;\ni--;";
        AnalizadorSemantico sem = analizar(codigo);

        assertFalse(sem.tieneErrores(), sem.getErrores().toString());
    }

    // ═══════════════════════════════════════════════════════════════
    // Tests de la regla: Aislamiento de Ámbitos (Scopes)
    // ═══════════════════════════════════════════════════════════════

    @Test
    @DisplayName("Variable declarada dentro de if NO es visible fuera del bloque")
    void testAislamientoAmbitoIf() throws Exception {
        // 'local' se declara dentro del if y se usa fuera → debe generar error
        String codigo = "int x = 5;\n"
                + "if (x > 0) {\n"
                + "    int local = 99;\n"
                + "}\n"
                + "Console.WriteLine(local);";  // 'local' ya no existe aquí
        AnalizadorSemantico sem = analizar(codigo);

        assertTrue(sem.tieneErrores());
        assertTrue(sem.getErrores().stream()
                .anyMatch(e -> e.getTipoError().equals("NO_DECLARADA")
                        && e.getMensaje().contains("local")));
    }

    @Test
    @DisplayName("Variable declarada dentro de while NO es visible fuera del bloque")
    void testAislamientoAmbitoWhile() throws Exception {
        String codigo = "int i = 0;\n"
                + "while (i < 3) {\n"
                + "    int temp = i * 2;\n"
                + "    i = i + 1;\n"
                + "}\n"
                + "Console.WriteLine(temp);";   // 'temp' no existe aquí
        AnalizadorSemantico sem = analizar(codigo);

        assertTrue(sem.tieneErrores());
        assertTrue(sem.getErrores().stream()
                .anyMatch(e -> e.getTipoError().equals("NO_DECLARADA")
                        && e.getMensaje().contains("temp")));
    }

    @Test
    @DisplayName("Variable del for NO es visible fuera del bloque for")
    void testAislamientoAmbitoFor() throws Exception {
        // La variable 'i' del for no debe ser visible después del for
        String codigo = "for (int i = 0; i < 5; i++) {\n"
                + "    Console.WriteLine(i);\n"
                + "}\n"
                + "Console.WriteLine(i);";   // 'i' no existe aquí
        AnalizadorSemantico sem = analizar(codigo);

        assertTrue(sem.tieneErrores());
        assertTrue(sem.getErrores().stream()
                .anyMatch(e -> e.getTipoError().equals("NO_DECLARADA")
                        && e.getMensaje().contains("i")));
    }

    @Test
    @DisplayName("Variable global es visible dentro de bloques anidados")
    void testVariableGlobalVisibleEnBloques() throws Exception {
        String codigo = "int total = 0;\n"
                + "int i = 0;\n"
                + "while (i < 5) {\n"
                + "    total = total + i;\n"   // 'total' e 'i' son globales
                + "    i = i + 1;\n"
                + "}\n"
                + "Console.WriteLine(total);";
        AnalizadorSemantico sem = analizar(codigo);

        assertFalse(sem.tieneErrores(), sem.getErrores().toString());
    }

    @Test
    @DisplayName("Misma variable puede declararse en bloques hermanos independientes")
    void testRedeclaracionEnBloquesSeparados() throws Exception {
        // 'tmp' en el if y 'tmp' en el else son ámbitos distintos → válido
        String codigo = "int x = 1;\n"
                + "if (x > 0) {\n"
                + "    int tmp = 10;\n"
                + "    Console.WriteLine(tmp);\n"
                + "} else {\n"
                + "    int tmp = 20;\n"   // mismo nombre, diferente ámbito
                + "    Console.WriteLine(tmp);\n"
                + "}";
        AnalizadorSemantico sem = analizar(codigo);

        assertFalse(sem.tieneErrores(), sem.getErrores().toString());
    }

    @Test
    @DisplayName("Múltiples variables no declaradas generan un error por cada una")
    void testMultiplesVariablesNoDeclaradas() throws Exception {
        // Usar a, b, c sin declararlas
        String codigo = "int resultado = a + b + c;";
        AnalizadorSemantico sem = analizar(codigo);

        assertTrue(sem.tieneErrores());
        // Debe haber exactamente 3 errores: a, b y c
        long erroresNoDeclarada = sem.getErrores().stream()
                .filter(e -> e.getTipoError().equals("NO_DECLARADA"))
                .count();
        assertEquals(3, erroresNoDeclarada);
    }

    @Test
    @DisplayName("Variable usada en do-while sin declarar genera error")
    void testVariableNoDeclaradaEnDoWhile() throws Exception {
        String codigo = "do {\n"
                + "    Console.WriteLine(valor);\n"   // 'valor' no declarada
                + "} while (true);";
        AnalizadorSemantico sem = analizar(codigo);

        assertTrue(sem.tieneErrores());
        assertTrue(sem.getErrores().stream()
                .anyMatch(e -> e.getTipoError().equals("NO_DECLARADA")
                        && e.getMensaje().contains("valor")));
    }

    @Test
    @DisplayName("Programa completo válido no genera ningún error semántico")
    void testProgramaCompletoValido() throws Exception {
        String codigo = "int x = 10;\n"
                + "int y = 20;\n"
                + "int suma = x + y;\n"
                + "if (suma > 25) {\n"
                + "    Console.WriteLine(suma);\n"
                + "} else {\n"
                + "    int diferencia = y - x;\n"
                + "    Console.WriteLine(diferencia);\n"
                + "}\n"
                + "int i = 0;\n"
                + "while (i < 3) {\n"
                + "    i = i + 1;\n"
                + "}\n"
                + "Console.WriteLine(i);";
        AnalizadorSemantico sem = analizar(codigo);

        assertFalse(sem.tieneErrores(), sem.getErrores().toString());
    }
}
