package com.compilador.intermedio;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Evalúa una expresión en notación posfija con el algoritmo clásico de pila:
 * <ol>
 *   <li>Operando → se apila.</li>
 *   <li>Operador binario → se desapilan b y a, se apila (a op b).</li>
 *   <li>Operador unario (neg) → se desapila a, se apila -a.</li>
 *   <li>Al final la pila debe contener exactamente un valor: el resultado.</li>
 * </ol>
 *
 * Además registra una traza paso a paso para mostrarla en la interfaz.
 */
public final class EvaluadorPosfijo {

    /** Resultado de la evaluación junto con la traza de la pila. */
    public record Resultado(Valor valor, List<String> traza) {}

    private EvaluadorPosfijo() {}

    public static Resultado evaluar(List<ElementoPolaco> posfijo, EntornoValores entorno) throws ErrorEvaluacion {
        Deque<Valor> pila = new ArrayDeque<>();
        List<String> traza = new ArrayList<>();

        for (ElementoPolaco e : posfijo) {
            switch (e.clase()) {
                case OPERADOR_BINARIO -> {
                    if (pila.size() < 2) throw new ErrorEvaluacion("expresión posfija mal formada");
                    Valor b = pila.pop();
                    Valor a = pila.pop();
                    Valor r = a.operar(e.texto(), b);
                    pila.push(r);
                    traza.add(String.format("%-6s %s %s %s = %s  → pila: %s",
                            e.texto(), a, e.texto(), b, r, mostrar(pila)));
                }
                case OPERADOR_UNARIO -> {
                    if (pila.isEmpty()) throw new ErrorEvaluacion("expresión posfija mal formada");
                    Valor a = pila.pop();
                    Valor r = a.negar();
                    pila.push(r);
                    traza.add(String.format("%-6s -(%s) = %s  → pila: %s", e.texto(), a, r, mostrar(pila)));
                }
                case CONSTANTE -> {
                    pila.push(Valor.desdeLiteral(e.texto()));
                    traza.add(String.format("%-6s apilar  → pila: %s", e.texto(), mostrar(pila)));
                }
                case VARIABLE -> {
                    Valor v = entorno.obtener(e.texto());
                    if (v == null) throw new ErrorEvaluacion("'" + e.texto() + "' sin valor conocido");
                    pila.push(v);
                    traza.add(String.format("%-6s apilar %s  → pila: %s", e.texto(), v, mostrar(pila)));
                }
                case EXTERNO -> throw new ErrorEvaluacion(
                        "'" + e.texto() + "' no es un valor numérico evaluable");
            }
        }
        if (pila.size() != 1) {
            throw new ErrorEvaluacion("expresión posfija mal formada");
        }
        return new Resultado(pila.pop(), List.copyOf(traza));
    }

    /** Muestra la pila de fondo a tope: [a, b, c]. */
    private static String mostrar(Deque<Valor> pila) {
        List<Valor> elementos = new ArrayList<>(pila);
        java.util.Collections.reverse(elementos);
        return elementos.stream().map(Valor::toString).collect(Collectors.joining(", ", "[", "]"));
    }
}
