package tracker.model;

public class Epic extends Task {
    public Epic(String name, String description, int id) {
        super(name, description, id);
    }

    @Override
    public String toString() {
        return id + "," + TaskType.EPIC + "," + name + "," + status + "," + description + ",";
    }
}
