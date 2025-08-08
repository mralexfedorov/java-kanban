package tracker.server;

import com.google.gson.*;
import com.sun.net.httpserver.HttpServer;
import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.handler.*;
import tracker.model.Epic;
import tracker.adapter.LocalDateAdapter;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.time.LocalDateTime;

public class HttpTaskServer {
    final TaskManager taskManager;

    private final HttpServer httpServer;

    public HttpTaskServer(int port, TaskManager taskManager) throws IOException {
        this.taskManager = taskManager;
        Gson gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter())
                .create();

        httpServer = HttpServer.create();
        httpServer.bind(new InetSocketAddress(port), 0);
        httpServer.createContext("/tasks", new TasksHandler(gson, taskManager));
        httpServer.createContext("/subtasks", new SubtasksHandler(gson, taskManager));
        httpServer.createContext("/epics", new EpicsHandler(gson, taskManager));
        httpServer.createContext("/history", new HistoryHandler(gson, taskManager));
        httpServer.createContext("/prioritized", new PrioritizedHandler(gson, taskManager));
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

    public void start() {
        httpServer.start();
    }

    public void stop() {
        httpServer.stop(0);
    }
}
