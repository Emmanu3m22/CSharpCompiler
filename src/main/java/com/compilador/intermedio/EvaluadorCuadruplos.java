package com.compilador.intermedio;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Ejecuta una lista de cuádruplos sobre un {@link EntornoValores}.
 *
 * Es de solo lectura respecto al entorno: el cuádruplo de asignación
 * {@code (=, x, —, destino)} solo produce el valor final; quien llama decide
 * si lo guarda (y con qué conversión de tipo). Esto permite evaluar la misma
 * expresión por otra vía (pila posfija) y comparar sin efectos secundarios.
 */
public final class EvaluadorCuadruplos {

    private EvaluadorCuadruplos() {}

    /**
     * @return el valor del último cuádruplo (resultado de la expresión)
     * @throws ErrorEvaluacion si algún operando no tiene valor numérico o hay división entre cero
     */
    public static Valor ejecutar(List<Cuadruplo> cuadruplos, EntornoValores entorno) throws ErrorEvaluacion {
        if (cuadruplos.isEmpty()) {
            throw new ErrorEvaluacion("no hay cuádruplos que evaluar");
        }
        Map<String, Valor> temporales = new HashMap<>();
        Valor ultimo = null;
        for (Cuadruplo c : cuadruplos) {
            Valor a = valorDe(c.arg1(), entorno, temporales);
            if (c.esAsignacion()) {
                ultimo = a;
            } else if (c.esNegacion()) {
                ultimo = a.negar();
            } else {
                Valor b = valorDe(c.arg2(), entorno, temporales);
                ultimo = a.operar(c.operador(), b);
            }
            if (c.resultado() != null && c.resultado().esTemporal()) {
                temporales.put(c.resultado().texto(), ultimo);
            }
        }
        return ultimo;
    }

    static Valor valorDe(Operando operando, EntornoValores entorno, Map<String, Valor> temporales)
            throws ErrorEvaluacion {
        if (operando == null) {
            throw new ErrorEvaluacion("operando faltante");
        }
        switch (operando.tipo()) {
            case CONSTANTE:
                return Valor.desdeLiteral(operando.texto());
            case TEMPORAL: {
                Valor v = temporales.get(operando.texto());
                if (v == null) throw new ErrorEvaluacion("temporal '" + operando.texto() + "' sin valor");
                return v;
            }
            case VARIABLE: {
                Valor v = entorno.obtener(operando.texto());
                if (v == null) throw new ErrorEvaluacion("'" + operando.texto() + "' sin valor conocido");
                return v;
            }
            default:
                throw new ErrorEvaluacion("'" + operando.texto() + "' no es un valor numérico evaluable");
        }
    }
}
