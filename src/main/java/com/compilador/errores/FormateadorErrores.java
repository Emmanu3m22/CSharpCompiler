package com.compilador.errores;

import java.util.ArrayList;
import java.util.List;

/**
 * Utilidad centralizada para transformar errores técnicos (de JavaCC o del analizador)
 * en mensajes comprensibles, amigables y educativos para el usuario en la interfaz gráfica.
 */
public class FormateadorErrores {

    /**
     * Contenedor de la información formateada para mostrar en la interfaz.
     */
    public static class ErrorFormateado {
        private final String titulo;
        private final String mensaje;
        private final String tokenEsperado;
        private final String sugerencia;

        public ErrorFormateado(String titulo, String mensaje, String tokenEsperado, String sugerencia) {
            this.titulo = titulo;
            this.mensaje = mensaje;
            this.tokenEsperado = tokenEsperado;
            this.sugerencia = sugerencia;
        }

        public String getTitulo() {
            return titulo;
        }

        public String getMensaje() {
            return mensaje;
        }

        public String getTokenEsperado() {
            return tokenEsperado;
        }

        public String getSugerencia() {
            return sugerencia;
        }
    }

    /**
     * Procesa un ErrorSintactico y genera una versión clara y descriptiva en español.
     */
    public static ErrorFormateado formatearSintactico(ErrorSintactico error) {
        String encontrado = error.getTokenEncontrado() != null ? error.getTokenEncontrado() : "";
        String rawMsg = error.getMensaje() != null ? error.getMensaje() : "";
        String esperadoToken = error.getTokenEsperado();

        // 1. Si el mensaje es una descripción personalizada (no proviene directamente del toString de ParseException)
        if (!rawMsg.startsWith("Encountered \"") && !rawMsg.contains("Was expecting:") && !rawMsg.contains("Was expecting one of:")) {
            String titulo = deducirTitulo(rawMsg, encontrado);
            String esperado = (esperadoToken != null && !"...".equals(esperadoToken)) ? traducirToken(esperadoToken) : "";
            String sugerencia = generarSugerencia(rawMsg, encontrado, esperado);
            return new ErrorFormateado(titulo, rawMsg, esperado, sugerencia);
        }

        // 2. Si es un mensaje nativo de JavaCC ("Was expecting:" o "Was expecting one of:")
        if (rawMsg.contains("Was expecting:")) {
            // JavaCC esperaba exactamente UN token
            String[] partes = rawMsg.split("Was expecting:");
            String tokenRaw = partes.length > 1 ? partes[1].replaceAll("[\r\n\t]", " ").trim() : "";
            String esperadoLimpio = limpiarTokenJavaCC(tokenRaw);

            return procesarEsperadoUnico(encontrado, esperadoLimpio);
        } else if (rawMsg.contains("Was expecting one of:")) {
            // JavaCC esperaba uno de varios tokens posibles
            String[] partes = rawMsg.split("Was expecting one of:");
            String tokensRaw = partes.length > 1 ? partes[1] : "";
            return procesarEsperadosMultiples(encontrado, tokensRaw);
        }

        // 3. Fallback general para errores no clasificados
        String msg = "Error de sintaxis cerca de '" + encontrado + "'.";
        return new ErrorFormateado("Error de Sintaxis", msg, "", "Revisa la estructura de la sentencia.");
    }

    /**
     * Procesa un ErrorLexico y genera un mensaje y sugerencia claros.
     */
    public static ErrorFormateado formatearLexico(ErrorLexico error) {
        String lexema = error.getLexema() != null ? error.getLexema() : "";
        String titulo = "Carácter Inválido";
        String mensaje;
        String sugerencia;

        if ("#".equals(lexema)) {
            titulo = "Directiva no Soportada";
            mensaje = "El símbolo '#' (usado para directivas de preprocesador como #region o #if) no está soportado en este compilador.";
            sugerencia = "Elimina las directivas de preprocesador del código.";
        } else if ("$".equals(lexema)) {
            titulo = "Interpolación no Soportada";
            mensaje = "El símbolo '$' (interpolación de cadenas de texto) no está implementado.";
            sugerencia = "Usa concatenación de texto tradicional con el operador '+'.";
        } else if ("\\".equals(lexema)) {
            titulo = "Secuencia de Escape Inválida";
            mensaje = "La barra invertida '\\' solo es válida dentro de cadenas o caracteres como secuencia de escape (ej. '\\n').";
            sugerencia = "Elimina la barra suelta o escríbela dentro de comillas.";
        } else if ("`".equals(lexema)) {
            titulo = "Carácter Inválido";
            mensaje = "El carácter acento grave '`' no existe en la sintaxis de C#.";
            sugerencia = "Elimina el carácter '`' del identificador.";
        } else if ("¿".equals(lexema)) {
            titulo = "Carácter Inválido";
            mensaje = "El signo de interrogación de apertura '¿' no es un símbolo válido en C# fuera de cadenas de texto.";
            sugerencia = "Si es parte de un texto, enciérralo entre comillas dobles \"...\".";
        } else if (error.getMensaje() != null && error.getMensaje().toLowerCase().contains("identificador")) {
            titulo = "Identificador Inválido";
            mensaje = "El identificador '" + lexema + "' contiene caracteres no permitidos o formato incorrecto.";
            sugerencia = "Los identificadores deben comenzar con letra o guión bajo y no contener símbolos especiales.";
        } else {
            mensaje = "Símbolo '" + lexema + "' no reconocido por el analizador léxico.";
            sugerencia = "Verifica que el símbolo sea válido en el lenguaje C#.";
        }

        return new ErrorFormateado(titulo, mensaje, "", sugerencia);
    }

