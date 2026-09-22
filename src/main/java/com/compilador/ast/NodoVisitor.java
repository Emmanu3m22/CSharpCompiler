package com.compilador.ast;

public interface NodoVisitor<T> {

    T visitar(NodoPrograma nodo);

    // Acciones
    T visitar(NodoAsignacion nodo);
    T visitar(NodoAccesoArreglo nodo);
    T visitar(NodoCasteo nodo);
    T visitar(NodoDeclaracion nodo);
    T visitar(NodoComando nodo);
    T visitar(NodoBloque nodo);

    // Tipos de datos
    T visitar(NodoCadena nodo);
    T visitar(NodoArreglo nodo);
    T visitar(NodoBooleano nodo);
    T visitar(NodoNumero nodo);
    T visitar(NodoIdentificador nodo);
    T visitar(NodoInstanciacion nodo);
    
    // Estructuras de control
    T visitar(NodoSwitch nodo);
    T visitar(NodoEstructura nodo);
    T visitar(NodoCaso nodo);
    T visitar(NodoDoWhile nodo);
    T visitar(NodoFor nodo);
    T visitar(NodoIf nodo);
    T visitar(NodoTernario nodo);
    T visitar(NodoWhile nodo);

    // Operaciones
    T visitar(NodoAgrupacion nodo);
    T visitar(NodoNegacionLogica nodo);
    T visitar(NodoLlamadaMetodo nodo);
    T visitar(NodoOperacionLogica nodo);
    T visitar(NodoOperacion nodo);
    T visitar(NodoOperacionRelacional nodo);
    T visitar(NodoIncremento nodo);
}
