# LenguajeCScharp — Proyecto de Lenguajes y Autómatas II

Compilador/analizador construido con **JavaCC** sobre **Maven**.

---

## Requisitos previos

| Herramienta | Versión requerida | Notas |
|---|---|---|
| **JDK (Java)** | 25 | Verifica con `java -version` |
| **Maven** | 3.6+ | Verifica con `mvn -version`. Si usas IntelliJ, ya trae uno embebido y no es obligatorio instalarlo aparte |
| **IDE** | El que prefieras (IntelliJ, VS Code, Eclipse) | Todos funcionan, pero cada uno requiere pasos iniciales distintos (ver abajo) |

> El proyecto compila con `source`/`target` **25** en el `pom.xml`. Asegúrate de tener el JDK 25 instalado antes de compilar.

---

## Cómo ejecutar el proyecto por primera vez

1. **Compila el proyecto desde la terminal**, en la raíz del proyecto (donde está `pom.xml`):
   ```
   mvn clean compile
   ```
   Esto es el paso más importante. Genera `Analizador.java` a partir del `Analizador.jj`. Sin este paso, **cualquier IDE va a marcar error** en las clases que usan `Analizador`, porque el archivo simplemente no existe todavía en esa máquina.

2. **Abre el proyecto en tu IDE** (IntelliJ, VS Code, Eclipse, etc.)

3. **Crea un archivo `entrada.txt`** en la raíz del proyecto (mismo nivel que `pom.xml`) con un texto de prueba válido según la gramática actual, por ejemplo:
   ```
   123
   ```

4. **Ejecuta `Main.java`**. Si todo salió bien, debería imprimir:
   ```
   Análisis exitoso
   ```

### Si tu IDE marca error en `Analizador` después del paso 1

Cada IDE indexa el código generado de forma distinta. Prueba lo siguiente según el caso:

**IntelliJ IDEA**
- Panel de Maven (derecha) → botón de reload 🔄
- Si no funciona: clic derecho en `target/generated-sources/javacc` → *Mark Directory as → Generated Sources Root*
- Si sigue sin funcionar: `File → Invalidate Caches... → Invalidate and Restart`

**VS Code**
- `Ctrl+Shift+P` → **"Java: Clean Java Language Server Workspace"** → Reload
- Si persiste, agrega manualmente la carpeta generada en `.vscode/settings.json`:
  ```json
  {
    "java.project.sourcePaths": [
      "src/main/java",
      "target/generated-sources/javacc"
    ]
  }
  ```

**Cualquier IDE**
- Vuelve a correr `mvn clean compile` desde terminal — esto fuerza que se regenere todo desde cero y suele resolver la mayoría de los casos de "cannot find symbol" o "cannot be resolved to a type".

---

## Errores comunes que ya nos pasaron (y cómo se resolvieron)



### 1. `Analizador cannot be resolved to a type` / `cannot find symbol`
**Causa:** el código generado (`Analizador.java`) todavía no existe en esa máquina/IDE porque no se ha corrido `mvn compile`, o el IDE no ha reindexado.
**Solución:** correr `mvn clean compile` y luego forzar recarga del IDE (ver sección de arriba).

### 2. `FileNotFoundException: entrada.txt`
**Causa:** el programa intenta leer un archivo de entrada que no existe.
**Solución:** crear `entrada.txt` en la raíz del proyecto con un texto válido según la gramática.


### 3. Aparecía `--enable-preview` al ejecutar sin haberlo pedido
**Causa:** en IntelliJ, *Project Structure → Language Level* estaba configurado en una versión con *preview features* (ej. "24 - Stream gathers") en vez de coincidir con el `pom.xml`.
**Solución:** *File → Project Structure → Project → Language Level* → seleccionar la versión sin "preview" que coincida con el `pom.xml`.


### 4. Llamar a un método de instancia como si fuera estático
**Causa:** código como `Analizador.inicio()` en vez de `parser.inicio()`. Los métodos generados por JavaCC pertenecen al objeto (instancia), no a la clase.
**Solución:**
```java
Analizador parser = new Analizador(new FileInputStream("entrada.txt"));
parser.inicio();
```

---

## Comandos útiles

| Comando | Qué hace |
|---|---|
| `mvn clean` | Borra `target/` por completo |
| `mvn compile` | Compila el proyecto y genera el parser a partir del `.jj` |
| `mvn clean compile` | Combina ambos — recomendado si algo se ve raro |
| `mvn compile exec:java` | Compila y ejecuta el `Main` directamente (si el plugin `exec-maven-plugin` está configurado) |