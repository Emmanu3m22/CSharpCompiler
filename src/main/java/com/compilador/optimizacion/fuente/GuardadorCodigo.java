package com.compilador.optimizacion.fuente;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

/**
 * Guarda el código optimizado en disco (UTF-8, sin BOM). Sin dependencias de Swing.
 */
public final class GuardadorCodigo {

    private GuardadorCodigo() {}

    /** Nombre sugerido a partir del archivo original: "ventas.cs" → "ventas_optimizado.cs". */
    public static String sugerirNombre(String nombreOriginal) {
        if (nombreOriginal == null || nombreOriginal.isBlank()) {
            return "codigo_optimizado.cs";
        }
        String base = nombreOriginal.trim();
        int punto = base.lastIndexOf('.');
        if (punto > 0) {
            base = base.substring(0, punto);
        }
        return base + "_optimizado.cs";
    }

    /** Agrega la extensión .cs si el destino no la trae. */
    public static Path conExtension(Path destino) {
        String nombre = destino.getFileName().toString();
        if (nombre.toLowerCase().endsWith(".cs")) {
            return destino;
        }
        return destino.resolveSibling(nombre + ".cs");
    }

    /**
     * @param sobrescribir si es false y el archivo existe, lanza {@link FileAlreadyExistsException}
     * @return la ruta real donde se guardó
     */
    public static Path guardar(Path destino, String codigo, boolean sobrescribir) throws IOException {
        if (destino == null || destino.getFileName() == null) {
            throw new IllegalArgumentException("Debes indicar un archivo de destino.");
        }
        if (codigo == null || codigo.isBlank()) {
            throw new IllegalArgumentException("No hay código optimizado para guardar.");
        }
        Path real = conExtension(destino);
        if (!sobrescribir && Files.exists(real)) {
            throw new FileAlreadyExistsException(real.toString());
        }
        Files.writeString(real, codigo, StandardCharsets.UTF_8,
                StandardOpenOption.CREATE, StandardOpenOption.TRUNCATE_EXISTING, StandardOpenOption.WRITE);
        return real;
    }
}
