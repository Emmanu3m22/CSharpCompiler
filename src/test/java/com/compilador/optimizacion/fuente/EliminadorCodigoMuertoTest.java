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

        // Ambos hallazgos son INALCANZABLE
        Hallazgo h1 = res.hallazgos().get(0);
        assertEquals(Hallazgo.Tipo.INALCANZABLE, h1.tipo());
        assertTrue(h1.motivo().contains("return"));
        assertTrue(h1.codigoEliminado().contains("int muerto = 10;"));

        Hallazgo h2 = res.hallazgos().get(1);
        assertEquals(Hallazgo.Tipo.INALCANZABLE, h2.tipo());
        assertTrue(h2.motivo().contains("return"));
        assertTrue(h2.codigoEliminado().contains("Console.WriteLine(muerto);"));

        // El código después conserva return a; y descarta lo posterior
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

        // 'int vivo = 2;' está fuera del if, por lo que NO es inalcanzable tras el return del bloque then
        assertFalse(res.codigoDespues().contains("muerto"));
        assertTrue(res.codigoDespues().contains("int vivo = 2;"));
    }
}
