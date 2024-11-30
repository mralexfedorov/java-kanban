package test;

import controllers.InMemoryTaskManager;
import controllers.Managers;
import model.Task;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class TaskTest {
    InMemoryTaskManager inMemoryTaskManager;

    @BeforeEach
    void initializeTask() {
        Managers<InMemoryTaskManager> managers = new Managers<>(new InMemoryTaskManager());
        inMemoryTaskManager = managers.getDefault();
    }

    @Test
    void checkTask() {
        Task task1 = new Task("model.Task 1", "Do task 1", 1);
        inMemoryTaskManager.createTask(task1);
        Task task2 = new Task("model.Task 2", "Do task 2", 1);
        inMemoryTaskManager.createTask(task2);

        assertEquals(task1, task2, "Объекты не равны");
    }
}