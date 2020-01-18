package sqlg2;

import org.antlr.v4.runtime.Token;
import sqlg2.db.JdbcInterface;
import sqlg2.db.SQLGLogger;
import sqlg2.lexer.Java8Lexer;

import java.io.*;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

final class MethodRunner {

    static final String GENERATED_WARNING = "// THIS FILE IS MACHINE-GENERATED, DO NOT EDIT";

    private final GTestImpl test = GTestImpl.INSTANCE;
    private final Class<?> cls;
    private final String className;
    private final String encoding;
    private final String tab;
    private final String separate;
    private final List<Entry> entries;
    private final File srcRoot;
    private final boolean gwt;
    private final SQLGWarn warn;
    private final boolean log;

    private final StringBuilder later = new StringBuilder();
    private final Map<String, ColumnData> generatedIn;
    private final Map<String, ColumnData> generatedOut;
    private final Set<String> generatedMethod = new HashSet<>();

    MethodRunner(Class<?> cls, String className, String encoding, String tab,
                 String separate, List<Entry> entries, File srcRoot, boolean gwt, SQLGWarn warn, boolean log,
                 Map<String, ColumnData> generatedIn, Map<String, ColumnData> generatedOut) {
        this.cls = cls;
        this.className = className;
        this.encoding = encoding;
        this.tab = tab;
        this.separate = separate;
        this.entries = entries;
        this.srcRoot = srcRoot;
        this.gwt = gwt;
        this.warn = warn;
        this.log = log;
        this.generatedIn = generatedIn;
        this.generatedOut = generatedOut;
    }

    private void generateConstructor(StringBuilder buf,
                                     String start, String entryName,
                                     List<ColumnInfo> columns,
                                     String[] types, boolean db) {
        buf.append('\n');
        if (db) {
            buf.append(start).append(tab);
            buf.append("public " + entryName + "(");
            for (int j = 0; j < columns.size(); j++) {
                if (j > 0) {
                    buf.append(", ");
                }
                buf.append(types[j]);
                buf.append(' ');
                buf.append(columns.get(j).name);
            }
            buf.append(") {\n");
            for (ColumnInfo col : columns) {
                String field = col.name;
                buf.append(start).append(tab).append(tab).append("this." + field + " = " + field + ";\n");
            }
        } else {
            buf.append(start).append(tab);
            buf.append("public " + entryName + "() {\n");
        }
        buf.append(start).append(tab).append("}\n");
    }

    private void generateConstructors(StringBuilder buf,
                                      String start, String entryName,
                                      List<ColumnInfo> columns,
                                      String[] types, boolean editable) {
        if (editable) {
            generateConstructor(buf, start, entryName, columns, types, false);
        }
        generateConstructor(buf, start, entryName, columns, types, true);
    }

    private Object[] getTestParams(Method method) throws ParseException {
        Class<?>[] types = method.getParameterTypes();
        Object[] ret = new Object[types.length];
        for (int i = 0; i < types.length; i++) {
            ret[i] = test.getTestObject(types[i]);
            if (ret[i] == null) {
                throw new ParseException("Non-standard type in entry params: " + types[i]);
            }
        }
        return ret;
    }

    private static String getterSetterName(String prefix, String name) {
        return prefix + Character.toUpperCase(name.charAt(0)) + name.substring(1);
    }

    private static String getterName(String name, boolean beansGetter) {
        if (beansGetter) {
            return getterSetterName("get", name);
        } else {
            return name;
        }
    }

    private static String setterName(String name) {
        return getterSetterName("set", name);
    }

