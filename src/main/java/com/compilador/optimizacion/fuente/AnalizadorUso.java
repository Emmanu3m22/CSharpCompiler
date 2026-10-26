package com.compilador.optimizacion.fuente;

import com.compilador.AnalizadorConstants;
import com.compilador.Token;

import java.util.*;

/**
 * Analizador de uso de identificadores a nivel de tokens.
 *
 * Determina qué variables son leídas en el programa y si una expresión contiene
 * efectos secundarios (llamadas a métodos, instanciación con new, incrementos/decrementos).
 */
public class AnalizadorUso {

    private static final Set<Integer> TIPOS_DATO = Set.of(
            AnalizadorConstants.INT,
            AnalizadorConstants.FLOAT,
            AnalizadorConstants.DOUBLE,
            AnalizadorConstants.BOOL,
            AnalizadorConstants.STRING_T,
            AnalizadorConstants.CHAR_T,
            AnalizadorConstants.BYTE,
            AnalizadorConstants.SBYTE,
            AnalizadorConstants.SHORT,
            AnalizadorConstants.USHORT,
            AnalizadorConstants.UINT,
            AnalizadorConstants.LONG,
            AnalizadorConstants.ULONG,
            AnalizadorConstants.DECIMAL,
            AnalizadorConstants.OBJECT,
            AnalizadorConstants.VAR,
            AnalizadorConstants.VOID
    );

    private static final Set<Integer> MODIFICADORES = Set.of(
            AnalizadorConstants.PUBLIC,
            AnalizadorConstants.PRIVATE,
            AnalizadorConstants.PROTECTED,
            AnalizadorConstants.INTERNAL,
            AnalizadorConstants.STATIC,
            AnalizadorConstants.VIRTUAL,
            AnalizadorConstants.OVERRIDE,
            AnalizadorConstants.CONST,
            AnalizadorConstants.READONLY
    );

    private static final Set<Integer> OPERADORES_ASIG_COMPUESTA = Set.of(
            AnalizadorConstants.SUMA_ASIG,
            AnalizadorConstants.RESTA_ASIG,
            AnalizadorConstants.MULT_ASIG,
            AnalizadorConstants.DIV_ASIG,
            AnalizadorConstants.MOD_ASIG
    );

    /**
     * Recolecta todos los identificadores que son leídos/utilizados en el código fuente,
     * ignorando los tokens contenidos dentro de los rangos eliminados dados.
     */
    public static Set<String> obtenerVariablesLeidas(CodigoFuente fuente, List<RangoTexto> rangosIgnorados) {
        if (fuente == null) return Collections.emptySet();

        List<Token> tokens = fuente.getTokens();
        Set<String> leidas = new HashSet<>();
        List<RangoTexto> ignorados = CodigoFuente.normalizarRangos(rangosIgnorados);

        for (int i = 0; i < tokens.size(); i++) {
            Token t = tokens.get(i);

            // Ignorar tokens en rangos eliminados
            if (estaEnRangos(fuente.inicioDe(t), ignorados)) {
                continue;
            }

            if (t.kind == AnalizadorConstants.IDENTIFICADOR) {
                String nombre = t.image;

                // Si es un nombre de método en llamada directa (ej: foo(x)), no es lectura de variable
                if (esNombreDeLlamada(tokens, i)) {
                    continue;
                }

                // Si forma parte del nombre de clase en declaración "class MiClase" o "namespace N"
                if (esNombreDeEstructura(tokens, i)) {
                    continue;
                }

                // Si es la declaración de la variable (ej: "int x"), no es lectura
                if (esDeclaradorDeVariable(tokens, i)) {
                    continue;
                }

                // Si es el lado izquierdo de una asignación simple ("x = ..."), no es lectura
                if (esDestinoAsignacionSimple(tokens, i)) {
                    continue;
                }

                // En cualquier otro caso (RHS de asignación, argumento de función, condición,
                // asignación compuesta x += 1, índice arr[x], return x), es una LECTURA.
                leidas.add(nombre);
            }
        }

        return leidas;
    }

