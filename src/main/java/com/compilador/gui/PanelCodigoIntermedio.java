package com.compilador.gui;

import com.compilador.intermedio.Cuadruplo;
import com.compilador.intermedio.ProgramaIntermedio;
import com.compilador.intermedio.ResultadoExpresion;


import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Panel para visualizar el código intermedio generado:
 * <ul>
 *   <li>Tabla superior: lista de expresiones con notación infija, prefija (polaca),
 *       posfija (polaca inversa) y resultado final.</li>
 *   <li>Pestaña inferior 1: Cuádruplos generados (op, arg1, arg2, res) con temporales tN.</li>
 *   <li>Pestaña inferior 2: Traza paso a paso de la pila posfija.</li>
 * </ul>
 */
public class PanelCodigoIntermedio extends JPanel {

    private static final String[] COL_EXPRESIONES = {
            "Destino", "Infijo", "Prefijo", "Posfijo", "Resultado"
    };
    private static final int COL_RESULTADO = 4;
    private static final String[] COL_CUADRUPLOS = {
            "#", "Operador", "Arg 1", "Arg 2", "Resultado"
    };

    private final JTable tablaExpresiones;
    private final DefaultTableModel modeloExpresiones;
    private final JTable tablaCuadruplos;
    private final DefaultTableModel modeloCuadruplos;
    private final JTextArea areaTrazaPila;
    private final JLabel labelEstado;
    private final BiConsumer<Integer, Integer> alSeleccionarUbicacion;

    private ProgramaIntermedio ultimoPrograma;

    public PanelCodigoIntermedio() {
        this((l, c) -> {});
    }

    public PanelCodigoIntermedio(BiConsumer<Integer, Integer> alSeleccionarUbicacion) {
        this.alSeleccionarUbicacion = alSeleccionarUbicacion != null ? alSeleccionarUbicacion : (l, c) -> {};

        setLayout(new BorderLayout(0, 0));
        setBackground(Colores.FONDO_PANEL);

        // ── Encabezado ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Colores.FONDO_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));

        JLabel titulo = new JLabel("Código Intermedio (Polaca y Cuádruplos)");
        titulo.setFont(Colores.FUENTE_TITULO);
        titulo.setForeground(Colores.ACENTO_INTERMEDIO);
        header.add(titulo, BorderLayout.WEST);

        labelEstado = new JLabel("Sin análisis");
        labelEstado.setFont(Colores.FUENTE_PEQUENA);
        labelEstado.setForeground(Colores.TEXTO_TENUE);
        header.add(labelEstado, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Split principal (arriba: expresiones, abajo: pestañas de detalle) ──
        JSplitPane split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        split.setDividerLocation(200);
        split.setDividerSize(4);
        Colores.estilizarSplit(split);

        // Tabla de expresiones
        modeloExpresiones = new DefaultTableModel(COL_EXPRESIONES, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaExpresiones = crearTabla(modeloExpresiones, Colores.ACENTO_INTERMEDIO, Colores.TEXTO_NORMAL);
        tablaExpresiones.getColumnModel().getColumn(0).setPreferredWidth(70);
        tablaExpresiones.getColumnModel().getColumn(1).setPreferredWidth(140);
        tablaExpresiones.getColumnModel().getColumn(2).setPreferredWidth(140);
        tablaExpresiones.getColumnModel().getColumn(3).setPreferredWidth(140);
        tablaExpresiones.getColumnModel().getColumn(4).setPreferredWidth(80);

        tablaExpresiones.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                actualizarDetallePorSeleccion();
            }
        });

        JScrollPane scrollExpresiones = new JScrollPane(tablaExpresiones);
        scrollExpresiones.setBorder(BorderFactory.createEmptyBorder());
        scrollExpresiones.getViewport().setBackground(Colores.FONDO_PANEL);
        split.setTopComponent(Colores.conMargen(scrollExpresiones));

        // ── Pestañas inferiores de detalle ──
        JTabbedPane pestanasDetalle = new JTabbedPane(JTabbedPane.TOP);
        pestanasDetalle.setBackground(Colores.FONDO_PANEL);
        Colores.estilizarPestanas(pestanasDetalle);

        // Subpestaña 1: Cuádruplos
        modeloCuadruplos = new DefaultTableModel(COL_CUADRUPLOS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaCuadruplos = crearTabla(modeloCuadruplos, Colores.ACENTO_INTERMEDIO, Colores.TEXTO_NORMAL);
        JScrollPane scrollCuad = new JScrollPane(tablaCuadruplos);
        scrollCuad.setBorder(BorderFactory.createEmptyBorder());
        scrollCuad.getViewport().setBackground(Colores.FONDO_PANEL);
        pestanasDetalle.addTab("Cuádruplos", Colores.conMargen(scrollCuad));

        // Subpestaña 2: Traza de pila (posfijo)
        areaTrazaPila = new JTextArea();
        areaTrazaPila.setFont(Colores.FUENTE_TABLA);
        areaTrazaPila.setBackground(Colores.FONDO_EDITOR);
        areaTrazaPila.setForeground(Colores.TEXTO_NORMAL);
        areaTrazaPila.setCaretColor(Colores.TEXTO_NORMAL);
        areaTrazaPila.setEditable(false);
        areaTrazaPila.setMargin(new java.awt.Insets(6, 8, 6, 8));
        JScrollPane scrollTraza = new JScrollPane(areaTrazaPila);
        scrollTraza.getViewport().setBackground(Colores.FONDO_EDITOR);
        pestanasDetalle.addTab("Traza de Pila (Posfijo)", Colores.conMargen(scrollTraza));

        split.setBottomComponent(pestanasDetalle);
        add(split, BorderLayout.CENTER);
    }

