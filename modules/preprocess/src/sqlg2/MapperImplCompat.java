package sqlg2;

/**
 * {@link Mapper} implementation which is compatible with pre-4.0 SQL DATE columns behaviour
 */
public class MapperImplCompat extends MapperImpl {

    protected FetchClass getDateClass() {
        return new FetchClass(java.util.Date.class, "getTimestamp");
    }
}
