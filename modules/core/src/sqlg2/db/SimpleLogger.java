package sqlg2.db;

import java.io.*;
import java.text.DateFormat;
import java.util.Date;

public class SimpleLogger implements SQLGLogger {

    private static final ThreadLocal<DateFormat> TIMESTAMP_FORMAT = new ThreadLocal<DateFormat>() {
        protected DateFormat initialValue() {
            return DateFormat.getDateTimeInstance();
        }
    };

    protected final PrintWriter out;
    protected final PrintWriter err;

    private PrintWriter fileOutput = null;
    private Thread hook = null;

    public SimpleLogger(String errorFile) {
        this(true, true, errorFile);
    }

    public SimpleLogger(boolean stdout, boolean stderr, String errorFile) {
        this(stdout, stderr, errorFile == null ? null : new File(errorFile));
    }

    public SimpleLogger(File errorFile) {
        this(true, true, errorFile);
    }

    public SimpleLogger(boolean stdout, boolean stderr, File errorFile) {
        if (stdout && stderr) {
            out = new PrintWriter(System.out, true);
            err = new PrintWriter(System.err, true);
        } else if (stdout) {
            out = err = new PrintWriter(System.out, true);
        } else if (stderr) {
            out = err = new PrintWriter(System.err, true);
        } else {
            out = err = null;
        }
        if (errorFile != null) {
            try {
                fileOutput = new PrintWriter(new BufferedWriter(new FileWriter(errorFile, true)), true);
                String date = TIMESTAMP_FORMAT.get().format(new Date());
                fileOutput.println("------------------ Log started: " + date + " ------------------");
                try {
                    hook = new Thread() {
                        public void run() {
                            hook = null;
                            close();
                        }
                    };
                    Runtime.getRuntime().addShutdownHook(hook);
                } catch (Exception ex) {
                    hook = null;
                }
            } catch (IOException ex) {
                ex.printStackTrace();
            }
        }
    }

    public static void doPrintTimestamp(PrintWriter pw, String type) {
        pw.println("[" + type + "] " + TIMESTAMP_FORMAT.get().format(new Date()));
    }

    public void trace(String message) {
        if (out != null) {
            out.println(message);
        }
    }

    protected void printTimestamp(PrintWriter pw, String type) {
        doPrintTimestamp(pw, type);
    }

    protected void print(PrintWriter pw, String type, String message) {
        if (pw == null)
            return;
        printTimestamp(pw, type);
        pw.println(message);
    }

    public void info(String message) {
        print(out, "INFO", message);
        print(fileOutput, "INFO", message);
    }

    public void error(String message) {
        print(err, "ERROR", message);
        print(fileOutput, "ERROR", message);
    }

    public static void doPrintException(PrintWriter pw, Throwable th) {
        if (pw == null)
            return;
        doPrintTimestamp(pw, "ERROR");
        th.printStackTrace(pw);
        pw.println("-------------------------------");
    }

    protected void printException(PrintWriter pw, Throwable th) {
        doPrintException(pw, th);
    }

    public void error(Throwable th) {
        printException(err, th);
        printException(fileOutput, th);
    }

    public PrintWriter getFileOutput() {
        return fileOutput;
    }

    public void close() {
        if (fileOutput != null) {
            fileOutput.close();
            fileOutput = null;
        }
        if (hook != null) {
            Runtime.getRuntime().removeShutdownHook(hook);
            hook = null;
        }
    }
}
