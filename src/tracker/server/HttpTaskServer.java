package tracker.server;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;

public class HttpTaskServer {
    private final int port;
    final TaskManager taskManager;
    private static final Charset DEFAULT_CHARSET = StandardCharsets.UTF_8;
    private static final Gson gson = new Gson();
    private HttpServer httpServer;

    public HttpTaskServer(int port, TaskManager taskManager) {
        this.port = port;
        this.taskManager = taskManager;
    }

    public static void main(String[] args) throws IOException {
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
                        BaseHttpHandler.sendText(httpExchange, "Получен список задач");
                        try (OutputStream os = httpExchange.getResponseBody()) {
                            os.write(gson.toJson(taskManager.getTasks()).getBytes());
                        }
                        return;
                    } else if (partsOfPath.length == 3) {
                        int id = Integer.parseInt(partsOfPath[2]);
                        Task task = taskManager.getTaskById(id);
                        if (task == null) {
                            BaseHttpHandler.sendNotFound(httpExchange);
                        } else {
                            BaseHttpHandler.sendText(httpExchange, "Задача с id=" + task.getId() + " найдена");
                            try (OutputStream os = httpExchange.getResponseBody()) {
                                os.write(gson.toJson(task).getBytes());
                            }
                        }
                    } else {
                        BaseHttpHandler.sendNotFound(httpExchange);
                    }
                    httpExchange.close();
                }
                case "POST" -> {
                    if (partsOfPath.length == 3) {
                        InputStream inputStream = httpExchange.getRequestBody();
                        String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                        Task task = gson.fromJson(body, Task.class);
                        taskManager.createTask(task);
                        BaseHttpHandler.sendText(httpExchange, "Задача с id=" + task.getId() + " создана");
                    } else {
                        BaseHttpHandler.sendNotFound(httpExchange);
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
                            BaseHttpHandler.sendText(httpExchange, "Задача с id=" + task.getId() + " удалена");
                        }
                    } else {
                        BaseHttpHandler.sendNotFound(httpExchange);
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
                        BaseHttpHandler.sendText(httpExchange, "Получен список подзадач");
                        try (OutputStream os = httpExchange.getResponseBody()) {
                            os.write(gson.toJson(taskManager.getSubtasks()).getBytes());
                        }
                        return;
                    } else if (partsOfPath.length == 3) {
                        int id = Integer.parseInt(partsOfPath[2]);
                        Subtask task = taskManager.getSubtaskById(id);
                        if (task == null) {
                            BaseHttpHandler.sendNotFound(httpExchange);
                        } else {
                            BaseHttpHandler.sendText(httpExchange, "Подзадача с id=" + task.getId() + " найдена");
                            try (OutputStream os = httpExchange.getResponseBody()) {
                                os.write(gson.toJson(task).getBytes());
                            }
                        }
                    } else {
                        BaseHttpHandler.sendNotFound(httpExchange);
                    }
                    httpExchange.close();
                }
                case "POST" -> {
                    if (partsOfPath.length == 3) {
                        InputStream inputStream = httpExchange.getRequestBody();
                        String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                        Subtask task = gson.fromJson(body, Subtask.class);
                        taskManager.createSubtask(task);
                        BaseHttpHandler.sendText(httpExchange, "Подзадача с id=" + task.getId() + " создана");
                    } else {
                        BaseHttpHandler.sendNotFound(httpExchange);
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
                            taskManager.deleteTask(task);
                            BaseHttpHandler.sendText(httpExchange, "Подзадача с id=" + task.getId() + " удалена");
                        }
                    } else {
                        BaseHttpHandler.sendNotFound(httpExchange);
                    }
                    httpExchange.close();
                }
                default -> BaseHttpHandler.sendNotFound(httpExchange);
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
                        BaseHttpHandler.sendText(httpExchange, "Получен список эпиков");
                        try (OutputStream os = httpExchange.getResponseBody()) {
                            os.write(gson.toJson(taskManager.getEpics()).getBytes());
                        }
                        return;
                    } else if (partsOfPath.length == 3) {
                        int id = Integer.parseInt(partsOfPath[2]);
                        Epic epic = taskManager.getEpicById(id);
                        if (epic == null) {
                            BaseHttpHandler.sendNotFound(httpExchange);
                        } else {
                            BaseHttpHandler.sendText(httpExchange, "Эпик с id=" + epic.getId() + " найден");
                            try (OutputStream os = httpExchange.getResponseBody()) {
                                os.write(gson.toJson(epic).getBytes());
                            }
                        }
                    } else if (partsOfPath.length == 4 && partsOfPath[3].equals("subtasks")) {
                        int id = Integer.parseInt(partsOfPath[2]);
                        Epic epic = taskManager.getEpicById(id);
                        if (epic == null) {
                            BaseHttpHandler.sendNotFound(httpExchange);
                        } else {
                            BaseHttpHandler.sendText(httpExchange, "Эпик с id=" + epic.getId() + " найден");
                            try (OutputStream os = httpExchange.getResponseBody()) {
                                os.write(gson.toJson(taskManager.getEpicsSubtasks(id)).getBytes());
                            }
                        }
                    } else {
                        BaseHttpHandler.sendNotFound(httpExchange);
                    }
                    httpExchange.close();
                }
                case "POST" -> {
                    if (partsOfPath.length == 3) {
                        InputStream inputStream = httpExchange.getRequestBody();
                        String body = new String(inputStream.readAllBytes(), DEFAULT_CHARSET);
                        Epic epic = gson.fromJson(body, Epic.class);
                        taskManager.createEpic(epic);
                        BaseHttpHandler.sendText(httpExchange, "Эпик с id=" + epic.getId() + " создан");
                    } else {
                        BaseHttpHandler.sendNotFound(httpExchange);
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
                            BaseHttpHandler.sendText(httpExchange, "Эпик с id=" + epic.getId() + " удален");
                        }
                    } else {
                        BaseHttpHandler.sendNotFound(httpExchange);
                    }
                    httpExchange.close();
                }
                default -> BaseHttpHandler.sendNotFound(httpExchange);
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
                    BaseHttpHandler.sendText(httpExchange, "Получена история просмотра задач");
                    try (OutputStream os = httpExchange.getResponseBody()) {
                        os.write(gson.toJson(taskManager.getHistory()).getBytes());
                    }
                    return;
                } else {
                    BaseHttpHandler.sendNotFound(httpExchange);
                }
                httpExchange.close();
            } else {
                BaseHttpHandler.sendNotFound(httpExchange);
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
                    BaseHttpHandler.sendText(httpExchange, "Получен отсортированный список задач");
                    try (OutputStream os = httpExchange.getResponseBody()) {
                        os.write(gson.toJson(taskManager.getPrioritizedTasks()).getBytes());
                    }
                    return;
                } else {
                    BaseHttpHandler.sendNotFound(httpExchange);
                }
                httpExchange.close();
            } else {
                BaseHttpHandler.sendNotFound(httpExchange);
            }
        }
    }
}