    Method[] checkEntries(Map<String, RowTypeCutPaste> rowTypeMap, Map<String, List<ParamCutPaste>> bindMap, List<String> allParameters) throws Exception {
        Method[] methods = cls.getDeclaredMethods();
        GTest.setTest(test);
        Method[] entryMethods = new Method[entries.size()];
        if (log) {
            System.out.println(cls.getCanonicalName());
        }
        test.paramTypeMap.clear();
        test.bindMap = bindMap;
        GBase inst = null;
        for (int i = 0; i < entries.size(); i++) {
            Entry entry = entries.get(i);
            String entryName = className + "." + entry.methodToCall;
            int found = -1;
            for (int j = 0; j < methods.length; j++) {
                Method method = methods[j];
                if (method.getName().equals(entry.methodToCall)) {
                    if (found >= 0) {
                        throw new ParseException("Entry " + entryName + " occured more than once");
                    }
                    found = j;
                }
            }
            if (found < 0)
                throw new ParseException("Entry " + entryName + " not found");
            Method toCall = methods[found];
            int modifiers = toCall.getModifiers();
            if ((modifiers & Modifier.STATIC) != 0) {
                throw new ParseException("Business method " + entryName + " cannot be static");
            }
            if (entry.publish) {
                if ((modifiers & Modifier.PUBLIC) == 0) {
                    throw new ParseException("Business method " + entryName + " should be public");
                }
            } else {
                toCall.setAccessible(true);
            }
            entryMethods[i] = toCall;
            if (entry.noTest)
                continue;
            if (log) {
                System.out.println(toCall.getName());
            }
            test.startCall();
            if (inst == null) {
                Constructor<?> cons = cls.getConstructor(LocalWrapperBase.class);
                JdbcInterface trans = new JdbcInterface(test.connection, test.specific, new SQLGLogger.Simple());
                LocalWrapperBase lwb = new LocalWrapperBase(trans, false);
                inst = (GBase) cons.newInstance(lwb);
            }
            try {
                toCall.invoke(inst, getTestParams(toCall));
            } catch (InvocationTargetException itex) {
                if (itex.getTargetException() instanceof Exception)
                    throw (Exception) itex.getTargetException();
                throw itex;
            } finally {
                inst.closeStatements();
            }
            Class<?> rowTypeClass = test.returnClass;
            RowTypeCutPaste inClass = GTestImpl.isInClass(cls, rowTypeClass, rowTypeMap);
            if (GTestImpl.isOutClass(rowTypeClass)) {
                generateImplOut(entryName, rowTypeClass, test.meta);
            } else if (inClass != null) {
                generateImplIn(inClass, entryName, rowTypeClass, test.meta);
            }
        }
        checkParamTypes(allParameters);

        return entryMethods;
    }

    private void checkCompatibility(Class<?> rowTypeClass, ColumnData cols1, ColumnData cols2) throws ParseException {
        if (cols1.meta != cols2.meta) {
            throw new ParseException(
                "Column meta differs for " + rowTypeClass.getSimpleName() + ": "
                + cols1.entryName + " vs " + cols2.entryName
            );
        }
        if (cols1.columns.size() != cols2.columns.size()) {
            throw new ParseException(
                "Column count differs for " + rowTypeClass.getSimpleName() + ": "
                + cols1.columns.size() + " in " + cols1.entryName + ", "
                + cols2.columns.size() + " in " + cols2.entryName
            );
        }
        for (int i = 0; i < cols1.columns.size(); i++) {
            ColumnInfo col1 = cols1.columns.get(i);
            ColumnInfo col2 = cols2.columns.get(i);
            String error =
                "Different types for " + col1.name
                + " in " + cols1.entryName + " (" + col1.type.getCanonicalName() + ")"
                + " and " + cols2.entryName + " (" + col2.type.getCanonicalName() + ")";
            if (col1.special != col2.special) {
                throw new ParseException(error);
            }
            boolean differentType = !col1.type.getName().equals(col2.type.getName());
            if (col1.special) {
                if (differentType) {
                    throw new ParseException(error);
                }
            } else {
                if (differentType) {
                    switch (warn) {
                    case warn:
                        System.err.println("WARNING: " + error);
                        break;
                    case error:
                        throw new ParseException(error);
                    }
                }
            }
        }
    }

