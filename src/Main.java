import tracker.controllers.Managers;
import tracker.controllers.TaskManager;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;

public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager inMemoryTaskManager = Managers.getDefault();

        // Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей
        Task task1 = new Task("tracker.model.Task 1", "Do task 1", inMemoryTaskManager.getTaskId());
        inMemoryTaskManager.createTask(task1);
        Task task2 = new Task("tracker.model.Task 2", "Do task 2", inMemoryTaskManager.getTaskId());
        inMemoryTaskManager.createTask(task2);
        Epic epic1 = new Epic("tracker.model.Epic 1", "Do all subtasks from epic 1", inMemoryTaskManager.getTaskId());
        inMemoryTaskManager.createEpic(epic1);
        Epic epic2 = new Epic("tracker.model.Epic 2", "Do all subtasks from epic 2", inMemoryTaskManager.getTaskId());
        inMemoryTaskManager.createEpic(epic2);
        Subtask subtask1 = new Subtask("tracker.model.Subtask 1", "Do subtask 1",
                inMemoryTaskManager.getTaskId(), epic1);
        inMemoryTaskManager.createSubtask(subtask1);
        Subtask subtask2 = new Subtask("tracker.model.Subtask 2", "Do subtask 2",
                inMemoryTaskManager.getTaskId(), epic1);
        inMemoryTaskManager.createSubtask(subtask2);
        Subtask subtask3 = new Subtask("tracker.model.Subtask 3", "Do subtask 3",
                inMemoryTaskManager.getTaskId(), epic2);
        inMemoryTaskManager.createSubtask(subtask3);

        // Распечатайте списки эпиков, задач и подзадач через System.out.println(..)
        System.out.println("1. Задачи созданы");
        System.out.println(inMemoryTaskManager.getTasks());
        System.out.println(inMemoryTaskManager.getSubtasks());
        System.out.println(inMemoryTaskManager.getEpics());

        // Измените статусы созданных объектов, распечатайте их. Проверьте,
        // что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
        task1.setStatus(Status.IN_PROGRESS);
        inMemoryTaskManager.updateTask(task1);
        task2.setStatus(Status.DONE);
        inMemoryTaskManager.updateTask(task2);
        subtask1.setStatus(Status.IN_PROGRESS);
        inMemoryTaskManager.updateSubtask(subtask1);
        subtask2.setStatus(Status.DONE);
        inMemoryTaskManager.updateSubtask(subtask2);
        subtask3.setStatus(Status.DONE);
        inMemoryTaskManager.updateSubtask(subtask3);
        System.out.println("2. Статус задач изменен");
        System.out.println(inMemoryTaskManager.getTasks());
        System.out.println(inMemoryTaskManager.getSubtasks());
        System.out.println(inMemoryTaskManager.getEpics());

        // И, наконец, попробуйте удалить одну из задач и один из эпиков.
        inMemoryTaskManager.deleteTask(task2);
        inMemoryTaskManager.deleteEpic(epic1);
        System.out.println("3. Удалена задача и эпик");
        System.out.println(inMemoryTaskManager.getTasks());
        System.out.println(inMemoryTaskManager.getSubtasks());
        System.out.println(inMemoryTaskManager.getEpics());
    }
}
