package sqlg2;

import sqlg2.db.*;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.ConcurrentMap;

/**
 * For internal use.
 * Base class for local wrappers.
 */
public class LocalWrapperBase {

    private static final String INTERFACE_PREFIX = "I";

    public static final String DEFAULT_IMPL_PACKAGE = "wrapper";
    public static final String IMPL_FIELD = "_IMPLEMENTATION_PACKAGE";

    public static final String DEFAULT_WRAPPER_PACKAGE = "wrapper";
    public static final String WRAPPER_FIELD = "_WRAPPER_PACKAGE";

    public static final String DEFAULT_MAPPER_CLASS = RuntimeMapperImpl.class.getName();
    public static final String MAPPER_FIELD = "_RUNTIME_MAPPER";

    private final InternalTransaction trans;
    private final boolean inline;

    /**
     * Constructor.
     *
     * @param trans transaction in which this object will work
     */
    public LocalWrapperBase(InternalTransaction trans, boolean inline) {
        this.trans = trans;
        this.inline = inline;
    }

    final Connection getConnection() throws SQLException {
        return trans.getConnection();
    }

    /**
     * Transaction finish.
     *
     * @param ok transaction success (true if everything's ok and we need to COMMIT, and false
     * if something went wrong and we need to ROLLBACK).
     */
    protected final void endTransaction(boolean ok) throws SQLException {
        if (!inline) {
            if (ok) {
                trans.commitImmediate();
            } else {
                trans.rollbackImmediate();
            }
        }
    }

    public static String getImplName(String name) {
        return name + "Impl";
    }

    private static String getStaticField(Class<?> base, String field, String defValue) {
        try {
            Field sepField = base.getField(field);
            return (String) sepField.get(null);
        } catch (NoSuchFieldException nsfe) {
            // ignore
        } catch (ClassCastException cce) {
            // ignore
        } catch (IllegalAccessException iae) {
            // ignore
        }
        return defValue;
    }

    private static String getBaseNameFromInterface(String interfaceName) {
        return interfaceName.substring(INTERFACE_PREFIX.length());
    }

    public static String getInterfaceName(String baseName) {
        return INTERFACE_PREFIX + baseName;
    }

    public static String getLocalWrapperName(String baseName) {
        return "LW" + baseName;
    }

