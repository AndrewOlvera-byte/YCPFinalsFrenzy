package models;

import java.sql.*;
import java.util.*;

import GameEngine.GameEngine;


public class ConversationManager {
    private GameEngine engine;
    // maps conversation_id → its tree
    private Map<String, ConversationTree> conversations = new HashMap<>();

    public ConversationManager(GameEngine engine) {
        this.engine = engine;
    }

    /**
     * Call once at startup to load all conversations into memory.
     * Assumes two tables:
     *   conversation_nodes(
     *     conversation_id,
     *     node_id,
     *     message,
     *     become_aggressive,
     *     drop_item,
     *     item_to_drop,
     *     is_root
     *   )
     *   conversation_edges(
     *     conversation_id,
     *     parent_node_id,
     *     input_key,
     *     child_node_id
     *   )
     */
    public void loadConversations() {
        // temp map: key = conversation_id + "::" + node_id → ConversationNode
        Map<String, ConversationNode> nodeMap = new HashMap<>();
        // track root nodes per conversation
        Map<String, List<ConversationNode>> roots = new HashMap<>();

        try (Connection conn = DerbyDatabase.getConnection()) {
            // 1) Load all nodes
            String nodeSql =
                "SELECT conversation_id, node_id, message, " +
                "       become_aggressive, drop_item, item_to_drop, is_root " +
                "  FROM conversation_nodes";
            try (PreparedStatement ps = conn.prepareStatement(nodeSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String convId = rs.getString("conversation_id");
                    String nodeId = rs.getString("node_id");
                    String key    = convId + "::" + nodeId;
                    ConversationNode node = new ConversationNode(rs.getString("message"));
                    node.setBecomeAggressive(rs.getBoolean("become_aggressive"));
                    node.setDropItem(rs.getBoolean("drop_item"));
                    node.setItemToDrop(rs.getInt("item_to_drop"));

                    nodeMap.put(key, node);
                    if (rs.getBoolean("is_root")) {
                        roots.computeIfAbsent(convId, c -> new ArrayList<>()).add(node);
                    }
                }
            }

            // 2) Wire up edges
            String edgeSql =
                "SELECT conversation_id, parent_node_id, input_key, child_node_id " +
                "  FROM conversation_edges";
            try (PreparedStatement ps = conn.prepareStatement(edgeSql);
                 ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String convId    = rs.getString("conversation_id");
                    String parentKey = convId + "::" + rs.getString("parent_node_id");
                    String childKey  = convId + "::" + rs.getString("child_node_id");
                    ConversationNode parent = nodeMap.get(parentKey);
                    ConversationNode child  = nodeMap.get(childKey);
                    if (parent != null && child != null) {
                        parent.addResponse(rs.getString("input_key"), child);
                    }
                }
            }

            // 3) Build a ConversationTree for each conversation_id
            for (Map.Entry<String, List<ConversationNode>> e : roots.entrySet()) {
                String convId         = e.getKey();
                ConversationNode root = e.getValue().get(0);  // assume one root per convo
                conversations.put(convId, new ConversationTree(root));
            }

        } catch (SQLException ex) {
            throw new RuntimeException("Failed to load conversations", ex);
        }
    }

    /** Allow GameEngine.loadData() to inject a fake map. */
    public void setConversations(Map<String,ConversationTree> data) {
        this.conversations = data;
    }

    /**
     * Retrieve the ConversationTree for a given ID (e.g. "curly").
     * @return the tree, or null if no such conversation was loaded.
     */
    public ConversationTree getConversation(String conversationId) {
        return conversations.get(conversationId);
    }
}
