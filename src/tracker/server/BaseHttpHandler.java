package tracker.server;

import com.sun.net.httpserver.HttpExchange;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class BaseHttpHandler {
    public static void sendText(HttpExchange httpExchange, String text, int code) throws IOException {
        byte[] response = text.getBytes(StandardCharsets.UTF_8);
        httpExchange.getResponseHeaders().add("Content-Type", "application/json;charset=utf-8");
        httpExchange.sendResponseHeaders(code, response.length);
        httpExchange.getResponseBody().write(response);
        httpExchange.close();
    }

    public static void sendNotFound(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(404, 0);
        httpExchange.close();
    }

    public static void sendHasOverlaps(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(406, 0);
        httpExchange.close();
    }

    public static void sendServerError(HttpExchange httpExchange) throws IOException {
        httpExchange.sendResponseHeaders(500, 0);
        httpExchange.close();
    }
}
