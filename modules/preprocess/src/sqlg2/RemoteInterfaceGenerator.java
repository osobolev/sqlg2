package sqlg2;

import sqlg2.db.InformationException;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Method;
import java.sql.SQLException;

/**
 * Implementation of {@link WrapperGenerator} for generation of data access interface
 * and local and RMI wrappers implementing this interface.
 */
public class RemoteInterfaceGenerator implements WrapperGenerator {

    private final String db = "_db";

    private final File baseDir;
    private final String pack;
    private final String separate;
    private final String name;
    private PrintWriter wrif;
    private PrintWriter wrdb;
    private String tab = "";

    public RemoteInterfaceGenerator(String pack, String className, String separate, File dir) {
        this.pack = pack;
        this.separate = separate;
        this.baseDir = dir;
        this.name = className;
    }

    private static String getClassName(Class<?> cls) {
        return GTestImpl.INSTANCE.mapper.getClassName(cls, null);
    }

    private static void insertImports(PrintWriter wr, String[] imports, Class<?> cls) {
        for (String i : imports) {
            wr.write("import " + i + ";\n");
        }
        wr.write("import static " + cls.getName() + ".*;\n\n");
    }

    private void interfaceHeader(PrintWriter wrif, String[] imports, Class<?> cls, String interfaceName) {
        MethodRunner.writeHeader(wrif, pack, "");
        insertImports(wrif, imports, cls);
        wrif.write("import sqlg2.db.IDBCommon;\n\n");
        String addIface;
        if (cls != null) {
            Class<?>[] ifaces = cls.getInterfaces();
            StringBuilder buf = new StringBuilder();
            for (Class<?> iface : ifaces) {
                buf.append(", ");
                buf.append(getClassName(iface));
            }
            addIface = buf.toString();
        } else {
            addIface = "";
        }
        wrif.write(MethodRunner.GENERATED_WARNING + "\n");
        wrif.write("@SuppressWarnings({\"UnnecessaryInterfaceModifier\", \"UnnecessaryFullyQualifiedName\", \"RedundantSuppression\"})\n");
        wrif.write("public interface " + interfaceName + " extends IDBCommon" + addIface + " {\n");
    }

    public void init(String encoding, String tab, Class<?> cls, String[] imports) throws IOException {
        this.tab = tab;
        File dir = Main.packageDir(baseDir, separate);

        String interfaceName = LocalWrapperBase.getInterfaceName(name);
        File fif = new File(baseDir, interfaceName + ".java");
        wrif = MethodRunner.open(fif, encoding);
        interfaceHeader(wrif, imports, cls, interfaceName);

        String dbName = LocalWrapperBase.getLocalWrapperName(name);
        File fdb = new File(dir, dbName + ".java");
        wrdb = MethodRunner.open(fdb, encoding);
        MethodRunner.writeHeader(wrdb, pack, separate);
        wrdb.write("\n");
        insertImports(wrdb, imports, cls);
        wrdb.write("import sqlg2.LocalWrapperBase;\n");
        wrdb.write("import sqlg2.db.InternalTransaction;\n\n");
        wrdb.write(MethodRunner.GENERATED_WARNING + "\n");
        wrdb.write("@SuppressWarnings({\"MissortedModifiers\", \"UnnecessaryFullyQualifiedName\", \"MethodParameterNamingConvention\", \"InstanceVariableNamingConvention\", \"LocalVariableNamingConvention\", \"RedundantSuppression\"})\n");
        wrdb.write("public final class " + dbName + " extends LocalWrapperBase implements " + interfaceName + " {\n\n");
        wrdb.write(tab + "private final " + name + " " + db + ";\n\n");
        wrdb.write(tab + "public " + dbName + "(InternalTransaction trans, boolean inline) {\n");
        wrdb.write(tab + tab + "super(trans, inline);\n");
        wrdb.write(tab + tab + "this." + db + " = new " + name + "(this);\n");
        wrdb.write(tab + "}\n");
    }

    private static String addException(int nexceptions, String exception) {
        if (nexceptions > 0) {
            return ", " + exception;
        } else {
            return " throws " + exception;
        }
    }

    public void addMethod(Method method, String javadoc, String methodHeader, String returnType, String[] params) {
        Class<?>[] exs = method.getExceptionTypes();
        boolean hasSql = false;
        for (Class<?> ex : exs) {
            if (SQLException.class.equals(ex)) {
                hasSql = true;
            }
        }
        String addThrow = hasSql ? "" : addException(exs.length, "java.sql.SQLException");

        // output interface method
        if (javadoc != null) {
            wrif.write("\n" + tab + javadoc);
        }
        wrif.write("\n" + tab + methodHeader + addThrow + ";\n");

        // output wrapper method
        Class<?> retType = method.getReturnType();
        String call;
        {
            StringBuilder buf = new StringBuilder();
            buf.append(method.getName());
            buf.append('(');
            for (int i = 0; i < params.length; i++) {
                if (i > 0)
                    buf.append(", ");
                buf.append(params[i]);
            }
            buf.append(')');
            call = buf.toString();
        }

        String ok = "_ok";
        String ret = "_ret";
        String t0 = "_t0";

        wrdb.write("\n" + tab + "synchronized " + methodHeader + addThrow + " {\n");
        wrdb.write(tab + tab + "boolean " + ok + " = false;\n");
        wrdb.write(tab + tab + "long " + t0 + " = System.currentTimeMillis();\n");
        wrdb.write(tab + tab + "try {\n");
        if (Void.TYPE.equals(retType)) {
            wrdb.write(tab + tab + tab + db + "." + call + ";\n");
            wrdb.write(tab + tab + tab + ok + " = true;\n");
        } else {
            wrdb.write(tab + tab + tab + returnType + " " + ret + " = " + db + "." + call + ";\n");
            wrdb.write(tab + tab + tab + ok + " = true;\n");
            wrdb.write(tab + tab + tab + "return " + ret + ";\n");
        }
        for (Class<?> ex : exs) {
            if (InformationException.class.isAssignableFrom(ex)) {
                wrdb.write(tab + tab + "} catch (" + getClassName(ex) + " ex) {\n");
                wrdb.write(tab + tab + tab + ok + " = !ex.isError();\n");
                wrdb.write(tab + tab + tab + "throw ex;\n");
            }
        }
        wrdb.write(tab + tab + "} finally {\n");
        wrdb.write(tab + tab + tab + db + ".closeStatements();\n");
        wrdb.write(tab + tab + tab + db + ".traceSql(" + ok + ", " + t0 + ");\n");
        wrdb.write(tab + tab + tab + "endTransaction(" + ok + ");\n");
        wrdb.write(tab + tab + "}\n");
        wrdb.write(tab + "}\n");
    }

    private static void end(PrintWriter pw) {
        if (pw != null) {
            pw.write("}\n");
            pw.close();
        }
    }

    public void close() {
        end(wrif);
        end(wrdb);
    }
}
