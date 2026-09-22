package com.compilador.semantic;

import com.compilador.ast.ParametroMetodo;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Firma de un método definido por el usuario.
 */
public class SimboloMetodo {

    private final String nombre;
    private final List<ParametroMetodo> parametros;
    private final int lineaDeclaracion;
    private final int columnaDeclaracion;

    public SimboloMetodo(String nombre, List<ParametroMetodo> parametros,
                         int lineaDeclaracion, int columnaDeclaracion) {
        this.nombre = nombre;
        this.parametros = Collections.unmodifiableList(new ArrayList<>(parametros));
        this.lineaDeclaracion = lineaDeclaracion;
        this.columnaDeclaracion = columnaDeclaracion;
    }

    public String getNombre() {
        return nombre;
    }

    public List<ParametroMetodo> getParametros() {
        return parametros;
    }

    public int getLineaDeclaracion() {
        return lineaDeclaracion;
    }

    public int getColumnaDeclaracion() {
        return columnaDeclaracion;
    }

    public int getCantidadMinimaArgumentos() {
        int obligatorios = 0;
        for (ParametroMetodo parametro : parametros) {
            if (!parametro.esOpcional()) {
                obligatorios++;
            }
        }
        return obligatorios;
    }

    public int getCantidadMaximaArgumentos() {
        return parametros.size();
    }
}