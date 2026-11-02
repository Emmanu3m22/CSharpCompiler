package com.compilador.optimizacion.fuente;

import com.compilador.ast.NodoPrograma;
import com.compilador.intermedio.GeneradorCodigoIntermedio;
import com.compilador.intermedio.ProgramaIntermedio;
import com.compilador.intermedio.Valor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class IntegracionCodigoMuertoTest {

    @Test
    @DisplayName("AST limpio genera menos cuádruplos y conserva los mismos valores de variables vivas")
    void testIntegracionCodigoIntermedio() {
        String codigo = """
                int a = 5;
                int b = 3;
                int r1 = a + b;
                int basura = 50 * 2 + 10;
                Console.WriteLine(r1);
                """;

        OptimizadorFuente opt = OptimizadorFuente.crearPorDefecto();
        ResultadoOptimizacionFuente res = opt.optimizar(codigo);

        assertNotNull(res.astLimpio(), "El AST limpio debe generarse exitosamente");
        assertEquals(1, res.totalEliminaciones());

        // Generar código intermedio para el AST original (re-parseado)
        GeneradorCodigoIntermedio gen = new GeneradorCodigoIntermedio();
        com.compilador.Analizador parserOriginal = new com.compilador.Analizador(
                new java.io.ByteArrayInputStream(codigo.getBytes(java.nio.charset.StandardCharsets.UTF_8)));
        NodoPrograma astOriginal;
        try {
            astOriginal = parserOriginal.programa();
        } catch (Exception e) {
            fail("El código original debe parsear sin errores: " + e.getMessage());
            return;
        }

        ProgramaIntermedio progOriginal = gen.generar(astOriginal);
        ProgramaIntermedio progLimpio = gen.generar(res.astLimpio());

        // El AST limpio debe tener estrictamente menos cuádruplos porque 'basura' fue eliminada
        assertTrue(progLimpio.totalCuadruplos() < progOriginal.totalCuadruplos(),
                String.format("El código limpio debe tener menos cuádruplos (Original: %d, Limpio: %d)",
                        progOriginal.totalCuadruplos(), progLimpio.totalCuadruplos()));

        // El valor final de la variable viva 'r1' debe ser idéntico (8)
        Valor valOriginal = progOriginal.entorno().obtener("r1");
        Valor valLimpio = progLimpio.entorno().obtener("r1");

        assertNotNull(valOriginal);
        assertNotNull(valLimpio);
        assertEquals(valOriginal, valLimpio, "El valor de la variable viva 'r1' debe ser idéntico");
        assertEquals(8, valLimpio.comoEntero());

        // La variable muerta 'basura' NO debe existir en el entorno del código limpio
        assertFalse(progLimpio.entorno().conoce("basura"), "'basura' no debe existir en el entorno limpio");
    }

    @Test
    @DisplayName("Idempotencia: Optimizar un código ya limpio no genera nuevas eliminaciones")
    void testIdempotencia() {
        String codigo = """
                int a = 5;
                int b = 3;
                int r1 = a + b;
                int basura = 50 * 2 + 10;
                Console.WriteLine(r1);
                """;

        OptimizadorFuente opt = OptimizadorFuente.crearPorDefecto();
        ResultadoOptimizacionFuente res1 = opt.optimizar(codigo);
        assertTrue(res1.totalEliminaciones() > 0);

        // Segunda pasada sobre el código ya limpio
        ResultadoOptimizacionFuente res2 = opt.optimizar(res1.codigoFinal());
        assertEquals(0, res2.totalEliminaciones(), "Una segunda pasada no debe encontrar más código muerto");
        assertEquals(res1.codigoFinal().trim(), res2.codigoFinal().trim());
    }

    @Test
    @DisplayName("Las líneas de los nodos en el AST limpio coinciden con las originales")
    void testLineasOriginalesConservadas() {
        String codigo = """
                int a = 5;
                int muerto = 100;
                Console.WriteLine(a);
                """;

        OptimizadorFuente opt = OptimizadorFuente.crearPorDefecto();
        ResultadoOptimizacionFuente res = opt.optimizar(codigo);

        assertNotNull(res.astLimpio());
        // En el AST original, Console.WriteLine(a) está en la línea 3
        // En el AST limpio generado desde el código enmascarado, debe seguir estando en la línea 3
        assertEquals(2, res.astLimpio().getSentencias().size());
        int lineaWriteLine = res.astLimpio().getSentencias().get(1).getLinea();
        assertEquals(3, lineaWriteLine, "La línea de Console.WriteLine debe ser 3 gracias al enmascaramiento");
    }
}
