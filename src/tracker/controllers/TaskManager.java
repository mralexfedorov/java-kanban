package tracker.controllers;

import tracker.model.Epic;
import tracker.model.Subtask;
import tracker.model.Task;

import java.util.ArrayList;

public interface TaskManager {
    // Получение списка всех задач.
    int getTaskId();

    ArrayList<Task> getTasks();

    ArrayList<Subtask> getSubtasks();

    ArrayList<Epic> getEpics();

    // Удаление всех задач.
    void deleteAllTasks();

    void deleteAllSubtasks();

    void deleteAllEpics();

    // Получение по идентификатору.
    Task getTaskById(int id);

    Subtask getSubtaskById(int id);

    Epic getEpicById(int id);

    // Получение списка всех подзадач определённого эпика
    ArrayList<Subtask> getEpicsSubtasks(int epicId);

    // Создание. Сам объект должен передаваться в качестве параметра.
    void createTask(Task task);

    void createSubtask(Subtask task);

    void createEpic(Epic task);

    // Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    void updateTask(Task task);

    void updateSubtask(Subtask task);
    void updateEpicStatus(Epic epic);

    void updateEpic(Epic task);

    // Удаление по идентификатору
    void deleteTask(Task task);

    void deleteSubtask(Subtask task);

    void deleteEpic(Epic epic);

    ArrayList<Task> getHistory();
}
