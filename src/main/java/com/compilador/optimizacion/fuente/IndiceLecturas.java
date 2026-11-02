package com.compilador.optimizacion.fuente;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Índice de lecturas de variables: para cada nombre, las posiciones de texto donde se lee.
 * Se construye una sola vez por pase; luego se consulta ignorando los rangos ya eliminados.
 */
public final class IndiceLecturas {

    private final Map<String, List<Integer>> posiciones = new HashMap<>();

    void registrar(String nombre, int posicion) {
        posiciones.computeIfAbsent(nombre, k -> new ArrayList<>()).add(posicion);
    }

    /** @return true si el nombre se lee en alguna posición fuera de los rangos marcados. */
    public boolean estaLeida(String nombre, RangosMarcados eliminados) {
        List<Integer> lista = posiciones.get(nombre);
        if (lista == null) return false;
        if (eliminados == null || eliminados.isEmpty()) return true;
        for (int pos : lista) {
            if (!eliminados.contiene(pos)) return true;
        }
        return false;
    }

    public Set<String> nombresLeidos(RangosMarcados eliminados) {
        Set<String> res = new HashSet<>();
        for (String nombre : posiciones.keySet()) {
            if (estaLeida(nombre, eliminados)) res.add(nombre);
        }
        return res;
    }
}
