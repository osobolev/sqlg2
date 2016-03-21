package sqlg2;

import antlr.Token;
import antlr.TokenStreamException;
import sqlg2.lexer.JavaLexer;
import sqlg2.lexer.JavaTokenTypes;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

class ParserBase {

    static final String PREPROCESSOR_LINE = "/* PREPROCESSOR GENERATED CODE - DO NOT REMOVE THIS LINE */";

    protected final String text;
    private final JavaLexer lexer;
    private Token token = null;

    protected ParserBase(String text) throws TokenStreamException, IOException {
        this.text = preprocessFile(text);
        lexer = new JavaLexer(new StringReader(this.text));
        lexer.setTabSize(1);
        token = lexer.nextToken();
    }

    protected final boolean eof() {
        return token.getType() == JavaTokenTypes.EOF;
    }

    protected final Token get() {
        return token;
    }

    protected final void next() throws TokenStreamException {
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
