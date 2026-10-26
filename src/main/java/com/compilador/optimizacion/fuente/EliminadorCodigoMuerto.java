package com.compilador.optimizacion.fuente;

import java.util.List;

/**
 * Pase de optimización para la eliminación de código muerto.
 *
 * Identifica y descarta:
 * <ul>
 *   <li>R1: Sentencias inalcanzables tras return, break, continue o throw.</li>
 *   <li>R2/R3: Ramas muertas por condiciones constantes (ej. if (false), while (false)).</li>
 *   <li>R4: Variables declaradas o asignadas que nunca se leen (análisis de uso de variables).</li>
 * </ul>
 */
public class EliminadorCodigoMuerto implements PaseOptimizacionFuente {

    @Override
    public String nombre() {
        return "Eliminación de Código Muerto";
    }

    @Override
    public String descripcion() {
        return "Detecta y elimina código inalcanzable, condiciones constantes y variables sin uso posterior.";
    }

    @Override
    public EtapaOptimizacion aplicar(CodigoFuente fuente) {
        if (fuente == null || fuente.getTexto().isEmpty()) {
            return EtapaOptimizacion.sinCambios(nombre(), descripcion(), "", 0L);
        }
        long t0 = System.nanoTime();
        // El motor de reglas completo se integrará en los commits siguientes
        return EtapaOptimizacion.sinCambios(nombre(), descripcion(), fuente.getTexto(), System.nanoTime() - t0);
    }
}
