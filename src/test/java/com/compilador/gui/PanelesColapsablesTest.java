package com.compilador.gui;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Paneles contraíbles del IDE. Usan solo componentes ligeros, así que corren también sin pantalla.
 * No se llama a {@code alternar()} (persiste preferencias reales del usuario) salvo restaurándolas.
 */
public class PanelesColapsablesTest {

    // ───────────────────────── PanelResumen ─────────────────────────

    @Test
    @DisplayName("PanelResumen: resumen corto, total de errores, color y notificación de cambios")
    void resumenCorto() {
        PanelResumen r = new PanelResumen();
        AtomicInteger cambios = new AtomicInteger();
        r.setAlCambiar(cambios::incrementAndGet);

        assertEquals("Sin análisis", r.textoCorto());
        assertEquals(Colores.TEXTO_TENUE, r.colorEstado());

        r.actualizar(10, 0, 0, 0);
        assertTrue(r.textoCorto().contains("10 tokens"));
        assertTrue(r.textoCorto().contains("sin errores"));
        assertEquals(0, r.totalErrores());
        assertEquals(Colores.EXITO, r.colorEstado());

        r.actualizar(5, 1, 2, 3);
        assertEquals(6, r.totalErrores());
        assertTrue(r.textoCorto().contains("6 errores"), r.textoCorto());
        assertEquals(Colores.ERROR, r.colorEstado());

        r.actualizar(5, 1, 0, 0);
        assertTrue(r.textoCorto().contains("1 error") && !r.textoCorto().contains("1 errores"));

        r.limpiar();
        assertEquals("Sin análisis", r.textoCorto());
        assertEquals(0, r.totalErrores());
        assertEquals(4, cambios.get(), "Debe avisar en cada actualizar() y limpiar()");
    }

    // ───────────────────────── ResumenColapsable ─────────────────────────

    @Test
    @DisplayName("ResumenColapsable: al contraer oculta el resumen y muestra el indicador de una línea")
    void resumenColapsable() {
        PanelResumen r = new PanelResumen();
        ResumenColapsable c = new ResumenColapsable(r);

        c.colapsar(false);
        assertFalse(c.estaColapsado());
        assertTrue(r.isVisible());
        assertEquals("", c.textoIndicador(), "Expandido no repite el resumen en la barra");

        r.actualizar(7, 0, 1, 0);
        c.colapsar(true);
        assertTrue(c.estaColapsado());
        assertFalse(r.isVisible());
        assertTrue(c.textoIndicador().contains("7 tokens"), c.textoIndicador());
        assertTrue(c.textoIndicador().contains("1 error"));

        // El indicador se mantiene al día mientras está contraído
        r.actualizar(9, 0, 0, 0);
        c.actualizarIndicador();
        assertTrue(c.textoIndicador().contains("sin errores"));

        c.colapsar(false);
        assertTrue(r.isVisible());
        assertEquals("", c.textoIndicador());
    }

    // ───────────────────────── ZonaCentralIde ─────────────────────────

    private static JTabbedPane crearPestanas() {
        JTabbedPane t = new JTabbedPane();
        t.addTab("Errores", new JPanel());
        t.addTab("Léxico", new JPanel());
        t.addTab("Semántico", new JPanel());
        return t;
    }

    private static JPanel buscarRiel(ZonaCentralIde zona) {
        for (Component c : zona.getComponents()) {
            if (c instanceof JPanel p && p.getPreferredSize().width == 30) return p;
        }
        fail("No se encontró el riel lateral");
        return null;
    }

    @Test
    @DisplayName("ZonaCentralIde: contraer reubica el editor y deja un riel con una entrada por pestaña")
    void zonaCentral() {
        JPanel editor = new JPanel();
        JTabbedPane pestanas = crearPestanas();
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editor, pestanas);
        ZonaCentralIde zona = new ZonaCentralIde(editor, split, pestanas);

        zona.colapsar(false);
        assertFalse(zona.estaColapsado());
        assertSame(split, editor.getParent());
        assertSame(zona, split.getParent());
        JPanel riel = buscarRiel(zona);
        assertEquals(3, riel.getComponentCount(), "Expandido: alternador + etiqueta + espacio");

        zona.colapsar(true);
        assertTrue(zona.estaColapsado());
        assertSame(zona, editor.getParent(), "El editor ocupa todo el ancho");
        assertNull(split.getParent());
        assertEquals(pestanas.getTabCount() + 2, riel.getComponentCount(),
                "Contraído: alternador + una entrada por pestaña + espacio");

        zona.setConteoErrores(4);   // reconstruye el riel con el contador, sin romperse
        assertEquals(pestanas.getTabCount() + 2, riel.getComponentCount());

        zona.colapsar(false);
        assertSame(split, editor.getParent());
        assertSame(zona, split.getParent());
        assertSame(editor, split.getLeftComponent());
        assertSame(pestanas, split.getRightComponent());
    }

    @Test
    @DisplayName("ZonaCentralIde: mostrarPestana expande el panel y selecciona la pestaña")
    void mostrarPestanaExpande() {
        boolean preferenciaOriginal = PreferenciasApp.analisisColapsado();
        try {
            JPanel editor = new JPanel();
            JTabbedPane pestanas = crearPestanas();
            JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editor, pestanas);
            ZonaCentralIde zona = new ZonaCentralIde(editor, split, pestanas);

            zona.colapsar(true);
            zona.mostrarPestana(2);
            assertFalse(zona.estaColapsado());
            assertEquals(2, pestanas.getSelectedIndex());

            zona.mostrarPestana(99);   // índice inválido: no falla ni cambia la selección
            assertEquals(2, pestanas.getSelectedIndex());
        } finally {
            PreferenciasApp.setAnalisisColapsado(preferenciaOriginal);
        }
    }
}
