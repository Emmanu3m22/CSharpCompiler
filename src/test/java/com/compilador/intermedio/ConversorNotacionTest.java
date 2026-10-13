package com.compilador.intermedio;

import com.compilador.Analizador;
import com.compilador.ParseException;
import com.compilador.ast.NodoAsignacion;
import com.compilador.ast.NodoDeclaracion;
import com.compilador.ast.NodoOperacion;
import com.compilador.ast.NodoPrograma;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ConversorNotacionTest {

    private NodoPrograma parsear(String codigo) throws ParseException {
        Analizador parser = new Analizador(new StringReader(codigo));
        return parser.programa();
    }

    private com.compilador.ast.Nodo extraerExpresion(String sentencia) throws ParseException {
        Analizador parser = new Analizador(new StringReader(sentencia));
        NodoPrograma prog = parser.programa();
        assertNotNull(prog);
        if (prog.getSentencias().isEmpty()) {
            if (!parser.getErroresSintacticos().isEmpty()) {
                throw new AssertionError("Error sintáctico en '" + sentencia + "': " + parser.getErroresSintacticos());
            }
            throw new AssertionError("Programa vacío para '" + sentencia + "'");
        }
        com.compilador.ast.Nodo nodo = prog.getSentencias().get(0);
        if (nodo instanceof NodoDeclaracion decl) {
            return decl.getInicializacion();
        } else if (nodo instanceof NodoAsignacion asig) {
            return asig.getExpresion();
        }
        return nodo;
    }

    @ParameterizedTest(name = "{0} -> Prefijo: {1} | Posfijo: {2}")
    @CsvSource(delimiter = '|', value = {
            "x = a + b;                   | + a b                      | a b +",
            "x = a + b * c;               | + a * b c                  | a b c * +",
            "x = (a + b) * c;             | * + a b c                  | a b + c *",
            "x = a - b - c;               | - - a b c                  | a b - c -",
            "x = a / b * c;               | * / a b c                  | a b / c *",
            "x = a % b + c;               | + % a b c                  | a b % c +",
            "x = (a + b) * c - d / 2;     | - * + a b c / d 2          | a b + c * d 2 / -",
            "x = a * (b + c * (d - a)) % 7; | % * a + b * c - d a 7    | a b c d a - * + * 7 %",
            "x = 2.5 * -3;                | * 2.5 -3                   | 2.5 -3 *",
            "x = -a + b;                  | + neg a b                  | a neg b +",
            "x = -(a + b);                | neg + a b                  | a b + neg"
    })
    @DisplayName("Conversión de prefijo y posfijo en expresiones aritméticas")
    void testNotacionesPolacas(String codigo, String prefijoEsperado, String posfijoEsperado) throws Exception {
        com.compilador.ast.Nodo expr = extraerExpresion(codigo);
        assertNotNull(expr, "La expresión no debe ser nula");

        String prefijoReal = ConversorNotacion.prefijo(expr);
        String posfijoReal = ConversorNotacion.posfijo(expr);

        assertEquals(prefijoEsperado.trim(), prefijoReal.trim());
        assertEquals(posfijoEsperado.trim(), posfijoReal.trim());
    }

    @Test
    @DisplayName("Reconstrucción infija sin paréntesis redundantes")
    void testInfijoSinParentesisRedundantes() throws Exception {
        com.compilador.ast.Nodo expr = extraerExpresion("x = ((a + b)) * (c);");
        assertEquals("(a + b) * c", ConversorNotacion.infijo(expr));

        com.compilador.ast.Nodo expr2 = extraerExpresion("x = a + (b * c);");
        assertEquals("a + b * c", ConversorNotacion.infijo(expr2));

        com.compilador.ast.Nodo expr3 = extraerExpresion("x = a - (b - c);");
        assertEquals("a - (b - c)", ConversorNotacion.infijo(expr3));
    }
}
