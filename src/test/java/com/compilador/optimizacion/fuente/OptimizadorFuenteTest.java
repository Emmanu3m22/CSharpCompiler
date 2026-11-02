package com.compilador.optimizacion.fuente;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class OptimizadorFuenteTest {

    @Test
    @DisplayName("Optimizador vacío devuelve resultado sin cambios")
    void testOptimizadorVacio() {
        OptimizadorFuente opt = new OptimizadorFuente();
        String codigo = "int x = 10;\nConsole.WriteLine(x);";
        ResultadoOptimizacionFuente res = opt.optimizar(codigo);

        assertNotNull(res);
        assertEquals(codigo, res.codigoOriginal());
        assertEquals(codigo, res.codigoFinal());
        assertTrue(res.etapas().isEmpty());
        assertEquals(0, res.totalEliminaciones());
    }

    @Test
    @DisplayName("Maneja código nulo y vacío")
    void testCodigoNuloYVacio() {
        OptimizadorFuente opt = new OptimizadorFuente();
        ResultadoOptimizacionFuente resNulo = opt.optimizar(null);
        assertNotNull(resNulo);
        assertEquals("", resNulo.codigoOriginal());

        ResultadoOptimizacionFuente resVacio = opt.optimizar("   ");
        assertNotNull(resVacio);
    }

    @Test
    @DisplayName("Encadena pases y propaga versión enmascarada entre ellos")
    void testEncadenamientoDePases() {
        OptimizadorFuente opt = new OptimizadorFuente();

        // Pase 1 falso: elimina "LINEA_MUERTA_1;\n"
        opt.agregarPase(new PaseOptimizacionFuente() {
            @Override
            public String nombre() { return "Pase Falso 1"; }
            @Override
            public String descripcion() { return "Elimina palabra muerta 1"; }
            @Override
            public EtapaOptimizacion aplicar(CodigoFuente fuente) {
                String txt = fuente.getTexto();
                String target = "LINEA_MUERTA_1;";
                int idx = txt.indexOf(target);
                if (idx >= 0) {
                    RangoTexto r = new RangoTexto(idx, idx + target.length());
                    Hallazgo h = new Hallazgo(1, Hallazgo.Tipo.OTRO, target, "Prueba pase 1");
                    String enmascarado = fuente.enmascarar(List.of(r));
                    String colapsado = fuente.colapsar(List.of(r));
                    return new EtapaOptimizacion(nombre(), descripcion(), txt, colapsado, enmascarado,
                            List.of(h), List.of(r), 1000L);
                }
                return EtapaOptimizacion.sinCambios(nombre(), descripcion(), txt, 0L);
            }
        });

        // Pase 2 falso: comprueba que recibe el texto enmascarado del pase 1
        opt.agregarPase(new PaseOptimizacionFuente() {
            @Override
            public String nombre() { return "Pase Falso 2"; }
            @Override
            public String descripcion() { return "Verifica propagación"; }
            @Override
            public EtapaOptimizacion aplicar(CodigoFuente fuente) {
                // En el texto enmascarado, "LINEA_MUERTA_1;" ya son espacios
                assertFalse(fuente.getTexto().contains("LINEA_MUERTA_1;"));
                return EtapaOptimizacion.sinCambios(nombre(), descripcion(), fuente.getTexto(), 500L);
            }
        });

        String original = "int a = 1;\nLINEA_MUERTA_1;\nint b = 2;";
        ResultadoOptimizacionFuente res = opt.optimizar(original);

        assertEquals(2, res.etapas().size());
        assertEquals(1, res.totalEliminaciones());
        assertFalse(res.codigoFinal().contains("LINEA_MUERTA_1;"));
        assertTrue(res.codigoFinal().contains("int a = 1;"));
        assertTrue(res.codigoFinal().contains("int b = 2;"));
    }

    @Test
    @DisplayName("crearPorDefecto() contiene EliminadorCodigoMuerto")
    void testCrearPorDefecto() {
        OptimizadorFuente opt = OptimizadorFuente.crearPorDefecto();
        assertNotNull(opt);
        assertEquals(1, opt.getPases().size());
        assertInstanceOf(EliminadorCodigoMuerto.class, opt.getPases().get(0));
    }
}
