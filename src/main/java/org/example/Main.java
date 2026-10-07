package org.example;

import org.antlr.v4.gui.TreeViewer;
import org.antlr.v4.runtime.BaseErrorListener;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.RecognitionException;
import org.antlr.v4.runtime.Recognizer;
import org.antlr.v4.runtime.Token;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.GraphicsEnvironment;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Arrays;

/** Analiza un programa DFD y muestra su árbol, siguiendo el ejemplo de la cátedra. */
public final class Main {
    private static final String ARCHIVO_PREDETERMINADO = "src/test/resources/programa_informe1.dfd";

    private Main() {
    }

    public static void main(String[] args) {
        int codigo = analizar(args);
        if (codigo != 0) {
            System.exit(codigo);
        }
    }

    private static int analizar(String[] args) {
        boolean sinGui = false;
        String archivo = null;
        for (String arg : args) {
            if (arg.equals("--sin-gui")) {
                sinGui = true;
            } else if (arg.startsWith("--") || archivo != null) {
                System.err.println("Uso: java org.example.Main [--sin-gui] [archivo.dfd]");
                return 2;
            } else {
                archivo = arg;
            }
        }
        if (archivo == null) {
            archivo = ARCHIVO_PREDETERMINADO;
        }

        DFDLexer lexer;
        try {
            lexer = new DFDLexer(CharStreams.fromPath(Path.of(archivo), StandardCharsets.UTF_8));
        } catch (IOException | InvalidPathException e) {
            System.err.println("ERROR_ARCHIVO: no se pudo leer '" + archivo + "': " + e.getMessage());
            return 2;
        }

        System.out.println("ARCHIVO=" + archivo);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        tokens.fill();
        int erroresLexicos = 0;
        for (Token token : tokens.getTokens()) {
            if (token.getType() == DFDLexer.ERROR || token.getType() == DFDLexer.CADENA_SIN_CIERRE) {
                erroresLexicos++;
                String tipo = lexer.getVocabulary().getSymbolicName(token.getType());
                System.err.printf("ERROR_LEXICO linea=%d columna=%d %s: %s%n",
                        token.getLine(), token.getCharPositionInLine(), tipo, escapar(token.getText()));
            }
        }
        if (erroresLexicos != 0) {
            System.out.println("ERRORES_LEXICOS=" + erroresLexicos + " ANALISIS_SINTACTICO=OMITIDO");
            System.out.println("RECHAZADO");
            return 1;
        }

        tokens.seek(0);
        DFDParser parser = new DFDParser(tokens);
        parser.removeErrorListeners();
        parser.addErrorListener(new BaseErrorListener() {
            @Override
            public void syntaxError(Recognizer<?, ?> recognizer, Object offendingSymbol, int line,
                                    int charPositionInLine, String msg, RecognitionException e) {
                System.err.printf("ERROR_SINTACTICO linea=%d columna=%d: %s%n", line, charPositionInLine, msg);
            }
        });
        // ANTLR recupera los errores y sigue analizando; EOF impide aceptar sólo un prefijo.
        DFDParser.ProgramaContext arbol = parser.programa();
        int erroresSintacticos = parser.getNumberOfSyntaxErrors();
        System.out.println("ERRORES_LEXICOS=0 ERRORES_SINTACTICOS=" + erroresSintacticos);
        if (erroresSintacticos != 0) {
            System.out.println("RECHAZADO");
            return 1;
        }

        System.out.println("ACEPTADO");
        System.out.println(arbol.toStringTree(parser));
        if (!sinGui) {
            if (GraphicsEnvironment.isHeadless()) {
                System.out.println("Modo consola: entorno sin pantalla.");
            } else {
                mostrarArbol(archivo, parser, arbol);
            }
        }
        return 0;
    }

    private static void mostrarArbol(String archivo, DFDParser parser, DFDParser.ProgramaContext arbol) {
        SwingUtilities.invokeLater(() -> {
            TreeViewer visor = new TreeViewer(Arrays.asList(parser.getRuleNames()), arbol);
            visor.setScale(2);

            JFrame ventana = new JFrame("Árbol sintáctico DFD - " + archivo);
            ventana.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            ventana.setSize(1500, 1000);
            ventana.add(new JScrollPane(visor));
            ventana.setLocationRelativeTo(null);
            ventana.setVisible(true);
        });
    }

    private static String escapar(String texto) {
        return texto.replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
