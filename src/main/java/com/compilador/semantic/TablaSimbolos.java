package com.compilador.semantic;

import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;

/**
 * Tabla de símbolos del compilador.
 * Registra variables/identificadores declarados, su tipo y su ámbito.
 * Soporta ámbitos anidados (global, funciones, bloques if/while, etc.).
 */
public class TablaSimbolos {

    /** Mapa de nombre de variable → símbolo, organizado por ámbito */
    private final Map<String, Simbolo> simbolos;

    /** Ámbito actual (global, nombre de función, etc.) */
    private String ambitoActual;

    /** Pila de ámbitos para soportar anidamiento */
    private final List<String> pilaAmbitos;

    public TablaSimbolos() {
        this.simbolos = new HashMap<>();
        this.ambitoActual = "global";
        this.pilaAmbitos = new ArrayList<>();
        this.pilaAmbitos.add("global");
    }

    /**
     * Registra un nuevo símbolo en la tabla.
     * @return true si se registró correctamente, false si ya existía en el ámbito actual
     */
    public boolean registrar(Simbolo simbolo) {
        String clave = generarClave(simbolo.getNombre());
        if (simbolos.containsKey(clave)) {
            return false; // Variable ya declarada en este ámbito
        }
        simbolo.setAmbito(ambitoActual);
        simbolos.put(clave, simbolo);
        return true;
    }

    /**
     * Busca un símbolo por nombre, primero en el ámbito actual y luego en los superiores.
     * @return el Simbolo encontrado o null si no existe
     */
    public Simbolo buscar(String nombre) {
        // Buscar desde el ámbito actual hacia arriba
        for (int i = pilaAmbitos.size() - 1; i >= 0; i--) {
            String clave = pilaAmbitos.get(i) + "::" + nombre;
            if (simbolos.containsKey(clave)) {
                return simbolos.get(clave);
            }
        }
        return null;
    }

    /**
     * Verifica si un símbolo existe en la tabla (en cualquier ámbito visible).
     */
    public boolean existe(String nombre) {
        return buscar(nombre) != null;
    }

    /**
     * Entra a un nuevo ámbito (ej: al entrar a una función o bloque).
     */
    public void entrarAmbito(String nombre) {
        this.ambitoActual = nombre;
        this.pilaAmbitos.add(nombre);
    }

    /**
     * Sale del ámbito actual y regresa al anterior.
     */
    public void salirAmbito() {
        if (pilaAmbitos.size() > 1) {
            pilaAmbitos.remove(pilaAmbitos.size() - 1);
            ambitoActual = pilaAmbitos.get(pilaAmbitos.size() - 1);
        }
    }

    public String getAmbitoActual() {
        return ambitoActual;
    }

    /**
     * Retorna todos los símbolos registrados (para visualización en la GUI).
     */
    public Map<String, Simbolo> getSimbolos() {
        return new HashMap<>(simbolos);
    }

    private String generarClave(String nombre) {
        return ambitoActual + "::" + nombre;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("=== Tabla de Símbolos ===\n");
        for (Map.Entry<String, Simbolo> entry : simbolos.entrySet()) {
            sb.append("  ").append(entry.getValue()).append("\n");
        }
        return sb.toString();
    }
}
