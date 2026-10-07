package org.example;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;

import javax.imageio.ImageIO;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;

/** Pruebas reproducibles sin ventanas ni dependencias adicionales de testing. */
public final class VerificarAnalizador {
    private static int ejecutadas;
    private static int fallidas;

    private VerificarAnalizador() {
    }

    public static void main(String[] args) throws Exception {
        Path temporales = Files.createTempDirectory(Path.of("target"), "pruebas-dfd-");
        try {
            probar("coma_fuera_del_vocabulario", () -> comprobar(
                    MostrarTokens.tokenLines(",").equals(List.of("ERROR | , | 1 | 0")),
                    "La coma debe producir ERROR, según el informe corregido."));
            probar("reservadas_y_prefijos", () -> comprobar(
                    MostrarTokens.tokenLines("real real1 not not1").equals(List.of(
                            "REAL | real | 1 | 0", "ID | real1 | 1 | 5",
                            "NOT | not | 1 | 11", "ID | not1 | 1 | 15")),
                    "real/not son reservadas; sus prefijos no reservan identificadores."));
            probar("numeros_y_signos", () -> comprobar(
                    MostrarTokens.tokenLines("-3.5 +2 12.").equals(List.of(
                            "MENOS | - | 1 | 0", "NUMERO | 3.5 | 1 | 1",
                            "MAS | + | 1 | 5", "NUMERO | 2 | 1 | 6",
                            "NUMERO | 12 | 1 | 8", "ERROR | . | 1 | 10")),
                    "Los signos son tokens separados y un punto incompleto es ERROR."));
            probar("cadenas_y_cierre", () -> comprobar(
                    MostrarTokens.tokenLines("\"\" \"Dato @#\"\n\"sin cierre").equals(List.of(
                            "CADENA | \"\" | 1 | 0", "CADENA | \"Dato @#\" | 1 | 3",
                            "CADENA_SIN_CIERRE | \"sin cierre | 2 | 0")),
                    "Las cadenas vacías/especiales son válidas y el cierre faltante conserva su posición."));
            probar("tokens_del_programa_original", () -> {
                String fuente = Files.readString(Path.of("src/test/resources/programa_informe1.dfd"));
                String evidencia = Files.readString(Path.of("evidencia/salidas/programa_informe1.txt"),
                        StandardCharsets.UTF_16LE).replace("\uFEFF", "");
                comprobar(MostrarTokens.tokenLines(fuente).equals(evidencia.lines().toList()),
                        "La unificación debe conservar los tokens, lexemas y posiciones originales.");
            });
            probar("precedencia_aritmetica", () -> {
                DFDParser.ExpresionContext expresion = expresion("2 + 3 * 4");
                DFDParser.SumaContext suma = expresion.disyuncion().conjuncion(0)
                        .negacion(0).relacion().suma(0);
                comprobar(suma.producto().size() == 2
                                && suma.producto(0).getText().equals("2")
                                && suma.producto(1).getText().equals("3*4"),
                        "El producto 3*4 debe pertenecer al segundo operando de la suma.");
            });
            probar("precedencia_logica", () -> {
                DFDParser.DisyuncionContext disyuncion = expresion("not x < 10 && true || false")
                        .disyuncion();
                DFDParser.ConjuncionContext conjuncion = disyuncion.conjuncion(0);
                comprobar(disyuncion.conjuncion().size() == 2
                                && conjuncion.negacion().size() == 2
                                && conjuncion.negacion(0).NOT() != null
                                && conjuncion.negacion(0).negacion().relacion().getText().equals("x<10"),
                        "La agrupación debe ser ((not (x<10)) && true) || false.");
            });

            probar("programa_original", () -> verificarSalida(ejecutarMain(
                    "--sin-gui", "src/test/resources/programa_informe1.dfd"), 0, null));
            probar("constructos_y_precedencia", () -> verificarSalida(ejecutarMain(
                    "--sin-gui", "src/test/resources/casos/sintaxis_precedencia_valida.dfd"), 0, null));
            probar("archivo_predeterminado", () -> verificarSalida(ejecutarMain("--sin-gui"), 0, null));
            probar("consola_automatica_sin_pantalla", () -> verificarSalida(ejecutarMain(), 0, null));
            probar("ruta_absoluta", () -> verificarSalida(ejecutarMain("--sin-gui",
                    Path.of("src/test/resources/programa_informe1.dfd").toAbsolutePath().toString()), 0, null));
            probar("comparacion_encadenada", () -> verificarSalida(ejecutarMain("--sin-gui",
                    "src/test/resources/casos/sintaxis_comparacion_encadenada.dfd"), 1, "ERROR_SINTACTICO"));
            probar("punto_y_coma_faltante", () -> {
                Salida salida = ejecutarMain("--sin-gui",
                        "src/test/resources/casos/sintaxis_punto_y_coma_faltante.dfd");
                verificarSalida(salida, 1, "ERROR_SINTACTICO");
                comprobar(salida.texto.contains("linea=4 columna=0"), "Se perdió la posición del error.");
            });

            List<Caso> casos = List.of(
                    new Caso("niveles_y_bloques_anidados", "SISTEMA Prueba;\nNIVEL 0 {\n"
                            + "entidad cli = \"Cliente\"; proceso p = \"Gestionar\"; almacen bd = \"Datos\";\n"
                            + "flujo f = conectar(p -> bd) con_dato \"Registro\";\n"
                            + "entero x = 0; si (true) { mientras (x < 2) { x = x + 1; } }\n"
                            + "sino { imprimir(\"Sin datos\"); } validar_modelo(0);\n"
                            + "} NIVEL 1 {} FIN_SISTEMA", 0, null),
                    new Caso("todos_los_operadores", programa("real r = -(2 + 3.5) / +2 - 1 * 4;\n"
                            + "booleano b = (1 == 1) && (2 != 3) && (1 <= 2) && (3 >= 2)\n"
                            + "|| (2 > 1) && not (1 < 0); imprimir(contar_nodos(entidad));"), 0, null),
                    new Caso("sistema_sin_niveles", "SISTEMA Vacio; FIN_SISTEMA", 0, null),
                    new Caso("tipos_reservados_para_semantica", programa("entero x = true + 1;"), 0, null),
                    new Caso("llave_faltante", "SISTEMA P; NIVEL 0 { entero x = 1; FIN_SISTEMA", 1, "ERROR_SINTACTICO"),
                    new Caso("parentesis_faltante", programa("imprimir(1;"), 1, "ERROR_SINTACTICO"),
                    new Caso("fin_sistema_faltante", "SISTEMA P; NIVEL 0 {}", 1, "ERROR_SINTACTICO"),
                    new Caso("contenido_posterior", "SISTEMA P; FIN_SISTEMA entero x = 1;", 1, "ERROR_SINTACTICO"),
                    new Caso("archivo_vacio", "", 1, "ERROR_SINTACTICO"),
                    new Caso("flujo_mal_formado", programa("flujo f = conectar(a b) con_dato \"Dato\";"), 1, "ERROR_SINTACTICO"),
                    new Caso("caracter_invalido", programa("  @"), 1, "ERROR_LEXICO"),
                    new Caso("coma_invalida", programa("imprimir(1, 2);"), 1, "ERROR_LEXICO"),
                    new Caso("cadena_sin_cierre", programa("imprimir(\"sin cierre"), 1, "ERROR_LEXICO"),
                    new Caso("recuperacion_sintactica", programa("entero x = ;\nentero y = ;"), 1, "ERROR_SINTACTICO")
            );
            for (Caso caso : casos) {
                Path archivo = temporales.resolve(caso.nombre + ".dfd");
                Files.writeString(archivo, caso.fuente, StandardCharsets.UTF_8);
                probar(caso.nombre, () -> {
                    Salida salida = ejecutarMain("--sin-gui", archivo.toString());
                    verificarSalida(salida, caso.codigo, caso.diagnostico);
                    if ("ERROR_LEXICO".equals(caso.diagnostico)) {
                        comprobar(salida.texto.contains("ANALISIS_SINTACTICO=OMITIDO")
                                        && !salida.texto.contains("ERROR_SINTACTICO"),
                                "Un error léxico debe impedir el análisis sintáctico.");
                    }
                    if (caso.nombre.equals("caracter_invalido")) {
                        comprobar(salida.texto.contains("linea=3 columna=2"), "Posición léxica incorrecta.");
                    }
                    if (caso.nombre.equals("recuperacion_sintactica")) {
                        comprobar(salida.texto.lines().filter(l -> l.startsWith("ERROR_SINTACTICO")).count() >= 2,
                                "El parser debe continuar después del primer error.");
                    }
                });
            }
            probar("archivo_inexistente", () -> verificarSalida(ejecutarMain(
                    "--sin-gui", temporales.resolve("no-existe.dfd").toString()), 2, "ERROR_ARCHIVO"));
            probar("opcion_desconocida", () -> verificarSalida(ejecutarMain("--desconocida"), 2, "Uso:"));
            probar("demasiados_archivos", () -> verificarSalida(ejecutarMain(
                    "--sin-gui", "uno.dfd", "dos.dfd"), 2, "Uso:"));
            probar("visor_del_arbol_sin_pantalla", () -> {
                DFDParser parser = new DFDParser(new CommonTokenStream(new DFDLexer(CharStreams.fromString(
                        Files.readString(Path.of("src/test/resources/programa_informe1.dfd"))))));
                DFDParser.ProgramaContext arbol = parser.programa();
                SwingUtilities.invokeAndWait(() -> {
                    TreeViewer visor = new TreeViewer(Arrays.asList(parser.getRuleNames()), arbol);
                    visor.setScale(2);
                    Dimension dimension = visor.getPreferredSize();
                    comprobar(dimension.width > 0 && dimension.height > 0, "El visor no tiene contenido.");
                    visor.setSize(dimension);
                    BufferedImage imagen = new BufferedImage(dimension.width, dimension.height,
                            BufferedImage.TYPE_INT_RGB);
                    Graphics2D graphics = imagen.createGraphics();
                    graphics.setColor(Color.WHITE);
                    graphics.fillRect(0, 0, dimension.width, dimension.height);
                    graphics.setColor(Color.BLACK);
                    visor.paint(graphics);
                    graphics.dispose();
                    try {
                        ImageIO.write(imagen, "png", Path.of("target/arbol-programa-informe1.png").toFile());
                    } catch (java.io.IOException e) {
                        throw new java.io.UncheckedIOException(e);
                    }
                });
            });
        } finally {
            try (var archivos = Files.list(temporales)) {
                for (Path archivo : archivos.toList()) {
                    Files.delete(archivo);
                }
            }
            Files.delete(temporales);
        }
        System.out.printf("PRUEBAS=%d CORRECTAS=%d FALLIDAS=%d%n", ejecutadas, ejecutadas - fallidas, fallidas);
        if (fallidas != 0) {
            System.exit(1);
        }
    }

