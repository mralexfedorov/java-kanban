package controllers;

import model.Epic;
import model.Status;
import model.Subtask;
import model.Task;

import java.util.ArrayList;
import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> tasks;
    private final HashMap<Integer, Subtask> subtasks;
    private final HashMap<Integer, Epic> epics;

    private int taskId;

    public TaskManager() {
        tasks = new HashMap<>();
        subtasks = new HashMap<>();
        epics = new HashMap<>();
        taskId = 1;
    }

    public int getTaskId() {
        return taskId;
    }

    // Получение списка всех задач.
    public ArrayList<Task> getTasks() {
        return new ArrayList<>(tasks.values());
    }
    public ArrayList<Subtask> getSubtasks() {
        return new ArrayList<>(subtasks.values());
    }
    public ArrayList<Epic> getEpics() {
        return new ArrayList<>(epics.values());
    }

    // Удаление всех задач.
    public void deleteAllTasks() {
        tasks.clear();
    }

    private void deleteAllSubtasks() {
        subtasks.clear();
    }

    private void deleteAllEpics() {
        epics.clear();
    }

    // Получение по идентификатору.
    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    public Subtask getSubtaskById(int id) {
        return subtasks.get(id);
    }

    public Epic getEpicById(int id) {
        return epics.get(id);
    }

    // Получение списка всех подзадач определённого эпика
    public ArrayList<Subtask> getEpicsSubtasks(int epicId) {
        ArrayList<Subtask> epicsSubtasks = new ArrayList<>();
        for (Subtask task: subtasks.values()) {
            if (task.getEpic().getId() == epicId) {
                epicsSubtasks.add(task);
            }
        }
        return  epicsSubtasks;
    }

    // Создание. Сам объект должен передаваться в качестве параметра.
    public void createTask(Task task) {
        tasks.put(taskId, task);
        taskId++;
    }

    public void createSubtask(Subtask task) {
        subtasks.put(taskId, task);
        updateEpicStatus(task.getEpic());
        taskId++;
    }

    public void createEpic(Epic task) {
        epics.put(taskId, task);
        taskId++;
    }

    // Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateTask(Task task) {
        Task updatedTask = tasks.get(task.getId());
        if (updatedTask == null) {
            System.out.println("Задача с id = " + task.getId() + " не найдена");
        }

        tasks.put(task.getId(), task);
    }

    public void updateSubtask(Subtask task) {
        Subtask updatedTask = subtasks.get(task.getId());
        if (updatedTask == null) {
            System.out.println("Задача с id = " + task.getId() + " не найдена");
        }

        updateEpicStatus(task.getEpic());
        subtasks.put(task.getId(), task);
    }

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
        epics.put(epic.getId(), epic);
    }

    public void updateEpic(Epic task) {
        Epic updatedTask = epics.get(task.getId());
        if (updatedTask == null) {
            System.out.println("Задача с id = " + task.getId() + " не найдена");
        }

        epics.put(task.getId(), task);
    }

    // Удаление по идентификатору
    public void deleteTask(Task task) {
        tasks.remove(task.getId());
    }

    public void deleteSubtask(Subtask task) {
        updateEpicStatus(task.getEpic());
        subtasks.remove(task.getId());
    }

    public void deleteEpic(Epic epic) {
        ArrayList<Integer> subtasksForDeletion = new ArrayList<>();
        for (Subtask task: subtasks.values()) {
            if (task.getEpic().equals(epic)) {
                subtasksForDeletion.add(task.getId());
            }
        }

        for (int taskId: subtasksForDeletion) {
            subtasks.remove(taskId);
        }
        epics.remove(epic.getId());
    }
}
