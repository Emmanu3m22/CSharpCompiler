package com.compilador.gui;

import com.compilador.errores.ErrorSintactico;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;

/**
 * Panel para mostrar el resultado del análisis sintáctico:
 * - AST generado (representación textual)
 * - Errores sintácticos en una tabla
 */
public class PanelSintactico extends JPanel {

    private final JTextArea areaAST;
    private final JTable tablaErrores;
    private final DefaultTableModel modeloErrores;
    private final JLabel labelEstado;

    private static final String[] COLUMNAS_ERR = {"Línea", "Columna", "Encontrado", "Esperado", "Mensaje"};

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

        // Tabla de errores
        modeloErrores = new DefaultTableModel(COLUMNAS_ERR, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaErrores = new JTable(modeloErrores);
        tablaErrores.setFont(Colores.FUENTE_TABLA);
        tablaErrores.setBackground(Colores.FONDO_TABLA_ROW1);
        tablaErrores.setForeground(Colores.TEXTO_NORMAL);
        tablaErrores.setSelectionBackground(Colores.FONDO_SELECCION);
        tablaErrores.setGridColor(Colores.BORDE);
        tablaErrores.setRowHeight(24);
        tablaErrores.setShowVerticalLines(false);

        JTableHeader th = tablaErrores.getTableHeader();
        th.setFont(Colores.FUENTE_TITULO);
        th.setBackground(Colores.FONDO_TABLA_HEADER);
        th.setForeground(Colores.TEXTO_NORMAL);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Colores.ACENTO_SINTACTICO));
        th.setReorderingAllowed(false);

        // Renderer para filas alternadas
        tablaErrores.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Colores.FONDO_TABLA_ROW1 : Colores.FONDO_TABLA_ROW2);
                }
                c.setForeground(Colores.ERROR);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return c;
            }
        });

        JScrollPane scrollErr = new JScrollPane(tablaErrores);
        scrollErr.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Colores.BORDE),
            "Errores Sintácticos",
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
     * Carga los errores sintácticos en la tabla.
     */
    public void cargarErrores(List<ErrorSintactico> errores) {
        modeloErrores.setRowCount(0);
        for (ErrorSintactico e : errores) {
            modeloErrores.addRow(new Object[]{
                e.getLinea(),
                e.getColumna(),
                e.getTokenEncontrado(),
                e.getTokenEsperado() != null ? e.getTokenEsperado() : "-",
                e.getMensaje()
            });
        }
        if (errores.isEmpty()) {
            labelEstado.setText("✓ Sin errores");
            labelEstado.setForeground(Colores.EXITO);
        } else {
            labelEstado.setText("✗ " + errores.size() + " error" + (errores.size() > 1 ? "es" : ""));
            labelEstado.setForeground(Colores.ERROR);
        }
    }

    /**
     * Limpia el panel.
     */
    public void limpiar() {
        areaAST.setText("");
        modeloErrores.setRowCount(0);
        labelEstado.setText("");
    }
}
