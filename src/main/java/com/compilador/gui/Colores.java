package com.compilador.gui;

import java.awt.Color;
import java.awt.Font;

/**
 * Paleta de colores centralizada para toda la interfaz.
 * Tema morado basado en los tonos del icono de la aplicación.
 *
 * Uso: Colores.FONDO_PRINCIPAL, Colores.TEXTO_NORMAL, etc.
 */
public final class Colores {

    private Colores() {} // No instanciable

    // ── Fondos morados claros basados en el icono ──
    public static final Color FONDO_PRINCIPAL    = new Color(231, 213, 242); // #E7D5F2
    public static final Color FONDO_PANEL        = new Color(238, 228, 246); // #EEE4F6
    public static final Color FONDO_EDITOR       = new Color(250, 247, 252); // #FAF7FC
    public static final Color FONDO_LINEAS       = new Color(238, 228, 246); // #EEE4F6
    public static final Color FONDO_TOOLBAR      = new Color(233, 220, 243); // #E9DCF3
    public static final Color FONDO_TABLA_HEADER = new Color(217, 194, 234); // #D9C2EA
    public static final Color FONDO_TABLA_ROW1   = new Color(248, 243, 251); // #F8F3FB
    public static final Color FONDO_TABLA_ROW2   = new Color(238, 228, 246); // #EEE4F6
    public static final Color FONDO_SELECCION    = new Color(216, 188, 235); // #D8BCEB
    public static final Color FONDO_CONTENEDOR   = new Color(238, 228, 246); // #EEE4F6
    public static final Color FONDO_BIENVENIDA_LATERAL = new Color(233, 220, 243); // #E9DCF3
    public static final Color FONDO_TARJETA      = new Color(248, 243, 251); // #F8F3FB
    public static final Color FONDO_TARJETA_HOVER = new Color(230, 213, 240); // #E6D5F0
    public static final Color FONDO_INSIGNIA     = new Color(231, 213, 242); // #E7D5F2
    public static final Color FONDO_BARRA_TITULO = new Color(87, 53, 120);   // #573578
    public static final Color FONDO_BOTON        = new Color(112, 70, 157);  // #70469D
    public static final Color FONDO_BOTON_HOVER  = new Color(87, 53, 120);   // #573578

    // ── Texto morado oscuro ──
    public static final Color TEXTO_NORMAL       = new Color(45, 23, 64);    // #2D1740
    public static final Color TEXTO_TENUE        = new Color(91, 67, 112);   // #5B4370
    public static final Color TEXTO_LINEAS       = new Color(114, 83, 138);  // #72538A
    public static final Color TEXTO_BARRA_TITULO = new Color(250, 247, 252); // #FAF7FC
    public static final Color TEXTO_BOTON        = new Color(250, 247, 252); // #FAF7FC

    // ── Acentos diferenciados por etapa del compilador ──
    public static final Color ACENTO_LEXICO            = new Color(122, 53, 199);  // #7A35C7 Violeta
    public static final Color ACENTO_SINTACTICO        = new Color(14, 116, 144);  // #0E7490 Cian petróleo
    public static final Color ACENTO_SEMANTICO         = new Color(194, 65, 92);   // #C2415C Coral frambuesa
    public static final Color ACENTO_INTERMEDIO        = new Color(43, 89, 195);   // #2B59C3 Azul cobalto
    public static final Color ACENTO_OPTIMIZACION      = new Color(180, 83, 9);    // #B45309 Ámbar tostado
    public static final Color ACENTO_PRINCIPAL         = new Color(122, 53, 199);  // #7A35C7
    public static final Color ACENTO_CYAN              = new Color(14, 116, 144);  // #0E7490
    public static final Color ACENTO_CORAL             = new Color(194, 65, 92);   // #C2415C

    // ── Estados: conserva el significado y mejora el contraste en fondos claros ──
    public static final Color EXITO              = new Color(43, 119, 77);   // Verde
    public static final Color ERROR              = new Color(247, 81, 111);   // #F7516F Rojo
    public static final Color ADVERTENCIA        = new Color(145, 91, 20);   // Ámbar
    public static final Color INFO               = new Color(49, 91, 145);   // Azul

    // ── Sintaxis: conserva los tonos por categoría con contraste para editor claro ──
    public static final Color SINTAXIS_KEYWORD   = new Color(103, 57, 140);  // Morado
    public static final Color SINTAXIS_STRING    = new Color(43, 119, 77);   // Verde
    public static final Color SINTAXIS_NUMERO    = new Color(145, 78, 28);   // Naranja
    public static final Color SINTAXIS_COMENTARIO = new Color(82, 102, 133); // Azul gris
    public static final Color SINTAXIS_OPERADOR  = new Color(38, 105, 121);  // Cyan

    // ── Bordes ──
    public static final Color BORDE              = new Color(196, 173, 214); // #C4ADD6
    public static final Color BORDE_ENFOCADO     = new Color(150, 89, 206);  // #9659CE

    // ── Fuentes ──
    public static final Font FUENTE_CODIGO       = new Font("Consolas", Font.PLAIN, 14);
    public static final Font FUENTE_NORMAL       = new Font("Segoe UI Emoji", Font.PLAIN, 13);
    public static final Font FUENTE_TITULO       = new Font("Segoe UI Emoji", Font.BOLD, 14);
    public static final Font FUENTE_PEQUENA      = new Font("Segoe UI Emoji", Font.PLAIN, 11);
    public static final Font FUENTE_TABLA        = new Font("Consolas", Font.PLAIN, 12);

    // ── Selección de alto contraste ──
    public static final Color SEL_FONDO          = new Color(216, 188, 235); // #D8BCEB
    public static final Color SEL_TEXTO          = new Color(45, 23, 64);    // #2D1740

    public static String toHex(Color color) {
        return String.format("%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
    }

    /** Estiliza un JTabbedPane con tonos lavanda y texto oscuro. */
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
