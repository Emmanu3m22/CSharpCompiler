package com.compilador.optimizacion.fuente;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class EliminadorCodigoMuertoTest {

    @Test
    @DisplayName("R1: Elimina sentencias tras return en el mismo ámbito")
    void testR1InalcanzableTrasReturn() {
        String codigo = """
                int a = 5;
                return a;
                int muerto = 10;
                Console.WriteLine(muerto);
                """;

        EliminadorCodigoMuerto eliminador = new EliminadorCodigoMuerto();
        EtapaOptimizacion res = eliminador.aplicar(new CodigoFuente(codigo));

        assertTrue(res.hayCambios());
        assertEquals(2, res.totalEliminaciones());

        Hallazgo h1 = res.hallazgos().get(0);
        assertEquals(Hallazgo.Tipo.INALCANZABLE, h1.tipo());
        assertTrue(h1.motivo().contains("return"));
        assertTrue(h1.codigoEliminado().contains("int muerto = 10;"));

        Hallazgo h2 = res.hallazgos().get(1);
        assertEquals(Hallazgo.Tipo.INALCANZABLE, h2.tipo());
        assertTrue(h2.motivo().contains("return"));
        assertTrue(h2.codigoEliminado().contains("Console.WriteLine(muerto);"));

        assertTrue(res.codigoDespues().contains("int a = 5;"));
        assertTrue(res.codigoDespues().contains("return a;"));
        assertFalse(res.codigoDespues().contains("int muerto = 10;"));
        assertFalse(res.codigoDespues().contains("Console.WriteLine(muerto);"));
    }

    @Test
    @DisplayName("R1: Sentencias en bloques separados no se consideran inalcanzables")
    void testR1BloquesSeparados() {
        String codigo = """
                if (x > 0) {
                    return 1;
                }
                int vivo = 2;
                Console.WriteLine(vivo);
                """;

        EliminadorCodigoMuerto eliminador = new EliminadorCodigoMuerto();
        EtapaOptimizacion res = eliminador.aplicar(new CodigoFuente(codigo));

        assertFalse(res.codigoDespues().contains("muerto"));
        assertTrue(res.codigoDespues().contains("int vivo = 2;"));
    }

    @Test
    @DisplayName("R2: Elimina if (false) y while (false) por condición constante")
    void testR2CondicionConstanteFalsa() {
        String codigo = """
                int a = 1;
                if (false) {
                    int b = 2;
                    Console.WriteLine(b);
                }
                while (false) {
                    int c = 3;
                }
                Console.WriteLine(a);
                """;

        EliminadorCodigoMuerto eliminador = new EliminadorCodigoMuerto();
        EtapaOptimizacion res = eliminador.aplicar(new CodigoFuente(codigo));

        assertTrue(res.hayCambios());
        assertEquals(2, res.totalEliminaciones());

        for (Hallazgo h : res.hallazgos()) {
            assertEquals(Hallazgo.Tipo.CONDICION_CONSTANTE, h.tipo());
            assertTrue(h.motivo().contains("false"));
        }

        assertFalse(res.codigoDespues().contains("if (false)"));
        assertFalse(res.codigoDespues().contains("while (false)"));
        assertTrue(res.codigoDespues().contains("int a = 1;"));
        assertTrue(res.codigoDespues().contains("Console.WriteLine(a);"));
    }

    @Test
    @DisplayName("R3: if (true) elimina la rama else inalcanzable")
    void testR3IfTrueEliminaElse() {
        String codigo = """
                int x = 10;
                if (true) {
                    Console.WriteLine(x);
                } else {
                    int muerto = 99;
                }
                """;

        EliminadorCodigoMuerto eliminador = new EliminadorCodigoMuerto();
        EtapaOptimizacion res = eliminador.aplicar(new CodigoFuente(codigo));

        assertTrue(res.hayCambios());
        assertEquals(1, res.totalEliminaciones());

        Hallazgo h = res.hallazgos().get(0);
        assertEquals(Hallazgo.Tipo.CONDICION_CONSTANTE, h.tipo());
        assertTrue(h.motivo().contains("else"));

        assertTrue(res.codigoDespues().contains("if (true)"));
        assertTrue(res.codigoDespues().contains("Console.WriteLine(x);"));
        assertFalse(res.codigoDespues().contains("else"));
        assertFalse(res.codigoDespues().contains("int muerto = 99;"));
    }

    @Test
    @DisplayName("Condiciones variables o no constantes se conservan intactas")
    void testCondicionNoConstanteSeConserva() {
        String codigo = """
                int a = 5;
                if (a > 0) {
                    Console.WriteLine(a);
                }
                """;

        EliminadorCodigoMuerto eliminador = new EliminadorCodigoMuerto();
        EtapaOptimizacion res = eliminador.aplicar(new CodigoFuente(codigo));

        assertFalse(res.hayCambios());
        assertEquals(codigo, res.codigoDespues());
    }
}
