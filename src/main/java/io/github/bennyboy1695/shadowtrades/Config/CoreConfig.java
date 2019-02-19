package io.github.bennyboy1695.shadowtrades.Config;

import ninja.leaping.configurate.objectmapping.Setting;
import ninja.leaping.configurate.objectmapping.serialize.ConfigSerializable;

@ConfigSerializable
public class CoreConfig {

    @Setting
    public TradeCommand tradeCommand = new TradeCommand();

    @ConfigSerializable
    public static class TradeCommand {

        @Setting(value = "placeItemsInEnderIfFull", comment = "This will allow the plugin to place items into the players enderchest if their inventory is full!")
        public boolean placeItemsInEnderIfFull = false;
    }
}
