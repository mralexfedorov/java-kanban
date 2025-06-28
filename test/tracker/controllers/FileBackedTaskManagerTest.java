package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {
    File file;

    @Override
    @BeforeEach
    void initializeTask() {
        try {
            file = File.createTempFile("sprint7-", ".csv");
        } catch (IOException e) {
            System.out.println("Ошибка создания файла");
        }

        taskManager = FileBackedTaskManager.loadFromFile(file);
    }

    @Override
    @Test
    void checkEpic() {
        super.checkEpic();
    }

    @Test
    void checkImportFromEmptyFile() {
        taskManager = FileBackedTaskManager.loadFromFile(file);
        assertEquals(0, taskManager.getTasks().size(), "Список задач не пустой");
        assertEquals(0, taskManager.getEpics().size(), "Список эпиков не пустой");
        assertEquals(0, taskManager.getSubtasks().size(), "Список подзадач не пустой");
    }

    @Test
    void checkImportFromFile() {
        Task task1 = new Task("task 1", "task 1", 1, Status.NEW);
        Task task2 = new Task("task 2", "task 2", 2, Status.NEW);
        Epic epic1 = new Epic("epic 1", "epic 1", 3);
        Epic epic2 = new Epic("epic 2", "epic 2", 4);
        Subtask subtask1 = new Subtask("subtask 1", "subtask 1", 5, epic1);
        Subtask subtask2 = new Subtask("subtask 2", "subtask 2", 6, epic1);
        Subtask subtask3 = new Subtask("subtask 3", "subtask 3", 7, epic2);

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);

        TaskManager taskManager1 = FileBackedTaskManager.loadFromFile(file);
        TaskManager taskManager2 = FileBackedTaskManager.loadFromFile(file);

        assertTrue(tasksAreEquals(taskManager1.getTaskById(1), taskManager2.getTaskById(1)),
                "Исходная задача не соответствует загруженной");
        assertTrue(tasksAreEquals(taskManager1.getTaskById(2), taskManager2.getTaskById(2)),
                "Исходная задача не соответствует загруженной");
        assertTrue(tasksAreEquals(taskManager1.getEpicById(3), taskManager2.getEpicById(3)),
                "Исходная задача не соответствует загруженной");
        assertTrue(tasksAreEquals(taskManager1.getEpicById(4), taskManager2.getEpicById(4)),
                "Исходная задача не соответствует загруженной");
        assertTrue(tasksAreEquals(taskManager1.getSubtaskById(5), taskManager2.getSubtaskById(5)),
                "Исходная задача не соответствует загруженной");
        assertTrue(tasksAreEquals(taskManager1.getSubtaskById(6), taskManager2.getSubtaskById(6)),
                "Исходная задача не соответствует загруженной");
        assertTrue(tasksAreEquals(taskManager1.getSubtaskById(7), taskManager2.getSubtaskById(7)),
                "Исходная задача не соответствует загруженной");
    }

    boolean tasksAreEquals(Task task1, Task task2) {
        return task1.getId() == task2.getId()
                && task1.getName().equals(task2.getName())
                && task1.getDescription().equals(task2.getDescription())
                && task1.getStatus().equals(task2.getStatus());
    }
}