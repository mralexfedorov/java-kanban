package tracker.controllers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

abstract class TaskManagerTest<T extends TaskManager> {

    TaskManager taskManager;
    final long MINUTES_IN_DAY = 60 * 24;
    final LocalDateTime TASK_START_TIME = LocalDateTime.now();

    @BeforeEach
    void initializeTask() {
        taskManager = Managers.getDefault();
    }
    @Test
    void checkEpic() {
        Task task1 = new Task("Task 1", "Do task 1", taskManager.getTaskId(), MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(4));
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Do task 2", taskManager.getTaskId(), MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(3));
        taskManager.createTask(task2);
        Epic epic1 = new Epic("Epic 1", "Do all subtasks from epic 1", taskManager.getTaskId());
        taskManager.createEpic(epic1);
        Epic epic2 = new Epic("Epic 2", "Do all subtasks from epic 2", taskManager.getTaskId());
        taskManager.createEpic(epic2);
        Subtask subtask1 = new Subtask("Subtask 1", "Do subtask 1", taskManager.getTaskId(), epic1,
                MINUTES_IN_DAY, TASK_START_TIME.minusDays(2));
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Do subtask 2", taskManager.getTaskId(), epic1,
                MINUTES_IN_DAY, TASK_START_TIME.minusDays(1));
        taskManager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask("Subtask 3", "Do subtask 3", taskManager.getTaskId(), epic2,
                MINUTES_IN_DAY, TASK_START_TIME);
        taskManager.createSubtask(subtask3);

        assertEquals(1, taskManager.getTaskById(task1.getId()).get().getId(), "Неверный Id");
        assertEquals(2, taskManager.getTaskById(task2.getId()).get().getId(), "Неверный Id");
        assertEquals(3, taskManager.getEpicById(epic1.getId()).getId(), "Неверный Id");
        assertEquals(4, taskManager.getEpicById(epic2.getId()).getId(), "Неверный Id");
        assertEquals(5, taskManager.getSubtaskById(subtask1.getId()).get().getId(), "Неверный Id");
        assertEquals(6, taskManager.getSubtaskById(subtask2.getId()).get().getId(), "Неверный Id");
        assertEquals(7, taskManager.getSubtaskById(subtask3.getId()).get().getId(), "Неверный Id");
    }

    @Test
    void checkEpicStatus() {
        // Все подзадачи со статусом NEW.
        Epic epic1 = new Epic("Epic 1", "Do all subtasks from epic 1", taskManager.getTaskId());
        taskManager.createEpic(epic1);
        Subtask subtask1 = new Subtask("Subtask 1", "Do subtask 1", taskManager.getTaskId(), epic1,
                MINUTES_IN_DAY, TASK_START_TIME.minusDays(3));
        taskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Do subtask 2", taskManager.getTaskId(), epic1,
                MINUTES_IN_DAY, TASK_START_TIME.minusDays(2));
        taskManager.createSubtask(subtask2);
        assertEquals(Status.NEW, epic1.getStatus(), "Статус эпика должен быть NEW");
        // Подзадачи со статусами NEW и DONE.
        subtask2.setStatus(Status.DONE);
        taskManager.updateEpicStatus(epic1);
        assertEquals(Status.IN_PROGRESS, epic1.getStatus(), "Статус эпика должен быть IN_PROGRESS");
        // Подзадачи со статусом IN_PROGRESS.
        Epic epic2 = new Epic("Epic 2", "Do all subtasks from epic 2", taskManager.getTaskId());
        taskManager.createEpic(epic2);
        Subtask subtask3 = new Subtask("Subtask 3", "Do subtask 3", taskManager.getTaskId(), epic2,
                MINUTES_IN_DAY, TASK_START_TIME.minusDays(1));
        taskManager.createSubtask(subtask3);
        Subtask subtask4 = new Subtask("Subtask 4", "Do subtask 4", taskManager.getTaskId(), epic2,
                MINUTES_IN_DAY, TASK_START_TIME);
        taskManager.createSubtask(subtask4);
        subtask3.setStatus(Status.IN_PROGRESS);
        subtask4.setStatus(Status.IN_PROGRESS);
        taskManager.updateEpicStatus(epic2);
        assertEquals(Status.IN_PROGRESS, epic2.getStatus(), "Статус эпика должен быть IN_PROGRESS");
        // Все подзадачи со статусом DONE.
        subtask1.setStatus(Status.DONE);
        subtask2.setStatus(Status.DONE);
        subtask3.setStatus(Status.DONE);
        subtask4.setStatus(Status.DONE);
        taskManager.updateEpicStatus(epic1);
        taskManager.updateEpicStatus(epic2);
        assertEquals(Status.DONE, epic1.getStatus(), "Статус эпика должен быть DONE");
        assertEquals(Status.DONE, epic2.getStatus(), "Статус эпика должен быть DONE");
    }

    @Test
    void checkTaskIntersection() {
        // При наличии пересечения по времени выполнения задача не будет создана
        Task task1 = new Task("Task 1", "Do task 1", taskManager.getTaskId(), MINUTES_IN_DAY,
                TASK_START_TIME);
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Do task 2", taskManager.getTaskId(), MINUTES_IN_DAY,
                TASK_START_TIME);
        taskManager.createTask(task2);
        assertEquals(1, taskManager.getTaskById(task1.getId()).get().getId(), "Неверный Id");
        assertEquals(1, taskManager.getTasks().size(), "Неверное количество задач");
        task2.setStartTime(TASK_START_TIME.plusDays(1));
        taskManager.createTask(task2);
        assertEquals(2, taskManager.getTaskById(task2.getId()).get().getId(), "Неверный Id");
    }
}
