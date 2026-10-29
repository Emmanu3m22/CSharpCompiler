package com.compilador.optimizacion.fuente;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reproduce el caso real: variables sin uso dentro de métodos de una clase,
 * con cadenas interpoladas que sí leen variables.
 */
public class VariablesSinUsoEnClaseTest {

    private static final String CORTO = """
            class SistemaVentas
            {
                static void Main()
                {
                    int precio = 250;
                    int cantidad = 3;

                    int variableMuerta1 = 100;
                    int variableMuerta2 = 200;
                    int variableMuerta3 = 300;

                    int subtotal = precio * cantidad;

                    Console.WriteLine(subtotal);
                }
            }
            """;

    private static final String COMPLETO = """
            class SistemaVentas
            {
                static void Main()
                {
                    double precioProducto = 250.0;
                    int cantidad = 3;
                    double descuento = 0.10;

                    double precioCuadrado = 62500.0;
                    double costoEnvio = 80.0;
                    int productosAdicionales = 5;

                    double subtotal = precioProducto * cantidad;
                    double montoDescuento = subtotal * descuento;
                    double total = subtotal - montoDescuento;

                    Console.WriteLine($"Precio unitario: {precioProducto}");
                    Console.WriteLine($"Cantidad: {cantidad}");
                    Console.WriteLine($"Subtotal: {subtotal}");
                    Console.WriteLine($"Descuento: {montoDescuento}");
                    Console.WriteLine($"Total a pagar: {total}");
                }
            }
            """;

    @Test
    @DisplayName("Elimina variables muertas dentro de Main en una clase")
    void eliminaMuertasEnClase() {
        String fin = OptimizadorFuente.crearPorDefecto().optimizar(CORTO).codigoFinal();
        assertFalse(fin.contains("variableMuerta"), fin);
        assertTrue(fin.contains("int precio = 250"), fin);
        assertTrue(fin.contains("int cantidad = 3"), fin);
        assertTrue(fin.contains("int subtotal"), fin);
    }

    @Test
    @DisplayName("Conserva variables leídas solo desde cadenas interpoladas")
    void conservaVariablesInterpoladas() {
        ResultadoOptimizacionFuente res = OptimizadorFuente.crearPorDefecto().optimizar(COMPLETO);
        String fin = res.codigoFinal();
        for (String v : new String[]{"precioProducto", "cantidad", "descuento", "subtotal", "montoDescuento", "total"}) {
            assertTrue(fin.contains("double " + v) || fin.contains("int " + v), "Debe conservar " + v + "\n" + fin);
        }
        for (String v : new String[]{"precioCuadrado", "costoEnvio", "productosAdicionales"}) {
            assertFalse(fin.contains(v), "Debe eliminar " + v + "\n" + fin);
        }
        assertNotNull(res.astLimpio());
    }

    @Test
    @DisplayName("Idempotencia sobre el programa completo")
    void idempotente() {
        OptimizadorFuente opt = OptimizadorFuente.crearPorDefecto();
        ResultadoOptimizacionFuente r1 = opt.optimizar(COMPLETO);
        ResultadoOptimizacionFuente r2 = opt.optimizar(r1.codigoFinal());
        assertEquals(0, r2.totalEliminaciones());
    }

    @Test
    @DisplayName("No elimina variables con efectos secundarios ni usadas en otra variable viva")
    void conservaEfectosYDependencias() {
        String src = """
                class C
                {
                    static int Calcula() { return 1; }
                    static void Main()
                    {
                        int a = 5;
                        int b = a + 1;
                        int ruido = Calcula();
                        Console.WriteLine(b);
                    }
                }
                """;
        String fin = OptimizadorFuente.crearPorDefecto().optimizar(src).codigoFinal();
        assertTrue(fin.contains("int a = 5"), fin);
        assertTrue(fin.contains("int b"), fin);
        assertTrue(fin.contains("Calcula()"), fin);
    }
}
