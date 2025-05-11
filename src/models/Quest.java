package models;

// No-frills quest instance that tracks progress
public class Quest {
    public enum Status { IN_PROGRESS, COMPLETE }
    private final QuestDefinition def;
    private Status status = Status.IN_PROGRESS;
    private int progress = 0;

    public Quest(QuestDefinition def) {
        this.def = def;
    }

    /** Load a Quest with a given status & progress (for persistence) */
    public Quest(QuestDefinition def, Status status, int progress) {
        this(def);
        this.status   = status;
        this.progress = progress;
    }

    public void advance(String eventType, String eventName, int amount) {
        if (status == Status.IN_PROGRESS
            && def.getTargetType().equals(eventType)
            && def.getTargetName().equals(eventName)) {
            progress = Math.min(def.getTargetCount(), progress + amount);
            if (progress >= def.getTargetCount()) {
                status = Status.COMPLETE;
            }
        }
    }

    public boolean isComplete() {
        return status == Status.COMPLETE;
    }

    public QuestDefinition getDef() {
        return def;
    }

    public Status getStatus() {
        return status;
    }

    public int getProgress() {
        return progress;
    }
} 