package com.compilador.optimizacion.fuente;

import com.compilador.Analizador;
import com.compilador.ast.NodoIf;
import com.compilador.ast.NodoPrograma;
import com.compilador.ast.RecorridoAST;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Robustez del pipeline: casos límite de seguridad, aislamiento de pases, rendimiento
 * y piezas de infraestructura ({@link RangosMarcados}, {@link RecorridoAST}).
 */
public class RobustezOptimizacionTest {

    private static String optimizar(String codigo) {
        return OptimizadorFuente.crearPorDefecto().optimizar(codigo).codigoFinal();
    }

    private static String enClase(String cuerpoMain) {
        return "class C\n{\n    static void Main()\n    {\n" + cuerpoMain + "\n    }\n}\n";
    }

    // ---------- Seguridad del análisis de variables ----------

    @Test
    @DisplayName("Asignación compuesta sin lectores: declaración y 'x += 1' se eliminan")
    void asignacionCompuestaSinUso() {
        String fin = optimizar(enClase("int x = 0;\nx += 1;\nint y = 2;\nConsole.WriteLine(y);"));
        assertFalse(fin.contains("int x"), fin);
        assertFalse(fin.contains("x +="), fin);
        assertTrue(fin.contains("int y = 2"), fin);
    }

    @Test
    @DisplayName("Asignación compuesta leída en una condición: se conserva")
    void asignacionCompuestaConLector() {
        String fin = optimizar(enClase("int i = 0;\nwhile (i < 3) { i += 1; }\nConsole.WriteLine(1);"));
        assertTrue(fin.contains("int i = 0"), fin);
        assertTrue(fin.contains("i += 1"), fin);
    }

    @Test
    @DisplayName("Escrituras dentro de try (no visibles al recolector) mantienen viva la declaración")
    void escrituraDentroDeTry() {
        String codigo = enClase("int x = 0;\ntry { x = 5; } catch { }\nConsole.WriteLine(1);");
        ResultadoOptimizacionFuente res = OptimizadorFuente.crearPorDefecto().optimizar(codigo);
        assertTrue(res.codigoFinal().contains("int x = 0"), res.codigoFinal());
        assertEquals(0, ValidadorSintaxis.contarErrores(res.codigoEnmascaradoFinal()));
    }

    @Test
    @DisplayName("Incremento ++ cuenta como efecto secundario: no se elimina")
    void incrementoSeConserva() {
        String fin = optimizar(enClase("int n = 0;\nn++;\nConsole.WriteLine(1);"));
        assertTrue(fin.contains("n++"), fin);
        assertTrue(fin.contains("int n = 0"), fin);
    }

    @Test
    @DisplayName("Asignaciones a parámetros del método no se eliminan")
    void parametrosSeConservan() {
        String codigo = """
                class C
                {
                    static void Fija(int r)
                    {
                        r = 3;
                    }
                    static void Main() { Console.WriteLine(1); }
                }
                """;
        assertTrue(optimizar(codigo).contains("r = 3"));
    }

    @Test
    @DisplayName("Arreglos: creación y escritura de elementos se conservan si se leen")
    void arreglosSeConservan() {
        String fin = optimizar(enClase("int[] arr = new int[3];\narr[0] = 1;\nConsole.WriteLine(arr[0]);"));
        assertTrue(fin.contains("new int[3]"), fin);
        assertTrue(fin.contains("arr[0] = 1"), fin);
    }

    @Test
    @DisplayName("Campos de clase no leídos no se eliminan")
    void camposSeConservan() {
        String codigo = """
                class C
                {
                    int campo = 5;
                    static void Main() { Console.WriteLine(1); }
                }
                """;
        assertTrue(optimizar(codigo).contains("int campo = 5"));
    }

    @Test
    @DisplayName("Punto fijo: cadena a -> b -> c sin lectores se elimina completa")
    void puntoFijoCadena() {
        String fin = optimizar(enClase("int a = 1;\nint b = a;\nint c = b;\nConsole.WriteLine(7);"));
        assertFalse(fin.contains("int a"), fin);
        assertFalse(fin.contains("int b"), fin);
        assertFalse(fin.contains("int c"), fin);
    }

    @Test
    @DisplayName("Interpolación con expresiones y formato conserva las variables usadas")
    void interpolacionConFormato() {
        String fin = optimizar(enClase("double p = 2.5;\nint q = 4;\nint z = 9;\nConsole.WriteLine($\"{p:F2} y {q + 1}\");"));
        assertTrue(fin.contains("double p"), fin);
        assertTrue(fin.contains("int q"), fin);
        assertFalse(fin.contains("int z"), fin);
    }

    // ---------- Aislamiento de pases ----------

