grammar DFD;

// Gramática combinada: ANTLR genera DFDLexer y DFDParser desde este archivo.
// La entrada completa se comprueba con programa, incluido EOF.
programa
    : SISTEMA ID PUNTO_Y_COMA defNivel* FIN_SISTEMA EOF
    ;

defNivel
    : NIVEL NUMERO bloque
    ;

bloque
    : LLAVE_ABRE sentencia* LLAVE_CIERRA
    ;

sentencia
    : declaracionNodo
    | declaracionFlujo
    | declaracionVar
    | asignacion PUNTO_Y_COMA
    | condicional
    | iterativa
    | llamadaFuncion PUNTO_Y_COMA
    ;

declaracionNodo
    : tipoNodo ID ASIGNAR CADENA PUNTO_Y_COMA
    ;

tipoNodo
    : ENTIDAD
    | PROCESO
    | ALMACEN
    ;

declaracionFlujo
    : FLUJO ID ASIGNAR CONECTAR PAREN_ABRE ID FLECHA ID PAREN_CIERRA
      CON_DATO CADENA PUNTO_Y_COMA
    ;

declaracionVar
    : tipo ID ASIGNAR expresion PUNTO_Y_COMA
    ;

tipo
    : ENTERO
    | REAL
    | BOOLEANO
    ;

asignacion
    : ID ASIGNAR expresion
    ;

condicional
    : SI PAREN_ABRE expresion PAREN_CIERRA bloque (SINO bloque)?
    ;

iterativa
    : MIENTRAS PAREN_ABRE expresion PAREN_CIERRA bloque
    ;

llamadaFuncion
    : IMPRIMIR PAREN_ABRE expresion PAREN_CIERRA
    | VALIDAR_MODELO PAREN_ABRE NUMERO PAREN_CIERRA
    | CONTAR_NODOS PAREN_ABRE tipoNodo PAREN_CIERRA
    ;

// Precedencia creciente: ||, &&, not, comparación, suma, producto y signos.
expresion
    : disyuncion
    ;

disyuncion
    : conjuncion (OR conjuncion)*
    ;

conjuncion
    : negacion (AND negacion)*
    ;

// not a < b se interpreta como not (a < b).
negacion
    : NOT negacion
    | relacion
    ;

// Sólo una comparación por relación: a < b < c es un error sintáctico.
relacion
    : suma (operadorRelacional suma)?
    ;

operadorRelacional
    : IGUAL_IGUAL
    | DISTINTO
    | MENOR
    | MAYOR
    | MENOR_IGUAL
    | MAYOR_IGUAL
    ;

suma
    : producto ((MAS | MENOS) producto)*
    ;

producto
    : unaria ((POR | DIV) unaria)*
    ;

unaria
    : (MAS | MENOS) unaria
    | primaria
    ;

primaria
    : ID
    | NUMERO
    | CADENA
    | TRUE
    | FALSE
    | llamadaFuncion
    | PAREN_ABRE expresion PAREN_CIERRA
    ;

// Palabras reservadas; se distingue entre mayúsculas y minúsculas.
SISTEMA          : 'SISTEMA';
FIN_SISTEMA      : 'FIN_SISTEMA';
NIVEL            : 'NIVEL';
ENTIDAD          : 'entidad';
PROCESO          : 'proceso';
ALMACEN          : 'almacen';
FLUJO            : 'flujo';
CONECTAR         : 'conectar';
CON_DATO         : 'con_dato';
SI               : 'si';
SINO             : 'sino';
MIENTRAS         : 'mientras';
ENTERO           : 'entero';
BOOLEANO         : 'booleano';
REAL             : 'real';
TRUE             : 'true';
FALSE            : 'false';
NOT              : 'not';
IMPRIMIR         : 'imprimir';
VALIDAR_MODELO   : 'validar_modelo';
CONTAR_NODOS     : 'contar_nodos';

// Operadores.
FLECHA           : '->';
IGUAL_IGUAL      : '==';
DISTINTO         : '!=';
MENOR_IGUAL      : '<=';
MAYOR_IGUAL      : '>=';
AND              : '&&';
OR               : '||';
ASIGNAR          : '=';
MAS              : '+';
MENOS            : '-';
POR              : '*';
DIV              : '/';
MENOR            : '<';
MAYOR            : '>';

// Delimitadores. La coma no pertenece al vocabulario corregido.
PUNTO_Y_COMA     : ';';
LLAVE_ABRE       : '{';
LLAVE_CIERRA     : '}';
PAREN_ABRE       : '(';
PAREN_CIERRA     : ')';

// El signo se reconoce por separado; las cadenas no tienen escapes.
CADENA           : '"' ~["\r\n]* '"';
CADENA_SIN_CIERRE : '"' ~["\r\n]*;
NUMERO           : [0-9]+ ('.' [0-9]+)?;
ID               : [a-zA-Z] [a-zA-Z0-9_]*;

ESPACIOS         : [ \t\r\n]+ -> skip;
ERROR            : .;
