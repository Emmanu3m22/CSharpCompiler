package com.compilador.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Contenedor del resumen del análisis, contraíble como el panel inferior de un IDE.
 *
 * Siempre muestra una barra de encabezado (clic para contraer o expandir). Cuando el
 * panel está contraído, la barra enseña un resumen en una línea (tokens y errores) para
 * que se sepa qué hay dentro sin abrirlo.
 */
public class ResumenColapsable extends JPanel {

    private static final int ALTO_BARRA = 26;

    private final PanelResumen resumen;
    private final JLabel flecha = new JLabel();
    private final JLabel titulo = new JLabel("RESUMEN");
    private final JLabel indicador = new JLabel();

    private boolean colapsado = false;

    public ResumenColapsable(PanelResumen resumen) {
        this.resumen = resumen;
        setLayout(new BorderLayout());
        setBackground(Colores.FONDO_PANEL);

        JPanel barra = new JPanel(new BorderLayout(10, 0));
        barra.setBackground(Colores.FONDO_TOOLBAR);
        barra.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 0, 0, Colores.BORDE),
                BorderFactory.createEmptyBorder(0, 12, 0, 12)));
        barra.setPreferredSize(new Dimension(100, ALTO_BARRA));
        barra.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        flecha.setFont(Colores.FUENTE_PEQUENA.deriveFont(11f));
        flecha.setForeground(Colores.TEXTO_TENUE);
        titulo.setFont(Colores.FUENTE_PEQUENA.deriveFont(Font.BOLD, 11f));
        titulo.setForeground(Colores.TEXTO_TENUE);
        indicador.setFont(Colores.FUENTE_PEQUENA.deriveFont(12f));

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        izquierda.setOpaque(false);
        izquierda.add(flecha);
        izquierda.add(titulo);
        JPanel envoltorio = new JPanel(new GridBagLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(izquierda);
        barra.add(envoltorio, BorderLayout.WEST);

        JPanel centro = new JPanel(new GridBagLayout());
        centro.setOpaque(false);
        centro.add(indicador);
        JPanel alineado = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        alineado.setOpaque(false);
        alineado.add(centro);
        barra.add(alineado, BorderLayout.CENTER);

        MouseAdapter clic = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                alternar();
            }
        };
        for (Component c : new Component[]{barra, envoltorio, izquierda, flecha, titulo, centro, alineado, indicador}) {
            c.addMouseListener(clic);
        }

        add(barra, BorderLayout.NORTH);
        add(resumen, BorderLayout.CENTER);

        colapsar(PreferenciasApp.resumenColapsado());
    }

    /** Texto actual del indicador de la barra (vacío cuando el panel está expandido). */
    String textoIndicador() {
        return indicador.getText();
    }

    public boolean estaColapsado() {
        return colapsado;
    }

    /** Alterna entre contraído y expandido y recuerda la elección. */
    public void alternar() {
        colapsar(!colapsado);
        PreferenciasApp.setResumenColapsado(colapsado);
    }

    void colapsar(boolean contraer) {
        colapsado = contraer;
        resumen.setVisible(!contraer);
        flecha.setText(contraer ? "\u25B8" : "\u25BE");
        setToolTipText(contraer
                ? "Mostrar el resumen del análisis (Ctrl+J)"
                : "Ocultar el resumen del análisis (Ctrl+J)");
        actualizarIndicador();
        revalidate();
        repaint();
    }

    /** Refresca el resumen de una línea que se ve cuando el panel está contraído. */
    public void actualizarIndicador() {
        if (colapsado) {
            indicador.setText(resumen.textoCorto());
            indicador.setForeground(resumen.colorEstado());
        } else {
            indicador.setText("");
        }
    }
}
