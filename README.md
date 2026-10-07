# Analizadores léxico y sintáctico del lenguaje DFD

Proyecto ANTLR 4 para los avances 2 y 3 del Taller 2026 de Lenguajes de Programación y Compiladores. Sigue la organización del ejemplo `sintactico2026` aprobado por la cátedra: una gramática combinada y un ejecutable que muestra el árbol sintáctico.

## Requisitos

- JDK 17 o superior.
- Maven 3.9 o superior.

## Compilar

Ejecutar los comandos desde la carpeta que contiene `pom.xml`:

```powershell
mvn -q clean package
```

`src/main/antlr4/org/example/DFD.g4` es el único archivo de gramática. Contiene las reglas sintácticas y léxicas; Maven genera `DFDLexer`, `DFDParser`, listeners y visitors en `target/generated-sources/antlr4`. No se editan las clases generadas.

## Analizar y mostrar el árbol

Para abrir el árbol gráfico del programa de ejemplo:

```powershell
mvn -q exec:exec `
  '-Dexec.executable=java' `
  '-Dexec.args=-cp %classpath org.example.Main'
```

Para analizar otro archivo, agregar su ruta. Si contiene espacios, encerrarla entre comillas dobles dentro de `-Dexec.args`:

```powershell
mvn -q exec:exec `
  '-Dexec.executable=java' `
  '-Dexec.args=-cp %classpath org.example.Main src/test/resources/casos/sintaxis_precedencia_valida.dfd'
```

`Main [--sin-gui] [archivo.dfd]` admite un archivo, con ruta absoluta o relativa a la carpeta de ejecución. Sin archivo usa `src/test/resources/programa_informe1.dfd`. En IntelliJ se puede ejecutar directamente `org.example.Main` después de generar las fuentes con Maven.

La consola informa aceptación o rechazo y la cantidad de errores. Los programas válidos muestran también el árbol textual y, por defecto, una ventana `TreeViewer` con barras de desplazamiento. Para trabajar sólo en consola:

```powershell
mvn -q exec:exec `
  '-Dexec.executable=java' `
  '-Dexec.args=-cp %classpath org.example.Main --sin-gui src/test/resources/programa_informe1.dfd'
```

Si el entorno no dispone de pantalla, se utiliza automáticamente la consola. Los programas con errores no abren ventanas ni muestran un árbol como válido.

Los diagnósticos distinguen `ERROR_LEXICO` de `ERROR_SINTACTICO` e incluyen `linea` (desde 1) y `columna` (desde 0). Ante un error léxico se omite el parser; ante errores sintácticos ANTLR intenta recuperarse para continuar el análisis. El proceso Java devuelve `0` para aceptación, `1` para errores del lenguaje y `2` para argumentos o archivos inválidos. Maven puede informar su propio código de fallo al ejecutar un proceso rechazado.

## Mostrar tokens

```powershell
mvn -q exec:java `
  '-Dexec.mainClass=org.example.MostrarTokens' `
  '-Dexec.args=src/test/resources/programa_informe1.dfd'
```

Cada línea informa `TIPO | LEXEMA | LINEA | COLUMNA`. Los caracteres fuera del vocabulario se emiten como `ERROR`; las cadenas sin cierre se emiten como `CADENA_SIN_CIERRE`. Los blancos y EOF no se imprimen. `MostrarTokens` conserva su función de mostrar tokens; para validar programas se utiliza `Main`.

Las entradas originales están en `src/test/resources/casos` y sus salidas históricas en `evidencia/salidas`.

## Reglas del lenguaje

La regla inicial `programa` valida desde `SISTEMA` hasta `FIN_SISTEMA EOF`, incluidos niveles, nodos, flujos, variables, asignaciones, bloques, `si`/`sino`, `mientras` y funciones del dominio.

Se conserva la especificación del informe corregido: tipos `entero`, `real` y `booleano`; identificadores ASCII; números con parte decimal opcional; cadenas sin escapes; palabras reservadas sensibles a mayúsculas. No se definen comentarios ni una coma como delimitador.

La precedencia, de menor a mayor, es `||`, `&&`, `not`, comparación, suma/resta, multiplicación/división y signos unarios. `not x < 10` se agrupa como `not (x < 10)`. Se rechaza `1 < 2 < 3`; se pueden escribir relaciones separadas con `&&`.

El parser comprueba la estructura. La compatibilidad de tipos, los índices enteros de nivel y las reglas de conexiones DFD corresponden a la etapa semántica.

## Verificar las pruebas

Para compilar desde cero y ejecutar la suite sin ventanas:

```powershell
mvn -q clean package
./tools/verify_parser.ps1
```

El verificador ejecuta `VerificarAnalizador`, que comprueba tokens y posiciones, precedencia en el árbol, programas válidos e inválidos, recuperación sintáctica, errores léxicos, consumo hasta EOF, rutas y códigos de salida. También renderiza el visor sin pantalla en `target/arbol-programa-informe1.png` como comprobación interna. El resumen debe indicar `FALLIDAS=0`.

Para ejecutarlo directamente, sin el script de PowerShell:

```powershell
mvn -q test-compile exec:exec `
  '-Dexec.executable=java' `
  '-Dexec.classpathScope=test' `
  '-Dexec.args=-Djava.awt.headless=true -cp %classpath org.example.VerificarAnalizador'
```

ANTLR también permite inspeccionar el árbol con `TestRig`:

```powershell
mvn -q exec:java `
  '-Dexec.mainClass=org.antlr.v4.gui.TestRig' `
  '-Dexec.args=org.example.DFD programa -tree src/test/resources/programa_informe1.dfd'
```

Para distinguir errores léxicos y sintácticos y obtener un estado de aceptación, se utiliza `Main`.

## Informes anteriores

Los informes DOCX/PDF y sus generadores se conservan como documentación de la versión anterior, que utilizaba dos gramáticas. Esta adaptación no los regenera. La organización y los comandos vigentes se describen en este README.
