package tracker.model;

import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    TaskManager inMemoryTaskManager;

    @BeforeEach
    void initializeTask() {
        inMemoryTaskManager = Managers.getDefault();
    }

    @Test
    void checkTask() {
        Task task1 = new Task("Task 1", "Do task 1", 1);
        inMemoryTaskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Do task 2", 1);
        inMemoryTaskManager.createTask(task2);

        assertEquals(task1, task2, "Объекты не равны");
    }
}