    private void generateImplIn(RowTypeCutPaste rowType, String entryName, Class<?> cls, boolean meta) throws ParseException {
        List<ColumnInfo> columns = test.columns;
        if (columns == null)
            throw new ParseException("Method " + entryName + " should perform SELECT for " + cls.getSimpleName());
        {
            String key = cls.getName();
            ColumnData existingData = generatedIn.get(key);
            ColumnData newData = new ColumnData(entryName, columns, meta);
            if (existingData != null) {
                checkCompatibility(cls, existingData, newData);
                return;
            }
            generatedIn.put(key, newData);
        }
        String[] types = getTestColumns();
        boolean isInterface = rowType.isInterface;
        boolean editable = rowType.editable;
        {
            StringBuilder buf = new StringBuilder();
            String modifiers = isInterface ? "" : "public abstract ";
            buf.append("\n\n");
            for (int j = 0; j < columns.size(); j++) {
                buf.append(tab).append(tab).append(modifiers);
                buf.append(types[j]);
                buf.append(' ');
                buf.append(getterName(columns.get(j).name, false));
                buf.append("();\n");
            }
            if (editable) {
                buf.append('\n');
                for (int j = 0; j < columns.size(); j++) {
                    buf.append(tab).append(tab).append(modifiers).append("void ");
                    buf.append(setterName(columns.get(j).name));
                    buf.append("(");
                    buf.append(types[j]);
                    buf.append(" value);\n");
                }
            }
            buf.append(tab);
            rowType.replaceTo = buf.toString();
        }
        String implName = rowType.className + "Impl";
        {
            later.append("\n").append(tab).append("public static final class ");
            later.append(implName).append(" ").append(isInterface ? "implements" : "extends").append(" ");
            later.append(separate == null ? "" : className + ".");
            later.append(rowType.className);
            if (isInterface) {
                later.append(",");
            } else {
                later.append(" implements");
            }
            later.append(" java.io.Serializable {\n\n");
            generateFields(types, columns, editable);
            generateConstructors(later, tab, implName, columns, types, editable);
            generateGettersSetters(later, tab, false, types, columns, editable);
            later.append(tab).append("}\n");
        }
        generateCreate(cls, implName, columns);
    }

    private void generateFields(String[] types, List<ColumnInfo> columns, boolean editable) {
        for (int j = 0; j < columns.size(); j++) {
            if (editable) {
                later.append(tab).append(tab).append("private ");
            } else {
                later.append(tab).append(tab).append("private final ");
            }
            later.append(types[j]);
            later.append(' ');
            later.append(columns.get(j).name);
            later.append(";\n");
        }
    }

    private void generateGettersSetters(StringBuilder buf, String tab1,
                                        boolean beansGetter, String[] types, List<ColumnInfo> columns, boolean editable) {
        for (int j = 0; j < columns.size(); j++) {
            buf.append('\n');
            buf.append(tab1).append(tab).append("public ");
            buf.append(types[j]);
            buf.append(' ');
            buf.append(getterName(columns.get(j).name, beansGetter));
            buf.append("() {\n");
            buf.append(tab1).append(tab).append(tab);
            buf.append("return ").append(columns.get(j).name).append(";\n");
            buf.append(tab1).append(tab).append("}\n");
        }
        if (editable) {
            for (int j = 0; j < columns.size(); j++) {
                buf.append('\n');
                buf.append(tab1).append(tab).append("public void ");
                buf.append(setterName(columns.get(j).name));
                buf.append("(");
                buf.append(types[j]);
                buf.append(" value) {\n");
                buf.append(tab1).append(tab).append(tab);
                buf.append("this.").append(columns.get(j).name).append(" = value;\n");
                buf.append(tab1).append(tab).append("}\n");
            }
        }
    }

    private String[] getTestColumns() {
        List<ColumnInfo> columns = test.columns;
        String[] types = new String[columns.size()];
        for (int j = 0; j < columns.size(); j++) {
            if (test.meta) {
                types[j] = "sqlg2.db.MetaColumn";
            } else {
                types[j] = test.mapper.getClassName(columns.get(j).type, null);
            }
        }
        return types;
    }

