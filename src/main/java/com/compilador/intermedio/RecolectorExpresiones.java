package com.compilador.intermedio;

import com.compilador.ast.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Recorre el AST en orden secuencial para extraer todas las operaciones
 * aritméticas raíz, manteniendo al mismo tiempo un {@link EntornoValores}
 * con los valores de las variables conocidos hasta cada punto.
 */
public class RecolectorExpresiones implements NodoVisitor<Void> {

    private final EntornoValores entorno;
    private final GeneradorCuadruplos generadorCuadruplos;
    private final List<ResultadoExpresion> resultados;
    private final List<Cuadruplo> todosCuadruplos;

    public RecolectorExpresiones() {
        this.entorno = new EntornoValores();
        this.generadorCuadruplos = new GeneradorCuadruplos();
        this.resultados = new ArrayList<>();
        this.todosCuadruplos = new ArrayList<>();
    }

    public ProgramaIntermedio recolectar(NodoPrograma programa) {
        if (programa != null) {
            programa.accept(this);
        }
        return new ProgramaIntermedio(
                List.copyOf(resultados),
                List.copyOf(todosCuadruplos),
                entorno
        );
    }

    public EntornoValores getEntorno() {
        return entorno;
    }

    public List<ResultadoExpresion> getResultados() {
        return resultados;
    }

    public List<Cuadruplo> getTodosCuadruplos() {
        return todosCuadruplos;
    }

    // ── Visitantes del AST ─────────────────────────────────────────

