package com.compilador.gui;

import com.compilador.optimizacion.fuente.Hallazgo;
import com.compilador.optimizacion.fuente.ResultadoOptimizacionFuente;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.util.List;
import java.util.function.BiConsumer;

/**
 * Panel para visualizar el proceso de optimización:
 * <ul>
 *   <li>Visualización del Código Optimizado (limpio, sin código muerto ni comentarios).</li>
 *   <li>Bitácora de eliminaciones con número de línea, regla aplicada, código eliminado y motivo pedagógico.</li>
 *   <li>Navegación interactiva al editor principal al hacer clic en cualquier hallazgo.</li>
 * </ul>
 */
public class PanelOptimizacion extends JPanel {

    private static final String[] COL_HALLAZGOS = {
            "#", "Línea", "Regla", "Código Eliminado", "Motivo"
    };

    private final JLabel labelEstado;
    private final JTextArea areaDespues;
    private final JTable tablaHallazgos;
    private final DefaultTableModel modeloHallazgos;
    private final BiConsumer<Integer, Integer> alSeleccionarUbicacion;

    private ResultadoOptimizacionFuente ultimoResultado;

    public PanelOptimizacion() {
        this((l, c) -> {});
    }

    public PanelOptimizacion(BiConsumer<Integer, Integer> alSeleccionarUbicacion) {
        this.alSeleccionarUbicacion = alSeleccionarUbicacion != null ? alSeleccionarUbicacion : (l, c) -> {};

        setLayout(new BorderLayout(0, 0));
        setBackground(Colores.FONDO_PANEL);

        // ── Encabezado ──
        JPanel header = new JPanel(new BorderLayout());
        header.setBackground(Colores.FONDO_PANEL);
        header.setBorder(BorderFactory.createEmptyBorder(6, 10, 6, 10));

        JLabel titulo = new JLabel("Optimización de Código (Eliminación de Código Muerto)");
        titulo.setFont(Colores.FUENTE_TITULO);
        titulo.setForeground(Colores.ACENTO_OPTIMIZACION);
        header.add(titulo, BorderLayout.WEST);

        labelEstado = new JLabel("Sin análisis");
        labelEstado.setFont(Colores.FUENTE_PEQUENA);
        labelEstado.setForeground(Colores.TEXTO_TENUE);
        header.add(labelEstado, BorderLayout.EAST);

        add(header, BorderLayout.NORTH);

        // ── Split Vertical Principal: Arriba (Código Optimizado) | Abajo (Bitácora de Hallazgos) ──
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPrincipal.setDividerLocation(320);
        splitPrincipal.setDividerSize(5);
        Colores.estilizarSplit(splitPrincipal);

        // ── Panel Superior: Código Optimizado Único (Limpio) ──
        areaDespues = crearAreaTexto();
        JPanel contenedorDespues = crearContenedorCodigo("CÓDIGO OPTIMIZADO (LIMPIO)", Colores.ACENTO_OPTIMIZACION, areaDespues);

        splitPrincipal.setTopComponent(Colores.conMargen(contenedorDespues));

        // ── Panel Inferior: Tabla de Hallazgos y Motivos ──
        modeloHallazgos = new DefaultTableModel(COL_HALLAZGOS, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
        tablaHallazgos = crearTabla(modeloHallazgos);
        tablaHallazgos.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaHallazgos.getColumnModel().getColumn(1).setPreferredWidth(60);
        tablaHallazgos.getColumnModel().getColumn(2).setPreferredWidth(160);
        tablaHallazgos.getColumnModel().getColumn(3).setPreferredWidth(220);
        tablaHallazgos.getColumnModel().getColumn(4).setPreferredWidth(320);

        tablaHallazgos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                seleccionarHallazgo();
            }
        });

        JScrollPane scrollTabla = new JScrollPane(tablaHallazgos);
        scrollTabla.setBorder(BorderFactory.createEmptyBorder());
        scrollTabla.getViewport().setBackground(Colores.FONDO_PANEL);

        JPanel panelInferior = new JPanel(new BorderLayout(0, 4));
        panelInferior.setBackground(Colores.FONDO_PANEL);

        JLabel lblBitacora = new JLabel("Bitácora de Código Muerto Eliminado y Motivos:");
        lblBitacora.setFont(Colores.FUENTE_NORMAL);
        lblBitacora.setForeground(Colores.TEXTO_TENUE);
        lblBitacora.setBorder(BorderFactory.createEmptyBorder(4, 6, 2, 6));

        panelInferior.add(lblBitacora, BorderLayout.NORTH);
        panelInferior.add(Colores.conMargen(scrollTabla), BorderLayout.CENTER);

        splitPrincipal.setBottomComponent(panelInferior);

        add(splitPrincipal, BorderLayout.CENTER);
    }

    /**
     * Carga el resultado del pipeline de optimización.
     */
    public void cargar(ResultadoOptimizacionFuente resultado) {
        this.ultimoResultado = resultado;
        limpiarVistas();

        if (resultado == null) {
            labelEstado.setText("Sin análisis");
            labelEstado.setForeground(Colores.TEXTO_TENUE);
            return;
        }

        if (resultado.omitida()) {
            labelEstado.setText("⚠ " + (resultado.motivoOmision() != null ? resultado.motivoOmision() : "Optimización omitida"));
            labelEstado.setForeground(Colores.ADVERTENCIA);
            areaDespues.setText("// Optimización omitida debido a errores en fases previas.");
            return;
        }

        areaDespues.setText(resultado.codigoFinal());

        List<Hallazgo> hallazgos = resultado.todosLosHallazgos();
        int i = 1;
        for (Hallazgo h : hallazgos) {
            modeloHallazgos.addRow(new Object[]{
                    i++,
                    "Línea " + h.linea(),
                    h.tipo().getEtiqueta(),
                    h.codigoEliminado(),
                    h.motivo()
            });
        }

        if (hallazgos.isEmpty()) {
            labelEstado.setText("✓ 0 código muerto detectado (código óptimo)");
            labelEstado.setForeground(Colores.EXITO);
        } else {
            labelEstado.setText(String.format("✓ %d eliminación(es) · Ahorro en cuádruplos y sentencias", hallazgos.size()));
            labelEstado.setForeground(Colores.ACENTO_OPTIMIZACION);
            if (tablaHallazgos.getRowCount() > 0) {
                tablaHallazgos.setRowSelectionInterval(0, 0);
            }
        }
    }

    /**
     * Informa que la optimización fue omitida (por ejemplo por errores léxicos o sintácticos).
     */
    public void mostrarOmitida(String motivo, String codigoOriginal) {
        limpiar();
        areaDespues.setText("// La optimización fue omitida porque el código contiene errores.\n// Corrige los errores en las pestañas anteriores para habilitar la optimización.");
        labelEstado.setText("⚠ " + motivo);
        labelEstado.setForeground(Colores.ADVERTENCIA);
    }

    /**
     * Limpia completamente el panel.
     */
    public void limpiar() {
        this.ultimoResultado = null;
        limpiarVistas();
        labelEstado.setText("");
    }

    private void limpiarVistas() {
        areaDespues.setText("");
        modeloHallazgos.setRowCount(0);
    }

    private void seleccionarHallazgo() {
        int fila = tablaHallazgos.getSelectedRow();
        if (fila < 0 || ultimoResultado == null) return;

        List<Hallazgo> hallazgos = ultimoResultado.todosLosHallazgos();
        if (fila >= hallazgos.size()) return;

        Hallazgo h = hallazgos.get(fila);

        // Navegar en el editor principal (panel izquierdo) donde reside el código original
        if (h.linea() > 0) {
            alSeleccionarUbicacion.accept(h.linea(), 1);
        }
    }

    private JTextArea crearAreaTexto() {
        JTextArea area = new JTextArea();
        area.setFont(Colores.FUENTE_CODIGO);
        area.setBackground(Colores.FONDO_EDITOR);
        area.setForeground(Colores.TEXTO_NORMAL);
        area.setCaretColor(Colores.TEXTO_NORMAL);
        area.setEditable(false);
        area.setMargin(new Insets(8, 8, 8, 8));
        return area;
    }

    private JPanel crearContenedorCodigo(String titulo, Color colorAcento, JTextArea area) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Colores.FONDO_PANEL);

        JLabel lbl = new JLabel(titulo);
        lbl.setFont(Colores.FUENTE_NORMAL);
        lbl.setForeground(colorAcento);
        lbl.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 0, 1, 0, Colores.BORDE),
                BorderFactory.createEmptyBorder(4, 6, 4, 6)
        ));
        p.add(lbl, BorderLayout.NORTH);

        JScrollPane scroll = new JScrollPane(area);
        scroll.setBorder(BorderFactory.createLineBorder(Colores.BORDE, 1));
        scroll.getViewport().setBackground(Colores.FONDO_EDITOR);
        p.add(scroll, BorderLayout.CENTER);

        return p;
    }

    private JTable crearTabla(DefaultTableModel modelo) {
        JTable tabla = new JTable(modelo);
        tabla.setFont(Colores.FUENTE_TABLA);
        tabla.setBackground(Colores.FONDO_TABLA_ROW1);
        tabla.setForeground(Colores.TEXTO_NORMAL);
        tabla.setSelectionBackground(Colores.SEL_FONDO);
        tabla.setSelectionForeground(Colores.SEL_TEXTO);
        tabla.setGridColor(Colores.BORDE);
        tabla.setRowHeight(24);
        tabla.setShowVerticalLines(false);
        tabla.setIntercellSpacing(new Dimension(0, 1));

        JTableHeader th = tabla.getTableHeader();
        th.setFont(Colores.FUENTE_TITULO);
        th.setBackground(Colores.FONDO_TABLA_HEADER);
        th.setForeground(Colores.TEXTO_NORMAL);
        th.setBorder(BorderFactory.createMatteBorder(0, 0, 2, 0, Colores.ACENTO_OPTIMIZACION));
        th.setReorderingAllowed(false);

        tabla.setDefaultRenderer(Object.class, new DefaultTableCellRenderer() {
            @Override
            public Component getTableCellRendererComponent(JTable table, Object value,
                                                           boolean isSelected, boolean hasFocus, int row, int column) {
                Component c = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
                if (!isSelected) {
                    c.setBackground(row % 2 == 0 ? Colores.FONDO_TABLA_ROW1 : Colores.FONDO_TABLA_ROW2);
                }
                c.setForeground(isSelected ? Colores.SEL_TEXTO : Colores.TEXTO_NORMAL);
                setBorder(BorderFactory.createEmptyBorder(0, 6, 0, 6));

                // Colorear el tipo de regla para visualización clara
                if (column == 2 && value != null) {
                    String tipo = value.toString();
                    if (tipo.contains("Inalcanzable")) {
                        c.setForeground(isSelected ? Colores.SEL_TEXTO : Colores.ERROR);
                    } else if (tipo.contains("Constante")) {
                        c.setForeground(isSelected ? Colores.SEL_TEXTO : Colores.ADVERTENCIA);
                    } else if (tipo.contains("Sin Uso")) {
                        c.setForeground(isSelected ? Colores.SEL_TEXTO : Colores.ACENTO_OPTIMIZACION);
                    }
                }
                return c;
            }
        });

        return tabla;
    }
}
