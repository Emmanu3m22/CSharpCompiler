package com.compilador;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.compilador.ast.ASTPrinter;
import com.compilador.ast.NodoPrograma;

class ASTPrinterTest {

    @Test
    void muestraLaPrecedenciaComoJerarquia() throws Exception {
        String arbol = imprimir("x = 2 + 3 * 4;");

        assertTrue(arbol.indexOf("Operacion: +") < arbol.indexOf("Operacion: *"));
        assertTrue(arbol.indexOf("Operacion: *") < arbol.indexOf("Numero: 3"));
        assertTrue(arbol.contains("├── Operacion: *") || arbol.contains("└── Operacion: *"));
    }

    @Test
    void conservaLaAgrupacionExplicita() throws Exception {
        String arbol = imprimir("x = (2 + 3) * 4;");

        assertTrue(arbol.contains("Operacion: *"));
        assertTrue(arbol.contains("Agrupacion"));
        assertTrue(arbol.indexOf("Agrupacion") < arbol.indexOf("Operacion: +"));
    }

    @Test
    void muestraCamposYRelacionesDeSentencias() throws Exception {
        String arbol = imprimir("int x = 5; x = 10;");

        assertTrue(arbol.startsWith("Programa"));
        assertTrue(arbol.contains("Declaracion"));
        assertTrue(arbol.contains("Tipo: int"));
        assertTrue(arbol.contains("Identificador: x"));
        assertTrue(arbol.contains("Inicializacion"));
        assertTrue(arbol.contains("Asignacion"));
        assertTrue(arbol.contains("Expresion"));
        assertTrue(arbol.contains("Numero: 10"));
    }

    @Test
    void muestraEstructurasDeControlYListas() throws Exception {
        String arbol = imprimir("if (x > 0) { Console.WriteLine(x); } else { x++; }");

        assertTrue(arbol.contains("If"));
        assertTrue(arbol.contains("Condicion"));
        assertTrue(arbol.contains("Then"));
        assertTrue(arbol.contains("Else"));
        assertTrue(arbol.contains("LlamadaMetodo: Console.WriteLine"));
        assertTrue(arbol.contains("Incremento: ++"));
    }

    private String imprimir(String codigo) throws Exception {
        Analizador parser = new Analizador(new StringReader(codigo));
        NodoPrograma programa = parser.programa();
        return new ASTPrinter().imprimir(programa);
    }
}