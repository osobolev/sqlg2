package complex.mapper;

/**
 * Object DB type is mapped to this class.
 */
public final class UserObj {

    public final long id;
    public final String name;

    public UserObj(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String toString() {
        return id + ". " + name;
    }
}
