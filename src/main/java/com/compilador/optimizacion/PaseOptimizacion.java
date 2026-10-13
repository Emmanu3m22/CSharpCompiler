package com.compilador.optimizacion;

import com.compilador.intermedio.Cuadruplo;
import java.util.List;

/**
 * Contrato base para un pase de optimización sobre el código intermedio.
 *
 * Cada pase toma una lista de cuádruplos y produce una nueva lista de
 * cuádruplos optimizados sin mutar los originales.
 */
public interface PaseOptimizacion {

    /** Nombre descriptivo del pase (ej: "Plegado de Constantes", "Propagación de Copias"). */
    String nombre();

    /**
     * Aplica la optimización sobre una lista de cuádruplos.
     *
     * @param entrada cuádruplos originales
     * @return cuádruplos resultantes tras aplicar la transformación
     */
    List<Cuadruplo> optimizar(List<Cuadruplo> entrada);
}
