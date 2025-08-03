package tracker;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.LocalDateAdapter;
import tracker.model.Subtask;
import tracker.model.Task;
import tracker.server.HttpTaskServer;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

public class HttpTaskManagerTasksTest {

    TaskManager taskManager = new InMemoryTaskManager();
    HttpTaskServer taskServer = new HttpTaskServer(8080, taskManager);
    private static final Gson gson = new GsonBuilder()
                .serializeNulls()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateAdapter())
            .create();
    final long MINUTES_IN_DAY = 60 * 24;
    final LocalDateTime TASK_START_TIME = LocalDateTime.now();

    @BeforeEach
    public void setUp() throws IOException {
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testTasks() throws IOException, InterruptedException {
        Task task1 = new Task("Task 1", "Do task 1", 1, MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(4));
        Task task2 = new Task("Task 2", "Do task 2", 2, MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(4));

        String task1Json = gson.toJson(task1);
        String task2Json = gson.toJson(task2);
        URI urlTasks = URI.create("http://localhost:8080/tasks");

        HttpResponse<String> response;
        HttpRequest request;

        // 1. создаем 1 задачу
        HttpClient client = HttpClient.newHttpClient();
        request = HttpRequest.newBuilder().uri(urlTasks).POST(HttpRequest.BodyPublishers.ofString(task1Json)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());
        List<Task> tasksFromManager = taskManager.getTasks();

        assertNotNull(tasksFromManager, "Список задач пуст");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Task 1", tasksFromManager.get(0).getName(), "Некорректное имя задачи");

        // 2. создаем 2 задачу
        request = HttpRequest.newBuilder().uri(urlTasks).POST(HttpRequest.BodyPublishers.ofString(task2Json)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());

        task2.setStartTime(TASK_START_TIME.minusDays(3));
        task2Json = gson.toJson(task2);
        request = HttpRequest.newBuilder().uri(urlTasks).POST(HttpRequest.BodyPublishers.ofString(task2Json)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        tasksFromManager = taskManager.getTasks();
        assertNotNull(tasksFromManager, "Список задач пуст");
        assertEquals(2, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Task 1", tasksFromManager.get(0).getName(), "Некорректное имя задачи");
        assertEquals("Task 2", tasksFromManager.get(1).getName(), "Некорректное имя задачи");

        // 3. удаляем 1 задачу
        request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/tasks/1")).DELETE()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        tasksFromManager = taskManager.getTasks();
        assertNotNull(tasksFromManager, "Список задач пуст");
        assertEquals(1, tasksFromManager.size(), "Некорректное количество задач");
        assertEquals("Task 2", tasksFromManager.get(0).getName(), "Некорректное имя задачи");

        // 4. удаляем 2 задачу
        request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/tasks/2")).DELETE()
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        tasksFromManager = taskManager.getTasks();
        assertEquals(0, tasksFromManager.size(), "Некорректное количество задач");
    }

    @Test
    public void checkEpicsAndSubtasks() throws IOException, InterruptedException {
        Epic epic1 = new Epic("Epic 1", "Do all subtasks from epic 1", 1);
        Epic epic2 = new Epic("Epic 2", "Do all subtasks from epic 2", 2);
        Subtask subtask1 = new Subtask("Subtask 1", "Do subtask 1", 3, epic1, MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(2));
        Subtask subtask2 = new Subtask("Subtask 2", "Do subtask 2", 4, epic1, MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(1));
        Subtask subtask3 = new Subtask("Subtask 3", "Do subtask 3", 5, epic2, MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(1));

        String epic1Json = gson.toJson(epic1);
        String epic2Json = gson.toJson(epic2);
        String subtask1Json = gson.toJson(subtask1);
        String subtask2Json = gson.toJson(subtask2);
        String subtask3Json = gson.toJson(subtask3);

        URI urlEpics = URI.create("http://localhost:8080/epics");
        URI urlSubtasks = URI.create("http://localhost:8080/subtasks");

        HttpRequest request;
        HttpResponse<String> response;
        // 1. Создаем эпики
        HttpClient client = HttpClient.newHttpClient();
        request = HttpRequest.newBuilder().uri(urlEpics).POST(HttpRequest.BodyPublishers.ofString(epic1Json)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        request = HttpRequest.newBuilder().uri(urlEpics).POST(HttpRequest.BodyPublishers.ofString(epic2Json)).build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Epic> epicsFromManager = taskManager.getEpics();

        assertNotNull(epicsFromManager, "Список задач пуст");
        assertEquals(2, epicsFromManager.size(), "Некорректное количество эпиков");
        assertEquals("Epic 1", epicsFromManager.get(0).getName(), "Некорректное имя эпика");
        assertEquals("Epic 2", epicsFromManager.get(1).getName(), "Некорректное имя эпика");

        // 2. Создаем подзадачи
        request = HttpRequest.newBuilder().uri(urlSubtasks).POST(HttpRequest.BodyPublishers.ofString(subtask1Json))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        request = HttpRequest.newBuilder().uri(urlSubtasks).POST(HttpRequest.BodyPublishers.ofString(subtask2Json)).
                build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        request = HttpRequest.newBuilder().uri(urlSubtasks).POST(HttpRequest.BodyPublishers.ofString(subtask3Json))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(406, response.statusCode());

        subtask3.setStartTime(TASK_START_TIME);
        subtask3Json = gson.toJson(subtask3);
        request = HttpRequest.newBuilder().uri(urlSubtasks).POST(HttpRequest.BodyPublishers.ofString(subtask3Json))
                .build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(201, response.statusCode());

        List<Subtask> subtasksFromManager = taskManager.getSubtasks();

        assertNotNull(subtasksFromManager, "Список задач пуст");
        assertEquals(3, subtasksFromManager.size(), "Некорректное количество подзадач");
        assertEquals("Subtask 1", subtasksFromManager.get(0).getName(), "Некорректное имя подзадачи");
        assertEquals("Subtask 2", subtasksFromManager.get(1).getName(), "Некорректное имя подзадачи");
        assertEquals("Subtask 3", subtasksFromManager.get(2).getName(), "Некорректное имя подзадачи");

        // 3. удаляем подзадачи
        request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/subtasks/3"))
                .DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/subtasks/4"))
                .DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/subtasks/5"))
                    .DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        subtasksFromManager = taskManager.getSubtasks();
        assertEquals(0, subtasksFromManager.size(), "Некорректное количество задач");

        // 4. удаляем эпики
        request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/epics/1"))
                    .DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        request = HttpRequest.newBuilder().uri(URI.create("http://localhost:8080/epics/2"))
                    .DELETE().build();
        response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode());

        epicsFromManager = taskManager.getEpics();
        assertEquals(0, epicsFromManager.size(), "Некорректное количество задач");
    }
}