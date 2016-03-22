package remote;

import java.io.*;
import javax.servlet.http.*;

import sqlg2.db.HttpDispatcher;

public final class SqlgServlet extends HttpServlet {

    private HttpDispatcher http;

    public SqlgServlet(HttpDispatcher http) {
        this.http = http;
    }

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        http.dispatch(request.getRemoteHost(), request.getInputStream(), response.getOutputStream());
    }
}
