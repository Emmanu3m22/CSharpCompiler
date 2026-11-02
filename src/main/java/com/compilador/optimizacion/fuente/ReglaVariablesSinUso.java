package com.compilador.optimizacion.fuente;

import com.compilador.ast.*;

import java.util.*;

/**
 * R4: elimina variables locales declaradas o asignadas que nunca se leen.
 *
 * Usa un ciclo de punto fijo: si {@code b} dependía de {@code a} y {@code b} se elimina,
 * {@code a} pasa a ser candidata. El índice de lecturas se calcula una sola vez y cada
 * iteración solo consulta qué lecturas siguen "vivas" fuera de los rangos ya marcados.
 *
 * Reglas de seguridad:
 * <ul>
 *   <li>No se tocan campos de clase (solo variables dentro de métodos/constructores/operadores).</li>
 *   <li>No se eliminan asignaciones a parámetros (podrían ser ref/out o visibles al llamador).</li>
 *   <li>No se elimina ninguna sentencia con efectos secundarios (llamadas, new, ++, --).</li>
 *   <li>Una asignación que no es candidata (p. ej. dentro de un try) cuenta como lectura.</li>
 * </ul>
 */
public class ReglaVariablesSinUso implements ReglaCodigoMuerto {

    @Override
    public String id() {
        return "R4";
    }

    @Override
    public void aplicar(NodoPrograma ast, CodigoFuente fuente, List<Hallazgo> hallazgos, RangosMarcados rangos) {
        List<Candidato> candidatos = recolectarCandidatos(ast);

        Set<Integer> posicionesCandidatas = new HashSet<>();
        for (Candidato c : candidatos) {
            int idx = fuente.buscarIndiceToken(c.linea, c.columna);
            if (idx >= 0) posicionesCandidatas.add(fuente.inicioDe(fuente.getTokens().get(idx)));
        }
        IndiceLecturas lecturas = AnalizadorUso.indexarLecturas(fuente, posicionesCandidatas);

        Set<Integer> eliminados = new HashSet<>();
        boolean huboCambios = true;
        while (huboCambios) {
            huboCambios = false;
            for (int i = 0; i < candidatos.size(); i++) {
                if (eliminados.contains(i)) continue;
                Candidato cand = candidatos.get(i);
                if (lecturas.estaLeida(cand.nombre, rangos)) continue;

                int idxToken = fuente.buscarIndiceToken(cand.linea, cand.columna);
                if (idxToken < 0) continue;
                int idxFin = fuente.delimitarSentencia(idxToken);
                if (AnalizadorUso.tieneEfectosSecundarios(fuente, idxToken, idxFin)) continue;

                RangoTexto rango = fuente.obtenerRangoSentencia(idxToken);
                if (rango == null || rangos.contiene(rango.inicio())) continue;

                rangos.agregar(rango);
                eliminados.add(i);
                huboCambios = true;
                String texto = fuente.getTexto().substring(rango.inicio(), rango.fin()).trim();
                hallazgos.add(new Hallazgo(
                        cand.linea,
                        Hallazgo.Tipo.VARIABLE_SIN_USO,
                        texto,
                        String.format("La variable '%s' se %s pero nunca se lee en el programa.",
                                cand.nombre, cand.esDeclaracion ? "declara" : "asigna")));
            }
        }
    }

    private record Candidato(String nombre, int linea, int columna, boolean esDeclaracion) {}

    private static List<Candidato> recolectarCandidatos(NodoPrograma ast) {
        List<Candidato> lista = new ArrayList<>();
        ast.accept(new RecorridoAST() {
            private int profundidadClase = 0;
            private int profundidadMetodo = 0;
            private final Deque<Set<String>> parametros = new ArrayDeque<>();

            @Override
            public Void visitar(NodoPrograma nodo) {
                sentencias(nodo.getSentencias());
                return null;
            }

            @Override
            public Void visitar(NodoBloque nodo) {
                sentencias(nodo.getSentencias());
                return null;
            }

            @Override
            public Void visitar(NodoCaso nodo) {
                sentencias(nodo.getSentencias());
                return null;
            }

            private void sentencias(List<Nodo> sentencias) {
                if (sentencias == null) return;
                boolean esMiembroDeClase = profundidadClase > 0 && profundidadMetodo == 0;
                Set<String> params = parametros.isEmpty() ? Set.of() : parametros.peek();
                for (Nodo s : sentencias) {
                    if (s == null) continue;
                    if (esMiembroDeClase && (s instanceof NodoDeclaracion || s instanceof NodoAsignacion)) {
                        continue;
                    }
                    if (s instanceof NodoDeclaracion decl && decl.getIdentificador() != null) {
                        lista.add(new Candidato(decl.getIdentificador(), decl.getLinea(), decl.getColumna(), true));
                    } else if (s instanceof NodoAsignacion asig && asig.getIdentificador() != null) {
                        if (!params.contains(asig.getIdentificador())) {
                            lista.add(new Candidato(asig.getIdentificador(), asig.getLinea(), asig.getColumna(), false));
                        }
                    } else {
                        s.accept(this);
                    }
                }
            }

            @Override
            public Void visitar(NodoEstructura nodo) {
                String tipo = nodo.getTipoEstructura();
                boolean ejecutable = "method".equals(tipo) || "constructor".equals(tipo) || "operator".equals(tipo);
                boolean contenedor = "class".equals(tipo) || "namespace".equals(tipo) || "interface".equals(tipo);
                if (nodo.getCuerpo() == null || !(ejecutable || contenedor)) {
                    return null;
                }
                if (ejecutable) {
                    profundidadMetodo++;
                    Set<String> nombres = new HashSet<>();
                    if (nodo.getParametros() != null) {
                        for (ParametroMetodo p : nodo.getParametros()) {
                            if (p != null && p.getNombre() != null) nombres.add(p.getNombre());
                        }
                    }
                    parametros.push(nombres);
                } else {
                    profundidadClase++;
                }
                try {
                    nodo.getCuerpo().accept(this);
                } finally {
                    if (ejecutable) {
                        profundidadMetodo--;
                        parametros.pop();
                    } else {
                        profundidadClase--;
                    }
                }
                return null;
            }
        });
        return lista;
    }
}
