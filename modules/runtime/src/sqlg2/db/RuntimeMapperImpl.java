package sqlg2.db;

/**
 * Default implementation of {@link RuntimeMapper} which has no custom-mapped types.
 */
public class RuntimeMapperImpl implements RuntimeMapper {

    /**
     * Utility method to cast {@link CustomMapper} of any type to comply with {@link #getMapper} method signature.
     */
    @SuppressWarnings("unchecked")
    public static <T> CustomMapper<T> cast(CustomMapper<?> mapper) {
        return (CustomMapper<T>) mapper;
    }

    public <T> CustomMapper<T> getMapper(Class<T> cls) {
        throw new SQLGException("Cannot find mapping for class " + cls.getCanonicalName());
    }
}
