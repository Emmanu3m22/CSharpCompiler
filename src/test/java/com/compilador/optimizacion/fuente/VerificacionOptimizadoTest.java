package com.compilador.optimizacion.fuente;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class VerificacionOptimizadoTest {

    private static final String VALIDO = """
            class C
            {
                static void Main()
                {
                    int a = 1;
                    Console.WriteLine(a);
                }
            }
            """;

    @Test
    @DisplayName("Código válido: sin errores")
    void codigoValido() {
        VerificacionOptimizado v = VerificadorCodigoOptimizado.verificar(VALIDO);
        assertTrue(v.valido(), v.errores().toString());
    }

    @Test
    @DisplayName("Error sintáctico: se reporta fase, línea y mensaje")
    void errorSintactico() {
        String roto = VALIDO.replace("int a = 1;", "int a = ;");
        VerificacionOptimizado v = VerificadorCodigoOptimizado.verificar(roto);
        assertFalse(v.valido());
        VerificacionOptimizado.ErrorVerificacion e = v.errores().get(0);
        assertEquals("Sintáctico", e.fase());
        assertTrue(e.linea() > 0);
        assertFalse(e.mensaje().isBlank());
        assertTrue(e.comoTexto().contains("línea"));
    }

    @Test
    @DisplayName("Error semántico: variable no declarada")
    void errorSemantico() {
        String roto = VALIDO.replace("int a = 1;", "int b = 1;");
        VerificacionOptimizado v = VerificadorCodigoOptimizado.verificar(roto);
        assertFalse(v.valido());
        assertTrue(v.errores().stream().anyMatch(e -> e.fase().equals("Semántico")), v.errores().toString());
    }

    @Test
    @DisplayName("Error léxico: carácter inválido")
    void errorLexico() {
        String roto = VALIDO.replace("int a = 1;", "int a = 1; `");
        VerificacionOptimizado v = VerificadorCodigoOptimizado.verificar(roto);
        assertFalse(v.valido());
    }

    @Test
    @DisplayName("El pipeline entrega verificación válida y resultado aplicable")
    void pipelineValido() {
        String codigo = VALIDO.replace("int a = 1;", "int a = 1;\n        int muerta = 5;");
        ResultadoOptimizacionFuente res = OptimizadorFuente.crearPorDefecto().optimizar(codigo);
        assertTrue(res.verificacion().valido());
        assertTrue(res.aplicable());
        assertFalse(res.rechazada());
        assertFalse(res.codigoFinal().contains("muerta"));
    }

    @Test
    @DisplayName("Si el código optimizado es inválido se rechaza: queda el original y se explica por qué")
    void optimizacionRechazada() {
        PaseOptimizacionFuente danino = new PaseOptimizacionFuente() {
            public String nombre() { return "Dañino"; }
            public String descripcion() { return "devuelve un texto final inválido"; }
            public EtapaOptimizacion aplicar(CodigoFuente f) {
                // La máscara es válida (pasa el chequeo del pase), pero el texto final no
                return new EtapaOptimizacion(nombre(), descripcion(), f.getTexto(),
                        f.getTexto().replace("int a = 1;", "int a = ;"), f.getTexto(),
                        List.of(new Hallazgo(5, Hallazgo.Tipo.VARIABLE_SIN_USO, "x", "falso")),
                        List.of(new RangoTexto(0, 1)), 1L);
            }
        };
        ResultadoOptimizacionFuente res = new OptimizadorFuente().agregarPase(danino).optimizar(VALIDO);

        assertTrue(res.rechazada());
        assertFalse(res.aplicable());
        assertEquals(VALIDO, res.codigoFinal(), "Debe quedar el código original");
        assertNull(res.astLimpio());
        assertEquals(0, res.totalEliminaciones());
        assertTrue(res.codigoRechazado().contains("int a = ;"));
        assertFalse(res.verificacion().valido());
        assertFalse(res.advertencias().isEmpty());
        assertTrue(res.advertencias().get(0).contains("descartó"));
    }

    @Test
    @DisplayName("No se rechaza si el original ya tenía los mismos errores")
    void noRechazaSiOriginalYaTeniaErrores() {
        String conError = VALIDO.replace("int a = 1;", "int a = ;");
        ResultadoOptimizacionFuente res = OptimizadorFuente.crearPorDefecto().optimizar(conError);
        assertFalse(res.rechazada());
        assertFalse(res.aplicable(), "Con errores no es aplicable");
    }
}
