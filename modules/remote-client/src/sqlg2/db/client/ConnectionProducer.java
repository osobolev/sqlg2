package sqlg2.db.client;

import sqlg2.db.IRemoteDBInterface;

public interface ConnectionProducer {

    IRemoteDBInterface open() throws Exception;
}
