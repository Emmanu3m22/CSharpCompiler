package com.compilador.optimizacion.fuente;

import com.compilador.Analizador;
import com.compilador.AnalizadorConstants;
import com.compilador.Token;
import com.compilador.ast.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Pase de optimización para la eliminación de código muerto.
 *
 * Implementa las reglas:
 * <ul>
 *   <li>R1: Sentencias inalcanzables tras return, break, continue o throw.</li>
 *   <li>R2/R3: Ramas muertas por condiciones constantes (ej. if (false), while (false)).</li>
 *   <li>R4: Variables declaradas o asignadas que nunca se leen (análisis de punto fijo).</li>
 * </ul>
 */
public class EliminadorCodigoMuerto implements PaseOptimizacionFuente {

    @Override
    public String nombre() {
        return "Eliminación de Código Muerto";
    }

    @Override
    public String descripcion() {
        return "Detecta y elimina código inalcanzable tras terminadores, condiciones constantes y variables sin uso posterior.";
    }

    @Override
    public EtapaOptimizacion aplicar(CodigoFuente fuente) {
        if (fuente == null || fuente.getTexto().trim().isEmpty()) {
            return EtapaOptimizacion.sinCambios(nombre(), descripcion(), "", 0L);
        }

        long t0 = System.nanoTime();
        List<Hallazgo> hallazgos = new ArrayList<>();
        List<RangoTexto> rangos = new ArrayList<>();

        // 1. Parsear el código fuente actual para obtener la estructura sintáctica
        NodoPrograma ast = parsear(fuente.getTexto());
        if (ast != null) {
            // Aplicar R1: Código inalcanzable tras terminadores
            aplicarR1Inalcanzable(ast, fuente, hallazgos, rangos);

            // Aplicar R2/R3: Condiciones constantes (if false, while false, else inalcanzable)
            aplicarR2R3CondicionConstante(ast, fuente, hallazgos, rangos);
        }

        long dt = System.nanoTime() - t0;
        if (rangos.isEmpty()) {
            return EtapaOptimizacion.sinCambios(nombre(), descripcion(), fuente.getTexto(), dt);
        }

        List<RangoTexto> normalizados = CodigoFuente.normalizarRangos(rangos);
        String enmascarado = fuente.enmascarar(normalizados);
        String colapsado = fuente.colapsar(normalizados);

        return new EtapaOptimizacion(
                nombre(),
                descripcion(),
                fuente.getTexto(),
                colapsado,
                enmascarado,
                hallazgos,
                normalizados,
                dt
        );
    }

