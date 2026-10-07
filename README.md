# Analizadores léxico y sintáctico del lenguaje DFD

Proyecto ANTLR 4 para los avances 2 y 3 del Taller 2026 de Lenguajes de Programación y Compiladores.

## Requisitos

- JDK 17 o superior.
- Maven 3.9 o superior.

## Compilar y probar

```powershell
mvn -q clean package
```

## Mostrar tokens

```powershell
mvn -q exec:java `
  '-Dexec.mainClass=org.example.MostrarTokens' `
  '-Dexec.args=src/test/resources/programa_informe1.dfd'
```

Cada línea informa `TIPO | LEXEMA | LINEA | COLUMNA`. Los caracteres que no pertenecen al vocabulario se emiten como `ERROR` para que el diagnóstico quede visible.

Las entradas de prueba están en `src/test/resources/casos` y las salidas ejecutadas se conservan en `evidencia/salidas`.

## Analizador sintáctico

`DFDParser.g4` consume los tokens de `DFDLexer.g4` y valida la estructura del programa completo, incluidas las declaraciones DFD, los bloques, las estructuras `si`/`sino` y `mientras`, las llamadas a funciones y las expresiones con precedencia. El parser no realiza comprobaciones semánticas.

Para compilar las gramáticas y comprobar los casos válidos e inválidos:

```powershell
mvn -q clean package
./tools/verify_parser.ps1
```

El verificador usa el `TestRig` de ANTLR para mostrar el árbol sintáctico de los programas válidos y comprobar diagnósticos en los programas con errores sintácticos. Las fuentes Java del proyecto no implementan el parser: ANTLR genera sus clases a partir de las gramáticas `.g4`.

## Generar el informe de avance 3

Con `uv` instalado, el DOCX editable y el PDF se generan con:

```powershell
uv run --no-project --python 3.13 --with python-docx --with reportlab tools/generate_report_3.py
```
