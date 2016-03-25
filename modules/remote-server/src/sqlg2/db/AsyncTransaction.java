package sqlg2.db;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

final class AsyncTransaction implements ISimpleTransaction {

    private final DBInterface db;

    AsyncTransaction(DBInterface db) {
        this.db = db;
    }

    @SuppressWarnings("unchecked")
    public <T extends IDBCommon> T getInterface(final Class<T> iface) {
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class[] {iface}, new InvocationHandler() {
            public Object invoke(Object proxy, final Method method, final Object[] args) throws Throwable {
                Thread thread = new Thread(new Runnable() {
                    public void run() {
                        try {
                            DBInterface background = db.createBackground();
                            try {
                                T target = background.getSimpleTransaction().getInterface(iface);
                                method.invoke(target, args);
                            } finally {
                                background.close();
                            }
                        } catch (InvocationTargetException ex) {
                            db.getLogger().error(ex.getTargetException());
                        } catch (Exception ex) {
                            db.getLogger().error(ex);
                        }
                    }
                });
                thread.start();
                return null;
            }
        });
    }
}
