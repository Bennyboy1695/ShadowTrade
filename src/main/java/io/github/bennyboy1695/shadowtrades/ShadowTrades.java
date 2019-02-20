package io.github.bennyboy1695.shadowtrades;

import com.google.inject.Inject;
import io.github.bennyboy1695.shadowtrades.Command.ShadowTradeCommand;
import io.github.bennyboy1695.shadowtrades.Command.TradeCommand;
import io.github.bennyboy1695.shadowtrades.Config.ConfigManager;
import io.github.bennyboy1695.shadowtrades.Storage.SQLManager;
import io.github.bennyboy1695.shadowtrades.Util.InventoryUtils;
import io.github.bennyboy1695.shadowtrades.Util.Trade;
import ninja.leaping.configurate.objectmapping.GuiceObjectMapperFactory;
import org.slf4j.Logger;
import org.spongepowered.api.Sponge;
import org.spongepowered.api.config.ConfigDir;
import org.spongepowered.api.event.Listener;
import org.spongepowered.api.event.game.GameReloadEvent;
import org.spongepowered.api.event.game.state.GameStartedServerEvent;
import org.spongepowered.api.plugin.Dependency;
import org.spongepowered.api.plugin.Plugin;
import org.spongepowered.api.plugin.PluginContainer;
import org.spongepowered.api.service.pagination.PaginationService;

import java.nio.file.Path;
import java.util.ArrayList;

@Plugin(id = "shadowtrades", name = "ShadowTrades", description = "A trade plugin that allows you to give an item in exchange for another.", authors = {"Bennyboy1695"}, dependencies = @Dependency(id = "teslalibs"))
public class ShadowTrades {

    @Inject
    public GuiceObjectMapperFactory factory;
    @Inject
    @ConfigDir(sharedRoot = false)
    private Path configDirectory;
    @Inject
    private Logger logger;
    @Inject
    private PluginContainer pluginContainer;
    private ConfigManager configManager;
    private SQLManager sqlManager;
    private ArrayList<Trade> trades = new ArrayList<>();
    private static ShadowTrades instance;


    @Listener
    public void onServerStart(GameStartedServerEvent event) {
        instance = this;
        //Configs
        logger.info("Loading Configs ...");
        configManager = new ConfigManager(this);
        if (configManager.loadCore() && configManager.loadMessages()) {
            logger.info("Loaded Configs!");
        } else {
            logger.warn("Failed to load configs!");
        }

        new InventoryUtils(this);

        //Database
        logger.info("Loading SQLLite Storage");
        sqlManager = new SQLManager(this,"Trades", "trades", "failedStacks");

        logger.info("Loading Trades");
        trades = sqlManager.getTrades();

        //Commands
        logger.info("Registering Commands!");
        new ShadowTradeCommand(this).register();
        new TradeCommand(this).register();

        //Listeners
        Sponge.getEventManager().registerListeners(this, new ShadowTradeCommand(this));
    }

    @Listener
    public void onReload(GameReloadEvent event) {
        doReload();
    }

    public void doReload() {
        logger.info("Reloading Configs ...");
        configManager = new ConfigManager(this);
        if (configManager.loadCore() && configManager.loadMessages()) {
            logger.info("Reloaded Configs!");
        } else {
            logger.warn("Failed to reload configs!");
        }
        logger.info("Reloading SQLLite Storage");
        sqlManager = getSqlManager();

        logger.info("Reloading Trades");
        trades = getSqlManager().getTrades();
    }

    public SQLManager getSqlManager() {
        return sqlManager;
    }

    public Logger getLogger() {
        return logger;
    }

    public Path getConfigDirectory() {
        return configDirectory;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public ArrayList<Trade> getTrades() {
        return trades;
    }

    public PluginContainer getPluginContainer() {
        return pluginContainer;
    }

    public static ShadowTrades getInstance() {
        return instance;
    }

    public PaginationService getPaginationService() {
        return Sponge.getServiceManager().provide(PaginationService.class).get();
    }
}
