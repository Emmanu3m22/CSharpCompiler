package com.compilador.gui;

import com.compilador.semantic.ErrorSemantico;
import com.compilador.semantic.Simbolo;
import com.compilador.semantic.TablaSimbolos;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.List;
import java.util.Map;

/**
 * Panel para mostrar el resultado del análisis semántico:
 * - Tabla de símbolos (variables declaradas, tipo, ámbito, inicialización)
 * - Errores semánticos
 */
public class PanelSemantico extends JPanel {

    private final JTable tablaSimbolos;
    private final DefaultTableModel modeloSimbolos;
    private final JTable tablaErrores;
    private final DefaultTableModel modeloErrores;
    private final JLabel labelEstado;

    private static final String[] COL_SIMBOLOS = {"Nombre", "Tipo", "Ámbito", "Línea", "Inicializado"};
    private static final String[] COL_ERRORES  = {"Línea", "Columna", "Tipo Error", "Mensaje"};

    public PanelSemantico() {
        setLayout(new BorderLayout(0, 4));
        setBackground(Colores.FONDO_PANEL);

        // ── Encabezado ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Colores.FONDO_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel titulo = new JLabel("Análisis Semántico");
        titulo.setFont(Colores.FUENTE_TITULO);
        titulo.setForeground(Colores.ACENTO_SEMANTICO);
        header.add(titulo, BorderLayout.WEST);

        labelEstado = new JLabel("");
        labelEstado.setFont(Colores.FUENTE_PEQUENA);
        labelEstado.setForeground(Colores.TEXTO_TENUE);
        header.add(labelEstado, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Split: símbolos arriba, errores abajo ──
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setBackground(Colores.FONDO_PANEL);
        split.setDividerLocation(180);
        split.setDividerSize(4);
        split.setBorder(BorderFactory.createEmptyBorder());

        // Tabla de símbolos
        modeloSimbolos = new DefaultTableModel(COL_SIMBOLOS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaSimbolos = crearTabla(modeloSimbolos, Colores.ACENTO_SEMANTICO, Colores.TEXTO_NORMAL);

        JScrollPane scrollSim = new JScrollPane(tablaSimbolos);
        scrollSim.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Colores.BORDE),
            "Tabla de Símbolos",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            Colores.FUENTE_PEQUENA,
            Colores.TEXTO_TENUE
        ));
        scrollSim.getViewport().setBackground(Colores.FONDO_PANEL);
        split.setTopComponent(scrollSim);

        // Tabla de errores
        modeloErrores = new DefaultTableModel(COL_ERRORES, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tablaErrores = crearTabla(modeloErrores, Colores.ACENTO_SEMANTICO, Colores.ERROR);

        JScrollPane scrollErr = new JScrollPane(tablaErrores);
        scrollErr.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Colores.BORDE),
            "Errores Semánticos",
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
     * Carga la tabla de símbolos desde el analizador semántico.
     */
    public void cargarSimbolos(TablaSimbolos tabla) {
        modeloSimbolos.setRowCount(0);
        Map<String, Simbolo> simbolos = tabla.getSimbolos();
        for (Simbolo s : simbolos.values()) {
            modeloSimbolos.addRow(new Object[]{
                s.getNombre(),
                s.getTipo().getNombre(),
                s.getAmbito(),
                s.getLineaDeclaracion(),
                s.isInicializado() ? "Sí" : "No"
            });
        }
    }

    /**
     * Carga los errores semánticos en la tabla.
     */
    public void cargarErrores(List<ErrorSemantico> errores) {
        modeloErrores.setRowCount(0);
        for (ErrorSemantico e : errores) {
            modeloErrores.addRow(new Object[]{
                e.getLinea(),
                e.getColumna(),
                e.getTipoError(),
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
        modeloSimbolos.setRowCount(0);
        modeloErrores.setRowCount(0);
        labelEstado.setText("");
    }

    // ── Helper para crear tablas con estilo unificado ──

    private JTable crearTabla(DefaultTableModel modelo, Color accentColor, Color textColor) {
        JTable tabla = new JTable(modelo);
        tabla.setFont(Colores.FUENTE_TABLA);
        tabla.setBackground(Colores.FONDO_TABLA_ROW1);
        tabla.setForeground(textColor);
        tabla.setSelectionBackground(Colores.FONDO_SELECCION);
        tabla.setSelectionForeground(Colores.TEXTO_NORMAL);
        tabla.setGridColor(Colores.BORDE);
        tabla.setRowHeight(24);
        tabla.setShowVerticalLines(false);

        JTableHeader th = tabla.getTableHeader();
        th.setFont(Colores.FUENTE_TITULO);
        th.setBackground(Colores.FONDO_TABLA_HEADER);
        th.setForeground(Colores.TEXTO_NORMAL);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, accentColor));
        th.setReorderingAllowed(false);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Colores.FONDO_TABLA_ROW1 : Colores.FONDO_TABLA_ROW2);
                }
                c.setForeground(textColor);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return c;
            }
        });

        return tabla;
    }
}
