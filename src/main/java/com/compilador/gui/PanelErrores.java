package com.compilador.gui;

import com.compilador.errores.ErrorLexico;
import com.compilador.errores.ErrorSintactico;
import com.compilador.semantic.ErrorSemantico;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Panel elegante para mostrar errores léxicos y sintácticos.
 * Utiliza una paleta de azul marino con colores pastel.
 */
public class PanelErrores extends JPanel {

    private final JPanel panelErroresLexicos;
    private final JPanel panelErroresSintacticos;
    private final JPanel panelErroresSemanticos;
    private final BiConsumer<Integer, Integer> alSeleccionarError;
    private JLabel labelCountLex;
    private JLabel labelCountSint;
    private JLabel labelCountSem;

    public PanelErrores() {
        this((linea, columna) -> { });
    }

    public PanelErrores(BiConsumer<Integer, Integer> alSeleccionarError) {
        this.alSeleccionarError = alSeleccionarError;
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        setBackground(Colores.FONDO_PRINCIPAL);
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        // ── Sección de Errores Léxicos ──
        JPanel encabezadoLexico = crearSeccionErrores(true);
        panelErroresLexicos = new JPanel();
        panelErroresLexicos.setLayout(new BoxLayout(panelErroresLexicos, BoxLayout.Y_AXIS));
        panelErroresLexicos.setBackground(Colores.FONDO_PANEL);
        panelErroresLexicos.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scrollLex = new JScrollPane(panelErroresLexicos);
        scrollLex.setBackground(Colores.FONDO_PANEL);
        scrollLex.setBorder(BorderFactory.createLineBorder(Colores.BORDE, 1));
        scrollLex.getViewport().setBackground(Colores.FONDO_PANEL);
        JPanel seccionLexica = new JPanel(new BorderLayout(0, 4));
        seccionLexica.setBackground(Colores.FONDO_PRINCIPAL);
        seccionLexica.add(encabezadoLexico, BorderLayout.NORTH);
        seccionLexica.add(scrollLex, BorderLayout.CENTER);

        // ── Sección de Errores Sintácticos ──
        JPanel encabezadoSintactico = crearSeccionErrores(false);
        panelErroresSintacticos = new JPanel();
        panelErroresSintacticos.setLayout(new BoxLayout(panelErroresSintacticos, BoxLayout.Y_AXIS));
        panelErroresSintacticos.setBackground(Colores.FONDO_PANEL);
        panelErroresSintacticos.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scrollSint = new JScrollPane(panelErroresSintacticos);
        scrollSint.setBackground(Colores.FONDO_PANEL);
        scrollSint.setBorder(BorderFactory.createLineBorder(Colores.BORDE, 1));
        scrollSint.getViewport().setBackground(Colores.FONDO_PANEL);

        JPanel seccionSintactica = new JPanel(new BorderLayout(0, 4));
        seccionSintactica.setBackground(Colores.FONDO_PRINCIPAL);
        seccionSintactica.add(encabezadoSintactico, BorderLayout.NORTH);
        seccionSintactica.add(scrollSint, BorderLayout.CENTER);

        // ── Sección de Errores Semánticos ──
        JPanel encabezadoSemantico = crearSeccionErroresSem();
        panelErroresSemanticos = new JPanel();
        panelErroresSemanticos.setLayout(new BoxLayout(panelErroresSemanticos, BoxLayout.Y_AXIS));
        panelErroresSemanticos.setBackground(Colores.FONDO_PANEL);
        panelErroresSemanticos.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scrollSem = new JScrollPane(panelErroresSemanticos);
        scrollSem.setBackground(Colores.FONDO_PANEL);
        scrollSem.setBorder(BorderFactory.createLineBorder(Colores.BORDE, 1));
        scrollSem.getViewport().setBackground(Colores.FONDO_PANEL);

        JPanel seccionSemantica = new JPanel(new BorderLayout(0, 4));
        seccionSemantica.setBackground(Colores.FONDO_PRINCIPAL);
        seccionSemantica.add(encabezadoSemantico, BorderLayout.NORTH);
        seccionSemantica.add(scrollSem, BorderLayout.CENTER);

        // ── Layout: split triple (léxico | sintáctico | semántico) ──
        JSplitPane splitLexSint = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT, seccionLexica, seccionSintactica);
        splitLexSint.setResizeWeight(0.5);
        splitLexSint.setDividerLocation(0.5);
        splitLexSint.setDividerSize(5);
        splitLexSint.setBorder(BorderFactory.createEmptyBorder());
        splitLexSint.setBackground(Colores.FONDO_PRINCIPAL);

