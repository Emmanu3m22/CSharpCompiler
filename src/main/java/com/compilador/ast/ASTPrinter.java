package com.compilador.ast;

import java.util.ArrayList;
import java.util.List;

/**
 * Visitor que representa el AST como un arbol jerarquico legible.
 * La salida se construye a partir de los campos de cada nodo, no de toString().
 */
public class ASTPrinter implements NodoVisitor<Void> {

    private final StringBuilder salida = new StringBuilder();
    private final List<Boolean> ramas = new ArrayList<>();

    public String imprimir(Nodo nodo) {
        salida.setLength(0);
        ramas.clear();
        if (nodo == null) {
            return "(AST vacio)";
        }
        nodo.accept(this);
        return salida.toString();
    }

    @Override
    public Void visitar(NodoPrograma nodo) {
        linea("Programa");
        hijos(lista(nodo.getSentencias()));
        return null;
    }

    @Override
    public Void visitar(NodoDeclaracion nodo) {
        linea("Declaracion");
        hijos(valor("Tipo: " + nodo.getTipoDato()),
                valor("Identificador: " + nodo.getIdentificador()),
                nombrado("Inicializacion", nodo.getInicializacion()));
        return null;
    }

    @Override
    public Void visitar(NodoAsignacion nodo) {
        linea("Asignacion");
        hijos(nodo("Destino", nodo.getDestino()), nodo("Expresion", nodo.getExpresion()));
        return null;
    }

    @Override
    public Void visitar(NodoComando nodo) {
        linea("Comando: " + nodo.getComando());
        hijos(nombrado("Argumento", nodo.getArgumento()));
        return null;
    }

    @Override
    public Void visitar(NodoBloque nodo) {
        linea("Bloque");
        hijos(lista(nodo.getSentencias()));
        return null;
    }

    @Override
    public Void visitar(NodoAccesoArreglo nodo) {
        linea("AccesoArreglo");
        hijos(nodo("Arreglo", nodo.getArreglo()), nodo("Indice", nodo.getIndice()));
        return null;
    }

    @Override
    public Void visitar(NodoCasteo nodo) {
        linea("Casteo: " + nodo.getTipo());
        hijos(nodo("Expresion", nodo.getExpresion()));
        return null;
    }

    @Override
    public Void visitar(NodoCadena nodo) {
        linea("Cadena: " + nodo.getValor());
        return null;
    }

    @Override
    public Void visitar(NodoArreglo nodo) {
        linea("Arreglo");
        hijos(valor("Tipo: " + nodo.getTipoElemento()), valor("Tamanio: " + nodo.getTamaño()));
        return null;
    }

    @Override
    public Void visitar(NodoBooleano nodo) {
        linea("Booleano: " + nodo.getValor());
        return null;
    }

    @Override
    public Void visitar(NodoNumero nodo) {
        linea("Numero: " + nodo.getValor());
        return null;
    }

    @Override
    public Void visitar(NodoIdentificador nodo) {
        linea("Identificador: " + nodo.getNombre());
        return null;
    }

    @Override
    public Void visitar(NodoInstanciacion nodo) {
        linea("Instanciacion: " + nodo.getTipo());
        if (nodo.isEsArreglo()) {
            hijos(nodo("IndiceArreglo", nodo.getIndiceArreglo()));
        } else {
            hijos(nombrados("Argumentos", nodo.getArgumentos()));
        }
        return null;
    }

    @Override
    public Void visitar(NodoSwitch nodo) {
        linea("Switch");
        hijos(nodo("Expresion", nodo.getExpresion()), lista(nodo.getCasos()));
        return null;
    }

    @Override
    public Void visitar(NodoEstructura nodo) {
        linea("Estructura: " + nodo.getTipoEstructura());
        hijos(valor("Identificador: " + nodo.getIdentificador()), nodo("Cuerpo", nodo.getCuerpo()));
        return null;
    }

    @Override
    public Void visitar(NodoCaso nodo) {
        linea(nodo.isEsDefault() ? "Default" : "Case");
        if (!nodo.isEsDefault()) {
            hijos(nodo("Valor", nodo.getValor()), lista(nodo.getSentencias()));
        } else {
            hijos(lista(nodo.getSentencias()));
        }
        return null;
    }

    @Override
    public Void visitar(NodoDoWhile nodo) {
        linea("DoWhile");
        hijos(nodo("Cuerpo", nodo.getCuerpo()), nodo("Condicion", nodo.getCondicion()));
        return null;
    }

