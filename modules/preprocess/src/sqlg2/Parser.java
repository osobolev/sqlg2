package sqlg2;

import org.antlr.v4.runtime.Token;
import sqlg2.lexer.Java8Lexer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.lang.annotation.Annotation;
import java.util.*;

final class Parser extends ParserBase {

    private static final String SQL_ANNOTATION = annotationName(Sql.class);
    private static final String QUERY_ANNOTATION = annotationName(Query.class);
    private static final String STATEMENT_ANNOTATION = annotationName(Prepare.class);
    private static final String KEY_STATEMENT_ANNOTATION = annotationName(PrepareKey.class);
    private static final String CALL_ANNOTATION = annotationName(Call.class);
    private static final String BUSINESS_ANNOTATION = annotationName(Business.class);
    private static final String BUSINESS_NOTEST_ANNOTATION = annotationName(BusinessNoSql.class);
    private static final String CHECK_PARAMS_ANNOTATION = annotationName(CheckParams.class);
    private static final String ROWTYPE_ANNOTATION = annotationName(RowType.class);
    static final String EDITABLE_ROWTYPE_ANNOTATION = annotationName(EditableRowType.class);
    private static final String SQLG_ANNOTATION = annotationName(SQLG.class);

    private static final int[] MODIFIERS = {
        Java8Lexer.PUBLIC, Java8Lexer.PROTECTED, Java8Lexer.PRIVATE,
        Java8Lexer.STATIC, Java8Lexer.ABSTRACT, Java8Lexer.FINAL,
        Java8Lexer.NATIVE, Java8Lexer.SYNCHRONIZED, Java8Lexer.TRANSIENT,
        Java8Lexer.VOLATILE, Java8Lexer.STRICTFP
    };

    private final List<CutPaste> fragments = new ArrayList<>();
    private final List<Entry> entries = new ArrayList<>();
    private final List<String> imports = new ArrayList<>();
    private AfterCutPaste after = null;
    private boolean needsProcessing = false;
    private final Map<String, List<ParamCutPaste>> bindMap = new HashMap<>();
    private final Map<String, RowTypeCutPaste> rowTypeMap;
    private final List<String> parameters = new ArrayList<>();
    private final String className;

    Parser(String text, String className,
           Map<String, RowTypeCutPaste> rowTypeMap) throws IOException {
        super(text);
        this.className = className;
        this.rowTypeMap = rowTypeMap;
    }

    private static String annotationName(Class<? extends Annotation> cls) {
        String name = cls.getName();
        int p = name.lastIndexOf('.');
        if (p < 0) {
            return name;
        } else {
            return name.substring(p + 1);
        }
    }

    private static String extractQuery(String comment) {
        comment = comment.substring(2, comment.length() - 2);
        BufferedReader rdr = new BufferedReader(new StringReader(comment));
        StringBuilder buf = new StringBuilder();
        while (true) {
            String s;
            try {
                s = rdr.readLine();
            } catch (IOException ex) {
                break;
            }
            if (s == null)
                break;
            s = s.trim();
            while (s.startsWith("*"))
                s = s.substring(1).trim();
            if (s.length() > 0) {
                if (buf.length() > 0)
                    buf.append('\n');
                buf.append(s);
            }
        }
        try {
            rdr.close();
        } catch (IOException ex) {
            // ignore
        }
        return buf.toString();
    }

