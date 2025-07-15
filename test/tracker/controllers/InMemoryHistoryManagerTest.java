package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

class InMemoryHistoryManagerTest {
    TaskManager taskManager;
    final long MINUTES_IN_DAY = 60 * 24;
    final LocalDateTime TASK_START_TIME = LocalDateTime.now();

    @BeforeEach
    void initializeTask() {
        taskManager = Managers.getDefault();
    }
    @Test
    void checkInMemoryHistoryManager() {
        Task task1 = new Task("Task 1", "Do task 1", taskManager.getTaskId(), MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(4));
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Do task 2", taskManager.getTaskId(), MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(3));
        taskManager.createTask(task2);
        Epic epic1 = new Epic("Epic 1", "Do all subtasks from epic 1",
                taskManager.getTaskId());
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Epic 2", "Do all subtasks from epic 2",
                taskManager.getTaskId());
        taskManager.createEpic(epic2);
        Subtask subtask1 = new Subtask("Subtask 1", "Do subtask 1",
                taskManager.getTaskId(), epic1, MINUTES_IN_DAY, TASK_START_TIME.minusDays(2));
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Do subtask 2",
                taskManager.getTaskId(), epic1, MINUTES_IN_DAY, TASK_START_TIME.minusDays(1));
        taskManager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask("Subtask 3", "Do subtask 3",
                taskManager.getTaskId(), epic2, MINUTES_IN_DAY, TASK_START_TIME);
        taskManager.createSubtask(subtask3);

        taskManager.getTaskById(task1.getId());
        taskManager.getTaskById(task2.getId());
        taskManager.getEpicById(epic1.getId());
        taskManager.getEpicById(epic2.getId());
        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getSubtaskById(subtask2.getId());
        taskManager.getSubtaskById(subtask3.getId());
        
        task1.setName("Task 1 (upd)");
        
        taskManager.getTaskById(task1.getId());

        assertEquals("Task 2", taskManager.getHistory().get(0).getName());
        assertEquals("Task 1 (upd)", taskManager.getHistory().get(6).getName());

        taskManager.getTaskById(task2.getId());
        taskManager.getEpicById(epic1.getId());
        taskManager.getEpicById(epic2.getId());
        taskManager.getSubtaskById(subtask1.getId());
        taskManager.getSubtaskById(subtask2.getId());
        taskManager.getSubtaskById(subtask3.getId());

        assertEquals(7, taskManager.getHistory().size());

        taskManager.deleteTask(task1);
        taskManager.deleteSubtask(subtask1);
        taskManager.deleteEpic(epic1);

        assertEquals(3, taskManager.getHistory().size());

        taskManager.deleteAllTasks();

        assertEquals(2, taskManager.getHistory().size());
    }
}