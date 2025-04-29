package models;

import java.io.IOException;
import java.util.*;

public class FakeGameDatabase {
    // In-memory maps for lookups
    private final Map<Integer, Item> itemMap = new HashMap<>();
    private final Map<Integer, NPC> npcMap   = new HashMap<>();

    /**
     * Load all Items (+ components) from db/items.csv & db/item_components.csv
     */
    private void loadItems() {
        try (ReadCSV reader = new ReadCSV("db/items.csv")) {
            List<String> row;
            while ((row = reader.next()) != null) {
                int id          = Integer.parseInt(row.get(0));
                String name     = row.get(1);
                int value       = Integer.parseInt(row.get(2));
                int weight      = Integer.parseInt(row.get(3));
                String longDesc = row.get(4);
                String shortDesc= row.get(5);
                String type     = row.get(6);
                Item it;
                switch (type.toUpperCase()) {
                case "UTILITY":
                    int healing      = Integer.parseInt(row.get(7));
                    double dmgMulti  = Double.parseDouble(row.get(8));
                    it = new Utility(value, weight, name, null,
                                     longDesc, shortDesc,
                                     healing, dmgMulti);
                    break;
                case "WEAPON":
                    int attackDmg    = Integer.parseInt(row.get(9));
                    it = new Weapon(value, weight, name, null,
                                    attackDmg,
                                    longDesc, shortDesc);
                    break;
                default:
                    it = new Item(value, weight, name, null,
                                  longDesc, shortDesc);
                }
                itemMap.put(id, it);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load items.csv", e);
        }

        // Now read components
        try (ReadCSV reader = new ReadCSV("db/item_components.csv")) {
            List<String> row;
            while ((row = reader.next()) != null) {
                int id        = Integer.parseInt(row.get(0));
                String comp   = row.get(1);
                Item it       = itemMap.get(id);
                if (it instanceof Weapon) {
                    ((Weapon)it).addComponent(comp);
                } else if (it instanceof Utility) {
                    ((Utility)it).addComponent(comp);
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load item_components.csv", e);
        }
    }

    /**
     * Build and return a List<Room> from:
     *   db/rooms.csv,
     *   db/connections.csv,
     *   db/room_inventory.csv,
     *   db/npcs.csv,
     *   db/npc_inventory.csv,
     *   db/npc_room.csv
     */
    public List<Room> loadAllRooms() {
        loadItems();

        // 1) read rooms base
        List<Room> rooms = new ArrayList<>();
        try (ReadCSV reader = new ReadCSV("db/rooms.csv")) {
            List<String> row;
            while ((row = reader.next()) != null) {
                // columns: room_id,room_name,required_key,long_desc,short_desc
                // we ignore room_id in favor of list-index == room_id-1
                rooms.add(new Room(
                    row.get(1),
                    new Inventory(new ArrayList<>(), 300),
                    new Connections(),
                    new ArrayList<>(),
                    row.get(2),
                    row.get(3),
                    row.get(4)
                ));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load rooms.csv", e);
        }

        // 2) load connections
        try (ReadCSV reader = new ReadCSV("db/connections.csv")) {
            List<String> row;
            while ((row = reader.next()) != null) {
                int fromId = Integer.parseInt(row.get(0)) - 1;
                String dir = row.get(1);
                int toId   = Integer.parseInt(row.get(2)) - 1;
                rooms.get(fromId).getConnections().setConnection(dir, toId);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load connections.csv", e);
        }

        // 3) room inventory
        try (ReadCSV reader = new ReadCSV("db/room_inventory.csv")) {
            List<String> row;
            while ((row = reader.next()) != null) {
                int roomId = Integer.parseInt(row.get(0)) - 1;
                int itemId = Integer.parseInt(row.get(1));
                rooms.get(roomId).addItem(itemMap.get(itemId));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load room_inventory.csv", e);
        }

        // 4) load NPC skeletons
        try (ReadCSV reader = new ReadCSV("db/npcs.csv")) {
            List<String> row;
            while ((row = reader.next()) != null) {
                int    npcId     = Integer.parseInt(row.get(0));
                String name      = row.get(1);
                int    hp        = Integer.parseInt(row.get(2));
                boolean agress   = Boolean.parseBoolean(row.get(3));
                int    dmg       = Integer.parseInt(row.get(4));
                String longDesc  = row.get(5);
                String shortDesc = row.get(6);
                NPC npc = new NPC(name, hp, agress, new String[]{}, dmg,
                                  new Inventory(new ArrayList<>(), 100),
                                  longDesc, shortDesc);
                npcMap.put(npcId, npc);
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load npcs.csv", e);
        }

        // 5) npc inventory
        try (ReadCSV reader = new ReadCSV("db/npc_inventory.csv")) {
            List<String> row;
            while ((row = reader.next()) != null) {
                int npcId  = Integer.parseInt(row.get(0));
                int itemId = Integer.parseInt(row.get(1));
                NPC npc    = npcMap.get(npcId);
                npc.getInventory().addItem(itemMap.get(itemId));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load npc_inventory.csv", e);
        }

        // 6) place NPCs in rooms
        try (ReadCSV reader = new ReadCSV("db/npc_room.csv")) {
            List<String> row;
            while ((row = reader.next()) != null) {
                int npcId   = Integer.parseInt(row.get(0));
                int roomId  = Integer.parseInt(row.get(1)) - 1;
                rooms.get(roomId).getCharacterContainer().add(npcMap.get(npcId));
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load npc_room.csv", e);
        }

        return rooms;
    }

    /**
     * Build and return the single Player (ID=1) from:
     *   db/player.csv,
     *   db/player_inventory.csv
     */
    public Player loadPlayer() {
        Player p = null;

        // 1) read player base
        try (ReadCSV reader = new ReadCSV("db/player.csv")) {
            List<String> row;
            while ((row = reader.next()) != null) {
                int id = Integer.parseInt(row.get(0));
                if (id != 1) {
                    continue;
                }
                String name      = row.get(1);
                int    hp        = Integer.parseInt(row.get(2));
                int    sp        = Integer.parseInt(row.get(3));
                double dm        = Double.parseDouble(row.get(4));
                String longDesc  = row.get(5);
                String shortDesc = row.get(6);
                Inventory inv    = new Inventory(new ArrayList<>(), 30);
                p = new Player(name, hp, sp, inv, longDesc, shortDesc, dm, sp, sp);
                break;
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load player.csv", e);
        }

        if (p == null) {
            throw new IllegalStateException("player.csv did not contain ID=1");
        }

        // 2) fill inventory
        try (ReadCSV reader = new ReadCSV("db/player_inventory.csv")) {
            List<String> row;
            while ((row = reader.next()) != null) {
                int pid    = Integer.parseInt(row.get(0));
                int itemId = Integer.parseInt(row.get(1));
                if (pid == 1) {
                    p.addItem(itemMap.get(itemId));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Failed to load player_inventory.csv", e);
        }

        return p;
    }
} 