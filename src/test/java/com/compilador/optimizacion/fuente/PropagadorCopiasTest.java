package com.compilador.optimizacion.fuente;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class PropagadorCopiasTest {

    @Test
    void propagaYEliminaCopiaLocalCuandoYaNoHaceFalta() {
        String codigo = """
                int precio = 100;
                int copiaPrecio = precio;
                int cantidad = 3;

                int total = copiaPrecio * cantidad;

                Console.WriteLine(total);
                """;

        EtapaOptimizacion resultado = new PropagadorCopias().aplicar(new CodigoFuente(codigo));

        assertTrue(resultado.hayCambios());
        assertEquals(Hallazgo.Tipo.PROPAGACION_COPIAS, resultado.hallazgos().get(0).tipo());
        assertEquals("Propagación de Copias", resultado.hallazgos().get(0).tipo().getEtiqueta());
        assertTrue(resultado.codigoDespues().contains("int total = precio * cantidad;"));
        assertFalse(resultado.codigoDespues().contains("copiaPrecio"));
    }

    @Test
    void noPropagaUsosPosterioresALaModificacionDelOriginal() {
        String codigo = """
                int original = 10;
                int copia = original;
                original = 20;
                int total = copia + 1;
                """;

        EtapaOptimizacion resultado = new PropagadorCopias().aplicar(new CodigoFuente(codigo));

        assertTrue(resultado.codigoDespues().contains("int total = copia + 1;"));
        assertTrue(resultado.codigoDespues().contains("int copia = original;"));
    }

    @Test
    void noPropagaDespuesDeModificarLaCopia() {
        String codigo = """
                int original = 10;
                int copia = original;
                copia = 20;
                int total = copia + 1;
                """;

        EtapaOptimizacion resultado = new PropagadorCopias().aplicar(new CodigoFuente(codigo));

        assertTrue(resultado.codigoDespues().contains("int total = copia + 1;"));
        assertTrue(resultado.codigoDespues().contains("int copia = original;"));
    }

    @Test
    void propagaYEliminaUnaAsignacionSimplePosterior() {
        String codigo = """
                int original = 10;
                int copia = 0;
                copia = original;
                int total = copia + 1;
                """;

        EtapaOptimizacion resultado = new PropagadorCopias().aplicar(new CodigoFuente(codigo));

        assertTrue(resultado.codigoDespues().contains("int copia = 0;"));
        assertFalse(resultado.codigoDespues().contains("copia = original;"));
        assertTrue(resultado.codigoDespues().contains("int total = original + 1;"));
    }

    @Test
    void evitaReferenciasIncorrectasAlEncontrarCopiasEncadenadas() {
        String codigo = """
                int original = 10;
                int copia1 = original;
                int copia2 = copia1;
                int total = copia2 + 1;
                """;

        EtapaOptimizacion resultado = new PropagadorCopias().aplicar(new CodigoFuente(codigo));

        assertTrue(resultado.codigoDespues().contains("int copia2 = original;"));
        assertTrue(resultado.codigoDespues().contains("int total = copia2 + 1;"));
        assertFalse(resultado.codigoDespues().contains("int total = copia1 + 1;"));
    }

    @Test
    void propagaLecturasPreviasPeroConservaLaCopiaUsadaTrasUnaModificacion() {
        String codigo = """
                int original = 10;
                int copia = original;
                Console.WriteLine(copia);
                original = 20;
                Console.WriteLine(copia);
                """;

        EtapaOptimizacion resultado = new PropagadorCopias().aplicar(new CodigoFuente(codigo));

        assertTrue(resultado.codigoDespues().contains("Console.WriteLine(original);"));
        assertTrue(resultado.codigoDespues().contains("int copia = original;"));
        assertTrue(resultado.codigoDespues().contains("Console.WriteLine(copia);"));
    }

    @Test
    void noPropagaEntreBloquesConVariablesDelMismoNombre() {
        String codigo = """
                int original = 10;
                int copia = original;
                if (true) {
                    int copia = 20;
                    Console.WriteLine(copia);
                }
                Console.WriteLine(copia);
                """;

        EtapaOptimizacion resultado = new PropagadorCopias().aplicar(new CodigoFuente(codigo));

        assertTrue(resultado.codigoDespues().contains("Console.WriteLine(copia);"));
        assertFalse(resultado.codigoDespues().contains("Console.WriteLine(original);"));
    }

    @Test
    void optimizadorPorDefectoRegistraElPaseSinEliminarElPaseExistente() {
        var pases = OptimizadorFuente.crearPorDefecto().getPases();

        assertEquals(2, pases.size());
        assertInstanceOf(EliminadorCodigoMuerto.class, pases.get(0));
        assertInstanceOf(PropagadorCopias.class, pases.get(1));
    }
}
