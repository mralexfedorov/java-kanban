package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.File;
import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {
    File file;
    final long MINUTES_IN_DAY = 60 * 24;
    final LocalDateTime TASK_START_TIME = LocalDateTime.now();

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
        Task task1 = new Task("task 1", "task 1", 1, Status.NEW, MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(4));
        Task task2 = new Task("task 2", "task 2", 2, Status.NEW, MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(3));
        Epic epic1 = new Epic("epic 1", "epic 1", 3);
        Epic epic2 = new Epic("epic 2", "epic 2", 4);
        Subtask subtask1 = new Subtask("subtask 1", "subtask 1", 5, epic1, MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(2));
        Subtask subtask2 = new Subtask("subtask 2", "subtask 2", 6, epic1, MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(1));
        Subtask subtask3 = new Subtask("subtask 3", "subtask 3", 7, epic2, MINUTES_IN_DAY,
                TASK_START_TIME);

        taskManager.createTask(task1);
        taskManager.createTask(task2);
        taskManager.createEpic(epic1);
        taskManager.createEpic(epic2);
        taskManager.createSubtask(subtask1);
        taskManager.createSubtask(subtask2);
        taskManager.createSubtask(subtask3);

        TaskManager taskManager1 = FileBackedTaskManager.loadFromFile(file);
        TaskManager taskManager2 = FileBackedTaskManager.loadFromFile(file);

        assertTrue(tasksAreEquals(taskManager1.getTaskById(1).get(), taskManager2.getTaskById(1).get()),
                "Исходная задача не соответствует загруженной");
        assertTrue(tasksAreEquals(taskManager1.getTaskById(2).get(), taskManager2.getTaskById(2).get()),
                "Исходная задача не соответствует загруженной");
        assertTrue(epicsAreEquals(taskManager1.getEpicById(3), taskManager2.getEpicById(3)),
                "Исходная задача не соответствует загруженной");
        assertTrue(epicsAreEquals(taskManager1.getEpicById(4), taskManager2.getEpicById(4)),
                "Исходная задача не соответствует загруженной");
        assertTrue(tasksAreEquals(taskManager1.getSubtaskById(5).get(), taskManager2.getSubtaskById(5).get()),
                "Исходная задача не соответствует загруженной");
        assertTrue(tasksAreEquals(taskManager1.getSubtaskById(6).get(), taskManager2.getSubtaskById(6).get()),
                "Исходная задача не соответствует загруженной");
        assertTrue(tasksAreEquals(taskManager1.getSubtaskById(7).get(), taskManager2.getSubtaskById(7).get()),
                "Исходная задача не соответствует загруженной");


    }

    boolean tasksAreEquals(Task task1, Task task2) {
        return task1.getId() == task2.getId()
                && task1.getName().equals(task2.getName())
                && task1.getDescription().equals(task2.getDescription())
                && task1.getStatus().equals(task2.getStatus())
                && task1.getStartTime().equals(task2.getStartTime())
                && task1.getDuration() == task2.getDuration();
    }

    boolean epicsAreEquals(Epic epic1, Epic epic2) {
        return epic1.getId() == epic2.getId()
                && epic1.getName().equals(epic2.getName())
                && epic1.getDescription().equals(epic2.getDescription())
                && epic1.getStatus().equals(epic2.getStatus())
                && epic1.getStartTime().equals(epic2.getStartTime())
                && epic1.getDuration() == epic2.getDuration()
                && epic1.getEndTime().equals(epic2.getEndTime());
    }
}