package sqlg2;

import sqlg2.db.DBSpecific;
import sqlg2.db.Impl;
import sqlg2.db.RuntimeMapper;

import java.io.*;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.*;

final class Main extends Options {

    private File workTmpDir = null;

    Main(Options o) {
        super(o);
    }

    private static String stripExtension(String str) {
        String ext = ".java";
        if (str.endsWith(ext)) {
            return str.substring(0, str.length() - ext.length());
        }
        return null;
    }

    private String getPackage(File file) {
        String rootPath = srcRoot.getAbsolutePath();
        String filePath = file.getAbsolutePath();
        if (filePath.startsWith(rootPath)) {
            String sub = filePath.substring(rootPath.length());
            if (sub.length() > 1) {
                String noExt = stripExtension(sub.substring(1));
                if (noExt == null)
                    return null;
                String fileName = noExt.replace(File.separatorChar, '.');
                int p = fileName.lastIndexOf('.');
                if (p >= 0) {
                    return fileName.substring(0, p);
                } else {
                    return "";
                }
            }
        }
        return null;
    }

    private static String getClassName(File file) {
        return stripExtension(file.getName());
    }

    static File packageDir(File baseDir, String pack) {
        File ret = baseDir;
        if (pack != null) {
            StringTokenizer tok = new StringTokenizer(pack, ".");
            while (tok.hasMoreTokens()) {
                String t = tok.nextToken();
                ret = new File(ret, t);
            }
        }
        ret.mkdirs();
        return ret;
    }

