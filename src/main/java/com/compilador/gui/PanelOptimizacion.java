package com.compilador.gui;

import com.compilador.optimizacion.fuente.GuardadorCodigo;
import com.compilador.optimizacion.fuente.Hallazgo;
import com.compilador.optimizacion.fuente.ResultadoOptimizacionFuente;
import com.compilador.optimizacion.fuente.VerificacionOptimizado;

import javax.swing.*;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;
import java.awt.*;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Path;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * Panel para visualizar el proceso de optimización:
 * <ul>
 *   <li>Código optimizado (limpio, sin código muerto ni comentarios) con opciones de
 *       guardar, copiar y reemplazar el contenido del editor.</li>
 *   <li>Bitácora de eliminaciones con número de línea, regla aplicada, código eliminado y motivo.</li>
 *   <li>Verificación del código optimizado: errores léxicos, sintácticos y semánticos, y
 *       advertencias del optimizador, explicados con claridad.</li>
 * </ul>
 * Si el código optimizado resultaría inválido, la optimización no se aplica y se explica por qué.
 */
public class PanelOptimizacion extends JPanel {

    private static final String[] COL_HALLAZGOS = {
            "#", "Línea", "Regla", "Código Eliminado", "Motivo"
    };

    private static final String[] COL_VERIFICACION = {
            "Tipo", "Línea (código optimizado)", "Mensaje", "Sugerencia"
    };

    private static final String TITULO_CODIGO_OK = "CÓDIGO OPTIMIZADO (LIMPIO)";
    private static final String TITULO_CODIGO_RECHAZADO =
            "CÓDIGO OPTIMIZADO RECHAZADO — NO SE APLICÓ (las líneas de los errores son las de este texto)";

    private final JLabel labelEstado;
    private final JLabel labelTituloCodigo;
    private final JTextArea areaDespues;
    private final JTable tablaHallazgos;
    private final DefaultTableModel modeloHallazgos;
    private final JTable tablaVerificacion;
    private final DefaultTableModel modeloVerificacion;
    private final JTabbedPane pestanasInferiores;
    private final JButton btnGuardar;
    private final JButton btnCopiar;
    private final JButton btnReemplazar;
    private final JLabel labelAcciones;
    private final BiConsumer<Integer, Integer> alSeleccionarUbicacion;
    private final Consumer<String> alReemplazarEditor;
    private final Supplier<String> nombreArchivoOriginal;

    private ResultadoOptimizacionFuente ultimoResultado;
    /** Líneas (del código optimizado) de cada fila de la tabla de verificación; 0 = sin ubicación. */
    private int[] lineasVerificacion = new int[0];

    public PanelOptimizacion() {
        this((l, c) -> {});
    }

    public PanelOptimizacion(BiConsumer<Integer, Integer> alSeleccionarUbicacion) {
        this(alSeleccionarUbicacion, null, null);
    }

