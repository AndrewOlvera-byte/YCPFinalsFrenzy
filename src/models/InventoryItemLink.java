package models;

import orm.annotations.Entity;
import orm.annotations.Table;
import orm.annotations.Id;
import orm.annotations.GeneratedValue;
import orm.annotations.Column;

@Entity
@Table(name = "inventory_items")
public class InventoryItemLink {
    @Id
    @GeneratedValue
    @Column(name = "id")
    private int id;
    
    @Column(name = "inventory_id")
    private int inventoryId;
    
    @Column(name = "item_id")
    private int itemId;
    
    // Default no-argument constructor required by the ORM
    public InventoryItemLink() {
    }
    
    // Minimal constructor for convenience
    public InventoryItemLink(int inventoryId, int itemId) {
        this.inventoryId = inventoryId;
        this.itemId = itemId;
    }
    
    public int getId() {
        return id;
    }
    
    public void setId(int id) {
        this.id = id;
    }
    
    public int getInventoryId() {
        return inventoryId;
    }
    
    public void setInventoryId(int inventoryId) {
        this.inventoryId = inventoryId;
    }
    
    public int getItemId() {
        return itemId;
    }
    
    public void setItemId(int itemId) {
        this.itemId = itemId;
    }
}
