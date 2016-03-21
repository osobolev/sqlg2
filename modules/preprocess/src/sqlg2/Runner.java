package sqlg2;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.StringTokenizer;

final class Runner {

    private final File tmpDir;

    Runner(File tmpDir) {
        this.tmpDir = tmpDir;
    }

    private void compile(File srcRoot, String encoding, String classpath, File file) throws ParseException {
        List<String> params = new ArrayList<String>(Arrays.asList(
            "-classpath",
            tmpDir.getAbsolutePath()
                + File.pathSeparator + srcRoot.getAbsolutePath()
                + (classpath == null ? "" : File.pathSeparator + classpath),
            "-d", tmpDir.getAbsolutePath(),
            file.getAbsolutePath()
        ));
        if (encoding != null) {
            params.addAll(0, Arrays.asList("-encoding", encoding));
        }

        String[] arguments = params.toArray(new String[params.size()]);
        try {
            JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
            ByteArrayOutputStream buf = new ByteArrayOutputStream();
            if (compiler.run(null, buf, buf, arguments) != 0) {
                throw new ParseException(buf.toString());
            }
        } catch (ParseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ParseException(ex.getMessage());
        }
    }

    private static boolean isFromSameSource(String className, String name) {
        if (name.equals(className))
            return true;
        if (name.startsWith(className)) {
            return name.substring(className.length()).startsWith("$");
        } else {
            return false;
        }
    }

    Class<?> compileAndLoad(File srcRoot, File file, final String className, String encoding, String classpath) throws MalformedURLException, ClassNotFoundException, ParseException {
        compile(srcRoot, encoding, classpath, file);

        StringTokenizer tok = new StringTokenizer(classpath, File.pathSeparator);
        URL[] urls = new URL[1 + tok.countTokens()];
        urls[0] = tmpDir.toURI().toURL();
        int i = 1;
        while (tok.hasMoreTokens()) {
            String t = tok.nextToken();
            urls[i] = new File(t).toURI().toURL();
            i++;
        }
        URLClassLoader cll = new URLClassLoader(urls, getClass().getClassLoader()) {
            protected synchronized Class<?> loadClass(String name, boolean resolve) throws ClassNotFoundException {
                if (isFromSameSource(className, name)) {
                    Class<?> c = findLoadedClass(name);
                    if (c == null) {
                        c = findClass(name);
                    }
                    if (resolve) {
                        resolveClass(c);
                    }
                    return c;
                } else {
                    return super.loadClass(name, resolve);
                }
            }
        };
        return cll.loadClass(className);
    }
}
