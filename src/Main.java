public class Main {

    public static void main(String[] args) {
        System.out.println("Поехали!");
        TaskManager taskManager = new TaskManager();

        // Создайте две задачи, а также эпик с двумя подзадачами и эпик с одной подзадачей
        Task task1 = new Task("Task 1", "Do task 1", taskManager.getTaskId());
        taskManager.createTask(task1);
        Task task2 = new Task("Task 2", "Do task 2", taskManager.getTaskId());
        taskManager.createTask(task2);
        Epic epic1 = new Epic("Epic 1", "Do all subtasks from epic 1", taskManager.getTaskId());
        taskManager.createTask(epic1);
        Epic epic2 = new Epic("Epic 2", "Do all subtasks from epic 2", taskManager.getTaskId());
        taskManager.createTask(epic2);
        Subtask subtask1 = new Subtask("Subtask 1", "Do subtask 1", taskManager.getTaskId(), epic1);
        taskManager.createTask(subtask1);
        Subtask subtask2 = new Subtask("Subtask 2", "Do subtask 2", taskManager.getTaskId(), epic1);
        taskManager.createTask(subtask2);
        Subtask subtask3 = new Subtask("Subtask 3", "Do subtask 3", taskManager.getTaskId(), epic2);
        taskManager.createTask(subtask3);

        // Распечатайте списки эпиков, задач и подзадач через System.out.println(..)
        System.out.println("1. Задачи созданы");
        System.out.println(taskManager.getAllTasks());

        // Измените статусы созданных объектов, распечатайте их. Проверьте,
        // что статус задачи и подзадачи сохранился, а статус эпика рассчитался по статусам подзадач.
        task1.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(task1);
        task2.setStatus(Status.DONE);
        taskManager.updateTask(task2);
        subtask1.setStatus(Status.IN_PROGRESS);
        taskManager.updateTask(subtask1);
        subtask2.setStatus(Status.DONE);
        taskManager.updateTask(subtask2);
        subtask3.setStatus(Status.DONE);
        taskManager.updateTask(subtask3);
        System.out.println("2. Статус задач изменен");
        System.out.println(taskManager.getAllTasks());

        // И, наконец, попробуйте удалить одну из задач и один из эпиков.
        taskManager.deleteTask(task2.getId());
        taskManager.deleteTask(epic1.getId());
        System.out.println("3. Удалена задача и эпик");
        System.out.println(taskManager.getAllTasks());
    }
}
