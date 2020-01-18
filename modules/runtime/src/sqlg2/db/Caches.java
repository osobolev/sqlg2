package sqlg2.db;

import sqlg2.GBase;

import java.lang.reflect.Constructor;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * For internal use.
 * Caches results of reflection wizardry performed by runtime.
 */
public final class Caches {

    private static <K, V> ConcurrentMap<K, V> create() {
        return new ConcurrentHashMap<>();
    }

    /**
     * Mapping of RowType class to its factory.
     */
    private final ConcurrentMap<Class<?>, RowTypeFactory> rowTypeFactoryMap = create();
    /**
     * Mapping of base class to its runtime mapper factory.
     */
    private final ConcurrentMap<Class<? extends GBase>, RuntimeMapper> runtimeMapperMap = create();
    /**
     * Mapping of data access interface class to its implementation.
     */
    private final ConcurrentMap<Class<?>, Constructor<?>> wrapperMap = create();

    public ConcurrentMap<Class<?>, RowTypeFactory> getRowTypeFactoryMap() {
        return rowTypeFactoryMap;
    }

    public ConcurrentMap<Class<? extends GBase>, RuntimeMapper> getRuntimeMapperMap() {
        return runtimeMapperMap;
    }

    ConcurrentMap<Class<?>, Constructor<?>> getWrapperMap() {
        return wrapperMap;
    }
}
