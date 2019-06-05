package sqlg2.db.client;

import java.io.IOException;

public interface IHttpClientFactory {

    IHttpClient getClient() throws IOException;
}
