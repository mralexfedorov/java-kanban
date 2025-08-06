package tracker.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.exceptions.OverlapsException;
import tracker.model.Subtask;

import java.io.IOException;
import java.io.InputStream;

public class SubtasksHandler extends BaseHttpHandler {
    public SubtasksHandler(Gson gson, TaskManager taskManager) {
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
                    BaseHttpHandler.sendText(httpExchange, gson.toJson(taskManager.getSubtasks()), 200);
                    return;
                } else if (partsOfPath.length == 3) {
                    int id = Integer.parseInt(partsOfPath[2]);
                    Subtask task = taskManager.getSubtaskById(id);
                    if (task == null) {
                        BaseHttpHandler.sendNotFound(httpExchange);
                    } else {
                        BaseHttpHandler.sendText(httpExchange, gson.toJson(task), 200);
                    }
                } else {
                    BaseHttpHandler.sendServerError(httpExchange);
                }
                httpExchange.close();
            }
            case "POST" -> {
                if (partsOfPath.length == 2) {
                    InputStream inputStream = httpExchange.getRequestBody();
                    String body = new String(inputStream.readAllBytes(), charset);
                    Subtask task = gson.fromJson(body, Subtask.class);
                    if (body.isEmpty()) {
                        BaseHttpHandler.sendServerError(httpExchange);
                    } else {
                        if (task.getId() != 0) {
                            try {
                                taskManager.updateSubtask(task);
                            } catch (OverlapsException e) {
                                BaseHttpHandler.sendHasOverlaps(httpExchange);
                            }
                        } else {
                            try {
                                taskManager.createSubtask(new Subtask(task.getName(), task.getDescription(),
                                        taskManager.getTaskId(), task.getStatus(), task.getEpic(), task.getDuration(),
                                        task.getStartTime()));
                            } catch (OverlapsException e) {
                                BaseHttpHandler.sendHasOverlaps(httpExchange);
                            }
                        }
                        BaseHttpHandler.sendText(httpExchange, gson.toJson(task), 201);
                    }
                    try {
                        taskManager.createSubtask(task);
                    } catch (OverlapsException e) {
                        BaseHttpHandler.sendHasOverlaps(httpExchange);
                    }
                } else {
                    BaseHttpHandler.sendHasOverlaps(httpExchange);
                }
                httpExchange.close();
            }
            case "DELETE" -> {
                if (partsOfPath.length == 3) {
                    int id = Integer.parseInt(partsOfPath[2]);
                    Subtask task = taskManager.getSubtaskById(id);
                    if (task == null) {
                        BaseHttpHandler.sendNotFound(httpExchange);
                    } else {
                        taskManager.deleteSubtask(task);
                        BaseHttpHandler.sendText(httpExchange, gson.toJson(task), 200);
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
