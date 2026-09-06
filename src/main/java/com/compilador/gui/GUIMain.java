package com.compilador.gui;

import javax.swing.*;

/**
 * Clase principal para lanzar la interfaz gráfica del compilador.
 */
public class GUIMain {
    public static void main(String[] args) {
        // Lanzar la GUI en el Event Dispatch Thread
        SwingUtilities.invokeLater(() -> {
            AplicacionPrincipal ventana = new AplicacionPrincipal();
            ventana.setVisible(true);
        });
    }
}
