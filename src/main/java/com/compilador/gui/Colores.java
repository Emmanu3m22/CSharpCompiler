package com.compilador.gui;

import java.awt.Color;
import java.awt.Font;

/**
 * Paleta de colores centralizada para toda la interfaz.
 * Tema elegante: Azul marino con colores pastel.
 *
 * Uso: Colores.FONDO_PRINCIPAL, Colores.TEXTO_NORMAL, etc.
 */
public final class Colores {

    private Colores() {} // No instanciable

    // ── Fondos (Azul marino + Pastel) ──
    public static final Color FONDO_PRINCIPAL    = new Color(15, 38, 70);     // Azul marino oscuro
    public static final Color FONDO_PANEL        = new Color(25, 55, 95);     // Azul marino medio
    public static final Color FONDO_EDITOR       = new Color(20, 45, 80);     // Azul marino claro
    public static final Color FONDO_LINEAS       = new Color(15, 38, 70);     // Azul marino oscuro
    public static final Color FONDO_TOOLBAR      = new Color(20, 50, 90);     // Azul marino
    public static final Color FONDO_TABLA_HEADER = new Color(30, 65, 115);    // Azul marino más claro
    public static final Color FONDO_TABLA_ROW1   = new Color(25, 55, 95);     // Azul marino medio
    public static final Color FONDO_TABLA_ROW2   = new Color(32, 62, 105);    // Azul marino alternado
    public static final Color FONDO_SELECCION    = new Color(50, 90, 150);    // Azul marino selección
    public static final Color FONDO_CONTENEDOR   = new Color(35, 70, 120);    // Azul marino contenedor

    // ── Texto (Pastel y claros) ──
    public static final Color TEXTO_NORMAL       = new Color(220, 237, 255); // Blanco pastel azulado
    public static final Color TEXTO_TENUE        = new Color(170, 195, 230); // Azul pastel suave
    public static final Color TEXTO_LINEAS       = new Color(110, 145, 190); // Azul pastel medio

    // ── Acentos por fase (Colores Pastel) ──
    public static final Color ACENTO_LEXICO      = new Color(150, 200, 255); // Azul pastel claro
    public static final Color ACENTO_SINTACTICO  = new Color(180, 240, 200); // Verde pastel
    public static final Color ACENTO_SEMANTICO   = new Color(230, 190, 255); // Morado pastel
    public static final Color ACENTO_PRINCIPAL   = new Color(150, 200, 255); // Azul pastel principal

    // ── Estados (Pastel elegante) ──
    public static final Color EXITO              = new Color(180, 240, 200); // Verde pastel
    public static final Color ERROR              = new Color(255, 180, 200); // Rosa pastel error
    public static final Color ADVERTENCIA        = new Color(255, 220, 150); // Naranja pastel
    public static final Color INFO               = new Color(150, 200, 255); // Azul pastel info

    // ── Sintaxis (resaltado de código) ──
    public static final Color SINTAXIS_KEYWORD   = new Color(220, 190, 255); // Morado pastel
    public static final Color SINTAXIS_STRING    = new Color(180, 240, 200); // Verde pastel
    public static final Color SINTAXIS_NUMERO    = new Color(255, 200, 150); // Naranja pastel
    public static final Color SINTAXIS_COMENTARIO = new Color(130, 170, 210); // Azul gris pastel
    public static final Color SINTAXIS_OPERADOR  = new Color(150, 220, 240); // Cyan pastel

    // ── Bordes ──
    public static final Color BORDE              = new Color(50, 100, 160);    // Azul marino borde
    public static final Color BORDE_ENFOCADO     = new Color(150, 200, 255);  // Azul pastel enfocado

    // ── Fuentes ──
    public static final Font FUENTE_CODIGO       = new Font("Consolas", Font.PLAIN, 14);
    public static final Font FUENTE_NORMAL       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_TITULO       = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FUENTE_PEQUENA      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FUENTE_TABLA        = new Font("Consolas", Font.PLAIN, 12);
}
