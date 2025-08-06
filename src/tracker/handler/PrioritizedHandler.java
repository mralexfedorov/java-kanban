package tracker.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;

import java.io.IOException;

public class PrioritizedHandler extends BaseHttpHandler{
    public PrioritizedHandler(Gson gson, TaskManager taskManager) {
        super(gson, taskManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        String[] partsOfPath = path.split("/");
        if (method.equals("GET")) {
            if (partsOfPath.length == 2) {
                BaseHttpHandler.sendText(httpExchange, gson.toJson(taskManager.getPrioritizedTasks()), 200);
                return;
            } else {
                BaseHttpHandler.sendServerError(httpExchange);
            }
            httpExchange.close();
        } else {
            BaseHttpHandler.sendServerError(httpExchange);
        }
    }
}
