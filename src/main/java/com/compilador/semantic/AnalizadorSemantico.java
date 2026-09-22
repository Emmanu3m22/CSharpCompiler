package com.compilador.semantic;

import com.compilador.ast.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Analizador semántico del compilador.
 * Recorre el AST generado por el sintáctico y aplica validaciones:
 * - Variables declaradas antes de uso
 * - Tipos compatibles en operaciones
 * - No redeclaración de variables en el mismo ámbito
 *
 * NOTA: Este es un esqueleto funcional. El equipo semántico completará
 * las validaciones específicas del lenguaje.
 */
public class AnalizadorSemantico implements NodoVisitor<Void> {

    private final TablaSimbolos tablaSimbolos;
    private final List<ErrorSemantico> errores;

    public AnalizadorSemantico() {
        this.tablaSimbolos = new TablaSimbolos();
        this.errores = new ArrayList<>();
    }

    /**
     * Punto de entrada: analiza un programa completo.
     * @param programa El nodo raíz del AST
     * @return Lista de errores semánticos encontrados (vacía si no hay errores)
     */
    public List<ErrorSemantico> analizar(NodoPrograma programa) {
        errores.clear();
        programa.accept(this);
        return errores;
    }

    /**
     * Recorre un programa mediante el doble despacho de cada nodo.
     */
    @Override
    public Void visitar(NodoPrograma nodo) {
        visitarHijos(nodo.getSentencias());
        return null;
    }

    @Override
    public Void visitar(NodoDeclaracion nodo) {
        analizarDeclaracion(nodo);
        visitarHijo(nodo.getInicializacion());
        return null;
    }

    @Override
    public Void visitar(NodoAsignacion nodo) {
        analizarAsignacion(nodo);
        visitarHijo(nodo.getDestino());
        visitarHijo(nodo.getExpresion());
        return null;
    }

    @Override
    public Void visitar(NodoComando nodo) {
        analizarComando(nodo);
        visitarHijo(nodo.getArgumento());
        return null;
    }

    @Override
    public Void visitar(NodoBloque nodo) {
        visitarHijos(nodo.getSentencias());
        return null;
    }

    @Override
    public Void visitar(NodoAccesoArreglo nodo) {
        visitarHijo(nodo.getArreglo());
        visitarHijo(nodo.getIndice());
        return null;
    }

    @Override
    public Void visitar(NodoCasteo nodo) {
        visitarHijo(nodo.getExpresion());
        return null;
    }

    @Override
    public Void visitar(NodoAgrupacion nodo) {
        visitarHijo(nodo.getExpresion());
        return null;
    }

    @Override
    public Void visitar(NodoNegacionLogica nodo) {
        visitarHijo(nodo.getExpresion());
        return null;
    }

    @Override
    public Void visitar(NodoOperacion nodo) {
        visitarHijo(nodo.getIzquierdo());
        visitarHijo(nodo.getDerecho());
        return null;
    }

    @Override
    public Void visitar(NodoOperacionLogica nodo) {
        visitarHijo(nodo.getIzquierdo());
        visitarHijo(nodo.getDerecho());
        return null;
    }

    @Override
    public Void visitar(NodoOperacionRelacional nodo) {
        visitarHijo(nodo.getIzquierdo());
        visitarHijo(nodo.getDerecho());
        return null;
    }

    @Override
    public Void visitar(NodoIf nodo) {
        visitarHijo(nodo.getCondicion());
        visitarHijo(nodo.getBloqueThen());
        visitarHijo(nodo.getBloqueElse());
        return null;
    }

    @Override
    public Void visitar(NodoWhile nodo) {
        visitarHijo(nodo.getCondicion());
        visitarHijo(nodo.getCuerpo());
        return null;
    }

    @Override
    public Void visitar(NodoDoWhile nodo) {
        visitarHijo(nodo.getCuerpo());
        visitarHijo(nodo.getCondicion());
        return null;
    }

    @Override
    public Void visitar(NodoFor nodo) {
        visitarHijo(nodo.getInicializacion());
        visitarHijo(nodo.getCondicion());
        visitarHijo(nodo.getActualizacion());
        visitarHijo(nodo.getCuerpo());
        return null;
    }

