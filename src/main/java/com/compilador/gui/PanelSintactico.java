package com.compilador.gui;

import com.compilador.errores.ErrorSintactico;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Insets;
import java.util.List;

/**
 * Panel para mostrar el resultado del análisis sintáctico:
 * - AST generado (representación textual)
 * - Errores sintácticos con detalles contextuales
 */
public class PanelSintactico extends JPanel {

    private final JTextArea areaAST;
    private final JPanel panelErrores;
    private final JLabel labelEstado;
    private final java.util.function.BiConsumer<Integer, Integer> alSeleccionarError;

    public PanelSintactico() {
        this((linea, columna) -> { });
    }

    public PanelSintactico(java.util.function.BiConsumer<Integer, Integer> alSeleccionarError) {
        this.alSeleccionarError = alSeleccionarError;
        setLayout(new BorderLayout(0, 4));
        setBackground(Colores.FONDO_PANEL);

        // ── Encabezado ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Colores.FONDO_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel titulo = new JLabel("Análisis Sintáctico");
        titulo.setFont(Colores.FUENTE_TITULO);
        titulo.setForeground(Colores.ACENTO_SINTACTICO);
        header.add(titulo, BorderLayout.WEST);

        labelEstado = new JLabel("");
        labelEstado.setFont(Colores.FUENTE_PEQUENA);
        labelEstado.setForeground(Colores.TEXTO_TENUE);
        header.add(labelEstado, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Split: AST arriba, errores abajo ──
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setBackground(Colores.FONDO_PANEL);
        split.setDividerLocation(200);
        split.setDividerSize(4);
        split.setBorder(BorderFactory.createEmptyBorder());

        // AST
        areaAST = new JTextArea();
        areaAST.setFont(Colores.FUENTE_TABLA);
        areaAST.setBackground(Colores.FONDO_EDITOR);
        areaAST.setForeground(Colores.ACENTO_SINTACTICO);
        areaAST.setCaretColor(Colores.TEXTO_NORMAL);
        areaAST.setEditable(false);
        areaAST.setMargin(new Insets(8, 8, 8, 8));
        areaAST.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Colores.BORDE),
            "Árbol Sintáctico (AST)",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            Colores.FUENTE_PEQUENA,
            Colores.TEXTO_TENUE
        ));

        JScrollPane scrollAST = new JScrollPane(areaAST);
        scrollAST.setBorder(BorderFactory.createEmptyBorder());
        split.setTopComponent(scrollAST);

        // Panel de errores detallados
        panelErrores = new JPanel();
        panelErrores.setLayout(new BoxLayout(panelErrores, BoxLayout.Y_AXIS));
        panelErrores.setBackground(Colores.FONDO_PANEL);
        panelErrores.setBorder(BorderFactory.createEmptyBorder(8, 8, 8, 8));

        JScrollPane scrollErr = new JScrollPane(panelErrores);
        scrollErr.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Colores.BORDE),
            "Errores Sintácticos Detectados",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            Colores.FUENTE_PEQUENA,
            Colores.TEXTO_TENUE
        ));
        scrollErr.getViewport().setBackground(Colores.FONDO_PANEL);
        split.setBottomComponent(scrollErr);

        add(split, BorderLayout.CENTER);
    }

    /**
     * Muestra el AST generado.
     */
    public void mostrarAST(String ast) {
        areaAST.setText(ast);
        areaAST.setCaretPosition(0);
    }

    /**
     * Carga los errores sintácticos con detalles visuales.
     */
    public void cargarErrores(List<ErrorSintactico> errores) {
        panelErrores.removeAll();

        if (errores.isEmpty()) {
            JLabel lblExito = new JLabel("✓ Sin errores sintácticos");
            lblExito.setFont(Colores.FUENTE_NORMAL);
            lblExito.setForeground(Colores.EXITO);
            panelErrores.add(lblExito);
            labelEstado.setText("✓ Sintaxis válida");
        } else {
            labelEstado.setText("✗ " + errores.size() + " error" + 
                (errores.size() > 1 ? "es" : "") + " sintáctico" + 
                (errores.size() > 1 ? "s" : ""));

            for (ErrorSintactico error : errores) {
                panelErrores.add(crearItemError(error));
                panelErrores.add(Box.createVerticalStrut(8));
            }
        }

        panelErrores.add(Box.createVerticalGlue());
        panelErrores.revalidate();
        panelErrores.repaint();
    }

    private JPanel crearItemError(ErrorSintactico error) {
        JPanel itemPanel = new JPanel(new BorderLayout(8, 0));
        itemPanel.setBackground(Colores.FONDO_TABLA_ROW1);
        Color colorEstado = Colores.ADVERTENCIA;

        // Borde minimalista: línea lateral delgada de 3px y padding compacto
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 3, 0, 0, colorEstado),
            BorderFactory.createEmptyBorder(6, 10, 6, 10)
        ));
        itemPanel.setCursor(new java.awt.Cursor(java.awt.Cursor.HAND_CURSOR));

        // Contenedor principal vertical
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(Colores.FONDO_TABLA_ROW1);

        com.compilador.errores.FormateadorErrores.ErrorFormateado info = 
            com.compilador.errores.FormateadorErrores.formatearSintactico(error);

        // Fila 1: [Línea X, Col Y] • Título  ---  Chip del token
        JPanel filaHeader = new JPanel(new BorderLayout(6, 0));
        filaHeader.setBackground(Colores.FONDO_TABLA_ROW1);

        JLabel lblTitulo = new JLabel("Línea " + error.getLinea() + ":" + error.getColumna() + "  •  " + info.getTitulo());
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblTitulo.setForeground(colorEstado);
        filaHeader.add(lblTitulo, BorderLayout.WEST);

        if (error.getTokenEncontrado() != null && !error.getTokenEncontrado().isEmpty()) {
            JLabel lblToken = new JLabel(" " + error.getTokenEncontrado() + " ");
            lblToken.setFont(new Font("Consolas", Font.BOLD, 12));
            lblToken.setForeground(Colores.ADVERTENCIA); // Dorado claro de alto contraste
            lblToken.setBackground(new java.awt.Color(12, 28, 52)); // Fondo oscuro nítido
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

        // Hover effect y click para saltar a la línea del error
        java.awt.event.MouseAdapter clickListener = new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    alSeleccionarError.accept(error.getLinea(), error.getColumna());
                }
            }
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                itemPanel.setBackground(Colores.FONDO_SELECCION);
                contenido.setBackground(Colores.FONDO_SELECCION);
                filaHeader.setBackground(Colores.FONDO_SELECCION);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, Colores.BORDE_ENFOCADO),
                    BorderFactory.createEmptyBorder(6, 10, 6, 10)
                ));
            }
            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
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
     * Limpia el panel.
     */
    public void limpiar() {
        areaAST.setText("");
        panelErrores.removeAll();
        labelEstado.setText("");
        panelErrores.revalidate();
        panelErrores.repaint();
    }
}
