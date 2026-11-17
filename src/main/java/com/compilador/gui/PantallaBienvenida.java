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
 * barra lateral izquierda con identidad multicolor por fase, acciones primarias
 * con acento propio y preferencias; y panel principal alineado a la izquierda
 * con archivos recientes y tarjetas de plantillas categorizadas por color.
 */
public class PantallaBienvenida extends JPanel {

    private static final int ANCHO_LATERAL = 300;
    private static final int ANCHO_MAX_CONTENIDO = 780;

    private record ParBordes(Border normal, Border hover) {}

    private record MetaEjemplo(String categoria, String descripcion, Color acento) {}

    private static final MetaEjemplo[] METADATOS_EJEMPLOS = {
        new MetaEjemplo("TAC", "Precedencia mixta, paréntesis anidados y generación de cuádruplos", Colores.ACENTO_INTERMEDIO),
        new MetaEjemplo("Básico", "Declaración de cadena e impresión básica por consola", Colores.ACENTO_SINTACTICO),
        new MetaEjemplo("Aritmética", "Tipos numéricos, aritmética básica y salida estándar", Colores.ACENTO_LEXICO),
        new MetaEjemplo("Tipos", "Expresiones con booleanos, decimales y cadenas con formato", Colores.ACENTO_SEMANTICO),
        new MetaEjemplo("Diagnóstico", "Casos con redeclaraciones y variables no declaradas para diagnóstico", Colores.ACENTO_CORAL),
        new MetaEjemplo("Base", "Estructura mínima sin sentencias ejecutables", Colores.ACENTO_CYAN),
        new MetaEjemplo("Optimización", "Pruebas R1-R4: código inalcanzable, condiciones constantes y variables sin uso", Colores.ACENTO_OPTIMIZACION)
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
        JPanel lateral = new JPanel(new BorderLayout(0, 18));
        lateral.setName("panel-lateral-bienvenida");
        lateral.setBackground(Colores.FONDO_BIENVENIDA_LATERAL);
        lateral.setPreferredSize(new Dimension(ANCHO_LATERAL, 0));
        lateral.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 1, Colores.BORDE),
            BorderFactory.createEmptyBorder(28, 24, 22, 24)
        ));

        JPanel superior = new JPanel();
        superior.setOpaque(false);
        superior.setLayout(new BoxLayout(superior, BoxLayout.Y_AXIS));

        // ── Identidad de la aplicación ──
        JPanel filaMarca = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
        filaMarca.setOpaque(false);
        filaMarca.setAlignmentX(CENTER_ALIGNMENT);

        JLabel iconoMarca = new JLabel(IconoAplicacion.cargar(64));

        // JLabel insigniaVersion = new JLabel(" C# · V1.0");
        // insigniaVersion.setFont(new Font("Consolas", Font.BOLD, 12));
        // insigniaVersion.setForeground(Colores.ACENTO_CYAN);

        filaMarca.add(iconoMarca);
        // filaMarca.add(insigniaVersion);

        JLabel titulo = new JLabel(AplicacionPrincipal.TITULO_APLICACION);
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titulo.setForeground(Colores.TEXTO_NORMAL);
        titulo.setAlignmentX(LEFT_ALIGNMENT);

        superior.add(filaMarca);
        superior.add(Box.createVerticalStrut(6));
        superior.add(titulo);
        superior.add(Box.createVerticalStrut(10));
        superior.add(crearPildorasFases());
        superior.add(Box.createVerticalStrut(12));
        superior.add(lineaAcentoMulticolor());
        superior.add(Box.createVerticalStrut(24));

        // ── Acciones principales ──
        superior.add(encabezadoSeccion("Comenzar", Colores.ACENTO_PRINCIPAL));
        superior.add(Box.createVerticalStrut(4));
        superior.add(botonAccionLateral("+", "Nuevo archivo", "Crear código C# en blanco",
                "accion-nuevo", Colores.ACENTO_SINTACTICO, alNuevo));
        superior.add(Box.createVerticalStrut(8));
        superior.add(botonAccionLateral("↗", "Abrir archivo…", "Cargar archivo .cs o .txt",
                "accion-abrir", Colores.ACENTO_INTERMEDIO, alAbrir));
        superior.add(Box.createVerticalStrut(8));
        superior.add(botonAccionLateral("▸", "Ir al editor", "Continuar en el área de trabajo",
                "accion-continuar", Colores.ACENTO_CYAN, alContinuar));

        lateral.add(superior, BorderLayout.NORTH);

        // ── Pie del panel lateral ──
        JPanel pie = new JPanel();
        pie.setOpaque(false);
        pie.setLayout(new BoxLayout(pie, BoxLayout.Y_AXIS));

        JPanel filaAtajos1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filaAtajos1.setOpaque(false);
        filaAtajos1.setAlignmentX(LEFT_ALIGNMENT);
        filaAtajos1.add(pildora("F5 · Analizar código", Colores.ACENTO_SINTACTICO));

        JPanel filaAtajos2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        filaAtajos2.setOpaque(false);
        filaAtajos2.setAlignmentX(LEFT_ALIGNMENT);
        filaAtajos2.add(pildora("Ctrl+B Paneles", Colores.ACENTO_LEXICO));
        filaAtajos2.add(pildora("Ctrl+J Resumen", Colores.ACENTO_SEMANTICO));

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

        pie.add(filaAtajos1);
        pie.add(Box.createVerticalStrut(5));
        pie.add(filaAtajos2);
        pie.add(Box.createVerticalStrut(12));
        pie.add(chkMostrar);

        lateral.add(pie, BorderLayout.SOUTH);
        return lateral;
    }

    private JPanel crearPildorasFases() {
        JPanel contenedor = new JPanel();
        contenedor.setOpaque(false);
        contenedor.setLayout(new BoxLayout(contenedor, BoxLayout.Y_AXIS));
        contenedor.setAlignmentX(LEFT_ALIGNMENT);

        JPanel fila1 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        fila1.setOpaque(false);
        fila1.setAlignmentX(LEFT_ALIGNMENT);
        fila1.add(pildora("Léxico", Colores.ACENTO_LEXICO));
        fila1.add(pildora("Sintáctico", Colores.ACENTO_SINTACTICO));
        fila1.add(pildora("Semántico", Colores.ACENTO_SEMANTICO));

        JPanel fila2 = new JPanel(new FlowLayout(FlowLayout.LEFT, 5, 0));
        fila2.setOpaque(false);
        fila2.setAlignmentX(LEFT_ALIGNMENT);
        fila2.add(pildora("TAC", Colores.ACENTO_INTERMEDIO));
        fila2.add(pildora("Optimización", Colores.ACENTO_OPTIMIZACION));

        contenedor.add(fila1);
        contenedor.add(Box.createVerticalStrut(5));
        contenedor.add(fila2);
        return contenedor;
    }

    private JLabel pildora(String texto, Color acento) {
        JLabel l = new JLabel(texto);
        l.setOpaque(true);
        l.setBackground(Colores.FONDO_INSIGNIA);
        l.setForeground(acento);
        l.setFont(new Font("Segoe UI", Font.BOLD, 10));
        l.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(2, 7, 2, 7)
        ));
        return l;
    }

    // ───────────────────────── Panel Principal (Alineado a la Izquierda) ─────────────────────────

    private JPanel crearPanelPrincipal() {
        JPanel contenedor = new JPanel(new BorderLayout());
        contenedor.setName("panel-principal-bienvenida");
        contenedor.setBackground(Colores.FONDO_PRINCIPAL);
        contenedor.setBorder(BorderFactory.createEmptyBorder(32, 44, 32, 44));

        JPanel columnaIzquierda = new JPanel();
        columnaIzquierda.setOpaque(false);
        columnaIzquierda.setLayout(new BoxLayout(columnaIzquierda, BoxLayout.Y_AXIS));
        columnaIzquierda.setAlignmentX(LEFT_ALIGNMENT);

        // ── Sección: Archivos recientes ──
        columnaIzquierda.add(encabezadoSeccion("Archivos recientes", Colores.ACENTO_CYAN));
        columnaIzquierda.add(Box.createVerticalStrut(4));

        listaRecientes.setName("lista-recientes");
        listaRecientes.setOpaque(false);
        listaRecientes.setLayout(new BoxLayout(listaRecientes, BoxLayout.Y_AXIS));
        listaRecientes.setAlignmentX(LEFT_ALIGNMENT);
        columnaIzquierda.add(listaRecientes);

        columnaIzquierda.add(Box.createVerticalStrut(26));

        // ── Sección: Plantillas y ejemplos ──
        columnaIzquierda.add(encabezadoSeccion("Plantillas y ejemplos incluidos", Colores.ACENTO_SEMANTICO));
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
        rejillaEjemplos.setMaximumSize(new Dimension(ANCHO_MAX_CONTENIDO, 400));

        for (int i = 0; i < Ejemplos.cantidad(); i++) {
            MetaEjemplo meta = i < METADATOS_EJEMPLOS.length
                    ? METADATOS_EJEMPLOS[i]
                    : new MetaEjemplo("Ejemplo", "Ejemplo de código C#", Colores.ACENTO_PRINCIPAL);
            rejillaEjemplos.add(tarjetaEjemplo(i, Ejemplos.getNombre(i), meta));
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
            estadoVacio.setBackground(Colores.FONDO_TARJETA);
            estadoVacio.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(1, 0, 1, 1, Colores.BORDE),
                BorderFactory.createCompoundBorder(
                    BorderFactory.createMatteBorder(0, 3, 0, 0, Colores.ACENTO_CYAN),
                    BorderFactory.createEmptyBorder(10, 12, 10, 14)
                )
            ));
            estadoVacio.setAlignmentX(LEFT_ALIGNMENT);
            estadoVacio.setMaximumSize(new Dimension(ANCHO_MAX_CONTENIDO, 42));

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
                                      String nombreComponente, Color acento, Runnable accion) {
        JPanel tarjeta = new JPanel(new BorderLayout(12, 0));
        tarjeta.setName(nombreComponente);
        tarjeta.setBackground(Colores.FONDO_TARJETA);
        tarjeta.setAlignmentX(LEFT_ALIGNMENT);
        tarjeta.setMaximumSize(new Dimension(Integer.MAX_VALUE, 52));
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ParBordes bordes = crearBordesTarjeta(acento, 8, 11, 12);
        tarjeta.setBorder(bordes.normal());

        JLabel lblSimbolo = new JLabel(simbolo);
        lblSimbolo.setName(nombreComponente + "-icono");
        lblSimbolo.setFont(new Font("Consolas", Font.BOLD, 16));
        lblSimbolo.setForeground(acento);

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

        instalarInteraccionTarjeta(tarjeta, bordes, accion);
        return tarjeta;
    }

    private JPanel filaReciente(File archivo) {
        JPanel fila = new JPanel(new BorderLayout(12, 0));
        fila.setName("reciente-" + archivo.getName());
        fila.setBackground(Colores.FONDO_TARJETA);
        fila.setAlignmentX(LEFT_ALIGNMENT);
        fila.setMaximumSize(new Dimension(ANCHO_MAX_CONTENIDO, 38));
        fila.setPreferredSize(new Dimension(ANCHO_MAX_CONTENIDO, 38));
        fila.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        fila.setToolTipText(archivo.getAbsolutePath());

        ParBordes bordes = crearBordesTarjeta(Colores.ACENTO_CYAN, 6, 11, 12);
        fila.setBorder(bordes.normal());

        JPanel izquierda = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 0));
        izquierda.setOpaque(false);
        izquierda.add(pildora("CS", Colores.ACENTO_CYAN));

        JLabel lblNombre = new JLabel(archivo.getName());
        lblNombre.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblNombre.setForeground(Colores.TEXTO_NORMAL);
        izquierda.add(lblNombre);

        String carpeta = archivo.getParent() != null ? archivo.getParent() : archivo.getAbsolutePath();
        JLabel lblRuta = new JLabel(carpeta, SwingConstants.RIGHT);
        lblRuta.setFont(new Font("Consolas", Font.PLAIN, 11));
        lblRuta.setForeground(Colores.TEXTO_LINEAS);

        fila.add(izquierda, BorderLayout.WEST);
        fila.add(lblRuta, BorderLayout.CENTER);

        instalarInteraccionTarjeta(fila, bordes, () -> alAbrirReciente.accept(archivo));
        return fila;
    }

    private JPanel tarjetaEjemplo(int indice, String nombre, MetaEjemplo meta) {
        JPanel tarjeta = new JPanel(new BorderLayout(0, 5));
        tarjeta.setName("ejemplo-" + indice);
        tarjeta.setBackground(Colores.FONDO_TARJETA);
        tarjeta.setPreferredSize(new Dimension(380, 74));
        tarjeta.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        ParBordes bordes = crearBordesTarjeta(meta.acento(), 8, 12, 12);
        tarjeta.setBorder(bordes.normal());

        JPanel encabezado = new JPanel(new BorderLayout(8, 0));
        encabezado.setOpaque(false);

        JLabel lblTitulo = new JLabel(nombre);
        lblTitulo.setFont(new Font("Segoe UI", Font.BOLD, 13));
        lblTitulo.setForeground(Colores.TEXTO_NORMAL);

        JLabel lblCategoria = pildora(meta.categoria(), meta.acento());
        lblCategoria.setName("ejemplo-" + indice + "-categoria");

        encabezado.add(lblTitulo, BorderLayout.WEST);
        encabezado.add(lblCategoria, BorderLayout.EAST);

        JLabel lblDesc = new JLabel("<html><div style='width:270px'>" + meta.descripcion() + "</div></html>");
        lblDesc.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        lblDesc.setForeground(Colores.TEXTO_TENUE);

        tarjeta.add(encabezado, BorderLayout.NORTH);
        tarjeta.add(lblDesc, BorderLayout.CENTER);

        instalarInteraccionTarjeta(tarjeta, bordes, () -> alElegirEjemplo.accept(indice));
        return tarjeta;
    }

    private ParBordes crearBordesTarjeta(Color acento, int padV, int padIzq, int padDer) {
        Border normal = BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(1, 0, 1, 1, Colores.BORDE),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 3, 0, 0, acento),
                BorderFactory.createEmptyBorder(padV, padIzq, padV, padDer)
            )
        );
        Border hover = BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(acento, 1),
            BorderFactory.createCompoundBorder(
                BorderFactory.createMatteBorder(0, 2, 0, 0, acento),
                BorderFactory.createEmptyBorder(padV, padIzq, padV, padDer)
            )
        );
        return new ParBordes(normal, hover);
    }

    private void instalarInteraccionTarjeta(JPanel tarjeta, ParBordes bordes, Runnable accion) {
        MouseAdapter adaptador = new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                tarjeta.setBackground(Colores.FONDO_TARJETA_HOVER);
                tarjeta.setBorder(bordes.hover());
            }

            @Override
            public void mouseExited(MouseEvent e) {
                Point p = SwingUtilities.convertPoint(e.getComponent(), e.getPoint(), tarjeta);
                if (!tarjeta.contains(p)) {
                    tarjeta.setBackground(Colores.FONDO_TARJETA);
                    tarjeta.setBorder(bordes.normal());
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

    private JComponent lineaAcentoMulticolor() {
        Color[] segmentos = {
            Colores.ACENTO_LEXICO,
            Colores.ACENTO_SINTACTICO,
            Colores.ACENTO_SEMANTICO,
            Colores.ACENTO_INTERMEDIO,
            Colores.ACENTO_OPTIMIZACION
        };
        JPanel barra = new JPanel(new GridLayout(1, segmentos.length, 3, 0));
        barra.setOpaque(false);
        barra.setAlignmentX(LEFT_ALIGNMENT);
        barra.setMaximumSize(new Dimension(160, 3));
        barra.setPreferredSize(new Dimension(160, 3));
        for (Color c : segmentos) {
            JPanel seg = new JPanel();
            seg.setBackground(c);
            barra.add(seg);
        }
        return barra;
    }

    private JPanel encabezadoSeccion(String texto, Color acento) {
        JPanel fila = new JPanel(new FlowLayout(FlowLayout.LEFT, 6, 0));
        fila.setOpaque(false);
        fila.setAlignmentX(LEFT_ALIGNMENT);
        fila.setBorder(BorderFactory.createEmptyBorder(0, 0, 6, 0));

        JLabel punto = new JLabel("●");
        punto.setFont(new Font("Segoe UI", Font.PLAIN, 9));
        punto.setForeground(acento);

        JLabel titulo = new JLabel(texto.toUpperCase());
        titulo.setFont(new Font("Segoe UI", Font.BOLD, 11));
        titulo.setForeground(Colores.TEXTO_TENUE);

        fila.add(punto);
        fila.add(titulo);
        return fila;
    }
}
