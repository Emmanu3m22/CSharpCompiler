package com.compilador.gui;

/**
 * Códigos de ejemplo precargados para que el usuario pueda probar
 * el compilador sin tener que escribir código desde cero.
 *
 * Cada ejemplo tiene un nombre descriptivo y el código fuente.
 */
public final class Ejemplos {

    private Ejemplos() {} // No instanciable

    /** Nombres de los ejemplos disponibles */
    public static final String[] NOMBRES = {
        "Ejemplo Final Sin Errores",
        "Ejemplo Final Con Errores",
        "Polaca y Cuádruplos",
        "Hola Mundo",
        "Variables y operaciones",
        "Expresiones complejas",
        "Errores de ejemplo",
        "Programa vacío",
        "Eliminación de Código Muerto"
    };

    /** Código fuente de cada ejemplo (misma posición que NOMBRES) */
    public static final String[] CODIGOS = {
        // Ejemplo final sin errores
        """
        
        // CODIGO DE PRUEBA 1: VALIDO Y OPTIMIZABLE 

        // 1. Declaracion e inicializacion de variables base del sistema
        int precioUnitario = 120;
        int cantidadLote = 15;
        int costoEnvio = 85;
        int descuentoFijo = 30;
        int stockInicial = 200;
        int stockMinimo = 25;
        int codigoCategoria = 2;
        int nivelOperacion = 1;
        int totalUnidadesProcesadas = 0;
        int penalizacionAcumulada = 0;

        double tasaImpuesto = 0.16;
        double factorBonificacion = 0.05;
        double tarifaBaseDouble = 250.50;
        double pesoPaqueteKg = 12.75;
        double recargoLogistico = 18.25;

        float voltajeSensor = 12.5f;
        float ajusteCalibracion = 0.5f;

        decimal presupuestoAnual = 15000.00m;
        decimal gastoOperativo = 2450.50m;

        bool sistemaActivo = true;
        bool alertaCritica = false;

        string nombreSucursal = "Planta Toluca Norte";
        string codigoAlmacen = "ALM_04";

        char nivelPrioridad = 'A';
        char estadoLote = 'V';

        int [] historialLotes = new int [5];

        // 2. Variables sin uso posterior (ReglaVariablesSinUso - Codigo Muerto)
        int auditoriaInterna = 9012;
        int reservaEmergencia = 450;
        int calculoDescartadoEntero = auditoriaInterna + reservaEmergencia * 2;
        double coeficienteObsoleto = 1.45;
        double margenProyectado = coeficienteObsoleto * 2.50;
        float sensorAuxiliar = 3.3f;
        bool auditoriaHabilitada = true;
        string etiquetaRespaldo = "RESPALDO_TEMP_2026";
        char marcaDescartada = 'Z';

        // 3. Copias del mismo tipo sin modificacion intermedia (PropagadorCopias)
        int copiaPrecioUnitario = precioUnitario;
        int copiaCantidadLote = cantidadLote;
        int copiaCostoEnvio = costoEnvio;
        int copiaDescuentoFijo = descuentoFijo;

        double copiaTasaImpuesto = tasaImpuesto;
        double copiaTarifaBase = tarifaBaseDouble;
        double copiaPesoPaquete = pesoPaqueteKg;

        float copiaVoltajeSensor = voltajeSensor;
        decimal copiaPresupuesto = presupuestoAnual;
        bool copiaSistemaActivo = sistemaActivo;
        string copiaNombreSucursal = nombreSucursal;
        char copiaNivelPrioridad = nivelPrioridad;

        // 4. Operaciones aritmeticas que aprovechan la propagacion de copias
        int subtotalEntero = copiaPrecioUnitario * copiaCantidadLote;
        int totalConEnvio = subtotalEntero + copiaCostoEnvio;
        int totalNetoEntero = totalConEnvio - copiaDescuentoFijo;

        double montoImpuesto = copiaTarifaBase * copiaTasaImpuesto;
        double montoBonificacion = copiaTarifaBase * factorBonificacion;
        double costoPorPeso = copiaPesoPaquete * 2.0;
        double totalDoubleFinal = copiaTarifaBase + montoImpuesto - montoBonificacion + costoPorPeso;

        float lecturaCalibrada = copiaVoltajeSensor + ajusteCalibracion;
        decimal saldoDisponible = copiaPresupuesto - gastoOperativo;

        // 5. Evaluacion de condiciones constantes (ReglaCondicionConstante)
        if (false) {
            int ajusteFantasma = 5000;
            totalNetoEntero = totalNetoEntero + ajusteFantasma;
            Console.WriteLine("Esta rama nunca debe ejecutarse");
        }

        if (10 > 25) {
            double tarifaInvalida = 999.99;
            totalDoubleFinal = totalDoubleFinal + tarifaInvalida;
        } else {
            totalDoubleFinal = totalDoubleFinal + recargoLogistico;
        }

        if (true) {
            stockInicial -= copiaCantidadLote;
        } else {
            int reposicionImposible = 1000;
            stockInicial += reposicionImposible;
        }

        if (100 == 100) {
            penalizacionAcumulada += 5;
        } else {
            penalizacionAcumulada += 500;
        }

        // 6. Validaciones logicas y relacionales con copias propagadas
        if (copiaSistemaActivo && totalNetoEntero > 1000) {
            Console.WriteLine(copiaNombreSucursal);
            Console.WriteLine(copiaNivelPrioridad);
        } else if (alertaCritica || stockInicial < stockMinimo) {
            Console.WriteLine(codigoAlmacen);
        } else {
            Console.WriteLine(estadoLote);
        }

        // 7. Ciclo for con asignaciones compuestas, incrementos y codigo inalcanzable
        int indiceFor = 0;
        int acumuladorCiclo = 0;
        int factorMultiplicador = 3;
        int copiaFactor = factorMultiplicador;

        for (indiceFor = 0; indiceFor < 8; indiceFor++) {
            acumuladorCiclo += copiaFactor;
            totalUnidadesProcesadas++;

            if (indiceFor == 2) {
                acumuladorCiclo += 10;
                continue;
                // Codigo inalcanzable despues de continue (ReglaInalcanzable)
                int variableMuertaContinue = 777;
                acumuladorCiclo = acumuladorCiclo + variableMuertaContinue;
            }

            if (indiceFor == 6) {
                acumuladorCiclo -= 4;
                break;
                // Codigo inalcanzable despues de break (ReglaInalcanzable)
                int variableMuertaBreak = 888;
                totalUnidadesProcesadas = totalUnidadesProcesadas + variableMuertaBreak;
            }

            acumuladorCiclo *= 2;
            acumuladorCiclo /= 2;
        }

        // 8. Ciclo while con condicion constante y ciclo while operativo
        while (false) {
            int contadorInalcanzable = 1;
            contadorInalcanzable++;
            Console.WriteLine(contadorInalcanzable);
        }

        int iteradorWhile = 0;
        int sumaControlWhile = 100;
        int incrementoBase = 15;
        int copiaIncrementoBase = incrementoBase;

        while (iteradorWhile < 5) {
            sumaControlWhile += copiaIncrementoBase;
            iteradorWhile++;

            if (iteradorWhile == 4) {
                sumaControlWhile %= 70;
                break;
                // Instrucciones muertas posteriores al break (ReglaInalcanzable)
                int errorLogico = 9999;
                sumaControlWhile = errorLogico;
            }
        }

        // 9. Ciclo do-while con propagacion de copias y decremento
        int intentosRestantes = 3;
        int energiaSistema = 90;
        int consumoPorCiclo = 10;
        int copiaConsumo = consumoPorCiclo;

        do {
            energiaSistema -= copiaConsumo;
            intentosRestantes--;
        } while (intentosRestantes > 0);

        // 10. Estructura switch-case con codigo inalcanzable tras break
        int resultadoCategoria = 0;
        int bonoBaseCategoria = 40;
        int copiaBonoCategoria = bonoBaseCategoria;

        switch (codigoCategoria) {
            case 1:
                resultadoCategoria = copiaBonoCategoria + 10;
                break;
                int instruccionMuertaCaso1 = 111;
                resultadoCategoria = instruccionMuertaCaso1;
            case 2:
                resultadoCategoria = copiaBonoCategoria * 2;
                break;
                int instruccionMuertaCaso2 = 222;
                resultadoCategoria = instruccionMuertaCaso2;
            case 3:
                resultadoCategoria = copiaBonoCategoria + 30;
                break;
            default:
                resultadoCategoria = 5;
                break;
        }

        // 11. Consolidacion final de metricas y salida por consola
        int resumenOperativo = totalNetoEntero + acumuladorCiclo + sumaControlWhile + resultadoCategoria;
        int copiaResumenOperativo = resumenOperativo;
        int resultadoFinalAuditado = copiaResumenOperativo + energiaSistema - penalizacionAcumulada;

        // Variable final sin uso para verificar limpieza al cierre del programa
        int indicadorDescartadoFinal = resultadoFinalAuditado * 0;

        if (nivelOperacion >= 1 && copiaSistemaActivo) {
            Console.WriteLine(resultadoFinalAuditado);
            Console.WriteLine(totalDoubleFinal);
            Console.WriteLine(lecturaCalibrada);
            Console.WriteLine(saldoDisponible);
            Console.WriteLine("Proceso finalizado correctamente");
        }
        """,

        // Código de prueba final 2: con errores
        """
        // CODIGO DE PRUEBA 2: ERRORES LEXICOS, SINTACTICOS Y SEMANTICOS

        // ----------------------------------------------------------------------------
        // BLOQUE BASE: Declaraciones validas de referencia para la tabla de simbolos
        // ----------------------------------------------------------------------------
        int stockBase = 150;
        int limiteMinimo = 20;
        int contadorGeneral = 0;
        double tasaImpuesto = 0.16;
        double precioBase = 320.50;
        float factorAjuste = 1.25f;
        decimal presupuestoTotal = 8500.00m;
        bool estadoActivo = true;
        string mensajeSistema = "Sistema de inventario";
        char categoriaPrincipal = 'A';
        int [] registrosValidos = new int [4];

        // ----------------------------------------------------------------------------
        // SECCION 1: ERRORES LEXICOS (Tokens mal formados y caracteres fuera de alfabeto)
        // ----------------------------------------------------------------------------
        // Identificadores que inician con digitos (violan LMa|LMi al inicio)
        int 1producto = 45;
        double 24horas = 12.5;
        string 99etiqueta = "ErrorLexicoID";

        // Literales numericos mal construidos (violan expresiones regulares de 2.3)
        double dobleSinDecimales = 45.;
        double dobleSinEntero = .75;
        double dobleMultiplePunto = 12.34.56;
        float flotanteSinPunto = 18f;
        float flotanteSufijoInvalido = 18.5d;
        decimal decimalSinPunto = 500m;

        // Literales char mal formados (mas de un caracter o vacio)
        char caracterMultiple = 'AB';
        char caracterVacio = '';

        // Caracteres no pertenecientes al alfabeto definido (enie, grados, signos abiertos)
        int cantidad_año = 2026;
        int nivel_señal = 80;
        int temperatura° = 28;
        string pregunta = "¿Continuar proceso?";
        string exclamacion = "¡Alerta critica!";

        // Cadena sin comilla de cierre
        string cadenaAbierta = "Esta cadena nunca se cierra correctamente;

        // ----------------------------------------------------------------------------
        // SECCION 2: ERRORES SEMANTICOS - REDECLARACION DE VARIABLES
        // ----------------------------------------------------------------------------
        int stockBase = 300;
        double stockBase = 99.99;
        double tasaImpuesto = 0.08;
        bool estadoActivo = false;
        string mensajeSistema = "Intento de redeclaracion";
        char categoriaPrincipal = 'B';
        int contadorGeneral = 10;

        // ----------------------------------------------------------------------------
        // SECCION 3: ERRORES SEMANTICOS - VARIABLES NO DECLARADAS
        // ----------------------------------------------------------------------------
        variableNoDeclarada = 100;
        costoTransporte = 45.50;
        int resultadoConFantasma = stockBase + variableInexistente * 3;
        double calculoSinDeclarar = precioBase * impuestoDesconocido;
        identificadorFantasma++;
        otroFantasma--;
        acumuladorInexistente += 25;
        descuentoInexistente -= 5;
        Console.WriteLine(datoNoRegistrado);

        if (banderaInexistente && stockBase > 10) {
            Console.WriteLine(mensajeNoDeclarado);
        }

        // ----------------------------------------------------------------------------
        // SECCION 4: ERRORES SEMANTICOS - INCOMPATIBILIDAD DE TIPOS Y ARGUMENTOS
        // ----------------------------------------------------------------------------
        // Asignaciones entre tipos incompatibles
        int enteroConCadena = "Texto en entero";
        int enteroConBooleano = true;
        double dobleConCadena = "No es numero";
        double dobleConBooleano = false;
        bool booleanoConEntero = 123;
        bool booleanoConCadena = "true";
        string cadenaConEntero = 404;
        char caracterConCadena = "XYZ";

        // Reasignaciones con tipo incorrecto a variables ya declaradas
        stockBase = "Doscientos";
        tasaImpuesto = false;
        estadoActivo = 99;
        mensajeSistema = 12.34;

        // Operaciones aritmeticas y relacionales con tipos incompatibles
        int sumaInvalida = mensajeSistema + stockBase;
        double restaInvalida = precioBase - estadoActivo;
        bool relacionInvalida = "cadena" < 50;

        // Cantidad incorrecta de argumentos en metodos (ARGUMENTOS_DE_MENOS / DE_MAS)
        Console.WriteLine();
        Console.WriteLine("Primer argumento", stockBase);
        Console.WriteLine("A", "B", "C", 123);

        // ----------------------------------------------------------------------------
        // SECCION 5: ERRORES SINTACTICOS - DECLARACIONES, ASIGNACIONES Y EXPRESIONES
        // ----------------------------------------------------------------------------
        // Falta de punto y coma al final de sentencias
        int faltaPuntoComa1 = 50
        double faltaPuntoComa2 = 14.5
        stockBase = 200
        Console.WriteLine(stockBase)

        // Declaraciones e inicializaciones mal estructuradas
        int = 80;
        double sinOperadorAsignacion 19.99;
        string nombreIncompleto = ;
        int [] arregloSinTamano = new int [];
        int [] arregloSinNew = int [5];

        // Expresiones aritmeticas y logicas con sintaxis rota
        int expresionRota1 = 10 + * 5;
        int expresionRota2 = (25 + 15 * 2;
        int expresionRota3 = 40 + 10);
        int expresionRota4 = 100 / ;
        bool logicaRota1 = (stockBase == );
        bool logicaRota2 = (&& estadoActivo);

        // Operadores de incremento/decremento prefijos (gramatica solo admite postfijo)
        ++contadorGeneral;
        --contadorGeneral;
        contadorGeneral ++= 2;

        // Llamadas a Console.WriteLine mal cerradas
        Console.WriteLine("Parentesis sin cerrar";
        Console.WriteLine);

        // ----------------------------------------------------------------------------
        // SECCION 6: ERRORES SINTACTICOS - CONDICIONALES IF / ELSE / SWITCH
        // ----------------------------------------------------------------------------
        // Condicion if sin parentesis
        if stockBase > limiteMinimo {
            Console.WriteLine("Faltan parentesis en if");
        }

        // Condicion if sin llaves de bloque (la gramatica exige <bloque>)
        if (stockBase > 10)
            stockBase = stockBase - 5;

        // Bloque else huerfano sin if previo
        else {
            stockBase = 0;
        }

        // Else if mal formado
        if (contadorGeneral == 0) {
            contadorGeneral = 1;
        } else if contadorGeneral == 1 {
            contadorGeneral = 2;
        }

        // Estructura switch-case con errores de sintaxis
        switch stockBase {
            case 10;
                contadorGeneral = 10;
                break
            case :
                contadorGeneral = 20;
                break;
            default;
                contadorGeneral = 0;
                break;
        }

        // ----------------------------------------------------------------------------
        // SECCION 7: ERRORES SINTACTICOS - CICLOS WHILE, DO-WHILE Y FOR
        // ----------------------------------------------------------------------------
        // Ciclo while sin parentesis y sin llave de cierre
        while contadorGeneral < 10 {
            contadorGeneral++;
        }

        while (contadorGeneral < 5) {
            contadorGeneral++;
        // Falta llave de cierre del while anterior

        // Ciclo do-while sin punto y coma final y sin parentesis
        do {
            stockBase--;
        } while (stockBase > 50)

        do {
            stockBase--;
        } while stockBase > 25;

        // Ciclo for con separadores incorrectos (comas en lugar de punto y coma)
        for (int i = 0, i < 10, i++) {
            contadorGeneral += i;
        }

        // Ciclo for incompleto (falta seccion de actualizacion)
        for (int j = 0; j < 8) {
            contadorGeneral += j;
        }

        // Ciclo for sin parentesis
        for int k = 0; k < 5; k++ {
            contadorGeneral++;
        }

        // ----------------------------------------------------------------------------
        // SECCION 8: COMBINACION DE ERRORES AL CIERRE DEL PROGRAMA
        // ----------------------------------------------------------------------------
        int 7cierreInvalido = "Mezcla lexico y semantico";
        int stockBase = variableFinalNoDeclarada + * 10
        Console.WriteLine("Fin de prueba con errores", 1, 2)
        break
        continue
        """,

        // ── Polaca y Cuádruplos (Múltiples expresiones complejas) ──
        """
        // Múltiples expresiones aritméticas complejas
        int a = 5;
        int b = 3;
        int c = 4;
        int d = 10;
        double e = 2.5;

        // 1. Precedencia mixta con agrupación: (5+3)*4 - 10/2 = 27
        int r1 = (a + b) * c - d / 2;

        // 2. Anidamiento profundo y módulo: 5 * (3 + 4 * (10 - 5)) % 7 = 115 % 7 = 3
        int r2 = a * (b + c * (d - a)) % 7;

        // 3. Aritmética mixta con decimales: (5 + 2.5) * (4 - 3) / 2 = 3.75
        double r3 = (a + e) * (c - b) / 2;

        // 4. División entera compuesta: ((5 - 3) * (4 + 10)) / (3 + 1) = 28 / 4 = 7
        int r4 = ((a - b) * (c + d)) / (b + 1);

        // 5. Asignación compuesta sobre resultado previo: 27 + 3 * 2 = 33
        r1 += r2 * 2;

        Console.WriteLine(r1);
        Console.WriteLine(r2);
        Console.WriteLine(r3);
        Console.WriteLine(r4);
        """,

        // ── Hola Mundo ──
            """
            // Programa básico: Hola Mundo
            string saludo = "Hola mundo";
            Console.WriteLine(saludo);
            """,

        // ── Variables y operaciones ──
        "// Declaraciones con diferentes tipos\n" +
        "int x = 10;\n" +
        "int y = 20;\n" +
        "float resultado = 0.0;\n" +
        "\n" +
        "// Operación aritmética\n" +
        "resultado = x + y * 2;\n" +
        "\n" +
        "// Salida por consola\n" +
        "Console.WriteLine(resultado);\n",

        // ── Expresiones complejas ──
        "// Operaciones con paréntesis y múltiples tipos\n" +
        "int a = 5;\n" +
        "int b = 3;\n" +
        "int c = (a + b) * 2;\n" +
        "double d = 3.14;\n" +
        "\n" +
        "// Booleanos\n" +
        "bool activo = true;\n" +
        "bool inactivo = false;\n" +
        "\n" +
        "// Cadena con caracteres especiales\n" +
        "string msg = \"Resultado: \";\n" +
        "Console.WriteLine(msg);\n" +
        "Console.WriteLine(c);\n",

        // ── Errores de ejemplo ──
        "// Este código tiene errores intencionales\n" +
        "int x = 10;\n" +
        "int x = 20;\n" +            // Redeclaración
        "y = 5;\n" +                  // Variable no declarada
        "Console.WriteLine(x);\n",

        // ── Programa vacío ──
        "// Programa sin sentencias\n",

        // ── Eliminación de Código Muerto ──
        """
        // Ejemplo de Eliminación de Código Muerto (Reglas R1, R2, R3, R4)
        int a = 10;
        int b = 20;

        // Variables vivas: se calculan y se imprimen
        int r1 = a + b * 2;

        // R4: Variables sin uso posterior ni efectos secundarios
        int basura1 = 50 * 2 + 10;
        int tempInutil = a * 100;

        // R2: Condición constante falsa (el cuerpo nunca se ejecutará)
        if (false) {
            int muerto1 = 999;
            Console.WriteLine(muerto1);
        }

        while (false) {
            int muerto2 = 888;
        }

        // R3: Condición constante verdadera con else inalcanzable
        if (true) {
            Console.WriteLine(r1);
        } else {
            int muertoElse = 777;
        }

        // R1: Código inalcanzable tras terminador return
        return 0;

        int codigoInalcanzable = 500;
        Console.WriteLine(codigoInalcanzable);
        """
    };

    /**
     * Retorna el número de ejemplos disponibles.
     */
    public static int cantidad() {
        return NOMBRES.length;
    }

    /**
     * Retorna el código del ejemplo en la posición indicada.
     */
    public static String getCodigo(int indice) {
        if (indice >= 0 && indice < CODIGOS.length) {
            return CODIGOS[indice];
        }
        return "";
    }

    /**
     * Retorna el nombre del ejemplo en la posición indicada.
     */
    public static String getNombre(int indice) {
        if (indice >= 0 && indice < NOMBRES.length) {
            return NOMBRES[indice];
        }
        return "Desconocido";
    }
}
