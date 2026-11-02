package com.compilador.optimizacion.fuente;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Conjunto de rangos de texto marcados para eliminación.
 *
 * Mantiene los rangos originales (para reportar) y, en paralelo, una versión fusionada
 * y ordenada que permite consultar {@link #contiene(int)} en O(log n).
 */
public final class RangosMarcados {

    private final List<RangoTexto> crudos = new ArrayList<>();
    private final TreeMap<Integer, Integer> disjuntos = new TreeMap<>();

    public void agregar(RangoTexto r) {
        if (r == null) return;
        crudos.add(r);
        int ini = r.inicio();
        int fin = r.fin();
        Map.Entry<Integer, Integer> previo = disjuntos.floorEntry(ini);
        if (previo != null && previo.getValue() >= ini) {
            ini = previo.getKey();
            fin = Math.max(fin, previo.getValue());
        }
        Map.Entry<Integer, Integer> e;
        while ((e = disjuntos.ceilingEntry(ini)) != null && e.getKey() <= fin) {
            fin = Math.max(fin, e.getValue());
            disjuntos.remove(e.getKey());
        }
        disjuntos.put(ini, fin);
    }

    /** @return true si la posición cae dentro de algún rango marcado. */
    public boolean contiene(int pos) {
        Map.Entry<Integer, Integer> e = disjuntos.floorEntry(pos);
        return e != null && pos < e.getValue();
    }

    public boolean isEmpty() {
        return crudos.isEmpty();
    }

    public List<RangoTexto> comoLista() {
        return List.copyOf(crudos);
    }
}