    private static ErrorFormateado procesarEsperadoUnico(String encontrado, String esperadoRaw) {
        String esperado = traducirToken(esperadoRaw);

        if (";".equals(esperadoRaw) || esperadoRaw.contains(";")) {
            return new ErrorFormateado(
                "Falta Punto y Coma",
                "Falta un punto y coma ';' al final de la instrucción.",
                ";",
                "Coloca ';' al final de la sentencia."
            );
        } else if (")".equals(esperadoRaw) || esperadoRaw.contains(")")) {
            return new ErrorFormateado(
                "Falta Cerrar Paréntesis",
                "Falta cerrar paréntesis ')' en la expresión o lista de parámetros.",
                ")",
                "Agrega ')' antes de continuar con el bloque o sentencia."
            );
        } else if ("(".equals(esperadoRaw) || esperadoRaw.contains("(")) {
            return new ErrorFormateado(
                "Falta Abrir Paréntesis",
                "Falta abrir paréntesis '(' después de la estructura o nombre de método.",
                "(",
                "Abre '(' para los argumentos o condición."
            );
        } else if ("}".equals(esperadoRaw) || esperadoRaw.contains("}")) {
            return new ErrorFormateado(
                "Falta Cerrar Llave",
                "Falta cerrar llave '}' para concluir el bloque de código o clase.",
                "}",
                "Cierra el bloque con '}'."
            );
        } else if ("{".equals(esperadoRaw) || esperadoRaw.contains("{")) {
            return new ErrorFormateado(
                "Falta Abrir Llave",
                "Falta abrir llave '{' para iniciar el cuerpo de la clase, método o bloque.",
                "{",
                "Abre el bloque de código con '{'."
            );
        } else if (":".equals(esperadoRaw) || esperadoRaw.contains(":")) {
            return new ErrorFormateado(
                "Faltan Dos Puntos",
                "Faltan dos puntos ':' (requerido en cláusulas 'case', 'default' o herencia).",
                ":",
                "Coloca ':' después del caso o nombre de la clase base."
            );
        } else if (",".equals(esperadoRaw) || esperadoRaw.contains(",")) {
            return new ErrorFormateado(
                "Falta Coma",
                "Falta una coma ',' para separar los elementos o parámetros.",
                ",",
                "Separa cada elemento con ','."
            );
        } else if ("=".equals(esperadoRaw) || esperadoRaw.contains("=")) {
            return new ErrorFormateado(
                "Falta Asignación",
                "Falta el operador de asignación '='.",
                "=",
                "Asigna un valor usando el operador '='."
            );
        } else if (esperadoRaw.contains("IDENTIFICADOR")) {
            return new ErrorFormateado(
                "Identificador Faltante",
                "Se esperaba un nombre o identificador válido.",
                "Identificador",
                "Escribe el nombre de la variable, función o clase."
            );
        }

        return new ErrorFormateado(
            "Elemento Faltante",
            "Se esperaba " + esperado + " antes de '" + encontrado + "'.",
            esperado,
            "Inserta el elemento esperado en esta posición."
        );
    }

