package sqlg2.db;

public interface SQLGLogger {

    void trace(String message);

    void info(String message);

    void error(String message);

    void error(Throwable error);

    class Simple implements SQLGLogger {

        public void trace(String message) {
            System.out.println(message);
        }

        public void info(String message) {
            System.out.println(message);
        }

        public void error(String message) {
            System.err.println(message);
        }

        public void error(Throwable error) {
            error.printStackTrace();
        }
    }
}
