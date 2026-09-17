package com.compilador.gui;

import com.compilador.errores.ErrorSintactico;

import javax.swing.*;
import java.awt.BorderLayout;
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

    public PanelSintactico() {
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
        JPanel itemPanel = new JPanel(new BorderLayout(8, 4));
        itemPanel.setBackground(Colores.FONDO_TABLA_ROW1);
        itemPanel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(8, 10, 8, 10)
        ));
        itemPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 120));

        // Lado izquierdo: Contenido del error
        JPanel contenido = new JPanel();
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBackground(Colores.FONDO_TABLA_ROW1);

        // Línea 1: Ubicación
        JLabel lblUbicacion = new JLabel("📍 Línea " + error.getLinea() + ", Columna " + error.getColumna());
        lblUbicacion.setFont(new Font("Segoe UI", Font.BOLD, 12));
        lblUbicacion.setForeground(Colores.ADVERTENCIA);
        contenido.add(lblUbicacion);

        // Limpiar el mensaje de JavaCC
        String rawMsg = error.getMensaje() != null ? error.getMensaje() : "";
        String esperadoLimpio = error.getTokenEsperado();
        String mensajeLimpio = rawMsg;
        
        if (rawMsg.contains("Was expecting:")) {
            String[] parts = rawMsg.split("Was expecting:");
            esperadoLimpio = parts[1].replaceAll("\n", "").replaceAll("\r", "").replaceAll("    ", " ").trim();
            
            if (esperadoLimpio.contains("\";\"")) {
                esperadoLimpio = ";";
                mensajeLimpio = "Falta un punto y coma ';' al final de la instrucción.";
            } else {
                mensajeLimpio = "Se encontró un token inesperado.";
            }
        } else if (rawMsg.contains("Was expecting one of:")) {
            String[] parts = rawMsg.split("Was expecting one of:");
            esperadoLimpio = parts[1].replaceAll("\n", "").replaceAll("\r", "").replaceAll("    ", " ").replaceAll("\\.\\.\\.", "").trim();
            
            if (esperadoLimpio.contains("\";\"")) {
                esperadoLimpio = ";";
                mensajeLimpio = "Falta un punto y coma ';' al final de la instrucción.";
            } else {
                mensajeLimpio = "Se encontró un token inesperado.";
            }
        }

        // Línea 2: Token encontrado
        JLabel lblEncontrado = new JLabel("<html><b>Se encontró:</b> <span style='color:#E06C75; font-family:monospace;'>" + 
            error.getTokenEncontrado().replace("<", "&lt;").replace(">", "&gt;") + "</span></html>");
        lblEncontrado.setFont(Colores.FUENTE_TABLA);
        contenido.add(lblEncontrado);

        // Línea 3: Token esperado (limpio)
        if (!"...".equals(esperadoLimpio) && !esperadoLimpio.isEmpty()) {
            JLabel lblEsperado = new JLabel("<html><b>Se esperaba:</b> <span style='color:#98C379; font-family:monospace;'>" + 
                esperadoLimpio.replace("<", "&lt;").replace(">", "&gt;") + "</span></html>");
            lblEsperado.setFont(Colores.FUENTE_TABLA);
            contenido.add(lblEsperado);
        }

        // Línea 4: Mensaje de error
        JLabel lblMensaje = new JLabel("<html><p style='width:350px; color:#ABB2BF; margin-top:4px;'>" + 
            mensajeLimpio.replace("<", "&lt;").replace(">", "&gt;") + "</p></html>");
        lblMensaje.setFont(Colores.FUENTE_NORMAL);
        contenido.add(lblMensaje);

        itemPanel.add(contenido, BorderLayout.CENTER);

        // Lado derecho: Icono
        JLabel iconoError = new JLabel("⚠️");
        iconoError.setFont(new Font("Arial", Font.PLAIN, 24));
        iconoError.setHorizontalAlignment(SwingConstants.CENTER);
        iconoError.setPreferredSize(new Dimension(40, 80));
        itemPanel.add(iconoError, BorderLayout.EAST);

        // Hover effect
        itemPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseEntered(java.awt.event.MouseEvent e) {
                itemPanel.setBackground(Colores.FONDO_SELECCION);
                contenido.setBackground(Colores.FONDO_SELECCION);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colores.BORDE_ENFOCADO, 2),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }

            @Override
            public void mouseExited(java.awt.event.MouseEvent e) {
                itemPanel.setBackground(Colores.FONDO_TABLA_ROW1);
                contenido.setBackground(Colores.FONDO_TABLA_ROW1);
                itemPanel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colores.BORDE, 1),
                    BorderFactory.createEmptyBorder(8, 10, 8, 10)
                ));
            }
        });

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