    @Override
    public Void visitar(NodoFor nodo) {
        linea("For");
        hijos(nodo("Inicializacion", nodo.getInicializacion()), nodo("Condicion", nodo.getCondicion()),
                nodo("Actualizacion", nodo.getActualizacion()), nodo("Cuerpo", nodo.getCuerpo()));
        return null;
    }

    @Override
    public Void visitar(NodoIf nodo) {
        linea("If");
        hijos(nodo("Condicion", nodo.getCondicion()), nodo("Then", nodo.getBloqueThen()),
                nodo("Else", nodo.getBloqueElse()));
        return null;
    }

    @Override
    public Void visitar(NodoTernario nodo) {
        linea("Ternario");
        hijos(nodo("Condicion", nodo.getCondicion()), nodo("Verdadero", nodo.getVerdadero()),
                nodo("Falso", nodo.getFalso()));
        return null;
    }

    @Override
    public Void visitar(NodoWhile nodo) {
        linea("While");
        hijos(nodo("Condicion", nodo.getCondicion()), nodo("Cuerpo", nodo.getCuerpo()));
        return null;
    }

    @Override
    public Void visitar(NodoAgrupacion nodo) {
        linea("Agrupacion");
        hijos(nodo(nodo.getExpresion()));
        return null;
    }

    @Override
    public Void visitar(NodoNegacionLogica nodo) {
        linea("NegacionLogica");
        hijos(nodo("Expresion", nodo.getExpresion()));
        return null;
    }

    @Override
    public Void visitar(NodoLlamadaMetodo nodo) {
        linea("LlamadaMetodo: " + nodo.getIdentificador());
        hijos(nombrados("Argumentos", nodo.getArgumentos()));
        return null;
    }

    @Override
    public Void visitar(NodoOperacionLogica nodo) {
        return visitarOperacion("OperacionLogica: " + nodo.getOperador(), nodo.getIzquierdo(), nodo.getDerecho());
    }

    @Override
    public Void visitar(NodoOperacion nodo) {
        return visitarOperacion("Operacion: " + nodo.getOperador(), nodo.getIzquierdo(), nodo.getDerecho());
    }

    @Override
    public Void visitar(NodoOperacionRelacional nodo) {
        return visitarOperacion("OperacionRelacional: " + nodo.getOperador(), nodo.getIzquierdo(), nodo.getDerecho());
    }

    @Override
    public Void visitar(NodoIncremento nodo) {
        linea("Incremento: " + nodo.getOperador());
        hijos(valor("Identificador: " + nodo.getIdentificador()));
        return null;
    }

    private Void visitarOperacion(String etiqueta, Nodo izquierdo, Nodo derecho) {
        linea(etiqueta);
        hijos(nodo("Izquierdo", izquierdo), nodo("Derecho", derecho));
        return null;
    }

    private void hijos(Hijo... hijos) {
        for (int i = 0; i < hijos.length; i++) {
            ramas.add(i == hijos.length - 1);
            hijos[i].dibujar();
            ramas.remove(ramas.size() - 1);
        }
    }

    private Hijo valor(String texto) {
        return () -> linea(texto);
    }

    private Hijo nodo(Nodo nodo) {
        return () -> {
            if (nodo == null) {
                linea("(null)");
            } else {
                nodo.accept(this);
            }
        };
    }

    private Hijo nodo(String etiqueta, Nodo nodo) {
        return () -> {
            linea(etiqueta);
            if (nodo != null) {
                hijos(nodo(nodo));
            } else {
                hijos(valor("(null)"));
            }
        };
    }

    private Hijo nombrado(String etiqueta, Nodo nodo) {
        return nodo(etiqueta, nodo);
    }

    private Hijo lista(List<? extends Nodo> nodos) {
        return () -> {
            if (nodos == null || nodos.isEmpty()) {
                linea("(vacio)");
                return;
            }
            List<Hijo> hijos = new ArrayList<>();
            for (Nodo nodo : nodos) {
                hijos.add(nodo(nodo));
            }
            hijos(hijos.toArray(new Hijo[0]));
        };
    }

    private Hijo nombrados(String etiqueta, List<? extends Nodo> nodos) {
        return () -> {
            linea(etiqueta);
            hijos(lista(nodos));
        };
    }

    private void linea(String texto) {
        if (!ramas.isEmpty()) {
            for (int i = 0; i < ramas.size() - 1; i++) {
                salida.append(ramas.get(i) ? "    " : "│   ");
            }
            salida.append(ramas.get(ramas.size() - 1) ? "└── " : "├── ");
        }
        salida.append(texto).append(System.lineSeparator());
    }

    @FunctionalInterface
    private interface Hijo {
        void dibujar();
    }
}