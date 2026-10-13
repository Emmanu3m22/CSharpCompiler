package com.compilador.intermedio;

import com.compilador.ast.*;

import java.util.ArrayList;
import java.util.List;

/**
 * Traduce expresiones aritméticas del AST a cuádruplos.
 *
 * Recorre en postorden: cada hoja produce un {@link Operando} (constante o
 * variable) y cada operación emite {@code (op, izq, der, tN)} devolviendo el
 * temporal {@code tN}. Si la expresión tiene destino se cierra con
 * {@code (=, tN, —, destino)}.
 *
 * El contador de temporales vive en la instancia y NO se reinicia entre
 * expresiones: así la lista global de cuádruplos de todo el programa es una IR
 * válida (sin temporales repetidos) sobre la que se puede optimizar.
 */
public class GeneradorCuadruplos {

    private int contadorTemporales = 0;

    /**
     * Genera los cuádruplos de una expresión.
     *
     * @param expresion raíz de la expresión
     * @param destino   variable que recibe el resultado, o null si no hay asignación
     * @return cuádruplos generados (en orden de ejecución)
     */
    public List<Cuadruplo> generar(Nodo expresion, String destino) {
        List<Cuadruplo> salida = new ArrayList<>();
        Operando resultado = generar(expresion, salida);
        if (destino != null) {
            salida.add(Cuadruplo.asignacion(resultado, Operando.variable(destino)));
        }
        return salida;
    }

    /** Número de temporales emitidos hasta ahora. */
    public int getContadorTemporales() {
        return contadorTemporales;
    }

    private Operando generar(Nodo nodo, List<Cuadruplo> salida) {
        Nodo n = ConversorNotacion.sinAgrupacion(nodo);
        switch (n) {
            case NodoOperacion op -> {
                Operando izq = generar(op.getIzquierdo(), salida);
                Operando der = generar(op.getDerecho(), salida);
                Operando temporal = nuevoTemporal();
                salida.add(Cuadruplo.binario(op.getOperador(), izq, der, temporal));
                return temporal;
            }
            case NodoMenosUnario m -> {
                Operando operando = generar(m.getExpresion(), salida);
                Operando temporal = nuevoTemporal();
                salida.add(Cuadruplo.negacion(operando, temporal));
                return temporal;
            }
            case NodoNumero num -> {
                return Operando.constante(num.getValor());
            }
            case NodoIdentificador id -> {
                return Operando.variable(id.getNombre());
            }
            case null, default -> {
                return Operando.externo(ConversorNotacion.atomo(n));
            }
        }
    }

    private Operando nuevoTemporal() {
        return Operando.temporal(++contadorTemporales);
    }
}
