package tracker.model;

import java.time.LocalDateTime;

public class Subtask extends Task {
    private final Epic epic;

    public Subtask(String name, String description, int id, Epic epic, long duration, LocalDateTime startTime) {
        super(name, description, id, duration, startTime);
        this.epic = epic;
    }

    public Subtask(String name, String description, int id, Status status, Epic epic, long duration,
                   LocalDateTime startTime) {
        super(name, description, id, status, duration, startTime);
        this.epic = epic;
    }

    public Epic getEpic() {
        return epic;
    }

    @Override
    public String toString() {
        return id + "," + TaskType.SUBTASK + "," + name + "," + status + "," + description + "," + duration  + ","
                + startTime.toString() + "," + "," + epic.id;
    }
}
