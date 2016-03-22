package sqlg2.db.remote;

import sqlg2.db.IDBCommon;
import sqlg2.db.ISimpleTransaction;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

public class HttpSimpleTransaction extends HttpProxy implements ISimpleTransaction {

    private final HttpCommand command;

    HttpSimpleTransaction(HttpId id, HttpProxy root, HttpCommand command) {
        super(id, root);
        this.command = command;
    }

    @SuppressWarnings("unchecked")
    public final <T extends IDBCommon> T getInterface(final Class<T> iface) {
        return (T) Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] {iface}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return httpInvoke(command, iface, method.getName(), method.getParameterTypes(), args);
            }
        });
    }
}
