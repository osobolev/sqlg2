package sqlg2.db;

import java.io.*;
import java.text.DateFormat;
import java.util.Date;

public class SimpleLogger implements SQLGLogger {

    private static final ThreadLocal<DateFormat> DATE_FORMAT = new ThreadLocal<DateFormat>() {
        protected DateFormat initialValue() {
            return DateFormat.getDateTimeInstance();
        }
    };

    public final PrintWriter err = new PrintWriter(System.err, true);
    public final PrintWriter out = new PrintWriter(System.out, true);

    private PrintWriter fileOutput = null;
    private Thread hook = null;

    public SimpleLogger(String errorFile) {
        this(new File(errorFile));
    }

    public SimpleLogger(File errorFile) {
        try {
            fileOutput = new PrintWriter(new BufferedWriter(new FileWriter(errorFile, true)), true);
            String date = DATE_FORMAT.get().format(new Date());
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
            ex.printStackTrace(err);
        }
    }

    public void trace(String message) {
        out.println(message);
    }

    public void info(String message) {
        out.print(message);
        if (fileOutput != null) {
            fileOutput.println(message);
        }
    }

    public void error(String message) {
        err.println(message);
        if (fileOutput != null) {
            fileOutput.println(message);
        }
    }

    public static void printException(PrintWriter pw, Throwable th) {
        pw.println(DATE_FORMAT.get().format(new Date()));
        th.printStackTrace(pw);
        pw.println("-------------------------------");
    }

    public void error(Throwable th) {
        printException(err, th);
        if (fileOutput != null) {
            printException(fileOutput, th);
        }
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
