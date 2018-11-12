package sqlg2.db.client;

import sqlg2.db.IDBCommon;
import sqlg2.db.ISimpleTransaction;
import sqlg2.db.remote.HttpCommand;
import sqlg2.db.remote.HttpId;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

class HttpSimpleTransaction implements ISimpleTransaction {

    protected final HttpRootObject rootObject;
    protected final HttpId id;
    private final HttpCommand command;

    HttpSimpleTransaction(HttpRootObject rootObject, HttpId id, HttpCommand command) {
        this.rootObject = rootObject;
        this.id = id;
        this.command = command;
    }

    public final <T extends IDBCommon> T getInterface(final Class<T> iface) {
        return iface.cast(Proxy.newProxyInstance(iface.getClassLoader(), new Class<?>[] {iface}, new InvocationHandler() {
            public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                return rootObject.httpInvoke(method.getGenericReturnType(), command, id, iface, method.getName(), method.getParameterTypes(), args);
            }
        }));
    }
}
