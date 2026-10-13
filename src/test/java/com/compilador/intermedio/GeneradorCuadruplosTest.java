package com.compilador.intermedio;

import com.compilador.Analizador;
import com.compilador.ParseException;
import com.compilador.ast.NodoAsignacion;
import com.compilador.ast.NodoPrograma;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class GeneradorCuadruplosTest {

    private com.compilador.ast.Nodo parsearExpresion(String codigo) throws ParseException {
        Analizador parser = new Analizador(new StringReader(codigo));
        NodoPrograma prog = parser.programa();
        NodoAsignacion asig = (NodoAsignacion) prog.getSentencias().get(0);
        return asig.getExpresion();
    }

    @Test
    @DisplayName("Genera cuádruplos respetando precedencia y asignación final")
    void testCuadruplosPrecedencia() throws Exception {
        com.compilador.ast.Nodo expr = parsearExpresion("r = a + b * c;");
        GeneradorCuadruplos gen = new GeneradorCuadruplos();
        List<Cuadruplo> cuadruplos = gen.generar(expr, "r");

        assertEquals(3, cuadruplos.size());

        // 1. t1 = b * c
        Cuadruplo c1 = cuadruplos.get(0);
        assertEquals("*", c1.operador());
        assertEquals("b", c1.arg1().texto());
        assertEquals(Operando.Tipo.VARIABLE, c1.arg1().tipo());
        assertEquals("c", c1.arg2().texto());
        assertEquals("t1", c1.resultado().texto());
        assertEquals(Operando.Tipo.TEMPORAL, c1.resultado().tipo());

        // 2. t2 = a + t1
        Cuadruplo c2 = cuadruplos.get(1);
        assertEquals("+", c2.operador());
        assertEquals("a", c2.arg1().texto());
        assertEquals("t1", c2.arg2().texto());
        assertEquals("t2", c2.resultado().texto());

        // 3. r = t2
        Cuadruplo c3 = cuadruplos.get(2);
        assertTrue(c3.esAsignacion());
        assertEquals("t2", c3.arg1().texto());
        assertNull(c3.arg2());
        assertEquals("r", c3.resultado().texto());
    }

    @Test
    @DisplayName("Temporales continuos a lo largo de varias expresiones")
    void testTemporalesContinuos() throws Exception {
        com.compilador.ast.Nodo expr1 = parsearExpresion("x = a + b;");
        com.compilador.ast.Nodo expr2 = parsearExpresion("y = c * d;");

        GeneradorCuadruplos gen = new GeneradorCuadruplos();
        List<Cuadruplo> l1 = gen.generar(expr1, "x");
        List<Cuadruplo> l2 = gen.generar(expr2, "y");

        assertEquals("t1", l1.get(0).resultado().texto());
        assertEquals("t2", l2.get(0).resultado().texto());
        assertEquals(2, gen.getContadorTemporales());
    }

    @Test
    @DisplayName("Expresión sin destino no genera cuádruplo de asignación")
    void testSinDestino() throws Exception {
        com.compilador.ast.Nodo expr = parsearExpresion("x = a + b;");
        GeneradorCuadruplos gen = new GeneradorCuadruplos();
        List<Cuadruplo> cuadruplos = gen.generar(expr, null);

        assertEquals(1, cuadruplos.size());
        assertEquals("+", cuadruplos.get(0).operador());
        assertEquals("t1", cuadruplos.get(0).resultado().texto());
    }

    @Test
    @DisplayName("Menos unario genera cuádruplo con operador neg")
    void testMenosUnarioCuadruplo() throws Exception {
        com.compilador.ast.Nodo expr = parsearExpresion("x = -a + b;");
        GeneradorCuadruplos gen = new GeneradorCuadruplos();
        List<Cuadruplo> cuadruplos = gen.generar(expr, "x");

        assertEquals(3, cuadruplos.size());
        assertEquals("neg", cuadruplos.get(0).operador());
        assertEquals("a", cuadruplos.get(0).arg1().texto());
        assertEquals("t1", cuadruplos.get(0).resultado().texto());

        assertEquals("+", cuadruplos.get(1).operador());
        assertEquals("t1", cuadruplos.get(1).arg1().texto());
        assertEquals("b", cuadruplos.get(1).arg2().texto());
        assertEquals("t2", cuadruplos.get(1).resultado().texto());
    }
}
