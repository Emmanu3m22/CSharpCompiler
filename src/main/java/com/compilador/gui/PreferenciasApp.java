package com.compilador.gui;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.prefs.Preferences;

/**
 * Preferencias persistentes de la aplicación (registro de Windows / archivo de usuario):
 * si se muestra la bienvenida al iniciar y la lista de archivos recientes.
 */
final class PreferenciasApp {

    private static final int MAX_RECIENTES = 6;
    private static final String CLAVE_BIENVENIDA = "mostrarBienvenida";
    private static final String CLAVE_RECIENTES = "recientes";
    private static final String SEPARADOR = "|"; // carácter no válido en rutas de Windows

    private PreferenciasApp() {}

    private static Preferences nodo() {
        return Preferences.userNodeForPackage(PreferenciasApp.class);
    }

    static boolean mostrarBienvenida() {
        try {
            return nodo().getBoolean(CLAVE_BIENVENIDA, true);
        } catch (RuntimeException e) {
            return true;
        }
    }

    static void setMostrarBienvenida(boolean mostrar) {
        try {
            nodo().putBoolean(CLAVE_BIENVENIDA, mostrar);
        } catch (RuntimeException ignorada) {
            // sin persistencia disponible: se ignora
        }
    }

    /** Archivos recientes que todavía existen, del más nuevo al más antiguo. */
    static List<File> recientes() {
        List<File> lista = new ArrayList<>();
        try {
            String guardado = nodo().get(CLAVE_RECIENTES, "");
            for (String ruta : guardado.split("\\" + SEPARADOR)) {
                if (!ruta.isBlank()) {
                    File f = new File(ruta);
                    if (f.isFile()) lista.add(f);
                }
            }
        } catch (RuntimeException ignorada) {
            // sin persistencia disponible
        }
        return lista;
    }

    static void agregarReciente(File archivo) {
        if (archivo == null) return;
        try {
            List<String> rutas = new ArrayList<>();
            rutas.add(archivo.getAbsolutePath());
            for (File f : recientes()) {
                if (!f.getAbsolutePath().equals(archivo.getAbsolutePath())) {
                    rutas.add(f.getAbsolutePath());
                }
            }
            if (rutas.size() > MAX_RECIENTES) {
                rutas = rutas.subList(0, MAX_RECIENTES);
            }
            nodo().put(CLAVE_RECIENTES, String.join(SEPARADOR, rutas));
        } catch (RuntimeException ignorada) {
            // sin persistencia disponible
        }
    }
}
