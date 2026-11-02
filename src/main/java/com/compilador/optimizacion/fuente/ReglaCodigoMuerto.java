package com.compilador.optimizacion.fuente;

import com.compilador.ast.NodoPrograma;

import java.util.List;

/**
 * Regla individual de eliminación de código muerto. {@link EliminadorCodigoMuerto}
 * las ejecuta en orden sobre el mismo AST; cada una agrega los rangos de texto
 * que se pueden eliminar y los hallazgos que explican por qué.
 *
 * Para agregar una regla nueva basta implementar esta interfaz (idealmente extendiendo
 * {@link com.compilador.ast.RecorridoAST}) y registrarla en el constructor de
 * {@link EliminadorCodigoMuerto}.
 */
public interface ReglaCodigoMuerto {

    /** Identificador corto de la regla (ej. "R1"). */
    String id();

    void aplicar(NodoPrograma ast, CodigoFuente fuente, List<Hallazgo> hallazgos, RangosMarcados rangos);
}
