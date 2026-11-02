package com.compilador.optimizacion.fuente;

import com.compilador.Analizador;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;

/**
 * Utilidad para comprobar que un texto sigue siendo C# válido para el parser.
 */
final class ValidadorSintaxis {

    private ValidadorSintaxis() {}

    /**
     * Cuenta los errores léxicos y sintácticos del código.
     *
     * @return número de errores, o {@link Integer#MAX_VALUE} si el parser lanzó una excepción.
     */
    static int contarErrores(String codigo) {
        try {
            Analizador parser = new Analizador(
                    new ByteArrayInputStream(codigo.getBytes(StandardCharsets.UTF_8)));
            parser.programa();
            return parser.getErroresLexicos().size() + parser.getErroresSintacticos().size();
        } catch (Throwable e) {
            return Integer.MAX_VALUE;
        }
    }
}
