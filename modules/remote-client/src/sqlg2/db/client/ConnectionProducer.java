package sqlg2.db.client;

import sqlg2.db.IRemoteDBInterface;

/**
 * Used for client reconnects.
 */
public interface ConnectionProducer {

    IRemoteDBInterface open() throws Exception;
}
