package com.compilador.gui;

import java.awt.Color;
import java.awt.Font;

/**
 * Paleta de colores centralizada para toda la interfaz.
 * Tema elegante: Azul marino con colores pastel.
 *
 * Uso: Colores.FONDO_PRINCIPAL, Colores.TEXTO_NORMAL, etc.
 */
public final class Colores {

    private Colores() {} // No instanciable

    // ── Fondos (Azul marino + Pastel) ──
    public static final Color FONDO_PRINCIPAL    = new Color(15, 38, 70);     // Azul marino oscuro
    public static final Color FONDO_PANEL        = new Color(25, 55, 95);     // Azul marino medio
    public static final Color FONDO_EDITOR       = new Color(20, 45, 80);     // Azul marino claro
    public static final Color FONDO_LINEAS       = new Color(15, 38, 70);     // Azul marino oscuro
    public static final Color FONDO_TOOLBAR      = new Color(20, 50, 90);     // Azul marino
    public static final Color FONDO_TABLA_HEADER = new Color(30, 65, 115);    // Azul marino más claro
    public static final Color FONDO_TABLA_ROW1   = new Color(25, 55, 95);     // Azul marino medio
    public static final Color FONDO_TABLA_ROW2   = new Color(32, 62, 105);    // Azul marino alternado
    public static final Color FONDO_SELECCION    = new Color(50, 90, 150);    // Azul marino selección
    public static final Color FONDO_CONTENEDOR   = new Color(35, 70, 120);    // Azul marino contenedor

    // ── Texto (Pastel y claros) ──
    public static final Color TEXTO_NORMAL       = new Color(220, 237, 255); // Blanco pastel azulado
    public static final Color TEXTO_TENUE        = new Color(170, 195, 230); // Azul pastel suave
    public static final Color TEXTO_LINEAS       = new Color(110, 145, 190); // Azul pastel medio

    // ── Acentos por fase (Colores Pastel) ──
    public static final Color ACENTO_LEXICO      = new Color(150, 200, 255); // Azul pastel claro
    public static final Color ACENTO_SINTACTICO  = new Color(180, 240, 200); // Verde pastel
    public static final Color ACENTO_SEMANTICO   = new Color(230, 190, 255); // Morado pastel
    public static final Color ACENTO_INTERMEDIO  = new Color(255, 215, 140); // Ámbar pastel intermedio
    public static final Color ACENTO_OPTIMIZACION = new Color(130, 220, 180); // Verde menta pastel optimización
    public static final Color ACENTO_PRINCIPAL   = new Color(150, 200, 255); // Azul pastel principal

    // ── Estados (Pastel elegante) ──
    public static final Color EXITO              = new Color(180, 240, 200); // Verde pastel
    public static final Color ERROR              = new Color(255, 180, 200); // Rosa pastel error
    public static final Color ADVERTENCIA        = new Color(255, 220, 150); // Naranja pastel
    public static final Color INFO               = new Color(150, 200, 255); // Azul pastel info

    // ── Sintaxis (resaltado de código) ──
    public static final Color SINTAXIS_KEYWORD   = new Color(220, 190, 255); // Morado pastel
    public static final Color SINTAXIS_STRING    = new Color(180, 240, 200); // Verde pastel
    public static final Color SINTAXIS_NUMERO    = new Color(255, 200, 150); // Naranja pastel
    public static final Color SINTAXIS_COMENTARIO = new Color(130, 170, 210); // Azul gris pastel
    public static final Color SINTAXIS_OPERADOR  = new Color(150, 220, 240); // Cyan pastel

    // ── Bordes ──
    public static final Color BORDE              = new Color(50, 100, 160);    // Azul marino borde
    public static final Color BORDE_ENFOCADO     = new Color(150, 200, 255);  // Azul pastel enfocado

    // ── Fuentes ──
    public static final Font FUENTE_CODIGO       = new Font("Consolas", Font.PLAIN, 14);
    public static final Font FUENTE_NORMAL       = new Font("Segoe UI Emoji", Font.PLAIN, 13);
    public static final Font FUENTE_TITULO       = new Font("Segoe UI Emoji", Font.BOLD, 14);
    public static final Font FUENTE_PEQUENA      = new Font("Segoe UI Emoji", Font.PLAIN, 11);
    public static final Font FUENTE_TABLA        = new Font("Consolas", Font.PLAIN, 12);

    // ── Selección de alto contraste (fondo claro + texto oscuro) ──
    public static final Color SEL_FONDO          = new Color(150, 200, 255);
    public static final Color SEL_TEXTO          = new Color(10, 25, 50);

