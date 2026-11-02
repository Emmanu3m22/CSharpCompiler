package com.compilador.optimizacion.fuente;

import com.compilador.AnalizadorConstants;
import com.compilador.Token;
import com.compilador.ast.*;

import java.util.List;

/**
 * R2/R3: ramas muertas por condiciones booleanas constantes
 * ({@code if (false)}, {@code while (false)} y el {@code else} de un {@code if (true)}).
 */
public class ReglaCondicionConstante implements ReglaCodigoMuerto {

    @Override
    public String id() {
        return "R2/R3";
    }

    @Override
    public void aplicar(NodoPrograma ast, CodigoFuente fuente, List<Hallazgo> hallazgos, RangosMarcados rangos) {
        ast.accept(new RecorridoAST() {
            @Override
            public Void visitar(NodoIf nodo) {
                Boolean valor = evaluarBooleanoConstante(nodo.getCondicion());
                if (Boolean.FALSE.equals(valor) && nodo.getBloqueElse() == null) {
                    if (eliminarSentencia(nodo, "Condición constante 'false': el cuerpo del 'if' nunca se ejecutará.")) {
                        return null;
                    }
                } else if (Boolean.TRUE.equals(valor) && nodo.getBloqueElse() != null) {
                    eliminarElse(nodo);
                }
                return super.visitar(nodo);
            }

            @Override
            public Void visitar(NodoWhile nodo) {
                if (Boolean.FALSE.equals(evaluarBooleanoConstante(nodo.getCondicion()))
                        && eliminarSentencia(nodo, "Condición constante 'false': el ciclo 'while' nunca se ejecutará.")) {
                    return null;
                }
                return super.visitar(nodo);
            }

            private boolean eliminarSentencia(Nodo nodo, String motivo) {
                int idx = fuente.buscarIndiceToken(nodo.getLinea(), nodo.getColumna());
                if (idx < 0) return false;
                RangoTexto r = fuente.obtenerRangoSentencia(idx);
                if (r == null || rangos.contiene(r.inicio())) return false;
                rangos.agregar(r);
                String txt = fuente.getTexto().substring(r.inicio(), r.fin()).trim();
                hallazgos.add(new Hallazgo(nodo.getLinea(), Hallazgo.Tipo.CONDICION_CONSTANTE, txt, motivo));
                return true;
            }

            private void eliminarElse(NodoIf nodo) {
                Nodo blqElse = nodo.getBloqueElse();
                int idxElse = buscarTokenElse(fuente, nodo.getLinea(), blqElse.getLinea());
                if (idxElse < 0) return;
                int idxFin = fuente.delimitarSentencia(idxElse);
                int ini = fuente.inicioDe(fuente.getTokens().get(idxElse));
                int fin = fuente.finDe(fuente.getTokens().get(idxFin));
                RangoTexto r = new RangoTexto(ini, fin);
                if (rangos.contiene(r.inicio())) return;
                rangos.agregar(r);
                String txt = fuente.getTexto().substring(ini, fin).trim();
                hallazgos.add(new Hallazgo(blqElse.getLinea(), Hallazgo.Tipo.CONDICION_CONSTANTE, txt,
                        "Condición constante 'true': la rama 'else' es inalcanzable y se elimina."));
            }
        });
    }

    private static Boolean evaluarBooleanoConstante(Nodo cond) {
        if (cond == null) return null;
        if (cond instanceof NodoAgrupacion g) {
            return evaluarBooleanoConstante(g.getExpresion());
        }
        if (cond instanceof NodoBooleano b) {
            return b.getValor();
        }
        if (cond instanceof NodoNegacionLogica neg) {
            Boolean sub = evaluarBooleanoConstante(neg.getExpresion());
            return sub != null ? !sub : null;
        }
        return null;
    }

    private static int buscarTokenElse(CodigoFuente fuente, int lineaIf, int lineaElse) {
        List<Token> tokens = fuente.getTokens();
        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);
            if (t.kind == AnalizadorConstants.ELSE && t.beginLine >= lineaIf && t.beginLine <= lineaElse) {
                return i;
            }
        }
        return -1;
    }
}
