package com.compilador.optimizacion.fuente;

/**
 * Contrato base para un pase de optimización a nivel de código fuente.
 *
 * Cada pase toma el {@link CodigoFuente} actual y produce una {@link EtapaOptimizacion}
 * con el código transformado, los hallazgos y el texto enmascarado para la siguiente etapa.
 *
 * Para agregar una nueva optimización (ej. operaciones aritméticas, redundancias, ciclos):
 * <ol>
 *   <li>Implementar esta interfaz.</li>
 *   <li>Registrarla en {@link OptimizadorFuente#agregarPase(PaseOptimizacionFuente)}.</li>
 * </ol>
 */
public interface PaseOptimizacionFuente {

    /**
     * Nombre corto identificador del pase (ej. "Eliminación de Código Muerto").
     */
    String nombre();

    /**
     * Descripción general del objetivo de este pase.
     */
    String descripcion();

    /**
     * Ejecuta el pase de optimización sobre el código fuente dado.
     *
     * @param fuente representación del código fuente actual con sus tokens
     * @return resultado detallado de la etapa
     */
    EtapaOptimizacion aplicar(CodigoFuente fuente);
}
