package test;

import controllers.InMemoryTaskManager;
import controllers.Managers;
import model.Epic;
import model.Subtask;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SubtaskTest {
    InMemoryTaskManager inMemoryTaskManager;

    @BeforeEach
    void initializeTask() {
        Managers<InMemoryTaskManager> managers = new Managers<>(new InMemoryTaskManager());
        inMemoryTaskManager = managers.getDefault();
    }

    @Test
    void checkSubtask() {
        Epic epic1 = new Epic("model.Epic 1", "Do all subtasks from epic 1",
                inMemoryTaskManager.getTaskId());
        inMemoryTaskManager.createEpic(epic1);
        Epic epic2 = new Epic("model.Epic 2", "Do all subtasks from epic 2",
                inMemoryTaskManager.getTaskId());
        inMemoryTaskManager.createEpic(epic2);

        Subtask subtask1 = new Subtask("model.Subtask 1", "Do subtask 1", 1, epic1);
        inMemoryTaskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("model.Subtask 2", "Do subtask 2", 1, epic2);
        inMemoryTaskManager.createSubtask(subtask2);

        assertEquals(subtask1, subtask2, "Объекты не равны");
    }
}