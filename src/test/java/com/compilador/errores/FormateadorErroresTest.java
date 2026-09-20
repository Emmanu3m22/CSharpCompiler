package com.compilador.errores;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class FormateadorErroresTest {

    @Test
    @DisplayName("Formatea correctamente error de punto y coma faltante")
    void testFaltaPuntoYComa() {
        String javaccMsg = "Encountered \"int\" at line 10, column 5.\nWas expecting:\n    \";\"\n";
        ErrorSintactico error = new ErrorSintactico("int", ";", javaccMsg, 10, 5);

        FormateadorErrores.ErrorFormateado info = FormateadorErrores.formatearSintactico(error);

        assertEquals("Falta Punto y Coma", info.getTitulo());
        assertTrue(info.getMensaje().contains("punto y coma ';'"), "Debe mencionar punto y coma");
        assertEquals(";", info.getTokenEsperado());
        assertFalse(info.getSugerencia().isEmpty());
    }

    @Test
    @DisplayName("Formatea instrucción iniciada con literal numérico")
    void testLiteralNumericoAlInicio() {
        String javaccMsg = "Encountered \"100\" at line 12, column 5.\nWas expecting one of:\n    \"class\" ...\n    \"public\" ...\n";
        ErrorSintactico error = new ErrorSintactico("100", "...", javaccMsg, 12, 5);

        FormateadorErrores.ErrorFormateado info = FormateadorErrores.formatearSintactico(error);

        assertEquals("Instrucción Inválida", info.getTitulo());
        assertTrue(info.getMensaje().contains("valor numérico"), "Debe advertir sobre valor numérico");
    }

    @Test
    @DisplayName("Preserva y mejora mensajes personalizados del parser")
    void testMensajePersonalizado() {
        ErrorSintactico error = new ErrorSintactico("{", "...", "Se esperaba el nombre de la clase o herencia válida", 7, 5);

        FormateadorErrores.ErrorFormateado info = FormateadorErrores.formatearSintactico(error);

        assertEquals("Error en Declaración de Clase", info.getTitulo());
        assertEquals("Se esperaba el nombre de la clase o herencia válida", info.getMensaje());
        assertTrue(info.getSugerencia().contains("nombre para la clase"));
    }

    @Test
    @DisplayName("Formatea errores léxicos específicos como #, $ y escape")
    void testErroresLexicos() {
        ErrorLexico errorHash = new ErrorLexico("#", "Símbolo no reconocido", 10, 1);
        FormateadorErrores.ErrorFormateado infoHash = FormateadorErrores.formatearLexico(errorHash);
        assertTrue(infoHash.getMensaje().contains("preprocesador"));

        ErrorLexico errorDolar = new ErrorLexico("$", "Símbolo no reconocido", 56, 30);
        FormateadorErrores.ErrorFormateado infoDolar = FormateadorErrores.formatearLexico(errorDolar);
        assertTrue(infoDolar.getMensaje().contains("interpolación"));

        ErrorLexico errorEscape = new ErrorLexico("\\", "Símbolo no reconocido", 65, 27);
        FormateadorErrores.ErrorFormateado infoEscape = FormateadorErrores.formatearLexico(errorEscape);
        assertTrue(infoEscape.getMensaje().contains("secuencia de escape"));
    }
}