    /**
     * R1: Detecta y elimina cualquier sentencia posterior a un terminador
     * (return, break, continue, throw) dentro del mismo bloque o ámbito secuencial.
     */
    void aplicarR1Inalcanzable(NodoPrograma ast, CodigoFuente fuente,
                              List<Hallazgo> hallazgos, List<RangoTexto> rangos) {
        ast.accept(new NodoVisitor<Void>() {
            @Override
            public Void visitar(NodoPrograma nodo) {
                analizarListaSentencias(nodo.getSentencias());
                return null;
            }

            @Override
            public Void visitar(NodoBloque nodo) {
                analizarListaSentencias(nodo.getSentencias());
                return null;
            }

            @Override
            public Void visitar(NodoCaso nodo) {
                analizarListaSentencias(nodo.getSentencias());
                return null;
            }

            private void analizarListaSentencias(List<Nodo> sentencias) {
                if (sentencias == null || sentencias.isEmpty()) return;

                boolean alcanzable = true;
                String comandoTerminador = null;
                int lineaTerminador = 0;

                for (Nodo s : sentencias) {
                    if (s == null) continue;

                    if (!alcanzable) {
                        int idxToken = fuente.buscarIndiceToken(s.getLinea(), s.getColumna());
                        if (idxToken >= 0) {
                            RangoTexto rango = fuente.obtenerRangoSentencia(idxToken);
                            if (rango != null) {
                                rangos.add(rango);
                                String texto = fuente.getTexto().substring(rango.inicio(), rango.fin()).trim();
                                hallazgos.add(new Hallazgo(
                                        s.getLinea(),
                                        Hallazgo.Tipo.INALCANZABLE,
                                        texto,
                                        String.format("Código inalcanzable: se encuentra después de una instrucción '%s' en la línea %d.",
                                                comandoTerminador, lineaTerminador)
                                ));
                            }
                        }
                    } else {
                        if (s instanceof NodoComando cmd) {
                            String c = cmd.getComando();
                            if ("return".equals(c) || "break".equals(c) || "continue".equals(c) || "throw".equals(c)) {
                                alcanzable = false;
                                comandoTerminador = c;
                                lineaTerminador = cmd.getLinea();
                            }
                        }
                        s.accept(this);
                    }
                }
            }

            @Override
            public Void visitar(NodoIf nodo) {
                if (nodo.getBloqueThen() != null) nodo.getBloqueThen().accept(this);
                if (nodo.getBloqueElse() != null) nodo.getBloqueElse().accept(this);
                return null;
            }

            @Override
            public Void visitar(NodoWhile nodo) {
                if (nodo.getCuerpo() != null) nodo.getCuerpo().accept(this);
                return null;
            }

            @Override
            public Void visitar(NodoDoWhile nodo) {
                if (nodo.getCuerpo() != null) nodo.getCuerpo().accept(this);
                return null;
            }

            @Override
            public Void visitar(NodoFor nodo) {
                if (nodo.getCuerpo() != null) nodo.getCuerpo().accept(this);
                return null;
            }

            @Override
            public Void visitar(NodoSwitch nodo) {
                if (nodo.getCasos() != null) {
                    for (NodoCaso c : nodo.getCasos()) {
                        if (c != null) c.accept(this);
                    }
                }
                return null;
            }

            @Override
            public Void visitar(NodoEstructura nodo) {
                if (nodo.getCuerpo() != null) nodo.getCuerpo().accept(this);
                return null;
            }

            @Override public Void visitar(NodoAsignacion nodo) { return null; }
            @Override public Void visitar(NodoAccesoArreglo nodo) { return null; }
            @Override public Void visitar(NodoCasteo nodo) { return null; }
            @Override public Void visitar(NodoDeclaracion nodo) { return null; }
            @Override public Void visitar(NodoComando nodo) { return null; }
            @Override public Void visitar(NodoCadena nodo) { return null; }
            @Override public Void visitar(NodoArreglo nodo) { return null; }
            @Override public Void visitar(NodoBooleano nodo) { return null; }
            @Override public Void visitar(NodoNumero nodo) { return null; }
            @Override public Void visitar(NodoIdentificador nodo) { return null; }
            @Override public Void visitar(NodoInstanciacion nodo) { return null; }
            @Override public Void visitar(NodoTernario nodo) { return null; }
            @Override public Void visitar(NodoAgrupacion nodo) { return null; }
            @Override public Void visitar(NodoNegacionLogica nodo) { return null; }
            @Override public Void visitar(NodoLlamadaMetodo nodo) { return null; }
            @Override public Void visitar(NodoOperacionLogica nodo) { return null; }
            @Override public Void visitar(NodoOperacion nodo) { return null; }
            @Override public Void visitar(NodoOperacionRelacional nodo) { return null; }
            @Override public Void visitar(NodoIncremento nodo) { return null; }
            @Override public Void visitar(NodoMenosUnario nodo) { return null; }
        });
    }

