package com.compilador.gui;

import javax.swing.*;
import java.awt.BorderLayout;
import java.awt.Color;

/**
 * Panel de resumen que muestra estadísticas del análisis:
 * cantidad de tokens, errores por tipo, y estado general.
 */
public class PanelResumen extends JPanel {

    private final JLabel labelTokens;
    private final JLabel labelErrLex;
    private final JLabel labelErrSint;
    private final JLabel labelErrSem;
    private final JLabel labelEstado;
    private final JPanel barraEstado;

    // Ultimo analisis (para el resumen corto cuando el panel esta contraido)
    private boolean analizado = false;
    private int ultTokens;
    private int ultErrLex;
    private int ultErrSint;
    private int ultErrSem;
    private Runnable alCambiar;

    public PanelResumen() {
        setLayout(new BorderLayout(0, 8));
        setBackground(Colores.FONDO_PANEL);
        setBorder(BorderFactory.createEmptyBorder(8, 12, 8, 12));

        // ── Título ──
        JLabel titulo = new JLabel("Resumen del Análisis");
        titulo.setFont(Colores.FUENTE_TITULO);
        titulo.setForeground(Colores.TEXTO_NORMAL);
        add(titulo, BorderLayout.NORTH);

        // ── Panel de métricas ──
        JPanel metricas = new JPanel();
        metricas.setLayout(new BoxLayout(metricas, BoxLayout.Y_AXIS));
        metricas.setBackground(Colores.FONDO_PANEL);

        labelTokens = crearMetrica("Tokens:", "0", Colores.ACENTO_LEXICO);
        labelErrLex = crearMetrica("Errores Léxicos:", "0", Colores.ACENTO_LEXICO);
        labelErrSint = crearMetrica("Errores Sintácticos:", "0", Colores.ACENTO_SINTACTICO);
        labelErrSem = crearMetrica("Errores Semánticos:", "0", Colores.ACENTO_SEMANTICO);

        metricas.add(labelTokens);
        metricas.add(labelErrLex);
        metricas.add(labelErrSint);
        metricas.add(labelErrSem);
        add(metricas, BorderLayout.CENTER);

        // ── Barra de estado ──
        barraEstado = new JPanel(new BorderLayout());
        barraEstado.setBackground(Colores.FONDO_TOOLBAR);
        barraEstado.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Colores.BORDE),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));

        labelEstado = new JLabel("● Esperando análisis...");
        labelEstado.setFont(Colores.FUENTE_NORMAL);
        labelEstado.setForeground(Colores.TEXTO_TENUE);
        barraEstado.add(labelEstado, BorderLayout.CENTER);
        add(barraEstado, BorderLayout.SOUTH);
    }

    /**
     * Actualiza todas las métricas del resumen.
     */
    public void actualizar(int tokens, int errLex, int errSint, int errSem) {
        analizado = true;
        ultTokens = tokens;
        ultErrLex = errLex;
        ultErrSint = errSint;
        ultErrSem = errSem;
        actualizarVista(tokens, errLex, errSint, errSem);
        if (alCambiar != null) alCambiar.run();
    }

    private void actualizarVista(int tokens, int errLex, int errSint, int errSem) {
        labelTokens.setText(formatearMetrica("Tokens:", tokens, Colores.ACENTO_LEXICO));
        labelErrLex.setText(formatearMetrica("Errores Léxicos:", errLex,
                errLex > 0 ? Colores.ERROR : Colores.ACENTO_LEXICO));
        labelErrSint.setText(formatearMetrica("Errores Sintácticos:", errSint,
                errSint > 0 ? Colores.ERROR : Colores.ACENTO_SINTACTICO));
        labelErrSem.setText(formatearMetrica("Errores Semánticos:", errSem,
                errSem > 0 ? Colores.ERROR : Colores.ACENTO_SEMANTICO));

        int totalErrores = errLex + errSint + errSem;
        if (totalErrores == 0) {
            labelEstado.setText("✓ Análisis completado sin errores");
            labelEstado.setForeground(Colores.EXITO);
            barraEstado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, Colores.EXITO),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
        } else {
            labelEstado.setText("✗ " + totalErrores + " error" + (totalErrores > 1 ? "es" : "") + " encontrado" + (totalErrores > 1 ? "s" : ""));
            labelEstado.setForeground(Colores.ERROR);
            barraEstado.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(2, 0, 0, 0, Colores.ERROR),
                BorderFactory.createEmptyBorder(6, 8, 6, 8)
            ));
        }
    }

    /**
     * Restaura el panel a su estado inicial.
     */
    public void limpiar() {
        analizado = false;
        ultTokens = ultErrLex = ultErrSint = ultErrSem = 0;
        limpiarVista();
        if (alCambiar != null) alCambiar.run();
    }

    private void limpiarVista() {
        labelTokens.setText(formatearMetrica("Tokens:", 0, Colores.ACENTO_LEXICO));
        labelErrLex.setText(formatearMetrica("Errores Léxicos:", 0, Colores.ACENTO_LEXICO));
        labelErrSint.setText(formatearMetrica("Errores Sintácticos:", 0, Colores.ACENTO_SINTACTICO));
        labelErrSem.setText(formatearMetrica("Errores Semánticos:", 0, Colores.ACENTO_SEMANTICO));
        labelEstado.setText("● Esperando análisis...");
        labelEstado.setForeground(Colores.TEXTO_TENUE);
        barraEstado.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 0, 0, Colores.BORDE),
            BorderFactory.createEmptyBorder(6, 8, 6, 8)
        ));
    }

    // ── Resumen corto (para la barra contraible) ──

    /** Se invoca cada vez que cambian los datos del resumen. */
    public void setAlCambiar(Runnable alCambiar) {
        this.alCambiar = alCambiar;
    }

    public int totalErrores() {
        return ultErrLex + ultErrSint + ultErrSem;
    }

    /** Resumen de una línea: tokens, errores por fase y estado general. */
    public String textoCorto() {
        if (!analizado) return "Sin análisis";
        String base = ultTokens + " tokens · " + ultErrLex + " léxicos · "
                + ultErrSint + " sintácticos · " + ultErrSem + " semánticos";
        int total = totalErrores();
        return base + "   " + (total == 0 ? "✓ sin errores" : "✗ " + total + (total == 1 ? " error" : " errores"));
    }

    public Color colorEstado() {
        if (!analizado) return Colores.TEXTO_TENUE;
        return totalErrores() == 0 ? Colores.EXITO : Colores.ERROR;
    }

    // ── Helpers ──

    private JLabel crearMetrica(String nombre, String valor, Color color) {
        JLabel label = new JLabel(formatearMetrica(nombre, Integer.parseInt(valor), color));
        label.setFont(Colores.FUENTE_NORMAL);
        return label;
    }

    private String formatearMetrica(String nombre, int valor, Color color) {
        String hex = String.format("#%02x%02x%02x", color.getRed(), color.getGreen(), color.getBlue());
        return "<html><font color='#" + Colores.toHex(Colores.TEXTO_TENUE) + "'>" + nombre
                + "</font> <font color='" + hex + "'><b>" + valor + "</b></font></html>";
    }
}
