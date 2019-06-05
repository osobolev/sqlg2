package sqlg2.db.client;

import sqlg2.db.IDBCommon;
import sqlg2.db.remote.HttpCommand;
import sqlg2.db.remote.HttpId;
import sqlg2.db.remote.HttpResult;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;

public interface IClientSerializer {

    interface ReqRespConsumer {

        void writeToServer(OutputStream os) throws IOException;

        HttpResult readFromServer(InputStream is) throws IOException;
    }

    interface ReqRespProcessor {

        HttpResult process(ReqRespConsumer consumer) throws IOException;
    }

    HttpResult clientToServer(ReqRespProcessor processor, HttpId id, HttpCommand command,
                              Class<? extends IDBCommon> iface, Type retType, String method, Class<?>[] paramTypes, Object[] params) throws IOException;
}
