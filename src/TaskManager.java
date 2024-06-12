import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;

public class TaskManager {
    private final HashMap<Integer, Task> tasks;
    private int taskId;

    public TaskManager() {
        tasks = new HashMap<>();
        taskId = 1;
    }

    public int getTaskId() {
        return taskId;
    }

    // Получение списка всех задач.
    public Collection<Task> getAllTasks() {
        return tasks.values();
    }

    // Удаление всех задач.
    public void deleteAllTasks() {
        tasks.clear();
    }

    // Получение по идентификатору.
    public Task getTaskById(int id) {
        return tasks.get(id);
    }

    // Получение списка всех подзадач определённого эпика
    public ArrayList<Subtask> getEpicsSubtasks(int epicId) {
        ArrayList<Subtask> epicsSubtasks = new ArrayList<>();
        for (Task task: tasks.values()) {
            if (task instanceof Subtask && ((Subtask) task).getEpic().getId() == epicId) {
                epicsSubtasks.add((Subtask) task);
            }
        }
        return  epicsSubtasks;
    }

    // Создание. Сам объект должен передаваться в качестве параметра.
    public void createTask(Task task) {
        tasks.put(taskId, task);
        taskId++;
    }

    // Обновление. Новая версия объекта с верным идентификатором передаётся в виде параметра.
    public void updateTask(Task task) {
        Task updatedTask = tasks.get(task.getId());
        if (updatedTask == null) {
            System.out.println("Задача с id = " + task.getId() + " не найдена");
        }

        tasks.put(task.getId(), task);

        if (task instanceof Subtask) {
            Epic epic = ((Subtask) task).getEpic();
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

            tasks.put(epic.getId(), epic);
        }
    }

    // Удаление по идентификатору
    public void deleteTask(int id) {
        ArrayList<Integer> tasksForDeletion = new ArrayList<>();
        tasksForDeletion.add(id);
        if (tasks.get(id) instanceof Epic) {
            for (Task task: tasks.values()) {
                if (task instanceof Subtask && ((Subtask) task).getEpic().getId() == id) {
                    if (!tasksForDeletion.contains(task.getId())) {
                        tasksForDeletion.add(task.getId());
                    }
                }
            }
        }
        for (int taskId: tasksForDeletion) {
            tasks.remove(taskId);
        }
    }

    private void deleteAllSubtasks() {
        tasks.clear();
    }
}
