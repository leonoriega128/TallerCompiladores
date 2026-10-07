lexer grammar DFDLexer;

// Palabras reservadas del lenguaje DFD.
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
TRUE             : 'true';
FALSE            : 'false';
IMPRIMIR         : 'imprimir';
VALIDAR_MODELO   : 'validar_modelo';
CONTAR_NODOS     : 'contar_nodos';

// Operadores: los de dos caracteres se declaran explícitamente.
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

// Delimitadores.
PUNTO_Y_COMA     : ';';
LLAVE_ABRE       : '{';
LLAVE_CIERRA     : '}';
PAREN_ABRE       : '(';
PAREN_CIERRA     : ')';
COMA             : ',';

// Literales y nombres.
CADENA           : '"' ~["\r\n]* '"';
NUMERO           : [0-9]+ ('.' [0-9]+)?;
ID               : [a-zA-Z] [a-zA-Z0-9_]*;

ESPACIOS         : [ \t\r\n]+ -> skip;
ERROR            : .;
