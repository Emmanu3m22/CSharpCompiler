package com.compilador.gui;

import com.compilador.errores.ErrorLexico;
import com.compilador.errores.ErrorSintactico;

import javax.swing.*;
import java.awt.*;
import java.util.List;

/**
 * Panel elegante para mostrar errores léxicos y sintácticos.
 * Utiliza una paleta de azul marino con colores pastel.
 */
public class PanelErrores extends JPanel {

    private final JPanel panelErroresLexicos;
    private final JPanel panelErroresSintacticos;
    private JLabel labelCountLex;
    private JLabel labelCountSint;

    public PanelErrores() {
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Colores.FONDO_PRINCIPAL);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ── Sección de Errores Léxicos ──
        add(crearSeccionErrores(true));
        panelErroresLexicos = new JPanel();
        panelErroresLexicos.setLayout(new BoxLayout(panelErroresLexicos, BoxLayout.Y_AXIS));
        panelErroresLexicos.setBackground(Colores.FONDO_PANEL);
        panelErroresLexicos.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scrollLex = new JScrollPane(panelErroresLexicos);
        scrollLex.setBackground(Colores.FONDO_PANEL);
        scrollLex.setBorder(BorderFactory.createLineBorder(Colores.BORDE, 1));
        scrollLex.getViewport().setBackground(Colores.FONDO_PANEL);
        add(scrollLex);
        add(Box.createVerticalStrut(12));

        // ── Sección de Errores Sintácticos ──
        add(crearSeccionErrores(false));
        panelErroresSintacticos = new JPanel();
        panelErroresSintacticos.setLayout(new BoxLayout(panelErroresSintacticos, BoxLayout.Y_AXIS));
        panelErroresSintacticos.setBackground(Colores.FONDO_PANEL);
        panelErroresSintacticos.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scrollSint = new JScrollPane(panelErroresSintacticos);
        scrollSint.setBackground(Colores.FONDO_PANEL);
        scrollSint.setBorder(BorderFactory.createLineBorder(Colores.BORDE, 1));
        scrollSint.getViewport().setBackground(Colores.FONDO_PANEL);
        add(scrollSint);