    /**
     * Carga el resultado del generador de código intermedio.
     */
    public void cargar(ProgramaIntermedio programa) {
        this.ultimoPrograma = programa;
        modeloExpresiones.setRowCount(0);
        modeloCuadruplos.setRowCount(0);
        areaTrazaPila.setText("");

        if (programa == null || programa.estaVacio()) {
            labelEstado.setText("0 operaciones aritméticas");
            labelEstado.setForeground(Colores.TEXTO_TENUE);
            areaTrazaPila.setText("No se encontraron operaciones aritméticas en el código.");
            return;
        }

        for (ResultadoExpresion exp : programa.expresiones()) {
            modeloExpresiones.addRow(new Object[]{
                    exp.destino() != null ? exp.destino() : "—",
                    exp.infijo(),
                    exp.prefijo(),
                    exp.posfijo(),
                    exp.resultadoOErrorTexto()
            });
        }

        cargarCuadruplos(programa.cuadruplos());

        labelEstado.setText(String.format("✓ %d operación(es), %d cuádruplo(s)",
                programa.totalExpresiones(), programa.totalCuadruplos()));
        labelEstado.setForeground(Colores.EXITO);

        // Seleccionar la primera fila automáticamente si existe
        if (tablaExpresiones.getRowCount() > 0) {
            tablaExpresiones.setRowSelectionInterval(0, 0);
        }
    }

    /**
     * Limpia todo el contenido del panel.
     */
    public void limpiar() {
        this.ultimoPrograma = null;
        modeloExpresiones.setRowCount(0);
        modeloCuadruplos.setRowCount(0);
        areaTrazaPila.setText("");
        labelEstado.setText("");
    }

    private void actualizarDetallePorSeleccion() {
        int fila = tablaExpresiones.getSelectedRow();
        if (fila < 0 || ultimoPrograma == null || fila >= ultimoPrograma.expresiones().size()) {
            if (ultimoPrograma != null) {
                cargarCuadruplos(ultimoPrograma.cuadruplos());
            }
            areaTrazaPila.setText("Selecciona una operación de la tabla superior para ver su traza de pila.");
            return;
        }

        ResultadoExpresion seleccionada = ultimoPrograma.expresiones().get(fila);

        // Actualizar cuádruplos de la expresión seleccionada
        cargarCuadruplos(seleccionada.cuadruplos());

        // Actualizar traza de pila
        if (seleccionada.trazaPila() != null && !seleccionada.trazaPila().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            sb.append("Evaluación con pila sobre el posfijo: ").append(seleccionada.posfijo()).append("\n\n");
            for (String paso : seleccionada.trazaPila()) {
                sb.append("  ").append(paso).append("\n");
            }
            sb.append("\nResultado final: ").append(seleccionada.resultadoOErrorTexto()).append("\n");
            areaTrazaPila.setText(sb.toString());
            areaTrazaPila.setCaretPosition(0);
        } else {
            areaTrazaPila.setText("Sin traza de pila (" + seleccionada.resultadoOErrorTexto() + ")");
        }

        // Navegar a la línea en el editor si es válida
        if (seleccionada.linea() > 0) {
            alSeleccionarUbicacion.accept(seleccionada.linea(), 1);
        }
    }

    private void cargarCuadruplos(List<Cuadruplo> lista) {
        modeloCuadruplos.setRowCount(0);
        if (lista == null) return;
        int i = 1;
        for (Cuadruplo c : lista) {
            modeloCuadruplos.addRow(new Object[]{
                    i++,
                    c.operador(),
                    c.arg1Texto(),
                    c.arg2Texto(),
                    c.resultadoTexto()
            });
        }
    }

    // ── Helper para crear tablas con estilo unificado ──

    private JTable crearTabla(DefaultTableModel modelo, Color accentColor, Color textColor) {
        JTable tabla = new JTable(modelo);
        tabla.setFont(Colores.FUENTE_TABLA);
        tabla.setBackground(Colores.FONDO_TABLA_ROW1);
        tabla.setForeground(textColor);
        tabla.setSelectionBackground(Colores.SEL_FONDO);
        tabla.setSelectionForeground(Colores.SEL_TEXTO);
        tabla.setGridColor(Colores.BORDE);
        tabla.setRowHeight(24);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new java.awt.Dimension(0, 1));

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
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Colores.FONDO_TABLA_ROW1 : Colores.FONDO_TABLA_ROW2);
                }
                c.setForeground(isSelected ? Colores.SEL_TEXTO : textColor);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

                // Resaltar en verde resultados exitosos, en advertencia errores de evaluación
                if (value != null && column == COL_RESULTADO && "Destino".equals(table.getColumnName(0))) {
                    String s = value.toString();
                    if (s.startsWith("No evaluable")) {
                        c.setForeground(Colores.ADVERTENCIA);
                    } else if (!isSelected) {
                        c.setForeground(Colores.EXITO);
                    }
                }
                return c;
            }
        });

        return tabla;
    }
}
