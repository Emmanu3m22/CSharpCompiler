package com.compilador.gui;

import com.compilador.optimizacion.fuente.*;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.awt.GraphicsEnvironment;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Prueba de humo de la GUI: se omite en entornos sin pantalla.
 * Verifica que todos los estados del panel se cargan sin excepciones.
 */
public class PanelOptimizacionSmokeTest {

    private static final String VALIDO = """
            class C
            {
                static void Main()
                {
                    int a = 1;
                    int muerta = 2;
                    Console.WriteLine(a);
                }
            }
            """;

    @Test
    @DisplayName("El panel carga resultado válido, rechazado, omitido y vacío sin fallar")
    void cargaTodosLosEstados() {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Sin pantalla: se omite");

        PanelOptimizacion panel = new PanelOptimizacion((l, c) -> {}, codigo -> {}, () -> "ventas.cs");

        assertDoesNotThrow(() -> panel.cargar(OptimizadorFuente.crearPorDefecto().optimizar(VALIDO)));

        PaseOptimizacionFuente danino = new PaseOptimizacionFuente() {
            public String nombre() { return "Dañino"; }
            public String descripcion() { return "texto final inválido"; }
            public EtapaOptimizacion aplicar(CodigoFuente f) {
                return new EtapaOptimizacion(nombre(), descripcion(), f.getTexto(),
                        f.getTexto().replace("int a = 1;", "int a = ;"), f.getTexto(),
                        List.of(new Hallazgo(5, Hallazgo.Tipo.VARIABLE_SIN_USO, "x", "falso")),
                        List.of(new RangoTexto(0, 1)), 1L);
            }
        };
        ResultadoOptimizacionFuente rechazado = new OptimizadorFuente().agregarPase(danino).optimizar(VALIDO);
        assertTrue(rechazado.rechazada());
        assertDoesNotThrow(() -> panel.cargar(rechazado));

        assertDoesNotThrow(() -> panel.cargar(ResultadoOptimizacionFuente.omitida("x", "motivo")));
        assertDoesNotThrow(() -> panel.mostrarOmitida("errores", "x"));
        assertDoesNotThrow(() -> panel.cargar(null));
        assertDoesNotThrow(panel::limpiar);
    }
}