    public static Class<?> loadClass(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    /**
     * Returns implementation class name for data access interface of given type.
     * Example: pack.ITestDao -> pack.wrapper.LWTestDao
     *
     * @param iface data access interface
     */
    public static String getWrapperNameFromInterface(Class<? extends IDBCommon> iface) throws ClassNotFoundException {
        String name = iface.getName();
        int p = name.lastIndexOf('.');
        String baseClassName;
        String baseName;
        // pack.ITestDao -> baseName = TestDao, baseClassName = pack.TestDao
        if (p < 0) {
            baseName = baseClassName = getBaseNameFromInterface(name);
        } else {
            baseName = getBaseNameFromInterface(name.substring(p + 1));
            baseClassName = name.substring(0, p + 1) + baseName;
        }
        Class<?> baseClass = loadClass(baseClassName);
        String wrapPack = getStaticField(baseClass, WRAPPER_FIELD, null);
        if (wrapPack == null) {
            wrapPack = getStaticField(baseClass, IMPL_FIELD, DEFAULT_WRAPPER_PACKAGE);
        }
        if (wrapPack.length() > 0) {
            wrapPack += ".";
        }
        String wrapperName = getLocalWrapperName(baseName);
        if (p < 0) {
            return wrapPack + wrapperName;
        } else {
            return name.substring(0, p + 1) + wrapPack + wrapperName;
        }
    }

    private static String getNestedImplName(String name) {
        return name + "Impl";
    }

    /**
     * pack.TestDao -> pack.wrapper.TestDaoImpl
     */
    private static String getBaseImplName(Class<?> base) {
        String implPack = getStaticField(base, IMPL_FIELD, DEFAULT_IMPL_PACKAGE);
        if (implPack.length() > 0) {
            implPack += ".";
        }
        String pname = base.getName();
        int p = pname.lastIndexOf('.');
        if (p < 0) {
            return implPack + getImplName(pname);
        } else {
            return pname.substring(0, p + 1) + implPack + getImplName(pname.substring(p + 1));
        }
    }

    private static Method findMethod(Class<?> cls, Class<? extends GBase> base) {
        Throwable cause;
        try {
            String implName = getBaseImplName(base);
            Class<?> impl = loadClass(implName);
            return impl.getMethod("create" + cls.getSimpleName(), ResultSet.class, GBase.class);
        } catch (ClassNotFoundException cnfe) {
            cause = cnfe;
        } catch (NoSuchMethodException nsme) {
            cause = nsme;
        }
        throw new SQLGException("Cannot find factory method", cause);
    }

    RowTypeFactory findFactory(Class<?> cls, Class<? extends GBase> base) {
        ConcurrentMap<Class<?>, RowTypeFactory> cache = trans.getCaches().getRowTypeFactoryMap();
        RowTypeFactory f = cache.get(cls);
        if (f != null) {
            return f;
        } else {
            Method method = findMethod(cls, base);
            RowTypeFactory factory = new RowTypeFactory(method);
            cache.put(cls, factory);
            return factory;
        }
    }

    /**
     * pack.TestDao$Row -> pack.wrapper.TestDaoImpl$RowImpl
     */
    private static String getImplName(Class<?> parent, Class<?> cls) {
        String baseName = getBaseImplName(parent);
        String name = cls.getName();
        int q = name.lastIndexOf('$');
        return baseName + "$" + getNestedImplName(name.substring(q + 1));
    }

    static Constructor<?> getDefaultConstructor(Class<?> cls) {
        Throwable cause;
        try {
            Class<?> parent = cls.getDeclaringClass();
            if (parent == null) {
                return cls.getConstructor();
            } else {
                String implName = getImplName(parent, cls);
                Class<?> impl = loadClass(implName);
                return impl.getConstructor();
            }
        } catch (ClassNotFoundException cnfe) {
            cause = cnfe;
        } catch (NoSuchMethodException nsme) {
            cause = nsme;
        }
        throw new SQLGException("Cannot find constructor", cause);
    }

    final DBSpecific getSpecific() {
        return trans.getSpecific();
    }

    final SQLGLogger getLogger() {
        return trans.getLogger();
    }

    final void trace(boolean ok, long time, String sql, Parameter[] params) {
        String message = trans.getSqlTrace().getTraceMessage(ok, time);
        if (message != null) {
            SQLGLogger logger = trans.getLogger();
            logger.error(message);
            if (sql != null) {
                logger.error("Last SQL:");
                logger.error(sql);
                if (params != null && params.length > 0) {
                    StringBuilder buf = new StringBuilder();
                    buf.append("with params (");
                    for (int i = 0; i < params.length; i++) {
                        if (i > 0) {
                            buf.append(", ");
                        }
                        buf.append(params[i]);
                    }
                    buf.append(")");
                    logger.error(buf.toString());
                }
            }
        }
    }

    final <T extends IDBCommon> T getInlineInterface(Class<T> iface) {
        return trans.getInlineInterface(iface);
    }

    RuntimeMapper getMapper(Class<? extends GBase> base) {
        if (GBase.test != null) {
            return GBase.test.getRuntimeMapper();
        } else {
            ConcurrentMap<Class<? extends GBase>, RuntimeMapper> cache = trans.getCaches().getRuntimeMapperMap();
            RuntimeMapper m = cache.get(base);
            if (m != null) {
                return m;
            } else {
                String runtimeClassName = getStaticField(base, MAPPER_FIELD, DEFAULT_MAPPER_CLASS);
                Throwable cause;
                try {
                    Class<?> rtClass = loadClass(runtimeClassName);
                    RuntimeMapper mapper = (RuntimeMapper) rtClass.newInstance();
                    cache.put(base, mapper);
                    return mapper;
                } catch (ClassNotFoundException ex) {
                    cause = ex;
                } catch (InstantiationException ex) {
                    cause = ex;
                } catch (IllegalAccessException ex) {
                    cause = ex;
                }
                throw new SQLGException("Cannot create runtime mapper", cause);
            }
        }
    }
}
