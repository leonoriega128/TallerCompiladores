package org.example;

import org.antlr.v4.runtime.CharStream;
import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.Token;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/** Ejecuta el analizador léxico y muestra la ubicación de cada token. */
public final class MostrarTokens {

    private MostrarTokens() {
    }

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            System.err.println("Uso: java org.example.MostrarTokens <archivo.dfd>");
            System.exit(2);
        }

        for (String line : tokenLines(Path.of(args[0]))) {
            System.out.println(line);
        }
    }

    static List<String> tokenLines(String input) {
        DFDLexer lexer = new DFDLexer(CharStreams.fromString(input));
        return tokenLines(lexer);
    }

    private static List<String> tokenLines(Path input) throws IOException {
        CharStream chars = CharStreams.fromPath(input, StandardCharsets.UTF_8);
        return tokenLines(new DFDLexer(chars));
    }

    private static List<String> tokenLines(DFDLexer lexer) {
        CommonTokenStream stream = new CommonTokenStream(lexer);
        stream.fill();
        List<String> lines = new ArrayList<>();
        for (Token token : stream.getTokens()) {
            if (token.getType() == Token.EOF) {
                continue;
            }
            String type = lexer.getVocabulary().getSymbolicName(token.getType());
            if (type == null) {
                type = lexer.getVocabulary().getDisplayName(token.getType());
            }
            lines.add(type + " | " + escape(token.getText()) + " | "
                    + token.getLine() + " | " + token.getCharPositionInLine());
        }
        return lines;
    }

    private static String escape(String text) {
        return text.replace("\\", "\\\\")
                .replace("\r", "\\r")
                .replace("\n", "\\n")
                .replace("\t", "\\t");
    }
}
