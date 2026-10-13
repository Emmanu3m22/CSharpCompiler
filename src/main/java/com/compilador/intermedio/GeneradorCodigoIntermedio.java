package com.compilador.intermedio;

import com.compilador.ast.Nodo;
import com.compilador.ast.NodoPrograma;

/**
 * Fachada principal del generador de código intermedio.
 *
 * Conecta el AST con el recolector de expresiones, generador de notaciones
 * polacas (prefijo y posfijo), generador de cuádruplos y evaluadores.
 */
public class GeneradorCodigoIntermedio {

    /**
     * Genera la representación intermedia de un programa completo.
     *
     * @param ast raíz del árbol sintáctico
     * @return programa intermedio con expresiones analizadas y cuádruplos globales
     */
    public ProgramaIntermedio generar(NodoPrograma ast) {
        if (ast == null) {
            return new ProgramaIntermedio(java.util.List.of(), java.util.List.of(), new EntornoValores());
        }
        RecolectorExpresiones recolector = new RecolectorExpresiones();
        return recolector.recolectar(ast);
    }

    /**
     * Helper para generar la representación intermedia a partir de un nodo cualquiera.
     */
    public ProgramaIntermedio generar(Nodo nodo) {
        if (nodo == null) {
            return new ProgramaIntermedio(java.util.List.of(), java.util.List.of(), new EntornoValores());
        }
        if (nodo instanceof NodoPrograma prog) {
            return generar(prog);
        }
        RecolectorExpresiones recolector = new RecolectorExpresiones();
        recolector.visitarSubexpresiones(nodo);
        return new ProgramaIntermedio(
                java.util.List.copyOf(recolector.getResultados()),
                java.util.List.copyOf(recolector.getTodosCuadruplos()),
                recolector.getEntorno()
        );
    }

    /**
     * Genera un reporte formateado en texto para imprimir en consola.
     */
    public String imprimirReporte(ProgramaIntermedio programa) {
        if (programa == null || programa.estaVacio()) {
            return "No se encontraron operaciones aritméticas en el código.";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("════════════════════════════════════════════════════════════════════════════════\n");
        sb.append("                  CÓDIGO INTERMEDIO Y NOTACIÓN POLACA                           \n");
        sb.append("════════════════════════════════════════════════════════════════════════════════\n\n");

        int i = 1;
        for (ResultadoExpresion exp : programa.expresiones()) {
            sb.append(String.format("► Operación #%d (Línea %d)%s:\n",
                    i++, exp.linea(),
                    exp.destino() != null ? " [Asignada a: " + exp.destino() + "]" : ""));
            sb.append(String.format("   Infijo    : %s\n", exp.infijo()));
            sb.append(String.format("   Prefijo   : %s\n", exp.prefijo()));
            sb.append(String.format("   Posfijo   : %s\n", exp.posfijo()));
            sb.append(String.format("   Resultado : %s\n", exp.resultadoOErrorTexto()));
            sb.append("   Cuádruplos:\n");
            int j = 1;
            for (Cuadruplo c : exp.cuadruplos()) {
                sb.append(String.format("      (%d) %-4s %-12s %-12s %-12s\n",
                        j++, c.operador(), c.arg1Texto(), c.arg2Texto(), c.resultadoTexto()));
            }
            sb.append("\n");
        }

        sb.append(String.format("Total: %d operación(es), %d cuádruplo(s) generado(s).\n",
                programa.totalExpresiones(), programa.totalCuadruplos()));
        return sb.toString();
    }
}
