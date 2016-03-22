package columns.mapper;

public final class User {

    public final long id;
    public final String name;

    public User(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String toString() {
        return id + ". " + name;
    }
}
