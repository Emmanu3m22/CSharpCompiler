package com.compilador.gui;

import java.awt.Color;
import java.awt.Font;

/**
 * Paleta de colores centralizada para toda la interfaz.
 * Tema oscuro inspirado en IDEs modernos.
 *
 * Uso: Colores.FONDO_PRINCIPAL, Colores.TEXTO_NORMAL, etc.
 */
public final class Colores {

    private Colores() {} // No instanciable

    // ── Fondos ──
    public static final Color FONDO_PRINCIPAL    = new Color(30, 30, 46);
    public static final Color FONDO_PANEL        = new Color(36, 36, 54);
    public static final Color FONDO_EDITOR       = new Color(24, 24, 37);
    public static final Color FONDO_LINEAS       = new Color(30, 30, 46);
    public static final Color FONDO_TOOLBAR      = new Color(40, 40, 60);
    public static final Color FONDO_TABLA_HEADER = new Color(50, 50, 75);
    public static final Color FONDO_TABLA_ROW1   = new Color(36, 36, 54);
    public static final Color FONDO_TABLA_ROW2   = new Color(42, 42, 63);
    public static final Color FONDO_SELECCION    = new Color(68, 71, 124);

    // ── Texto ──
    public static final Color TEXTO_NORMAL       = new Color(205, 214, 244);
    public static final Color TEXTO_TENUE         = new Color(147, 153, 178);
    public static final Color TEXTO_LINEAS       = new Color(100, 106, 134);

    // ── Acentos por fase ──
    public static final Color ACENTO_LEXICO      = new Color(137, 180, 250); // Azul suave
    public static final Color ACENTO_SINTACTICO  = new Color(166, 227, 161); // Verde suave
    public static final Color ACENTO_SEMANTICO   = new Color(203, 166, 247); // Morado suave
    public static final Color ACENTO_PRINCIPAL   = new Color(137, 180, 250); // Azul principal

    // ── Estados ──
    public static final Color EXITO              = new Color(166, 227, 161);
    public static final Color ERROR              = new Color(243, 139, 168);
    public static final Color ADVERTENCIA        = new Color(249, 226, 175);
    public static final Color INFO               = new Color(137, 180, 250);

    // ── Sintaxis (resaltado de código) ──
    public static final Color SINTAXIS_KEYWORD   = new Color(203, 166, 247);
    public static final Color SINTAXIS_STRING     = new Color(166, 227, 161);
    public static final Color SINTAXIS_NUMERO    = new Color(250, 179, 135);
    public static final Color SINTAXIS_COMENTARIO = new Color(108, 112, 134);
    public static final Color SINTAXIS_OPERADOR  = new Color(148, 226, 213);

    // ── Bordes ──
    public static final Color BORDE              = new Color(69, 71, 90);
    public static final Color BORDE_ENFOCADO     = new Color(137, 180, 250);

    // ── Fuentes ──
    public static final Font FUENTE_CODIGO       = new Font("Consolas", Font.PLAIN, 14);
    public static final Font FUENTE_NORMAL       = new Font("Segoe UI", Font.PLAIN, 13);
    public static final Font FUENTE_TITULO       = new Font("Segoe UI", Font.BOLD, 14);
    public static final Font FUENTE_PEQUENA      = new Font("Segoe UI", Font.PLAIN, 11);
    public static final Font FUENTE_TABLA        = new Font("Consolas", Font.PLAIN, 12);
}
