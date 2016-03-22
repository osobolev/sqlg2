package runtime.mapper;

public abstract class AbstractId {

    private long id;

    protected AbstractId() {
    }

    protected AbstractId(long id) {
        this.id = id;
    }

    public String toString() {
        return String.valueOf(id);
    }

    public long getId() {
        return id;
    }
}
