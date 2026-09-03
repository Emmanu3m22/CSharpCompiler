package com.compilador.gui;

import com.compilador.errores.ErrorLexico;

import javax.swing.*;
import javax.swing.table.*;
import java.awt.*;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Panel que muestra los tokens reconocidos por el analizador léxico
 * en formato de tabla (lexema, tipo de token, línea, columna).
 */
public class TablaTokens extends JPanel {

    private final JTable tabla;
    private final DefaultTableModel modelo;
    private final JLabel labelConteo;
    private Set<String> tokenesConError = new HashSet<>();

    private static final String[] COLUMNAS = { "Lexema", "ID", "Línea", "Columna" };

    public TablaTokens() {
        setLayout(new BorderLayout(0, 4));
        setBackground(Colores.FONDO_PANEL);

        // ── Encabezado ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Colores.FONDO_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel titulo = new JLabel("Tokens Reconocidos");
        titulo.setFont(Colores.FUENTE_TITULO);
        titulo.setForeground(Colores.ACENTO_LEXICO);
        header.add(titulo, BorderLayout.WEST);

        labelConteo = new JLabel("0 tokens");
        labelConteo.setFont(Colores.FUENTE_PEQUENA);
        labelConteo.setForeground(Colores.TEXTO_TENUE);
        header.add(labelConteo, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Tabla ──
        modelo = new DefaultTableModel(COLUMNAS, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        tabla = new JTable(modelo);
        tabla.setFont(Colores.FUENTE_TABLA);
        tabla.setBackground(Colores.FONDO_TABLA_ROW1);
        tabla.setForeground(Colores.TEXTO_NORMAL);
        tabla.setSelectionBackground(Colores.FONDO_SELECCION);
        tabla.setSelectionForeground(Colores.TEXTO_NORMAL);
        tabla.setGridColor(Colores.BORDE);
        tabla.setRowHeight(24);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new Dimension(0, 1));

        // Header de la tabla
        JTableHeader th = tabla.getTableHeader();
        th.setFont(Colores.FUENTE_TITULO);
        th.setBackground(Colores.FONDO_TABLA_HEADER);
        th.setForeground(Colores.TEXTO_NORMAL);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Colores.ACENTO_LEXICO));
        th.setReorderingAllowed(false);

        // Ancho de columnas
        tabla.getColumnModel().getColumn(0).setPreferredWidth(150); // Lexema
        tabla.getColumnModel().getColumn(1).setPreferredWidth(140); // Tipo
        tabla.getColumnModel().getColumn(2).setPreferredWidth(50); // Línea
        tabla.getColumnModel().getColumn(3).setPreferredWidth(60); // Columna

        // Renderer para filas alternadas con resaltado de errores
        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                    boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value,
                        isSelected, hasFocus, row, column);

                // Obtener datos de la fila
                String lexema = (String) table.getValueAt(row, 0);
                String linea = (String) table.getValueAt(row, 2);
                String columna = (String) table.getValueAt(row, 3);
                String clave = lexema + "@" + linea + "@" + columna;

                if (!isSelected) {
                    if (tokenesConError.contains(clave)) {
                        // Resaltar tokens con errores en rojo
                        c.setBackground(new Color(255, 200, 200)); // Rojo suave
                        c.setForeground(new Color(139, 0, 0)); // Rojo oscuro para el texto
                    } else {
                        c.setBackground(row % 2 == 0 ? Colores.FONDO_TABLA_ROW1 : Colores.FONDO_TABLA_ROW2);
                        c.setForeground(Colores.TEXTO_NORMAL);
                    }
                }
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));
                return c;
            }
        });

        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Colores.FONDO_PANEL);
        add(scroll, BorderLayout.CENTER);
    }

    /**
     * Carga los tokens del analizador en la tabla.
     * Se recibe la lista de tokens ya extraídos del parser.
     *
     * @param tokens lista de tokens (cada uno es un arreglo: [lexema, tipo, línea,
     *               columna])
     */
    public void cargarTokens(List<String[]> tokens) {
        cargarTokens(tokens, null);
    }

    /**
     * Carga los tokens del analizador en la tabla y resalta los errores léxicos.
     *
     * @param tokens         lista de tokens (cada uno es un arreglo: [lexema, tipo,
     *                       línea, columna])
     * @param erroresLexicos lista de errores léxicos para resaltar
     */
    public void cargarTokens(List<String[]> tokens, List<ErrorLexico> erroresLexicos) {
        modelo.setRowCount(0);
        tokenesConError.clear();

        // Construir un set de tokens con errores para búsqueda rápida
        if (erroresLexicos != null) {
            for (ErrorLexico error : erroresLexicos) {
                String clave = error.getLexema() + "@" + error.getLinea() + "@" + error.getColumna();
                tokenesConError.add(clave);
            }
        }

        // Agregar tokens a la tabla
        for (String[] token : tokens) {
            modelo.addRow(token);
        }
        labelConteo.setText(tokens.size() + " token" + (tokens.size() != 1 ? "s" : ""));
        tabla.repaint();
    }

    /**
     * Limpia la tabla de tokens.
     */
    public void limpiar() {
        modelo.setRowCount(0);
        labelConteo.setText("0 tokens");
    }
}
