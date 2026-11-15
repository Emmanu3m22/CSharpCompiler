package com.compilador.gui;

import javax.swing.*;
import javax.swing.border.Border;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.IntConsumer;

/**
 * Pantalla de bienvenida con disposición lateral asimétrica estilo IDE moderno:
 * barra lateral izquierda con identidad, acciones primarias y preferencias,
 * y panel principal alineado a la izquierda con archivos recientes y plantillas.
 */
public class PantallaBienvenida extends JPanel {

    private static final int ANCHO_LATERAL = 300;
    private static final int ANCHO_MAX_CONTENIDO = 780;

    private static final String[] DESCRIPCIONES_EJEMPLOS = {
        "Precedencia mixta, paréntesis anidados y generación de cuádruplos",
        "Declaración de cadena e impresión básica por consola",
        "Tipos numéricos, aritmética básica y salida estándar",
        "Expresiones con booleanos, decimales y cadenas con formato",
        "Casos con redeclaraciones y variables no declaradas para diagnóstico",
        "Estructura mínima sin sentencias ejecutables",
        "Pruebas de reglas R1-R4: código inalcanzable, condiciones constantes y variables sin uso"
    };

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

        setLayout(new BorderLayout(0, 0));
        setBackground(Colores.FONDO_PRINCIPAL);

        add(crearPanelLateral(), BorderLayout.WEST);
        add(crearPanelPrincipal(), BorderLayout.CENTER);

