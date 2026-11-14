package com.compilador.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

/**
 * Zona central del IDE: editor a la izquierda y panel de análisis (errores, léxico,
 * sintáctico, semántico, intermedio, optimización) a la derecha.
 *
 * El panel de análisis se puede contraer. Al contraerse queda un riel vertical con el
 * nombre de cada pestaña (y el número de errores), como las barras laterales de un IDE;
 * al hacer clic en una pestaña del riel el panel se expande directamente en ella.
 */
public class ZonaCentralIde extends JPanel {

    private static final int ANCHO_RIEL = 30;

    private final JComponent editor;
    private final JSplitPane split;
    private final JTabbedPane pestanas;
    private final JPanel riel = new JPanel();

    private boolean colapsado = false;
    private int ubicacionDivisor = 500;
    private int errores = 0;

    public ZonaCentralIde(JComponent editor, JSplitPane split, JTabbedPane pestanas) {
        this.editor = editor;
        this.split = split;
        this.pestanas = pestanas;

        setLayout(new BorderLayout());
        setBackground(Colores.FONDO_PRINCIPAL);

        riel.setLayout(new BoxLayout(riel, BoxLayout.Y_AXIS));
        riel.setBackground(Colores.FONDO_TOOLBAR);
        riel.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 0, Colores.BORDE));
        riel.setPreferredSize(new Dimension(ANCHO_RIEL, 10));

        add(split, BorderLayout.CENTER);
        add(riel, BorderLayout.EAST);
        reconstruirRiel();

        if (PreferenciasApp.analisisColapsado()) {
            colapsar(true);
        }
    }

    public boolean estaColapsado() {
        return colapsado;
    }

    /** Alterna entre contraído y expandido y recuerda la elección. */
    public void alternar() {
        colapsar(!colapsado);
        PreferenciasApp.setAnalisisColapsado(colapsado);
    }

    /** Expande el panel (si estaba contraído) y muestra la pestaña indicada. */
    public void mostrarPestana(int indice) {
        if (colapsado) {
            colapsar(false);
            PreferenciasApp.setAnalisisColapsado(false);
        }
        if (indice >= 0 && indice < pestanas.getTabCount()) {
            pestanas.setSelectedIndex(indice);
        }
    }

    /** Número de errores que se muestra junto a la pestaña "Errores" mientras el panel está contraído. */
    public void setConteoErrores(int cantidad) {
        if (cantidad == errores) return;
        errores = cantidad;
        if (colapsado) reconstruirRiel();
    }

    void colapsar(boolean contraer) {
        if (contraer == colapsado) return;
        if (contraer) {
            ubicacionDivisor = split.getDividerLocation();
            remove(split);
            add(editor, BorderLayout.CENTER);   // reubica el editor fuera del split
        } else {
            remove(editor);
            split.setLeftComponent(editor);
            add(split, BorderLayout.CENTER);
        }
        colapsado = contraer;
        reconstruirRiel();
        revalidate();
        repaint();
        if (!contraer) {
            final int ubicacion = ubicacionDivisor;
            SwingUtilities.invokeLater(() -> split.setDividerLocation(ubicacion));
        }
    }

    // ───────────────────────── Riel lateral ─────────────────────────

    private void reconstruirRiel() {
        riel.removeAll();

        JLabel alternador = new JLabel(colapsado ? "\u25C2" : "\u25B8", SwingConstants.CENTER);
        alternador.setFont(Colores.FUENTE_NORMAL);
        alternador.setForeground(Colores.TEXTO_TENUE);
        alternador.setAlignmentX(Component.CENTER_ALIGNMENT);
        alternador.setMaximumSize(new Dimension(ANCHO_RIEL, 30));
        alternador.setPreferredSize(new Dimension(ANCHO_RIEL, 30));
        alternador.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        alternador.setToolTipText(colapsado
                ? "Mostrar el panel de análisis (Ctrl+B)"
                : "Ocultar el panel de análisis (Ctrl+B)");
        alternador.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                alternar();
            }
        });
        riel.add(alternador);

        if (colapsado) {
            for (int i = 0; i < pestanas.getTabCount(); i++) {
                final int indice = i;
                String texto = pestanas.getTitleAt(i);
                Color color = Colores.TEXTO_NORMAL;
                if (i == 0 && errores > 0) {
                    texto += "  (" + errores + ")";
                    color = Colores.ERROR;
                }
                BotonVertical b = new BotonVertical(texto, color);
                b.setToolTipText("Abrir " + pestanas.getTitleAt(i));
                b.addMouseListener(new MouseAdapter() {
                    @Override
                    public void mouseClicked(MouseEvent e) {
                        mostrarPestana(indice);
                    }
                });
                riel.add(b);
            }
        } else {
            BotonVertical etiqueta = new BotonVertical("ANÁLISIS", Colores.TEXTO_LINEAS);
            etiqueta.setToolTipText("Ocultar el panel de análisis (Ctrl+B)");
            etiqueta.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    alternar();
                }
            });
            riel.add(etiqueta);
        }
        riel.add(Box.createVerticalGlue());
        riel.revalidate();
        riel.repaint();
    }

    /** Texto girado 90° (se lee de arriba hacia abajo) que se resalta al pasar el cursor. */
    static final class BotonVertical extends JComponent {
        private final String texto;
        private final Color color;
        private boolean encima = false;

        BotonVertical(String texto, Color color) {
            this.texto = texto;
            this.color = color;
            setFont(Colores.FUENTE_PEQUENA.deriveFont(Font.BOLD, 12f));
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            setAlignmentX(Component.CENTER_ALIGNMENT);
            FontMetrics fm = getFontMetrics(getFont());
            Dimension d = new Dimension(ANCHO_RIEL, fm.stringWidth(texto) + 26);
            setPreferredSize(d);
            setMaximumSize(d);
            setMinimumSize(d);
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    encima = true;
                    repaint();
                }

                @Override
                public void mouseExited(MouseEvent e) {
                    encima = false;
                    repaint();
                }
            });
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            if (encima) {
                g2.setColor(Colores.FONDO_SELECCION);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
            g2.setFont(getFont());
            g2.setColor(encima ? Colores.ACENTO_PRINCIPAL : color);
            FontMetrics fm = g2.getFontMetrics();
            // Girar 90° en sentido horario y centrar el texto
            g2.translate(getWidth() / 2 + fm.getAscent() / 2 - 2, 13);
            g2.rotate(Math.PI / 2);
            g2.drawString(texto, 0, 0);
            g2.dispose();
        }
    }
}
