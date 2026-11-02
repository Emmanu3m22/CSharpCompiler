package com.compilador.optimizacion.fuente;

/**
 * Intervalo de caracteres [inicio, fin) en el texto del código fuente.
 */
public record RangoTexto(int inicio, int fin) implements Comparable<RangoTexto> {

    public RangoTexto {
        if (inicio < 0 || fin < inicio) {
            throw new IllegalArgumentException("Rango inválido: [" + inicio + ", " + fin + ")");
        }
    }

    public int longitud() {
        return fin - inicio;
    }

    public boolean contiene(int indice) {
        return indice >= inicio && indice < fin;
    }

    public boolean seSuperpone(RangoTexto otro) {
        return otro != null && this.inicio < otro.fin && otro.inicio < this.fin;
    }

    @Override
    public int compareTo(RangoTexto o) {
        int comp = Integer.compare(this.inicio, o.inicio);
        if (comp != 0) return comp;
        return Integer.compare(this.fin, o.fin);
    }
}