    private static ErrorFormateado procesarEsperadosMultiples(String encontrado, String tokensRaw) {
        String tokenLimpio = encontrado.trim();

        // 1. Detección de errores comunes según el token encontrado
        if (tokenLimpio.matches("\\d+(\\.\\d+)?([fFmMdDlL])?")) {
            return new ErrorFormateado(
                "Instrucción Inválida",
                "No se puede iniciar una instrucción con un valor numérico ('" + tokenLimpio + "').",
                "Declaración o asignación",
                "Una instrucción debe iniciar con un tipo de dato, nombre de variable o palabra clave."
            );
        }

        if (tokenLimpio.startsWith("\"") || tokenLimpio.startsWith("'")) {
            return new ErrorFormateado(
                "Valor Literal Inesperado",
                "Literal de texto o carácter inesperado en esta posición: " + tokenLimpio,
                "Instrucción válida",
                "Verifica que la asignación o llamada a método esté completa."
            );
        }

        if (";".equals(tokenLimpio)) {
            return new ErrorFormateado(
                "Expresión Incompleta",
                "Punto y coma ';' inesperado. La instrucción anterior está vacía o incompleta.",
                "Expresión o valor",
                "Completa la expresión antes del punto y coma ';'."
            );
        }

        if ("}".equals(tokenLimpio)) {
            return new ErrorFormateado(
                "Llave de Cierre Inesperada",
                "Llave de cierre '}' inesperada. Puede haber una instrucción incompleta o sobra una llave.",
                "Instrucción o ';'",
                "Verifica que todas las sentencias dentro del bloque estén completas."
            );
        }

        if ("{".equals(tokenLimpio)) {
            return new ErrorFormateado(
                "Llave de Apertura Inesperada",
                "Llave de apertura '{' inesperada. Revisa si la cabecera de la función o clase es correcta.",
                "Cabecera válida",
                "Asegúrate de definir correctamente la clase, método o estructura antes de abrir '{'."
            );
        }

        if (")".equals(tokenLimpio)) {
            return new ErrorFormateado(
                "Paréntesis Inesperado",
                "Paréntesis de cierre ')' inesperado o expresión vacía dentro del paréntesis.",
                "Expresión",
                "Verifica que la condición o los parámetros no estén vacíos."
            );
        }

        if ("]".equals(tokenLimpio)) {
            return new ErrorFormateado(
                "Corchete Inesperado",
                "Corchete de cierre ']' sin tamaño de arreglo o índice especificado.",
                "Tamaño de arreglo",
                "Especifica el tamaño del arreglo entre corchetes (ej. 'new int[10]')."
            );
        }

        if ("EOF".equalsIgnoreCase(tokenLimpio) || tokenLimpio.isEmpty()) {
            return new ErrorFormateado(
                "Fin de Archivo Inesperado",
                "El archivo terminó de forma inesperada. Faltan llaves de cierre '}' para completar bloques.",
                "}",
                "Asegúrate de que cada llave '{' abierta tenga su correspondiente llave de cierre '}'."
            );
        }

        if ("this".equals(tokenLimpio)) {
            return new ErrorFormateado(
                "Asignación Inválida",
                "No se puede asignar un valor a la palabra reservada 'this' (es de solo lectura).",
                "Variable asignable",
                "Asigna a una propiedad o campo: 'this.campo = valor;'."
            );
        }

        // 2. Uso incorrecto de palabras reservadas como identificadores
        if (esPalabraReservada(tokenLimpio)) {
            // Si el token es un tipo de dato y se encuentra dentro de un bloque donde se esperaba una instrucción:
            // Ejemplo: `int a = 1 \n int b = 2;` -> faltaba ';' en la línea anterior
            if (tokensRaw.contains("\";\"")) {
                return new ErrorFormateado(
                    "Falta Punto y Coma",
                    "Falta un punto y coma ';' al final de la instrucción anterior a '" + tokenLimpio + "'.",
                    ";",
                    "Agrega ';' al finalizar la instrucción previa."
                );
            }

            return new ErrorFormateado(
                "Palabra Reservada Mal Utilizada",
                "La palabra reservada '" + tokenLimpio + "' no puede ser utilizada como nombre de variable o en esta posición.",
                "Identificador",
                "Elige un nombre de identificador diferente o usa '@" + tokenLimpio + "' si deseas usar esa palabra clave."
            );
        }

        // 3. Identificador en posición errónea (ej: falta tipo de dato)
        if (tokenLimpio.matches("[a-zA-Z_][a-zA-Z0-9_]*")) {
            return new ErrorFormateado(
                "Declaración o Sentencia Incorrecta",
                "Identificador '" + tokenLimpio + "' inesperado en este contexto (¿falta tipo de dato o palabra clave previa?).",
                "Tipo de dato o instrucción",
                "Si estás declarando una variable, antepón su tipo (ej. 'int " + tokenLimpio + " = ...;')."
            );
        }

        // 4. Operador inesperado o duplicado (+, *, etc.)
        if (tokenLimpio.matches("[+\\-*/%&|^=!<>]")) {
            return new ErrorFormateado(
                "Operador Inesperado",
                "Operador '" + tokenLimpio + "' inesperado o duplicado en la expresión.",
                "Operando o expresión",
                "Verifica que no haya dos operadores consecutivos ni falte un valor."
            );
        }

        // 5. Lista reducida de tokens limpios
        List<String> opciones = extraerOpcionesTokens(tokensRaw);
        String esperadoResumen;
        if (opciones.isEmpty() || opciones.size() > 4) {
            esperadoResumen = "Instrucción, expresión u operador válido";
        } else {
            esperadoResumen = String.join(", ", opciones);
        }

        return new ErrorFormateado(
            "Sintaxis Inesperada",
            "Sintaxis inesperada cerca de '" + tokenLimpio + "'.",
            esperadoResumen,
            "Verifica la sintaxis de la instrucción."
        );
    }

