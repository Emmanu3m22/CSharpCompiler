package com.compilador.gui;

import javax.swing.*;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.*;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FontMetrics;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.RenderingHints;
import java.util.ArrayList;
import java.util.List;

/**
 * Editor de código con números de línea y resaltado de sintaxis básico.
 * Panel izquierdo principal de la interfaz.
 */
public class EditorCodigo extends JPanel {

    private final JTextPane editor;
    private final LineNumberGutter lineas;
    private final JScrollPane scrollPane;
    private final JLabel lblCursor;
        private final List<Object> resaltadosErrores = new ArrayList<>();
        private final Highlighter.HighlightPainter pintorError =
            new DefaultHighlighter.DefaultHighlightPainter(new Color(120, 45, 55));

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
        lineas = new LineNumberGutter(editor);

        // ── ScrollPane ──
        scrollPane = new JScrollPane(editor);
        scrollPane.setRowHeaderView(lineas);
        scrollPane.setBorder(BorderFactory.createEmptyBorder());
        scrollPane.getViewport().setBackground(Colores.FONDO_EDITOR);
        scrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);

        add(scrollPane, BorderLayout.CENTER);

        // ── Indicador del Cursor ──
        lblCursor = new JLabel("Línea 1, Columna 1");
        lblCursor.setFont(Colores.FUENTE_CODIGO);
        lblCursor.setForeground(Colores.TEXTO_NORMAL);
        lblCursor.setBorder(BorderFactory.createEmptyBorder(4, 8, 4, 8));
        add(lblCursor, BorderLayout.SOUTH);

        editor.addCaretListener(new CaretListener() {
            @Override
            public void caretUpdate(CaretEvent e) {
                int pos = e.getDot();
                try {
                    Element root = editor.getDocument().getDefaultRootElement();
                    int row = root.getElementIndex(pos);
                    int col = pos - root.getElement(row).getStartOffset();
                    lblCursor.setText("Línea " + (row + 1) + ", Columna " + (col + 1));
                    lineas.repaint();
                } catch (Exception ex) {
                    lblCursor.setText("Línea 1, Columna 1");
                }
            }
        });

        // ── Listener para actualizar líneas y resaltado ──
        editor.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                actualizar();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                actualizar();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                lineas.repaint();
            }
            
            private void actualizar() {
                SwingUtilities.invokeLater(() -> {
                    limpiarResaltadoErrores();
                    aplicarResaltado();
                    lineas.revalidate();
                    lineas.repaint();
                });
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
        limpiarResaltadoErrores();
        editor.setText(codigo);
        aplicarResaltado();
        lineas.revalidate();
        lineas.repaint();
    }

    /**
     * Limpia el contenido del editor.
     */
    public void limpiar() {
        limpiarResaltadoErrores();
        editor.setText("");
        lineas.revalidate();
        lineas.repaint();
    }

    /**
     * Resalta con fondo rojo las líneas que contienen errores.
     */
    public void marcarLineasConErrores(List<Integer> lineasConError) {
        limpiarResaltadoErrores();
        Element raiz = editor.getDocument().getDefaultRootElement();

        for (int linea : lineasConError) {
            int indiceLinea = linea - 1;
            if (indiceLinea < 0 || indiceLinea >= raiz.getElementCount()) {
                continue;
            }

            Element elementoLinea = raiz.getElement(indiceLinea);
            int inicio = elementoLinea.getStartOffset();
            int fin = Math.min(elementoLinea.getEndOffset(), editor.getDocument().getLength());
            if (inicio < fin) {
                try {
                    resaltadosErrores.add(editor.getHighlighter().addHighlight(
                            inicio, fin, pintorError));
                } catch (BadLocationException ignored) {
                    // La línea puede cambiar mientras se actualiza el documento.
                }
            }
        }
    }

    private void limpiarResaltadoErrores() {
        Highlighter highlighter = editor.getHighlighter();
        for (Object resaltado : resaltadosErrores) {
            highlighter.removeHighlight(resaltado);
        }
        resaltadosErrores.clear();
    }

    /**
     * Coloca el cursor en una ubicación del código y desplaza el editor hasta ella.
     */
    public void irAUbicacion(int linea, int columna) {
        Element raiz = editor.getDocument().getDefaultRootElement();
        int indiceLinea = Math.max(0, Math.min(linea - 1, raiz.getElementCount() - 1));
        Element elementoLinea = raiz.getElement(indiceLinea);
        int posicion = elementoLinea.getStartOffset() + Math.max(0, columna - 1);
        posicion = Math.min(posicion, editor.getDocument().getLength());

        editor.requestFocusInWindow();
        editor.setCaretPosition(posicion);
        try {
            Rectangle rectangulo = editor.modelToView2D(posicion).getBounds();
            editor.scrollRectToVisible(rectangulo);
        } catch (BadLocationException ignored) {
            // La posición ya fue acotada al contenido actual del documento.
        }
    }

    /**
     * Componente personalizado para dibujar los números de línea alineados.
     */
    private class LineNumberGutter extends JComponent {
        private final JTextPane textPane;
        private FontMetrics fm;

        public LineNumberGutter(JTextPane textPane) {
            this.textPane = textPane;
            setFont(Colores.FUENTE_CODIGO);
            fm = getFontMetrics(Colores.FUENTE_CODIGO);
        }

        @Override
        public Dimension getPreferredSize() {
            int lines = getLineCount();
            int digits = Math.max(3, String.valueOf(lines).length());
            int width = fm.stringWidth("0") * digits + 16;
            return new Dimension(width, textPane.getPreferredSize().height);
        }

        private int getLineCount() {
            Element root = textPane.getDocument().getDefaultRootElement();
            return root.getElementCount();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            
            Graphics2D g2d = (Graphics2D) g;
            g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            g2d.setColor(Colores.FONDO_LINEAS);
            g2d.fillRect(0, 0, getWidth(), getHeight());
            
            g2d.setColor(Colores.BORDE);
            g2d.drawLine(getWidth() - 1, 0, getWidth() - 1, getHeight());
            
            g2d.setColor(Colores.TEXTO_LINEAS);
            g2d.setFont(Colores.FUENTE_CODIGO);
            
            Rectangle clip = g.getClipBounds();
            if (clip == null) return;
            
            int startOffset = textPane.viewToModel2D(new Point(0, clip.y));
            int endOffset = textPane.viewToModel2D(new Point(0, clip.y + clip.height));
            
            Element root = textPane.getDocument().getDefaultRootElement();
            int startLine = root.getElementIndex(startOffset);
            int endLine = root.getElementIndex(endOffset);
            
            try {
                for (int i = startLine; i <= endLine; i++) {
                    Element line = root.getElement(i);
                    Rectangle r = textPane.modelToView2D(line.getStartOffset()).getBounds();
                    
                    String lineNumber = String.valueOf(i + 1);
                    int stringWidth = fm.stringWidth(lineNumber);
                    int x = getWidth() - stringWidth - 8;
                    int y = r.y + fm.getAscent() + (r.height - fm.getHeight()) / 2;
                    
                    g2d.drawString(lineNumber, x, y);
                }
            } catch (BadLocationException e) {
                // Ignorar
            }
        }
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
