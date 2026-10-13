package com.compilador.intermedio;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Objects;

/**
 * Valor numérico con semántica aritmética similar a C#:
 * <ul>
 *   <li>entero op entero → entero (la división es entera: 7 / 2 = 3)</li>
 *   <li>si algún operando es decimal → decimal (7.0 / 2 = 3.5)</li>
 *   <li>{@code %} conserva el signo del dividendo (-7 % 3 = -1)</li>
 * </ul>
 */
public final class Valor {

    private final boolean entero;
    private final long valorEntero;
    private final double valorDecimal;

    private Valor(boolean entero, long valorEntero, double valorDecimal) {
        this.entero = entero;
        this.valorEntero = valorEntero;
        this.valorDecimal = valorDecimal;
    }

    public static Valor entero(long v) {
        return new Valor(true, v, v);
    }

    public static Valor decimal(double v) {
        return new Valor(false, (long) v, v);
    }

    /**
     * Convierte un literal del código fuente (ej: "42", "-3.14", "10L", "2.5f") en un valor.
     */
    public static Valor desdeLiteral(String literal) throws ErrorEvaluacion {
        String limpio = literal.replaceAll("[LlUuFfMmDd]+$", "");
        try {
            if (limpio.contains(".")) {
                return decimal(Double.parseDouble(limpio));
            }
            return entero(Long.parseLong(limpio));
        } catch (NumberFormatException e) {
            throw new ErrorEvaluacion("literal numérico inválido '" + literal + "'");
        }
    }

    public boolean esEntero() {
        return entero;
    }

    public double comoDecimal() {
        return entero ? valorEntero : valorDecimal;
    }

    public long comoEntero() {
        return entero ? valorEntero : (long) valorDecimal;
    }

    public Valor aDecimal() {
        return entero ? decimal(valorEntero) : this;
    }

    /** Conversión explícita a entero (trunca hacia cero, como un cast de C#). */
    public Valor aEntero() {
        return entero ? this : entero((long) valorDecimal);
    }

    public Valor negar() {
        return entero ? entero(-valorEntero) : decimal(-valorDecimal);
    }

    /**
     * Aplica un operador aritmético binario.
     */
    public Valor operar(String operador, Valor otro) throws ErrorEvaluacion {
        if (entero && otro.entero) {
            long a = valorEntero;
            long b = otro.valorEntero;
            return switch (operador) {
                case "+" -> entero(a + b);
                case "-" -> entero(a - b);
                case "*" -> entero(a * b);
                case "/" -> {
                    if (b == 0) throw new ErrorEvaluacion("división entre cero");
                    yield entero(a / b);
                }
                case "%" -> {
                    if (b == 0) throw new ErrorEvaluacion("módulo entre cero");
                    yield entero(a % b);
                }
                default -> throw new ErrorEvaluacion("operador desconocido '" + operador + "'");
            };
        }
        double a = comoDecimal();
        double b = otro.comoDecimal();
        return switch (operador) {
            case "+" -> decimal(a + b);
            case "-" -> decimal(a - b);
            case "*" -> decimal(a * b);
            case "/" -> {
                if (b == 0) throw new ErrorEvaluacion("división entre cero");
                yield decimal(a / b);
            }
            case "%" -> {
                if (b == 0) throw new ErrorEvaluacion("módulo entre cero");
                yield decimal(a % b);
            }
            default -> throw new ErrorEvaluacion("operador desconocido '" + operador + "'");
        };
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Valor v)) return false;
        if (entero != v.entero) return false;
        return entero ? valorEntero == v.valorEntero
                : Double.compare(valorDecimal, v.valorDecimal) == 0;
    }

    @Override
    public int hashCode() {
        return entero ? Objects.hash(true, valorEntero) : Objects.hash(false, valorDecimal);
    }

    /**
     * Enteros sin punto ("27"); decimales siempre con punto ("3.75", "3.0") y
     * redondeados a 12 cifras significativas para evitar ruido de punto flotante.
     */
    @Override
    public String toString() {
        if (entero) {
            return Long.toString(valorEntero);
        }
        if (Double.isNaN(valorDecimal) || Double.isInfinite(valorDecimal)) {
            return Double.toString(valorDecimal);
        }
        String texto = new BigDecimal(valorDecimal).round(new MathContext(12))
                .stripTrailingZeros().toPlainString();
        return texto.contains(".") ? texto : texto + ".0";
    }
}
