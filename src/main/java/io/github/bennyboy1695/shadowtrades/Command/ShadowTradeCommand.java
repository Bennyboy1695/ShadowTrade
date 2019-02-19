package io.github.bennyboy1695.shadowtrades.Command;

import io.github.bennyboy1695.shadowtrades.ShadowTrades;
import io.github.bennyboy1695.shadowtrades.Util.InventoryUtils;
import io.github.bennyboy1695.shadowtrades.Util.Trade;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.item.inventory.ClickInventoryEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.*;
import org.spongepowered.api.item.inventory.entity.PlayerInventory;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.action.TextActions;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.*;

public class ShadowTradeCommand {

    private ShadowTrades plugin;
    private ArrayList<Integer> borderLimit = new ArrayList<>(Arrays.asList(13,14,15,16,17,18,19,20,21,22,23,24,25,26,27));
    private ItemStack border = ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DYE_COLOR, DyeColors.BLACK).add(Keys.DISPLAY_NAME, Text.of("")).build();
    private InventoryTitle requiredTitle;
    private InventoryTitle givenTitle;
    private InventoryTitle displayTitle;

    public ShadowTradeCommand(ShadowTrades plugin) {
        this.plugin = plugin;
    }

    public void register() {
        CommandSpec add = CommandSpec.builder()
                .executor(this::addCommand)
                .permission("shadowtrade.admin.trade.add")
                .build();

        CommandSpec remove = CommandSpec.builder()
                .executor(this::removeCommand)
                .permission("shadowtrade.admin.trade.remove")
                .build();

        CommandSpec parent = CommandSpec.builder()
                .executor(this::parentCommand)
                .child(add, "add")
                .child(remove, "remove")
                .build();

        Sponge.getCommandManager().register(plugin, parent, "shadowtrade");

    }

    public CommandResult parentCommand(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            player.sendMessage(Text.of(TextColors.AQUA, "Usage: /shadowtrade <add|remove>"));
            //player.sendMessage(Text.of(String.valueOf(plugin.getTrades())));
        } else {
            src.sendMessage(Text.of(TextColors.RED, "You must be a player to run this command!"));
        }
        return CommandResult.success();
    }

    public CommandResult addCommand(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            String step = (args.<String>getOne("step").isPresent() ? args.<String>getOne("step").get() : null);
            boolean finished = (args.<String>getOne("finished").isPresent() && args.<String>getOne("finished").get().toLowerCase().equals("complete"));
            Player player = (Player) src;
            final Trade[] trade = {null};
            displayTitle = InventoryTitle.of(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.setupInv.displayItemInvTitle));
            givenTitle = InventoryTitle.of(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.setupInv.givenItemsInvTitle));
            requiredTitle = InventoryTitle.of(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.setupInv.requiredItemsInvTitle));
            Inventory.Builder inventory = Inventory.builder().of(InventoryArchetypes.DISPENSER).property(displayTitle);
            inventory.listener(InteractInventoryEvent.Close.class, close -> {
                if (close.getTargetInventory().getInventoryProperty(InventoryTitle.class).get().equals(displayTitle)) {
                    HashMap<Integer, ItemStack> mapped = removePlayerAndPanes(close.getTargetInventory());
                    for (Map.Entry<Integer, ItemStack> entry : mapped.entrySet()) {
                        if (entry.getKey().equals(5)) {
                            if (!entry.getValue().equals(border)) {
                                trade[0] = new Trade(entry.getValue(),null, null);
                            }
                        }
                    }
                }
               Inventory.Builder requiredInv = Inventory.builder().of(InventoryArchetypes.CHEST).property(requiredTitle);
               requiredInv.listener(InteractInventoryEvent.Close.class, close1 -> {
                   if (close1.getTargetInventory().getInventoryProperty(InventoryTitle.class).get().equals(requiredTitle)) {
                       HashMap<Integer, ItemStack> invMapped = removePlayerAndPanes(close1.getTargetInventory());
                       invMapped.forEach((i,s) -> {System.out.println(s.toString());});
                       if (invMapped.size() < 13) {
                           ArrayList<ItemStack> stacks = new ArrayList<>();
                           for (Map.Entry<Integer, ItemStack> entry : invMapped.entrySet()) {
                               if (entry.getKey() < 12)
                                   stacks.add(entry.getValue());
                           }
                           trade[0] = new Trade(trade[0].getDisplayItem(), InventoryUtils.convertItemArrayToJsonArray(stacks), null);
                           Inventory.Builder givenInv = Inventory.builder().of(InventoryArchetypes.CHEST).property(givenTitle);
                           givenInv.listener(InteractInventoryEvent.Close.class, close2 -> {
                               if (close2.getTargetInventory().getInventoryProperty(InventoryTitle.class).get().equals(givenTitle)) {
                                   HashMap<Integer, ItemStack> invMapped1 = removePlayerAndPanes(close2.getTargetInventory());
                                   if (invMapped1.size() < 13) {
                                       ArrayList<ItemStack> stacks1 = new ArrayList<>();
                                       for (Map.Entry<Integer, ItemStack> entry : invMapped1.entrySet()) {
                                           if (entry.getKey() < 12)
                                               stacks1.add(entry.getValue());
                                       }
                                       trade[0] = new Trade(trade[0].getDisplayItem(), trade[0].getRequiredItems(), InventoryUtils.convertItemArrayToJsonArray(stacks1));
                                       trade[0].setId(UUID.randomUUID().toString().replaceAll("-.*", ""));
                                       if (trade[0].getId() != null && trade[0].getGivenItems() != null && trade[0].getRequiredItems() != null) {
                                           plugin.getTrades().add(plugin.getSqlManager().addNewTrade(trade[0]));
                                           player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.setupInv.successfulSetupReply));
                                       }
                                   } else {
                                       player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.setupInv.tooManyItemsReply));
                                   }
                               }

                           });
                           Inventory given = givenInv.build(plugin);
                           HashMap<Integer, Inventory> slots = InventoryUtils.getMappedInventorySlots(given);
                           for (Map.Entry<Integer, Inventory> slot : slots.entrySet()) {
                               if (borderLimit.contains(slot.getKey())) {
                                   slot.getValue().set(border);
                               }
                           }
                           player.openInventory(given);
                       } else {
                           player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.setupInv.tooManyItemsReply));
                       }
                   }
               });
               Inventory required = requiredInv.build(plugin);
                HashMap<Integer, Inventory> slots = InventoryUtils.getMappedInventorySlots(required);
                for (Map.Entry<Integer, Inventory> slot : slots.entrySet()) {
                    if (borderLimit.contains(slot.getKey())) {
                        slot.getValue().set(border);
                    }
                }
               player.openInventory(required);
            });

            Inventory needsBorder = inventory.build(plugin);
            for (Map.Entry<Integer, Inventory> borderSet : InventoryUtils.getMappedInventorySlots(needsBorder).entrySet()) {
                if (!borderSet.getKey().equals(5)){
                    borderSet.getValue().set(border);
                }
            }
            player.openInventory(needsBorder);
        } else {
            src.sendMessage(Text.of(TextColors.RED, "You must be a player to run this command!"));
        }
        return CommandResult.success();
    }

    public CommandResult removeCommand(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            ArrayList<Text> contents = new ArrayList<>();
            for (Trade trade : plugin.getTrades()) {
                Text.Builder builder = Text.builder();
                builder.onClick(TextActions.executeCallback(source -> {
                    if (plugin.getSqlManager().removeTrade(trade.getId()))
                        source.sendMessage(Text.of(TextColors.GREEN, "Removed Trade!"));
                }));
                builder.append(Text.builder().append(Text.of(TextColors.GOLD, trade.getDisplayItem().getTranslation().get())).onHover(TextActions.showItem(trade.getDisplayItem().createSnapshot())).append(Text.of(" | ")).append(Text.builder().append(Text.of(trade.getRequiredItems().toString())).build()).append(Text.of(" | ")).append(Text.builder().append(Text.of(trade.getGivenItems().toString())).build()).build());
                Text text = builder.build();
                contents.add(text);
            }
            plugin.getPaginationService().builder().contents(contents).sendTo(player);
        } else {
            src.sendMessage(Text.of(TextColors.RED, "You must be a player to run this command!"));
        }
        return CommandResult.success();
    }

    @Listener
    public void onInventoryClick(ClickInventoryEvent event) {
        if (event.getTargetInventory() instanceof PlayerInventory) {
            event.setCancelled(false);
        } else {
            if (event.getTargetInventory().getInventoryProperty(InventoryTitle.class).isPresent()) {
                if (event.getCursorTransaction().getDefault().equals(border.createSnapshot()) || event.getCursorTransaction().getOriginal().equals(border.createSnapshot()) || event.getCursorTransaction().getFinal().equals(border.createSnapshot())) {
                    event.setCancelled(true);
                }
            }
        }
    }

    public HashMap<Integer, ItemStack> removePlayerAndPanes(Inventory inventory) {
        HashMap<Integer, ItemStack> finalMap = new HashMap<>();
        int count = 0;
        if (inventory.getInventoryProperty(InventoryTitle.class).get().equals(displayTitle) || inventory.getInventoryProperty(InventoryTitle.class).get().equals(requiredTitle) ||  inventory.getInventoryProperty(InventoryTitle.class).get().equals(givenTitle)) {
            for (Map.Entry<Integer, Optional<ItemStack>> entry : InventoryUtils.getMappedSlots(inventory).entrySet()) {
                if (count < 13) {
                    count++;
                    if (entry.getValue().isPresent()) {
                        if (!entry.getValue().get().equals(border)) {
                            finalMap.put(entry.getKey(), entry.getValue().get());
                        }
                    }
                }
            }
        }
        return finalMap;
    }
}
