package tracker.controllers;

import tracker.exceptions.ManagerSaveException;
import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private static final String HEADER = "id,type,name,status,description,epic";
    private File file;

    public static void main(String[] args) {
        // Заведите несколько разных задач, эпиков и подзадач.
        Task task1 = new Task("task 1", "task 1", 1, Status.NEW);
        Task task2 = new Task("task 2", "task 2", 2, Status.NEW);
        Epic epic1 = new Epic("epic 1", "epic 1", 3);
        Epic epic2 = new Epic("epic 2", "epic 2", 4);
        Subtask subtask1 = new Subtask("subtask 1", "subtask 1", 5, epic1);
        Subtask subtask2 = new Subtask("subtask 2", "subtask 2", 6, epic1);
        Subtask subtask3 = new Subtask("subtask 3", "subtask 3", 7, epic2);

        try {
            File tempFile = File.createTempFile("sprint7-", ".csv");
            FileBackedTaskManager fileBackedTasksManager =
                    new FileBackedTaskManager(tempFile);

            fileBackedTasksManager.createTask(task1);
            fileBackedTasksManager.createTask(task2);
            fileBackedTasksManager.createEpic(epic1);
            fileBackedTasksManager.createEpic(epic2);
            fileBackedTasksManager.createSubtask(subtask1);
            fileBackedTasksManager.createSubtask(subtask2);
            fileBackedTasksManager.createSubtask(subtask3);

            fileBackedTasksManager.getTaskById(1);
            fileBackedTasksManager.getTaskById(2);
            fileBackedTasksManager.getTaskById(7);
            fileBackedTasksManager.getTaskById(3);
            fileBackedTasksManager.getTaskById(5);

            System.out.println(fileBackedTasksManager.getHistory());

            // Создайте новый FileBackedTaskManager-менеджер из этого же файла.
            FileBackedTaskManager fileBackedTasksManagerRestored = new FileBackedTaskManager(tempFile);

            // Проверьте, что все задачи, эпики, подзадачи, которые были в старом менеджере, есть в новом.
            fileBackedTasksManagerRestored.getTaskById(1);
            fileBackedTasksManagerRestored.getTaskById(2);
            fileBackedTasksManagerRestored.getTaskById(3);
            fileBackedTasksManagerRestored.getTaskById(4);
            fileBackedTasksManagerRestored.getTaskById(5);
            fileBackedTasksManagerRestored.getTaskById(6);
            fileBackedTasksManagerRestored.getTaskById(7);

            System.out.println(fileBackedTasksManagerRestored.getHistory());

        } catch (IOException e) {
            System.out.println("Ошибка создания файла");
        }
    }

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
        loadFromFile(this);
    }

    @Override
    public void createTask(Task task) {
        super.createTask(task);
        save();
    }

    @Override
    public void createSubtask(Subtask subtask) {
        super.createSubtask(subtask);
        save();
    }

    @Override
    public void createEpic(Epic task) {
        super.createEpic(task);
        save();
    }

    @Override
    public void updateTask(Task task) {
        super.updateTask(task);
        save();
    }

    @Override
    public void updateSubtask(Subtask task) {
        super.updateSubtask(task);
        save();
    }

    @Override
    public void updateEpic(Epic task) {
        super.updateEpic(task);
        save();
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        super.updateEpicStatus(epic);
        save();
    }

    @Override
    public void deleteTask(Task task) {
        super.deleteTask(task);
        save();
    }

    @Override
    public void deleteSubtask(Subtask task) {
        super.deleteSubtask(task);
        save();
    }

    @Override
    public void deleteEpic(Epic epic) {
        super.deleteEpic(epic);
        save();
    }

    @Override
    public void deleteAllTasks() {
        super.deleteAllTasks();
        save();
    }

    @Override
    public void deleteAllSubtasks() {
        super.deleteAllSubtasks();
        save();
    }

    @Override
    public void deleteAllEpics() {
        super.deleteAllEpics();
        save();
    }

    protected void save() {
        try (Writer fileWriter = new FileWriter(file, false)) {
                fileWriter.write(HEADER);
                for (Task task: this.getTasks()) {
                    fileWriter.write("\n" + task.toString());
                }
                for (Task task: this.getEpics()) {
                    fileWriter.write("\n" + task.toString());
                }
                for (Task task: this.getSubtasks()) {
                    fileWriter.write("\n" + task.toString());
                }
            } catch (IOException ioException) {
                throw new ManagerSaveException("Ошибка при создании файла.");
            }
    }

    static void loadFromFile(FileBackedTaskManager fileBackedTasksManager) {
        try {
            Path path = fileBackedTasksManager.file.toPath();
            if (Files.exists(path)) {
                String[] lines = Files.readString(path).replace(HEADER + "\n","").split("\n");
                for (String line : lines) {
                    if (!line.equals("")) {
                        fromString(line, fileBackedTasksManager);
                    }
                }
            }
        } catch (IOException ioException) {
            throw new ManagerSaveException("Ошибка при чтении файла.");
        }
    }

    static void fromString(String value, FileBackedTaskManager fileBackedTasksManager) {
        String[] elements = value.split(",");
        switch (elements[1]) {
            case "TASK" -> fileBackedTasksManager.createTask(new Task(elements[2], elements[4],
                        Integer.parseInt(elements[0]), Status.valueOf(elements[3])));
            case "EPIC" -> fileBackedTasksManager.createEpic(new Epic(elements[2], elements[4],
                    Integer.parseInt(elements[0])));
            case "SUBTASK" -> fileBackedTasksManager.createSubtask(new Subtask(elements[2], elements[4],
                    Integer.parseInt(elements[0]), fileBackedTasksManager.getEpicById(Integer.parseInt(elements[5]))));
        }
    }
}
