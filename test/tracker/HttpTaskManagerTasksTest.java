package tracker;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import tracker.controllers.InMemoryTaskManager;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
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

    final long MINUTES_IN_DAY = 60 * 24;
    final LocalDateTime TASK_START_TIME = LocalDateTime.now();

    public HttpTaskManagerTasksTest() throws IOException {
    }

    @BeforeEach
    public void setUp() {
        taskServer.start();
    }

    @AfterEach
    public void shutDown() {
        taskServer.stop();
    }

    @Test
    public void testTasks() throws IOException, InterruptedException {
        String task1Json = "{\"name\":\"Task 1\",\"description\":\"Do task 1\",\"status\":\"NEW\"" +
                ",\"duration\":" + MINUTES_IN_DAY + ",\"startTime\": \"" + TASK_START_TIME.minusDays(4) + "\"}";
        String task2Json = "{\"name\":\"Task 2\",\"description\":\"Do task 2\",\"status\":\"NEW\"" +
                ",\"duration\":" + MINUTES_IN_DAY + ",\"startTime\": \"" + TASK_START_TIME.minusDays(4) + "\"}";
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

        task2Json = "{\"name\":\"Task 2\",\"description\":\"Do task 2\",\"status\":\"NEW\"" +
                ",\"duration\":" + MINUTES_IN_DAY + ",\"startTime\": \"" + TASK_START_TIME.minusDays(3) + "\"}";
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
        String epic1Json = "{\"endTime\":\"1970-01-01T00:00:00\",\"emptyDateTime\":\"1970-01-01T00:00:00\"" +
                ",\"name\":\"Epic 1\",\"description\":\"Do all subtasks from epic 1\",\"id\":1,\"status\":null" +
                ",\"duration\":0,\"startTime\":\"1970-01-01T00:00:00\"}";
        String epic2Json = "{\"endTime\":\"1970-01-01T00:00:00\",\"emptyDateTime\":\"1970-01-01T00:00:00\"" +
                ",\"name\":\"Epic 2\",\"description\":\"Do all subtasks from epic 2\",\"id\":2,\"status\":null" +
                ",\"duration\":0,\"startTime\":\"1970-01-01T00:00:00\"}";
        String subtask1Json = "{\"epic\":{\"endTime\":\"1970-01-01T00:00:00\",\"emptyDateTime\":\"1970-01-01T00:00:00\"" +
                ",\"name\":\"Epic 1\",\"description\":\"Do all subtasks from epic 1\",\"id\":1,\"status\":null" +
                ",\"duration\":0,\"startTime\":\"1970-01-01T00:00:00\"},\"name\":\"Subtask 1\"" +
                ",\"description\":\"Do subtask 1\",\"status\":\"NEW\",\"duration\":" + MINUTES_IN_DAY +
                ",\"startTime\":\"" + TASK_START_TIME.minusDays(3) + "\"}";
        String subtask2Json = "{\"epic\":{\"endTime\":\"1970-01-01T00:00:00\",\"emptyDateTime\":\"1970-01-01T00:00:00\"" +
                ",\"name\":\"Epic 1\",\"description\":\"Do all subtasks from epic 1\",\"id\":1,\"status\":null" +
                ",\"duration\":0,\"startTime\":\"1970-01-01T00:00:00\"},\"name\":\"Subtask 2\"" +
                ",\"description\":\"Do subtask 2\",\"status\":\"NEW\",\"duration\":" + MINUTES_IN_DAY +
                ",\"startTime\":\"" + TASK_START_TIME.minusDays(2) + "\"}";
        String subtask3Json = "{\"epic\":{\"endTime\":\"1970-01-01T00:00:00\",\"emptyDateTime\":\"1970-01-01T00:00:00\"" +
                ",\"name\":\"Epic 2\",\"description\":\"Do all subtasks from epic 2\",\"id\":2,\"status\":null" +
                ",\"duration\":0,\"startTime\":\"1970-01-01T00:00:00\"},\"name\":\"Subtask 3\"" +
                ",\"description\":\"Do subtask 3\",\"status\":\"NEW\",\"duration\":" + MINUTES_IN_DAY +
                ",\"startTime\":\"" + TASK_START_TIME.minusDays(2) + "\"}";

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

        subtask3Json = "{\"epic\":{\"endTime\":\"1970-01-01T00:00:00\",\"emptyDateTime\":\"1970-01-01T00:00:00\"" +
                ",\"name\":\"Epic 2\",\"description\":\"Do all subtasks from epic 2\",\"id\":2,\"status\":null" +
                ",\"duration\":0,\"startTime\":\"1970-01-01T00:00:00\"},\"name\":\"Subtask 3\"" +
                ",\"description\":\"Do subtask 3\",\"status\":\"NEW\",\"duration\":" + MINUTES_IN_DAY +
                ",\"startTime\":\"" + TASK_START_TIME.minusDays(1) + "\"}";
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