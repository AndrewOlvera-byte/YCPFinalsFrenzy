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



CREATE TABLE ITEM (
  item_id           INT PRIMARY KEY,
  name              VARCHAR(255),
  value             INT,
  weight            INT,
  long_description  VARCHAR(1024),
  short_description VARCHAR(255),
  type              VARCHAR(50),   -- e.g. "weapon" or "potion"
  healing           INT,           -- how much HP it restores
  damage_multi      DOUBLE,        -- multiplier for player damage
  attack_damage     INT            -- for weapons: base attack
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
  last_saved    TIMESTAMP
);
