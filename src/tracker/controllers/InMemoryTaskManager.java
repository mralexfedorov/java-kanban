package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Status;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class InMemoryTaskManager implements TaskManager {
    protected final HashMap<Integer, Task> tasks;
    protected final HashMap<Integer, Subtask> subtasks;
    protected final HashMap<Integer, Epic> epics;
    private final HistoryManager inMemoryHistoryManager;
    protected int taskId;

    public InMemoryTaskManager() {
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
        taskId = 1;
        inMemoryHistoryManager = Managers.getDefaultHistory();
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
    public Task getTaskById(int id) {
        Task task = tasks.get(id);
        if (task != null) {
            inMemoryHistoryManager.add(task);
        }
        return task;
    }

    @Override
    public Subtask getSubtaskById(int id) {
        Subtask subtask = subtasks.get(id);
        if (subtask != null) {
            inMemoryHistoryManager.add(subtask);
        }
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
        tasks.put(taskId, task);
        taskId++;
    }

    @Override
    public void createSubtask(Subtask task) {
        subtasks.put(taskId, task);
        updateEpicStatus(task.getEpic());
        taskId++;
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

        tasks.put(task.getId(), task);
    }

    @Override
    public void updateSubtask(Subtask task) {
        Subtask updatedTask = subtasks.get(task.getId());
        if (updatedTask == null) {
            System.out.println("Задача с id = " + task.getId() + " не найдена");
        }

        updateEpicStatus(task.getEpic());
        subtasks.put(task.getId(), task);
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
    }

    @Override
    public void deleteSubtask(Subtask task) {
        inMemoryHistoryManager.remove(task.getId());
        subtasks.remove(task.getId());
        updateEpicStatus(task.getEpic());
    }

    @Override
    public void deleteEpic(Epic epic) {
        ArrayList<Subtask> subtasksForDeletion = getEpicsSubtasks(epic.getId());

        for (Subtask subtask: subtasksForDeletion) {
            inMemoryHistoryManager.remove(subtask.getId());
            subtasks.remove(subtask.getId());
        }
        inMemoryHistoryManager.remove(epic.getId());
        epics.remove(epic.getId());
    }

    @Override
    public List<Task> getHistory() {
        return inMemoryHistoryManager.getHistory();
    }
}
