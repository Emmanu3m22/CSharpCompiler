package com.compilador.gui;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Pantalla de bienvenida minimalista, al estilo de un IDE: acciones de inicio,
 * archivos recientes, ejemplos y la opción de no volver a mostrarla.
 */
public class PantallaBienvenida extends JPanel {

    private final Runnable alNuevo;
    private final Runnable alAbrir;
    private final IntConsumer alElegirEjemplo;
    private final Consumer<File> alAbrirReciente;
    private final Runnable alContinuar;

    private final JPanel listaRecientes = new JPanel();

    public PantallaBienvenida(Runnable alNuevo, Runnable alAbrir, IntConsumer alElegirEjemplo,
                              Consumer<File> alAbrirReciente, Runnable alContinuar) {
        this.alNuevo = alNuevo;
        this.alAbrir = alAbrir;
        this.alElegirEjemplo = alElegirEjemplo;
        this.alAbrirReciente = alAbrirReciente;
        this.alContinuar = alContinuar;

        setLayout(new GridBagLayout());
        setBackground(Colores.FONDO_PRINCIPAL);

        JPanel contenido = new JPanel();
        contenido.setOpaque(false);
        contenido.setLayout(new BoxLayout(contenido, BoxLayout.Y_AXIS));
        contenido.setBorder(BorderFactory.createEmptyBorder(24, 24, 24, 24));

        // ── Encabezado ──
        JLabel marca = new JLabel("</>");
        marca.setFont(new Font("Consolas", Font.BOLD, 34));
        marca.setForeground(Colores.ACENTO_PRINCIPAL);
        marca.setAlignmentX(LEFT_ALIGNMENT);

        JLabel titulo = new JLabel("Compilador LenguajeCSharp");
        titulo.setFont(new Font("Segoe UI", Font.PLAIN, 28));
        titulo.setForeground(Colores.TEXTO_NORMAL);
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Léxico · Sintáctico · Semántico · Código intermedio · Optimización");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        subtitulo.setForeground(Colores.TEXTO_TENUE);
        subtitulo.setAlignmentX(LEFT_ALIGNMENT);

        contenido.add(marca);
        contenido.add(Box.createVerticalStrut(6));
        contenido.add(titulo);
        contenido.add(Box.createVerticalStrut(4));
        contenido.add(subtitulo);
        contenido.add(Box.createVerticalStrut(18));
        contenido.add(lineaAcento());
        contenido.add(Box.createVerticalStrut(22));

        // ── Columnas ──
        JPanel columnas = new JPanel(new GridLayout(1, 2, 60, 0));
        columnas.setOpaque(false);
        columnas.setAlignmentX(LEFT_ALIGNMENT);

        JPanel izquierda = columna();
        izquierda.add(encabezadoSeccion("Comenzar"));
        izquierda.add(enlace("Nuevo archivo", alNuevo));
        izquierda.add(enlace("Abrir archivo…", alAbrir));
        izquierda.add(enlace("Continuar en el editor", alContinuar));
        izquierda.add(Box.createVerticalStrut(22));
        izquierda.add(encabezadoSeccion("Recientes"));
        listaRecientes.setOpaque(false);
        listaRecientes.setLayout(new BoxLayout(listaRecientes, BoxLayout.Y_AXIS));
        listaRecientes.setAlignmentX(LEFT_ALIGNMENT);
        izquierda.add(listaRecientes);

        JPanel derecha = columna();
        derecha.add(encabezadoSeccion("Ejemplos"));
        for (int i = 0; i < Ejemplos.cantidad(); i++) {
            final int idx = i;
            derecha.add(enlace(Ejemplos.getNombre(i), () -> this.alElegirEjemplo.accept(idx)));
        }

        columnas.add(izquierda);
        columnas.add(derecha);
        contenido.add(columnas);
        contenido.add(Box.createVerticalStrut(26));

        // ── Pie ──
        JCheckBox chkMostrar = new JCheckBox("Mostrar esta pantalla al iniciar");
        chkMostrar.setOpaque(false);
        chkMostrar.setForeground(Colores.TEXTO_TENUE);
        chkMostrar.setFont(Colores.FUENTE_PEQUENA.deriveFont(12f));
        chkMostrar.setFocusPainted(false);
        chkMostrar.setAlignmentX(LEFT_ALIGNMENT);
        chkMostrar.setSelected(PreferenciasApp.mostrarBienvenida());
        chkMostrar.addActionListener(e -> PreferenciasApp.setMostrarBienvenida(chkMostrar.isSelected()));
        contenido.add(chkMostrar);

        add(contenido);
        actualizarRecientes();
    }

    /** Vuelve a leer los archivos recientes (se llama cada vez que se muestra la pantalla). */
    public void actualizarRecientes() {
        listaRecientes.removeAll();
        List<File> recientes = PreferenciasApp.recientes();
        if (recientes.isEmpty()) {
            JLabel vacio = new JLabel("Aún no hay archivos recientes");
            vacio.setFont(Colores.FUENTE_PEQUENA.deriveFont(12f));
            vacio.setForeground(Colores.TEXTO_LINEAS);
            vacio.setAlignmentX(LEFT_ALIGNMENT);
            vacio.setBorder(BorderFactory.createEmptyBorder(3, 0, 3, 0));
            listaRecientes.add(vacio);
        } else {
            for (File f : recientes) {
                JLabel l = enlace(f.getName(), () -> alAbrirReciente.accept(f));
                l.setToolTipText(f.getAbsolutePath());
                listaRecientes.add(l);
            }
        }
        listaRecientes.revalidate();
        listaRecientes.repaint();
    }

    // ───────────────────────── Componentes ─────────────────────────

    private JPanel columna() {
        JPanel p = new JPanel();
        p.setOpaque(false);
        p.setLayout(new BoxLayout(p, BoxLayout.Y_AXIS));
        return p;
    }

    private JComponent lineaAcento() {
        JPanel linea = new JPanel();
        linea.setBackground(Colores.ACENTO_PRINCIPAL);
        linea.setMaximumSize(new Dimension(48, 2));
        linea.setPreferredSize(new Dimension(48, 2));
        linea.setAlignmentX(LEFT_ALIGNMENT);
        return linea;
    }

    private JLabel encabezadoSeccion(String texto) {
        JLabel l = new JLabel(texto.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Colores.TEXTO_LINEAS);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 8, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }

    /** Texto con aspecto de enlace: resalta al pasar el cursor y ejecuta la acción al hacer clic. */
    private JLabel enlace(String texto, Runnable accion) {
        JLabel l = new JLabel(texto);
        l.setFont(new Font("Segoe UI", Font.PLAIN, 14));
        l.setForeground(Colores.TEXTO_NORMAL);
        l.setBorder(BorderFactory.createEmptyBorder(4, 0, 4, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        l.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        l.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                l.setForeground(Colores.ACENTO_PRINCIPAL);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                l.setForeground(Colores.TEXTO_NORMAL);
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) accion.run();
            }
        });
        return l;
    }
}