    /**
     * Verifica si el rango de tokens [idxInicio, idxFin] contiene alguna expresión con efectos secundarios
     * (llamadas a métodos, instanciación 'new', incrementos '++', decrementos '--').
     */
    public static boolean tieneEfectosSecundarios(CodigoFuente fuente, int idxInicio, int idxFin) {
        if (fuente == null || idxInicio < 0 || idxFin >= fuente.getTokens().size()) {
            return false;
        }
        List<Token> tokens = fuente.getTokens();
        for (int i = idxInicio; i <= idxFin; i++) {
            Token t = tokens.get(i);
            if (t.kind == AnalizadorConstants.NEW ||
                t.kind == AnalizadorConstants.INCREMENTO ||
                t.kind == AnalizadorConstants.DECREMENTO) {
                return true;
            }
            if (t.kind == AnalizadorConstants.IDENTIFICADOR && i + 1 <= idxFin) {
                Token sig = tokens.get(i + 1);
                if (sig.kind == AnalizadorConstants.PAREN_IZQ) {
                    return true; // Llamada a función/método
                }
            }
        }
        return false;
    }

    private static boolean estaEnRangos(int pos, List<RangoTexto> rangos) {
        if (rangos == null) return false;
        for (RangoTexto r : rangos) {
            if (r.contiene(pos)) return true;
        }
        return false;
    }

    private static boolean esNombreDeLlamada(List<Token> tokens, int idx) {
        if (idx + 1 < tokens.size()) {
            Token sig = tokens.get(idx + 1);
            if (sig.kind == AnalizadorConstants.PAREN_IZQ) {
                // Si antes tiene un punto (ej: Console.WriteLine), WriteLine es el método
                return true;
            }
        }
        return false;
    }

    private static boolean esNombreDeEstructura(List<Token> tokens, int idx) {
        if (idx > 0) {
            Token ant = tokens.get(idx - 1);
            if (ant.kind == AnalizadorConstants.CLASS ||
                ant.kind == AnalizadorConstants.INTERFACE ||
                ant.kind == AnalizadorConstants.NAMESPACE ||
                ant.kind == AnalizadorConstants.ENUM ||
                ant.kind == AnalizadorConstants.USING) {
                return true;
            }
        }
        return false;
    }

    private static boolean esDeclaradorDeVariable(List<Token> tokens, int idx) {
        int k = idx - 1;
        // Retroceder si hay corchetes de arreglo tipo int[]
        if (k >= 1 && tokens.get(k).kind == AnalizadorConstants.CORCHETE_DER && tokens.get(k - 1).kind == AnalizadorConstants.CORCHETE_IZQ) {
            k -= 2;
        }
        while (k >= 0 && MODIFICADORES.contains(tokens.get(k).kind)) {
            k--;
        }
        if (k >= 0 && TIPOS_DATO.contains(tokens.get(k).kind)) {
            return true;
        }
        // Casos como "int a, b;"
        if (idx >= 2 && tokens.get(idx - 1).kind == AnalizadorConstants.COMA) {
            int j = idx - 2;
            while (j >= 0 && tokens.get(j).kind != AnalizadorConstants.PUNTO_COMA && tokens.get(j).kind != AnalizadorConstants.LLAVE_IZQ) {
                if (TIPOS_DATO.contains(tokens.get(j).kind)) {
                    return true;
                }
                j--;
            }
        }
        return false;
    }

    private static boolean esDestinoAsignacionSimple(List<Token> tokens, int idx) {
        // En una asignación simple: id = expr;
        // El id es destino si va seguido inmediatamente por '='
        if (idx + 1 < tokens.size()) {
            Token sig = tokens.get(idx + 1);
            if (sig.kind == AnalizadorConstants.ASIGNACION) {
                // Verificar que no sea parte de una condición relacional "==" o casteo
                if (idx > 0 && tokens.get(idx - 1).kind == AnalizadorConstants.PUNTO) {
                    return false; // campo.id = expr (afecta un objeto externo)
                }
                return true;
            }
        }
        return false;
    }
}
