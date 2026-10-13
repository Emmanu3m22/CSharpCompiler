package com.compilador.intermedio;

/**
 * Operando de un cuádruplo.
 *
 * Se tipa explícitamente (constante / variable / temporal / externo) para que
 * las fases posteriores —en particular la optimización— puedan razonar sobre
 * él sin volver al AST. Ejemplo: el plegado de constantes solo necesita
 * preguntar {@link #esConstante()} a ambos argumentos.
 *
 * @param tipo  categoría del operando
 * @param texto representación textual (valor literal, nombre o "tN")
 */
public record Operando(Tipo tipo, String texto) {

    public enum Tipo {
        /** Literal numérico: 5, 2.5, -3 */
        CONSTANTE,
        /** Variable del programa fuente: a, total */
        VARIABLE,
        /** Temporal generado por el compilador: t1, t2 ... */
        TEMPORAL,
        /** Operando no aritmético tratado como átomo: llamadas, cadenas, arreglos */
        EXTERNO
    }

    public static Operando constante(String valor) {
        return new Operando(Tipo.CONSTANTE, valor);
    }

    public static Operando variable(String nombre) {
        return new Operando(Tipo.VARIABLE, nombre);
    }

    public static Operando temporal(int numero) {
        return new Operando(Tipo.TEMPORAL, "t" + numero);
    }

    public static Operando externo(String texto) {
        return new Operando(Tipo.EXTERNO, texto);
    }

    public boolean esConstante() {
        return tipo == Tipo.CONSTANTE;
    }

    public boolean esVariable() {
        return tipo == Tipo.VARIABLE;
    }

    public boolean esTemporal() {
        return tipo == Tipo.TEMPORAL;
    }

    public boolean esExterno() {
        return tipo == Tipo.EXTERNO;
    }

    @Override
    public String toString() {
        return texto;
    }
}
