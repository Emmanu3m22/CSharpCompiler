package com.compilador.optimizacion.fuente;

import com.compilador.AnalizadorConstants;
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
            // Se mantiene lo tokenizado hasta el momento
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

    public int inicioDe(Token t) {
        if (t == null) return 0;
        return posicionAIndice(t.beginLine, t.beginColumn);
    }

    public int finDe(Token t) {
        if (t == null) return 0;
        int end = posicionAIndice(t.endLine, t.endColumn);
        return Math.min(end + 1, texto.length());
    }

    /**
     * Busca el índice del token que coincide con la línea y columna exactas,
     * o el token más cercano que inicie en o después de esa posición.
     */
    public int buscarIndiceToken(int linea, int columna) {
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.beginLine == linea && t.beginColumn == columna) {
                return i;
            }
            if (t.beginLine > linea || (t.beginLine == linea && t.beginColumn >= columna)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Determina el índice del token final (inclusivo) de la sentencia que inicia en idxInicio.
     */
    public int delimitarSentencia(int idxInicio) {
        if (idxInicio < 0 || idxInicio >= tokens.size()) {
            return -1;
        }

        Token t = tokens.get(idxInicio);

        // Bloque { ... }
        if (t.kind == AnalizadorConstants.LLAVE_IZQ) {
            return saltarPares(idxInicio, AnalizadorConstants.LLAVE_IZQ, AnalizadorConstants.LLAVE_DER);
        }

        // else sent
        if (t.kind == AnalizadorConstants.ELSE) {
            return delimitarSentencia(idxInicio + 1);
        }

        // if (cond) sent [else sent]
        if (t.kind == AnalizadorConstants.IF) {
            int idx = idxInicio + 1;
            if (idx < tokens.size() && tokens.get(idx).kind == AnalizadorConstants.PAREN_IZQ) {
                idx = saltarPares(idx, AnalizadorConstants.PAREN_IZQ, AnalizadorConstants.PAREN_DER) + 1;
            }
            if (idx >= tokens.size()) return tokens.size() - 1;
            int finThen = delimitarSentencia(idx);
            int sig = finThen + 1;
            if (sig < tokens.size() && tokens.get(sig).kind == AnalizadorConstants.ELSE) {
                return delimitarSentencia(sig + 1);
            }
            return finThen;
        }

        // while (cond) sent
        if (t.kind == AnalizadorConstants.WHILE) {
            int idx = idxInicio + 1;
            if (idx < tokens.size() && tokens.get(idx).kind == AnalizadorConstants.PAREN_IZQ) {
                idx = saltarPares(idx, AnalizadorConstants.PAREN_IZQ, AnalizadorConstants.PAREN_DER) + 1;
            }
            if (idx >= tokens.size()) return tokens.size() - 1;
            return delimitarSentencia(idx);
        }

        // for / foreach (...) sent
        if (t.kind == AnalizadorConstants.FOR || t.kind == AnalizadorConstants.FOREACH) {
            int idx = idxInicio + 1;
            if (idx < tokens.size() && tokens.get(idx).kind == AnalizadorConstants.PAREN_IZQ) {
                idx = saltarPares(idx, AnalizadorConstants.PAREN_IZQ, AnalizadorConstants.PAREN_DER) + 1;
            }
            if (idx >= tokens.size()) return tokens.size() - 1;
            return delimitarSentencia(idx);
        }

        // do sent while (cond);
        if (t.kind == AnalizadorConstants.DO) {
            int finCuerpo = delimitarSentencia(idxInicio + 1);
            int idx = finCuerpo + 1;
            while (idx < tokens.size() && tokens.get(idx).kind != AnalizadorConstants.PUNTO_COMA) {
                idx++;
            }
            return Math.min(idx, tokens.size() - 1);
        }

        // switch (expr) { ... }
        if (t.kind == AnalizadorConstants.SWITCH) {
            int idx = idxInicio + 1;
            if (idx < tokens.size() && tokens.get(idx).kind == AnalizadorConstants.PAREN_IZQ) {
                idx = saltarPares(idx, AnalizadorConstants.PAREN_IZQ, AnalizadorConstants.PAREN_DER) + 1;
            }
            while (idx < tokens.size() && tokens.get(idx).kind != AnalizadorConstants.LLAVE_IZQ) {
                idx++;
            }
            if (idx < tokens.size()) {
                return saltarPares(idx, AnalizadorConstants.LLAVE_IZQ, AnalizadorConstants.LLAVE_DER);
            }
            return tokens.size() - 1;
        }

        // try { ... } catch (...) { ... } finally { ... }
        if (t.kind == AnalizadorConstants.TRY) {
            int idx = idxInicio + 1;
            int finActual = delimitarSentencia(idx);
            idx = finActual + 1;
            while (idx < tokens.size() && (tokens.get(idx).kind == AnalizadorConstants.CATCH || tokens.get(idx).kind == AnalizadorConstants.FINALLY)) {
                if (tokens.get(idx).kind == AnalizadorConstants.CATCH) {
                    idx++;
                    if (idx < tokens.size() && tokens.get(idx).kind == AnalizadorConstants.PAREN_IZQ) {
                        idx = saltarPares(idx, AnalizadorConstants.PAREN_IZQ, AnalizadorConstants.PAREN_DER) + 1;
                    }
                    finActual = delimitarSentencia(idx);
                    idx = finActual + 1;
                } else if (tokens.get(idx).kind == AnalizadorConstants.FINALLY) {
                    idx++;
                    finActual = delimitarSentencia(idx);
                    idx = finActual + 1;
                }
            }
            return finActual;
        }

        // Sentencia simple que termina en punto y coma ';'
        int nivelLlaves = 0;
        int nivelParentesis = 0;
        for (int i = idxInicio; i < tokens.size(); i++) {
            Token tk = tokens.get(i);
            if (tk.kind == AnalizadorConstants.LLAVE_IZQ) nivelLlaves++;
            else if (tk.kind == AnalizadorConstants.LLAVE_DER) {
                if (nivelLlaves > 0) nivelLlaves--;
                else return Math.max(idxInicio, i - 1);
            } else if (tk.kind == AnalizadorConstants.PAREN_IZQ) nivelParentesis++;
            else if (tk.kind == AnalizadorConstants.PAREN_DER) nivelParentesis--;
            else if (tk.kind == AnalizadorConstants.PUNTO_COMA && nivelLlaves == 0 && nivelParentesis == 0) {
                return i;
            }
        }

        return tokens.size() - 1;
    }

    private int saltarPares(int idxAbre, int kindAbre, int kindCierra) {
        int nivel = 0;
        for (int i = idxAbre; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.kind == kindAbre) {
                nivel++;
            } else if (t.kind == kindCierra) {
                nivel--;
                if (nivel == 0) {
                    return i;
                }
            }
        }
        return tokens.size() - 1;
    }

    /**
     * Obtiene el rango de caracteres [inicio, fin) correspondiente a la sentencia
     * que inicia en el token idxInicio.
     */
    public RangoTexto obtenerRangoSentencia(int idxInicio) {
        int idxFin = delimitarSentencia(idxInicio);
        if (idxFin < idxInicio || idxInicio < 0 || idxInicio >= tokens.size()) {
            return null;
        }
        Token tIni = tokens.get(idxInicio);
        Token tFin = tokens.get(idxFin);
        int ini = inicioDe(tIni);
        int fin = finDe(tFin);

        // Si la sentencia ocupa una línea o líneas completas, extendemos el rango
        // para consumir también la sangría inicial y el salto de línea final
        return expandirALineaCompleta(ini, fin);
    }

    private RangoTexto expandirALineaCompleta(int ini, int fin) {
        // Verificar si antes de 'ini' solo hay espacios/tabs desde el inicio de la línea
        int i = ini - 1;
        while (i >= 0 && texto.charAt(i) != '\n' && texto.charAt(i) != '\r') {
            if (texto.charAt(i) != ' ' && texto.charAt(i) != '\t') {
                return new RangoTexto(ini, fin); // Hay código previo en la misma línea
            }
            i--;
        }
        int nuevoIni = i + 1; // Inicio de línea (después del salto anterior)

        // Verificar si después de 'fin' solo hay espacios hasta el salto de línea
        int j = fin;
        while (j < texto.length() && texto.charAt(j) != '\n' && texto.charAt(j) != '\r') {
            if (texto.charAt(j) != ' ' && texto.charAt(j) != '\t') {
                return new RangoTexto(ini, fin); // Hay código posterior en la misma línea
            }
            j++;
        }
        int nuevoFin = j;
        if (nuevoFin < texto.length() && texto.charAt(nuevoFin) == '\r') {
            nuevoFin++;
        }
        if (nuevoFin < texto.length() && texto.charAt(nuevoFin) == '\n') {
            nuevoFin++;
        }

        return new RangoTexto(nuevoIni, nuevoFin);
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
