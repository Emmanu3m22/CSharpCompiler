package com.compilador.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowEvent;

/**
 * Barra de título personalizada y minimalista (reemplaza la del sistema).
 * Permite arrastrar la ventana, maximizar con doble clic y trae los botones
 * minimizar / maximizar / cerrar con los colores del tema.
 *
 * La ventana debe ser {@code undecorated}. {@link #instalarRedimension(JFrame)} agrega
 * el redimensionado por los bordes.
 */
public class BarraTitulo extends JPanel {

    private static final int ALTO = 34;

    private final JFrame ventana;
    private final JLabel lblTitulo;
    private final BotonVentana btnMaximizar;

    private boolean maximizada = false;
    private Rectangle limitesNormales;
    private Point offsetArrastre;

    public BarraTitulo(JFrame ventana, String titulo) {
        this.ventana = ventana;
        setLayout(new BorderLayout());
        setBackground(Colores.FONDO_BARRA_TITULO);
        setPreferredSize(new Dimension(100, ALTO));
        setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Colores.BORDE));

        // ── Marca + título ──
        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        izquierda.setOpaque(false);

        JLabel marca = new JLabel(IconoAplicacion.cargar(24));
        marca.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 8));
        izquierda.add(marca);

        lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(Colores.FUENTE_PEQUENA.deriveFont(12f));
        lblTitulo.setForeground(Colores.TEXTO_BARRA_TITULO);
        izquierda.add(lblTitulo);

        // el contenedor izquierdo ocupa toda la altura para centrar verticalmente
        JPanel envoltorio = new JPanel(new GridBagLayout());
        envoltorio.setOpaque(false);
        envoltorio.add(izquierda);
        add(envoltorio, BorderLayout.WEST);

        // ── Botones de ventana ──
        JPanel botones = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
        botones.setOpaque(false);
        BotonVentana btnMin = new BotonVentana(BotonVentana.Tipo.MINIMIZAR);
        btnMaximizar = new BotonVentana(BotonVentana.Tipo.MAXIMIZAR);
        BotonVentana btnCerrar = new BotonVentana(BotonVentana.Tipo.CERRAR);
        btnMin.addActionListener(e -> ventana.setState(Frame.ICONIFIED));
        btnMaximizar.addActionListener(e -> alternarMaximizada());
        btnCerrar.addActionListener(e ->
                ventana.dispatchEvent(new WindowEvent(ventana, WindowEvent.WINDOW_CLOSING)));
        botones.add(btnMin);
        botones.add(btnMaximizar);
        botones.add(btnCerrar);
        add(botones, BorderLayout.EAST);

        // ── Arrastrar y doble clic ──
        MouseAdapter arrastre = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    offsetArrastre = e.getPoint();
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (offsetArrastre == null || maximizada) return;
                Point pantalla = e.getLocationOnScreen();
                ventana.setLocation(pantalla.x - offsetArrastre.x, pantalla.y - offsetArrastre.y);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() == 2) {
                    alternarMaximizada();
                }
            }
        };
        for (Component c : new Component[]{this, envoltorio, izquierda, marca, lblTitulo}) {
            c.addMouseListener(arrastre);
            c.addMouseMotionListener(arrastre);
        }
    }

    public void setTitulo(String titulo) {
        lblTitulo.setText(titulo);
    }

    public boolean estaMaximizada() {
        return maximizada;
    }

    /** Maximiza respetando la barra de tareas (una ventana sin decoración la taparía). */
    public void alternarMaximizada() {
        if (maximizada) {
            if (limitesNormales != null) ventana.setBounds(limitesNormales);
            maximizada = false;
        } else {
            limitesNormales = ventana.getBounds();
            GraphicsConfiguration gc = ventana.getGraphicsConfiguration();
            Rectangle pantalla = gc.getBounds();
            Insets bordes = Toolkit.getDefaultToolkit().getScreenInsets(gc);
            ventana.setBounds(pantalla.x + bordes.left, pantalla.y + bordes.top,
                    pantalla.width - bordes.left - bordes.right,
                    pantalla.height - bordes.top - bordes.bottom);
            maximizada = true;
        }
        btnMaximizar.setRestaurar(maximizada);
    }

    /**
     * Habilita redimensionar la ventana arrastrando sus bordes (la zona de 5 px
     * alrededor del contenido). Debe llamarse una vez que la ventana tiene su root pane.
     */
    public static void instalarRedimension(JFrame ventana, BarraTitulo barra) {
        final int grosor = 5;
        JRootPane raiz = ventana.getRootPane();
        raiz.setOpaque(true);
        raiz.setBackground(Colores.BORDE);
        raiz.setBorder(BorderFactory.createEmptyBorder(1, 1, 1, 1));

        MouseAdapter redimensionar = new MouseAdapter() {
            private int borde = 0; // bits: 1=izq 2=der 4=arriba 8=abajo
            private Point inicio;
            private Rectangle limitesInicio;

            private int bordeEn(Point p) {
                int b = 0;
                if (p.x <= grosor) b |= 1;
                if (p.x >= raiz.getWidth() - grosor) b |= 2;
                if (p.y <= grosor) b |= 4;
                if (p.y >= raiz.getHeight() - grosor) b |= 8;
                return b;
            }

            private int cursorPara(int b) {
                return switch (b) {
                    case 1 -> Cursor.W_RESIZE_CURSOR;
                    case 2 -> Cursor.E_RESIZE_CURSOR;
                    case 4 -> Cursor.N_RESIZE_CURSOR;
                    case 8 -> Cursor.S_RESIZE_CURSOR;
                    case 5 -> Cursor.NW_RESIZE_CURSOR;
                    case 6 -> Cursor.NE_RESIZE_CURSOR;
                    case 9 -> Cursor.SW_RESIZE_CURSOR;
                    case 10 -> Cursor.SE_RESIZE_CURSOR;
                    default -> Cursor.DEFAULT_CURSOR;
                };
            }

            @Override
            public void mouseMoved(MouseEvent e) {
                if (barra.estaMaximizada()) {
                    raiz.setCursor(Cursor.getDefaultCursor());
                    return;
                }
                raiz.setCursor(Cursor.getPredefinedCursor(cursorPara(bordeEn(e.getPoint()))));
            }

            @Override
            public void mousePressed(MouseEvent e) {
                borde = barra.estaMaximizada() ? 0 : bordeEn(e.getPoint());
                inicio = e.getLocationOnScreen();
                limitesInicio = ventana.getBounds();
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (borde == 0 || inicio == null) return;
                Point actual = e.getLocationOnScreen();
                int dx = actual.x - inicio.x;
                int dy = actual.y - inicio.y;
                Rectangle r = new Rectangle(limitesInicio);
                Dimension min = ventana.getMinimumSize();
                if ((borde & 1) != 0) {
                    int nuevoAncho = Math.max(min.width, limitesInicio.width - dx);
                    r.x = limitesInicio.x + (limitesInicio.width - nuevoAncho);
                    r.width = nuevoAncho;
                }
                if ((borde & 2) != 0) r.width = Math.max(min.width, limitesInicio.width + dx);
                if ((borde & 4) != 0) {
                    int nuevoAlto = Math.max(min.height, limitesInicio.height - dy);
                    r.y = limitesInicio.y + (limitesInicio.height - nuevoAlto);
                    r.height = nuevoAlto;
                }
                if ((borde & 8) != 0) r.height = Math.max(min.height, limitesInicio.height + dy);
                ventana.setBounds(r);
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                borde = 0;
            }
        };
        raiz.addMouseListener(redimensionar);
        raiz.addMouseMotionListener(redimensionar);
    }

    // ───────────────────────── Botón de ventana ─────────────────────────

    /** Botón plano que dibuja su propio glifo (sin depender de fuentes ni emojis). */
    static final class BotonVentana extends JButton {
        enum Tipo { MINIMIZAR, MAXIMIZAR, CERRAR }

        private final Tipo tipo;
        private boolean restaurar = false;
        private boolean encima = false;

        BotonVentana(Tipo tipo) {
            this.tipo = tipo;
            setPreferredSize(new Dimension(46, ALTO));
            setFocusPainted(false);
            setBorderPainted(false);
            setContentAreaFilled(false);
            setOpaque(false);
            setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
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

        void setRestaurar(boolean restaurar) {
            this.restaurar = restaurar;
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            int w = getWidth();
            int h = getHeight();

            boolean cerrar = tipo == Tipo.CERRAR;
            if (encima) {
                g2.setColor(cerrar ? Colores.ERROR : Colores.FONDO_BOTON_HOVER);
                g2.fillRect(0, 0, w, h);
            }
            g2.setColor(Colores.TEXTO_BARRA_TITULO);
            g2.setStroke(new BasicStroke(1.2f));

            int cx = w / 2;
            int cy = h / 2;
            switch (tipo) {
                case MINIMIZAR -> g2.drawLine(cx - 5, cy, cx + 5, cy);
                case MAXIMIZAR -> {
                    if (restaurar) {
                        g2.drawRect(cx - 5, cy - 2, 7, 7);
                        g2.drawLine(cx - 2, cy - 5, cx + 5, cy - 5);
                        g2.drawLine(cx + 5, cy - 5, cx + 5, cy + 2);
                    } else {
                        g2.drawRect(cx - 5, cy - 5, 10, 10);
                    }
                }
                case CERRAR -> {
                    g2.drawLine(cx - 5, cy - 5, cx + 5, cy + 5);
                    g2.drawLine(cx - 5, cy + 5, cx + 5, cy - 5);
                }
            }
            g2.dispose();
        }
    }
}
