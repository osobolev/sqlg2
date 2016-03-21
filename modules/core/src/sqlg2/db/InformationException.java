package sqlg2.db;

/**
 * This is "informational" exception.
 * If a business method raises exception of this type, and
 * <code>isError() == false</code> then runtime system does not roll back
 * transaction - call is finished successfully, but exception is rethrown.
 * That way you can pass information about exceptional situation
 * without losing DB changes. Default value of <code>isError() == true</code>.
 */
public abstract class InformationException extends Exception {

    private boolean error = true;

    protected InformationException() {
    }

    protected InformationException(String s) {
        super(s);
    }

    /**
     * Defaults to true.
     */
    public boolean isError() {
        return error;
    }

    public void setError(boolean error) {
        this.error = error;
    }
}
