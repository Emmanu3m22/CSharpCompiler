package com.compilador.gui;

import javax.swing.*;
import javax.swing.text.*;
import java.awt.*;

/**
 * Editor de código con números de línea y resaltado de sintaxis básico.
 * Panel izquierdo principal de la interfaz.
 */
public class EditorCodigo extends JPanel {

    private final JTextPane editor;
    private final JTextArea lineas;
    private final JScrollPane scrollPane;

    /** Palabras reservadas del lenguaje para resaltado */
    private static final String[] KEYWORDS = {
            "int", "float", "double", "bool", "string", "char", "void",
            "if", "else", "while", "for", "do", "return",
            "true", "false", "class", "public", "private", "static",
            "new", "null"
    };

    public EditorCodigo() {
        setLayout(new BorderLayout());
        setBackground(Colores.FONDO_EDITOR);

        // ── Editor de texto principal ──
        editor = new JTextPane();
        editor.setFont(Colores.FUENTE_CODIGO);
        editor.setBackground(Colores.FONDO_EDITOR);
        editor.setForeground(Colores.TEXTO_NORMAL);
        editor.setCaretColor(Colores.TEXTO_NORMAL);
        editor.setSelectionColor(Colores.FONDO_SELECCION);
        editor.setSelectedTextColor(Colores.TEXTO_NORMAL);
        editor.setMargin(new Insets(8, 8, 8, 8));

        // ── Números de línea ──
        lineas = new JTextArea("  1 ");
        lineas.setFont(Colores.FUENTE_CODIGO);
        lineas.setBackground(Colores.FONDO_LINEAS);
        lineas.setForeground(Colores.TEXTO_LINEAS);
        lineas.setEditable(false);
        lineas.setFocusable(false);
        lineas.setMargin(new Insets(8, 4, 8, 8));
        lineas.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 1, Colores.BORDE));

        // ── ScrollPane ──
        scrollPane = new JScrollPane(editor);
        scrollPane.setRowHeaderView(lineas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Colores.FONDO_EDITOR);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

        // ── Listener para actualizar líneas y resaltado ──
        editor.getDocument().addDocumentListener(new javax.swing.event.DocumentListener() {
            @Override
            public void insertUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    actualizarLineas();
                    aplicarResaltado();
                });
            }

            @Override
            public void removeUpdate(javax.swing.event.DocumentEvent e) {
                SwingUtilities.invokeLater(() -> {
                    actualizarLineas();
                    aplicarResaltado();
                });
            }

            @Override
            public void changedUpdate(javax.swing.event.DocumentEvent e) {
            }
        });
    }

    /**
     * Retorna el texto escrito en el editor.
     */
    public String getCodigo() {
        return editor.getText();
    }

    /**
     * Establece el texto del editor y actualiza todo.
     */
    public void setCodigo(String codigo) {
        editor.setText(codigo);
        actualizarLineas();
        aplicarResaltado();
    }

    /**
     * Limpia el contenido del editor.
     */
    public void limpiar() {
        editor.setText("");
    }

    /**
     * Actualiza la columna de números de línea.
     */
    private void actualizarLineas() {
        String texto = editor.getText();
        int totalLineas = texto.split("\n", -1).length;
        StringBuilder sb = new StringBuilder();
        int anchoDigitos = String.valueOf(totalLineas).length();
        for (int i = 1; i <= totalLineas; i++) {
            sb.append(String.format("%" + (anchoDigitos + 1) + "d ", i));
            if (i < totalLineas)
                sb.append("\n");
        }
        lineas.setText(sb.toString());
    }

    /**
     * Aplica resaltado de sintaxis básico al contenido del editor.
     * Colorea: palabras reservadas, strings, números, comentarios, operadores.
     */
    private void aplicarResaltado() {
        StyledDocument doc = editor.getStyledDocument();
        String texto = editor.getText();

        // Estilo base
        Style estiloBase = doc.addStyle("base", null);
        StyleConstants.setForeground(estiloBase, Colores.TEXTO_NORMAL);
        StyleConstants.setFontFamily(estiloBase, Colores.FUENTE_CODIGO.getFamily());
        StyleConstants.setFontSize(estiloBase, Colores.FUENTE_CODIGO.getSize());
        doc.setCharacterAttributes(0, texto.length(), estiloBase, true);

        // Estilos por tipo de token
        Style keywordStyle = doc.addStyle("keyword", null);
        StyleConstants.setForeground(keywordStyle, Colores.SINTAXIS_KEYWORD);
        StyleConstants.setBold(keywordStyle, true);

        Style stringStyle = doc.addStyle("string", null);
        StyleConstants.setForeground(stringStyle, Colores.SINTAXIS_STRING);

        Style numStyle = doc.addStyle("number", null);
        StyleConstants.setForeground(numStyle, Colores.SINTAXIS_NUMERO);

        Style commentStyle = doc.addStyle("comment", null);
        StyleConstants.setForeground(commentStyle, Colores.SINTAXIS_COMENTARIO);
        StyleConstants.setItalic(commentStyle, true);

        Style opStyle = doc.addStyle("operator", null);
        StyleConstants.setForeground(opStyle, Colores.SINTAXIS_OPERADOR);

        // Resaltar comentarios de línea
        int idx = 0;
        while ((idx = texto.indexOf("//", idx)) >= 0) {
            int fin = texto.indexOf('\n', idx);
            if (fin < 0)
                fin = texto.length();
            doc.setCharacterAttributes(idx, fin - idx, commentStyle, true);
            idx = fin;
        }

        // Resaltar cadenas
        idx = 0;
        while ((idx = texto.indexOf('"', idx)) >= 0) {
            int fin = texto.indexOf('"', idx + 1);
            if (fin < 0)
                break;
            doc.setCharacterAttributes(idx, fin - idx + 1, stringStyle, true);
            idx = fin + 1;
        }

        // Resaltar keywords (solo si no están dentro de un identificador más largo)
        for (String kw : KEYWORDS) {
            idx = 0;
            while ((idx = texto.indexOf(kw, idx)) >= 0) {
                // Verificar que sea una palabra completa
                boolean inicioOk = idx == 0 || !Character.isLetterOrDigit(texto.charAt(idx - 1));
                boolean finOk = idx + kw.length() >= texto.length()
                        || !Character.isLetterOrDigit(texto.charAt(idx + kw.length()));
                if (inicioOk && finOk) {
                    doc.setCharacterAttributes(idx, kw.length(), keywordStyle, true);
                }
                idx += kw.length();
            }
        }

        // Resaltar "Console.WriteLine" como keyword
        String cwl = "Console.WriteLine";
        idx = 0;
        while ((idx = texto.indexOf(cwl, idx)) >= 0) {
            doc.setCharacterAttributes(idx, cwl.length(), keywordStyle, true);
            idx += cwl.length();
        }

        // Resaltar números
        for (int i = 0; i < texto.length(); i++) {
            if (Character.isDigit(texto.charAt(i))) {
                int start = i;
                while (i < texto.length() && (Character.isDigit(texto.charAt(i)) || texto.charAt(i) == '.')) {
                    i++;
                }
                // Solo si no es parte de un identificador
                boolean esNumero = start == 0 || !Character.isLetter(texto.charAt(start - 1));
                if (esNumero) {
                    doc.setCharacterAttributes(start, i - start, numStyle, true);
                }
            }
        }
    }

    /**
     * Retorna el componente editor subyacente (para integración avanzada).
     */
    public JTextPane getEditor() {
        return editor;
    }
}