    private static String programa(String sentencias) {
        return "SISTEMA Prueba;\nNIVEL 0 {\n" + sentencias + "\n}\nFIN_SISTEMA";
    }

    private static DFDParser.ExpresionContext expresion(String fuente) {
        DFDParser parser = new DFDParser(new CommonTokenStream(new DFDLexer(CharStreams.fromString(fuente))));
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                    int charPositionInLine, String msg, RecognitionException e) {
                throw new AssertionError(msg);
            }
        });
        return parser.expresion();
    }

    private static Salida ejecutarMain(String... argumentos) throws Exception {
        List<String> comando = new ArrayList<>(List.of(
                Path.of(System.getProperty("java.home"), "bin", "java").toString(),
                "-Djava.awt.headless=true", "-Dfile.encoding=UTF-8", "-cp",
                System.getProperty("java.class.path"), "org.example.Main"));
        comando.addAll(List.of(argumentos));
        Process proceso = new ProcessBuilder(comando).redirectErrorStream(true).start();
        var lectura = java.util.concurrent.CompletableFuture.supplyAsync(() -> {
            try {
                return new String(proceso.getInputStream().readAllBytes(), StandardCharsets.UTF_8);
            } catch (java.io.IOException e) {
                throw new java.io.UncheckedIOException(e);
            }
        });
        if (!proceso.waitFor(15, TimeUnit.SECONDS)) {
            proceso.destroyForcibly();
            throw new AssertionError("Main no terminó en modo consola.");
        }
        return new Salida(proceso.exitValue(), lectura.get(5, TimeUnit.SECONDS));
    }

    private static void verificarSalida(Salida salida, int codigo, String diagnostico) {
        comprobar(salida.codigo == codigo, "Código esperado " + codigo + ", recibido "
                + salida.codigo + ":\n" + salida.texto);
        if (codigo == 0) {
            comprobar(salida.texto.contains("ACEPTADO") && salida.texto.contains("ERRORES_SINTACTICOS=0")
                            && salida.texto.contains("ERRORES_LEXICOS=0") && salida.texto.contains("(programa "),
                    "Falta la aceptación, los conteos o el árbol:\n" + salida.texto);
        } else {
            comprobar(salida.texto.contains(diagnostico), "Falta " + diagnostico + ":\n" + salida.texto);
            comprobar(!salida.texto.contains("(programa ") && !salida.texto.contains("ACEPTADO"),
                    "Una entrada rechazada no debe presentarse como programa válido.");
            if (codigo == 1) {
                comprobar(salida.texto.contains("RECHAZADO"), "Falta el rechazo del programa.");
            }
        }
    }

    private static void probar(String nombre, Prueba prueba) {
        ejecutadas++;
        try {
            prueba.ejecutar();
            System.out.println(nombre + ": OK");
        } catch (Exception | AssertionError e) {
            fallidas++;
            System.err.println(nombre + ": FALLÓ — " + e.getMessage());
        }
    }

    private static void comprobar(boolean condicion, String mensaje) {
        if (!condicion) {
            throw new AssertionError(mensaje);
        }
    }

    private interface Prueba {
        void ejecutar() throws Exception;
    }

    private record Salida(int codigo, String texto) {
    }

    private record Caso(String nombre, String fuente, int codigo, String diagnostico) {
    }
}
