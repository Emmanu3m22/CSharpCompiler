package com.compilador.intermedio;

import com.compilador.ast.Nodo;

/**
 * Representa una expresión aritmética detectada en el AST antes de generar su IR.
 *
 * @param raiz    nodo raíz de la operación aritmética
 * @param destino variable de destino si proviene de una asignación o declaración inicializada, o null
 * @param linea   línea de código fuente donde se ubica
 */
public record ExpresionAritmetica(
        Nodo raiz,
        String destino,
        int linea
) {
    public String infijo() {
        return ConversorNotacion.infijo(raiz);
    }
}
