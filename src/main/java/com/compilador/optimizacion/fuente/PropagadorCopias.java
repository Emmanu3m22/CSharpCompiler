package com.compilador.optimizacion.fuente;

import com.compilador.Analizador;
import com.compilador.AnalizadorConstants;
import com.compilador.Token;
import com.compilador.ast.*;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.*;

/**
 * Propaga copias locales de variables dentro de un mismo bloque.
 *
 * Solo propaga asignaciones entre variables del mismo tipo y conserva la copia
 * cuando no puede demostrar que todas sus lecturas fueron reemplazadas.
 */
public class PropagadorCopias implements PaseOptimizacionFuente {

    private record Reemplazo(RangoTexto rango, String texto) {}

    private static final class Copia {
        private final String nombre;
        private final String original;
        private final RangoTexto rango;
        private final int linea;
        private boolean escrita;
        private boolean propagada;

        private Copia(
                String nombre,
                String original,
                RangoTexto rango,
                int linea
        ) {
            this.nombre = nombre;
            this.original = original;
            this.rango = rango;
            this.linea = linea;
        }
    }

    @Override
    public String nombre() {
        return "Propagación de Copias";
    }

    @Override
    public String descripcion() {
        return "Sustituye usos de copias locales por su variable original cuando no hay modificaciones intermedias.";
    }

    @Override
    public EtapaOptimizacion aplicar(CodigoFuente fuente) {
        if (fuente == null || fuente.getTexto().trim().isEmpty()) {
            return EtapaOptimizacion.sinCambios(nombre(), descripcion(), "", 0L);
        }

        long inicio = System.nanoTime();
        NodoPrograma ast = parsear(fuente.getTexto());
        if (ast == null) {
            return EtapaOptimizacion.sinCambios(
                    nombre(), descripcion(), fuente.getTexto(), System.nanoTime() - inicio);
        }

        List<Reemplazo> reemplazos = new ArrayList<>();
        List<Copia> copias = new ArrayList<>();
        procesarSentencias(ast.getSentencias(), new HashMap<>(), fuente, reemplazos, copias);

        List<RangoTexto> rangosReemplazados = reemplazos.stream()
                .map(Reemplazo::rango)
                .toList();
        RangosMarcados lecturasReemplazadas = new RangosMarcados();
        rangosReemplazados.forEach(lecturasReemplazadas::agregar);
        IndiceLecturas lecturas = AnalizadorUso.indexarLecturas(fuente);

        List<RangoTexto> eliminaciones = new ArrayList<>();
        List<Hallazgo> hallazgos = new ArrayList<>();
        for (Copia copia : copias) {
            if (!copia.propagada) continue;

            boolean eliminarCopia = !copia.escrita
                    && !lecturas.estaLeida(copia.nombre, lecturasReemplazadas);
            if (eliminarCopia) {
                eliminaciones.add(copia.rango);
            }

            hallazgos.add(new Hallazgo(
                    copia.linea,
                    Hallazgo.Tipo.PROPAGACION_COPIAS,
                    fuente.getTexto().substring(copia.rango.inicio(), copia.rango.fin()).trim(),
                    eliminarCopia
                            ? String.format("Los usos de '%s' se sustituyeron por '%s' y se eliminó su asignación.",
                                    copia.nombre, copia.original)
                            : String.format("Se propagaron los usos seguros de '%s' como '%s'; se conservó la declaración porque aún puede ser necesaria.",
                                    copia.nombre, copia.original)));
        }

        if (hallazgos.isEmpty()) {
            return EtapaOptimizacion.sinCambios(
                    nombre(), descripcion(), fuente.getTexto(), System.nanoTime() - inicio);
        }

        String transformado = aplicarEdiciones(fuente.getTexto(), reemplazos, eliminaciones);
        String codigoDespues = CodigoFuente.limpiarLineasVaciasExcessivas(
                CodigoFuente.quitarComentarios(transformado));
        long duracion = System.nanoTime() - inicio;
        return new EtapaOptimizacion(
                nombre(),
                descripcion(),
                fuente.getTexto(),
                codigoDespues,
                transformado,
                hallazgos,
                CodigoFuente.normalizarRangos(eliminaciones),
                duracion);
    }

    private static NodoPrograma parsear(String codigo) {
        try {
            Analizador parser = new Analizador(
                    new ByteArrayInputStream(codigo.getBytes(StandardCharsets.UTF_8)));
            return parser.programa();
        } catch (Exception e) {
            return null;
        }
    }

