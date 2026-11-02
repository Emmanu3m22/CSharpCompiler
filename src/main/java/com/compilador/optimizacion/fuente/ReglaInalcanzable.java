package com.compilador.optimizacion.fuente;

import com.compilador.ast.*;

import java.util.List;

/**
 * R1: elimina toda sentencia posterior a un terminador (return, break, continue, throw)
 * dentro del mismo bloque o ámbito secuencial.
 */
public class ReglaInalcanzable implements ReglaCodigoMuerto {

    @Override
    public String id() {
        return "R1";
    }

    @Override
    public void aplicar(NodoPrograma ast, CodigoFuente fuente, List<Hallazgo> hallazgos, RangosMarcados rangos) {
        ast.accept(new RecorridoAST() {
            @Override
            public Void visitar(NodoPrograma nodo) {
                analizar(nodo.getSentencias());
                return null;
            }

            @Override
            public Void visitar(NodoBloque nodo) {
                analizar(nodo.getSentencias());
                return null;
            }

            @Override
            public Void visitar(NodoCaso nodo) {
                analizar(nodo.getSentencias());
                return null;
            }

            private void analizar(List<Nodo> sentencias) {
                if (sentencias == null || sentencias.isEmpty()) return;

                boolean alcanzable = true;
                String comandoTerminador = null;
                int lineaTerminador = 0;

                for (Nodo s : sentencias) {
                    if (s == null) continue;

                    if (!alcanzable) {
                        marcarInalcanzable(s, comandoTerminador, lineaTerminador);
                        continue;
                    }
                    if (s instanceof NodoComando cmd && esTerminador(cmd.getComando())) {
                        alcanzable = false;
                        comandoTerminador = cmd.getComando();
                        lineaTerminador = cmd.getLinea();
                    }
                    s.accept(this);
                }
            }

            private boolean esTerminador(String c) {
                return "return".equals(c) || "break".equals(c) || "continue".equals(c) || "throw".equals(c);
            }

            private void marcarInalcanzable(Nodo s, String terminador, int lineaTerminador) {
                int idxToken = fuente.buscarIndiceToken(s.getLinea(), s.getColumna());
                if (idxToken < 0) return;
                RangoTexto rango = fuente.obtenerRangoSentencia(idxToken);
                if (rango == null || rangos.contiene(rango.inicio())) return;
                rangos.agregar(rango);
                String texto = fuente.getTexto().substring(rango.inicio(), rango.fin()).trim();
                hallazgos.add(new Hallazgo(
                        s.getLinea(),
                        Hallazgo.Tipo.INALCANZABLE,
                        texto,
                        String.format("Código inalcanzable: se encuentra después de una instrucción '%s' en la línea %d.",
                                terminador, lineaTerminador)));
            }
        });
    }
}
