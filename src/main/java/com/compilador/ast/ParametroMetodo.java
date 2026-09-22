package com.compilador.ast;

/**
 * Parámetro declarado por un método o constructor.
 */
public class ParametroMetodo {

    private final String tipo;
    private final String nombre;
    private final Nodo valorPorDefecto;

    public ParametroMetodo(String tipo, String nombre, Nodo valorPorDefecto) {
        this.tipo = tipo;
        this.nombre = nombre;
        this.valorPorDefecto = valorPorDefecto;
    }

    public String getTipo() {
        return tipo;
    }

    public String getNombre() {
        return nombre;
    }

    public Nodo getValorPorDefecto() {
        return valorPorDefecto;
    }

    public boolean esOpcional() {
        return valorPorDefecto != null;
    }
}