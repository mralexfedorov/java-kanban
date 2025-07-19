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
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.TreeSet;

public class FileBackedTaskManager extends InMemoryTaskManager {

    private static final String HEADER = "id,type,name,status,description,duration,startTime,endTime,epic";
    private final File file;

    public FileBackedTaskManager(File file) {
        super();
        this.file = file;
    }

    public static void main(String[] args) {
        final long MINUTES_IN_DAY = 60 * 24;
        final LocalDateTime TASK_START_TIME = LocalDateTime.now();

        // Заведите несколько разных задач, эпиков и подзадач.
        Task task1 = new Task("task 1", "task 1", 1, Status.NEW, MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(4));
        Task task2 = new Task("task 2", "task 2", 2, Status.NEW, MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(3));
        Epic epic1 = new Epic("epic 1", "epic 1", 3);
        Epic epic2 = new Epic("epic 2", "epic 2", 4);
        Subtask subtask1 = new Subtask("subtask 1", "subtask 1", 5, epic1, MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(2));
        Subtask subtask2 = new Subtask("subtask 2", "subtask 2", 6, epic1, MINUTES_IN_DAY,
                TASK_START_TIME.minusDays(1));
        Subtask subtask3 = new Subtask("subtask 3", "subtask 3", 7, epic2, MINUTES_IN_DAY,
                TASK_START_TIME);

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
            fileBackedTasksManager.getSubtaskById(7);
            fileBackedTasksManager.getEpicById(3);
            fileBackedTasksManager.getSubtaskById(5);

            System.out.println(fileBackedTasksManager.getHistory());

            // Создайте новый FileBackedTaskManager-менеджер из этого же файла.
            FileBackedTaskManager fileBackedTasksManagerRestored = FileBackedTaskManager.loadFromFile(tempFile);

            // Проверьте, что все задачи, эпики, подзадачи, которые были в старом менеджере, есть в новом.
            fileBackedTasksManagerRestored.getTaskById(1);
            fileBackedTasksManagerRestored.getTaskById(2);
            fileBackedTasksManagerRestored.getEpicById(3);
            fileBackedTasksManagerRestored.getEpicById(4);
            fileBackedTasksManagerRestored.getSubtaskById(5);
            fileBackedTasksManagerRestored.getSubtaskById(6);
            fileBackedTasksManagerRestored.getSubtaskById(7);

            System.out.println(fileBackedTasksManagerRestored.getHistory());

        } catch (IOException e) {
            System.out.println("Ошибка создания файла");
        }
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
            fileWriter.write(prioritizedTaskstoString(this));
            } catch (IOException ioException) {
                throw new ManagerSaveException("Ошибка при создании файла.");
            }
    }

    public static FileBackedTaskManager loadFromFile(File file) {
        FileBackedTaskManager fileBackedTasksManager = new FileBackedTaskManager(file);
        try {
            Path path = file.toPath();
            if (Files.exists(path)) {
                String[] lines = Files.readString(path).replace(HEADER + "\n","").split("\n");
                for (String line : lines) {
                    if (!line.equals("")) {
                        fromString(line, fileBackedTasksManager);
                    }
                }
                List<String> sortedTasks = sortedTasksFromString(lines[lines.length - 1]);
                for (String element: sortedTasks) {
                    Optional<Task> task = fileBackedTasksManager.getTaskById(Integer.parseInt(element));
                    if (task.isPresent()) {
                        fileBackedTasksManager.getPrioritizedTasks().add(task.get());
                    } else {
                        Optional<Subtask> subtask = fileBackedTasksManager.getSubtaskById(Integer.parseInt(element));
                        subtask.ifPresent(value -> fileBackedTasksManager.getPrioritizedTasks().add(value));
                    }
                }
            }
        } catch (IOException ioException) {
            throw new ManagerSaveException("Ошибка при чтении файла.");
        }

        return fileBackedTasksManager;
    }

    static String prioritizedTaskstoString(FileBackedTaskManager fileBackedTaskManager) {
        StringBuilder result = new StringBuilder();

        TreeSet<Task> prioritizedTasks = fileBackedTaskManager.getPrioritizedTasks();
        for (Task task: prioritizedTasks) {
                if (result.length() == 0) {
                    result.append("\n\n").append(task.getId());
                } else {
                    result.append(",").append(task.getId());
                }
            }

        return result.toString();
    }

    static void fromString(String value, FileBackedTaskManager fileBackedTasksManager) {
        String[] elements = value.split(",");
        int id = Integer.parseInt(elements[0]);
        switch (elements[1]) {
            case "TASK" -> fileBackedTasksManager.tasks.put(id, new Task(elements[2], elements[4], id,
                    Status.valueOf(elements[3]), Long.parseLong(elements[5]), LocalDateTime.parse(elements[6])));
            case "EPIC" -> fileBackedTasksManager.epics.put(id, new Epic(elements[2], elements[4], id,
                    Status.valueOf(elements[3]), Long.parseLong(elements[5]), LocalDateTime.parse(elements[6]),
                    LocalDateTime.parse(elements[7])));
            case "SUBTASK" -> fileBackedTasksManager.subtasks.put(id, new Subtask(elements[2], elements[4], id,
                    Status.valueOf(elements[3]), fileBackedTasksManager.getEpicById(Integer.parseInt(elements[8])),
                    Long.parseLong(elements[5]), LocalDateTime.parse(elements[6])));
        }
        if (id > fileBackedTasksManager.taskId) {
            fileBackedTasksManager.taskId = id;
        }
    }

    static List<String> sortedTasksFromString(String value) {
        return Arrays.asList(value.split(","));
    }
}
