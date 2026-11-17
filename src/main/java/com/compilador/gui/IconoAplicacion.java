package com.compilador.gui;

import java.awt.Image;
import java.net.URL;

import javax.swing.ImageIcon;

final class IconoAplicacion {
    private static final String RUTA_RECURSO = "/IconoCompilador.png";

    private IconoAplicacion() {}

    static ImageIcon cargar(int tamano) {
        URL recurso = IconoAplicacion.class.getResource(RUTA_RECURSO);
        if (recurso == null) {
            throw new IllegalStateException("No se encontró el recurso " + RUTA_RECURSO);
        }

        ImageIcon original = new ImageIcon(recurso);
        if (original.getIconWidth() <= 0 || original.getIconHeight() <= 0) {
            throw new IllegalStateException("No se pudo cargar el recurso " + RUTA_RECURSO);
        }

        Image imagen = original.getImage().getScaledInstance(tamano, tamano, Image.SCALE_SMOOTH);
        return new ImageIcon(imagen);
    }
}
