package xyz.lokili.loutils.api;

import org.bukkit.configuration.file.FileConfiguration;

public interface IConfigManager {
    void reloadAll();
    FileConfiguration getConfig(String path);
    void saveConfig(String path);
    
    String getPrefix();
    String getMessage(String path);
    String getMessage(String path, String... replacements);
    String getRandomMessage(String path);
    String getRandomDeathMessage(String path, String... replacements);
    
    boolean isModuleEnabled(String module);
    
    // Specific config getters
    FileConfiguration getWhitelistConfig();
    FileConfiguration getAutoRestartConfig();
    FileConfiguration getDeathMessagesConfig();
    FileConfiguration getMessagesConfig();
}
