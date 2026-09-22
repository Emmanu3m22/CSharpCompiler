package com.compilador.ast;

public class NodoAccesoArreglo extends Nodo {
    private final Nodo arreglo;
    private final Nodo indice;

    public NodoAccesoArreglo(Nodo arreglo, Nodo indice, int linea, int columna) {
        super(linea, columna);
        this.arreglo = arreglo;
        this.indice = indice;
    }

    public NodoAccesoArreglo(String identificador, Nodo indice, int linea, int columna) {
        this(new NodoIdentificador(identificador, linea, columna), indice, linea, columna);
    }

    public Nodo getArreglo() {
        return arreglo;
    }

    public String getIdentificador() {
        return arreglo instanceof NodoIdentificador
                ? ((NodoIdentificador) arreglo).getNombre()
                : null;
    }

    public Nodo getIndice() {
        return indice;
    }

    @Override
    public String toString() {
        return "AccesoArreglo(" + arreglo + "[" + indice + "])";
    }

    @Override
    public <T> T accept(NodoVisitor<T> visitor) {
        return visitor.visitar(this);
    }
}
