package io.github.bennyboy1695.shadowtrades.Config;

import com.google.common.reflect.TypeToken;
import com.google.gson.*;
import io.github.bennyboy1695.shadowtrades.ShadowTrades;
import io.github.bennyboy1695.shadowtrades.Util.InventoryUtils;
import ninja.leaping.configurate.ConfigurationNode;
import ninja.leaping.configurate.ConfigurationOptions;
import ninja.leaping.configurate.commented.CommentedConfigurationNode;
import ninja.leaping.configurate.gson.GsonConfigurationLoader;
import ninja.leaping.configurate.hocon.HoconConfigurationLoader;
import ninja.leaping.configurate.loader.ConfigurationLoader;
import ninja.leaping.configurate.objectmapping.ObjectMappingException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.UUID;

public class ConfigManager {

    private ShadowTrades plugin;
    private File configDirectory;
    private CoreConfig coreConfig;
    private MessagesConfig messagesConfig;

    public ConfigManager(ShadowTrades plugin) {
        this.plugin = plugin;
        this.configDirectory = plugin.getConfigDirectory().toFile();
        if (!this.configDirectory.exists()) {
            this.configDirectory.mkdirs();
        }
    }

    public boolean loadCore() {
        try {
            File file = new File(plugin.getConfigDirectory().toFile(), "ShadowTrade.conf");
            if (!file.exists()) {
                file.createNewFile();
            }
            ConfigurationLoader<CommentedConfigurationNode> loader = ((HoconConfigurationLoader.Builder) HoconConfigurationLoader.builder().setFile(file)).build();
            CommentedConfigurationNode config = (CommentedConfigurationNode) loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(plugin.factory).setShouldCopyDefaults(true));
            this.coreConfig = ((CoreConfig) config.getValue(TypeToken.of(CoreConfig.class), new CoreConfig()));
            loader.save(config);
            return true;
        } catch (IOException | ObjectMappingException ex) {
            plugin.getLogger().error("Failed to load core configuration.", ex);
        }
        return false;
    }

    public boolean loadMessages() {
        try {
            File file = new File(plugin.getConfigDirectory().toFile(), "Messages.conf");
            if (!file.exists()) {
                file.createNewFile();
            }
            ConfigurationLoader<CommentedConfigurationNode> loader = ((HoconConfigurationLoader.Builder) HoconConfigurationLoader.builder().setFile(file)).build();
            CommentedConfigurationNode config = (CommentedConfigurationNode) loader.load(ConfigurationOptions.defaults().setObjectMapperFactory(plugin.factory).setShouldCopyDefaults(true));
            this.messagesConfig = ((MessagesConfig) config.getValue(TypeToken.of(MessagesConfig.class), new MessagesConfig()));
            loader.save(config);
            return true;
        } catch (IOException | ObjectMappingException ex) {
            plugin.getLogger().error("Failed to load messages configuration.", ex);
        }
        return false;
    }

    public CoreConfig getCore() {
        return this.coreConfig;
    }

    public MessagesConfig getMessages() {
        return this.messagesConfig;
    }
}
