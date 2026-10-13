package com.compilador.intermedio;

import com.compilador.Analizador;
import com.compilador.ParseException;
import com.compilador.ast.NodoPrograma;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class RecolectorExpresionesTest {

    private NodoPrograma parsear(String codigo) throws ParseException {
        Analizador parser = new Analizador(new StringReader(codigo));
        return parser.programa();
    }

    @Test
    @DisplayName("Recolecta exactamente las expresiones aritméticas raíz en declaraciones y asignaciones")
    void testRecolectaDeclaracionesYAsignaciones() throws Exception {
        String codigo = """
                int a = 5;
                int b = 3;
                int c = a + b * 2;
                int d = (c - 1) / 2;
                c += 10;
                """;
        NodoPrograma prog = parsear(codigo);
        GeneradorCodigoIntermedio gen = new GeneradorCodigoIntermedio();
        ProgramaIntermedio intermedio = gen.generar(prog);

        // a=5 y b=3 son literales (no aritméticos).
        // c = a + b * 2 (1)
        // d = (c - 1) / 2 (2)
        // c += 10 => c = c + 10 (3)
        assertEquals(3, intermedio.totalExpresiones());

        ResultadoExpresion e1 = intermedio.expresiones().get(0);
        assertEquals("c", e1.destino());
        assertEquals("a + b * 2", e1.infijo());
        assertEquals("+ a * b 2", e1.prefijo());
        assertEquals("a b 2 * +", e1.posfijo());
        assertEquals("11", e1.resultado().toString()); // 5 + 3*2 = 11

        ResultadoExpresion e2 = intermedio.expresiones().get(1);
        assertEquals("d", e2.destino());
        assertEquals("(c - 1) / 2", e2.infijo());
        assertEquals("5", e2.resultado().toString()); // (11 - 1) / 2 = 5

        ResultadoExpresion e3 = intermedio.expresiones().get(2);
        assertEquals("c", e3.destino());
        assertEquals("c + 10", e3.infijo());
        assertEquals("21", e3.resultado().toString()); // 11 + 10 = 21
    }

    @Test
    @DisplayName("Recolecta operaciones aritméticas dentro de estructuras de control (if, while, comandos)")
    void testRecolectaEstructurasDeControl() throws Exception {
        String codigo = """
                int a = 10;
                int b = 2;
                if (a + b > 5) {
                    Console.WriteLine(a * b);
                }
                while (a - 1 > 0) {
                    b = b + 1;
                }
                """;
        NodoPrograma prog = parsear(codigo);
        GeneradorCodigoIntermedio gen = new GeneradorCodigoIntermedio();
        ProgramaIntermedio intermedio = gen.generar(prog);

        // 1. a + b (dentro de la condición del if, sin destino)
        // 2. a * b (dentro del Console.WriteLine, sin destino)
        // 3. a - 1 (dentro de la condición del while, sin destino)
        // 4. b + 1 (asignación b = b + 1, con destino b)
        assertEquals(4, intermedio.totalExpresiones());

        ResultadoExpresion eIf = intermedio.expresiones().get(0);
        assertNull(eIf.destino());
        assertEquals("a + b", eIf.infijo());
        assertEquals("12", eIf.resultado().toString());

        ResultadoExpresion ePrint = intermedio.expresiones().get(1);
        assertNull(ePrint.destino());
        assertEquals("a * b", ePrint.infijo());
        assertEquals("20", ePrint.resultado().toString());
    }

    @Test
    @DisplayName("No recolecta expresiones puramente lógicas o relacionales si no tienen aritmética")
    void testIgnoraLogicaSinAritmetica() throws Exception {
        String codigo = """
                bool x = true;
                bool y = false;
                if (x && !y) {
                    x = y;
                }
                """;
        NodoPrograma prog = parsear(codigo);
        GeneradorCodigoIntermedio gen = new GeneradorCodigoIntermedio();
        ProgramaIntermedio intermedio = gen.generar(prog);

        assertEquals(0, intermedio.totalExpresiones());
        assertEquals(0, intermedio.totalCuadruplos());
    }
}
