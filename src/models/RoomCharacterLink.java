package models;

import javax.persistence.GeneratedValue;

import orm.annotations.*;

@Entity
@Table(name = "room_characters")
public class RoomCharacterLink {
    @Id
    @GeneratedValue
	@Column(name = "id")
    private int id;

    @Column(name = "room_id")
    private int roomId;

    @Column(name = "character_id")
    private int characterId;

    public RoomCharacterLink() {}

    public RoomCharacterLink(int id, int roomId, int characterId) {
        this.id = id;
        this.roomId = roomId;
        this.characterId = characterId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getRoomId() { return roomId; }
    public void setRoomId(int roomId) { this.roomId = roomId; }

    public int getCharacterId() { return characterId; }
    public void setCharacterId(int characterId) { this.characterId = characterId; }
}