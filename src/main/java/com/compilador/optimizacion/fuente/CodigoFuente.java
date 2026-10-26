package com.compilador.optimizacion.fuente;

import com.compilador.AnalizadorTokenManager;
import com.compilador.SimpleCharStream;
import com.compilador.Token;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Representa el código fuente con mapeo directo de caracteres y tokens.
 *
 * Utiliza {@link SimpleCharStream#setTabSize(int)} en 1 para garantizar
 * que cada carácter coincida exactamente con un desplazamiento en el texto original.
 */
public class CodigoFuente {

    private final String texto;
    private final List<Token> tokens;
    private final int[] iniciosDeLinea;

    public CodigoFuente(String texto) {
        this.texto = texto != null ? texto : "";
        this.iniciosDeLinea = calcularIniciosDeLinea(this.texto);
        this.tokens = tokenizar(this.texto);
    }

    private static int[] calcularIniciosDeLinea(String s) {
        List<Integer> inicios = new ArrayList<>();
        inicios.add(0);
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '\r') {
                if (i + 1 < s.length() && s.charAt(i + 1) == '\n') {
                    i++;
                }
                inicios.add(i + 1);
            } else if (c == '\n') {
                inicios.add(i + 1);
            }
        }
        int[] arr = new int[inicios.size()];
        for (int i = 0; i < inicios.size(); i++) {
            arr[i] = inicios.get(i);
        }
        return arr;
    }

    private static List<Token> tokenizar(String s) {
        List<Token> lista = new ArrayList<>();
        if (s.isEmpty()) {
            return lista;
        }
        try {
            SimpleCharStream scs = new SimpleCharStream(new StringReader(s), 1, 1);
            scs.setTabSize(1);
            AnalizadorTokenManager tm = new AnalizadorTokenManager(scs);
            Token t;
            while ((t = tm.getNextToken()) != null && t.kind != 0) {
                lista.add(t);
            }
        } catch (Throwable e) {
            // Si el tokenizador encuentra un error irrecuperable, conservamos lo obtenido hasta el momento
        }
        return Collections.unmodifiableList(lista);
    }

    public String getTexto() {
        return texto;
    }

    public List<Token> getTokens() {
        return tokens;
    }

    /**
     * Convierte una posición (línea, columna) 1-based en un índice de carácter 0-based.
     */
    public int posicionAIndice(int linea, int columna) {
        if (linea <= 0) return 0;
        int idxLinea = linea - 1;
        if (idxLinea >= iniciosDeLinea.length) {
            return texto.length();
        }
        int inicioLinea = iniciosDeLinea[idxLinea];
        int col = Math.max(1, columna) - 1;
        int pos = inicioLinea + col;
        return Math.min(pos, texto.length());
    }

    /**
     * Obtiene el índice inicial de un token.
     */
    public int inicioDe(Token t) {
        if (t == null) return 0;
        return posicionAIndice(t.beginLine, t.beginColumn);
    }

    /**
     * Obtiene el índice final exclusivo [inicio, fin) de un token.
     */
    public int finDe(Token t) {
        if (t == null) return 0;
        int end = posicionAIndice(t.endLine, t.endColumn);
        return Math.min(end + 1, texto.length());
    }

    /**
     * Enmascara los rangos indicados reemplazando sus caracteres por espacios en blanco,
     * pero PRESERVANDO los saltos de línea ('\r', '\n') para mantener exactamente
     * la numeración de líneas original.
     */
    public String enmascarar(List<RangoTexto> rangos) {
        if (rangos == null || rangos.isEmpty() || texto.isEmpty()) {
            return texto;
        }
        List<RangoTexto> ordenados = normalizarRangos(rangos);
        StringBuilder sb = new StringBuilder(texto);
        for (RangoTexto r : ordenados) {
            int ini = Math.max(0, Math.min(r.inicio(), texto.length()));
            int fin = Math.max(ini, Math.min(r.fin(), texto.length()));
            for (int i = ini; i < fin; i++) {
                char c = sb.charAt(i);
                if (c != '\r' && c != '\n') {
                    sb.setCharAt(i, ' ');
                }
            }
        }
        return sb.toString();
    }

    /**
     * Colapsa (elimina físicamente) los rangos del texto original para generar
     * la versión final limpia y legible.
     */
    public String colapsar(List<RangoTexto> rangos) {
        if (rangos == null || rangos.isEmpty() || texto.isEmpty()) {
            return texto;
        }
        List<RangoTexto> ordenados = normalizarRangos(rangos);
        StringBuilder sb = new StringBuilder();
        int cursor = 0;
        for (RangoTexto r : ordenados) {
            int ini = Math.max(0, Math.min(r.inicio(), texto.length()));
            int fin = Math.max(ini, Math.min(r.fin(), texto.length()));
            if (ini > cursor) {
                sb.append(texto, cursor, ini);
            }
            cursor = Math.max(cursor, fin);
        }
        if (cursor < texto.length()) {
            sb.append(texto, cursor, texto.length());
        }
        return limpiarLineasVaciasExcessivas(sb.toString());
    }

    private static String limpiarLineasVaciasExcessivas(String s) {
        // Normaliza múltiples líneas en blanco consecutivas a máximo una
        return s.replaceAll("([ \t]*\r?\n){3,}", "\n\n").trim() + "\n";
    }

    public static List<RangoTexto> normalizarRangos(List<RangoTexto> rangos) {
        if (rangos == null || rangos.isEmpty()) {
            return List.of();
        }
        List<RangoTexto> copia = new ArrayList<>(rangos);
        Collections.sort(copia);
        List<RangoTexto> fusionados = new ArrayList<>();
        RangoTexto actual = copia.get(0);
        for (int i = 1; i < copia.size(); i++) {
            RangoTexto siguiente = copia.get(i);
            if (siguiente.inicio() <= actual.fin()) {
                actual = new RangoTexto(actual.inicio(), Math.max(actual.fin(), siguiente.fin()));
            } else {
                fusionados.add(actual);
                actual = siguiente;
            }
        }
        fusionados.add(actual);
        return fusionados;
    }
}
