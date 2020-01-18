package sqlg2.db;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;

final class AsyncTransaction implements ISimpleTransaction {

    private final DBInterface db;

    AsyncTransaction(DBInterface db) {
        this.db = db;
    }

    public <T extends IDBCommon> T getInterface(Class<T> iface) {
        return iface.cast(Proxy.newProxyInstance(iface.getClassLoader(), new Class[] {iface}, (proxy, method, args) -> {
            Thread thread = new Thread(() -> {
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
            });
            thread.start();
            return null;
        }));
    }
}