    private Entry parseMethodHeader(String javadoc, String annotation) throws ParseException {
        int headerFrom = get().getStartIndex();
        int headerTo = -1;
        List<String> params = new ArrayList<>();
        int typeFrom = -1;
        int typeTo = -1;
        String lastIdent = null;
        String entryName = null;
        boolean wasParen = false;
        boolean parenClosed = false;
        int parenCount = 0;
        int genCount = 0;
        while (!eof()) {
            Token t = get();
            int id = t.getType();
            if (id == Java8Lexer.LBRACE) {
                headerTo = get().getStartIndex();
                next();
                break;
            } else if (id == Java8Lexer.LPAREN) {
                if (wasParen) {
                    parenCount++;
                } else {
                    wasParen = true;
                    lastIdent = null;
                }
            } else if (id == Java8Lexer.LT) {
                if (wasParen) {
                    genCount++;
                }
            } else if (id == Java8Lexer.COMMA) {
                if (wasParen && !parenClosed) {
                    if (parenCount <= 0 && genCount <= 0) {
                        if (lastIdent != null) {
                            params.add(lastIdent);
                        }
                    }
                }
            } else if (id == Java8Lexer.RPAREN) {
                if (wasParen && !parenClosed) {
                    if (parenCount <= 0) {
                        if (lastIdent != null) {
                            params.add(lastIdent);
                        }
                        parenClosed = true;
                    } else {
                        parenCount--;
                    }
                }
            } else if (id == Java8Lexer.GT) {
                if (wasParen && !parenClosed) {
                    if (genCount > 0) {
                        genCount--;
                    }
                }
            } else if (id == Java8Lexer.Identifier) {
                lastIdent = t.getText();
                if (!wasParen) {
                    entryName = lastIdent;
                    typeTo = t.getStartIndex();
                    if (typeFrom < 0) {
                        typeFrom = t.getStartIndex();
                    }
                }
            } else if (id == Java8Lexer.SEMI) {
                throw new ParseException("Unexpected semicolon", className + (entryName == null ? "" : "." + entryName));
            } else {
                if (!wasParen) {
                    if (typeFrom < 0) {
                        if (!(id == Java8Lexer.WS || id == Java8Lexer.LINE_COMMENT || id == Java8Lexer.COMMENT)) {
                            boolean isModifier = false;
                            for (int m : MODIFIERS) {
                                if (id == m) {
                                    isModifier = true;
                                    break;
                                }
                            }
                            if (!isModifier) {
                                typeFrom = t.getStartIndex();
                            }
                        }
                    }
                }
            }
            next();
        }
        if (entryName == null || headerTo < 0 || typeFrom < 0 || typeTo < 0)
            return null;

        boolean noTest = BUSINESS_NOTEST_ANNOTATION.equals(annotation);
        boolean publish = !CHECK_PARAMS_ANNOTATION.equals(annotation);
        return new Entry(
            javadoc,
            text.substring(headerFrom, headerTo).trim(),
            text.substring(typeFrom, typeTo).trim(),
            entryName,
            params.toArray(new String[0]),
            noTest, publish
        );
    }

    private static final class AssignDescriptor {

        final int from;
        final int to;
        final String varName;
        final String assign;

        AssignDescriptor(int from, int to, String varName, String assign) {
            this.from = from;
            this.to = to;
            this.varName = varName;
            this.assign = assign;
        }
    }

    private AssignDescriptor parseAssign() {
        int from = -1;
        int to = -1;
        int identCount = 0;
        String assign = "";
        String varName = null;
        while (!eof()) {
            Token t = get();
            if (t.getType() == Java8Lexer.ASSIGN) {
                next();
                if (!eof()) {
                    from = get().getStartIndex();
                }
                break;
            } else if (t.getType() == Java8Lexer.SEMI) {
                from = to = t.getStartIndex();
                assign = " =";
                break;
            } else if (t.getType() == Java8Lexer.Identifier) {
                identCount++;
                if (identCount == 2) {
                    varName = t.getText();
                }
            }
            next();
        }
        if (to < 0) {
            while (!eof()) {
                Token t = get();
                if (t.getType() == Java8Lexer.SEMI) {
                    to = t.getStartIndex();
                    next();
                    break;
                }
                next();
            }
        }
        return new AssignDescriptor(from, to, varName, assign);
    }

    private AssignDescriptor parseStatement(String entryName, String lastSqlVar, String lastSqlQuery,
                                            boolean allowOutParams, String whatToCall, String addParameter, boolean onlySql) throws ParseException {
        if (lastSqlVar == null && lastSqlQuery == null) {
            return null;
        }
        AssignDescriptor desc = parseAssign();
        if (!(desc.from >= 0 && desc.to >= 0)) {
            return null;
        }
        String pred;
        if (onlySql) {
            pred = desc.assign + " ";
        } else {
            pred = desc.assign + " " + whatToCall + "(" + addParameter;
        }
        if (lastSqlVar != null) {
            fragments.add(new SimpleCutPaste(desc.from, desc.to, pred + lastSqlVar + (onlySql ? "" : ")")));
        } else {
            String location = className + (entryName == null ? "" : "." + entryName);
            QPParser appender = new QPParser(location, allowOutParams, pred, onlySql, parameters, bindMap);
            BindVarCutPaste cp = appender.getStatementCutPaste(desc.from, desc.to, lastSqlQuery);
            fragments.add(cp);
        }
        return desc;
    }