        add(Box.createVerticalGlue());
    }

    /**
     * Crea el encabezado de una sección de errores.
     */
    private JPanel crearSeccionErrores(boolean esLexico) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(Colores.FONDO_CONTENEDOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, 
                esLexico ? Colores.ERROR : Colores.ADVERTENCIA),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        // Icono y título
        JLabel titulo = new JLabel(esLexico ? "🔴 Errores Léxicos" : "⚠️ Errores Sintácticos");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titulo.setForeground(esLexico ? Colores.ERROR : Colores.ADVERTENCIA);
        panel.add(titulo, BorderLayout.WEST);

        // Contador
        JLabel contador = new JLabel("0");
        contador.setFont(new Font("Segoe UI", Font.BOLD, 13));
        contador.setForeground(Colores.TEXTO_NORMAL);
        panel.add(contador, BorderLayout.EAST);

        if (esLexico) {
            labelCountLex = contador;
        } else {
            labelCountSint = contador;
        }

        return panel;
    }

    /**
     * Carga y muestra los errores léxicos.
     */
    public void cargarErroresLexicos(List<ErrorLexico> errores) {
        panelErroresLexicos.removeAll();
        labelCountLex.setText(String.valueOf(errores.size()));

        if (errores.isEmpty()) {
            JLabel labelVacio = new JLabel("✓ Sin errores léxicos");
            labelVacio.setFont(Colores.FUENTE_NORMAL);
            labelVacio.setForeground(Colores.EXITO);
            panelErroresLexicos.add(labelVacio);
        } else {
            for (ErrorLexico error : errores) {
                panelErroresLexicos.add(crearItemError(error.getLexema(), error.getMensaje(),
                    error.getLinea(), error.getColumna(), true));
                panelErroresLexicos.add(Box.createVerticalStrut(8));
            }
        }

        panelErroresLexicos.revalidate();
        panelErroresLexicos.repaint();
    }

    /**
     * Carga y muestra los errores sintácticos.
     */
    public void cargarErroresSintacticos(List<ErrorSintactico> errores) {
        panelErroresSintacticos.removeAll();
        labelCountSint.setText(String.valueOf(errores.size()));

        if (errores.isEmpty()) {
            JLabel labelVacio = new JLabel("✓ Sin errores sintácticos");
            labelVacio.setFont(Colores.FUENTE_NORMAL);
            labelVacio.setForeground(Colores.EXITO);
            panelErroresSintacticos.add(labelVacio);
        } else {
            for (ErrorSintactico error : errores) {
                panelErroresSintacticos.add(crearItemError(
                    error.getTokenEncontrado(),
                    error.getMensaje() + (error.getTokenEsperado() != null ? 
                        " (esperaba: " + error.getTokenEsperado() + ")" : ""),
                    error.getLinea(), error.getColumna(), false));
                panelErroresSintacticos.add(Box.createVerticalStrut(8));
            }
        }

        panelErroresSintacticos.revalidate();
        panelErroresSintacticos.repaint();
    }

    /**
     * Crea un item visual para mostrar un error con diseño elegante.
     */
    private JPanel crearItemError(String lexema, String mensaje, int linea, int columna, boolean esLexico) {
        JPanel itemPanel = new JPanel(new BorderLayout(8, 4));
        itemPanel.setBackground(Colores.FONDO_TABLA_ROW1);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Lado izquierdo: Icono y contenido
        JPanel contenido = new JPanel(new GridLayout(3, 1, 0, 3));
        contenido.setBackground(Colores.FONDO_TABLA_ROW1);

        // Lexema/Token encontrado
        JLabel labelLexema = new JLabel("Token: " + lexema);
        labelLexema.setFont(new Font("Consolas", Font.BOLD, 12));
        labelLexema.setForeground(esLexico ? Colores.ERROR : Colores.ADVERTENCIA);
        contenido.add(labelLexema);

        // Mensaje de error
        JLabel labelMensaje = new JLabel(mensaje);
        labelMensaje.setFont(Colores.FUENTE_NORMAL);
        labelMensaje.setForeground(Colores.TEXTO_NORMAL);
        labelMensaje.setVerticalAlignment(SwingConstants.TOP);
        contenido.add(labelMensaje);

        // Ubicación
        JLabel labelUbicacion = new JLabel("Línea " + linea + ", Columna " + columna);
        labelUbicacion.setFont(Colores.FUENTE_PEQUENA);
        labelUbicacion.setForeground(Colores.TEXTO_TENUE);
        contenido.add(labelUbicacion);

        itemPanel.add(contenido, BorderLayout.CENTER);

        // Lado derecho: Icono visual
        JLabel iconoError = new JLabel(esLexico ? "⚫" : "⚠");
        iconoError.setFont(new Font("Arial", Font.PLAIN, 20));
        iconoError.setHorizontalAlignment(SwingConstants.CENTER);
        iconoError.setPreferredSize(new Dimension(30, 60));
        itemPanel.add(iconoError, BorderLayout.EAST);

        // Hover effect
        itemPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                itemPanel.setBackground(Colores.FONDO_SELECCION);
                contenido.setBackground(Colores.FONDO_SELECCION);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colores.BORDE_ENFOCADO, 2),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                itemPanel.setBackground(Colores.FONDO_TABLA_ROW1);
                contenido.setBackground(Colores.FONDO_TABLA_ROW1);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colores.BORDE, 1),
                    BorderFactory.createEmptyBorder(8, 8, 8, 8)
                ));
            }
        });

        return itemPanel;
    }

    /**
     * Limpia todos los errores mostrados.
     */
    public void limpiar() {
        panelErroresLexicos.removeAll();
        panelErroresSintacticos.removeAll();
        labelCountLex.setText("0");
        labelCountSint.setText("0");

        // Mostrar mensajes de espera
        JLabel lblEspLex = new JLabel("Esperando análisis...");
        lblEspLex.setFont(Colores.FUENTE_NORMAL);
        lblEspLex.setForeground(Colores.TEXTO_TENUE);
        panelErroresLexicos.add(lblEspLex);

        JLabel lblEspSint = new JLabel("Esperando análisis...");
        lblEspSint.setFont(Colores.FUENTE_NORMAL);
        lblEspSint.setForeground(Colores.TEXTO_TENUE);
        panelErroresSintacticos.add(lblEspSint);

        panelErroresLexicos.revalidate();
        panelErroresSintacticos.revalidate();
        panelErroresLexicos.repaint();
        panelErroresSintacticos.repaint();
    }
}
