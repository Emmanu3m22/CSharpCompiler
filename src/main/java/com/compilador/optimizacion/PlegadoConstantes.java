package com.compilador.optimizacion;

import com.compilador.intermedio.Cuadruplo;
import com.compilador.intermedio.ErrorEvaluacion;
import com.compilador.intermedio.Operando;
import com.compilador.intermedio.Valor;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Pase de optimización: Plegado de Constantes (Constant Folding).
 *
 * Detecta operaciones cuyos dos operandos son constantes conocidas
 * y las evalúa en tiempo de compilación, reemplazando la operación
 * por una asignación simple o propagando la constante.
 */
public class PlegadoConstantes implements PaseOptimizacion {

    @Override
    public String nombre() {
        return "Plegado de Constantes";
    }

    @Override
    public List<Cuadruplo> optimizar(List<Cuadruplo> entrada) {
        if (entrada == null || entrada.isEmpty()) {
            return List.of();
        }

        List<Cuadruplo> salida = new ArrayList<>();
        Map<String, Operando> reemplazos = new HashMap<>();

        for (Cuadruplo c : entrada) {
            Operando arg1 = resolver(c.arg1(), reemplazos);
            Operando arg2 = resolver(c.arg2(), reemplazos);

            if (c.esBinario() && arg1 != null && arg2 != null && arg1.esConstante() && arg2.esConstante()) {
                try {
                    Valor v1 = Valor.desdeLiteral(arg1.texto());
                    Valor v2 = Valor.desdeLiteral(arg2.texto());
                    Valor resultado = v1.operar(c.operador(), v2);
                    Operando constRes = Operando.constante(resultado.toString());

                    // Reemplazamos la operación por una asignación directa
                    salida.add(Cuadruplo.asignacion(constRes, c.resultado()));
                    if (c.resultado() != null && c.resultado().esTemporal()) {
                        reemplazos.put(c.resultado().texto(), constRes);
                    }
                    continue;
                } catch (ErrorEvaluacion ignored) {
                    // Si hay división entre cero o similar, se deja intacto para tiempo de ejecución
                }
            } else if (c.esNegacion() && arg1 != null && arg1.esConstante()) {
                try {
                    Valor v = Valor.desdeLiteral(arg1.texto()).negar();
                    Operando constRes = Operando.constante(v.toString());
                    salida.add(Cuadruplo.asignacion(constRes, c.resultado()));
                    if (c.resultado() != null && c.resultado().esTemporal()) {
                        reemplazos.put(c.resultado().texto(), constRes);
                    }
                    continue;
                } catch (ErrorEvaluacion ignored) {
                }
            }

            // Cuádruplo con operandos propagados
            salida.add(new Cuadruplo(c.operador(), arg1, arg2, c.resultado()));
        }

        return salida;
    }

    private Operando resolver(Operando op, Map<String, Operando> reemplazos) {
        if (op == null) return null;
        if (op.esTemporal() && reemplazos.containsKey(op.texto())) {
            return reemplazos.get(op.texto());
        }
        return op;
    }
}
