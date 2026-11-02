package com.compilador.ast;

import java.util.List;

/**
 * Visitor base que recorre la estructura de <b>sentencias</b> del AST
 * (programa, bloques, casos, estructuras y cuerpos de if/while/do/for/switch).
 *
 * Todos los métodos descienden por defecto, de modo que una regla de optimización
 * solo necesita sobrescribir los nodos que le interesan (y llamar a
 * {@code super.visitar(nodo)} para seguir descendiendo). Las expresiones y los nodos
 * hoja no se recorren: devuelven {@code null}.
 */
public abstract class RecorridoAST implements NodoVisitor<Void> {

    /** Visita cada nodo no nulo de la lista. */
    protected void visitarTodos(List<? extends Nodo> nodos) {
        if (nodos == null) return;
        for (Nodo n : nodos) {
            if (n != null) n.accept(this);
        }
    }

    protected void visitarSiExiste(Nodo nodo) {
        if (nodo != null) nodo.accept(this);
    }

    @Override public Void visitar(NodoPrograma nodo) { visitarTodos(nodo.getSentencias()); return null; }
    @Override public Void visitar(NodoBloque nodo) { visitarTodos(nodo.getSentencias()); return null; }
    @Override public Void visitar(NodoCaso nodo) { visitarTodos(nodo.getSentencias()); return null; }
    @Override public Void visitar(NodoEstructura nodo) { visitarSiExiste(nodo.getCuerpo()); return null; }

    @Override
    public Void visitar(NodoIf nodo) {
        visitarSiExiste(nodo.getBloqueThen());
        visitarSiExiste(nodo.getBloqueElse());
        return null;
    }

    @Override public Void visitar(NodoWhile nodo) { visitarSiExiste(nodo.getCuerpo()); return null; }
    @Override public Void visitar(NodoDoWhile nodo) { visitarSiExiste(nodo.getCuerpo()); return null; }
    @Override public Void visitar(NodoFor nodo) { visitarSiExiste(nodo.getCuerpo()); return null; }
    @Override public Void visitar(NodoSwitch nodo) { visitarTodos(nodo.getCasos()); return null; }

    // Sentencias simples, expresiones y hojas: no hay nada que recorrer
    @Override public Void visitar(NodoAsignacion nodo) { return null; }
    @Override public Void visitar(NodoAccesoArreglo nodo) { return null; }
    @Override public Void visitar(NodoCasteo nodo) { return null; }
    @Override public Void visitar(NodoDeclaracion nodo) { return null; }
    @Override public Void visitar(NodoComando nodo) { return null; }
    @Override public Void visitar(NodoCadena nodo) { return null; }
    @Override public Void visitar(NodoArreglo nodo) { return null; }
    @Override public Void visitar(NodoBooleano nodo) { return null; }
    @Override public Void visitar(NodoNumero nodo) { return null; }
    @Override public Void visitar(NodoIdentificador nodo) { return null; }
    @Override public Void visitar(NodoInstanciacion nodo) { return null; }
    @Override public Void visitar(NodoTernario nodo) { return null; }
    @Override public Void visitar(NodoAgrupacion nodo) { return null; }
    @Override public Void visitar(NodoNegacionLogica nodo) { return null; }
    @Override public Void visitar(NodoLlamadaMetodo nodo) { return null; }
    @Override public Void visitar(NodoOperacionLogica nodo) { return null; }
    @Override public Void visitar(NodoOperacion nodo) { return null; }
    @Override public Void visitar(NodoOperacionRelacional nodo) { return null; }
    @Override public Void visitar(NodoIncremento nodo) { return null; }
    @Override public Void visitar(NodoMenosUnario nodo) { return null; }
}