    private static String deducirTitulo(String mensaje, String encontrado) {
        String lower = mensaje.toLowerCase();
        if (lower.contains("clase")) return "Error en Declaración de Clase";
        if (lower.contains("interfaz")) return "Error en Declaración de Interfaz";
        if (lower.contains("método") || lower.contains("metodo")) return "Error en Declaración de Método";
        if (lower.contains("paréntesis") || lower.contains("parentesis")) return "Paréntesis Faltante";
        if (lower.contains("punto y coma")) return "Falta Punto y Coma";
        if (lower.contains("bloque") || lower.contains("llave")) return "Error de Bloque";
        if (lower.contains("condición") || lower.contains("condicion")) return "Condición Inválida";
        return "Error Sintáctico";
    }

    private static String generarSugerencia(String mensaje, String encontrado, String esperado) {
        String lower = mensaje.toLowerCase();
        if (lower.contains("nombre de la clase")) {
            return "Agrega un nombre para la clase (ej. 'class MiClase').";
        }
        if (lower.contains("nombre del método") || lower.contains("parametros")) {
            return "Especifica el nombre y paréntesis con parámetros (ej. 'void MiMetodo()').";
        }
        if (lower.contains("después de 'if'") || lower.contains("despues de 'if'")) {
            return "Encierra la condición entre paréntesis: 'if (condición)'.";
        }
        if (lower.contains("falta '}'")) {
            return "Revisa que cada bloque '{' tenga su correspondiente llave de cierre '}'.";
        }
        if (esperado != null && !esperado.isEmpty()) {
            return "Se esperaba " + esperado + " cerca de '" + encontrado + "'.";
        }
        return "Revisa la sintaxis de esta instrucción.";
    }

    private static boolean esPalabraReservada(String token) {
        switch (token) {
            case "class": case "struct": case "interface": case "enum":
            case "public": case "private": case "protected": case "internal":
            case "static": case "virtual": case "override": case "readonly": case "const":
            case "int": case "float": case "double": case "bool": case "string": case "char": case "void":
            case "byte": case "sbyte": case "short": case "ushort": case "uint": case "long": case "ulong": case "decimal":
            case "object": case "var":
            case "if": case "else": case "while": case "for": case "do": case "switch": case "case": case "default":
            case "return": case "break": case "continue": case "try": case "catch": case "finally": case "throw":
            case "new": case "this": case "base": case "using": case "namespace":
                return true;
            default:
                return false;
        }
    }

    private static String limpiarTokenJavaCC(String raw) {
        if (raw == null) return "";
        return raw.replaceAll("\"", "")
                  .replaceAll("\\.\\.\\.", "")
                  .replaceAll("<", "")
                  .replaceAll(">", "")
                  .trim();
    }

    private static String traducirToken(String tokenRaw) {
        if (tokenRaw == null) return "";
        String limpio = limpiarTokenJavaCC(tokenRaw);
        switch (limpio) {
            case ";": case "PUNTO_COMA": return "Punto y coma ';'";
            case ":": case "DOS_PUNTOS": return "Dos puntos ':'";
            case ",": case "COMA": return "Coma ','";
            case ".": case "PUNTO": return "Punto '.'";
            case "(": case "PAREN_IZQ": return "Paréntesis de apertura '('";
            case ")": case "PAREN_DER": return "Paréntesis de cierre ')'";
            case "{": case "LLAVE_IZQ": return "Llave de apertura '{'";
            case "}": case "LLAVE_DER": return "Llave de cierre '}'";
            case "[": case "CORCHETE_IZQ": return "Corchete de apertura '['";
            case "]": case "CORCHETE_DER": return "Corchete de cierre ']'";
            case "=": case "ASIGNACION": return "Operador '='";
            case "IDENTIFICADOR": return "Identificador (nombre)";
            case "NUMERO_ENTERO": return "Número entero";
            case "NUMERO_DECIMAL": return "Número decimal";
            case "CADENA": return "Cadena de texto";
            case "CARACTER": return "Carácter literal";
            default: return "'" + limpio + "'";
        }
    }

    private static List<String> extraerOpcionesTokens(String tokensRaw) {
        List<String> lista = new ArrayList<>();
        if (tokensRaw == null) return lista;

        String[] lineas = tokensRaw.split("[\r\n]+");
        for (String linea : lineas) {
            String token = linea.replaceAll("\\.\\.\\.", "").trim();
            if (!token.isEmpty()) {
                String traducido = traducirToken(token);
                if (!lista.contains(traducido)) {
                    lista.add(traducido);
                }
            }
            if (lista.size() >= 4) break;
        }
        return lista;
    }
}
