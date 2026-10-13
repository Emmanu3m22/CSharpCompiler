package com.compilador.intermedio;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Valores conocidos de las variables mientras se recorre el programa en orden.
 *
 * Es deliberadamente simple (un solo ámbito, sin simular control de flujo):
 * su objetivo es poder mostrar el resultado de cada operación, no ejecutar el
 * programa completo.
 */
public class EntornoValores {

    private static final Set<String> TIPOS_DECIMALES = Set.of("float", "double", "decimal");
    private static final Set<String> TIPOS_ENTEROS = Set.of(
            "int", "long", "short", "byte", "sbyte", "uint", "ulong", "ushort");

    private final Map<String, Valor> valores = new HashMap<>();
    private final Map<String, String> tipos = new HashMap<>();

    /** Registra el tipo declarado de una variable y olvida cualquier valor previo. */
    public void declarar(String nombre, String tipo) {
        if (nombre == null) return;
        valores.remove(nombre);
        if (tipo != null) {
            tipos.put(nombre, tipo);
        }
    }

    /**
     * Asigna un valor aplicando la conversión implícita del tipo declarado
     * (ej: {@code double x = 7 / 2;} guarda 3.0).
     *
     * @return el valor efectivamente almacenado
     */
    public Valor asignar(String nombre, Valor valor) {
        Valor convertido = convertir(tipos.get(nombre), valor);
        valores.put(nombre, convertido);
        return convertido;
    }

    /** Marca la variable como "valor desconocido". */
    public void olvidar(String nombre) {
        if (nombre != null) {
            valores.remove(nombre);
        }
    }

    /** @return el valor actual o null si no se conoce */
    public Valor obtener(String nombre) {
        return valores.get(nombre);
    }

    public boolean conoce(String nombre) {
        return valores.containsKey(nombre);
    }

    public Map<String, Valor> instantanea() {
        return Map.copyOf(valores);
    }

    private static Valor convertir(String tipo, Valor valor) {
        if (tipo == null) return valor;
        if (TIPOS_DECIMALES.contains(tipo)) return valor.aDecimal();
        if (TIPOS_ENTEROS.contains(tipo)) return valor.aEntero();
        return valor;
    }
}
