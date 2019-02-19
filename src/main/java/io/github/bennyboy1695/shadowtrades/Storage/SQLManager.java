package io.github.bennyboy1695.shadowtrades.Storage;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import io.github.bennyboy1695.shadowtrades.ShadowTrades;
import io.github.bennyboy1695.shadowtrades.Util.InventoryUtils;
import io.github.bennyboy1695.shadowtrades.Util.Trade;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;

import java.io.File;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

public class SQLManager {

    private ShadowTrades plugin;
    private Database database;
    private String tradeTable = "tradeTable";
    private String failedStacksTable = "failedStacksTable";

    public SQLManager(ShadowTrades plugin, String dbName, String tradeTable, String failedStacksTable) {
        this.plugin = plugin;
        this.tradeTable = tradeTable;
        this.failedStacksTable = failedStacksTable;

        File dataFolder = ShadowTrades.getInstance().getConfigDirectory().toFile();
        try {
            File dbFile = new File(dataFolder + "/" + dbName + ".db");
            database = new SQLite(dbFile, dataFolder);
        } catch (Exception e) {
            ShadowTrades.getInstance().getLogger().warn("Failed Creating DB File!");
        }
        try {
            writeTables(tradeTable, failedStacksTable);
        } catch (SQLException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    private void writeTables(String tradeTable, String failedTradesTable) throws SQLException, ClassNotFoundException {
        String CreateTradesTable = "CREATE TABLE IF NOT EXISTS `" + tradeTable + "` (" +
                "`id` varchar(8)," +
                "`displayitem` varchar(10000)," +
                "`requireditems` varchar(100000)," +
                "`givenitems` varchar(100000)" +
                ");";

        String CreateFailedStacksTable = "CREATE TABLE IF NOT EXISTS `" + failedTradesTable + "` (" +
                "`uuid` varchar(36)," +
                "`faileditems` varchar(1000000)" +
                ");";

        database.openConnection();
        database.updateSQL(CreateTradesTable);
        database.updateSQL(CreateFailedStacksTable);
        database.closeConnection();
    }

    public Trade addNewTrade(Trade trade) {
        try {
            database.openConnection();
            PreparedStatement ps = database.getConnection().prepareStatement("INSERT INTO " + tradeTable + " (id, displayitem, requireditems, givenitems) VALUES(?,?,?,?)");
            ps.setString(1, trade.getId());
            ps.setString(2, InventoryUtils.serializeItemStack(trade.getDisplayItem()).get().toString());
            ps.setString(3, String.valueOf(trade.getRequiredItems()));
            ps.setString(4, String.valueOf(trade.getGivenItems()));
            ps.executeUpdate();
            database.closeConnection();
            return trade;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return trade;
    }

    public ArrayList<Trade> getTrades() {
        ArrayList<Trade> trades = new ArrayList<>();
        try {
            database.openConnection();
            PreparedStatement ps = database.getConnection().prepareStatement("SELECT * FROM " + this.tradeTable + ";");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                JsonParser parser = new JsonParser();
                JsonObject itemStack = parser.parse(rs.getString("displayitem")).getAsJsonObject();
                JsonArray requiredItems = parser.parse(rs.getString("requireditems")).getAsJsonArray();
                JsonArray givenItems = parser.parse(rs.getString("givenitems")).getAsJsonArray();
                Trade trade = new Trade(InventoryUtils.deserializeItemStack(itemStack).get(), requiredItems , givenItems);
                trade.setId(rs.getString("id"));
                trades.add(trade);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return trades;
    }

    public boolean removeTrade(String tradeID) {
        try {
            database.openConnection();
            PreparedStatement ps = database.getConnection().prepareStatement("DELETE FROM " + tradeTable + " WHERE id='" + tradeID + "';");
            ps.executeUpdate();
            database.closeConnection();
            Trade trade = plugin.getTrades().stream().filter(findTrade -> findTrade.getId().equals(tradeID)).findAny().get();
            plugin.getTrades().remove(trade);
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean addNewFailedTrade(Player player, ArrayList<ItemStack> failedStacks) {
        try {
            database.openConnection();
            PreparedStatement ps = database.getConnection().prepareStatement("INSERT INTO " + failedStacksTable + " (uuid, faileditems) VALUES(?,?)");
            ps.setString(1, player.getUniqueId().toString());
            ps.setString(2, InventoryUtils.convertItemArrayToJsonArray(failedStacks).toString());
            ps.executeUpdate();
            database.closeConnection();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean removeFailedTrade(Player player, ArrayList<ItemStack> failedStacks) {
        try {
            database.openConnection();
            PreparedStatement ps = database.getConnection().prepareStatement("DELETE FROM " + failedStacksTable + " WHERE uuid='" + player.getUniqueId().toString() + " AND faileditems='" + InventoryUtils.convertItemArrayToJsonArray(failedStacks).toString() + "';");
            ps.executeUpdate();
            database.closeConnection();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean doesPlayerHaveAFailedStack(Player player) {
        try {
            database.openConnection();
            PreparedStatement ps = database.getConnection().prepareStatement("SELECT * FROM " + failedStacksTable + " WHERE uuid='" + player.getUniqueId().toString() +"';");
            ResultSet rs = ps.executeQuery();
            return rs.next();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public ArrayList<ItemStack> getFailedStacks(Player player) {
        ArrayList<ItemStack> itemStacks = new ArrayList<>();
        try {
            database.openConnection();
            PreparedStatement ps = database.getConnection().prepareStatement("SELECT * FROM " + failedStacksTable + " WHERE uuid='" + player.getUniqueId().toString() +"';");
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                JsonParser parser = new JsonParser();
                JsonArray faileditems = parser.parse(rs.getString("faileditems")).getAsJsonArray();
                itemStacks = InventoryUtils.convertJsonArrayToItemArray(faileditems);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return itemStacks;
    }
}
