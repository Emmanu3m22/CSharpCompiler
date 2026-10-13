package com.compilador.optimizacion;

import com.compilador.intermedio.Cuadruplo;
import com.compilador.intermedio.ProgramaIntermedio;
import com.compilador.intermedio.ResultadoExpresion;

import java.util.ArrayList;
import java.util.List;

/**
 * Orquestador del pipeline de optimización de código intermedio.
 *
 * Permite encadenar múltiples pases independientes sobre la lista
 * de cuádruplos de la representación intermedia.
 */
public class Optimizador {

    private final List<PaseOptimizacion> pases;
    private final List<String> registroCambios;

    public Optimizador() {
        this.pases = new ArrayList<>();
        this.registroCambios = new ArrayList<>();
    }

    public static Optimizador crearPorDefecto() {
        Optimizador optimizador = new Optimizador();
        optimizador.agregarPase(new PlegadoConstantes());
        return optimizador;
    }

    public Optimizador agregarPase(PaseOptimizacion pase) {
        if (pase != null) {
            pases.add(pase);
        }
        return this;
    }

    public List<Cuadruplo> optimizarCuadruplos(List<Cuadruplo> entrada) {
        registroCambios.clear();
        List<Cuadruplo> actual = entrada != null ? List.copyOf(entrada) : List.of();

        for (PaseOptimizacion pase : pases) {
            int antes = actual.size();
            List<Cuadruplo> siguiente = pase.optimizar(actual);
            int despues = siguiente.size();
            registroCambios.add(String.format("Pase '%s': %d cuádruplos antes → %d después",
                    pase.nombre(), antes, despues));
            actual = siguiente;
        }

        return actual;
    }

    public ProgramaIntermedio optimizar(ProgramaIntermedio programa) {
        if (programa == null) return null;
        List<Cuadruplo> optimizados = optimizarCuadruplos(programa.cuadruplos());
        return new ProgramaIntermedio(
                programa.expresiones(),
                optimizados,
                programa.entorno()
        );
    }

    public List<String> getRegistroCambios() {
        return List.copyOf(registroCambios);
    }

    public List<PaseOptimizacion> getPases() {
        return List.copyOf(pases);
    }
}
