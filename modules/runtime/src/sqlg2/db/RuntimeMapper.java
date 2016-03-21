package sqlg2.db;

/**
 * This class defines custom Java/SQL mapping.
 */
public interface RuntimeMapper {

    /**
     * Returns mapping for class {@code cls}
     *
     * @param cls class for which to define mapping
     * @return mapping for the class {@code cls}
     */
    <T> CustomMapper<T> getMapper(Class<T> cls);
}
