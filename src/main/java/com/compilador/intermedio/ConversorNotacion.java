package com.compilador.intermedio;

import com.compilador.ast.*;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Convierte una expresión aritmética del AST a sus distintas notaciones.
 *
 * <ul>
 *   <li><b>Prefija</b> (polaca): recorrido preorden → operador, izquierdo, derecho.</li>
 *   <li><b>Posfija</b> (polaca inversa): recorrido postorden → izquierdo, derecho, operador.</li>
 *   <li><b>Infija</b>: reconstrucción con paréntesis solo donde hacen falta.</li>
 * </ul>
 *
 * Como el AST ya codifica precedencia y asociatividad, los paréntesis del
 * código fuente ({@link NodoAgrupacion}) son transparentes: en notación polaca
 * no se necesitan.
 *
 * El menos unario se representa con el operador {@value Cuadruplo#NEGACION}
 * para no confundirlo con la resta.
 */
public final class ConversorNotacion {

    private static final int PREC_ADITIVA = 1;
    private static final int PREC_MULTIPLICATIVA = 2;
    private static final int PREC_UNARIA = 3;

    private ConversorNotacion() {}

    // ── API pública ────────────────────────────────────────────────

    public static String prefijo(Nodo nodo) {
        return unir(prefijoElementos(nodo));
    }

    public static String posfijo(Nodo nodo) {
        return unir(posfijoElementos(nodo));
    }

    public static List<ElementoPolaco> prefijoElementos(Nodo nodo) {
        List<ElementoPolaco> salida = new ArrayList<>();
        prefijo(nodo, salida);
        return salida;
    }

    public static List<ElementoPolaco> posfijoElementos(Nodo nodo) {
        List<ElementoPolaco> salida = new ArrayList<>();
        posfijo(nodo, salida);
        return salida;
    }

    public static String infijo(Nodo nodo) {
        return infijo(nodo, 0, false);
    }

    /**
     * @return true si el nodo (ignorando paréntesis) es una operación aritmética
     *         (binaria o menos unario), es decir, algo que vale la pena traducir.
     */
    public static boolean esAritmetica(Nodo nodo) {
        Nodo n = sinAgrupacion(nodo);
        return n instanceof NodoOperacion || n instanceof NodoMenosUnario;
    }

    /** Quita todas las capas de paréntesis externas. */
    public static Nodo sinAgrupacion(Nodo nodo) {
        Nodo n = nodo;
        while (n instanceof NodoAgrupacion g) {
            n = g.getExpresion();
        }
        return n;
    }

    /**
     * Representación textual de un nodo que se trata como operando atómico
     * (número, variable, llamada, acceso a arreglo, cadena...).
     */
    public static String atomo(Nodo nodo) {
        return switch (nodo) {
            case null -> "?";
            case NodoNumero n -> n.getValor();
            case NodoIdentificador id -> id.getNombre();
            case NodoCadena c -> "\"" + c.getValor() + "\"";
            case NodoBooleano b -> String.valueOf(b.getValor());
            case NodoLlamadaMetodo m -> m.getIdentificador() + "("
                    + m.getArgumentos().stream().map(ConversorNotacion::infijo)
                        .collect(Collectors.joining(", ")) + ")";
            case NodoAccesoArreglo a -> atomo(a.getArreglo()) + "[" + infijo(a.getIndice()) + "]";
            case NodoCasteo c -> "(" + c.getTipo() + ")" + envolverSiCompuesto(c.getExpresion());
            case NodoIncremento inc -> inc.getIdentificador() + inc.getOperador();
            case NodoAgrupacion g -> "(" + infijo(g.getExpresion()) + ")";
            default -> nodo.toString();
        };
    }

    /** Clasifica un nodo hoja para la notación polaca. */
    static ElementoPolaco.Clase claseAtomo(Nodo nodo) {
        if (nodo instanceof NodoNumero) return ElementoPolaco.Clase.CONSTANTE;
        if (nodo instanceof NodoIdentificador) return ElementoPolaco.Clase.VARIABLE;
        return ElementoPolaco.Clase.EXTERNO;
    }

    // ── Recorridos ─────────────────────────────────────────────────

    private static void prefijo(Nodo nodo, List<ElementoPolaco> salida) {
        Nodo n = sinAgrupacion(nodo);
        switch (n) {
            case NodoOperacion op -> {
                salida.add(new ElementoPolaco(op.getOperador(), ElementoPolaco.Clase.OPERADOR_BINARIO));
                prefijo(op.getIzquierdo(), salida);
                prefijo(op.getDerecho(), salida);
            }
            case NodoMenosUnario m -> {
                salida.add(new ElementoPolaco(Cuadruplo.NEGACION, ElementoPolaco.Clase.OPERADOR_UNARIO));
                prefijo(m.getExpresion(), salida);
            }
            default -> salida.add(new ElementoPolaco(atomo(n), claseAtomo(n)));
        }
    }

    private static void posfijo(Nodo nodo, List<ElementoPolaco> salida) {
        Nodo n = sinAgrupacion(nodo);
        switch (n) {
            case NodoOperacion op -> {
                posfijo(op.getIzquierdo(), salida);
                posfijo(op.getDerecho(), salida);
                salida.add(new ElementoPolaco(op.getOperador(), ElementoPolaco.Clase.OPERADOR_BINARIO));
            }
            case NodoMenosUnario m -> {
                posfijo(m.getExpresion(), salida);
                salida.add(new ElementoPolaco(Cuadruplo.NEGACION, ElementoPolaco.Clase.OPERADOR_UNARIO));
            }
            default -> salida.add(new ElementoPolaco(atomo(n), claseAtomo(n)));
        }
    }

    /**
     * @param precPadre   precedencia del operador padre (0 = raíz)
     * @param ladoDerecho si el nodo es el operando derecho del padre; como todos
     *                    los operadores son asociativos por la izquierda,
     *                    {@code a - (b - c)} necesita paréntesis pero
     *                    {@code (a - b) - c} no.
     */
    private static String infijo(Nodo nodo, int precPadre, boolean ladoDerecho) {
        Nodo n = sinAgrupacion(nodo);
        switch (n) {
            case NodoOperacion op -> {
                int prec = precedencia(op.getOperador());
                String texto = infijo(op.getIzquierdo(), prec, false)
                        + " " + op.getOperador() + " "
                        + infijo(op.getDerecho(), prec, true);
                boolean parentesis = prec < precPadre || (prec == precPadre && ladoDerecho);
                return parentesis ? "(" + texto + ")" : texto;
            }
            case NodoMenosUnario m -> {
                String operando = infijo(m.getExpresion(), PREC_UNARIA, false);
                if (operando.startsWith("-")) {
                    operando = "(" + operando + ")";
                }
                return "-" + operando;
            }
            default -> {
                return atomo(n);
            }
        }
    }

    private static String envolverSiCompuesto(Nodo nodo) {
        Nodo n = sinAgrupacion(nodo);
        return esAritmetica(n) ? "(" + infijo(n) + ")" : atomo(n);
    }

    private static int precedencia(String operador) {
        return switch (operador) {
            case "*", "/", "%" -> PREC_MULTIPLICATIVA;
            default -> PREC_ADITIVA;
        };
    }

    private static String unir(List<ElementoPolaco> elementos) {
        return elementos.stream().map(ElementoPolaco::texto).collect(Collectors.joining(" "));
    }
}
