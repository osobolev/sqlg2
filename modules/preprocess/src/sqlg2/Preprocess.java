package sqlg2;

import org.apache.tools.ant.BuildException;
import org.apache.tools.ant.DirectoryScanner;
import org.apache.tools.ant.Location;
import org.apache.tools.ant.Task;
import org.apache.tools.ant.types.FileSet;
import sqlg2.db.DBSpecific;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * ANT task for SQLG preprocessor.
 */
public class Preprocess extends Task {

    private final List<FileSet> filesets = new ArrayList<>();

    private final Options options = new Options();

    /**
     * Sets flag to clean up temporary files.
     */
    public void setCleanup(boolean on) {
        options.cleanup = on;
    }

    /**
     * Sets source files encoding.
     */
    public void setEncoding(String encoding) {
        options.encoding = encoding;
    }

    /**
     * Sets tab size (&lt;0 for tab character).
     */
    public void setTabsize(int size) {
        options.setTabSize(size);
    }

    /**
     * Sets subpackage name for row type implementation classes.
     */
    public void setImplpack(String sub) {
        options.implPack = sub;
    }

    /**
     * Sets subpackage name for wrapper classes (null for no wrapper generation).
     */
    public void setWrappack(String sub) {
        options.wrapPack = sub;
    }

    /**
     * {@link Mapper} implementation class name.
     */
    public void setMapperclass(String cls) {
        options.mapperClass = cls;
    }

    /**
     * {@link DBSpecific} implementation class name.
     */
    public void setDbclass(String cls) {
        options.dbClass = cls;
    }

    /**
     * {@link WrapperGeneratorFactory} implementation class name.
     */
    public void setWrapperclass(String cls) {
        options.wrapperClass = cls;
    }

    /**
     * JDBC driver class name to be used during preprocess.
     */
    public void setDriverclass(String driver) {
        options.driverClass = driver;
    }

    /**
     * JDBC URL to be used during preprocess.
     */
    public void setUrl(String url) {
        options.url = url;
    }

    /**
     * DB user name to be used during preprocess.
     */
    public void setUser(String user) {
        options.user = user;
    }

    /**
     * DB user password to be used during preprocess.
     */
    public void setPassword(String password) {
        options.pass = password;
    }

    /**
     * CLASSPATH to be used during compilation of processed files.
     * Should include all required files referenced in processed files.
     * Usual CLASSPATH format.
     */
    public void setClasspath(String classpath) {
        options.classpath = classpath;
    }

    /**
     * Sets timestamp check mode.
     * @param force false to compare timestamp on original file and generated row type implementation,
     * true to always re-generate files.
     */
    public void setForce(boolean force) {
        options.checkTime = !force;
    }

    /**
     * Source files root. Package of class is determined relative to the source root.
     */
    public void setSrcroot(File srcroot) {
        options.srcRoot = srcroot;
    }

    /**
     * true to generate default constructor for row type classes (to make them GWT-serializable, for example)
     */
    public void setGwt(boolean gwt) {
        options.gwt = gwt;
    }

    /**
     * Warning output mode
     */
    public void setWarn(SQLGWarn warn) {
        options.warn = warn;
    }

    /**
     * Log method running
     */
    public void setLog(boolean log) {
        options.log = log;
    }

    public void setRuntimemapperclass(String cls) {
        options.runtimeMapperClass = cls;
    }

    /**
     * Preprocessor temporary files folder
     */
    public void setTmpdir(File tmpdir) {
        options.tmpDir = tmpdir;
    }

    /**
     * Adds files process.
     */
    public void addFileset(FileSet set) {
        filesets.add(set);
    }

    /**
     * Runs ANT task.
     */
    public void execute() throws BuildException {
        List<File> files = new ArrayList<>();
        for (FileSet fs : filesets) {
            DirectoryScanner ds = fs.getDirectoryScanner(getProject());
            String[] srcFiles = ds.getIncludedFiles();
            for (String srcFile : srcFiles) {
                File src = new File(fs.getDir(getProject()), srcFile);
                files.add(src);
            }
        }
        File[] filearr = files.toArray(new File[0]);
        try {
            new Main(options).workFiles(filearr, filearr);
        } catch (ParseException ex) {
            String at = ex.at;
            if (at == null) {
                throw new BuildException(ex.getMessage(), ex);
            } else {
                throw new BuildException(ex.getMessage(), ex, new Location(at));
            }
        } catch (Exception ex) {
            StackTraceElement[] st = ex.getStackTrace();
            int lastBase = -1;
            for (int i = st.length - 1; i >= 0; i--) {
                StackTraceElement element = st[i];
                if (GBase.class.getName().equals(element.getClassName())) {
                    lastBase = i;
                    break;
                }
            }
            if (lastBase >= 0 && lastBase < st.length - 1) {
                StackTraceElement element = st[lastBase + 1];
                String fileName = element.getFileName();
                int line = element.getLineNumber();
                throw new BuildException(fileName + ":" + line + ": " + ex.getMessage(), ex);
            } else {
                throw new BuildException(ex);
            }
        }
    }
}
