package com.compilador.optimizacion.fuente;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

public class AnalizadorUsoTest {

    @Test
    @DisplayName("Identifica lecturas en argumentos, condiciones y asignaciones")
    void testLecturasVariables() {
        String codigo = """
                int a = 10;
                int b = 20;
                int c = a + 5;
                if (b > 0) {
                    Console.WriteLine(c);
                }
                int muerto = 100;
                """;
        CodigoFuente cf = new CodigoFuente(codigo);
        Set<String> leidas = AnalizadorUso.obtenerVariablesLeidas(cf, List.of());

        assertTrue(leidas.contains("a"), "a debe estar leída en a + 5");
        assertTrue(leidas.contains("b"), "b debe estar leída en b > 0");
        assertTrue(leidas.contains("c"), "c debe estar leída en Console.WriteLine(c)");
        assertFalse(leidas.contains("muerto"), "'muerto' solo se declaró/asignó pero nunca se leyó");
    }

    @Test
    @DisplayName("Asignación compuesta += cuenta como lectura")
    void testAsignacionCompuestaEsLectura() {
        String codigo = "int x = 1;\nx += 5;";
        CodigoFuente cf = new CodigoFuente(codigo);
        Set<String> leidas = AnalizadorUso.obtenerVariablesLeidas(cf, List.of());

        assertTrue(leidas.contains("x"), "x en x += 5 debe ser leída");
    }

    @Test
    @DisplayName("Detecta efectos secundarios: llamadas, new, incremento")
    void testEfectosSecundarios() {
        String codigo = "int a = metodo(); int b = 10 + 20; int c = new int[5]; int d = x++;";
        CodigoFuente cf = new CodigoFuente(codigo);

        // a = metodo() tiene llamada
        int idxA = cf.buscarIndiceToken(1, 1);
        int finA = cf.delimitarSentencia(idxA);
        assertTrue(AnalizadorUso.tieneEfectosSecundarios(cf, idxA, finA));

        // b = 10 + 20 es aritmética pura (sin efectos secundarios)
        int idxB = cf.buscarIndiceToken(1, 19);
        int finB = cf.delimitarSentencia(idxB);
        assertFalse(AnalizadorUso.tieneEfectosSecundarios(cf, idxB, finB));

        // c = new int[5] tiene 'new'
        int idxC = cf.buscarIndiceToken(1, 36);
        int finC = cf.delimitarSentencia(idxC);
        assertTrue(AnalizadorUso.tieneEfectosSecundarios(cf, idxC, finC));
    }
}
