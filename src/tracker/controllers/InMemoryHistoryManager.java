package tracker.controllers;

import tracker.model.Task;

import java.util.ArrayList;

public class InMemoryHistoryManager implements HistoryManager{
    ArrayList<Task> history;

    public InMemoryHistoryManager() {
        history = new ArrayList<>();
    }

    @Override
    public void add(Task task) {
        updateHistory(new Task(task.getName(), task.getDescription(), task.getId()));
    }

    @Override
    public ArrayList<Task> getHistory() {
        return history;
    }

    private void updateHistory(Task task) {
        int MAX_HISTORY_LIST_SIZE = 10;
        if (history.size() >= MAX_HISTORY_LIST_SIZE) {
            history.remove(0);
        }
        history.add(task);
    }
}
