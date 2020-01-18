package sqlg2;

import org.antlr.v4.runtime.CharStreams;
import org.antlr.v4.runtime.Token;
import sqlg2.lexer.Java8Lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

class ParserBase {

    static final String PREPROCESSOR_LINE = "/* PREPROCESSOR GENERATED CODE - DO NOT REMOVE THIS LINE */";

    protected final String text;
    private final Java8Lexer lexer;
    private Token token;

    protected ParserBase(String text) throws IOException {
        this.text = preprocessFile(text);
        lexer = new Java8Lexer(CharStreams.fromString(this.text));
        token = lexer.nextToken();
    }

    protected final boolean eof() {
        return token.getType() == Java8Lexer.EOF;
    }

    protected final Token get() {
        return token;
    }

    protected final void next() {
        if (!eof()) {
            token = lexer.nextToken();
        }
    }

    private static String preprocessFile(String text) throws IOException {
        BufferedReader rdr = new BufferedReader(new StringReader(text));
        StringBuilder buf = new StringBuilder();
        boolean skipping = false;
        while (true) {
            String s = rdr.readLine();
            if (s == null)
                break;
            if (s.contains(PREPROCESSOR_LINE)) {
                while (buf.charAt(buf.length() - 1) <= ' ')
                    buf.deleteCharAt(buf.length() - 1);
                buf.append('\n');
                skipping = !skipping;
            } else {
                if (!skipping)
                    buf.append(s).append('\n');
            }
        }
        rdr.close();
        return buf.toString();
    }
}