    static String readFile(File file, String encoding) throws IOException {
        Reader rdr = null;
        try {
            InputStream is = new FileInputStream(file);
            rdr = encoding == null ? new InputStreamReader(is) : new InputStreamReader(is, encoding);
            rdr = new BufferedReader(rdr);
            StringBuilder buf = new StringBuilder();
            while (true) {
                int c = rdr.read();
                if (c < 0)
                    break;
                buf.append((char) c);
            }
            return buf.toString();
        } finally {
            if (rdr != null) {
                try {
                    rdr.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    private static void writeFile(File file, String text, String encoding) throws IOException {
        try (Writer wr = MethodRunner.open(file, encoding)) {
            wr.write(text);
        }
        // ignore
    }

    private static void copyFile(File in, File out) throws IOException {
        InputStream is = null;
        OutputStream os = null;
        try {
            is = new FileInputStream(in);
            is = new BufferedInputStream(is);
            os = new FileOutputStream(out);
            os = new BufferedOutputStream(os);
            while (true) {
                int c = is.read();
                if (c < 0)
                    break;
                os.write(c);
            }
        } finally {
            if (os != null) {
                try {
                    os.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ex) {
                    // ignore
                }
            }
        }
    }

    private File getOutFile(File out, String className) {
        File dir = packageDir(out.getParentFile(), implPack);
        return new File(dir, LocalWrapperBase.getImplName(className) + ".java");
    }

    private File getTmpDir() throws IOException {
        if (workTmpDir == null) {
            File tmp = File.createTempFile("sqlg", ".tmp", tmpDir);
            tmp.delete();
            tmp.mkdirs();
            workTmpDir = tmp;
        }
        return workTmpDir;
    }

    private File getTmpOutFile(File in) throws IOException {
        String pack = getPackage(in);
        return new File(packageDir(getTmpDir(), pack), in.getName());
    }

    private static boolean isDefault(String value, String defValue) {
        if (value == null) {
            return defValue == null;
        } else {
            return value.equals(defValue);
        }
    }

    private void writePackField(StringBuilder buf, boolean isDefault,
                                String field, String value) {
        if (isDefault)
            return;
        if (value == null)
            throw new NullPointerException();
        buf.append(tab).append("public static final String ").append(field).append(" = ");
        buf.append("\"").append(value).append("\";\n");
    }

    private void doWorkFiles(File[] in, File[] out) throws Exception {
        // 1. check modification time
        boolean[] needWork = new boolean[in.length];
        boolean needAnyWork;
        if (checkTime) {
            needAnyWork = false;
            for (int i = 0; i < in.length; i++) {
                if (!in[i].canRead())
                    throw new FileNotFoundException(in[i].getPath());
                String className = getClassName(in[i]);
                File impl = getOutFile(out[i], className);
                needWork[i] = !(impl.isFile() && in[i].lastModified() <= impl.lastModified());
                if (needWork[i]) {
                    needAnyWork = true;
                }
            }
        } else {
            Arrays.fill(needWork, true);
            needAnyWork = true;
        }
        if (!needAnyWork)
            return;
        // 2. parse & copy to temp
        Parser[] parsers = new Parser[in.length];
        needAnyWork = false;
        Map<String, RowTypeCutPaste> rowTypeMap = new HashMap<>();
        for (int i = 0; i < in.length; i++) {
            if (needWork[i]) {
                String text = readFile(in[i], encoding);
                String className = getClassName(in[i]);
                Parser parser = new Parser(text, className, rowTypeMap);
                parser.parseAll();
                parsers[i] = parser;
                if (parser.isNeedsProcessing()) {
                    needAnyWork = true;
                }
            }
        }
        if (!needAnyWork)
            return;
        File[] compFiles = new File[in.length];
        for (int i = 0; i < in.length; i++) {
            compFiles[i] = getTmpOutFile(in[i]);
            if (needWork[i]) {
                Parser parser = parsers[i];
                String newText;
                if (parser.isNeedsProcessing()) {
                    newText = parser.doCutPaste();
                } else {
                    newText = parser.text;
                }
                writeFile(compFiles[i], newText, encoding);
            } else {
                copyFile(in[i], compFiles[i]);
            }
        }
        // 3. run methods
        boolean inited = false;
        WrapperGeneratorFactory factory = null;
        Map<String, ColumnData> generatedIn = new HashMap<>();
        Map<String, ColumnData> generatedOut = new HashMap<>();
        for (int i = 0; i < in.length; i++) {
            if (!needWork[i])
                continue;
            Parser parser = parsers[i];
            if (!parser.isNeedsProcessing())
                continue;
            if (!inited) {
                Mapper mapper = (Mapper) Class.forName(mapperClass).newInstance();
                RuntimeMapper runtimeMapper = (RuntimeMapper) Class.forName(runtimeMapperClass).newInstance();
                DBSpecific specific;
                try {
                    Class.forName(driverClass);
                    specific = (DBSpecific) Class.forName(dbClass).newInstance();
                    factory = (WrapperGeneratorFactory) Class.forName(wrapperClass).newInstance();
                } catch (ClassNotFoundException ex) {
                    throw Impl.wrap(ex);
                }
                String checkerClassName = specific.getCheckerClassName();
                SqlChecker checker;
                try {
                    checker = (SqlChecker) Class.forName(checkerClassName).newInstance();
                } catch (ClassNotFoundException ex) {
                    throw Impl.wrap(ex);
                }
                Connection connection = DriverManager.getConnection(url, user, pass);
                connection.setAutoCommit(false);
                GTestImpl.INSTANCE.init(connection, checker, mapper, runtimeMapper, specific);
                inited = true;
            }
            String pack = getPackage(in[i]);
            String className = getClassName(in[i]);
            String fullClassName = pack == null || pack.length() <= 0 ? className : pack + "." + className;
            List<Entry> entries = parser.getEntries();
            String[] imports = parser.getImports();
            Class<?> cls = new Runner(getTmpDir()).compileAndLoad(srcRoot, compFiles[i], fullClassName, encoding, classpath);
            MethodRunner runner = new MethodRunner(
                cls, className, encoding, tab, implPack, entries, srcRoot, gwt, warn, log, generatedIn, generatedOut
            );
            boolean canWrap = GBase.class.isAssignableFrom(cls);
            // 4. generate wrappers
            if (wrapPack != null && canWrap) {
                Method[] entryMethods = runner.checkEntries(rowTypeMap, parser.getBindMap(), parser.getParameters());
                WrapperGenerator[] generators = factory.newGenerators(pack, className, wrapPack, out[i].getParentFile());
                for (WrapperGenerator g : generators) {
                    g.init(encoding, tab, cls, imports);
                }
                for (int j = 0; j < entries.size(); j++) {
                    Entry entry = entries.get(j);
                    Method toCall = entryMethods[j];
                    if (toCall == null)
                        continue;
                    for (WrapperGenerator g : generators) {
                        if (!entry.publish)
                            continue;
                        g.addMethod(toCall, entry.javadoc, entry.methodHeader, entry.returnType, entry.paramNames);
                    }
                }
                for (WrapperGenerator g : generators) {
                    g.close();
                }
            }

            AfterCutPaste after = parser.getAfter();
            if (after != null && canWrap) {
                boolean isDefaultImpl = isDefault(implPack, LocalWrapperBase.DEFAULT_IMPL_PACKAGE);
                boolean isDefaultWrap;
                if (wrapPack != null) {
                    isDefaultWrap = isDefault(wrapPack, LocalWrapperBase.DEFAULT_WRAPPER_PACKAGE) || wrapPack.equals(implPack);
                } else {
                    isDefaultWrap = true;
                }
                StringBuilder buf = new StringBuilder();
                writePackField(buf, isDefaultImpl, LocalWrapperBase.IMPL_FIELD, implPack);
                if (wrapPack != null) {
                    writePackField(buf, isDefaultWrap, LocalWrapperBase.WRAPPER_FIELD, wrapPack);
                }
                boolean isDefaultMapper = isDefault(runtimeMapperClass, LocalWrapperBase.DEFAULT_MAPPER_CLASS);
                writePackField(buf, isDefaultMapper, LocalWrapperBase.MAPPER_FIELD, runtimeMapperClass);
                if (buf.length() > 0) {
                    buf.insert(0, "\n" + tab + ParserBase.PREPROCESSOR_LINE + "\n\n");
                    buf.append("\n").append(tab).append(ParserBase.PREPROCESSOR_LINE + "\n");
                    after.replaceTo = buf.toString();
                }
            }
            String newText = parser.doCutPaste();
            writeFile(out[i], newText, encoding);

            runner.writeImpl(pack, getOutFile(out[i], className));
        }
    }

    void workFiles(File[] in, File[] out) throws Exception {
        try {
            doWorkFiles(in, out);
        } finally {
            if (GTestImpl.INSTANCE.connection != null) {
                try {
                    GTestImpl.INSTANCE.connection.close();
                } catch (SQLException ex) {
                    // ignore
                }
                if (url.startsWith("jdbc:derby:")) {
                    // Special case for Derby:
                    try {
                        DriverManager.getConnection("jdbc:derby:;shutdown=true");
                    } catch (SQLException ex) {
                        // ignore
                    }
                }
            }
            if (cleanup && workTmpDir != null) {
                cleanup(workTmpDir);
            }
        }
    }

    private static void cleanup(File dir) {
        File[] files = dir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    cleanup(file);
                } else {
                    if (!file.delete()) {
                        file.deleteOnExit();
                    }
                }
            }
        }
        if (!dir.delete()) {
            dir.deleteOnExit();
        }
    }
}