    private static void procesarSentencias(
            List<Nodo> sentencias,
            Map<String, String> simbolos,
            CodigoFuente fuente,
            List<Reemplazo> reemplazos,
            List<Copia> copias
    ) {
        Map<String, Copia> activas = new HashMap<>();
        if (sentencias == null) return;

        for (Nodo sentencia : sentencias) {
            if (sentencia == null) continue;
            if (esSentenciaSimple(sentencia)) {
                List<NodoIdentificador> usos = new ArrayList<>();
                recolectarUsos(sentencia, usos);
                for (NodoIdentificador uso : usos) {
                    Copia copia = activas.get(uso.getNombre());
                    if (copia == null || esArgumentoPorReferencia(fuente, uso)) continue;
                    Token token = tokenEn(fuente, uso.getLinea(), uso.getColumna());
                    if (token == null || !token.image.equals(copia.nombre)) continue;
                    int inicio = fuente.inicioDe(token);
                    int fin = fuente.finDe(token);
                    reemplazos.add(new Reemplazo(new RangoTexto(inicio, fin), copia.original));
                    copia.propagada = true;
                }

                Set<String> escrituras = obtenerEscrituras(sentencia, fuente);
                for (String nombre : escrituras) {
                    Copia escrita = buscarCopia(copias, nombre);
                    if (escrita != null) escrita.escrita = true;
                    activas.entrySet().removeIf(entrada ->
                            entrada.getKey().equals(nombre)
                                    || entrada.getValue().original.equals(nombre));
                }

                if (sentencia instanceof NodoDeclaracion declaracion) {
                    registrarDeclaracion(declaracion, simbolos, activas, fuente, copias);
                } else if (sentencia instanceof NodoAsignacion asignacion) {
                    registrarAsignacion(asignacion, simbolos, activas, fuente, copias);
                }
            } else {
                activas.clear();
                procesarBloquesAnidados(sentencia, fuente, reemplazos, copias);
            }
        }
    }

    private static boolean esSentenciaSimple(Nodo nodo) {
        if (nodo instanceof NodoDeclaracion || nodo instanceof NodoAsignacion
                || nodo instanceof NodoIncremento || nodo instanceof NodoLlamadaMetodo) {
            return true;
        }
        return nodo instanceof NodoComando comando
                && !"try".equals(comando.getComando())
                && !(comando.getArgumento() instanceof NodoBloque);
    }

    private static void registrarDeclaracion(
            NodoDeclaracion declaracion,
            Map<String, String> simbolos,
            Map<String, Copia> activas,
            CodigoFuente fuente,
            List<Copia> copias
    ) {
        String nombre = declaracion.getIdentificador();
        String tipo = declaracion.getTipoDato();
        Nodo inicializacion = declaracion.getInicializacion();
        int indice = fuente.buscarIndiceToken(declaracion.getLinea(), declaracion.getColumna());

        if (inicializacion instanceof NodoIdentificador origen
                && origen.getNombre() != null
                && simbolos.containsKey(origen.getNombre())
                && tipo != null
                && !tipo.equalsIgnoreCase("var")
                && tipo.equals(simbolos.get(origen.getNombre()))
                && !activas.containsKey(origen.getNombre())
                && !hayModificadorPrevio(fuente, indice)) {
            int indiceInicio = fuente.buscarIndiceToken(declaracion.getLinea(), declaracion.getColumna());
            RangoTexto rango = fuente.obtenerRangoSentencia(indiceInicio);
            if (rango != null) {
                Copia copia = new Copia(nombre, origen.getNombre(), rango, declaracion.getLinea());
                copias.add(copia);
                activas.put(nombre, copia);
            }
        }

        if (nombre != null && tipo != null) {
            simbolos.put(nombre, tipo);
        }
    }

    private static void registrarAsignacion(
            NodoAsignacion asignacion,
            Map<String, String> simbolos,
            Map<String, Copia> activas,
            CodigoFuente fuente,
            List<Copia> copias
    ) {
        if (!(asignacion.getDestino() instanceof NodoIdentificador destino)
                || !(asignacion.getExpresion() instanceof NodoIdentificador origen)) {
            return;
        }

        String tipoDestino = simbolos.get(destino.getNombre());
        String tipoOrigen = simbolos.get(origen.getNombre());
        if (tipoDestino == null
                || !tipoDestino.equals(tipoOrigen)
                || tipoDestino.equalsIgnoreCase("var")
                || destino.getNombre().equals(origen.getNombre())
                || activas.containsKey(origen.getNombre())) {
            return;
        }

        int indice = fuente.buscarIndiceToken(asignacion.getLinea(), asignacion.getColumna());
        RangoTexto rango = fuente.obtenerRangoSentencia(indice);
        if (rango == null) return;

        Copia copia = new Copia(
                destino.getNombre(), origen.getNombre(), rango, asignacion.getLinea());
        copias.add(copia);
        activas.put(copia.nombre, copia);
    }

