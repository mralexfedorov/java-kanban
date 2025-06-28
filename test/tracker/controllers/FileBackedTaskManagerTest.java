package tracker.controllers;

import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;

class FileBackedTaskManagerTest extends InMemoryTaskManagerTest {

    TaskManager taskManager;

    @Override
    @Test
    void checkEpic() {
        try {
            this.taskManager = new FileBackedTaskManager(File.createTempFile("sprint7-", ".csv"));
        } catch (IOException e) {
            System.out.println("Ошибка создания файла");
        }
        super.checkEpic();
    }
}