        actualizarRecientes();
    }

    // ───────────────────────── Panel Lateral Izquierdo ─────────────────────────

    private JPanel crearPanelLateral() {
        JPanel lateral = new JPanel(new BorderLayout(0, 20));
        lateral.setName("panel-lateral-bienvenida");
        lateral.setBackground(Colores.FONDO_PANEL);
        lateral.setPreferredSize(new Dimension(ANCHO_LATERAL, 0));
        lateral.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, Colores.BORDE),
            BorderFactory.createEmptyBorder(32, 26, 24, 26)
        ));

        JPanel superior = new JPanel();
        superior.setOpaque(false);
        superior.setLayout(new BoxLayout(superior, BoxLayout.Y_AXIS));

        // ── Identidad de la aplicación ──
        JPanel filaMarca = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaMarca.setOpaque(false);
        filaMarca.setAlignmentX(LEFT_ALIGNMENT);

        JLabel iconoMarca = new JLabel("</>");
        iconoMarca.setFont(new Font("Consolas", Font.BOLD, 24));
        iconoMarca.setForeground(Colores.ACENTO_PRINCIPAL);

        JLabel insigniaVersion = new JLabel("  IDE · C#");
        insigniaVersion.setFont(new Font("Consolas", Font.PLAIN, 11));
        insigniaVersion.setForeground(Colores.TEXTO_LINEAS);

        filaMarca.add(iconoMarca);
        filaMarca.add(insigniaVersion);

        JLabel titulo = new JLabel("Compilador C#");
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(Colores.TEXTO_NORMAL);
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        JLabel subtitulo = new JLabel("Léxico · Sintáctico · Semántico · TAC · Optimización");
        subtitulo.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitulo.setForeground(Colores.TEXTO_TENUE);
        subtitulo.setAlignmentX(LEFT_ALIGNMENT);

        superior.add(filaMarca);
        superior.add(Box.createVerticalStrut(8));
        superior.add(titulo);
        superior.add(Box.createVerticalStrut(4));
        superior.add(subtitulo);
        superior.add(Box.createVerticalStrut(16));
        superior.add(lineaAcento());
        superior.add(Box.createVerticalStrut(28));

        // ── Acciones principales ──
        superior.add(encabezadoSeccion("Comenzar"));
        superior.add(Box.createVerticalStrut(4));
        superior.add(botonAccionLateral("+", "Nuevo archivo", "Crear código C# en blanco", "accion-nuevo", alNuevo));
        superior.add(Box.createVerticalStrut(8));
        superior.add(botonAccionLateral("↗", "Abrir archivo…", "Cargar archivo .cs o .txt", "accion-abrir", alAbrir));
        superior.add(Box.createVerticalStrut(8));
        superior.add(botonAccionLateral("▸", "Ir al editor", "Continuar en el área de trabajo", "accion-continuar", alContinuar));

        lateral.add(superior, BorderLayout.NORTH);

        // ── Pie del panel lateral ──
        JPanel pie = new JPanel();
        pie.setOpaque(false);
        pie.setLayout(new BoxLayout(pie, BoxLayout.Y_AXIS));

        JLabel atajos = new JLabel("F5 Analizar  ·  Ctrl+B Paneles  ·  Ctrl+J Resumen");
        atajos.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        atajos.setForeground(Colores.TEXTO_LINEAS);
        atajos.setAlignmentX(LEFT_ALIGNMENT);

        JCheckBox chkMostrar = new JCheckBox("Mostrar bienvenida al iniciar");
        chkMostrar.setName("chk-mostrar-bienvenida");
        chkMostrar.setOpaque(false);
        chkMostrar.setForeground(Colores.TEXTO_TENUE);
        chkMostrar.setFont(Colores.FUENTE_PEQUENA.deriveFont(12f));
        chkMostrar.setFocusPainted(false);
        chkMostrar.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 0));
        chkMostrar.setAlignmentX(LEFT_ALIGNMENT);
        chkMostrar.setSelected(PreferenciasApp.mostrarBienvenida());
        chkMostrar.addActionListener(e -> PreferenciasApp.setMostrarBienvenida(chkMostrar.isSelected()));

        pie.add(atajos);
        pie.add(Box.createVerticalStrut(10));
        pie.add(chkMostrar);

        lateral.add(pie, BorderLayout.SOUTH);
        return lateral;
    }

    // ───────────────────────── Panel Principal (Alineado a la Izquierda) ─────────────────────────

    private JPanel crearPanelPrincipal() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setName("panel-principal-bienvenida");
        contenedor.setBackground(Colores.FONDO_PRINCIPAL);
        contenedor.setBorder(BorderFactory.createEmptyBorder(34, 44, 34, 44));

        JPanel columnaIzquierda = new JPanel();
        columnaIzquierda.setOpaque(false);
        columnaIzquierda.setLayout(new BoxLayout(columnaIzquierda, BoxLayout.Y_AXIS));
        columnaIzquierda.setAlignmentX(LEFT_ALIGNMENT);

        // ── Sección: Archivos recientes ──
        columnaIzquierda.add(encabezadoSeccion("Archivos recientes"));
        columnaIzquierda.add(Box.createVerticalStrut(4));

        listaRecientes.setName("lista-recientes");
        listaRecientes.setOpaque(false);
        listaRecientes.setLayout(new BoxLayout(listaRecientes, BoxLayout.Y_AXIS));
        listaRecientes.setAlignmentX(LEFT_ALIGNMENT);
        columnaIzquierda.add(listaRecientes);

        columnaIzquierda.add(Box.createVerticalStrut(28));

        // ── Sección: Plantillas y ejemplos ──
        columnaIzquierda.add(encabezadoSeccion("Plantillas y ejemplos incluidos"));
        JLabel descEjemplos = new JLabel("Abre un caso de prueba preconfigurado directamente en el editor:");
        descEjemplos.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descEjemplos.setForeground(Colores.TEXTO_TENUE);
        descEjemplos.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 0));
        descEjemplos.setAlignmentX(LEFT_ALIGNMENT);
        columnaIzquierda.add(descEjemplos);

        JPanel rejillaEjemplos = new JPanel(new GridLayout(0, 2, 12, 10));
        rejillaEjemplos.setName("grid-ejemplos");
        rejillaEjemplos.setOpaque(false);
        rejillaEjemplos.setAlignmentX(LEFT_ALIGNMENT);
        rejillaEjemplos.setMaximumSize(new Dimension(ANCHO_MAX_CONTENIDO, 340));

        for (int i = 0; i < Ejemplos.cantidad(); i++) {
            final int idx = i;
            String nombre = Ejemplos.getNombre(i);
            String desc = i < DESCRIPCIONES_EJEMPLOS.length ? DESCRIPCIONES_EJEMPLOS[i] : "Ejemplo de código C#";
            rejillaEjemplos.add(tarjetaEjemplo(idx, nombre, desc));
        }

        columnaIzquierda.add(rejillaEjemplos);

        // Anclar arriba y a la izquierda (NORTHWEST) para evitar centrado flotante
        JPanel anclaNoroeste = new JPanel(new BorderLayout());
        anclaNoroeste.setOpaque(false);
        anclaNoroeste.add(columnaIzquierda, BorderLayout.WEST);

        contenedor.add(anclaNoroeste, BorderLayout.NORTH);
        return contenedor;
    }

    /** Vuelve a leer los archivos recientes (se llama cada vez que se muestra la pantalla). */
    public void actualizarRecientes() {
        listaRecientes.removeAll();
        List<File> recientes = PreferenciasApp.recientes();
        if (recientes.isEmpty()) {
            JPanel estadoVacio = new JPanel(new BorderLayout());
            estadoVacio.setName("recientes-vacio");
            estadoVacio.setBackground(Colores.FONDO_EDITOR);
            estadoVacio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(Colores.BORDE, 1),
                BorderFactory.createEmptyBorder(10, 14, 10, 14)
            ));
            estadoVacio.setAlignmentX(LEFT_ALIGNMENT);
            estadoVacio.setMaximumSize(new Dimension(ANCHO_MAX_CONTENIDO, 40));

            JLabel vacio = new JLabel("Aún no hay archivos recientes. Usa «Abrir archivo…» o elige una plantilla.");
            vacio.setFont(Colores.FUENTE_PEQUENA.deriveFont(12f));
            vacio.setForeground(Colores.TEXTO_LINEAS);
            estadoVacio.add(vacio, BorderLayout.WEST);

            listaRecientes.add(estadoVacio);
        } else {
            for (int i = 0; i < recientes.size(); i++) {
                File f = recientes.get(i);
                if (i > 0) {
                    listaRecientes.add(Box.createVerticalStrut(6));
                }
                listaRecientes.add(filaReciente(f));
            }
        }
        listaRecientes.revalidate();
        listaRecientes.repaint();
    }

    // ───────────────────────── Componentes Interactivos ─────────────────────────

    private JPanel botonAccionLateral(String simbolo, String titulo, String subtitulo,
                                      String nombreComponente, Runnable accion) {
        JPanel tarjeta = new JPanel(new BorderLayout(12, 0));
        tarjeta.setName(nombreComponente);
        tarjeta.setBackground(Colores.FONDO_EDITOR);
        tarjeta.setAlignmentX(LEFT_ALIGNMENT);
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 50));
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Border bordeNormal = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
        Border bordeHover = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colores.BORDE_ENFOCADO, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
        tarjeta.setBorder(bordeNormal);

        JLabel lblSimbolo = new JLabel(simbolo);
        lblSimbolo.setFont(new Font("Consolas", Font.BOLD, 16));
        lblSimbolo.setForeground(Colores.ACENTO_PRINCIPAL);

        JPanel textos = new JPanel();
        textos.setOpaque(false);
        textos.setLayout(new BoxLayout(textos, BoxLayout.Y_AXIS));

        JLabel lblTitulo = new JLabel(titulo);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setForeground(Colores.TEXTO_NORMAL);

        JLabel lblSub = new JLabel(subtitulo);
        lblSub.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblSub.setForeground(Colores.TEXTO_TENUE);

        textos.add(lblTitulo);
        textos.add(lblSub);

        tarjeta.add(lblSimbolo, BorderLayout.WEST);
        tarjeta.add(textos, BorderLayout.CENTER);

        instalarInteraccionTarjeta(tarjeta, bordeNormal, bordeHover, accion);
        return tarjeta;
    }

    private JPanel filaReciente(File archivo) {
        JPanel fila = new JPanel(new BorderLayout(16, 0));
        fila.setName("reciente-" + archivo.getName());
        fila.setBackground(Colores.FONDO_EDITOR);
        fila.setAlignmentX(LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(ANCHO_MAX_CONTENIDO, 38));
        fila.setPreferredSize(new Dimension(ANCHO_MAX_CONTENIDO, 38));
        fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fila.setToolTipText(archivo.getAbsolutePath());

        Border bordeNormal = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        );
        Border bordeHover = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colores.BORDE_ENFOCADO, 1),
            BorderFactory.createEmptyBorder(6, 12, 6, 12)
        );
        fila.setBorder(bordeNormal);

        JLabel lblNombre = new JLabel(archivo.getName());
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNombre.setForeground(Colores.TEXTO_NORMAL);

        String carpeta = archivo.getParent() != null ? archivo.getParent() : archivo.getAbsolutePath();
        JLabel lblRuta = new JLabel(carpeta);
        lblRuta.setFont(new Font("Consolas", Font.PLAIN, 11));
        lblRuta.setForeground(Colores.TEXTO_LINEAS);

        fila.add(lblNombre, BorderLayout.WEST);
        fila.add(lblRuta, BorderLayout.EAST);

        instalarInteraccionTarjeta(fila, bordeNormal, bordeHover, () -> alAbrirReciente.accept(archivo));
        return fila;
    }

    private JPanel tarjetaEjemplo(int indice, String nombre, String descripcion) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 4));
        tarjeta.setName("ejemplo-" + indice);
        tarjeta.setBackground(Colores.FONDO_EDITOR);
        tarjeta.setPreferredSize(new Dimension(370, 58));
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        Border bordeNormal = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
        Border bordeHover = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colores.BORDE_ENFOCADO, 1),
            BorderFactory.createEmptyBorder(8, 12, 8, 12)
        );
        tarjeta.setBorder(bordeNormal);

        JPanel encabezado = new JPanel(new BorderLayout(8, 0));
        encabezado.setOpaque(false);

        JLabel lblTitulo = new JLabel(nombre);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setForeground(Colores.TEXTO_NORMAL);

        JLabel lblFlecha = new JLabel("→");
        lblFlecha.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        lblFlecha.setForeground(Colores.ACENTO_PRINCIPAL);

        encabezado.add(lblTitulo, BorderLayout.WEST);
        encabezado.add(lblFlecha, BorderLayout.EAST);

        JLabel lblDesc = new JLabel(descripcion);
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(Colores.TEXTO_TENUE);

        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(lblDesc, BorderLayout.CENTER);

        instalarInteraccionTarjeta(tarjeta, bordeNormal, bordeHover, () -> alElegirEjemplo.accept(indice));
        return tarjeta;
    }

    private void instalarInteraccionTarjeta(JPanel tarjeta, Border bordeNormal, Border bordeHover, Runnable accion) {
        MouseAdapter adaptador = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tarjeta.setBackground(Colores.FONDO_SELECCION);
                tarjeta.setBorder(bordeHover);
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), tarjeta);
                if (!tarjeta.contains(p)) {
                    tarjeta.setBackground(Colores.FONDO_EDITOR);
                    tarjeta.setBorder(bordeNormal);
                }
            }

            @Override
            public void mouseClicked(MouseEvent e) {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    accion.run();
                }
            }
        };
        registrarMouseRecursivo(tarjeta, adaptador);
    }

    private void registrarMouseRecursivo(Component c, MouseAdapter adaptador) {
        c.addMouseListener(adaptador);
        if (c instanceof Container cont) {
            for (Component hijo : cont.getComponents()) {
                registrarMouseRecursivo(hijo, adaptador);
            }
        }
    }

    private JComponent lineaAcento() {
        JPanel linea = new JPanel();
        linea.setBackground(Colores.ACENTO_PRINCIPAL);
        linea.setMaximumSize(new Dimension(44, 2));
        linea.setPreferredSize(new Dimension(44, 2));
        linea.setAlignmentX(LEFT_ALIGNMENT);
        return linea;
    }

    private JLabel encabezadoSeccion(String texto) {
        JLabel l = new JLabel(texto.toUpperCase());
        l.setFont(new Font("Segoe UI", Font.BOLD, 11));
        l.setForeground(Colores.TEXTO_LINEAS);
        l.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));
        l.setAlignmentX(LEFT_ALIGNMENT);
        return l;
    }
}