    private static boolean hayModificadorPrevio(CodigoFuente fuente, int indiceTipo) {
        if (indiceTipo <= 0) return false;
        int kind = fuente.getTokens().get(indiceTipo - 1).kind;
        return kind == AnalizadorConstants.CONST
                || kind == AnalizadorConstants.READONLY
                || kind == AnalizadorConstants.STATIC
                || kind == AnalizadorConstants.PUBLIC
                || kind == AnalizadorConstants.PRIVATE
                || kind == AnalizadorConstants.PROTECTED
                || kind == AnalizadorConstants.INTERNAL;
    }

    private static Set<String> obtenerEscrituras(Nodo sentencia, CodigoFuente fuente) {
        Set<String> escrituras = new HashSet<>();
        if (sentencia instanceof NodoAsignacion asignacion) {
            String destino = asignacion.getIdentificador();
            if (destino == null && asignacion.getDestino() instanceof NodoAccesoArreglo acceso) {
                destino = acceso.getIdentificador();
            }
            if (destino != null) escrituras.add(destino);
        } else if (sentencia instanceof NodoIncremento incremento) {
            if (incremento.getIdentificador() != null) {
                escrituras.add(incremento.getIdentificador());
            }
        }

        List<NodoIdentificador> usos = new ArrayList<>();
        recolectarUsos(sentencia, usos);
        for (NodoIdentificador uso : usos) {
            if (esArgumentoPorReferencia(fuente, uso)) {
                escrituras.add(uso.getNombre());
            }
        }
        return escrituras;
    }

    private static Copia buscarCopia(List<Copia> copias, String nombre) {
        for (int i = copias.size() - 1; i >= 0; i--) {
            if (copias.get(i).nombre.equals(nombre)) return copias.get(i);
        }
        return null;
    }

    private static boolean esArgumentoPorReferencia(CodigoFuente fuente, NodoIdentificador uso) {
        int indice = fuente.buscarIndiceToken(uso.getLinea(), uso.getColumna());
        if (indice <= 0 || indice >= fuente.getTokens().size()) return false;
        int anterior = fuente.getTokens().get(indice - 1).kind;
        return anterior == AnalizadorConstants.REF || anterior == AnalizadorConstants.OUT;
    }

    private static Token tokenEn(CodigoFuente fuente, int linea, int columna) {
        int indice = fuente.buscarIndiceToken(linea, columna);
        if (indice < 0 || indice >= fuente.getTokens().size()) return null;
        Token token = fuente.getTokens().get(indice);
        return token.beginLine == linea && token.beginColumn == columna ? token : null;
    }

    private static void recolectarUsos(Nodo nodo, List<NodoIdentificador> usos) {
        if (nodo == null) return;
        if (nodo instanceof NodoIdentificador identificador) {
            usos.add(identificador);
        } else if (nodo instanceof NodoDeclaracion declaracion) {
            recolectarUsos(declaracion.getInicializacion(), usos);
        } else if (nodo instanceof NodoAsignacion asignacion) {
            recolectarUsos(asignacion.getExpresion(), usos);
        } else if (nodo instanceof NodoComando comando) {
            recolectarUsos(comando.getArgumento(), usos);
        } else if (nodo instanceof NodoOperacion operacion) {
            recolectarUsos(operacion.getIzquierdo(), usos);
            recolectarUsos(operacion.getDerecho(), usos);
        } else if (nodo instanceof NodoOperacionLogica operacion) {
            recolectarUsos(operacion.getIzquierdo(), usos);
            recolectarUsos(operacion.getDerecho(), usos);
        } else if (nodo instanceof NodoOperacionRelacional operacion) {
            recolectarUsos(operacion.getIzquierdo(), usos);
            recolectarUsos(operacion.getDerecho(), usos);
        } else if (nodo instanceof NodoAgrupacion agrupacion) {
            recolectarUsos(agrupacion.getExpresion(), usos);
        } else if (nodo instanceof NodoCasteo casteo) {
            recolectarUsos(casteo.getExpresion(), usos);
        } else if (nodo instanceof NodoNegacionLogica negacion) {
            recolectarUsos(negacion.getExpresion(), usos);
        } else if (nodo instanceof NodoMenosUnario menos) {
            recolectarUsos(menos.getExpresion(), usos);
        } else if (nodo instanceof NodoTernario ternario) {
            recolectarUsos(ternario.getCondicion(), usos);
            recolectarUsos(ternario.getVerdadero(), usos);
            recolectarUsos(ternario.getFalso(), usos);
        } else if (nodo instanceof NodoAccesoArreglo acceso) {
            recolectarUsos(acceso.getArreglo(), usos);
            recolectarUsos(acceso.getIndice(), usos);
        } else if (nodo instanceof NodoLlamadaMetodo llamada) {
            if (llamada.getArgumentos() != null) {
                llamada.getArgumentos().forEach(argumento -> recolectarUsos(argumento, usos));
            }
        } else if (nodo instanceof NodoInstanciacion instanciacion) {
            if (instanciacion.getArgumentos() != null) {
                instanciacion.getArgumentos().forEach(argumento -> recolectarUsos(argumento, usos));
            }
            recolectarUsos(instanciacion.getIndiceArreglo(), usos);
        }
    }

