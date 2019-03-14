package io.github.bennyboy1695.shadowtrades.Command;

import com.mcsimonflash.sponge.teslalibs.inventory.Element;
import com.mcsimonflash.sponge.teslalibs.inventory.Layout;
import com.mcsimonflash.sponge.teslalibs.inventory.Page;
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
import org.spongepowered.api.item.ItemTypes;
import org.spongepowered.api.item.inventory.InventoryArchetype;
import org.spongepowered.api.item.inventory.InventoryArchetypes;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.entity.MainPlayerInventory;
import org.spongepowered.api.item.inventory.property.InventoryDimension;
import org.spongepowered.api.item.inventory.property.InventoryTitle;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.format.TextColors;
import org.spongepowered.api.text.serializer.TextSerializers;
import org.spongepowered.api.util.Tuple;
import org.spongepowered.api.util.rotation.Rotations;

import java.util.*;

@SuppressWarnings("Duplicates")
public class TradeCommand {

    private static ShadowTrades plugin;
    private static Element border = Element.builder().item(ItemStack.builder().itemType(ItemTypes.STAINED_GLASS_PANE).add(Keys.DYE_COLOR, DyeColors.BLACK).build()).build();
    private static InventoryDimension dimension = InventoryDimension.of(9, 6);
    private static InventoryArchetype archetype = InventoryArchetypes.DOUBLE_CHEST;

    public TradeCommand(ShadowTrades plugin) {
        this.plugin = plugin;
    }

    public void register() {
        CommandSpec getitems = CommandSpec.builder()
                .executor(this::getItems)
                .build();

        CommandSpec trade = CommandSpec.builder()
                .executor(this::trade)
                .child(getitems, "getitems")
                .build();

        Sponge.getCommandManager().register(plugin, trade, "trade");
    }

