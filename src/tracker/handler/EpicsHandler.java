package tracker.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.model.Epic;

import java.io.IOException;
import java.io.InputStream;

public class EpicsHandler extends BaseHttpHandler{
    public EpicsHandler(Gson gson, TaskManager taskManager) {
        super(gson, taskManager);
    }

    @Override
    public void handle(HttpExchange httpExchange) throws IOException {
        String method = httpExchange.getRequestMethod();
        String path = httpExchange.getRequestURI().getPath();
        String[] partsOfPath = path.split("/");
        switch (method) {
            case "GET" -> {
                if (partsOfPath.length == 2) {
                    BaseHttpHandler.sendText(httpExchange, gson.toJson(taskManager.getEpics()), 200);
                    return;
                } else if (partsOfPath.length == 3) {
                    int id = Integer.parseInt(partsOfPath[2]);
                    Epic epic = taskManager.getEpicById(id);
                    if (epic == null) {
                        BaseHttpHandler.sendNotFound(httpExchange);
                    } else {
                        BaseHttpHandler.sendText(httpExchange, gson.toJson(epic), 200);
                    }
                } else if (partsOfPath.length == 4 && partsOfPath[3].equals("subtasks")) {
                    int id = Integer.parseInt(partsOfPath[2]);
                    Epic epic = taskManager.getEpicById(id);
                    if (epic == null) {
                        BaseHttpHandler.sendNotFound(httpExchange);
                    } else {
                        BaseHttpHandler.sendText(httpExchange, gson.toJson(taskManager.getEpicsSubtasks(id)),
                                200);
                    }
                } else {
                    BaseHttpHandler.sendServerError(httpExchange);
                }
                httpExchange.close();
            }
            case "POST" -> {
                if (partsOfPath.length == 2) {
                    InputStream inputStream = httpExchange.getRequestBody();
                    String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                    if (body.isEmpty()) {
                        BaseHttpHandler.sendServerError(httpExchange);
                    } else {
                        Epic epic = gson.fromJson(body, Epic.class);
                        taskManager.createEpic(epic);
                        BaseHttpHandler.sendText(httpExchange, gson.toJson(epic), 201);
                    }
                } else {
                    BaseHttpHandler.sendServerError(httpExchange);
                }
                httpExchange.close();
            }
            case "DELETE" -> {
                if (partsOfPath.length == 3) {
                    int id = Integer.parseInt(partsOfPath[2]);
                    Epic epic = taskManager.getEpicById(id);
                    if (epic == null) {
                        BaseHttpHandler.sendNotFound(httpExchange);
                    } else {
                        taskManager.deleteEpic(epic);
                        BaseHttpHandler.sendText(httpExchange, gson.toJson(epic), 200);
                    }
                } else {
                    BaseHttpHandler.sendServerError(httpExchange);
                }
                httpExchange.close();
            }
            default -> BaseHttpHandler.sendServerError(httpExchange);
        }
    }
}
