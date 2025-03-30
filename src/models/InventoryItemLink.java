package models;

import javax.persistence.GeneratedValue;

import orm.annotations.*;

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

    public InventoryItemLink() {}

    public InventoryItemLink(int id, int inventoryId, int itemId) {
        this.id = id;
        this.inventoryId = inventoryId;
        this.itemId = itemId;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getInventoryId() { return inventoryId; }
    public void setInventoryId(int inventoryId) { this.inventoryId = inventoryId; }

    public int getItemId() { return itemId; }
    public void setItemId(int itemId) { this.itemId = itemId; }
}
