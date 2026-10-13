package com.compilador.intermedio;

/**
 * Error controlado al evaluar una expresión (división entre cero, variable sin
 * valor, operando no numérico...). Nunca escapa hacia la GUI: se convierte en
 * el texto "No evaluable: ..." de la fila correspondiente.
 */
public class ErrorEvaluacion extends Exception {

    public ErrorEvaluacion(String mensaje) {
        super(mensaje);
    }
}
