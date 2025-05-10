-- 1) Core entity tables
-- in schema.sql, either…
CREATE TABLE PLAYER (
  player_id INT PRIMARY KEY,
  name VARCHAR(100),
  hp INT,
  skill_points INT,
  damage_multi DOUBLE,
  long_description VARCHAR(1000),
  short_description VARCHAR(500),
  player_type VARCHAR(20),
  attack_boost INT,
  defense_boost INT
);
-- 6) Equipment linkage
CREATE TABLE ITEM (
  item_id           INT          PRIMARY KEY,
  name              VARCHAR(255),
  value             INT,
  weight            INT,
  long_description  VARCHAR(1024),
  short_description VARCHAR(255),
  type              VARCHAR(50),   -- 'WEAPON','UTILITY','ARMOR'
  healing           INT,           -- how much HP it restores
  damage_multi      DOUBLE,        -- multiplier for utilities
  attack_damage     INT,           -- for weapons
  attack_boost      INT,           -- bonus to attack for armor
  defense_boost     INT,           -- bonus to defense for armor
  slot              VARCHAR(16),   -- HEAD, TORSO, LEGS or ACCESSORY
  disassemblable    BOOLEAN DEFAULT FALSE
);




CREATE TABLE ROOM (
  room_id        INT PRIMARY KEY,
  room_name      VARCHAR(255),
  required_key   VARCHAR(255),
  long_description   VARCHAR(1024),
  short_description  VARCHAR(255)
);

CREATE TABLE NPC (
  npc_id         INT PRIMARY KEY,
  name           VARCHAR(255),
  hp             INT,
  aggression     BOOLEAN,
  damage         INT,
  long_description   VARCHAR(1024),
  short_description  VARCHAR(255)
);

-- 2) Component / mapping tables that reference only the above
CREATE TABLE ITEM_COMPONENT (
  item_id    INT,
  component  VARCHAR(255),
  PRIMARY KEY (item_id, component),
  FOREIGN KEY (item_id) REFERENCES ITEM(item_id)
);

CREATE TABLE ROOM_CONNECTIONS (
  from_room_id  INT,
  direction     VARCHAR(50),
  to_room_id    INT,
  PRIMARY KEY (from_room_id, direction),
  FOREIGN KEY (from_room_id) REFERENCES ROOM(room_id),
  FOREIGN KEY (to_room_id)   REFERENCES ROOM(room_id)
);

-- 3) Inventory and linkage tables
CREATE TABLE NPC_ROOM (
  npc_id     INT,
  room_id    INT,
  PRIMARY KEY (npc_id, room_id),
  FOREIGN KEY (npc_id)     REFERENCES NPC(npc_id),
  FOREIGN KEY (room_id)    REFERENCES ROOM(room_id)
);

CREATE TABLE ROOM_INVENTORY (
  room_id    INT,
  item_id    INT,
  PRIMARY KEY (room_id, item_id),
  FOREIGN KEY (room_id)    REFERENCES ROOM(room_id),
  FOREIGN KEY (item_id)    REFERENCES ITEM(item_id)
);

CREATE TABLE NPC_INVENTORY (
  npc_id     INT,
  item_id    INT,
  PRIMARY KEY (npc_id, item_id),
  FOREIGN KEY (npc_id)     REFERENCES NPC(npc_id),
  FOREIGN KEY (item_id)    REFERENCES ITEM(item_id)
);

CREATE TABLE PLAYER_INVENTORY (
  player_id  INT,
  item_id    INT,
  PRIMARY KEY (player_id, item_id),
  FOREIGN KEY (player_id)  REFERENCES PLAYER(player_id),
  FOREIGN KEY (item_id)    REFERENCES ITEM(item_id)
);

-- 4) Game state persistence
CREATE TABLE GAME_STATE (
  state_id      INT        PRIMARY KEY,
  current_room  INT,
  player_hp     INT,
  damage_multi  DOUBLE,
  running_message CLOB,    -- Adding column for runningMessage persistence
  last_saved    TIMESTAMP
);

-- 5) Conversation node then edges
CREATE TABLE conversation_nodes (
  conversation_id     VARCHAR(100)   NOT NULL,
  node_id             VARCHAR(100)   NOT NULL,
  is_root             BOOLEAN        DEFAULT FALSE,
  message             VARCHAR(4000),
  become_aggressive   BOOLEAN        DEFAULT FALSE,
  drop_item           BOOLEAN        DEFAULT FALSE,
  item_to_drop        INT            DEFAULT 0,
  PRIMARY KEY (conversation_id, node_id)
);

CREATE TABLE conversation_edges (
  conversation_id     VARCHAR(100)   NOT NULL,
  parent_node_id      VARCHAR(100)   NOT NULL,
  input_key           VARCHAR(4000)  NOT NULL,
  child_node_id       VARCHAR(100)   NOT NULL,
  FOREIGN KEY (conversation_id, parent_node_id)
    REFERENCES conversation_nodes(conversation_id, node_id)
    ON DELETE CASCADE,
  FOREIGN KEY (conversation_id, child_node_id)
    REFERENCES conversation_nodes(conversation_id, node_id)
    ON DELETE CASCADE
);
CREATE TABLE PLAYER_EQUIPMENT (
  player_id   INT         NOT NULL,
  slot        VARCHAR(16) NOT NULL,   -- e.g. 'HEAD','TORSO','LEGS','ACCESSORY'
  armor_id    INT         NOT NULL,
  PRIMARY KEY (player_id, slot),
  FOREIGN KEY (player_id) REFERENCES PLAYER(player_id),
  FOREIGN KEY (armor_id)  REFERENCES ITEM(item_id)
);
CREATE TABLE users (
  user_id INT 
    GENERATED ALWAYS AS IDENTITY
      (START WITH 1, INCREMENT BY 1),
  username VARCHAR(50) NOT NULL UNIQUE,
  password VARCHAR(255) NOT NULL,
  email    VARCHAR(255) NOT NULL UNIQUE,
  current_player_saves INT NOT NULL DEFAULT 0,
  PRIMARY KEY (user_id)
);

