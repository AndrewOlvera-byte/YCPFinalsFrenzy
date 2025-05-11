package models;

import java.util.*;
import models.QuestDefinition;
import GameEngine.GameEngine;
import java.lang.StringBuilder;
import models.Quest;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.sql.Types;

// Manages quest definitions loaded from data source
public class QuestManager {
    private Map<Integer, QuestDefinition> defs = new HashMap<>();

    /** Load quest definitions from /db/quests.csv into defs map */
    public void loadAll() {
        try (InputStream in = DatabaseInitializer.class.getResourceAsStream("/db/quests.csv");
             BufferedReader reader = new BufferedReader(new InputStreamReader(in))) {
            String header = reader.readLine(); // skip header
            if (header == null) return;
            String line;
            while ((line = reader.readLine()) != null) {
                String[] cols = parseCSVLine(line);
                if (cols.length < 9) continue;
                int questId = Integer.parseInt(cols[0].trim());
                String name = cols[1];
                String description = cols[2];
                QuestDefinition.Trigger trigger = QuestDefinition.Trigger.valueOf(cols[3].trim());
                String triggerTarget = cols[4];
                String targetType = cols[5];
                String targetName = cols[6];
                int targetCount = Integer.parseInt(cols[7].trim());
                int rewardSkillPoints = Integer.parseInt(cols[8].trim());
                QuestDefinition def = new QuestDefinition(
                    questId, name, description,
                    trigger, triggerTarget,
                    targetType, targetName,
                    targetCount, rewardSkillPoints
                );
                defs.put(questId, def);
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to load quests.csv", e);
        }
    }

    /** Parse a CSV line, honoring quotes */
    private static String[] parseCSVLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder sb = new StringBuilder();
        boolean inQuotes = false;
        for (char c : line.toCharArray()) {
            if (c == '"') {
                inQuotes = !inQuotes;
            } else if (c == ',' && !inQuotes) {
                fields.add(sb.toString()); sb.setLength(0);
            } else {
                sb.append(c);
            }
        }
        fields.add(sb.toString());
        return fields.toArray(new String[0]);
    }

    public QuestDefinition get(int id) {
        return defs.get(id);
    }

    public Collection<QuestDefinition> allDefs() {
        return defs.values();
    }

    /**
     * Automatically accept quests matching a trigger event and target.
     * Returns concatenated accept messages or null if none.
     */
    public String checkAndAccept(GameEngine engine, QuestDefinition.Trigger trigger, String targetName) {
        StringBuilder sb = new StringBuilder();
        for (QuestDefinition def : defs.values()) {
            if (def.getTrigger() == trigger
                && def.getTriggerTarget().equalsIgnoreCase(targetName)) {
                // skip if already active or completed
                boolean already = engine.getPlayer().getActiveQuests().stream()
                    .anyMatch(q -> q.getDef().getId() == def.getId())
                  || engine.getPlayer().getCompletedQuests().stream()
                    .anyMatch(q -> q.getDef().getId() == def.getId());
                if (!already) {
                    // accept directly on Player and build the message
                    engine.getPlayer().acceptQuest(def.getId(), this);
                    sb.append("\n<b>Quest accepted:</b> ").append(def.getName());
                }
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
} 