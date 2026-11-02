package com.compilador.optimizacion.fuente;

import com.compilador.Analizador;
import com.compilador.ast.NodoPrograma;
import com.compilador.errores.ErrorLexico;
import com.compilador.errores.ErrorSintactico;
import com.compilador.errores.FormateadorErrores;
import com.compilador.optimizacion.fuente.VerificacionOptimizado.ErrorVerificacion;
import com.compilador.semantic.AnalizadorSemantico;
import com.compilador.semantic.ErrorSemantico;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Verifica que un texto de código sea válido: análisis léxico, sintáctico y semántico,
 * con los mismos analizadores que usa la aplicación.
 */
public final class VerificadorCodigoOptimizado {

    private VerificadorCodigoOptimizado() {}

    public static VerificacionOptimizado verificar(String codigo) {
        List<ErrorVerificacion> errores = new ArrayList<>();
        if (codigo == null) {
            return VerificacionOptimizado.sinErrores();
        }

        NodoPrograma ast = null;
        Analizador parser = null;
        try {
            parser = new Analizador(new ByteArrayInputStream(codigo.getBytes(StandardCharsets.UTF_8)));
            ast = parser.programa();
        } catch (Throwable e) {
            errores.add(new ErrorVerificacion("Sintáctico", 0, 0,
                    "El analizador no pudo procesar el código",
                    String.valueOf(e.getMessage()).split("\n")[0], ""));
        }

        if (parser != null) {
            for (ErrorLexico e : parser.getErroresLexicos()) {
                FormateadorErrores.ErrorFormateado f = FormateadorErrores.formatearLexico(e);
                errores.add(new ErrorVerificacion("Léxico", e.getLinea(), e.getColumna(),
                        f.getTitulo(), f.getMensaje(), f.getSugerencia()));
            }
            for (ErrorSintactico e : parser.getErroresSintacticos()) {
                FormateadorErrores.ErrorFormateado f = FormateadorErrores.formatearSintactico(e);
                errores.add(new ErrorVerificacion("Sintáctico", e.getLinea(), e.getColumna(),
                        f.getTitulo(), f.getMensaje(), f.getSugerencia()));
            }
        }

        // El análisis semántico solo tiene sentido si el código tiene estructura válida
        if (ast != null && errores.isEmpty()) {
            try {
                for (ErrorSemantico e : new AnalizadorSemantico().analizar(ast)) {
                    errores.add(new ErrorVerificacion("Semántico", e.getLinea(), e.getColumna(),
                            e.getTipoError(), e.getMensaje(), ""));
                }
            } catch (Throwable e) {
                errores.add(new ErrorVerificacion("Semántico", 0, 0,
                        "El análisis semántico falló", String.valueOf(e.getMessage()), ""));
            }
        }
        return new VerificacionOptimizado(errores);
    }
}