    private void parseMethodBody(String entryName) throws ParseException {
        int count = 1;
        String lastSqlQuery = null;
        int lastSqlQueryCount = -1;
        String lastSqlVar = null;
        int lastSqlVarCount = -1;
        while (!eof()) {
            Token t = get();
            int id = t.getType();
            if (id == Java8Lexer.LBRACE) {
                count++;
            } else if (id == Java8Lexer.RBRACE) {
                count--;
                if (lastSqlQueryCount >= 0 && count < lastSqlQueryCount) {
                    lastSqlQuery = null;
                    lastSqlQueryCount = -1;
                }
                if (lastSqlVarCount >= 0 && count < lastSqlVarCount) {
                    lastSqlVar = null;
                    lastSqlVarCount = -1;
                }
                if (count <= 0) {
                    next();
                    break;
                }
            } else if (id == Java8Lexer.COMMENT) {
                lastSqlQuery = extractQuery(t.getText());
                lastSqlQueryCount = count;
                lastSqlVar = null;
                lastSqlVarCount = -1;
            } else if (id == Java8Lexer.AT) {
                next();
                if (eof())
                    break;
                t = get();
                if (t.getType() == Java8Lexer.Identifier && SQL_ANNOTATION.equals(t.getText())) {
                    if (lastSqlQuery != null) {
                        next();
                        AssignDescriptor desc = parseStatement(entryName, null, lastSqlQuery, true, null, "", true);
                        if (desc != null) {
                            lastSqlVar = desc.varName;
                            lastSqlVarCount = count;
                        }
                    }
                } else if (t.getType() == Java8Lexer.Identifier && QUERY_ANNOTATION.equals(t.getText())) {
                    next();
                    parseStatement(entryName, lastSqlVar, lastSqlQuery, false, "createQueryPiece", "", false);
                } else if (t.getType() == Java8Lexer.Identifier && STATEMENT_ANNOTATION.equals(t.getText())) {
                    next();
                    parseStatement(entryName, lastSqlVar, lastSqlQuery, false, "prepareStatement", "", false);
                } else if (t.getType() == Java8Lexer.Identifier && KEY_STATEMENT_ANNOTATION.equals(t.getText())) {
                    next();
                    String auto = null;
                    if (!eof()) {
                        t = get();
                        if (t.getType() == Java8Lexer.LPAREN) {
                            next();
                            if (!eof()) {
                                t = get();
                                if (t.getType() == Java8Lexer.StringLiteral) {
                                    auto = t.getText().substring(1, t.getText().length() - 1);
                                    next();
                                }
                            }
                            while (!eof()) {
                                if (get().getType() == Java8Lexer.RPAREN) {
                                    next();
                                    break;
                                }
                                next();
                            }
                        }
                    }
                    String autoKeys;
                    if (auto != null) {
                        StringBuilder buf = new StringBuilder();
                        StringTokenizer tok = new StringTokenizer(auto, ",");
                        while (tok.hasMoreTokens()) {
                            String col = tok.nextToken();
                            if (buf.length() > 0) {
                                buf.append(", ");
                            }
                            buf.append("\"" + col + "\"");
                        }
                        autoKeys = "new String[] {" + buf + "}";
                    } else {
                        autoKeys = "ALL_KEYS";
                    }
                    parseStatement(entryName, lastSqlVar, lastSqlQuery, false, "prepareStatementKey", autoKeys + ", ", false);
                } else if (t.getType() == Java8Lexer.Identifier && CALL_ANNOTATION.equals(t.getText())) {
                    next();
                    parseStatement(entryName, lastSqlVar, lastSqlQuery, true, "executeCall", "", false);
                }
                continue;
            }
            next();
        }
    }

    private RowTypeCutPaste parseClass(boolean editable) {
        String className = null;
        boolean justClass = false;
        boolean isInterface = false;
        int from = -1;
        while (!eof()) {
            Token t = get();
            int id = t.getType();
            if (id == Java8Lexer.CLASS || id == Java8Lexer.INTERFACE) {
                justClass = true;
                isInterface = id == Java8Lexer.INTERFACE;
            } else if (id == Java8Lexer.LBRACE) {
                next();
                if (!eof()) {
                    from = get().getStartIndex();
                }
                break;
            } else if (id == Java8Lexer.Identifier) {
                if (justClass) {
                    className = t.getText();
                }
                justClass = false;
            }
            next();
        }
        int count = 1;
        int to = -1;
        while (!eof()) {
            Token t = get();
            if (t.getType() == Java8Lexer.LBRACE) {
                count++;
            } else if (t.getType() == Java8Lexer.RBRACE) {
                count--;
                if (count <= 0) {
                    to = t.getStartIndex();
                    next();
                    break;
                }
            }
            next();
        }
        if (from >= 0 && to >= 0) {
            RowTypeCutPaste rowType = new RowTypeCutPaste(from, to, className, isInterface, editable);
            fragments.add(rowType);
            return rowType;
        }
        return null;
    }

