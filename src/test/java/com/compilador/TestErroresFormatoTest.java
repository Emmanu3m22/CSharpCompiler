package com.compilador;

import com.compilador.errores.ErrorLexico;
import com.compilador.errores.ErrorSintactico;
import com.compilador.errores.FormateadorErrores;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import static org.junit.jupiter.api.Assertions.*;

public class TestErroresFormatoTest {

    @Test
    @DisplayName("Verifica que TestErrores.cs genera mensajes limpios y comprensibles en el FormateadorErrores")
    void testFormatoEnTestErrores() throws Exception {
        File file = new File("TestErrores.cs");
        assertTrue(file.exists());

        Analizador parser = new Analizador(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        try {
            parser.programa();
        } catch (Exception ignored) { }

        var erroresSint = parser.getErroresSintacticos();
        var erroresLex = parser.getErroresLexicos();

        System.out.println("=== ERRORES LÉXICOS FORMATEADOS ===");
        for (ErrorLexico e : erroresLex) {
            var info = FormateadorErrores.formatearLexico(e);
            System.out.printf("[%d:%d] %s: %s (Carácter: '%s') -> Sugerencia: %s%n",
                e.getLinea(), e.getColumna(), info.getTitulo(), info.getMensaje(), e.getLexema(), info.getSugerencia());
            assertFalse(info.getMensaje().isEmpty());
        }

        System.out.println("\n=== ERRORES SINTÁCTICOS FORMATEADOS ===");
        for (ErrorSintactico e : erroresSint) {
            var info = FormateadorErrores.formatearSintactico(e);
            System.out.printf("[%d:%d] %s: %s (Token: '%s') [Esperado: %s] -> Sugerencia: %s%n",
                e.getLinea(), e.getColumna(), info.getTitulo(), info.getMensaje(), e.getTokenEncontrado(), info.getTokenEsperado(), info.getSugerencia());
            assertFalse(info.getMensaje().isEmpty());
            // Ningún mensaje debe contener texto crudo feo de JavaCC
            assertFalse(info.getMensaje().contains("Encountered \""), "No debe tener texto crudo de JavaCC");
            assertFalse(info.getMensaje().contains("Was expecting"), "No debe tener texto crudo de JavaCC");
        }

        assertTrue(erroresSint.size() >= 15, "Debe detectar al menos 15 errores sintácticos");
    }
}