    @Test
    @DisplayName("Un pase que lanza excepción se omite y se reporta como advertencia")
    void paseQueFalla() {
        PaseOptimizacionFuente roto = new PaseOptimizacionFuente() {
            public String nombre() { return "Roto"; }
            public String descripcion() { return "siempre falla"; }
            public EtapaOptimizacion aplicar(CodigoFuente f) { throw new IllegalStateException("boom"); }
        };
        String codigo = enClase("int a = 1;\nConsole.WriteLine(a);");
        ResultadoOptimizacionFuente res = new OptimizadorFuente().agregarPase(roto).optimizar(codigo);
        assertEquals(1, res.advertencias().size());
        assertEquals(codigo, res.codigoFinal());
    }

    @Test
    @DisplayName("Un pase que deja código inválido se revierte")
    void paseQueCorrompe() {
        PaseOptimizacionFuente corrupto = new PaseOptimizacionFuente() {
            public String nombre() { return "Corrupto"; }
            public String descripcion() { return "rompe la sintaxis"; }
            public EtapaOptimizacion aplicar(CodigoFuente f) {
                return new EtapaOptimizacion(nombre(), descripcion(), f.getTexto(), "class {{{ ;;",
                        "class {{{ ;;",
                        List.of(new Hallazgo(1, Hallazgo.Tipo.VARIABLE_SIN_USO, "x", "falso")),
                        List.of(new RangoTexto(0, 1)), 1L);
            }
        };
        String codigo = enClase("int a = 1;\nConsole.WriteLine(a);");
        ResultadoOptimizacionFuente res = new OptimizadorFuente().agregarPase(corrupto).optimizar(codigo);
        assertEquals(1, res.advertencias().size());
        assertEquals(codigo, res.codigoFinal());
        assertEquals(0, res.totalEliminaciones());
    }

    @Test
    @DisplayName("El pipeline por defecto no genera advertencias con código válido")
    void sinAdvertenciasNormales() {
        ResultadoOptimizacionFuente res = OptimizadorFuente.crearPorDefecto()
                .optimizar(enClase("int a = 1;\nint b = 2;\nConsole.WriteLine(a);"));
        assertTrue(res.advertencias().isEmpty(), res.advertencias().toString());
        assertNotNull(res.astLimpio());
    }

    // ---------- Rendimiento ----------

    @Test
    @DisplayName("Programa grande (2000 variables muertas) se optimiza en tiempo razonable")
    void rendimientoProgramaGrande() {
        StringBuilder sb = new StringBuilder();
        sb.append("int vivo = 1;\n");
        for (int i = 0; i < 2000; i++) {
            sb.append("int muerta").append(i).append(" = ").append(i).append(";\n");
        }
        sb.append("Console.WriteLine(vivo);");
        String codigo = enClase(sb.toString());

        ResultadoOptimizacionFuente[] res = new ResultadoOptimizacionFuente[1];
        assertTimeoutPreemptively(Duration.ofSeconds(30),
                () -> res[0] = OptimizadorFuente.crearPorDefecto().optimizar(codigo));
        assertEquals(2000, res[0].totalEliminaciones());
        assertTrue(res[0].codigoFinal().contains("int vivo = 1"));
        assertFalse(res[0].codigoFinal().contains("muerta"));
    }

    // ---------- Infraestructura ----------

    @Test
    @DisplayName("RangosMarcados fusiona rangos y responde contiene() correctamente")
    void rangosMarcados() {
        RangosMarcados r = new RangosMarcados();
        r.agregar(new RangoTexto(10, 20));
        r.agregar(new RangoTexto(30, 40));
        r.agregar(new RangoTexto(15, 35)); // une los dos
        r.agregar(new RangoTexto(12, 14)); // contenido
        assertFalse(r.contiene(9));
        assertTrue(r.contiene(10));
        assertTrue(r.contiene(25));
        assertTrue(r.contiene(39));
        assertFalse(r.contiene(40));
        assertEquals(4, r.comoLista().size());
        assertEquals(1, CodigoFuente.normalizarRangos(r.comoLista()).size());
    }

    @Test
    @DisplayName("RecorridoAST desciende por clases, métodos y estructuras de control por defecto")
    void recorridoPorDefecto() throws Exception {
        String codigo = """
                class C
                {
                    static void Main()
                    {
                        if (true) { if (false) { Console.WriteLine(1); } }
                        while (true) { if (true) { Console.WriteLine(2); } }
                    }
                }
                """;
        NodoPrograma ast = new Analizador(
                new ByteArrayInputStream(codigo.getBytes(StandardCharsets.UTF_8))).programa();
        AtomicInteger ifs = new AtomicInteger();
        ast.accept(new RecorridoAST() {
            @Override
            public Void visitar(NodoIf nodo) {
                ifs.incrementAndGet();
                return super.visitar(nodo);
            }
        });
        assertEquals(3, ifs.get());
    }
}
