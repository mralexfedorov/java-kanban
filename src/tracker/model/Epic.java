package tracker.model;

import java.time.LocalDateTime;

public class Epic extends Task {

    private LocalDateTime endTime;

    @Override
    public LocalDateTime getEndTime() {
        return endTime;
    }

    public void setEndTime(LocalDateTime endTime) {
        this.endTime = endTime;
    }

    public Epic(String name, String description, int id) {
        super(name, description, id);
        this.duration = 0;
        this.startTime = LocalDateTime.of(1970, 1, 1, 0, 0);
        this.endTime = LocalDateTime.of(1970, 1, 1, 0, 0);
    }

    public Epic(String name, String description, int id, Status status) {
        super(name, description, id, status);
        this.duration = 0;
        this.startTime = LocalDateTime.of(1970, 1, 1, 0, 0);
        this.endTime = LocalDateTime.of(1970, 1, 1, 0, 0);
    }

    public Epic(String name, String description, int id, Status status, long duration, LocalDateTime startTime,
                LocalDateTime endTime) {
        super(name, description, id, status, duration, startTime);
        this.endTime = endTime;
    }

    @Override
    public String toString() {
        return id + "," + TaskType.EPIC + "," + name + "," + status + "," + description + "," + duration  + ","
                + startTime.toString() + "," + endTime.toString() + ",";
    }
}
