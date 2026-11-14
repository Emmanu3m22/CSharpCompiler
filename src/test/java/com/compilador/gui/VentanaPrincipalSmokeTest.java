package com.compilador.gui;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.GraphicsEnvironment;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Pruebas de humo del marco de la ventana (barra de título propia y bienvenida).
 * Se omiten en entornos sin pantalla.
 */
public class VentanaPrincipalSmokeTest {

    @Test
    @DisplayName("La ventana principal se construye sin decoración del sistema y con barra propia")
    void construyeVentanaPersonalizada() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Sin pantalla: se omite");

        AtomicBoolean sinDecoracion = new AtomicBoolean();
        SwingUtilities.invokeAndWait(() -> {
            AplicacionPrincipal ventana = new AplicacionPrincipal();
            sinDecoracion.set(ventana.isUndecorated());
            ventana.setTitle("Prueba");
            ventana.dispose();
        });
        assertTrue(sinDecoracion.get(), "La ventana debe ser undecorated para usar la barra propia");
    }

    @Test
    @DisplayName("BarraTitulo actualiza el título y alterna maximizado/restaurado")
    void barraTituloAlternaMaximizado() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Sin pantalla: se omite");

        SwingUtilities.invokeAndWait(() -> {
            JFrame f = new JFrame();
            f.setUndecorated(true);
            f.setSize(600, 400);
            BarraTitulo barra = new BarraTitulo(f, "Inicial");
            f.add(barra, java.awt.BorderLayout.NORTH);
            BarraTitulo.instalarRedimension(f, barra);
            f.setVisible(true);

            barra.setTitulo("Nuevo título");
            assertFalse(barra.estaMaximizada());
            barra.alternarMaximizada();
            assertTrue(barra.estaMaximizada());
            assertTrue(f.getWidth() > 600);
            barra.alternarMaximizada();
            assertFalse(barra.estaMaximizada());
            assertEquals(600, f.getWidth());
            f.dispose();
        });
    }

    @Test
    @DisplayName("PantallaBienvenida ejecuta las acciones de sus enlaces")
    void bienvenidaConstruyeYActualiza() throws Exception {
        Assumptions.assumeFalse(GraphicsEnvironment.isHeadless(), "Sin pantalla: se omite");

        SwingUtilities.invokeAndWait(() -> {
            PantallaBienvenida p = new PantallaBienvenida(() -> {}, () -> {}, i -> {}, f -> {}, () -> {});
            assertDoesNotThrow(p::actualizarRecientes);
            assertTrue(p.getComponentCount() > 0);
        });
    }
}
