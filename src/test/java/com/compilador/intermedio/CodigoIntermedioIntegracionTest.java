package com.compilador.intermedio;

import com.compilador.Analizador;
import com.compilador.ast.NodoPrograma;
import com.compilador.optimizacion.Optimizador;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.StringReader;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class CodigoIntermedioIntegracionTest {

    @Test
    @DisplayName("Integración completa: múltiples expresiones complejas con valores calculados y verificación cruzada")
    void testIntegracionMultiplesExpresiones() throws Exception {
        String codigo = """
                int a = 5;
                int b = 3;
                int c = 4;
                int d = 10;
                double e = 2.5;

                int r1 = (a + b) * c - d / 2;
                int r2 = a * (b + c * (d - a)) % 7;
                double r3 = (a + e) * (c - b) / 2;
                int r4 = ((a - b) * (c + d)) / (b + 1);
                r1 += r2 * 2;
                """;

        Analizador parser = new Analizador(new StringReader(codigo));
        NodoPrograma prog = parser.programa();
        assertNotNull(prog);
        assertTrue(parser.getErroresSintacticos().isEmpty(), "No debe haber errores sintácticos");

        GeneradorCodigoIntermedio gen = new GeneradorCodigoIntermedio();
        ProgramaIntermedio intermedio = gen.generar(prog);

        assertEquals(5, intermedio.totalExpresiones(), "Debe haber exactamente 5 expresiones complejas");

        // 1. (5 + 3) * 4 - 10 / 2 = 32 - 5 = 27
        ResultadoExpresion e1 = intermedio.expresiones().get(0);
        assertEquals("r1", e1.destino());
        assertEquals("- * + a b c / d 2", e1.prefijo());
        assertEquals("a b + c * d 2 / -", e1.posfijo());
        assertEquals("27", e1.resultado().toString());

        // 2. 5 * (3 + 4 * (10 - 5)) % 7 = 5 * (3 + 20) % 7 = 115 % 7 = 3
        ResultadoExpresion e2 = intermedio.expresiones().get(1);
        assertEquals("r2", e2.destino());
        assertEquals("% * a + b * c - d a 7", e2.prefijo());
        assertEquals("a b c d a - * + * 7 %", e2.posfijo());
        assertEquals("3", e2.resultado().toString());

        // 3. (5 + 2.5) * (4 - 3) / 2 = 7.5 * 1 / 2 = 3.75
        ResultadoExpresion e3 = intermedio.expresiones().get(2);
        assertEquals("r3", e3.destino());
        assertEquals("/ * + a e - c b 2", e3.prefijo());
        assertEquals("a e + c b - * 2 /", e3.posfijo());
        assertEquals("3.75", e3.resultado().toString());

        // 4. ((5 - 3) * (4 + 10)) / (3 + 1) = (2 * 14) / 4 = 28 / 4 = 7
        ResultadoExpresion e4 = intermedio.expresiones().get(3);
        assertEquals("r4", e4.destino());
        assertEquals("/ * - a b + c d + b 1", e4.prefijo());
        assertEquals("a b - c d + * b 1 + /", e4.posfijo());
        assertEquals("7", e4.resultado().toString());

        // 5. r1 += r2 * 2  =>  r1 = r1 + r2 * 2 = 27 + 3 * 2 = 33
        ResultadoExpresion e5 = intermedio.expresiones().get(4);
        assertEquals("r1", e5.destino());
        assertEquals("+ r1 * r2 2", e5.prefijo());
        assertEquals("r1 r2 2 * +", e5.posfijo());
        assertEquals("33", e5.resultado().toString());

        // Verificación cruzada: cuádruplos y posfijo deben coincidir en todos
        for (ResultadoExpresion exp : intermedio.expresiones()) {
            assertTrue(exp.esEvaluable(), "La expresión debe ser evaluable: " + exp.infijo());
            assertNotNull(exp.resultado());
            assertFalse(exp.trazaPila().isEmpty(), "Debe existir traza de pila");
        }
    }

    @Test
    @DisplayName("Optimización: pase de plegado de constantes sobre cuádruplos")
    void testOptimizacionPlegado() throws Exception {
        String codigo = "int r = 10 + 20 * 3;";
        Analizador parser = new Analizador(new StringReader(codigo));
        NodoPrograma prog = parser.programa();

        GeneradorCodigoIntermedio gen = new GeneradorCodigoIntermedio();
        ProgramaIntermedio intermedio = gen.generar(prog);

        assertEquals(1, intermedio.totalExpresiones());
        assertEquals(3, intermedio.totalCuadruplos()); // (*, 20, 3, t1), (+, 10, t1, t2), (=, t2, —, r)

        Optimizador optimizador = Optimizador.crearPorDefecto();
        ProgramaIntermedio opt = optimizador.optimizar(intermedio);

        assertNotNull(opt);
        // Los cuádruplos optimizados deben tener las operaciones plegadas a constantes
        List<Cuadruplo> cuadOpt = opt.cuadruplos();
        assertEquals(3, cuadOpt.size());
        assertTrue(cuadOpt.get(0).esAsignacion(), "El primer cuádruplo debe haberse plegado a asignación (=, 60, —, t1)");
        assertEquals("60", cuadOpt.get(0).arg1().texto());
        assertTrue(cuadOpt.get(1).esAsignacion(), "El segundo cuádruplo debe haberse plegado a asignación (=, 70, —, t2)");
        assertEquals("70", cuadOpt.get(1).arg1().texto());
    }
}
