# YCP Finals Frenzy Game

A text-based adventure game set at York College of Pennsylvania.

## Database Persistence

This project uses Apache Derby for database persistence. Game objects (rooms, players, items, etc.) are stored in the database and loaded at runtime.

### CSV Data Structure

Initial game data is loaded from CSV files in the `src/csv` directory:

- `players.csv`: Contains player data
- `rooms.csv`: Contains room data
- `items.csv`: Contains item data
- `characters.csv`: Contains character data
- `connections.csv`: Contains room connection data

### Database Setup

1. Run the `Main` class to initialize the database
2. This will create the tables and load the initial data from CSV files

### Database Schema

The database schema consists of the following tables:

- `players`: Stores player information
- `rooms`: Stores room information
- `items`: Stores item information
- `weapon_properties`: Stores weapon-specific properties
- `utility_properties`: Stores utility-specific properties
- `characters`: Stores character information
- `connections`: Stores room connections
- `conversation_nodes`: Stores NPC conversation nodes
- `conversation_responses`: Stores NPC conversation responses

### Foreign Key Relationships

- Items reference their container (room or character) via `container_id`
- Characters reference their location via `room_id`
- Connections reference rooms via `room_id` and `connected_room_id`
- Conversation nodes reference NPCs via `npc_id`
- Conversation responses reference nodes via `parent_node_id` and `child_node_id`