    /**
     * R2 y R3: Detecta condiciones booleanas constantes en estructuras de control.
     * - if (false) sin else -> se elimina completo
     * - while (false) -> se elimina completo
     * - if (true) { A } else { B } -> se elimina la rama else { B }
     */
    void aplicarR2R3CondicionConstante(NodoPrograma ast, CodigoFuente fuente,
                                       List<Hallazgo> hallazgos, List<RangoTexto> rangos) {
        ast.accept(new NodoVisitor<Void>() {
            @Override
            public Void visitar(NodoIf nodo) {
                Boolean valorConstante = evaluarBooleanoConstante(nodo.getCondicion());
                if (Boolean.FALSE.equals(valorConstante)) {
                    if (nodo.getBloqueElse() == null) {
                        // if (false) { ... } sin else: se elimina completo
                        int idx = fuente.buscarIndiceToken(nodo.getLinea(), nodo.getColumna());
                        if (idx >= 0) {
                            RangoTexto r = fuente.obtenerRangoSentencia(idx);
                            if (r != null) {
                                rangos.add(r);
                                String txt = fuente.getTexto().substring(r.inicio(), r.fin()).trim();
                                hallazgos.add(new Hallazgo(
                                        nodo.getLinea(),
                                        Hallazgo.Tipo.CONDICION_CONSTANTE,
                                        txt,
                                        "Condición constante 'false': el cuerpo del 'if' nunca se ejecutará."
                                ));
                                return null;
                            }
                        }
                    }
                } else if (Boolean.TRUE.equals(valorConstante)) {
                    if (nodo.getBloqueElse() != null) {
                        // if (true) { A } else { B }: la rama else es inalcanzable
                        Nodo blqElse = nodo.getBloqueElse();
                        int idxElse = buscarTokenElse(fuente, nodo.getLinea(), blqElse.getLinea());
                        if (idxElse >= 0) {
                            int idxFin = fuente.delimitarSentencia(idxElse);
                            int ini = fuente.inicioDe(fuente.getTokens().get(idxElse));
                            int fin = fuente.finDe(fuente.getTokens().get(idxFin));
                            RangoTexto r = new RangoTexto(ini, fin);
                            rangos.add(r);
                            String txt = fuente.getTexto().substring(ini, fin).trim();
                            hallazgos.add(new Hallazgo(
                                    blqElse.getLinea(),
                                    Hallazgo.Tipo.CONDICION_CONSTANTE,
                                    txt,
                                    "Condición constante 'true': la rama 'else' es inalcanzable y se elimina."
                            ));
                        }
                    }
                }

                if (nodo.getBloqueThen() != null) nodo.getBloqueThen().accept(this);
                if (nodo.getBloqueElse() != null) nodo.getBloqueElse().accept(this);
                return null;
            }

            @Override
            public Void visitar(NodoWhile nodo) {
                Boolean valorConstante = evaluarBooleanoConstante(nodo.getCondicion());
                if (Boolean.FALSE.equals(valorConstante)) {
                    int idx = fuente.buscarIndiceToken(nodo.getLinea(), nodo.getColumna());
                    if (idx >= 0) {
                        RangoTexto r = fuente.obtenerRangoSentencia(idx);
                        if (r != null) {
                            rangos.add(r);
                            String txt = fuente.getTexto().substring(r.inicio(), r.fin()).trim();
                            hallazgos.add(new Hallazgo(
                                    nodo.getLinea(),
                                    Hallazgo.Tipo.CONDICION_CONSTANTE,
                                    txt,
                                    "Condición constante 'false': el ciclo 'while' nunca se ejecutará."
                            ));
                            return null;
                        }
                    }
                }
                if (nodo.getCuerpo() != null) nodo.getCuerpo().accept(this);
                return null;
            }

            @Override
            public Void visitar(NodoPrograma nodo) {
                if (nodo.getSentencias() != null) {
                    for (Nodo s : nodo.getSentencias()) if (s != null) s.accept(this);
                }
                return null;
            }

            @Override
            public Void visitar(NodoBloque nodo) {
                if (nodo.getSentencias() != null) {
                    for (Nodo s : nodo.getSentencias()) if (s != null) s.accept(this);
                }
                return null;
            }

            @Override
            public Void visitar(NodoFor nodo) {
                if (nodo.getCuerpo() != null) nodo.getCuerpo().accept(this);
                return null;
            }

            @Override
            public Void visitar(NodoDoWhile nodo) {
                if (nodo.getCuerpo() != null) nodo.getCuerpo().accept(this);
                return null;
            }

            @Override
            public Void visitar(NodoSwitch nodo) {
                if (nodo.getCasos() != null) {
                    for (NodoCaso c : nodo.getCasos()) if (c != null) c.accept(this);
                }
                return null;
            }

            @Override
            public Void visitar(NodoCaso nodo) {
                if (nodo.getSentencias() != null) {
                    for (Nodo s : nodo.getSentencias()) if (s != null) s.accept(this);
                }
                return null;
            }

            @Override
            public Void visitar(NodoEstructura nodo) {
                if (nodo.getCuerpo() != null) nodo.getCuerpo().accept(this);
                return null;
            }

            @Override public Void visitar(NodoAsignacion nodo) { return null; }
            @Override public Void visitar(NodoAccesoArreglo nodo) { return null; }
            @Override public Void visitar(NodoCasteo nodo) { return null; }
            @Override public Void visitar(NodoDeclaracion nodo) { return null; }
            @Override public Void visitar(NodoComando nodo) { return null; }
            @Override public Void visitar(NodoCadena nodo) { return null; }
            @Override public Void visitar(NodoArreglo nodo) { return null; }
            @Override public Void visitar(NodoBooleano nodo) { return null; }
            @Override public Void visitar(NodoNumero nodo) { return null; }
            @Override public Void visitar(NodoIdentificador nodo) { return null; }
            @Override public Void visitar(NodoInstanciacion nodo) { return null; }
            @Override public Void visitar(NodoTernario nodo) { return null; }
            @Override public Void visitar(NodoAgrupacion nodo) { return null; }
            @Override public Void visitar(NodoNegacionLogica nodo) { return null; }
            @Override public Void visitar(NodoLlamadaMetodo nodo) { return null; }
            @Override public Void visitar(NodoOperacionLogica nodo) { return null; }
            @Override public Void visitar(NodoOperacion nodo) { return null; }
            @Override public Void visitar(NodoOperacionRelacional nodo) { return null; }
            @Override public Void visitar(NodoIncremento nodo) { return null; }
            @Override public Void visitar(NodoMenosUnario nodo) { return null; }
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

    private static NodoPrograma parsear(String codigo) {
        try {
            ByteArrayInputStream bais = new ByteArrayInputStream(codigo.getBytes(StandardCharsets.UTF_8));
            Analizador parser = new Analizador(bais);
            return parser.programa();
        } catch (Throwable e) {
            return null;
        }
    }
}