    private CommandResult getItems(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            if (plugin.getSqlManager().doesPlayerHaveAFailedStack(player)) {
                ArrayList<ItemStack> failedStack = plugin.getSqlManager().getFailedStacks(player);
                ArrayList<ItemStack> result = InventoryUtils.givePlayerItemsFromArray(player, failedStack);
                if (result.isEmpty()) {
                    plugin.getSqlManager().removeFailedTrade(player, failedStack);
                    player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().tradeCommand.getItemsCommand.getAllItemsMessage));
                } else {
                    plugin.getSqlManager().addNewFailedTrade(player, result);
                    player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().tradeCommand.getItemsCommand.notEnoughRoomMessage));
                }
            } else {
                player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().tradeCommand.getItemsCommand.noFailedTradesMessage));
            }
        } else {
            src.sendMessage(Text.of(TextColors.RED, "You must be a player to run this command!"));
        }
        return CommandResult.success();
    }

    private CommandResult trade(CommandSource src, CommandContext args) throws CommandException {
        if (src instanceof Player) {
            Player player = (Player) src;
            mainTradingGui(player);
        } else {
            src.sendMessage(Text.of(TextColors.RED, "You must be a player to run this command!"));
        }
        return CommandResult.success();
    }

    private static Page mainTradingGui(Player player) {
        InventoryTitle title = InventoryTitle.of(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.tradeInv.displayNames.mainInvTitle));
        Element refresh = Element.builder().item(ItemStack.builder().itemType(ItemTypes.ENDER_EYE).add(Keys.DISPLAY_NAME, Text.of(TextColors.GREEN, "Refresh")).build()).onClick(action -> mainTradingGui(player)).build();
        Layout layout = Layout.builder().dimension(dimension).border(border).set(refresh, 49).build();
        if (plugin.getTrades().size() > 28) {
            layout = Layout.builder().dimension(dimension).border(border).set(refresh, 49).set(Page.PREVIOUS, 47).set(Page.NEXT, 51).build();
        }
        Page mainTradingGUI = Page.builder().archetype(archetype).layout(layout).property(title).build(plugin.getPluginContainer());

        List<Element> elements = new ArrayList<>();
        for (Trade trades : plugin.getTrades()) {
            elements.add(Element.builder().item(trades.getDisplayItem()).onClick(action -> tradingGUI(player, trades.getId())).build());
        }
        try {
            mainTradingGUI.define(elements);
            mainTradingGUI.open(player);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return mainTradingGUI;
    }

    private static void tradingGUI(Player player, String tradeID) {
        ArrayList<Integer> availableRequiredSlots = new ArrayList<>(Arrays.asList(10, 11, 12, 19, 20, 21, 28, 29, 30, 37, 38, 39));
        ArrayList<Integer> availableGivenSlots = new ArrayList<>(Arrays.asList(14, 15, 16, 23, 24, 25, 32, 33, 34, 41, 42, 43));
        Trade trade = null;
        for (Trade trades : plugin.getTrades()) {
            if (trades.getId().equals(tradeID))
                trade = trades;
        }
        if (trade != null) {
            InventoryTitle title = InventoryTitle.of(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.tradeInv.displayNames.tradingGuiTitle.replace("<DisplayItem>", (trade.getDisplayItem().get(Keys.DISPLAY_NAME).isPresent() ? TextSerializers.FORMATTING_CODE.serialize(trade.getDisplayItem().get(Keys.DISPLAY_NAME).get()) : trade.getDisplayItem().getTranslation().get()))));
            Layout.Builder layout = Layout.builder().dimension(dimension).border(border);
            layout.column(border, 4);

            List<Element> elements = new ArrayList<>();
            ArrayList<ItemStack> convertedRequired = InventoryUtils.convertJsonArrayToItemArray(trade.getRequiredItems());
            ArrayList<ItemStack> convertedGiven = InventoryUtils.convertJsonArrayToItemArray(trade.getGivenItems());
            int requiredSlot = 0;
            for (ItemStack stack : convertedRequired) {
                requiredSlot = availableRequiredSlots.get(0);
                layout.set(Element.of(stack), requiredSlot);
                availableRequiredSlots.remove((Object) requiredSlot);
            }
            int givenSlot = 0;
            for (ItemStack stack : convertedGiven) {
                givenSlot = availableGivenSlots.get(0);
                layout.set(Element.of(stack), givenSlot);
                availableGivenSlots.remove((Object) givenSlot);
            }
            ArrayList<Boolean> trueCount = new ArrayList<>();
            ArrayList<ItemStack> falseStacks = new ArrayList<>();
            for (ItemStack stack : convertedRequired) {
                if (player.getInventory().contains(stack)) {
                    trueCount.add(true);
                } else {
                    falseStacks.add(stack);
                }
            }
            List<Text> lore = new ArrayList<>();
            ItemStack finisher;
            Element doTrade;
            if (trueCount.size() == convertedRequired.size()) {
                finisher = ItemStack.builder().itemType(ItemTypes.WOOL).add(Keys.DYE_COLOR, DyeColors.GREEN).add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.tradeInv.displayNames.successTradeFinishDisplay)).add(Keys.ITEM_LORE, lore).build();
                Trade finalTrade = trade;
                doTrade = Element.builder().item(finisher).onClick(action -> {
                    Tuple<Boolean, ArrayList<ItemStack>> tradeResult = doTrade(finalTrade, player);
                    if (tradeResult.getFirst()) {
                        plugin.getLogger().info("Player: " + player.getName() + " made a successful trade of id: " + tradeID);
                        player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.tradeInv.successfulTradeMessage));
                    } else {
                        plugin.getLogger().info("Player: " + player.getName() + " made a successful trade but couldn't receive all items of id: " + tradeID);
                        player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.tradeInv.failedToGiveItemsMessage));
                        if (!tradeResult.getSecond().isEmpty())
                            plugin.getSqlManager().addNewFailedTrade(player, tradeResult.getSecond());
                    }
                    tradingGUI(player, tradeID);
                }).build();
            } else {
                lore.add(Text.of(Text.of(TextColors.AQUA, "You still need: ")));
                for (ItemStack stack : falseStacks) {
                    lore.add(Text.of(TextColors.GREEN, stack.getQuantity(), TextColors.GRAY, " X ", TextColors.GREEN, (stack.get(Keys.DISPLAY_NAME).isPresent() ? stack.get(Keys.DISPLAY_NAME).get() : stack.getTranslation().get())));
                }
                finisher = ItemStack.builder().itemType(ItemTypes.BARRIER).add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.tradeInv.displayNames.failTradeFinishDisplay)).add(Keys.ITEM_LORE, lore).build();
                doTrade = Element.builder().item(finisher).onClick(action -> {
                    player.closeInventory();
                    player.sendMessage(TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.tradeInv.failedTradeMessage));
                }).build();
            }

            layout.set(doTrade, 49);
            layout.set(Element.builder().item(ItemStack.builder().itemType(ItemTypes.ARROW).add(Keys.ROTATION, Rotations.BOTTOM).add(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(plugin.getConfigManager().getMessages().inventory.tradeInv.displayNames.inTradeMenuReturnDisplay)).build()).onClick(action -> mainTradingGui(player)).build(), 4);

            Page tradeGui = Page.builder().archetype(archetype).layout(layout.build()).property(title).build(plugin.getPluginContainer());
            try {
                tradeGui.define(elements);
                tradeGui.open(player);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private static Tuple<Boolean, ArrayList<ItemStack>> doTrade(Trade trade, Player player) {
        ArrayList<ItemStack> toGive = InventoryUtils.convertJsonArrayToItemArray(trade.getGivenItems());
        ArrayList<ItemStack> required = InventoryUtils.convertJsonArrayToItemArray(trade.getRequiredItems());
        ArrayList<ItemStack> failedStacks = new ArrayList<>();
        int count = 0;
        for (ItemStack stack : required) {
            if (player.getInventory().contains(stack)){
                player.getInventory().queryAny(stack).poll(stack.getQuantity());
            }
        }
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
        return new Tuple<>(count == toGive.size(), failedStacks);
    }

    private static int getMakeableTradesCount(Trade trade, Player player) {
        ArrayList<ItemStack> required = InventoryUtils.convertJsonArrayToItemArray(trade.getRequiredItems());
        int max = 0;
        for (ItemStack stack : required)
            max = Math.min(max, (int) Math.floor(player.getInventory().queryAny(stack).peek().get().getQuantity() / (double) stack.getQuantity()));
        return max;
    }
}