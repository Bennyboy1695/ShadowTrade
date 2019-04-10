package io.github.bennyboy1695.shadowtrades.Command;

import com.mcsimonflash.sponge.teslalibs.inventory.Element;
import com.mcsimonflash.sponge.teslalibs.inventory.Layout;
import com.mcsimonflash.sponge.teslalibs.inventory.Page;
import com.mcsimonflash.sponge.teslalibs.inventory.View;
import io.github.bennyboy1695.shadowtrades.Command.Elements.TypeElement;
import io.github.bennyboy1695.shadowtrades.ShadowTrades;
import io.github.bennyboy1695.shadowtrades.Util.InventoryUtils;
import io.github.bennyboy1695.shadowtrades.Util.Trade;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.command.CommandException;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.CommandSource;
import org.spongepowered.api.command.args.CommandContext;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.DyeColors;
import org.spongepowered.api.data.type.HandTypes;
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
    private ArrayList<Integer> selectedSlots = new ArrayList<>(Arrays.asList(45,46,47,48,49,50,51,52,53));
    private ItemStack border = ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DYE_COLOR, DyeColors.BLACK).add(Keys.DISPLAY_NAME, Text.of("")).build();
    private InventoryTitle requiredTitle;
    private InventoryTitle givenTitle;
    private InventoryTitle displayTitle;

    public ShadowTradeCommand(ShadowTrades plugin) {
        this.plugin = plugin;
    }

    public void register() {
        CommandSpec oldadd = CommandSpec.builder()
                .executor(this::oldaddCommand)
                .permission("shadowtrade.admin.trade.add")
                .build();

        CommandSpec add = CommandSpec.builder()
                .executor(this::addCommand)
                .permission("shadowtrade.admin.trade.add")
                .build();

        CommandSpec remove = CommandSpec.builder()
                .executor(this::removeCommand)
                .permission("shadowtrade.admin.trade.remove")
                .build();

        CommandSpec removeList = CommandSpec.builder()
                .executor(this::listRemoveCommand)
                .permission("shadowtrade.admin.trade.remove")
                .build();

        CommandSpec itemDisplay = CommandSpec.builder()
                .executor(this::itemDisplayCommand)
                .arguments(GenericArguments.remainingJoinedStrings(Text.of("display")))
                .permission("shadowtrade.admin.itemdisplay")
                .build();

        CommandSpec parent = CommandSpec.builder()
                .executor(this::parentCommand)
                .arguments(GenericArguments.seq(new TypeElement(Text.of("children"))))
                .child(oldadd, "oldadd")
                .child(add, "add", "create")
                .child(itemDisplay, "itemdisplay")
                .child(remove, "remove", "delete")
                .child(removeList, "removelist", "deletelist")
                .build();

        Sponge.getCommandManager().register(plugin, parent, "shadowtrade");

    }

    public CommandResult itemDisplayCommand(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            String display = args.<String>getOne("display").get();
            if (player.getItemInHand(HandTypes.MAIN_HAND).isPresent()) {
                ItemStack stack = player.getItemInHand(HandTypes.MAIN_HAND).get();
                stack.offer(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(display));
            }
        } else {
            src.sendMessage(Text.of(TextColors.RED, "You must be a player to run this command!"));
        }
        return CommandResult.success();
    }

    public CommandResult parentCommand(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            player.sendMessage(Text.of(TextColors.AQUA, "Usage: /shadowtrade <add|remove|itemdisplay> [displayname]"));
            //player.sendMessage(Text.of(String.valueOf(plugin.getTrades())));
        } else {
            src.sendMessage(Text.of(TextColors.RED, "You must be a player to run this command!"));
        }
        return CommandResult.success();
    }

    public CommandResult addCommand(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            displayItemGui(player);
        } else {
            src.sendMessage(Text.of(TextColors.RED, "You must be a player to run this command!"));
        }
        return CommandResult.success();
    }

    public CommandResult removeCommand(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            removeTradeGui(player);
        } else {
            src.sendMessage(Text.of(TextColors.RED, "You must be a player to run this command!"));
        }
        return CommandResult.success();
    }

    private void displayItemGui(Player player) {
        final ItemStack[] displayStack = {null};
        View view = View.builder().property(InventoryTitle.of(Text.of(TextColors.GOLD, "Setup: Display Item"))).archetype(InventoryArchetypes.DOUBLE_CHEST).build(plugin.getPluginContainer());
        Layout.Builder layout = Layout.builder().row(Element.of(border), 4);
        layout.set(Element.builder().item(ItemStack.builder().itemType(ItemTypes.CLOCK).add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Refresh!")).build()).onClick(click -> {
            displayItemGui(player);
        }).build(), 40);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(TextColors.AQUA, "This Gui is an exact replica of your inventory and will be updated every time you open it or click the refresh button. The hotbar is the top row."));
        lore.add(Text.of(TextColors.AQUA, "To set an item to be the Display Item, all you need to do is left click it and then click the accept button (green wool)!"));
        lore.add(Text.of(TextColors.AQUA, "Currently selected items will appear below this line. To remove them just click them!"));
        layout.set(Element.of(ItemStack.builder().itemType(ItemTypes.GLOWSTONE_DUST).add(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, "Info!")).add(Keys.ITEM_LORE, lore).build()), 36);
        if (displayStack[0] != null)
            layout.set(Element.builder().item(ItemStack.builder().itemType(ItemTypes.WOOL).add(Keys.DYE_COLOR, DyeColors.GREEN).add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Accept '" ,TextColors.RESET, (displayStack[0].get(Keys.DISPLAY_NAME).isPresent() ? displayStack[0].get(Keys.DISPLAY_NAME).get() : displayStack[0].getTranslation().get()) , TextColors.GREEN,"' as the display item!")).build()).onClick(click -> {
                requiredItemsGui(player, displayStack[0]);
            }).build(), 44);
        HashMap<Integer, Optional<ItemStack>> itemStacks = InventoryUtils.getMappedSlots(player.getInventory());
        for (Map.Entry<Integer, Optional<ItemStack>> entries : itemStacks.entrySet()) {
            if (entries.getValue().isPresent()) {
                layout.set(Element.builder().item(entries.getValue().get()).onClick(click -> {
                    displayStack[0] = click.getElement().getItem().createStack();
                    view.setElement(49, Element.builder().item(ItemStack.builder().fromItemStack(displayStack[0]).add(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "This is your currently selected Display item!")).build()).onClick(click2 -> view.setElement(49, Element.of(ItemStack.empty()))).build());
                    view.setElement(44, Element.builder().item(ItemStack.builder().itemType(ItemTypes.WOOL).add(Keys.DYE_COLOR, DyeColors.GREEN).add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Accept '" ,TextColors.RESET, (displayStack[0].get(Keys.DISPLAY_NAME).isPresent() ? displayStack[0].get(Keys.DISPLAY_NAME).get() : displayStack[0].getTranslation().get()) , TextColors.GREEN,"' as the display item!")).build()).onClick(click3 -> requiredItemsGui(player, displayStack[0])
                    ).build());
                }).build(), entries.getKey() + - 1);
            }
        }
        view.define(layout.build()).open(player);
    }

    private void requiredItemsGui(Player player, ItemStack displayStack) {
        ArrayList<ItemStack> requiredStacks = new ArrayList<>();
        final int[] itemCount = {0};
        View view = View.builder().property(InventoryTitle.of(Text.of(TextColors.GOLD, "Setup: Required Items"))).archetype(InventoryArchetypes.DOUBLE_CHEST).build(plugin.getPluginContainer());
        Layout.Builder layout = Layout.builder().row(Element.of(border), 4);
        layout.set(Element.builder().item(ItemStack.builder().itemType(ItemTypes.CLOCK).add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Refresh!")).build()).onClick(click -> {
            displayItemGui(player);
        }).build(), 40);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(TextColors.AQUA, "This Gui is an exact replica of your inventory and will be updated every time you open it or click the refresh button. The hotbar is the top row."));
        lore.add(Text.of(TextColors.AQUA, "To set an item to be part of the Required Items, all you need to do is left click it and then click the accept button (green wool)!"));
        lore.add(Text.of(TextColors.AQUA, "Currently selected items will appear below this line. To remove them just click them!"));
        layout.set(Element.of(ItemStack.builder().itemType(ItemTypes.GLOWSTONE_DUST).add(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, "Info!")).add(Keys.ITEM_LORE, lore).build()), 36);
        if (requiredStacks != null)
            if (!requiredStacks.isEmpty())
                layout.set(Element.builder().item(ItemStack.builder().itemType(ItemTypes.WOOL).add(Keys.DYE_COLOR, DyeColors.GREEN).add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Accept '" ,TextColors.RESET, "the items below", TextColors.GREEN,"' as the required items!")).build()).onClick(click -> {
                    giveItemsGui(player, displayStack, requiredStacks);
            }).build(), 44);
        HashMap<Integer, Optional<ItemStack>> itemStacks = InventoryUtils.getMappedSlots(player.getInventory());
        for (Map.Entry<Integer, Optional<ItemStack>> entries : itemStacks.entrySet()) {
            if (entries.getValue().isPresent()) {
                layout.set(Element.builder().item(entries.getValue().get()).onClick(click -> {
                    requiredStacks.add(click.getElement().getItem().createStack());
                    view.setElement(selectedSlots.get(itemCount[0]), Element.builder().item(ItemStack.builder().fromItemStack(click.getElement().getItem().createStack()).add(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "This is one of your currently selected required items!")).build()).onClick(click2 -> view.setElement(itemCount[0], Element.of(ItemStack.empty()))).build());
                    view.setElement(44, Element.builder().item(ItemStack.builder().itemType(ItemTypes.WOOL).add(Keys.DYE_COLOR, DyeColors.GREEN).add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Accept '" ,TextColors.RESET, "the items below", TextColors.GREEN,"' as the required items!")).build()).onClick(click3 -> {
                        giveItemsGui(player, displayStack, requiredStacks);
                    }).build());
                    itemCount[0]++;
                }).build(), entries.getKey() + - 1);
            }
        }
        view.define(layout.build()).open(player);
    }

    private void giveItemsGui(Player player, ItemStack displayItem, ArrayList<ItemStack> requiredStacks) {
        ArrayList<ItemStack> givenStacks = new ArrayList<>();
        final int[] itemCount = {0};
        View view = View.builder().property(InventoryTitle.of(Text.of(TextColors.GOLD, "Setup: Given Items"))).archetype(InventoryArchetypes.DOUBLE_CHEST).build(plugin.getPluginContainer());
        Layout.Builder layout = Layout.builder().row(Element.of(border), 4);
        layout.set(Element.builder().item(ItemStack.builder().itemType(ItemTypes.CLOCK).add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Refresh!")).build()).onClick(click -> {
            displayItemGui(player);
        }).build(), 40);
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(TextColors.AQUA, "If the stuff you want to give is command simply rename a command block to be your command."));
        lore.add(Text.of(TextColors.AQUA, "Command ares can be: @p for playername, @pl for lowercase playername, @u for player uuid and @return at the end to return the items required for the trade."));
        lore.add(Text.of(TextColors.AQUA, "This Gui is an exact replica of your inventory and will be updated every time you open it or click the refresh button. The hotbar is the top row."));
        lore.add(Text.of(TextColors.AQUA, "To set an item to be part of the Given Items, all you need to do is left click it and then click the accept button (green wool)!"));
        lore.add(Text.of(TextColors.AQUA, "Currently selected items will appear below this line. To remove them just click them!"));
        layout.set(Element.of(ItemStack.builder().itemType(ItemTypes.GLOWSTONE_DUST).add(Keys.DISPLAY_NAME, Text.of(TextColors.AQUA, "Info!")).add(Keys.ITEM_LORE, lore).build()), 36);
        if (!givenStacks.isEmpty())
            layout.set(Element.builder().item(ItemStack.builder().itemType(ItemTypes.WOOL).add(Keys.DYE_COLOR, DyeColors.GREEN).add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Accept '" ,TextColors.RESET, "the items below", TextColors.GREEN,"' as the given items!")).build()).onClick(click -> {
                player.closeInventory();
            }).build(), 44);
        HashMap<Integer, Optional<ItemStack>> itemStacks = InventoryUtils.getMappedSlots(player.getInventory());
        for (Map.Entry<Integer, Optional<ItemStack>> entries : itemStacks.entrySet()) {
            if (entries.getValue().isPresent()) {
                layout.set(Element.builder().item(entries.getValue().get()).onClick(click -> {
                    givenStacks.add(click.getElement().getItem().createStack());
                    view.setElement(selectedSlots.get(itemCount[0]), Element.builder().item(ItemStack.builder().fromItemStack(click.getElement().getItem().createStack()).add(Keys.DISPLAY_NAME, Text.of(TextColors.GOLD, "This is one of your currently selected given items!")).build()).onClick(click2 -> view.setElement(itemCount[0], Element.of(ItemStack.empty()))).build());
                    view.setElement(44, Element.builder().item(ItemStack.builder().itemType(ItemTypes.WOOL).add(Keys.DYE_COLOR, DyeColors.GREEN).add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Accept '" ,TextColors.RESET, "the items below", TextColors.GREEN,"' as the given items!")).build()).onClick(click3 -> {
                        Trade trade = new Trade(displayItem, InventoryUtils.convertItemArrayToJsonArray(requiredStacks), InventoryUtils.convertItemArrayToJsonArray(givenStacks));
                        trade.setId(UUID.randomUUID().toString().replaceAll("-.*", ""));
                        plugin.getTrades().add(plugin.getSqlManager().addNewTrade(trade));
                        player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.setupInv.successfulSetupReply));
                        player.closeInventory();
                    }).build());
                    itemCount[0]++;
                }).build(), entries.getKey() + - 1);
            }
        }
        view.define(layout.build()).open(player);
    }

    private void removeTradeGui(Player player) {
        final String[] tradeID = {""};
        Layout.Builder layoutBuilder = Layout.builder().border(Element.of(border));
        if (plugin.getTrades().size() > 28) {
            layoutBuilder.set(Page.PREVIOUS, 47).set(Page.NEXT, 51);
        }
        List<Text> lore = new ArrayList<>();
        lore.add(Text.of(TextColors.AQUA, "To remove a trade simply just click it. It will then be removed!"));
        layoutBuilder.set(Element.builder().item(ItemStack.builder().itemType(ItemTypes.CLOCK).add(Keys.ITEM_LORE, lore).add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Refresh!")).build()).onClick(click -> {
            removeTradeGui(player);
        }).build(), 49);
        Page removeGui = Page.builder().archetype(InventoryArchetypes.DOUBLE_CHEST).property(InventoryTitle.of(Text.of(TextColors.GOLD, "Removable Trades"))).layout(layoutBuilder.build()).build(plugin.getPluginContainer());
        List<Element> elements = new ArrayList<>();
        for (Trade trades : plugin.getTrades()) {
            List<Text> tradeLore = new ArrayList<>();
            tradeLore.add(Text.of(TextColors.AQUA, "Trade Requires:"));
            for (ItemStack stack : InventoryUtils.convertJsonArrayToItemArray(trades.getRequiredItems())) {
                tradeLore.add(Text.of(TextColors.GRAY, stack.getQuantity(), TextColors.GREEN, " X ", TextColors.GRAY, (stack.get(Keys.DISPLAY_NAME).isPresent() ? stack.get(Keys.DISPLAY_NAME).get() : stack.getTranslation().get())));
            }
            tradeLore.add(Text.of(TextColors.AQUA, "Trade Gives:"));
            for (ItemStack stack : InventoryUtils.convertJsonArrayToItemArray(trades.getGivenItems())) {
                tradeLore.add(Text.of(TextColors.GRAY, stack.getQuantity(), TextColors.GREEN, " X ", TextColors.GRAY, (stack.get(Keys.DISPLAY_NAME).isPresent() ? stack.get(Keys.DISPLAY_NAME).get() : stack.getTranslation().get())));
            }
            elements.add(Element.builder().item(ItemStack.builder().fromItemStack(trades.getDisplayItem()).add(Keys.ITEM_LORE, tradeLore).build()).onClick(click -> {
                plugin.getSqlManager().removeTrade(trades.getId());
                player.sendMessage(Text.of(TextColors.GOLD, "Removed Trade successfully!"));
                removeTradeGui(player);
            }).build());
        }
        removeGui.define(elements);
        removeGui.open(player);
    }

    public CommandResult oldaddCommand(CommandSource src, CommandContext args) throws CommandException {
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
                                           if (entry.getKey() < 12) {
                                               List<Text> lore = new ArrayList<>();
                                               ItemStack stack = entry.getValue();
                                               lore.add(Text.of(TextColors.AQUA, "Command block is used to represent a command that will be run!"));
                                               lore.add(Text.of(TextColors.AQUA, "The command that will be run is the one that is the name of the command block."));
                                               if (entry.getValue().getType().equals(ItemTypes.COMMAND_BLOCK))
                                                   stack.offer(Keys.ITEM_LORE, lore);
                                               stacks1.add(stack);
                                           }
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

    public CommandResult listRemoveCommand(CommandSource src, CommandContext args) throws CommandException {
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
