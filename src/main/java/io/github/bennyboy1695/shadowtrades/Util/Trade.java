package io.github.bennyboy1695.shadowtrades.Util;

import com.google.gson.JsonArray;
import org.spongepowered.api.item.inventory.ItemStack;

public class Trade {

    private String id;
    private ItemStack displayItem;
    private JsonArray requiredItems;
    private JsonArray givenItems;

    public Trade(ItemStack displayItem, JsonArray requiredItems, JsonArray givenItems) {
        this.displayItem = displayItem;
        this.requiredItems = requiredItems;
        this.givenItems = givenItems;
    }

    public ItemStack getDisplayItem() {
        return displayItem;
    }

    public JsonArray getRequiredItems() {
        return requiredItems;
    }

    public JsonArray getGivenItems() {
        return givenItems;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "[id='" + id + "', " +
                "displayitem='" + InventoryUtils.serializeItemStack(getDisplayItem()).get().toString() + "', " +
                "requireditems='" + requiredItems + "', " +
                "givenitems='" + givenItems + "]";
    }
}