    public PanelOptimizacion(BiConsumer<Integer, Integer> alSeleccionarUbicacion,
                             Consumer<String> alReemplazarEditor,
                             Supplier<String> nombreArchivoOriginal) {
        this.alSeleccionarUbicacion = alSeleccionarUbicacion != null ? alSeleccionarUbicacion : (l, c) -> {};
        this.alReemplazarEditor = alReemplazarEditor;
        this.nombreArchivoOriginal = nombreArchivoOriginal != null ? nombreArchivoOriginal : () -> null;

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

        // ── Barra de acciones ──
        JPanel acciones = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 4));
        acciones.setBackground(Colores.FONDO_PANEL);
        btnGuardar = crearBotonAccion("Guardar como…", "Guarda el código optimizado en un archivo .cs", e -> guardar());
        btnCopiar = crearBotonAccion("Copiar", "Copia el código optimizado al portapapeles", e -> copiar());
        btnReemplazar = crearBotonAccion("Reemplazar en editor",
                "Sustituye el código del editor por el optimizado", e -> reemplazarEnEditor());
        labelAcciones = new JLabel(" ");
        labelAcciones.setFont(Colores.FUENTE_PEQUENA);
        labelAcciones.setForeground(Colores.TEXTO_TENUE);
        acciones.add(btnGuardar);
        acciones.add(btnCopiar);
        acciones.add(btnReemplazar);
        acciones.add(labelAcciones);

        JPanel norte = new JPanel(new BorderLayout());
        norte.setBackground(Colores.FONDO_PANEL);
        norte.add(header, BorderLayout.NORTH);
        norte.add(acciones, BorderLayout.SOUTH);
        add(norte, BorderLayout.NORTH);

        // ── Split Vertical Principal: Arriba (Código Optimizado) | Abajo (Pestañas) ──
        JSplitPane splitPrincipal = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
        splitPrincipal.setDividerLocation(320);
        splitPrincipal.setDividerSize(5);
        Colores.estilizarSplit(splitPrincipal);

        areaDespues = crearAreaTexto();
        labelTituloCodigo = new JLabel(TITULO_CODIGO_OK);
        JPanel contenedorDespues = crearContenedorCodigo(labelTituloCodigo, Colores.ACENTO_OPTIMIZACION, areaDespues);
        splitPrincipal.setTopComponent(Colores.conMargen(contenedorDespues));

        // ── Pestaña: Eliminaciones ──
        modeloHallazgos = crearModelo(COL_HALLAZGOS);
        tablaHallazgos = crearTabla(modeloHallazgos);
        tablaHallazgos.getColumnModel().getColumn(0).setPreferredWidth(40);
        tablaHallazgos.getColumnModel().getColumn(1).setPreferredWidth(60);
        tablaHallazgos.getColumnModel().getColumn(2).setPreferredWidth(160);
        tablaHallazgos.getColumnModel().getColumn(3).setPreferredWidth(220);
        tablaHallazgos.getColumnModel().getColumn(4).setPreferredWidth(320);
        tablaHallazgos.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarHallazgo();
        });

        // ── Pestaña: Verificación ──
        modeloVerificacion = crearModelo(COL_VERIFICACION);
        tablaVerificacion = crearTabla(modeloVerificacion);
        tablaVerificacion.getColumnModel().getColumn(0).setPreferredWidth(170);
        tablaVerificacion.getColumnModel().getColumn(1).setPreferredWidth(130);
        tablaVerificacion.getColumnModel().getColumn(2).setPreferredWidth(360);
        tablaVerificacion.getColumnModel().getColumn(3).setPreferredWidth(260);
        tablaVerificacion.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) seleccionarErrorVerificacion();
        });

        pestanasInferiores = new JTabbedPane();
        pestanasInferiores.setBackground(Colores.FONDO_PANEL);
        pestanasInferiores.setForeground(Colores.TEXTO_NORMAL);
        pestanasInferiores.addTab("Eliminaciones", envolver(tablaHallazgos));
        pestanasInferiores.addTab("Verificación", envolver(tablaVerificacion));
        splitPrincipal.setBottomComponent(Colores.conMargen(pestanasInferiores));

        add(splitPrincipal, BorderLayout.CENTER);
        actualizarAcciones(null);
    }

    // ───────────────────────── Carga de resultados ─────────────────────────

    /**
     * Carga el resultado del pipeline de optimización.
     */
    public void cargar(ResultadoOptimizacionFuente resultado) {
        this.ultimoResultado = resultado;
        limpiarVistas();

        if (resultado == null) {
            labelEstado.setText("Sin análisis");
            labelEstado.setForeground(Colores.TEXTO_TENUE);
            actualizarAcciones(null);
            return;
        }

        if (resultado.omitida()) {
            labelEstado.setText("⚠ " + (resultado.motivoOmision() != null ? resultado.motivoOmision() : "Optimización omitida"));
            labelEstado.setForeground(Colores.ADVERTENCIA);
            areaDespues.setText("// Optimización omitida debido a errores en fases previas.");
            actualizarAcciones(resultado);
            return;
        }

        cargarVerificacion(resultado);

        if (resultado.rechazada()) {
            labelTituloCodigo.setText(TITULO_CODIGO_RECHAZADO);
            labelTituloCodigo.setForeground(Colores.ERROR);
            areaDespues.setText(resultado.codigoRechazado());
            areaDespues.setCaretPosition(0);
            labelEstado.setText(String.format("✗ Optimización NO aplicada: el código optimizado tendría %d error(es). Tu código original no se modificó.",
                    resultado.verificacion().totalErrores()));
            labelEstado.setForeground(Colores.ERROR);
            pestanasInferiores.setSelectedIndex(1);
            actualizarAcciones(resultado);
            return;
        }

        areaDespues.setText(resultado.codigoFinal());
        areaDespues.setCaretPosition(0);

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
        pestanasInferiores.setTitleAt(0, "Eliminaciones (" + hallazgos.size() + ")");

        if (!resultado.verificacion().valido()) {
            labelEstado.setText(String.format("✗ El código tiene %d error(es); no se puede guardar ni reemplazar. Revisa la pestaña Verificación.",
                    resultado.verificacion().totalErrores()));
            labelEstado.setForeground(Colores.ERROR);
            pestanasInferiores.setSelectedIndex(1);
        } else if (hallazgos.isEmpty()) {
            labelEstado.setText("✓ 0 código muerto detectado (código óptimo) · verificado sin errores");
            labelEstado.setForeground(Colores.EXITO);
        } else {
            labelEstado.setText(String.format("✓ %d eliminación(es) · verificado sin errores léxicos, sintácticos ni semánticos",
                    hallazgos.size()));
            labelEstado.setForeground(Colores.ACENTO_OPTIMIZACION);
            pestanasInferiores.setSelectedIndex(0);
            if (tablaHallazgos.getRowCount() > 0) {
                tablaHallazgos.setRowSelectionInterval(0, 0);
            }
        }
        if (!resultado.advertencias().isEmpty() && resultado.verificacion().valido()) {
            labelEstado.setText(labelEstado.getText() + " · ⚠ " + resultado.advertencias().size() + " advertencia(s)");
        }
        actualizarAcciones(resultado);
    }

    private void cargarVerificacion(ResultadoOptimizacionFuente resultado) {
        List<VerificacionOptimizado.ErrorVerificacion> errores = resultado.verificacion().errores();
        List<String> advertencias = resultado.advertencias();
        lineasVerificacion = new int[errores.size() + advertencias.size()];
        int fila = 0;

        for (VerificacionOptimizado.ErrorVerificacion e : errores) {
            modeloVerificacion.addRow(new Object[]{
                    "Error " + e.fase(),
                    e.linea() > 0 ? "Línea " + e.linea() + ", col. " + e.columna() : "—",
                    e.titulo() + ": " + e.mensaje(),
                    e.sugerencia()
            });
            lineasVerificacion[fila++] = e.linea();
        }
        for (String a : advertencias) {
            modeloVerificacion.addRow(new Object[]{"Advertencia del optimizador", "—", a, ""});
            lineasVerificacion[fila++] = 0;
        }
        if (errores.isEmpty() && advertencias.isEmpty()) {
            modeloVerificacion.addRow(new Object[]{
                    "✓ Sin problemas", "—",
                    "El código optimizado no tiene errores léxicos, sintácticos ni semánticos.", ""});
            lineasVerificacion = new int[]{0};
        }
        pestanasInferiores.setTitleAt(1, errores.isEmpty() && advertencias.isEmpty()
                ? "Verificación ✓"
                : "Verificación (" + errores.size() + " error(es), " + advertencias.size() + " advertencia(s))");
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
        actualizarAcciones(null);
    }

    private void limpiarVistas() {
        areaDespues.setText("");
        modeloHallazgos.setRowCount(0);
        modeloVerificacion.setRowCount(0);
        lineasVerificacion = new int[0];
        labelTituloCodigo.setText(TITULO_CODIGO_OK);
        labelTituloCodigo.setForeground(Colores.ACENTO_OPTIMIZACION);
        pestanasInferiores.setTitleAt(0, "Eliminaciones");
        pestanasInferiores.setTitleAt(1, "Verificación");
        labelAcciones.setText(" ");
    }

    // ───────────────────────── Acciones ─────────────────────────

    /** Habilita las acciones solo si hay un código optimizado válido y explica por qué si no. */
    private void actualizarAcciones(ResultadoOptimizacionFuente r) {
        boolean aplicable = r != null && r.aplicable();
        btnGuardar.setEnabled(aplicable);
        btnCopiar.setEnabled(aplicable);
        btnReemplazar.setEnabled(aplicable && alReemplazarEditor != null);

        String motivo;
        if (r == null) {
            motivo = "Analiza un código para habilitar estas opciones.";
        } else if (r.omitida()) {
            motivo = "Deshabilitado: la optimización se omitió por errores en el código.";
        } else if (r.rechazada()) {
            motivo = "Deshabilitado: el código optimizado no es válido (ver pestaña Verificación).";
        } else if (!r.verificacion().valido()) {
            motivo = "Deshabilitado: el código tiene errores (ver pestaña Verificación).";
        } else {
            motivo = " ";
        }
        labelAcciones.setText(motivo);
    }

    private String codigoOptimizado() {
        return ultimoResultado != null && ultimoResultado.aplicable() ? ultimoResultado.codigoFinal() : null;
    }

    private void guardar() {
        String codigo = codigoOptimizado();
        if (codigo == null) return;

        JFileChooser chooser = new JFileChooser(".");
        chooser.setDialogTitle("Guardar código optimizado");
        chooser.setFileFilter(new FileNameExtensionFilter("Archivos C# (*.cs)", "cs"));
        chooser.setSelectedFile(new File(GuardadorCodigo.sugerirNombre(nombreArchivoOriginal.get())));

        if (chooser.showSaveDialog(this) != JFileChooser.APPROVE_OPTION) return;
        Path destino = chooser.getSelectedFile().toPath();

        try {
            Path guardado;
            try {
                guardado = GuardadorCodigo.guardar(destino, codigo, false);
            } catch (FileAlreadyExistsException ex) {
                int r = JOptionPane.showConfirmDialog(this,
                        "El archivo ya existe:\n" + ex.getFile() + "\n¿Deseas sobrescribirlo?",
                        "Archivo existente", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
                if (r != JOptionPane.YES_OPTION) return;
                guardado = GuardadorCodigo.guardar(destino, codigo, true);
            }
            labelAcciones.setForeground(Colores.EXITO);
            labelAcciones.setText("✓ Guardado en " + guardado);
        } catch (IOException | IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this,
                    "No se pudo guardar el código optimizado:\n" + ex.getMessage(),
                    "Error al guardar", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void copiar() {
        String codigo = codigoOptimizado();
        if (codigo == null) return;
        Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(codigo), null);
        labelAcciones.setForeground(Colores.EXITO);
        labelAcciones.setText("✓ Código optimizado copiado al portapapeles");
    }

    private void reemplazarEnEditor() {
        String codigo = codigoOptimizado();
        if (codigo == null || alReemplazarEditor == null) return;
        int r = JOptionPane.showConfirmDialog(this,
                "Se reemplazará el contenido del editor por el código optimizado.\n"
                        + "El código original se perderá si no lo has guardado.\n¿Continuar?",
                "Reemplazar en editor", JOptionPane.YES_NO_OPTION, JOptionPane.WARNING_MESSAGE);
        if (r != JOptionPane.YES_OPTION) return;
        alReemplazarEditor.accept(codigo);
    }

    // ───────────────────────── Selección en tablas ─────────────────────────

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

    /** Resalta en el código optimizado la línea del error elegido en la tabla de verificación. */
    private void seleccionarErrorVerificacion() {
        int fila = tablaVerificacion.getSelectedRow();
        if (fila < 0 || fila >= lineasVerificacion.length) return;
        int linea = lineasVerificacion[fila];
        if (linea <= 0) return;
        try {
            int idx = Math.min(linea - 1, Math.max(0, areaDespues.getLineCount() - 1));
            int ini = areaDespues.getLineStartOffset(idx);
            int fin = areaDespues.getLineEndOffset(idx);
            areaDespues.requestFocusInWindow();
            areaDespues.select(ini, Math.max(ini, fin - 1));
        } catch (javax.swing.text.BadLocationException ignorada) {
            // línea fuera de rango: no hay nada que resaltar
        }
    }

    // ───────────────────────── Construcción de componentes ─────────────────────────

    private JButton crearBotonAccion(String texto, String tooltip, java.awt.event.ActionListener al) {
        JButton b = new JButton(texto);
        b.setFont(Colores.FUENTE_NORMAL);
        b.setToolTipText(tooltip);
        b.setFocusPainted(false);
        b.addActionListener(al);
        return b;
    }

    private DefaultTableModel crearModelo(String[] columnas) {
        return new DefaultTableModel(columnas, 0) {
            @Override
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        };
    }

    private JScrollPane envolver(JTable tabla) {
        JScrollPane scroll = new JScrollPane(tabla);
        scroll.setBorder(BorderFactory.createEmptyBorder());
        scroll.getViewport().setBackground(Colores.FONDO_PANEL);
        return scroll;
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

    private JPanel crearContenedorCodigo(JLabel lbl, Color colorAcento, JTextArea area) {
        JPanel p = new JPanel(new BorderLayout());
        p.setBackground(Colores.FONDO_PANEL);

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

                // Colorear el tipo (columna "Regla" en Eliminaciones, "Tipo" en Verificación)
                boolean esColumnaTipo = (table == tablaHallazgos && column == 2)
                        || (table == tablaVerificacion && column == 0);
                if (esColumnaTipo && value != null && !isSelected) {
                    String tipo = value.toString();
                    if (tipo.contains("Inalcanzable") || tipo.startsWith("Error")) {
                        c.setForeground(Colores.ERROR);
                    } else if (tipo.contains("Constante") || tipo.startsWith("Advertencia")) {
                        c.setForeground(Colores.ADVERTENCIA);
                    } else if (tipo.contains("Sin Uso")) {
                        c.setForeground(Colores.ACENTO_OPTIMIZACION);
                    } else if (tipo.startsWith("✓")) {
                        c.setForeground(Colores.EXITO);
                    }
                }
                return c;
            }
        });

        return tabla;
    }
}
