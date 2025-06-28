package tracker.model;

public class Epic extends Task {
    public Epic(String name, String description, int id) {
        super(name, description, id);
    }

    @Override
    public String toString() {
        return this.getId() + ",EPIC," + this.getName() + "," + this.getStatus() + "," + this.getDescription()  + ",";
    }
}
