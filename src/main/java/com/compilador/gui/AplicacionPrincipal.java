package com.compilador.gui;

import com.compilador.Analizador;
import com.compilador.AnalizadorConstants;
import com.compilador.ParseException;
import com.compilador.Token;
import com.compilador.TokenMgrError;
import com.compilador.ast.NodoPrograma;
import com.compilador.errores.ErrorLexico;
import com.compilador.errores.ErrorSintactico;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

/**
 * Ventana principal del compilador LenguajeCSharp.
 * Orquesta el flujo: editor → léxico/sintáctico → AST → semántico → resultados.
 *
 *
 */
public class AplicacionPrincipal extends JFrame {

    // ── Componentes de la interfaz ──
    private final EditorCodigo editor;
    private final TablaTokens tablaTokens;
    private final PanelErrores panelErrores;
    private final PanelSintactico panelSintactico;
    private final PanelResumen panelResumen;
    private JTabbedPane pestanas;

    // ── Botones del toolbar ──
    private JButton btnAnalizar;
    private JButton btnLimpiar;

    /**
     * Clase auxiliar para retornar tanto tokens como errores léxicos.
     */
    private static class TokenizationResult {
        List<String[]> tokens;
        List<ErrorLexico> erroresLexicos;

        TokenizationResult(List<String[]> tokens, List<ErrorLexico> erroresLexicos) {
            this.tokens = tokens;
            this.erroresLexicos = erroresLexicos;
        }
    }

    public AplicacionPrincipal() {
        super("Compilador LenguajeCSharp v1.0");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1200, 750);
        setMinimumSize(new Dimension(900, 550));
        setLocationRelativeTo(null);

        // ── Look & Feel del fondo ──
        getContentPane().setBackground(Colores.FONDO_PRINCIPAL);

        // ── Crear componentes ──
        editor = new EditorCodigo();
        tablaTokens = new TablaTokens();
        panelErrores = new PanelErrores();
        panelSintactico = new PanelSintactico();
        panelResumen = new PanelResumen();

        // ── Ensamblar layout ──
        setLayout(new BorderLayout(0, 0));
        add(crearToolbar(), BorderLayout.NORTH);
        add(crearContenidoPrincipal(), BorderLayout.CENTER);
        add(panelResumen, BorderLayout.SOUTH);

