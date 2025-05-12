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
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.SQLIntegrityConstraintViolationException;
import java.sql.ResultSet;
import models.DerbyDatabase;
import models.Item;

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
                    // Persist acceptance in DB
                    try (Connection conn = DerbyDatabase.getConnection();
                         PreparedStatement ps = conn.prepareStatement(
                             "INSERT INTO player_quests (player_id, quest_id, status, progress) VALUES (?, ?, ?, ?)"
                         )) {
                        ps.setInt(1, engine.getPlayer().getId());
                        ps.setInt(2, def.getId());
                        ps.setString(3, Quest.Status.IN_PROGRESS.name());
                        ps.setInt(4, 0);
                        ps.executeUpdate();
                    } catch (SQLIntegrityConstraintViolationException e) {
                        // ignore FK errors if quest_definition entry is missing
                    } catch (SQLException e) {
                        throw new RuntimeException("Failed to persist auto-accepted quest", e);
                    }
                    sb.append("\n<b>Quest accepted:</b> ").append(def.getName());
                    // Add retroactive completion for kill-based quests if target already slain
                    if (def.getTargetType().equalsIgnoreCase("KILL")) {
                        try (Connection conn2 = DerbyDatabase.getConnection();
                             PreparedStatement ps2 = conn2.prepareStatement(
                                 "SELECT hp FROM NPC WHERE name = ?"
                             )) {
                            ps2.setString(1, def.getTargetName());
                            try (ResultSet rs2 = ps2.executeQuery()) {
                                if (rs2.next() && rs2.getInt("hp") <= 0) {
                                    engine.fireEvent(def.getTargetType(), def.getTargetName(), def.getTargetCount());
                                    sb.append("\n<b>Quest completed:</b> ").append(def.getName());
                                }
                            }
                        } catch (SQLException e) {
                            throw new RuntimeException("Failed to check NPC status for quest", e);
                        }
                    }
                    // Add retroactive completion for collect-based quests if player already has target items
                    if (def.getTargetType().equalsIgnoreCase("COLLECT")) {
                        int have = 0;
                        for (Item it : engine.getPlayer().getInventory().getInventory()) {
                            if (it.getName().equalsIgnoreCase(def.getTargetName())) {
                                have++;
                            }
                        }
                        if (have >= def.getTargetCount()) {
                            engine.fireEvent(def.getTargetType(), def.getTargetName(), def.getTargetCount());
                            sb.append("\n<b>Quest completed:</b> ").append(def.getName());
                        }
                    }
                }
            }
        }
        return sb.length() > 0 ? sb.toString() : null;
    }
} 