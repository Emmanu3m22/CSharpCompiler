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
public class AnalizadorSemantico {

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
        for (Nodo sentencia : programa.getSentencias()) {
            analizarNodo(sentencia);
        }
        return errores;
    }

    /**
     * Analiza un nodo individual del AST según su tipo.
     * Despacha al método de análisis específico para cada tipo de nodo.
     */
    private void analizarNodo(Nodo nodo) {
        if (nodo instanceof NodoDeclaracion) {
            analizarDeclaracion((NodoDeclaracion) nodo);
        } else if (nodo instanceof NodoAsignacion) {
            analizarAsignacion((NodoAsignacion) nodo);
        } else if (nodo instanceof NodoComando) {
            analizarComando((NodoComando) nodo);
        }
        // TODO: Agregar más casos conforme se definan nuevos tipos de nodos
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
        if (!tablaSimbolos.existe(nodo.getIdentificador())) {
            errores.add(new ErrorSemantico(
                    "Variable '" + nodo.getIdentificador() + "' no ha sido declarada",
                    "NO_DECLARADA",
                    nodo.getLinea(), nodo.getColumna()
            ));
        } else {
            Simbolo simbolo = tablaSimbolos.buscar(nodo.getIdentificador());
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
