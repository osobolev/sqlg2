package sqlg2.db.client;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public interface IHttpClient extends Closeable {

    OutputStream toServer() throws IOException;

    InputStream fromServer() throws IOException;
}
