package com.compilador;

import com.compilador.ast.*;
import com.compilador.errores.*;
import com.compilador.semantic.*;

import java.io.*;
import java.util.List;

/**
 * Punto de entrada del compilador.
 * Orquesta el flujo: código fuente → léxico/sintáctico → AST → semántico → resultados.
 */
public class Main {

    public static void main(String[] args) {
        String archivo = "entrada.txt";
        if (args.length > 0) {
            archivo = args[0];
        }

        System.out.println("╔══════════════════════════════════════════╗");
        System.out.println("║   Compilador LenguajeCSharp v1.0        ║");
        System.out.println("╚══════════════════════════════════════════╝");
        System.out.println();

        try {
            // 1. Leer el archivo de entrada
            FileInputStream fis = new FileInputStream(archivo);
            System.out.println("► Analizando archivo: " + archivo);
            System.out.println();

            // 2. Crear el parser (incluye léxico + sintáctico)
            Analizador parser = new Analizador(fis);

            // 3. Ejecutar análisis léxico + sintáctico → genera AST
            NodoPrograma ast = parser.programa();

            // 4. Mostrar errores léxicos si los hay
            List<ErrorLexico> erroresLex = parser.getErroresLexicos();
            if (!erroresLex.isEmpty()) {
                System.out.println("── Errores Léxicos (" + erroresLex.size() + ") ──");
                for (ErrorLexico e : erroresLex) {
                    System.out.println("  ✗ " + e);
                }
                System.out.println();
            }

            // 5. Mostrar errores sintácticos si los hay
            List<ErrorSintactico> erroresSint = parser.getErroresSintacticos();
            if (!erroresSint.isEmpty()) {
                System.out.println("── Errores Sintácticos (" + erroresSint.size() + ") ──");
                for (ErrorSintactico e : erroresSint) {
                    System.out.println("  ✗ " + e);
                }
                System.out.println();
            }

            // 6. Mostrar AST generado
            System.out.println("── AST Generado ──");
            System.out.println(ast);
            System.out.println();

            // 7. Ejecutar análisis semántico
            AnalizadorSemantico semantico = new AnalizadorSemantico();
            List<ErrorSemantico> erroresSem = semantico.analizar(ast);

            if (!erroresSem.isEmpty()) {
                System.out.println("── Errores Semánticos (" + erroresSem.size() + ") ──");
                for (ErrorSemantico e : erroresSem) {
                    System.out.println("  ✗ " + e);
                }
                System.out.println();
            }

            // 8. Mostrar tabla de símbolos
            System.out.println("── Tabla de Símbolos ──");
            System.out.println(semantico.getTablaSimbolos());

            // 9. Resumen final
            System.out.println("── Resumen ──");
            int totalErrores = erroresLex.size() + erroresSint.size() + erroresSem.size();
            if (totalErrores == 0) {
                System.out.println("  ✓ Análisis completado sin errores.");
            } else {
                System.out.println("  ✗ Se encontraron " + totalErrores + " error(es) en total.");
            }

            fis.close();

        } catch (ParseException e) {
            System.err.println("Error de sintaxis: " + e.getMessage());
        } catch (FileNotFoundException e) {
            System.err.println("Archivo no encontrado: " + archivo);
        } catch (Exception e) {
            System.err.println("Error inesperado: " + e.getMessage());
            e.printStackTrace();
        }
    }
}