    private static void procesarBloquesAnidados(
            Nodo nodo,
            CodigoFuente fuente,
            List<Reemplazo> reemplazos,
            List<Copia> copias
    ) {
        if (nodo instanceof NodoEstructura estructura) {
            procesarEstructura(estructura, fuente, reemplazos, copias);
        } else if (nodo instanceof NodoIf condicional) {
            procesarBloque(condicional.getBloqueThen(), Map.of(), fuente, reemplazos, copias);
            procesarNodoAnidado(condicional.getBloqueElse(), fuente, reemplazos, copias);
        } else if (nodo instanceof NodoWhile ciclo) {
            procesarBloque(ciclo.getCuerpo(), Map.of(), fuente, reemplazos, copias);
        } else if (nodo instanceof NodoDoWhile ciclo) {
            procesarBloque(ciclo.getCuerpo(), Map.of(), fuente, reemplazos, copias);
        } else if (nodo instanceof NodoFor ciclo) {
            procesarBloque(ciclo.getCuerpo(), Map.of(), fuente, reemplazos, copias);
        } else if (nodo instanceof NodoSwitch seleccion) {
            if (seleccion.getCasos() != null) {
                for (NodoCaso caso : seleccion.getCasos()) {
                    if (caso != null) {
                        procesarSentencias(caso.getSentencias(), new HashMap<>(), fuente, reemplazos, copias);
                    }
                }
            }
        }
    }

    private static void procesarNodoAnidado(
            Nodo nodo,
            CodigoFuente fuente,
            List<Reemplazo> reemplazos,
            List<Copia> copias
    ) {
        if (nodo instanceof NodoBloque bloque) {
            procesarBloque(bloque, Map.of(), fuente, reemplazos, copias);
        } else if (nodo != null) {
            procesarBloquesAnidados(nodo, fuente, reemplazos, copias);
        }
    }

    private static void procesarBloque(
            Nodo nodo,
            Map<String, String> parametros,
            CodigoFuente fuente,
            List<Reemplazo> reemplazos,
            List<Copia> copias
    ) {
        if (nodo instanceof NodoBloque bloque) {
            procesarSentencias(bloque.getSentencias(), new HashMap<>(parametros), fuente, reemplazos, copias);
        } else if (nodo != null) {
            procesarNodoAnidado(nodo, fuente, reemplazos, copias);
        }
    }

    private static void procesarEstructura(
            NodoEstructura estructura,
            CodigoFuente fuente,
            List<Reemplazo> reemplazos,
            List<Copia> copias
    ) {
        String tipo = estructura.getTipoEstructura();
        NodoBloque cuerpo = estructura.getCuerpo();
        if (cuerpo == null) return;

        if ("method".equals(tipo) || "constructor".equals(tipo) || "operator".equals(tipo)) {
            Map<String, String> parametros = new HashMap<>();
            for (ParametroMetodo parametro : estructura.getParametros()) {
                if (parametro != null && parametro.getNombre() != null && parametro.getTipo() != null) {
                    parametros.put(parametro.getNombre(), parametro.getTipo());
                }
            }
            procesarBloque(cuerpo, parametros, fuente, reemplazos, copias);
        } else if ("class".equals(tipo) || "interface".equals(tipo) || "namespace".equals(tipo)) {
            for (Nodo miembro : cuerpo.getSentencias()) {
                if (miembro instanceof NodoEstructura anidada) {
                    procesarEstructura(anidada, fuente, reemplazos, copias);
                }
            }
        }
    }

    private static String aplicarEdiciones(
            String codigo,
            List<Reemplazo> reemplazos,
            List<RangoTexto> eliminaciones
    ) {
        List<Reemplazo> ediciones = new ArrayList<>(reemplazos);
        for (RangoTexto eliminacion : eliminaciones) {
            ediciones.add(new Reemplazo(eliminacion, ""));
        }
        ediciones.sort(Comparator.comparingInt(edicion -> edicion.rango().inicio()));

        StringBuilder resultado = new StringBuilder(codigo.length());
        int cursor = 0;
        for (Reemplazo edicion : ediciones) {
            RangoTexto rango = edicion.rango();
            if (rango.inicio() < cursor) continue;
            resultado.append(codigo, cursor, rango.inicio());
            resultado.append(edicion.texto());
            cursor = rango.fin();
        }
        resultado.append(codigo, cursor, codigo.length());
        return resultado.toString();
    }

}
