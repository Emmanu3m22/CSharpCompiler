package com.compilador.intermedio;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class EvaluadorTest {

    @Test
    @DisplayName("Aritmética entera con división truncada (semántica C#)")
    void testDivisionEntera() throws Exception {
        Valor a = Valor.entero(7);
        Valor b = Valor.entero(2);
        Valor res = a.operar("/", b);

        assertTrue(res.esEntero());
        assertEquals("3", res.toString());
        assertEquals(3L, res.comoEntero());
    }

    @Test
    @DisplayName("Aritmética decimal y promoción implícita")
    void testDivisionDecimal() throws Exception {
        Valor a = Valor.decimal(7.0);
        Valor b = Valor.entero(2);
        Valor res = a.operar("/", b);

        assertFalse(res.esEntero());
        assertEquals("3.5", res.toString());
    }

    @Test
    @DisplayName("Operador módulo con números negativos (semántica C#)")
    void testModuloNegativo() throws Exception {
        Valor a = Valor.entero(-7);
        Valor b = Valor.entero(3);
        Valor res = a.operar("%", b);

        assertEquals("-1", res.toString());
    }

    @Test
    @DisplayName("División entre cero produce ErrorEvaluacion sin excepciones inesperadas")
    void testDivisionEntreCero() {
        Valor a = Valor.entero(10);
        Valor cero = Valor.entero(0);

        ErrorEvaluacion ex = assertThrows(ErrorEvaluacion.class, () -> a.operar("/", cero));
        assertTrue(ex.getMessage().contains("división entre cero"));
    }

    @Test
    @DisplayName("Evaluador de cuádruplos resuelve variables y temporales")
    void testEvaluadorCuadruplosConVariables() throws Exception {
        EntornoValores entorno = new EntornoValores();
        entorno.declarar("x", "int");
        entorno.asignar("x", Valor.entero(10));
        entorno.declarar("y", "int");
        entorno.asignar("y", Valor.entero(5));

        // t1 = x + y (15)
        // t2 = t1 * 2 (30)
        // r = t2
        List<Cuadruplo> cuadruplos = List.of(
                Cuadruplo.binario("+", Operando.variable("x"), Operando.variable("y"), Operando.temporal(1)),
                Cuadruplo.binario("*", Operando.temporal(1), Operando.constante("2"), Operando.temporal(2)),
                Cuadruplo.asignacion(Operando.temporal(2), Operando.variable("r"))
        );

        Valor res = EvaluadorCuadruplos.ejecutar(cuadruplos, entorno);
        assertEquals("30", res.toString());
    }

    @Test
    @DisplayName("Evaluador posfijo con pila produce resultado y traza paso a paso")
    void testEvaluadorPosfijoConTraza() throws Exception {
        EntornoValores entorno = new EntornoValores();
        entorno.declarar("a", "int");
        entorno.asignar("a", Valor.entero(5));
        entorno.declarar("b", "int");
        entorno.asignar("b", Valor.entero(3));

        // a b + 2 *  =>  (5 + 3) * 2 = 16
        List<ElementoPolaco> posfijo = List.of(
                new ElementoPolaco("a", ElementoPolaco.Clase.VARIABLE),
                new ElementoPolaco("b", ElementoPolaco.Clase.VARIABLE),
                new ElementoPolaco("+", ElementoPolaco.Clase.OPERADOR_BINARIO),
                new ElementoPolaco("2", ElementoPolaco.Clase.CONSTANTE),
                new ElementoPolaco("*", ElementoPolaco.Clase.OPERADOR_BINARIO)
        );

        EvaluadorPosfijo.Resultado r = EvaluadorPosfijo.evaluar(posfijo, entorno);
        assertEquals("16", r.valor().toString());
        assertFalse(r.traza().isEmpty());
        assertTrue(r.traza().get(r.traza().size() - 1).contains("16"));
    }

    @Test
    @DisplayName("Variable no inicializada produce error controlado")
    void testVariableSinValor() {
        EntornoValores entorno = new EntornoValores();
        List<Cuadruplo> cuadruplos = List.of(
                Cuadruplo.binario("+", Operando.variable("noDeclarada"), Operando.constante("1"), Operando.temporal(1))
        );

        ErrorEvaluacion ex = assertThrows(ErrorEvaluacion.class,
                () -> EvaluadorCuadruplos.ejecutar(cuadruplos, entorno));
        assertTrue(ex.getMessage().contains("sin valor conocido"));
    }
}
