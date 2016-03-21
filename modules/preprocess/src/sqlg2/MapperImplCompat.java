package sqlg2;

public class MapperImplCompat extends MapperImpl {

    protected FetchClass getDateClass() {
        return new FetchClass(java.util.Date.class, "getTimestamp");
    }
}
