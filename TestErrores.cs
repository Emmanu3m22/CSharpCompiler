using System;

namespace PruebasCompiladorAvanzadas
{
    // Error Sintáctico 1: Modificadores de acceso en orden incorrecto o duplicados (aunque el parser puede perdonarlo, 'class public' en vez de 'public class' o usar un token inesperado)
    // Error Sintáctico 2: Falta el nombre de la clase
    public class 
    {
        // Error Léxico 1: Uso de '#'
        #region Configuracion

        // Error Sintáctico 3: Asignación a un literal
        100 = int maximo;

        // Error Léxico 2: Uso de backtick (`) no válido en C#
        int `contador = 0;

        // Error Sintáctico 4: Falta el tipo de dato en la declaración
        saldoActual = 500.50;

        // Error Léxico 3: Uso de arroba '@' suelta (asumiendo que nuestro lexer no soporta identificadores verbatim como @class)
        string @nombre = "Prueba";

        // Error Sintáctico 5: Falta paréntesis de apertura en los parámetros del método
        public void CalcularValores int a, int b)
        {
            // Error Sintáctico 6: Falta el punto y coma (;) al final
            int resultado = a + b

            // Error Léxico 4: Uso de bitwise NOT '~' (si el lexer no lo soporta)
            int negado = ~resultado;

            // Error Sintáctico 7: Operadores aritméticos duplicados/inválidos
            int calculoMalo = a + * b;

            // Error Sintáctico 8: Falta paréntesis en la condición del if
            if a > b 
            {
                Console.WriteLine("A es mayor");
            }

            // Error Sintáctico 9: Condición del while sin paréntesis de cierre
            while (resultado > 0 
            {
                // Error Sintáctico 10: Palabra clave usada como identificador
                int class = resultado;
                
                // Error Sintáctico 11: Incremento mal formado (separado por espacio o tipo incorrecto)
                resultado = resultado +;
            }

            // Error Sintáctico 12: Declaración de bucle for sin paréntesis inicial
            for int i = 0; i < 10; i++)
            {
                // Error Léxico 5: Interpolación de strings con '$' (si no está implementado)
                Console.WriteLine($"Valor de i: {i}");
            }

            // Error Sintáctico 13: Bloque try sin llaves
            try 
                Console.WriteLine("Intentando...");
            catch (Exception e)
            {
                // Error Léxico 6: Caracter de escape suelto o diagonal invertida suelta '\'
                char escape = \ ;
            }
            finally
            {
                // Error Sintáctico 14: Falta la palabra clave 'return' o se usa mal
                int return;
            }
            
            // Error Sintáctico 15: Llave de cierre extra (unmatched brace)
            }
        }

        // Error Sintáctico 16: Método sin tipo de retorno ni nombre válido
        public () 
        {
            // Error Sintáctico 17: Creación de objeto mal formada (falta 'new' o paréntesis)
            TestErrores obj = TestErrores;
        }

        // Error Léxico 7: Uso de ampersand simple '&' (si solo soportamos '&&')
        bool bandera = true & false;

        // Error Léxico 8: Uso de pipe simple '|' (si solo soportamos '||')
        bool bandera2 = true | false;

        // Error Sintáctico 18: Uso de 'this' fuera de un contexto válido o asignación incorrecta
        this = null;

        // Error Sintáctico 19: Declaración de arreglo mal estructurada
        int[] miArreglo = new int[];

        // Error Sintáctico 20: Switch sin llaves
        switch (a)
            case 1: break;

        // Error Léxico 9: Carácter desconocido '¿'
        string pregunta = "¿Qué tal?";

        // Error Léxico 10: Uso de un operador bit a bit XOR '^'
        int xorVal = a ^ b;
    }
}
