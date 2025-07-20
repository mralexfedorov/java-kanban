package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;

class InMemoryTaskManagerTest extends TaskManagerTest<InMemoryTaskManager> {

    @BeforeEach
    void initializeTask() {
        taskManager = new InMemoryTaskManager();
    }
}