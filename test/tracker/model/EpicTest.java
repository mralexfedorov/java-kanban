package tracker.model;

import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class EpicTest {
    TaskManager inMemoryTaskManager;

    @BeforeEach
    void initializeTask() {
        inMemoryTaskManager = Managers.getDefault();
    }

    @Test
    void checkEpic() {
        Epic epic1 = new Epic("Epic 1", "Do all subtasks from epic 1", 1);
        inMemoryTaskManager.createEpic(epic1);
        Epic epic2 = new Epic("Epic 2", "Do all subtasks from epic 2", 1);
        inMemoryTaskManager.createEpic(epic2);
        assertEquals(epic1, epic2, "Объекты не равны");
    }
}