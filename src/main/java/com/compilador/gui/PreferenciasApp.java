package com.compilador.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Preferencias persistentes de la aplicación (registro de Windows / archivo de usuario)
 * con respaldo en memoria para entornos con permisos restringidos:
 * controla si se muestra la bienvenida al iniciar, el estado de los paneles y los archivos recientes.
 */
final class PreferenciasApp {

    private static final int MAX_RECIENTES = 6;
    private static final String CLAVE_BIENVENIDA = "mostrarBienvenida";
    private static final String CLAVE_RECIENTES = "recientes";
    private static final String SEPARADOR = "|"; // carácter no válido en rutas de Windows

    private static Boolean memoriaBienvenida;
    private static Boolean memoriaAnalisisColapsado;
    private static Boolean memoriaResumenColapsado;
    private static List<String> memoriaRecientes;

    private PreferenciasApp() {}

    private static Preferences nodo() {
        return Preferences.userNodeForPackage(PreferenciasApp.class);
    }

    static boolean mostrarBienvenida() {
        if (memoriaBienvenida != null) {
            return memoriaBienvenida;
        }
        try {
            return nodo().getBoolean(CLAVE_BIENVENIDA, true);
        } catch (RuntimeException e) {
            return true;
        }
    }

    static void setMostrarBienvenida(boolean mostrar) {
        memoriaBienvenida = mostrar;
        try {
            nodo().putBoolean(CLAVE_BIENVENIDA, mostrar);
        } catch (RuntimeException ignorada) {
            // sin persistencia disponible: se conserva en memoria
        }
    }

    /** Archivos recientes que todavía existen, del más nuevo al más antiguo. */
    static List<File> recientes() {
        List<File> lista = new ArrayList<>();
        String guardado = "";
        try {
            guardado = nodo().get(CLAVE_RECIENTES, "");
        } catch (RuntimeException ignorada) {
            // sin persistencia disponible
        }

        List<String> candidatos = new ArrayList<>();
        if (memoriaRecientes != null && !memoriaRecientes.isEmpty()) {
            candidatos.addAll(memoriaRecientes);
        } else if (!guardado.isBlank()) {
            for (String ruta : guardado.split("\\" + SEPARADOR)) {
                if (!ruta.isBlank()) {
                    candidatos.add(ruta);
                }
            }
        }

        for (String ruta : candidatos) {
            File f = new File(ruta);
            if (f.isFile()) {
                lista.add(f);
            }
        }
        return lista;
    }

    static boolean analisisColapsado() {
        if (memoriaAnalisisColapsado != null) {
            return memoriaAnalisisColapsado;
        }
        return leerBooleano("analisisColapsado");
    }

    static void setAnalisisColapsado(boolean valor) {
        memoriaAnalisisColapsado = valor;
        escribirBooleano("analisisColapsado", valor);
    }

    static boolean resumenColapsado() {
        if (memoriaResumenColapsado != null) {
            return memoriaResumenColapsado;
        }
        return leerBooleano("resumenColapsado");
    }

    static void setResumenColapsado(boolean valor) {
        memoriaResumenColapsado = valor;
        escribirBooleano("resumenColapsado", valor);
    }

    private static boolean leerBooleano(String clave) {
        try {
            return nodo().getBoolean(clave, false);
        } catch (RuntimeException e) {
            return false;
        }
    }

    private static void escribirBooleano(String clave, boolean valor) {
        try {
            nodo().putBoolean(clave, valor);
        } catch (RuntimeException ignorada) {
            // sin persistencia disponible: se ignora
        }
    }

    static void agregarReciente(File archivo) {
        if (archivo == null) return;
        List<String> rutas = new ArrayList<>();
        rutas.add(archivo.getAbsolutePath());
        for (File f : recientes()) {
            if (!f.getAbsolutePath().equals(archivo.getAbsolutePath())) {
                rutas.add(f.getAbsolutePath());
            }
        }
        if (rutas.size() > MAX_RECIENTES) {
            rutas = new ArrayList<>(rutas.subList(0, MAX_RECIENTES));
        }
        memoriaRecientes = rutas;
        try {
            nodo().put(CLAVE_RECIENTES, String.join(SEPARADOR, rutas));
        } catch (RuntimeException ignorada) {
            // sin persistencia disponible: se conserva en memoria
        }
    }
}
