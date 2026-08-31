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
}
