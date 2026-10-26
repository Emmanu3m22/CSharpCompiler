package com.compilador.optimizacion.fuente;

import com.compilador.gui.Ejemplos;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class PipelineOptimizacionTest {

    @Test
    @DisplayName("Todos los ejemplos del compilador se procesan sin excepciones")
    void testTodosLosEjemplos() {
        OptimizadorFuente opt = OptimizadorFuente.crearPorDefecto();

        for (int i = 0; i < Ejemplos.cantidad(); i++) {
            String codigo = Ejemplos.getCodigo(i);
            String nombre = Ejemplos.getNombre(i);

            assertDoesNotThrow(() -> {
                ResultadoOptimizacionFuente res = opt.optimizar(codigo);
                assertNotNull(res);
                assertNotNull(res.codigoFinal());
            }, "El ejemplo '" + nombre + "' debe procesarse sin errores");
        }
    }

    @Test
    @DisplayName("El ejemplo de Eliminación de Código Muerto elimina múltiples tipos de código muerto")
    void testEjemploCodigoMuerto() {
        int idx = Ejemplos.cantidad() - 1;
        assertEquals("Eliminación de Código Muerto", Ejemplos.getNombre(idx));

        String codigo = Ejemplos.getCodigo(idx);
        OptimizadorFuente opt = OptimizadorFuente.crearPorDefecto();
        ResultadoOptimizacionFuente res = opt.optimizar(codigo);

        assertTrue(res.totalEliminaciones() >= 4,
                "El ejemplo de código muerto debe activar al menos 4 eliminaciones (R1, R2, R3, R4)");

        // Verificar que el código limpio no contiene las variables ni bloques muertos
        assertFalse(res.codigoFinal().contains("basura1"));
        assertFalse(res.codigoFinal().contains("muerto1"));
        assertFalse(res.codigoFinal().contains("muerto2"));
        assertFalse(res.codigoFinal().contains("muertoElse"));
        assertFalse(res.codigoFinal().contains("codigoInalcanzable"));

        // Y que sí conserva las variables y operaciones vivas
        assertTrue(res.codigoFinal().contains("int r1 = a + b * 2;"));
        assertTrue(res.codigoFinal().contains("Console.WriteLine(r1);"));
        assertTrue(res.codigoFinal().contains("return 0;"));

        // Y que el código limpio no contiene comentarios
        assertFalse(res.codigoFinal().contains("//"));
        assertFalse(res.codigoFinal().contains("/*"));
    }
}
