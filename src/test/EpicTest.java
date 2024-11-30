package test;

import controllers.InMemoryTaskManager;
import controllers.Managers;
import model.Epic;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    InMemoryTaskManager inMemoryTaskManager;

    @BeforeEach
    void initializeTask() {
        Managers<InMemoryTaskManager> managers = new Managers<>(new InMemoryTaskManager());
        inMemoryTaskManager = managers.getDefault();
    }

    @Test
    void checkEpic() {
        Epic epic1 = new Epic("model.Epic 1", "Do all subtasks from epic 1", 1);
        inMemoryTaskManager.createEpic(epic1);
        Epic epic2 = new Epic("model.Epic 2", "Do all subtasks from epic 2", 1);
        inMemoryTaskManager.createEpic(epic2);
        assertEquals(epic1, epic2, "Объекты не равны");
    }
}