    private void parseHeader() {
        boolean wasClass = false;
        while (!eof()) {
            Token t = get();
            int id = t.getType();
            if (id == Java8Lexer.LBRACE) {
                if (wasClass) {
                    next();
                    break;
                }
            } else if (id == Java8Lexer.AT) {
                next();
                if (eof())
                    break;
                t = get();
                if (t.getType() == Java8Lexer.Identifier && SQLG_ANNOTATION.equals(t.getText())) {
                    needsProcessing = true;
                   next();
                }
                continue;
            } else if (id == Java8Lexer.IMPORT) {
                next();
                StringBuilder buf = new StringBuilder();
                while (!eof()) {
                    t = get();
                    id = t.getType();
                    if (id == Java8Lexer.SEMI) {
                        next();
                        break;
                    }
                    buf.append(t.getText());
                    next();
                }
                imports.add(buf.toString().trim());
                continue;
            } else if (id == Java8Lexer.CLASS) {
                wasClass = true;
            }
            next();
        }
    }

    void parseAll() throws ParseException {
        parseHeader();
        if (!needsProcessing)
            return;
        int lastCurly = -1;
        String lastComment = null;
        String lastIdent = null;
        String identBeforeParen = null;
        while (!eof()) {
            Token t = get();
            int id = t.getType();
            if (id == Java8Lexer.AT) {
                next();
                if (eof())
                    break;
                t = get();
                if (t.getType() == Java8Lexer.Identifier) {
                    if (ROWTYPE_ANNOTATION.equals(t.getText()) || EDITABLE_ROWTYPE_ANNOTATION.equals(t.getText())) {
                        next();
                        if (!eof()) {
                            RowTypeCutPaste rowType = parseClass(EDITABLE_ROWTYPE_ANNOTATION.equals(t.getText()));
                            rowTypeMap.put(className + "." + rowType.className, rowType);
                        }
                    } else if (BUSINESS_ANNOTATION.equals(t.getText()) || BUSINESS_NOTEST_ANNOTATION.equals(t.getText()) || CHECK_PARAMS_ANNOTATION.equals(t.getText())) {
                        next();
                        if (!eof()) {
                            Entry entry = parseMethodHeader(lastComment, t.getText());
                            parseMethodBody(entry == null ? identBeforeParen : entry.methodToCall);
                            if (entry != null) {
                                entries.add(entry);
                            }
                        }
                    }
                }
                lastComment = null;
                identBeforeParen = null;
                continue;
            } else if (id == Java8Lexer.RBRACE) {
                lastCurly = t.getStartIndex();
                next();
                break;
            } else if (id == Java8Lexer.LBRACE) {
                next();
                parseMethodBody(identBeforeParen);
                lastComment = null;
                identBeforeParen = null;
                continue;
            } else if (id == Java8Lexer.LPAREN) {
                identBeforeParen = lastIdent;
            } else if (id == Java8Lexer.Identifier) {
                lastIdent = t.getText();
            } else if (id == Java8Lexer.COMMENT) {
                lastComment = t.getText();
            }
            next();
        }
        if (lastCurly >= 0) {
            after = new AfterCutPaste(lastCurly, lastCurly);
            fragments.add(after);
        }
    }

    String doCutPaste() {
        StringBuilder buf = new StringBuilder(text);
        for (int i = fragments.size() - 1; i >= 0; i--) {
            CutPaste cp = fragments.get(i);
            cp.cutPaste(buf);
        }
        return buf.toString();
    }

    List<Entry> getEntries() {
        return entries;
    }

    boolean isNeedsProcessing() {
        return needsProcessing;
    }

    AfterCutPaste getAfter() {
        return after;
    }

    String[] getImports() {
        return imports.toArray(new String[0]);
    }

    Map<String, List<ParamCutPaste>> getBindMap() {
        return bindMap;
    }

    List<String> getParameters() {
        return parameters;
    }
}
