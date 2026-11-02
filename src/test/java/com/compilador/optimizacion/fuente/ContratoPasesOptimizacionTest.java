package com.compilador.optimizacion.fuente;

import com.compilador.Analizador;
import com.compilador.ast.NodoPrograma;
import com.compilador.gui.Ejemplos;
import com.compilador.semantic.AnalizadorSemantico;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Tests de contrato: TODO pase registrado en {@link OptimizadorFuente#crearPorDefecto()}
 * debe cumplirlos con cualquier programa. Al agregar un pase nuevo basta registrarlo
 * para que quede cubierto automáticamente.
 *
 * Contrato:
 * <ol>
 *   <li>No lanza excepciones.</li>
 *   <li>No introduce errores léxicos/sintácticos nuevos.</li>
 *   <li>No introduce errores semánticos nuevos.</li>
 *   <li>Es idempotente: aplicarlo sobre su propio resultado no cambia nada.</li>
 * </ol>
 */
public class ContratoPasesOptimizacionTest {

    private static final String PROGRAMA_CLASE = """
            class SistemaVentas
            {
                static void Main()
                {
                    int precio = 250;
                    int cantidad = 3;
                    int muerta1 = 100;
                    int muerta2 = muerta1 + 1;
                    int total = precio * cantidad;
                    Console.WriteLine($"Total: {total}");
                }
            }
            """;

    private static final String PROGRAMA_RAMAS = """
            class P
            {
                static int F(int a)
                {
                    int sobra = 5;
                    if (false) { Console.WriteLine(1); }
                    if (true) { a = a + 1; } else { a = a - 1; }
                    while (false) { a = 0; }
                    return a;
                    Console.WriteLine(a);
                }
            }
            """;

    static Stream<Arguments> combinaciones() {
        List<String> nombres = new ArrayList<>();
        List<String> codigos = new ArrayList<>();
        for (int i = 0; i < Ejemplos.cantidad(); i++) {
            nombres.add("ejemplo:" + Ejemplos.getNombre(i));
            codigos.add(Ejemplos.getCodigo(i));
        }
        nombres.add("clase-ventas");
        codigos.add(PROGRAMA_CLASE);
        nombres.add("ramas-muertas");
        codigos.add(PROGRAMA_RAMAS);

        List<Arguments> args = new ArrayList<>();
        List<PaseOptimizacionFuente> pases = OptimizadorFuente.crearPorDefecto().getPases();
        for (PaseOptimizacionFuente pase : pases) {
            for (int i = 0; i < codigos.size(); i++) {
                args.add(Arguments.of(pase.nombre() + " / " + nombres.get(i), pase, codigos.get(i)));
            }
        }
        return args.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("combinaciones")
    void cumpleContrato(String etiqueta, PaseOptimizacionFuente pase, String codigo) {
        EtapaOptimizacion etapa = assertDoesNotThrow(() -> pase.aplicar(new CodigoFuente(codigo)));

        // 2. Sin errores sintácticos nuevos
        int erroresAntes = ValidadorSintaxis.contarErrores(codigo);
        int erroresDespues = ValidadorSintaxis.contarErrores(etapa.codigoEnmascarado());
        assertTrue(erroresDespues <= erroresAntes,
                "El pase introdujo errores de sintaxis (" + erroresAntes + " -> " + erroresDespues + ")");

        // 3. Sin errores semánticos nuevos (solo si ambos parsean limpio)
        if (erroresAntes == 0 && erroresDespues == 0) {
            assertTrue(contarErroresSemanticos(etapa.codigoEnmascarado()) <= contarErroresSemanticos(codigo),
                    "El pase introdujo errores semánticos");
        }

        // 4. Idempotencia
        EtapaOptimizacion segunda = pase.aplicar(new CodigoFuente(etapa.codigoEnmascarado()));
        assertFalse(segunda.hayCambios(), "Una segunda aplicación no debe encontrar más cambios");
    }

    private static int contarErroresSemanticos(String codigo) {
        try {
            Analizador parser = new Analizador(new ByteArrayInputStream(codigo.getBytes(StandardCharsets.UTF_8)));
            NodoPrograma ast = parser.programa();
            AnalizadorSemantico sem = new AnalizadorSemantico();
            sem.analizar(ast);
            return sem.getErrores().size();
        } catch (Throwable e) {
            return Integer.MAX_VALUE;
        }
    }
}