        JSplitPane splitErrores = new JSplitPane(
            JSplitPane.VERTICAL_SPLIT, splitLexSint, seccionSemantica);
        splitErrores.setResizeWeight(0.67);
        splitErrores.setDividerLocation(0.67);
        splitErrores.setDividerSize(5);
        splitErrores.setBorder(BorderFactory.createEmptyBorder());
        splitErrores.setBackground(Colores.FONDO_PRINCIPAL);
        add(splitErrores);
    }

    /**
     * Crea el encabezado de una sección de errores léxicos o sintácticos.
     */
    private JPanel crearSeccionErrores(boolean esLexico) {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(Colores.FONDO_CONTENEDOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0,
                esLexico ? Colores.ERROR : Colores.ADVERTENCIA),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel titulo = new JLabel(esLexico ? "🔴 Errores Léxicos" : "⚠️ Errores Sintácticos");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titulo.setForeground(esLexico ? Colores.ERROR : Colores.ADVERTENCIA);
        panel.add(titulo, BorderLayout.WEST);

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
     * Crea el encabezado de la sección de errores semánticos.
     */
    private JPanel crearSeccionErroresSem() {
        JPanel panel = new JPanel(new BorderLayout(12, 0));
        panel.setBackground(Colores.FONDO_CONTENEDOR);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 4, 0, 0, Colores.ACENTO_SEMANTICO),
            BorderFactory.createEmptyBorder(8, 8, 8, 8)
        ));

        JLabel titulo = new JLabel("🟣 Errores Semánticos");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        titulo.setForeground(Colores.ACENTO_SEMANTICO);
        panel.add(titulo, BorderLayout.WEST);

        labelCountSem = new JLabel("0");
        labelCountSem.setFont(new Font("Segoe UI", Font.BOLD, 13));
        labelCountSem.setForeground(Colores.TEXTO_NORMAL);
        panel.add(labelCountSem, BorderLayout.EAST);

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
                com.compilador.errores.FormateadorErrores.ErrorFormateado info = 
                    com.compilador.errores.FormateadorErrores.formatearLexico(error);
                panelErroresLexicos.add(crearItemError(info, error.getLexema(),
                    error.getLinea(), error.getColumna(), true));
                panelErroresLexicos.add(Box.createVerticalStrut(8));
            }
        }

        panelErroresLexicos.revalidate();
        panelErroresLexicos.repaint();
    }

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
                com.compilador.errores.FormateadorErrores.ErrorFormateado info =
                    com.compilador.errores.FormateadorErrores.formatearSintactico(error);
                panelErroresSintacticos.add(crearItemError(info, error.getTokenEncontrado(),
                    error.getLinea(), error.getColumna(), false));
                panelErroresSintacticos.add(Box.createVerticalStrut(8));
            }
        }

        panelErroresSintacticos.revalidate();
        panelErroresSintacticos.repaint();
    }

    /**
     * Carga y muestra los errores semánticos con el mismo formato visual.
     */
    public void cargarErroresSemanticos(List<ErrorSemantico> errores) {
        panelErroresSemanticos.removeAll();
        labelCountSem.setText(String.valueOf(errores.size()));

        if (errores.isEmpty()) {
            JLabel labelVacio = new JLabel("✓ Sin errores semánticos");
            labelVacio.setFont(Colores.FUENTE_NORMAL);
            labelVacio.setForeground(Colores.EXITO);
            panelErroresSemanticos.add(labelVacio);
        } else {
            for (ErrorSemantico error : errores) {
                panelErroresSemanticos.add(crearItemErrorSemantico(error));
                panelErroresSemanticos.add(Box.createVerticalStrut(8));
            }
        }

        panelErroresSemanticos.revalidate();
        panelErroresSemanticos.repaint();
    }

    /**
     * Crea un item visual para errores semánticos (mismo estilo, color morado).
     */
    private JPanel crearItemErrorSemantico(ErrorSemantico error) {
        JPanel itemPanel = new JPanel(new BorderLayout(8, 0));
        itemPanel.setBackground(Colores.FONDO_TABLA_ROW1);
        Color colorEstado = Colores.ACENTO_SEMANTICO;

        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, colorEstado),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(Colores.FONDO_TABLA_ROW1);

        // Fila 1: [Línea X, Col Y] • Tipo de error
        JPanel filaHeader = new JPanel(new BorderLayout(6, 0));
        filaHeader.setBackground(Colores.FONDO_TABLA_ROW1);

        JLabel lblTitulo = new JLabel(
            "Línea " + error.getLinea() + ":" + error.getColumna() + "  •  " + error.getTipoError());
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(colorEstado);
        filaHeader.add(lblTitulo, BorderLayout.WEST);
        contenido.add(filaHeader);
        contenido.add(Box.createVerticalStrut(3));

        // Fila 2: Mensaje del error
        String msgHtml = "<html><p style='width:340px; color:#DCEBFF; margin:0; padding:0;'>" +
            error.getMensaje().replace("<", "&lt;").replace(">", "&gt;") + "</p></html>";
        JLabel lblMensaje = new JLabel(msgHtml);
        lblMensaje.setFont(Colores.FUENTE_NORMAL);
        contenido.add(lblMensaje);

        itemPanel.add(contenido, BorderLayout.CENTER);

        // Hover
        MouseAdapter clickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    alSeleccionarError.accept(error.getLinea(), error.getColumna());
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                itemPanel.setBackground(Colores.FONDO_SELECCION);
                contenido.setBackground(Colores.FONDO_SELECCION);
                filaHeader.setBackground(Colores.FONDO_SELECCION);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, Colores.BORDE_ENFOCADO),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                itemPanel.setBackground(Colores.FONDO_TABLA_ROW1);
                contenido.setBackground(Colores.FONDO_TABLA_ROW1);
                filaHeader.setBackground(Colores.FONDO_TABLA_ROW1);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, colorEstado),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
            }
        };
        itemPanel.addMouseListener(clickListener);
        contenido.addMouseListener(clickListener);
        lblTitulo.addMouseListener(clickListener);
        lblMensaje.addMouseListener(clickListener);

        return itemPanel;
    }

    /**
     * Crea un item visual para mostrar un error léxico/sintáctico con diseño minimalista.
     */
    private JPanel crearItemError(com.compilador.errores.FormateadorErrores.ErrorFormateado info,
                                  String token, int linea, int columna, boolean esLexico) {
        JPanel itemPanel = new JPanel(new BorderLayout(8, 0));
        itemPanel.setBackground(Colores.FONDO_TABLA_ROW1);
        Color colorEstado = esLexico ? Colores.ERROR : Colores.ADVERTENCIA;

        // Borde minimalista: línea lateral delgada de 3px y padding compacto
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, colorEstado),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        itemPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));

        // Contenedor principal vertical
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(Colores.FONDO_TABLA_ROW1);

        // Fila 1: [Línea X, Col Y] • Título  ---  Chip del token
        JPanel filaHeader = new JPanel(new BorderLayout(6, 0));
        filaHeader.setBackground(Colores.FONDO_TABLA_ROW1);

        JLabel lblTitulo = new JLabel("Línea " + linea + ":" + columna + "  •  " + info.getTitulo());
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(colorEstado);
        filaHeader.add(lblTitulo, BorderLayout.WEST);

        if (token != null && !token.isEmpty()) {
            JLabel lblToken = new JLabel(" " + token + " ");
            lblToken.setFont(new Font("Consolas", Font.BOLD, 12));
            lblToken.setForeground(Colores.ADVERTENCIA); // Dorado claro de alto contraste
            lblToken.setBackground(new Color(12, 28, 52)); // Fondo oscuro nítido
            lblToken.setOpaque(true);
            lblToken.setBorder(BorderFactory.createLineBorder(Colores.BORDE, 1));
            filaHeader.add(lblToken, BorderLayout.EAST);
        }
        contenido.add(filaHeader);
        contenido.add(Box.createVerticalStrut(3));

        // Fila 2: Mensaje explicativo claro en texto legible
        String msgHtml = "<html><p style='width:340px; color:#DCEBFF; margin:0; padding:0;'>" + 
            info.getMensaje().replace("<", "&lt;").replace(">", "&gt;") + "</p></html>";
        JLabel lblMensaje = new JLabel(msgHtml);
        lblMensaje.setFont(Colores.FUENTE_NORMAL);
        contenido.add(lblMensaje);

        // Fila 3: Sugerencia o esperado sutil (solo si aporta)
        String extra = "";
        if (info.getSugerencia() != null && !info.getSugerencia().isEmpty()) {
            extra = "💡 " + info.getSugerencia();
        } else if (info.getTokenEsperado() != null && !info.getTokenEsperado().isEmpty()) {
            extra = "Se esperaba: " + info.getTokenEsperado();
        }

        if (!extra.isEmpty()) {
            contenido.add(Box.createVerticalStrut(2));
            JLabel lblExtra = new JLabel(extra);
            lblExtra.setFont(Colores.FUENTE_PEQUENA);
            lblExtra.setForeground(Colores.TEXTO_TENUE);
            contenido.add(lblExtra);
        }

        itemPanel.add(contenido, BorderLayout.CENTER);

        // Listener de clic directo
        MouseAdapter clickListener = new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    alSeleccionarError.accept(linea, columna);
                }
            }
            @Override
            public void mouseEntered(MouseEvent e) {
                itemPanel.setBackground(Colores.FONDO_SELECCION);
                contenido.setBackground(Colores.FONDO_SELECCION);
                filaHeader.setBackground(Colores.FONDO_SELECCION);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, Colores.BORDE_ENFOCADO),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                itemPanel.setBackground(Colores.FONDO_TABLA_ROW1);
                contenido.setBackground(Colores.FONDO_TABLA_ROW1);
                filaHeader.setBackground(Colores.FONDO_TABLA_ROW1);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, colorEstado),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
            }
        };

        itemPanel.addMouseListener(clickListener);
        contenido.addMouseListener(clickListener);
        lblTitulo.addMouseListener(clickListener);
        lblMensaje.addMouseListener(clickListener);

        return itemPanel;
    }

    /**
     * Limpia todos los errores mostrados.
     */
    public void limpiar() {
        panelErroresLexicos.removeAll();
        panelErroresSintacticos.removeAll();
        panelErroresSemanticos.removeAll();
        labelCountLex.setText("0");
        labelCountSint.setText("0");
        labelCountSem.setText("0");

        JLabel lblEspLex = new JLabel("Esperando análisis...");
        lblEspLex.setFont(Colores.FUENTE_NORMAL);
        lblEspLex.setForeground(Colores.TEXTO_TENUE);
        panelErroresLexicos.add(lblEspLex);

        JLabel lblEspSint = new JLabel("Esperando análisis...");
        lblEspSint.setFont(Colores.FUENTE_NORMAL);
        lblEspSint.setForeground(Colores.TEXTO_TENUE);
        panelErroresSintacticos.add(lblEspSint);

        JLabel lblEspSem = new JLabel("Esperando análisis...");
        lblEspSem.setFont(Colores.FUENTE_NORMAL);
        lblEspSem.setForeground(Colores.TEXTO_TENUE);
        panelErroresSemanticos.add(lblEspSem);

        panelErroresLexicos.revalidate();
        panelErroresSintacticos.revalidate();
        panelErroresSemanticos.revalidate();
        panelErroresLexicos.repaint();
        panelErroresSintacticos.repaint();
        panelErroresSemanticos.repaint();
    }
}