    @Override
    public Void visitar(NodoSwitch nodo) {
        visitarHijo(nodo.getExpresion());
        visitarHijos(nodo.getCasos());
        return null;
    }

    @Override
    public Void visitar(NodoCaso nodo) {
        visitarHijo(nodo.getValor());
        visitarHijos(nodo.getSentencias());
        return null;
    }

    @Override
    public Void visitar(NodoTernario nodo) {
        visitarHijo(nodo.getCondicion());
        visitarHijo(nodo.getVerdadero());
        visitarHijo(nodo.getFalso());
        return null;
    }

    @Override
    public Void visitar(NodoLlamadaMetodo nodo) {
        visitarHijos(nodo.getArgumentos());
        return null;
    }

    @Override
    public Void visitar(NodoInstanciacion nodo) {
        visitarHijos(nodo.getArgumentos());
        return null;
    }

    @Override
    public Void visitar(NodoArreglo nodo) {
        return null;
    }

    @Override
    public Void visitar(NodoEstructura nodo) {
        visitarHijo(nodo.getCuerpo());
        return null;
    }

    @Override
    public Void visitar(NodoIncremento nodo) {
        return null;
    }

    @Override
    public Void visitar(NodoIdentificador nodo) {
        return null;
    }

    @Override
    public Void visitar(NodoCadena nodo) {
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

    private void visitarHijo(Nodo nodo) {
        if (nodo != null) {
            nodo.accept(this);
        }
    }

    private void visitarHijos(List<? extends Nodo> nodos) {
        for (Nodo nodo : nodos) {
            visitarHijo(nodo);
        }
    }

    /**
     * Valida una declaración de variable:
     * - Que no esté ya declarada en el mismo ámbito
     * - Que el tipo sea válido
     */
    private void analizarDeclaracion(NodoDeclaracion nodo) {
        TipoDato tipo = TipoDato.desdeString(nodo.getTipoDato());

        if (tipo == TipoDato.DESCONOCIDO) {
            errores.add(new ErrorSemantico(
                    "Tipo de dato no reconocido: '" + nodo.getTipoDato() + "'",
                    "TIPO_INVALIDO",
                    nodo.getLinea(), nodo.getColumna()
            ));
            return;
        }

        Simbolo simbolo = new Simbolo(
                nodo.getIdentificador(), tipo,
                nodo.getLinea(), nodo.getColumna()
        );

        if (!tablaSimbolos.registrar(simbolo)) {
            errores.add(new ErrorSemantico(
                    "Variable '" + nodo.getIdentificador() + "' ya fue declarada en este ámbito",
                    "REDECLARACION",
                    nodo.getLinea(), nodo.getColumna()
            ));
        }

        // Si tiene inicialización, marcar como inicializada
        if (nodo.getInicializacion() != null) {
            simbolo.setInicializado(true);
        }
    }

    /**
     * Valida una asignación:
     * - Que la variable esté declarada
     */
    private void analizarAsignacion(NodoAsignacion nodo) {
        String identificador = nodo.getIdentificador();
        if (identificador == null) {
            return;
        }
        if (!tablaSimbolos.existe(identificador)) {
            errores.add(new ErrorSemantico(
                    "Variable '" + identificador + "' no ha sido declarada",
                    "NO_DECLARADA",
                    nodo.getLinea(), nodo.getColumna()
            ));
        } else {
            Simbolo simbolo = tablaSimbolos.buscar(identificador);
            if (simbolo != null) {
                simbolo.setInicializado(true);
            }
        }
    }

    /**
     * Valida un comando (placeholder para futuras validaciones).
     */
    private void analizarComando(NodoComando nodo) {
        // TODO: Validar argumentos del comando según su tipo
    }

    /**
     * Retorna la tabla de símbolos construida durante el análisis.
     */
    public TablaSimbolos getTablaSimbolos() {
        return tablaSimbolos;
    }

    /**
     * Retorna la lista de errores encontrados.
     */
    public List<ErrorSemantico> getErrores() {
        return errores;
    }

    /**
     * Indica si el análisis encontró errores.
     */
    public boolean tieneErrores() {
        return !errores.isEmpty();
    }
}