    @Override
    public Void visitar(NodoPrograma nodo) {
        if (nodo.getSentencias() != null) {
            for (Nodo sentencia : nodo.getSentencias()) {
                if (sentencia != null) sentencia.accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visitar(NodoDeclaracion nodo) {
        String id = nodo.getIdentificador();
        String tipo = nodo.getTipoDato();
        entorno.declarar(id, tipo);

        Nodo init = nodo.getInicializacion();
        if (init != null) {
            if (ConversorNotacion.esAritmetica(init)) {
                procesarExpresionAritmetica(init, id, nodo.getLinea());
            } else {
                evaluarAsignacionSimple(id, init);
                visitarSubexpresiones(init);
            }
        }
        return null;
    }

    @Override
    public Void visitar(NodoAsignacion nodo) {
        String destino = nodo.getIdentificador();
        Nodo expr = nodo.getExpresion();

        if (ConversorNotacion.esAritmetica(expr)) {
            procesarExpresionAritmetica(expr, destino, nodo.getLinea());
        } else {
            if (destino != null) {
                evaluarAsignacionSimple(destino, expr);
            }
            visitarSubexpresiones(expr);
        }

        if (nodo.getDestino() != null && destino == null) {
            visitarSubexpresiones(nodo.getDestino());
        }
        return null;
    }

    @Override
    public Void visitar(NodoIncremento nodo) {
        String id = nodo.getIdentificador();
        if (id != null && entorno.conoce(id)) {
            Valor actual = entorno.obtener(id);
            try {
                Valor uno = Valor.entero(1);
                Valor nuevo = nodo.getOperador().contains("++")
                        ? actual.operar("+", uno)
                        : actual.operar("-", uno);
                entorno.asignar(id, nuevo);
            } catch (ErrorEvaluacion ignored) {
            }
        }
        return null;
    }

    @Override
    public Void visitar(NodoBloque nodo) {
        if (nodo.getSentencias() != null) {
            for (Nodo s : nodo.getSentencias()) {
                if (s != null) s.accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visitar(NodoIf nodo) {
        visitarSubexpresiones(nodo.getCondicion());
        if (nodo.getBloqueThen() != null) nodo.getBloqueThen().accept(this);
        if (nodo.getBloqueElse() != null) nodo.getBloqueElse().accept(this);
        return null;
    }

    @Override
    public Void visitar(NodoWhile nodo) {
        visitarSubexpresiones(nodo.getCondicion());
        if (nodo.getCuerpo() != null) nodo.getCuerpo().accept(this);
        return null;
    }

    @Override
    public Void visitar(NodoDoWhile nodo) {
        if (nodo.getCuerpo() != null) nodo.getCuerpo().accept(this);
        visitarSubexpresiones(nodo.getCondicion());
        return null;
    }

    @Override
    public Void visitar(NodoFor nodo) {
        if (nodo.getInicializacion() != null) nodo.getInicializacion().accept(this);
        visitarSubexpresiones(nodo.getCondicion());
        if (nodo.getCuerpo() != null) nodo.getCuerpo().accept(this);
        visitarSubexpresiones(nodo.getActualizacion());
        return null;
    }

    @Override
    public Void visitar(NodoSwitch nodo) {
        visitarSubexpresiones(nodo.getExpresion());
        if (nodo.getCasos() != null) {
            for (NodoCaso c : nodo.getCasos()) {
                if (c != null) c.accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visitar(NodoCaso nodo) {
        visitarSubexpresiones(nodo.getValor());
        if (nodo.getSentencias() != null) {
            for (Nodo s : nodo.getSentencias()) {
                if (s != null) s.accept(this);
            }
        }
        return null;
    }

    @Override
    public Void visitar(NodoComando nodo) {
        visitarSubexpresiones(nodo.getArgumento());
        return null;
    }

    @Override
    public Void visitar(NodoEstructura nodo) {
        if (nodo.getCuerpo() != null) {
            nodo.getCuerpo().accept(this);
        }
        return null;
    }

    @Override
    public Void visitar(NodoAccesoArreglo nodo) {
        visitarSubexpresiones(nodo);
        return null;
    }

    @Override
    public Void visitar(NodoCasteo nodo) {
        visitarSubexpresiones(nodo);
        return null;
    }

    @Override
    public Void visitar(NodoCadena nodo) {
        return null;
    }

    @Override
    public Void visitar(NodoArreglo nodo) {
        return null;
    }

    @Override
    public Void visitar(NodoBooleano nodo) {
        return null;
    }

    @Override
    public Void visitar(NodoNumero nodo) {
        return null;
    }

    @Override
    public Void visitar(NodoIdentificador nodo) {
        return null;
    }

    @Override
    public Void visitar(NodoInstanciacion nodo) {
        visitarSubexpresiones(nodo);
        return null;
    }

    @Override
    public Void visitar(NodoTernario nodo) {
        visitarSubexpresiones(nodo);
        return null;
    }

    @Override
    public Void visitar(NodoAgrupacion nodo) {
        visitarSubexpresiones(nodo);
        return null;
    }

    @Override
    public Void visitar(NodoNegacionLogica nodo) {
        visitarSubexpresiones(nodo);
        return null;
    }

    @Override
    public Void visitar(NodoLlamadaMetodo nodo) {
        visitarSubexpresiones(nodo);
        return null;
    }

    @Override
    public Void visitar(NodoOperacionLogica nodo) {
        visitarSubexpresiones(nodo);
        return null;
    }

    @Override
    public Void visitar(NodoOperacion nodo) {
        // Operación aritmética suelta que apareció como sentencia
        procesarExpresionAritmetica(nodo, null, nodo.getLinea());
        return null;
    }

    @Override
    public Void visitar(NodoOperacionRelacional nodo) {
        visitarSubexpresiones(nodo);
        return null;
    }

    @Override
    public Void visitar(NodoMenosUnario nodo) {
        procesarExpresionAritmetica(nodo, null, nodo.getLinea());
        return null;
    }

    // ── Lógica de extracción y evaluación ──────────────────────────

    /**
     * Procesa una expresión aritmética raíz detectada en el AST:
     * - Obtiene infijo, prefijo y posfijo
     * - Genera cuádruplos con temporales
     * - Evalúa con cuádruplos y con la pila posfija
     * - Actualiza el entorno si hay destino
     */
    public void procesarExpresionAritmetica(Nodo expr, String destino, int linea) {
        String infijo = ConversorNotacion.infijo(expr);
        String prefijo = ConversorNotacion.prefijo(expr);
        String posfijo = ConversorNotacion.posfijo(expr);
        List<ElementoPolaco> posfijoElem = ConversorNotacion.posfijoElementos(expr);

        List<Cuadruplo> cuadruplos = generadorCuadruplos.generar(expr, destino);
        todosCuadruplos.addAll(cuadruplos);

        Valor resCuadruplos = null;
        String error = null;
        try {
            resCuadruplos = EvaluadorCuadruplos.ejecutar(cuadruplos, entorno);
        } catch (ErrorEvaluacion e) {
            error = e.getMessage();
        }

        Valor resPosfijo = null;
        List<String> traza = List.of();
        try {
            EvaluadorPosfijo.Resultado rPos = EvaluadorPosfijo.evaluar(posfijoElem, entorno);
            resPosfijo = rPos.valor();
            traza = rPos.traza();
        } catch (ErrorEvaluacion e) {
            if (error == null) error = e.getMessage();
        }

        Valor resultadoFinal = resCuadruplos != null ? resCuadruplos : resPosfijo;
        if (resultadoFinal != null && destino != null) {
            entorno.asignar(destino, resultadoFinal);
        } else if (destino != null && error != null) {
            entorno.olvidar(destino);
        }

        ResultadoExpresion resultado = new ResultadoExpresion(
                linea,
                destino,
                infijo,
                prefijo,
                posfijo,
                cuadruplos,
                resultadoFinal,
                error,
                traza
        );
        resultados.add(resultado);
    }

    /**
     * Inspecciona nodos compuestos para extraer cualquier expresión aritmética
     * raíz contenida dentro (ej: en argumentos de llamadas, índices, etc.).
     */
    public void visitarSubexpresiones(Nodo nodo) {
        if (nodo == null) return;

        if (ConversorNotacion.esAritmetica(nodo)) {
            procesarExpresionAritmetica(nodo, null, nodo.getLinea());
            return;
        }

        switch (nodo) {
            case NodoAgrupacion g -> visitarSubexpresiones(g.getExpresion());
            case NodoOperacionRelacional rel -> {
                visitarSubexpresiones(rel.getIzquierdo());
                visitarSubexpresiones(rel.getDerecho());
            }
            case NodoOperacionLogica log -> {
                visitarSubexpresiones(log.getIzquierdo());
                visitarSubexpresiones(log.getDerecho());
            }
            case NodoNegacionLogica neg -> visitarSubexpresiones(neg.getExpresion());
            case NodoTernario ter -> {
                visitarSubexpresiones(ter.getCondicion());
                visitarSubexpresiones(ter.getVerdadero());
                visitarSubexpresiones(ter.getFalso());
            }
            case NodoCasteo cas -> visitarSubexpresiones(cas.getExpresion());
            case NodoAccesoArreglo arr -> {
                visitarSubexpresiones(arr.getArreglo());
                visitarSubexpresiones(arr.getIndice());
            }
            case NodoLlamadaMetodo m -> {
                if (m.getArgumentos() != null) {
                    for (Nodo arg : m.getArgumentos()) {
                        visitarSubexpresiones(arg);
                    }
                }
            }
            case NodoInstanciacion inst -> {
                if (inst.getArgumentos() != null) {
                    for (Nodo arg : inst.getArgumentos()) {
                        visitarSubexpresiones(arg);
                    }
                }
                if (inst.getIndiceArreglo() != null) {
                    visitarSubexpresiones(inst.getIndiceArreglo());
                }
            }
            default -> {
            }
        }
    }

    /**
     * Evalúa inicializaciones o asignaciones no aritméticas directas
     * (números literales o copias de variables conocidas).
     */
    private void evaluarAsignacionSimple(String id, Nodo nodo) {
        if (id == null || nodo == null) return;
        Nodo n = ConversorNotacion.sinAgrupacion(nodo);
        switch (n) {
            case NodoNumero num -> {
                try {
                    entorno.asignar(id, Valor.desdeLiteral(num.getValor()));
                } catch (ErrorEvaluacion e) {
                    entorno.olvidar(id);
                }
            }
            case NodoIdentificador origen -> {
                if (entorno.conoce(origen.getNombre())) {
                    entorno.asignar(id, entorno.obtener(origen.getNombre()));
                } else {
                    entorno.olvidar(id);
                }
            }
            case NodoCasteo c -> {
                Nodo sub = ConversorNotacion.sinAgrupacion(c.getExpresion());
                if (sub instanceof NodoNumero num) {
                    try {
                        entorno.asignar(id, Valor.desdeLiteral(num.getValor()));
                    } catch (ErrorEvaluacion e) {
                        entorno.olvidar(id);
                    }
                } else if (sub instanceof NodoIdentificador origen && entorno.conoce(origen.getNombre())) {
                    entorno.asignar(id, entorno.obtener(origen.getNombre()));
                } else {
                    entorno.olvidar(id);
                }
            }
            default -> entorno.olvidar(id);
        }
    }
}
