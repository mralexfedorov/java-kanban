package tracker.server;

import com.google.gson.*;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.LocalDateAdapter;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private final int port;
    final TaskManager taskManager;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;

    private static Gson gson;
    private HttpServer httpServer;

    public HttpTaskServer(int port, TaskManager taskManager) {
        this.port = port;
        this.taskManager = taskManager;
        gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter())
                .create();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        final long MINUTES_IN_DAY = 60 * 24;
        final LocalDateTime TASK_START_TIME = LocalDateTime.now();
        TaskManager inMemoryTaskManager = Managers.getDefault();
        HttpTaskServer taskServer = new HttpTaskServer(8080, inMemoryTaskManager);
        taskServer.start();

        // Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей
        Task task1 = new Task("tracker.model.Task 1", "Do task 1", inMemoryTaskManager.getTaskId(),
                MINUTES_IN_DAY, TASK_START_TIME.minusDays(4));
        inMemoryTaskManager.createTask(task1);
        Task task2 = new Task("tracker.model.Task 2", "Do task 2", inMemoryTaskManager.getTaskId(),
                MINUTES_IN_DAY, TASK_START_TIME.minusDays(3));
        inMemoryTaskManager.createTask(task2);
        Epic epic1 = new Epic("tracker.model.Epic 1", "Do all subtasks from epic 1",
                inMemoryTaskManager.getTaskId());
        inMemoryTaskManager.createEpic(epic1);
        Epic epic2 = new Epic("tracker.model.Epic 2", "Do all subtasks from epic 2",
                inMemoryTaskManager.getTaskId());
        inMemoryTaskManager.createEpic(epic2);
        Subtask subtask1 = new Subtask("tracker.model.Subtask 1", "Do subtask 1", inMemoryTaskManager.getTaskId(),
                epic1, MINUTES_IN_DAY, TASK_START_TIME.minusDays(2));
        inMemoryTaskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("tracker.model.Subtask 2", "Do subtask 2", inMemoryTaskManager.getTaskId(),
                epic1, MINUTES_IN_DAY, TASK_START_TIME.minusDays(1));
        inMemoryTaskManager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask("tracker.model.Subtask 3", "Do subtask 3", inMemoryTaskManager.getTaskId(),
                epic2, MINUTES_IN_DAY, TASK_START_TIME);
        inMemoryTaskManager.createSubtask(subtask3);
    }

    public void start() throws IOException {
        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(port), 0);
        httpServer.createContext("/tasks", new TasksHandler());
        httpServer.createContext("/subtasks", new SubtasksHandler());
        httpServer.createContext("/epics", new EpicsHandler());
        httpServer.createContext("/history", new HistoryHandler());
        httpServer.createContext("/prioritized", new PrioritizedHandler());
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }

    class TasksHandler implements HttpHandler {
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
                        String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                        Task task = gson.fromJson(body, Task.class);
                        try {
                            taskManager.createTask(task);
                        } catch (InterruptedException e) {
                            BaseHttpHandler.sendHasOverlaps(httpExchange);
                        }
                        BaseHttpHandler.sendText(httpExchange, gson.toJson(task), 201);
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

    class SubtasksHandler implements HttpHandler {
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
                        String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                        Subtask task = gson.fromJson(body, Subtask.class);
                        try {
                            taskManager.createSubtask(task);
                        } catch (InterruptedException e) {
                            BaseHttpHandler.sendHasOverlaps(httpExchange);
                        }
                        BaseHttpHandler.sendText(httpExchange, gson.toJson(task), 201);
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

    class EpicsHandler implements HttpHandler {
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
                        Epic epic = gson.fromJson(body, Epic.class);
                        taskManager.createEpic(epic);
                        BaseHttpHandler.sendText(httpExchange, gson.toJson(epic), 201);
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

    class HistoryHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            String method = httpExchange.getRequestMethod();
            String path = httpExchange.getRequestURI().getPath();
            String[] partsOfPath = path.split("/");
            if (method.equals("GET")) {
                if (partsOfPath.length == 2) {
                    BaseHttpHandler.sendText(httpExchange, gson.toJson(taskManager.getHistory()), 200);
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

    class PrioritizedHandler implements HttpHandler {
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
}
