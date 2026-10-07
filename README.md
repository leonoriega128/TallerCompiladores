# Analizador léxico del lenguaje DFD

Proyecto ANTLR 4 para el avance 2 del Taller 2026 de Lenguajes de Programación y Compiladores.

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
