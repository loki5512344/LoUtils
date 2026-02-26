package xyz.lokili.loutils.services;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;
import xyz.lokili.loutils.constants.ConfigConstants;

import java.util.List;
import java.util.Random;

/**
 * Handles message retrieval and formatting
 * Single Responsibility: Message operations
 */
public class MessageService {
    
    private final Plugin plugin;
    private final ConfigLoader configLoader;
    private final Random random;
    
    public MessageService(Plugin plugin, ConfigLoader configLoader) {
        this.plugin = plugin;
        this.configLoader = configLoader;
        this.random = new Random();
    }
    
    public String getPrefix() {
        return plugin.getConfig().getString("prefix", "&#3BA8FF[LoUtils] &7");
    }
    
    public String getMessage(String path) {
        FileConfiguration messages = configLoader.getConfig(ConfigConstants.MESSAGES_CONFIG);
        if (messages == null) {
            return "&cMessages config not loaded";
        }
        return messages.getString(path, "&cMessage not found: " + path);
    }
    
    public String getMessage(String path, String... replacements) {
        String message = getMessage(path);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return message;
    }
    
    public String getRandomMessage(String path) {
        FileConfiguration messages = configLoader.getConfig(ConfigConstants.MESSAGES_CONFIG);
        if (messages == null) {
            return "&cMessages config not loaded";
        }
        
        List<String> messageList = messages.getStringList(path);
        if (messageList.isEmpty()) {
            return getMessage(path);
        }
        return messageList.get(random.nextInt(messageList.size()));
    }
    
    public String getRandomDeathMessage(String path, String... replacements) {
        FileConfiguration deathConfig = configLoader.getConfig(ConfigConstants.DEATH_MESSAGES_CONFIG);
        if (deathConfig == null) {
            return "&c{victim} погиб";
        }
        
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
    
    public String getConfigMessage(String configPath, String messagePath) {
        FileConfiguration config = configLoader.getConfig(configPath);
        if (config == null) {
            return "&cConfig not found: " + configPath;
        }
        return config.getString(messagePath, "&cMessage not found: " + messagePath);
    }
    
    public String getConfigMessage(String configPath, String messagePath, String... replacements) {
        String message = getConfigMessage(configPath, messagePath);
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        return message;
    }
}
