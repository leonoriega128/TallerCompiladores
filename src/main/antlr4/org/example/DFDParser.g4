parser grammar DFDParser;

options { tokenVocab=DFDLexer; }

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

expresion
    : disyuncion
    ;

disyuncion
    : conjuncion (OR conjuncion)*
    ;

conjuncion
    : negacion (AND negacion)*
    ;

negacion
    : NOT negacion
    | relacion
    ;

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
