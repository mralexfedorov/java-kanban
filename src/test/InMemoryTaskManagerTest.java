package test;

import controllers.InMemoryTaskManager;
import controllers.Managers;
import model.Epic;
import model.Subtask;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryTaskManagerTest {
    InMemoryTaskManager inMemoryTaskManager;

    @BeforeEach
    void initializeTask() {
        Managers<InMemoryTaskManager> managers = new Managers<>(new InMemoryTaskManager());
        inMemoryTaskManager = managers.getDefault();
    }

    @Test
    void checkEpic() {
        Task task1 = new Task("model.Task 1", "Do task 1", inMemoryTaskManager.getTaskId());
        inMemoryTaskManager.createTask(task1);
        Task task2 = new Task("model.Task 2", "Do task 2", inMemoryTaskManager.getTaskId());
        inMemoryTaskManager.createTask(task2);
        Epic epic1 = new Epic("model.Epic 1", "Do all subtasks from epic 1", inMemoryTaskManager.getTaskId());
        inMemoryTaskManager.createEpic(epic1);
        Epic epic2 = new Epic("model.Epic 2", "Do all subtasks from epic 2", inMemoryTaskManager.getTaskId());
        inMemoryTaskManager.createEpic(epic2);
        Subtask subtask1 = new Subtask("model.Subtask 1", "Do subtask 1",
                inMemoryTaskManager.getTaskId(), epic1);
        inMemoryTaskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("model.Subtask 2", "Do subtask 2",
                inMemoryTaskManager.getTaskId(), epic1);
        inMemoryTaskManager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask("model.Subtask 3", "Do subtask 3",
                inMemoryTaskManager.getTaskId(), epic2);
        inMemoryTaskManager.createSubtask(subtask3);

        assertEquals(1, task1.getId(), "Неверный Id");
        assertEquals(2, task2.getId(), "Неверный Id");
        assertEquals(3, epic1.getId(), "Неверный Id");
        assertEquals(4, epic2.getId(), "Неверный Id");
        assertEquals(5, subtask1.getId(), "Неверный Id");
        assertEquals(6, subtask2.getId(), "Неверный Id");
        assertEquals(7, subtask3.getId(), "Неверный Id");
    }
}