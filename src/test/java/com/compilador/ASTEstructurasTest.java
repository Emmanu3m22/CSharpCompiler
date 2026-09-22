package com.compilador;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.StringReader;

import org.junit.jupiter.api.Test;

import com.compilador.ast.ASTPrinter;
import com.compilador.ast.Nodo;
import com.compilador.ast.NodoBloque;
import com.compilador.ast.NodoEstructura;
import com.compilador.ast.NodoPrograma;

class ASTEstructurasTest {

    @Test
    void conservaLasSentenciasDentroDeNamespaceClaseYMetodo() throws Exception {
        NodoPrograma programa = parsear("namespace N { class C { void M() { int x = 1; x = 2; } } }");

        NodoEstructura namespace = estructura(programa.getSentencias().get(0), "namespace");
        NodoEstructura clase = estructura(namespace.getCuerpo().getSentencias().get(0), "class");
        NodoEstructura metodo = estructura(clase.getCuerpo().getSentencias().get(0), "method");

        assertEquals(1, namespace.getCuerpo().getSentencias().size());
        assertEquals(1, clase.getCuerpo().getSentencias().size());
        assertEquals(2, metodo.getCuerpo().getSentencias().size());
    }

    @Test
    void imprimeJerarquiaCompletaDeEstructuras() throws Exception {
        NodoPrograma programa = parsear("namespace N { class C { void M() { int x = 1; x = 2; } } }");
        String arbol = new ASTPrinter().imprimir(programa);

        assertTrue(arbol.indexOf("Estructura: namespace") < arbol.indexOf("Estructura: class"));
        assertTrue(arbol.indexOf("Estructura: class") < arbol.indexOf("Estructura: method"));
        assertTrue(arbol.indexOf("Estructura: method") < arbol.indexOf("Declaracion"));
        assertTrue(arbol.indexOf("Declaracion") < arbol.indexOf("Asignacion"));
    }

    private NodoPrograma parsear(String codigo) throws Exception {
        Analizador parser = new Analizador(new StringReader(codigo));
        NodoPrograma programa = parser.programa();
        assertEquals(0, parser.getErroresSintacticos().size(), parser.getErroresSintacticos().toString());
        return programa;
    }

    private NodoEstructura estructura(Nodo nodo, String tipo) {
        NodoEstructura estructura = assertInstanceOf(NodoEstructura.class, nodo);
        assertEquals(tipo, estructura.getTipoEstructura());
        assertInstanceOf(NodoBloque.class, estructura.getCuerpo());
        return estructura;
    }
}