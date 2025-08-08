package tracker.handler;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import tracker.controllers.TaskManager;
import tracker.exceptions.OverlapsException;
import tracker.model.Task;

import java.io.IOException;
import java.io.InputStream;

public class TasksHandler extends BaseHttpHandler {
    public TasksHandler(Gson gson, TaskManager taskManager) {
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
                    BaseHttpHandler.sendText(httpExchange, gson.toJson(taskManager.getTasks()), 200);
                    return;
                } else if (partsOfPath.length == 3) {
                    int id = Integer.parseInt(partsOfPath[2]);
                    Task task = taskManager.getTaskById(id);
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
                    if (body.isEmpty()) {
                        BaseHttpHandler.sendServerError(httpExchange);
                    } else {
                        Task task = gson.fromJson(body, Task.class);
                        if (task.getId() != 0) {
                            try {
                                taskManager.updateTask(task);
                            } catch (OverlapsException e) {
                                BaseHttpHandler.sendHasOverlaps(httpExchange);
                            }
                        } else {
                            try {
                                taskManager.createTask(new Task(task.getName(), task.getDescription(),
                                        taskManager.getTaskId(), task.getStatus(), task.getDuration(),
                                        task.getStartTime()));
                            } catch (OverlapsException e) {
                                BaseHttpHandler.sendHasOverlaps(httpExchange);
                            }
                        }
                        BaseHttpHandler.sendText(httpExchange, gson.toJson(task), 201);
                    }
                } else {
                    BaseHttpHandler.sendServerError(httpExchange);
                }
                httpExchange.close();
            }
            case "DELETE" -> {
                if (partsOfPath.length == 3) {
                    int id = Integer.parseInt(partsOfPath[2]);
                    Task task = taskManager.getTaskById(id);
                    if (task == null) {
                        BaseHttpHandler.sendNotFound(httpExchange);
                    } else {
                        taskManager.deleteTask(task);
                        BaseHttpHandler.sendText(httpExchange, gson.toJson(task), 200);
                    }
                } else {
                    BaseHttpHandler.sendServerError(httpExchange);
                }
                httpExchange.close();
            }
            default -> BaseHttpHandler.sendNotFound(httpExchange);
        }
    }
}
