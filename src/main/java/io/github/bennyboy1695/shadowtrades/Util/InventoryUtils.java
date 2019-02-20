package io.github.bennyboy1695.shadowtrades.Util;

import com.google.gson.*;
import io.github.bennyboy1695.shadowtrades.ShadowTrades;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import org.spongepowered.api.data.persistence.DataTranslators;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.Inventory;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class InventoryUtils {

    private static ShadowTrades plugin;

    public InventoryUtils(ShadowTrades plugin) {
        this.plugin = plugin;
    }

    public static Optional<JsonObject> serializeItemStack(ItemStack item) {
        try {
            StringWriter sink = new StringWriter();
            GsonConfigurationLoader loader = GsonConfigurationLoader.builder().setSink(() -> new BufferedWriter(sink)).build();
            ConfigurationNode node = DataTranslators.CONFIGURATION_NODE.translate(item.toContainer());
            loader.save(node);
            return Optional.ofNullable(new JsonParser().parse(sink.toString()).getAsJsonObject());
        } catch (Exception e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    public static Optional<ItemStack> deserializeItemStack(JsonObject object) {
        try {
            StringReader source = new StringReader(object.toString());
            HoconConfigurationLoader loader = HoconConfigurationLoader.builder().setSource(() -> new BufferedReader(source)).build();
            ConfigurationNode configurationNode = loader.load();
            return Optional.of(ItemStack.builder().fromContainer(DataTranslators.CONFIGURATION_NODE.translate(configurationNode)).build());
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public static HashMap<Integer, Optional<ItemStack>> getMappedSlots(Inventory inventory) {
        HashMap<Integer, Optional<ItemStack>> slotAndSlot = new HashMap<>();
        int slotCounter = 0;
        for (Inventory i : inventory.slots()) {
            slotCounter++;
            slotAndSlot.put(slotCounter, i.peek());
        }

        for (Map.Entry<Integer, Optional<ItemStack>> entry : slotAndSlot.entrySet()) {
        }
        return slotAndSlot;
    }

    public static HashMap<Integer, ItemStack> getMappedPanesRemoved(HashMap<Integer, Optional<ItemStack>> optionalItems) {
        HashMap<Integer, ItemStack> noOptionals = new HashMap<>();
        for (Map.Entry<Integer, Optional<ItemStack>> entry : optionalItems.entrySet()) {
            if (entry.getValue().isPresent() && !entry.getValue().get().getType().equals(ItemTypes.GLASS_PANE))
                noOptionals.put(entry.getKey(), entry.getValue().get());
        }
        return noOptionals;
    }

    public static HashMap<Integer, Inventory> getMappedInventorySlots(Inventory inventory) {
        HashMap<Integer, Inventory> slotAndSlot = new HashMap<>();
        int slotCounter = 0;
        for (Inventory i : inventory.slots()) {
            slotCounter++;
            slotAndSlot.put(slotCounter, i);
        }
        return slotAndSlot;
    }

    public static ArrayList<ItemStack> convertStrings(ArrayList<String> strings) {
        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        for (String string : strings) {
            JsonParser parser = new JsonParser();
            try {
                itemStacks.add(deserializeItemStack(parser.parse(string).getAsJsonObject()).get());
            } catch (JsonSyntaxException e) {
                System.out.println(e + string);
            }
        }
        return itemStacks;
    }

    public static JsonArray convertItemArrayToJsonArray(ArrayList<ItemStack> itemStacks) {
        JsonArray array = new JsonArray();
        for (ItemStack stack : itemStacks) {
            array.add(serializeItemStack(stack).get());
        }
        return array;
    }

    public static ArrayList<ItemStack> convertJsonArrayToItemArray(JsonArray array) {
        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        for (JsonElement element : array) {
            itemStacks.add(deserializeItemStack(element.getAsJsonObject()).get());
        }
        return itemStacks;
    }

    public static ArrayList<ItemStack> givePlayerItemsFromArray(Player player, ArrayList<ItemStack> itemStacks) {
        ArrayList<ItemStack> toGive = itemStacks;
        ArrayList<ItemStack> failedStacks = new ArrayList<>();
        int count = 0;
        for (ItemStack stack : toGive) {
            InventoryTransactionResult result = player.getInventory().query(MainPlayerInventory.class).offer(stack);
            if (result.getType().equals(InventoryTransactionResult.Type.FAILURE)) {
                if (plugin.getConfigManager().getCore().tradeCommand.placeItemsInEnderIfFull) {
                    InventoryTransactionResult enderResult = player.getEnderChestInventory().offer(stack);
                    if (enderResult.getType().equals(InventoryTransactionResult.Type.FAILURE)) {
                        failedStacks.add(stack);
                    } else {
                        player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.tradeInv.enderChestMessage));
                        count++;
                    }
                } else {
                    failedStacks.add(stack);
                }
            } else {
                count++;
            }
        }
        return failedStacks;
    }
}
