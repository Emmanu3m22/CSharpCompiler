package com.compilador.optimizacion.fuente;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

public class GuardadorCodigoTest {

    @Test
    @DisplayName("Sugiere nombre con sufijo _optimizado.cs")
    void sugerirNombre() {
        assertEquals("ventas_optimizado.cs", GuardadorCodigo.sugerirNombre("ventas.cs"));
        assertEquals("ventas_optimizado.cs", GuardadorCodigo.sugerirNombre("ventas.txt"));
        assertEquals("ventas_optimizado.cs", GuardadorCodigo.sugerirNombre("ventas"));
        assertEquals("codigo_optimizado.cs", GuardadorCodigo.sugerirNombre(null));
        assertEquals("codigo_optimizado.cs", GuardadorCodigo.sugerirNombre("  "));
    }

    @Test
    @DisplayName("Guarda en UTF-8 conservando acentos y agrega .cs")
    void guardaUtf8(@TempDir Path dir) throws IOException {
        String codigo = "class C { /* ñandú */ static void Main() { Console.WriteLine(\"áé\"); } }";
        Path real = GuardadorCodigo.guardar(dir.resolve("salida"), codigo, false);
        assertEquals("salida.cs", real.getFileName().toString());
        assertEquals(codigo, Files.readString(real, StandardCharsets.UTF_8));
        byte[] bytes = Files.readAllBytes(real);
        assertFalse(bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB,
                "No debe llevar BOM");
    }

    @Test
    @DisplayName("No sobrescribe sin permiso y sí con permiso")
    void sobrescritura(@TempDir Path dir) throws IOException {
        Path destino = dir.resolve("a.cs");
        GuardadorCodigo.guardar(destino, "uno", false);
        assertThrows(FileAlreadyExistsException.class, () -> GuardadorCodigo.guardar(destino, "dos", false));
        assertEquals("uno", Files.readString(destino));
        GuardadorCodigo.guardar(destino, "dos", true);
        assertEquals("dos", Files.readString(destino));
    }

    @Test
    @DisplayName("Rechaza código vacío o destino nulo con mensaje claro")
    void entradasInvalidas(@TempDir Path dir) {
        IllegalArgumentException e1 = assertThrows(IllegalArgumentException.class,
                () -> GuardadorCodigo.guardar(dir.resolve("a.cs"), "  ", true));
        assertTrue(e1.getMessage().contains("No hay código"));
        assertThrows(IllegalArgumentException.class, () -> GuardadorCodigo.guardar(null, "x", true));
    }
}