    /** Estiliza un JTabbedPane: pestaña activa clara con texto oscuro, inactivas oscuras con texto claro. */
    public static void estilizarPestanas(javax.swing.JTabbedPane tabs) {
        tabs.setUI(new javax.swing.plaf.basic.BasicTabbedPaneUI() {
            @Override
            protected void installDefaults() {
                super.installDefaults();
                tabInsets = new java.awt.Insets(6, 14, 6, 14);
                selectedTabPadInsets = new java.awt.Insets(0, 0, 0, 0);
                contentBorderInsets = new java.awt.Insets(0, 0, 0, 0);
                tabAreaInsets = new java.awt.Insets(0, 0, 0, 0);
            }
            @Override
            protected void paintTabBackground(java.awt.Graphics g, int tp, int idx,
                    int x, int y, int w, int h, boolean sel) {
                g.setColor(sel ? SEL_FONDO : FONDO_PANEL);
                g.fillRect(x + 2, y + 2, w - 4, h - 3);
            }
            @Override
            protected void paintTabBorder(java.awt.Graphics g, int tp, int idx,
                    int x, int y, int w, int h, boolean sel) {
                g.setColor(sel ? BORDE_ENFOCADO : BORDE);
                g.drawRect(x + 2, y + 2, w - 5, h - 4);
            }
            @Override
            protected void paintText(java.awt.Graphics g, int tp, Font font,
                    java.awt.FontMetrics m, int idx, String title, java.awt.Rectangle r, boolean sel) {
                ((java.awt.Graphics2D) g).setRenderingHint(java.awt.RenderingHints.KEY_TEXT_ANTIALIASING,
                        java.awt.RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                g.setFont(FUENTE_NORMAL);
                g.setColor(sel ? SEL_TEXTO : TEXTO_NORMAL);
                g.drawString(title, r.x, r.y + m.getAscent());
            }
            @Override
            protected void paintFocusIndicator(java.awt.Graphics g, int tp, java.awt.Rectangle[] rects,
                    int idx, java.awt.Rectangle iconRect, java.awt.Rectangle textRect, boolean sel) { }
            @Override
            protected void paintContentBorder(java.awt.Graphics g, int tp, int sel) {
                g.setColor(BORDE);
                g.drawLine(0, calculateTabAreaHeight(tp, runCount, maxTabHeight) , tabPane.getWidth(),
                        calculateTabAreaHeight(tp, runCount, maxTabHeight));
            }
        });
        tabs.setBackground(FONDO_PANEL);
        tabs.setForeground(TEXTO_NORMAL);
        tabs.setFont(FUENTE_NORMAL);
        tabs.setOpaque(true);
    }

    /** Estiliza un JComboBox con el mismo tema oscuro (valor actual, flecha y lista desplegable). */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public static void estilizarCombo(javax.swing.JComboBox combo) {
        combo.setUI(new javax.swing.plaf.basic.BasicComboBoxUI() {
            @Override
            protected javax.swing.JButton createArrowButton() {
                javax.swing.JButton b = new javax.swing.JButton("\u25be");
                b.setFont(FUENTE_NORMAL);
                b.setForeground(TEXTO_TENUE);
                b.setBackground(FONDO_EDITOR);
                b.setBorder(javax.swing.BorderFactory.createEmptyBorder(0, 6, 0, 6));
                b.setFocusPainted(false);
                b.setContentAreaFilled(false);
                b.setOpaque(true);
                return b;
            }
            @Override
            public void paintCurrentValueBackground(java.awt.Graphics g, java.awt.Rectangle r, boolean hasFocus) {
                g.setColor(FONDO_EDITOR);
                g.fillRect(r.x, r.y, r.width, r.height);
            }
            @Override
            public void paintCurrentValue(java.awt.Graphics g, java.awt.Rectangle r, boolean hasFocus) {
                javax.swing.ListCellRenderer rend = comboBox.getRenderer();
                java.awt.Component c = rend.getListCellRendererComponent(listBox, comboBox.getSelectedItem(), -1, false, false);
                c.setFont(FUENTE_NORMAL);
                c.setBackground(FONDO_EDITOR);
                c.setForeground(TEXTO_NORMAL);
                currentValuePane.paintComponent(g, c, comboBox, r.x, r.y, r.width, r.height, false);
            }
        });
        combo.setFont(FUENTE_NORMAL);
        combo.setBackground(FONDO_EDITOR);
        combo.setForeground(TEXTO_NORMAL);
        combo.setBorder(javax.swing.BorderFactory.createLineBorder(BORDE, 1));
        combo.setRenderer(new javax.swing.DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(javax.swing.JList<?> list, Object value,
                    int index, boolean isSelected, boolean cellHasFocus) {
                javax.swing.JLabel l = (javax.swing.JLabel) super.getListCellRendererComponent(
                        list, value, index, isSelected, cellHasFocus);
                l.setOpaque(true);
                l.setFont(FUENTE_NORMAL);
                l.setBorder(javax.swing.BorderFactory.createEmptyBorder(3, 8, 3, 8));
                boolean activo = isSelected && index >= 0;
                l.setBackground(activo ? SEL_FONDO : FONDO_EDITOR);
                l.setForeground(activo ? SEL_TEXTO : TEXTO_NORMAL);
                return l;
            }
        });
    }

    /** Envuelve un componente (tabla/scroll) con margen lateral, igual que el panel de errores. */
    public static javax.swing.JPanel conMargen(javax.swing.JComponent comp) {
        if (comp instanceof javax.swing.JScrollPane) {
            comp.setBorder(javax.swing.BorderFactory.createLineBorder(BORDE));
        }
        javax.swing.JPanel p = new javax.swing.JPanel(new java.awt.BorderLayout());
        p.setBackground(FONDO_PANEL);
        p.setBorder(javax.swing.BorderFactory.createEmptyBorder(6, 10, 6, 10));
        p.add(comp, java.awt.BorderLayout.CENTER);
        return p;
    }

    /** Aplica un divisor oscuro (sin franja blanca) a un JSplitPane. */
    public static void estilizarSplit(javax.swing.JSplitPane split) {
        split.setUI(new javax.swing.plaf.basic.BasicSplitPaneUI() {
            @Override
            public javax.swing.plaf.basic.BasicSplitPaneDivider createDefaultDivider() {
                return new javax.swing.plaf.basic.BasicSplitPaneDivider(this) {
                    @Override
                    public void paint(java.awt.Graphics g) {
                        g.setColor(BORDE);
                        g.fillRect(0, 0, getWidth(), getHeight());
                    }
                };
            }
        });
        split.setBorder(javax.swing.BorderFactory.createEmptyBorder());
        split.setBackground(FONDO_PANEL);
    }
}
