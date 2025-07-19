package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;

public class InMemoryTaskManager implements TaskManager {
    private final LocalDateTime emptyDateTime = LocalDateTime.of(1970, 1, 1, 0, 0);
    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Subtask> subtasks;
    protected final HashMap<Integer, Epic> epics;
    private final HistoryManager inMemoryHistoryManager;
    protected int taskId;

    private final TreeSet<Task> sortedTasks;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
        taskId = 1;
        inMemoryHistoryManager = Managers.getDefaultHistory();
        Comparator<Task> comparator = (task1, task2) -> {
            LocalDateTime t1 = task1.getStartTime();
            LocalDateTime t2 = task2.getStartTime();
            if (task1.equals(task2)) {
                return 0;
            } else if (t1.isBefore(t2)) {
                return -1;
            } else if (t1.isAfter(t2) || t1.equals(t2)) {
                return 1;
            }

            return 0;
        };
        this.sortedTasks = new TreeSet<>(comparator);
    }

    @Override
    public int getTaskId() {
        return taskId;
    }

    // Получение списка всех задач.
    @Override
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }

    @Override
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }

    @Override
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    // Удаление всех задач.
    @Override
    public void deleteAllTasks() {
        for (Integer taskId: tasks.keySet()) {
            inMemoryHistoryManager.remove(taskId);
        }
        tasks.clear();
    }

    @Override
    public void deleteAllSubtasks() {
        for (Integer subtaskId: subtasks.keySet()) {
            inMemoryHistoryManager.remove(subtaskId);
        }
        subtasks.clear();
    }

    @Override
    public void deleteAllEpics() {
        for (Integer epicId: epics.keySet()) {
            inMemoryHistoryManager.remove(epicId);
        }
        epics.clear();
    }

    // Получение по идентификатору.
    @Override
    public Optional<Task> getTaskById(int id) {
        Optional<Task> task = Optional.ofNullable(tasks.get(id));
        task.ifPresent(inMemoryHistoryManager::add);
        return task;
    }

    @Override
    public Optional<Subtask> getSubtaskById(int id) {
        Optional<Subtask> subtask = Optional.ofNullable(subtasks.get(id));
        subtask.ifPresent(inMemoryHistoryManager::add);
        return subtask;
    }

    @Override
    public Epic getEpicById(int id) {
        Epic epic = epics.get(id);
        if (epic != null) {
            inMemoryHistoryManager.add(epic);
        }
        return epic;
    }

    // Получение списка всех подзадач определённого эпика
    @Override
    public ArrayList<Subtask> getEpicsSubtasks(int epicId) {
        ArrayList<Subtask> epicsSubtasks = new ArrayList<>();
        for (Subtask task: subtasks.values()) {
            if (task.getEpic().getId() == epicId) {
                epicsSubtasks.add(task);
            }
        }
        return epicsSubtasks;
    }

    // Создание. Сам объект должен передаваться в качестве параметра.
    @Override
    public void createTask(Task task) {
        if (noIntersectionWithTasks(task) && noIntersectionWithSubtasks(task)) {
            tasks.put(taskId, task);
            sortedTasks.add(task);
            taskId++;
        } else {
            System.out.println("Пересечение с текущими задачами");
        }
    }

    @Override
    public void createSubtask(Subtask task) {
        if (noIntersectionWithTasks(task) && noIntersectionWithSubtasks(task)) {
            subtasks.put(taskId, task);
            sortedTasks.add(task);
            updateEpicStatus(task.getEpic());
            updateEpicEndTimeAndDuration(task.getEpic());
            taskId++;
        } else {
            System.out.println("Пересечение с текущими задачами.");
        }
    }

    @Override
    public void createEpic(Epic task) {
        epics.put(taskId, task);
        taskId++;
    }

    // Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    @Override
    public void updateTask(Task task) {
        Task updatedTask = tasks.get(task.getId());
        if (updatedTask == null) {
            System.out.println("Задача с id = " + task.getId() + " не найдена");
        }
        sortedTasks.remove(updatedTask);
        if (noIntersectionWithTasks(task) && noIntersectionWithSubtasks(task)) {
            tasks.put(task.getId(), task);
            sortedTasks.add(task);
        } else {
            System.out.println("Пересечение с текущими задачами");
        }
    }

    @Override
    public void updateSubtask(Subtask task) {
        Subtask updatedTask = subtasks.get(task.getId());
        if (updatedTask == null) {
            System.out.println("Задача с id = " + task.getId() + " не найдена");
        }
        sortedTasks.remove(updatedTask);
        if (noIntersectionWithTasks(task) && noIntersectionWithSubtasks(task)) {
            subtasks.put(task.getId(), task);
            sortedTasks.add(task);
            updateEpicStatus(task.getEpic());
            updateEpicEndTimeAndDuration(task.getEpic());
        } else {
            System.out.println("Пересечение с текущими задачами");
        }
    }

    @Override
    public void updateEpicStatus(Epic epic) {
        ArrayList<Subtask> epicsSubtasks =  getEpicsSubtasks(epic.getId());
        int numberOfNewTasks = 0;
        int numberOfDoneTasks = 0;
        for (Subtask subtask: epicsSubtasks) {
            if (subtask.getStatus() == Status.NEW) {
                numberOfNewTasks++;
            } else if (subtask.getStatus() == Status.DONE) {
                numberOfDoneTasks++;
            }
        }

        if (numberOfNewTasks == epicsSubtasks.size()) {
            epic.setStatus(Status.NEW);
        } else if (numberOfDoneTasks == epicsSubtasks.size()) {
            epic.setStatus(Status.DONE);
        } else {
            epic.setStatus(Status.IN_PROGRESS);
        }
    }

    void updateEpicEndTimeAndDuration(Epic epic) {
        LocalDateTime epicStartTime = emptyDateTime;
        LocalDateTime epicEndTime = emptyDateTime;
        ArrayList<Subtask> epicSubtasks = getEpicsSubtasks(epic.getId());
        for (Subtask subtask: epicSubtasks) {
            if (epicStartTime.isEqual(emptyDateTime)) {
                epicStartTime = subtask.getStartTime();
            } else if (!subtask.getStartTime().isEqual(emptyDateTime)
                    && subtask.getStartTime().isBefore(epicStartTime)) {
                epicStartTime = subtask.getStartTime();
            }

            if (epicEndTime.isEqual(emptyDateTime)) {
                epicEndTime = subtask.getEndTime();
            } else if (!subtask.getEndTime().isEqual(emptyDateTime)
                    && subtask.getEndTime().isAfter(epicEndTime)) {
                epicEndTime = subtask.getEndTime();
            }
        }

        epic.setStartTime(epicStartTime);
        epic.setDuration(Duration.between(epicStartTime, epicEndTime).toMinutes());
        epic.setEndTime(epicEndTime);
    }

    @Override
    public void updateEpic(Epic task) {
        Epic updatedTask = epics.get(task.getId());
        if (updatedTask == null) {
            System.out.println("Задача с id = " + task.getId() + " не найдена");
        }
        epics.put(task.getId(), task);
    }

    // Удаление по идентификатору
    @Override
    public void deleteTask(Task task) {
        inMemoryHistoryManager.remove(task.getId());
        tasks.remove(task.getId());
        sortedTasks.remove(task);
    }

    @Override
    public void deleteSubtask(Subtask task) {
        inMemoryHistoryManager.remove(task.getId());
        subtasks.remove(task.getId());
        sortedTasks.remove(task);
        updateEpicStatus(task.getEpic());
        updateEpicEndTimeAndDuration(task.getEpic());
    }

    @Override
    public void deleteEpic(Epic epic) {
        ArrayList<Subtask> subtasksForDeletion = getEpicsSubtasks(epic.getId());

        for (Subtask subtask: subtasksForDeletion) {
            inMemoryHistoryManager.remove(subtask.getId());
            subtasks.remove(subtask.getId());
            sortedTasks.remove(subtask);
        }
        inMemoryHistoryManager.remove(epic.getId());
        epics.remove(epic.getId());
    }

    @Override
    public List<Task> getHistory() {
        return inMemoryHistoryManager.getHistory();
    }

    @Override
    public TreeSet<Task> getPrioritizedTasks() {
        return sortedTasks;
    }

    private <T extends Task> boolean intersectionChecked(T task1, T task2) {
        LocalDateTime endTime1 = task1.getStartTime().plusMinutes(task1.getDuration()).minusSeconds(1);
        LocalDateTime endTime2 = task2.getStartTime().plusMinutes(task2.getDuration()).minusSeconds(1);
        return (task1.getStartTime().isBefore(task2.getStartTime()) || task1.getStartTime().isEqual(task2.getStartTime()))
                && (endTime1.isAfter(task2.getStartTime()) || endTime1.isEqual(task2.getStartTime()))
                || (task1.getStartTime().isBefore(endTime2) || task1.getStartTime().isEqual(endTime2))
                && (endTime1.isAfter(endTime2) || endTime1.isEqual(endTime2));
    }

    private <T extends Task> boolean noIntersectionWithTasks(T task) {
        List<Task> intersectedTasks = tasks.values().stream()
                .filter(currentTask -> intersectionChecked(task, currentTask))
                .toList();
        return intersectedTasks.isEmpty();
    }

    private <T extends Task> boolean noIntersectionWithSubtasks(T task) {
        List<Subtask> intersectedTasks = subtasks.values().stream()
                .filter(currentTask -> intersectionChecked(task, currentTask))
                .toList();
        return intersectedTasks.isEmpty();
    }
}

