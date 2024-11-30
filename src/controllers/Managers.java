package controllers;

public class Managers<T extends TaskManager> {
    private final T manager;

    public Managers(T manager) {
        this.manager = manager;
    }

    public T getDefault() {
        return manager;
    }
}
