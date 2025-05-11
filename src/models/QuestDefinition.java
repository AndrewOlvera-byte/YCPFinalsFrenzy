package models;

// No-frills quest definition template
public class QuestDefinition {
    // When and on what target the quest is auto-accepted
    public enum Trigger { ON_EXAMINE, ON_KILL, ON_COLLECT, ON_TALK }
    private final Trigger trigger;
    private final String triggerTarget;
    private final int id;
    private final String name;
    private final String description;
    private final String targetType;
    private final String targetName;
    private final int targetCount;
    private final int rewardSkillPoints;

    public QuestDefinition(int id, String name, String description,
                           Trigger trigger, String triggerTarget,
                           String targetType, String targetName,
                           int targetCount, int rewardSkillPoints) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.trigger = trigger;
        this.triggerTarget = triggerTarget;
        this.targetType = targetType;
        this.targetName = targetName;
        this.targetCount = targetCount;
        this.rewardSkillPoints = rewardSkillPoints;
    }

    public int getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getTargetType() { return targetType; }
    public String getTargetName() { return targetName; }
    public int getTargetCount() { return targetCount; }
    public int getRewardSkillPoints() { return rewardSkillPoints; }
    public Trigger getTrigger() { return trigger; }
    public String getTriggerTarget() { return triggerTarget; }
} 