    private void generateImplOut(String entryName, Class<?> cls, boolean meta) throws ParseException, IOException {
        List<ColumnInfo> columns = test.columns;
        if (columns == null)
            throw new ParseException("Method " + entryName + " should perform SELECT for " + cls.getSimpleName());
        String key = cls.getName();
        ColumnData existingData = generatedOut.get(key);
        ColumnData newData = new ColumnData(entryName, columns, meta);
        if (existingData != null) {
            checkCompatibility(cls, existingData, newData);
        } else {
            generatedOut.put(key, newData);

            String[] types = getTestColumns();
            String fullName = cls.getName();
            String packName;
            if (fullName.equals(cls.getSimpleName())) {
                packName = null;
            } else {
                packName = fullName.substring(0, fullName.length() - cls.getSimpleName().length() - 1);
            }
            File dir = Main.packageDir(srcRoot, packName);
            File file = new File(dir, cls.getSimpleName() + ".java");
            String text = Main.readFile(file, encoding);
            ParserBase parser = new ParserBase(text);
            Token start = null;
            Token end = null;
            boolean editable = false;
            while (!parser.eof()) {
                Token token = parser.get();
                int type = token.getType();
                if (type == Java8Lexer.LBRACE) {
                    if (start == null) {
                        start = token;
                    }
                } else if (type == Java8Lexer.RBRACE) {
                    end = token;
                } else if (type == Java8Lexer.AT) {
                    parser.next();
                    if (parser.eof())
                        break;
                    Token ann = parser.get();
                    if (ann.getType() == Java8Lexer.Identifier) {
                        if (Parser.EDITABLE_ROWTYPE_ANNOTATION.equals(ann.getText())) {
                            editable = true;
                        }
                    }
                }
                parser.next();
            }
            if (start == null || end == null)
                return;
            StringBuilder buf = new StringBuilder();
            buf.append("\n\n");
            for (int j = 0; j < columns.size(); j++) {
                if (editable || gwt) {
                    buf.append(tab).append("private ");
                } else {
                    buf.append(tab).append("private final ");
                }
                buf.append(types[j]);
                buf.append(' ');
                buf.append(columns.get(j).name);
                buf.append(";\n");
            }
            generateConstructors(buf, "", cls.getSimpleName(), columns, types, gwt);
            generateGettersSetters(buf, "", true, types, columns, editable);
            CutPaste cp = new SimpleCutPaste(start.getStartIndex() + 1, end.getStartIndex(), buf.toString());
            StringBuilder textBuf = new StringBuilder(parser.text);
            cp.cutPaste(textBuf);
            PrintWriter pw = open(file, encoding);
            pw.print(textBuf);
            pw.close();
        }
        generateCreate(cls, cls.getName(), columns);
    }

    private void generateCreate(Class<?> cls, String className, List<ColumnInfo> columns) {
        String key = cls.getName();
        if (!generatedMethod.contains(key)) {
            generatedMethod.add(key);

            later.append('\n');
            later.append(tab).append("public static ").append(className).append(" create").append(cls.getSimpleName()).append("(java.sql.ResultSet ");
            later.append(GTestImpl.RESULT_SET);
            later.append(", sqlg2.GBase ").append(GTestImpl.BASE).append(") throws java.sql.SQLException {\n");
            later.append(tab).append(tab).append("return new ").append(className).append("(");
            for (int i = 0; i < columns.size(); i++) {
                if (i > 0) {
                    later.append(", ");
                }
                ColumnInfo column = columns.get(i);
                later.append(column.fetchMethod);
            }
            later.append(");\n");
            later.append(tab).append("}\n");
        }
    }

    private void checkParamTypes(List<String> allParameters) throws ParseException {
        Set<String> missingParams = new HashSet<>(allParameters);
        missingParams.removeAll(test.paramTypeMap.keySet());
        if (missingParams.size() > 0) {
            StringBuilder buf = new StringBuilder();
            for (String param : allParameters) {
                if (missingParams.contains(param)) {
                    if (buf.length() > 0) {
                        buf.append(", ");
                    }
                    buf.append(param);
                }
            }
            throw new ParseException("Type is not known for parameters: " + buf);
        }
    }

    static void writeHeader(PrintWriter wr, String pack, String separate) {
        writeHeader(wr, pack, separate, true);
    }

    static PrintWriter open(File file, String encoding) throws IOException {
        OutputStream os = new FileOutputStream(file);
        Writer writer = encoding == null ? new OutputStreamWriter(os) : new OutputStreamWriter(os, encoding);
        return new PrintWriter(new EolnWriter(writer));
    }

    private static void writeHeader(PrintWriter wr, String pack, String separate, boolean addImports) {
        if (pack != null && pack.length() > 0) {
            if (separate.length() > 0) {
                wr.write("package " + pack + "." + separate + ";\n");
            } else {
                wr.write("package " + pack + ";\n");
            }
        } else {
            if (separate.length() > 0) {
                wr.write("package " + separate + ";\n");
            }
        }
        if (separate.length() > 0 && pack != null && pack.length() > 0) {
            wr.write("\nimport " + pack + ".*;\n");
        } else {
            if (addImports) {
                wr.write("\n");
            }
        }
    }

    void writeImpl(String pack, File file) throws IOException {
        PrintWriter pw = open(file, encoding);
        writeHeader(pw, pack, separate, false);
        pw.write("\n" + GENERATED_WARNING + "\n");
        pw.write("@SuppressWarnings({\"UnnecessaryFullyQualifiedName\", \"RedundantSuppression\"})\n");
        pw.write("public final class " + LocalWrapperBase.getImplName(className) + " extends sqlg2.db.Impl {\n");
        pw.write(later.toString());
        pw.write("}\n");
        pw.close();
    }
}
