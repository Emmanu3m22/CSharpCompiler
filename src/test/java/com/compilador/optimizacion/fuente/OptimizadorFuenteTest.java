package com.compilador.optimizacion.fuente;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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
    @DisplayName("crearPorDefecto() contiene EliminadorCodigoMuerto y PropagadorCopias")
    void testCrearPorDefecto() {
        OptimizadorFuente opt = OptimizadorFuente.crearPorDefecto();
        assertNotNull(opt);
        assertEquals(2, opt.getPases().size());
        assertInstanceOf(EliminadorCodigoMuerto.class, opt.getPases().get(0));
        assertInstanceOf(PropagadorCopias.class, opt.getPases().get(1));
    }

    @Test
    @DisplayName("Elimina comentarios aunque no haya código muerto ni copias (Ejemplo_Sin_Errores.txt)")
    void testQuitaComentariosAunqueNoHayaCodigoMuertoNiCopias() throws Exception {
        String codigoArchivo = Files.readString(Path.of("Ejemplo_Sin_Errores.txt"), StandardCharsets.UTF_8);
        OptimizadorFuente opt = OptimizadorFuente.crearPorDefecto();
        ResultadoOptimizacionFuente res = opt.optimizar(codigoArchivo);

        assertTrue(res.verificacion().valido(), res.verificacion().errores().toString());
        assertTrue(res.aplicable());
        assertFalse(res.codigoFinal().contains("//"), "No deben quedar comentarios de línea en codigoFinal");
        assertFalse(res.codigoFinal().contains("/*"), "No deben quedar comentarios de bloque en codigoFinal");
        assertTrue(res.codigoFinal().contains("static void Main()"));
        assertTrue(res.codigoFinal().contains("MostrarResumen(subtotal, descuento, impuesto, total);"));

        String codigoInlineYBloque = """
                // Comentario inicial
                /* Bloque con acentos: á é í ó ú */
                class P
                {
                    static void Main()
                    {
                        int valor = 42; // comentario en misma línea
                        Console.WriteLine(valor);
                    }
                }
                """;
        ResultadoOptimizacionFuente res2 = opt.optimizar(codigoInlineYBloque);
        assertTrue(res2.verificacion().valido());
        assertFalse(res2.codigoFinal().contains("//"));
        assertFalse(res2.codigoFinal().contains("/*"));
        assertTrue(res2.codigoFinal().contains("int valor = 42;"));
    }

    @Test
    @DisplayName("Optimizacion_Completa.txt elimina comentarios, copias, variables sin uso y ramas muertas")
    void testArchivoOptimizacionCompleta() throws Exception {
        String codigo = Files.readString(Path.of("Optimizacion_Completa.txt"), StandardCharsets.UTF_8);
        OptimizadorFuente opt = OptimizadorFuente.crearPorDefecto();
        ResultadoOptimizacionFuente res = opt.optimizar(codigo);

        assertTrue(res.verificacion().valido(), res.verificacion().errores().toString());
        assertTrue(res.aplicable());
        assertNotNull(res.astLimpio());

        String limpio = res.codigoFinal();
        assertFalse(limpio.contains("//"), "No deben quedar comentarios //");
        assertFalse(limpio.contains("/*"), "No deben quedar comentarios /*");
        assertFalse(limpio.contains("\n\n\n"), "No deben quedar múltiples líneas en blanco seguidas");

        // Variables sin uso (R4) eliminadas
        for (String varMuerta : List.of(
                "pedidosCancelados", "mesasDisponibles", "empleadosTemporales",
                "costoPublicidad", "propinaTemporal", "costoEnvase",
                "descuentoEspecial", "puntosPromocion")) {
            assertFalse(limpio.contains(varMuerta), "No debe permanecer variable sin uso: " + varMuerta);
        }

        // Copias propagadas y declaraciones eliminadas
        assertFalse(limpio.contains("copiaPrecio"));
        assertFalse(limpio.contains("copiaCantidad"));
        assertFalse(limpio.contains("copiaPostre"));
        assertTrue(limpio.contains("int subtotalCafe = precioCafe * cantidad;"));
        assertTrue(limpio.contains("int subtotalPostre = precioPostre * 2;"));

        // Condiciones constantes (R2/R3) y código inalcanzable (R1) eliminados
        assertFalse(limpio.contains("if (false)"));
        assertFalse(limpio.contains("while (false)"));
        assertFalse(limpio.contains("else"));
        assertFalse(limpio.contains("pedidoInexistente"));
        assertFalse(limpio.contains("productoTemporal"));
        assertFalse(limpio.contains("pedidoAlternativo"));
        assertFalse(limpio.contains("codigoInalcanzable"));
        assertTrue(limpio.contains("Console.WriteLine(\"Pedido registrado correctamente\");"));
        assertTrue(limpio.contains("return 0;"));
    }

    @Test
    @DisplayName("Multi-ronda: PropagadorCopias libera copia asignada y EliminadorCodigoMuerto elimina su declaración")
    void testMultiRondaPropagacionCopiasYVariablesSinUso() {
        String codigo = """
                class P
                {
                    static void Main()
                    {
                        // Acentos en misma línea con tab: \t acción óptima
                        int orig = 10;
                        int copia = 0;
                        copia = orig;
                        int r = copia + 1;
                        Console.WriteLine(r);
                    }
                }
                """;
        OptimizadorFuente opt = OptimizadorFuente.crearPorDefecto();
        ResultadoOptimizacionFuente res = opt.optimizar(codigo);

        assertTrue(res.verificacion().valido(), res.verificacion().errores().toString());
        assertTrue(res.aplicable());
        String limpio = res.codigoFinal();
        assertFalse(limpio.contains("//"), "No deben quedar comentarios");
        assertFalse(limpio.contains("copia"), "La variable 'copia' ya sin uso debe eliminarse en ronda 2");
        assertTrue(limpio.contains("int orig = 10;"));
        assertTrue(limpio.contains("int r = orig + 1;"));
        assertTrue(limpio.contains("Console.WriteLine(r);"));
    }

    @Test
    @DisplayName("Columnas con caracteres UTF-8 multibyte antes de declaración en la misma línea coinciden entre AST y CodigoFuente")
    void testAcentosUTF8EnMismaLinea() {
        String codigo = """
                class P
                {
                    static void Main()
                    {
                        /* áéíóúñ */ int muerta = 99;
                        /* áéíóú */ int orig = 10;
                        /* áéíóú */ int copia = orig;
                        /* áéíóú */ int viva = copia + 5;
                        Console.WriteLine(viva);
                    }
                }
                """;
        ResultadoOptimizacionFuente res = OptimizadorFuente.crearPorDefecto().optimizar(codigo);

        assertTrue(res.verificacion().valido(), res.verificacion().errores().toString());
        assertFalse(res.codigoFinal().contains("muerta"));
        assertFalse(res.codigoFinal().contains("copia"));
        assertFalse(res.codigoFinal().contains("/*"));
        assertTrue(res.codigoFinal().contains("int viva = orig + 5;"));
    }
}
