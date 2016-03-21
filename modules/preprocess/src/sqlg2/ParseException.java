package sqlg2;

final class ParseException extends Exception {

    final String at;

    ParseException(String message, String at) {
        super(message);
        this.at = at;
    }

    ParseException(String message) {
        this(message, null);
    }
}
