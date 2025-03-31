package models;

import orm.annotations.Entity;
import orm.annotations.Table;
import orm.annotations.Id;
import orm.annotations.GeneratedValue;
import orm.annotations.Column;

/**
 * RoomCharacterLink represents the join table between Room and Character.
 * It stores the association between a room (roomId) and a character (characterId).
 */
@Entity
@Table(name = "room_characters")
public class RoomCharacterLink {
    
    // Primary key for the join table.
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;
    
    @Column(name = "room_id")
    private int roomId;
    
    @Column(name = "character_id")
    private int characterId;
    
    // Default constructor remains unchanged.
    public RoomCharacterLink() {
    }
    
    public RoomCharacterLink(int roomId, int characterId) {
        this.roomId = roomId;
        this.characterId = characterId;
    }
    
    // Getters and Setters
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getRoomId() {
        return roomId;
    }
    
    public void setRoomId(int roomId) {
        this.roomId = roomId;
    }
    
    public int getCharacterId() {
        return characterId;
    }
    
    public void setCharacterId(int characterId) {
        this.characterId = characterId;
    }
}