        // ── Cargar ejemplo inicial ──
        if (Ejemplos.cantidad() > 0) {
            editor.setCodigo(Ejemplos.getCodigo(0));
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  TOOLBAR
    // ════════════════════════════════════════════════════════════════

    private JPanel crearToolbar() {
        JPanel toolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 8, 6));
        toolbar.setBackground(Colores.FONDO_TOOLBAR);
        toolbar.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 0, Colores.BORDE));

        // Botón Analizar
        btnAnalizar = crearBoton("▶ Analizar", Colores.ACENTO_PRINCIPAL, e -> ejecutarAnalisis());
        toolbar.add(btnAnalizar);

        // Botón Limpiar
        btnLimpiar = crearBoton("✕ Limpiar", Colores.TEXTO_TENUE, e -> limpiarTodo());
        toolbar.add(btnLimpiar);

        // Separador visual
        toolbar.add(crearSeparador());

        // Menú de ejemplos
        JComboBox<String> comboEjemplos = new JComboBox<>(Ejemplos.NOMBRES);
        comboEjemplos.setFont(Colores.FUENTE_NORMAL);
        comboEjemplos.setBackground(Colores.FONDO_PANEL);
        comboEjemplos.setForeground(Colores.TEXTO_NORMAL);
        comboEjemplos.setPreferredSize(new Dimension(200, 28));
        comboEjemplos.addActionListener(e -> {
            int idx = comboEjemplos.getSelectedIndex();
            if (idx >= 0) {
                editor.setCodigo(Ejemplos.getCodigo(idx));
                limpiarResultados();
            }
        });

        JLabel lblEjemplos = new JLabel("Ejemplo: ");
        lblEjemplos.setFont(Colores.FUENTE_NORMAL);
        lblEjemplos.setForeground(Colores.TEXTO_TENUE);
        toolbar.add(lblEjemplos);
        toolbar.add(comboEjemplos);

        // Separador visual
        toolbar.add(crearSeparador());

        // Botón Abrir archivo
        JButton btnAbrir = crearBoton(" Abrir", Colores.TEXTO_TENUE, e -> abrirArchivo());
        toolbar.add(btnAbrir);

        return toolbar;
    }

    private JButton crearBoton(String texto, Color color, ActionListener accion) {
        JButton btn = new JButton(texto);
        btn.setFont(Colores.FUENTE_NORMAL);
        btn.setForeground(color);
        btn.setBackground(Colores.FONDO_PANEL);
        btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(Colores.BORDE, 1),
            BorderFactory.createEmptyBorder(4, 12, 4, 12)
        ));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(accion);

        // Hover effect
        btn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btn.setBackground(Colores.FONDO_SELECCION);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colores.BORDE_ENFOCADO, 1),
                    BorderFactory.createEmptyBorder(4, 12, 4, 12)
                ));
            }
            @Override
            public void mouseExited(MouseEvent e) {
                btn.setBackground(Colores.FONDO_PANEL);
                btn.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(Colores.BORDE, 1),
                    BorderFactory.createEmptyBorder(4, 12, 4, 12)
                ));
            }
        });

        return btn;
    }

    private JSeparator crearSeparador() {
        JSeparator sep = new JSeparator(SwingConstants.VERTICAL);
        sep.setPreferredSize(new Dimension(1, 24));
        sep.setForeground(Colores.BORDE);
        return sep;
    }

    // ════════════════════════════════════════════════════════════════
    //  CONTENIDO PRINCIPAL (editor + pestañas)
    // ════════════════════════════════════════════════════════════════

    private JSplitPane crearContenidoPrincipal() {
        // Panel de pestañas (lado derecho)
        pestanas = new JTabbedPane(JTabbedPane.TOP);
        pestanas.setFont(Colores.FUENTE_NORMAL);
        pestanas.setBackground(Colores.FONDO_PANEL);
        pestanas.setForeground(Colores.TEXTO_NORMAL);

        pestanas.addTab("🔴 Errores", panelErrores);
        pestanas.addTab("Léxico", tablaTokens);
        pestanas.addTab("Sintáctico", panelSintactico);

        // Split pane
        JSplitPane split = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, editor, pestanas);
        split.setDividerLocation(500);
        split.setDividerSize(4);
        split.setBackground(Colores.FONDO_PRINCIPAL);
        split.setBorder(BorderFactory.createEmptyBorder());

        return split;
    }

    // ════════════════════════════════════════════════════════════════
    //  LÓGICA DEL ANÁLISIS
    // ════════════════════════════════════════════════════════════════

    /**
     * Ejecuta el pipeline completo: léxico → sintáctico → semántico.
     * Conecta con las clases existentes: Analizador, AnalizadorSemantico.
     */
    private void ejecutarAnalisis() {
        String codigo = editor.getCodigo().trim();
        if (codigo.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "El editor está vacío. Escribe código o selecciona un ejemplo.",
                "Sin código", JOptionPane.WARNING_MESSAGE);
            return;
        }

        limpiarResultados();

        try {
            // 1. Crear el parser con el código del editor
            ByteArrayInputStream bais = new ByteArrayInputStream(
                codigo.getBytes(StandardCharsets.UTF_8)
            );
            Analizador parser = new Analizador(bais);

            // 2. Ejecutar análisis léxico + sintáctico → AST
            NodoPrograma ast = null;
            //boolean parseOk = true;
            try {
                ast = parser.programa();
            } catch (ParseException ex) {
                //parseOk = false;
                // Capturar información del error y agregarlo a la lista
                Token tokenActual = ex.currentToken;
                if (tokenActual != null) {
                    String tokenEsperadoStr = tokenActual.next != null ? tokenActual.next.image : "(desconocido)";
                    ErrorSintactico error = new ErrorSintactico(
                        tokenActual.image,           // tokenEncontrado
                        tokenEsperadoStr,            // tokenEsperado
                        ex.getMessage(),             // mensaje
                        tokenActual.beginLine,       // linea
                        tokenActual.beginColumn      // columna
                    );
                    parser.getErroresSintacticos().add(error);
                }
            }

            // 3. Recoger errores léxicos y sintácticos
            List<ErrorLexico> erroresLex = parser.getErroresLexicos();
            List<ErrorSintactico> erroresSint = parser.getErroresSintacticos();

            // 4. Extraer tokens para la tabla
            //    Necesitamos re-tokenizer para obtener la lista de tokens
            TokenizationResult result = extraerTokensConErrores(codigo);
            List<String[]> tokensList = result.tokens;
            
            // Agregar errores léxicos encontrados durante la tokenización
            erroresLex.addAll(result.erroresLexicos);
            
            tablaTokens.cargarTokens(tokensList, result.erroresLexicos);

            // 5. Mostrar AST y errores sintácticos
            if (ast != null) {
                panelSintactico.mostrarAST(ast.toString());
            } else {
                panelSintactico.mostrarAST("(No se pudo generar el AST debido a errores de sintaxis)");
            }
            panelSintactico.cargarErrores(erroresSint);

            // 5b. Cargar errores en el panel elegante
            panelErrores.cargarErroresLexicos(erroresLex);
            panelErrores.cargarErroresSintacticos(erroresSint);

            // 6. Actualizar resumen
            panelResumen.actualizar(
                tokensList.size(),
                erroresLex.size(),
                erroresSint.size(),
                0  // Sin errores semánticos
            );

            // 7. Ir a la pestaña más relevante
            if (!erroresLex.isEmpty() || !erroresSint.isEmpty()) {
                pestanas.setSelectedIndex(0); // Errores
            }

        } catch (TokenMgrError ex) {
            panelResumen.actualizar(0, 1, 0, 0);
            JOptionPane.showMessageDialog(this,
                "Error del tokenizador: " + ex.getMessage(),
                "Error Léxico", JOptionPane.ERROR_MESSAGE);
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                "Error inesperado: " + ex.getMessage(),
                "Error", JOptionPane.ERROR_MESSAGE);
            ex.printStackTrace();
        }
    }

    /**
     * Re-tokeniza el código para obtener la lista de tokens y errores léxicos.
     * Crea un parser separado solo para extraer tokens uno por uno.
     */
    private TokenizationResult extraerTokensConErrores(String codigo) {
        List<String[]> tokens = new ArrayList<>();
        List<ErrorLexico> erroresLex = new ArrayList<>();
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(
                codigo.getBytes(StandardCharsets.UTF_8)
            );
            Analizador tokenizer = new Analizador(bais);

            com.compilador.Token t;
            while (true) {
                t = tokenizer.getNextToken();
                if (t.kind == 0) break; // EOF

                // Detectar tokens ERROR_LEXICO según AnalizadorConstants
                if (t.kind == com.compilador.AnalizadorConstants.ERROR_LEXICO) {
                    ErrorLexico error = new ErrorLexico(
                        t.image,
                        "Carácter no reconocido",
                        t.beginLine,
                        t.beginColumn
                    );
                    erroresLex.add(error);
                }

                // Obtener nombre del tipo de token desde la imagen del token
                String tipoNombre = obtenerNombreTipo(t.kind);
                tokens.add(new String[]{
                    t.image,
                    tipoNombre,
                    String.valueOf(t.beginLine),
                    String.valueOf(t.beginColumn)
                });
            }
        } catch (Exception e) {
            // Si hay error de tokenization, retornar lo que se pudo obtener
        }
        return new TokenizationResult(tokens, erroresLex);
    }

    /**
     * Mapea el kind de un token a un nombre legible.
     * Los kinds son generados por JavaCC en AnalizadorConstants.
     */
    private String obtenerNombreTipo(int kind) {
        try {
            // Usar el arreglo tokenImage generado por JavaCC
            String[] nombres = AnalizadorConstants.tokenImage;
            if (kind >= 0 && kind < nombres.length) {
                String nombre = nombres[kind];
                // Limpiar comillas del tokenImage
                if (nombre.startsWith("\"") && nombre.endsWith("\"")) {
                    nombre = nombre.substring(1, nombre.length() - 1);
                }
                return nombre;
            }
        } catch (Exception e) {
            // Fallback
        }
        return "TOKEN_" + kind;
    }

    // ════════════════════════════════════════════════════════════════
    //  UTILIDADES
    // ════════════════════════════════════════════════════════════════

    private void limpiarTodo() {
        editor.limpiar();
        limpiarResultados();
    }

    private void limpiarResultados() {
        tablaTokens.limpiar();
        panelErrores.limpiar();
        panelSintactico.limpiar();
        panelResumen.limpiar();
    }

    private void abrirArchivo() {
        JFileChooser chooser = new JFileChooser(".");
        chooser.setDialogTitle("Abrir archivo de código fuente");
        chooser.setFileFilter(new javax.swing.filechooser.FileNameExtensionFilter(
            "Archivos de texto (*.txt, *.cs)", "txt", "cs"
        ));

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File archivo = chooser.getSelectedFile();
            try {
                String contenido = new String(
                    java.nio.file.Files.readAllBytes(archivo.toPath()),
                    StandardCharsets.UTF_8
                );
                editor.setCodigo(contenido);
                limpiarResultados();
                setTitle("Compilador LenguajeCSharp — " + archivo.getName());
            } catch (IOException ex) {
                JOptionPane.showMessageDialog(this,
                    "No se pudo leer el archivo: " + ex.getMessage(),
                    "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    // ════════════════════════════════════════════════════════════════
    //  PUNTO DE ENTRADA
    // ════════════════════════════════════════════════════════════════

    /**
     * Lanza la aplicación gráfica del compilador.
     */
    public static void main(String[] args) {
        // Configurar look & feel del sistema
        try {
            UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
        } catch (Exception ignored) {}

        // Personalizar colores globales de Swing
        UIManager.put("Panel.background", Colores.FONDO_PRINCIPAL);
        UIManager.put("OptionPane.background", Colores.FONDO_PANEL);
        UIManager.put("OptionPane.messageForeground", Colores.TEXTO_NORMAL);

        SwingUtilities.invokeLater(() -> {
            AplicacionPrincipal app = new AplicacionPrincipal();
            app.setVisible(true);
        });
    }
}
