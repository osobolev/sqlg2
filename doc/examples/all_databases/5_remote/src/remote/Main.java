package remote;

/**
 * Runs both server and client
 */
public final class Main {

    public static void main(String[] args) throws Exception {
        if (args.length < 5) {
            System.err.println("Not all DB properties specified");
        } else {
            String driver = args[0];
            String url = args[1];
            String username = args[2];
            String password = args[3];
            String db = args[4];
            Server.runServer(driver, url, db);
            Client.runClient(username, password);
            System.exit(0);
        }
    }
}
