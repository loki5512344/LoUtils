package xyz.lokili.loutils.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.ColorUtil;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

public class ConfigManager {
    
    private final LoUtils plugin;
    private final Map<String, FileConfiguration> configs;
    private final Map<String, File> configFiles;
    private final Random random;
    
    private FileConfiguration messagesConfig;
    
    public ConfigManager(LoUtils plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.configFiles = new HashMap<>();
        this.random = new Random();
        loadAllConfigs();
    }
    
    public void loadAllConfigs() {
        // Main config
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        
        // Messages
        messagesConfig = loadConfig("messages.yml");
        
        // Module configs
        loadConfig("conf/whitelist.yml");
        loadConfig("conf/autorestart.yml");
        loadConfig("conf/dimensionlock.yml");
        loadConfig("conf/vanish.yml");
        loadConfig("conf/stats.yml");
        loadConfig("conf/deathmessages.yml");
        loadConfig("conf/party.yml");
        loadConfig("conf/enchant.yml");
        loadConfig("conf/tpsbar.yml");
    }
    
    private FileConfiguration loadConfig(String path) {
        File file = new File(plugin.getDataFolder(), path);
        
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            plugin.saveResource(path, false);
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        // Load defaults from jar
        InputStream defaultStream = plugin.getResource(path);
        if (defaultStream != null) {
            YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                    new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
            config.setDefaults(defaultConfig);
        }
        
        configs.put(path, config);
        configFiles.put(path, file);
        
        return config;
    }
    
    public void reloadAll() {
        plugin.reloadConfig();
        configs.clear();
        configFiles.clear();
        loadAllConfigs();
    }
    
    public FileConfiguration getConfig(String path) {
        return configs.get(path);
    }
    
    public FileConfiguration getWhitelistConfig() {
        return configs.get("conf/whitelist.yml");
    }
    
    public FileConfiguration getAutoRestartConfig() {
        return configs.get("conf/autorestart.yml");
    }
    
    public FileConfiguration getDimensionLockConfig() {
        return configs.get("conf/dimensionlock.yml");
    }
    
    public FileConfiguration getVanishConfig() {
        return configs.get("conf/vanish.yml");
    }
    
    public FileConfiguration getStatsConfig() {
        return configs.get("conf/stats.yml");
    }
    
    public FileConfiguration getDeathMessagesConfig() {
        return configs.get("conf/deathmessages.yml");
    }
    
    public FileConfiguration getPartyConfig() {
        return configs.get("conf/party.yml");
    }
    
    public FileConfiguration getMessagesConfig() {
        return messagesConfig;
    }
    
    public void saveConfig(String path) {
        FileConfiguration config = configs.get(path);
        File file = configFiles.get(path);
        if (config != null && file != null) {
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save " + path + ": " + e.getMessage());
            }
        }
    }
    
    // === Message helpers ===
    
    public String getPrefix() {
        return plugin.getConfig().getString("prefix", "&#3BA8FF[LoUtils] &7");
    }
    
    public String getMessage(String path) {
        return messagesConfig.getString(path, "&cMessage not found: " + path);
    }
    
    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return message;
    }
    
    public String getRandomMessage(String path) {
        List<String> messages = messagesConfig.getStringList(path);
        if (messages.isEmpty()) {
            return getMessage(path);
        }
        return messages.get(random.nextInt(messages.size()));
    }
    
    public String getRandomDeathMessage(String path, String... replacements) {
        FileConfiguration deathConfig = getDeathMessagesConfig();
        List<String> messages = deathConfig.getStringList(path);
        if (messages.isEmpty()) {
            return "&c{victim} погиб";
        }
        String message = messages.get(random.nextInt(messages.size()));
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return message;
    }
    
    public boolean isModuleEnabled(String module) {
        return plugin.getConfig().getBoolean("modules." + module, true);
    }
}
