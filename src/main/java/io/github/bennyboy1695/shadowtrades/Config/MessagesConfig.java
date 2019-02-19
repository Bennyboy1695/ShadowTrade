package io.github.bennyboy1695.shadowtrades.Config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class MessagesConfig {

    @Setting
    public TradeCommand tradeCommand = new TradeCommand();

    @ConfigSerializable
    public static class TradeCommand {

        @Setting
        public GetItemsCommand getItemsCommand = new GetItemsCommand();

        @ConfigSerializable
        public static class GetItemsCommand {

            @Setting(value = "gotAllItemsMessage", comment = "The message sent to the player if they get all the item's successfully!")
            public String getAllItemsMessage = "&2You now have received all the item's from your trade!";

            @Setting(value = "notEnoughRoomMessage", comment = "The message sent to the player if they didn't have enough room to get all the items!")
            public String notEnoughRoomMessage = "&cYou still didn't have enough space in your inventory! But you can run this command again to receive the remaining item's when you have space again!";

            @Setting(value = "noFailedTradesFoundMessage", comment = "The message sent to the player when there is no failed trades found matching their uuid!")
            public String noFailedTradesMessage = "&cYou have no failed trades available!";
        }

    }

    @Setting
    public Inventory inventory = new Inventory();

    @ConfigSerializable
    public static class Inventory  {

        @Setting
        public TradeInv tradeInv = new TradeInv();

        @ConfigSerializable
        public static class TradeInv {

            @Setting
            public DisplayNames displayNames = new DisplayNames();

            @ConfigSerializable
            public static class DisplayNames {

                @Setting(value = "mainInvTitle", comment = "The name displayed in the trade gui!")
                public String mainInvTitle = "&6Available Trades";

                @Setting(value = "tradingInvTitle", comment = "The name displayed in the gui when trading item's!")
                public String tradingGuiTitle = "&2Trade: <DisplayItem>";

                @Setting(value = "successfulTradeFinishItemDisplayName", comment = "The Display name of the item that is shown when making a trade if you have the items needed for it!")
                public String successTradeFinishDisplay = "&2Click me to finish your trade and receive your items!";

                @Setting(value = "failedTradeFinishItemDisplayName", comment = "The Display name of the item that is shown when making a trade if you do not have the items needed for it!")
                public String failTradeFinishDisplay = "&cYou do not have enough of the required items in your inventory!";

                @Setting(value = "inTradeMenuReturnDisplayName", comment = "The display name of the item that returns you to the main menu when in a trade menu!")
                public String inTradeMenuReturnDisplay = "&2Return to Main Menu!";
            }

            @Setting(value = "successfulTradeMessage", comment = "The message sent to the player when they have made a successful trade!")
            public String successfulTradeMessage = "&2Successfully traded!";

            @Setting(value = "failedToGiveItemsMessage", comment = "The message sent to the player when they have made a successful trade, but the plugin couldn't give them their item's due to a lack of space!")
            public String failedToGiveItemsMessage = "&cYou didn't have enough space in your inventory for some of the item's! We have saved those item's and once you have enough space run the command &6/trade getitems &cto get the remaining item's!";

            @Setting(value = "failedTradeMessage", comment = "The message sent to the player when they have clicked the barrier item to close the failed trade!")
            public String failedTradeMessage = "&cYou didn't have enough of the required item's so we closed the trade menu so that you can get the item's!";

            @Setting(value = "enderChestMessage", comment = "The message sent to the player if the items were placed in to their enderchest! NOTE: Requires placeItemsInEnderIfFull to be true in the main config!")
            public String enderChestMessage = "&2You didn't have enough space in your inventory so the item's were placed in your enderchest!";

        }

        @Setting
        public SetupInv setupInv = new SetupInv();

        @ConfigSerializable
        public static class SetupInv {

            @Setting(value = "displayItemInvTitle", comment = "The name displayed in the gui when your are setting up the display item!")
            public String displayItemInvTitle = "&6Trade Setup: Display Item";

            @Setting(value = "requiredItemsInvTitle", comment = "The name displayed in the gui when your are setting up the display item!")
            public String requiredItemsInvTitle = "&6Trade Setup: Required Items";

            @Setting(value = "givenItemsInvTitle", comment = "The name displayed in the gui when your are setting up the display item!")
            public String givenItemsInvTitle = "&6Trade Setup: Given Items";

            @Setting(value = "successfulSetupReply", comment = "The message you get when you have successfully made a trade!")
            public String successfulSetupReply = "&2You have successfully setup a trade!";

            @Setting(value = "tooManyItemsReply", comment = "The message you get when there is too many item's in one of the setup stages! NOTE: This should never be a message you get, but is there just incase.")
            public String tooManyItemsReply = "&cThere was too many items in this trade, the max allowed is 12!";
        }

    }
}
