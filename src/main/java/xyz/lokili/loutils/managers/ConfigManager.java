package xyz.lokili.loutils.managers;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.IConfigManager;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.services.ConfigLoader;
import xyz.lokili.loutils.services.MessageService;
import xyz.lokili.loutils.services.ModuleRegistry;

/**
 * Facade for configuration management
 * Delegates to specialized services (SOLID - Single Responsibility)
 */
public class ConfigManager implements IConfigManager {
    
    private final ConfigLoader configLoader;
    private final MessageService messageService;
    private final ModuleRegistry moduleRegistry;
    
    public ConfigManager(LoUtils plugin) {
        this.configLoader = new ConfigLoader(plugin);
        this.messageService = new MessageService(plugin, configLoader);
        this.moduleRegistry = new ModuleRegistry(plugin);
        
        configLoader.loadAllConfigs();
    }
    
    // === Delegation to ConfigLoader ===
    
    public void reloadAll() {
        configLoader.reloadAll();
    }
    
    public FileConfiguration getConfig(String path) {
        return configLoader.getConfig(path);
    }
    
    public FileConfiguration getWhitelistConfig() {
        return configLoader.getConfig(ConfigConstants.WHITELIST_CONFIG);
    }
    
    public FileConfiguration getAutoRestartConfig() {
        return configLoader.getConfig(ConfigConstants.AUTORESTART_CONFIG);
    }
    
    public FileConfiguration getStatsConfig() {
        return configLoader.getConfig("conf/stats.yml");
    }
    
    public FileConfiguration getDeathMessagesConfig() {
        return configLoader.getConfig(ConfigConstants.DEATH_MESSAGES_CONFIG);
    }
    
    public FileConfiguration getPartyConfig() {
        return configLoader.getConfig("conf/party.yml");
    }
    
    public FileConfiguration getMessagesConfig() {
        return configLoader.getConfig(ConfigConstants.MESSAGES_CONFIG);
    }
    
    public void saveConfig(String path) {
        configLoader.saveConfig(path);
    }
    
    // === Delegation to MessageService ===
    
    public String getPrefix() {
        return messageService.getPrefix();
    }
    
    public String getMessage(String path) {
        return messageService.getMessage(path);
    }
    
    public String getMessage(String path, String... replacements) {
        return messageService.getMessage(path, replacements);
    }
    
    public String getRandomMessage(String path) {
        return messageService.getRandomMessage(path);
    }
    
    public String getRandomDeathMessage(String path, String... replacements) {
        return messageService.getRandomDeathMessage(path, replacements);
    }
    
    // === Delegation to ModuleRegistry ===
    
    public boolean isModuleEnabled(String module) {
        return moduleRegistry.isModuleEnabled(module);
    }
    
    // === Service Getters (for direct access if needed) ===
    
    public ConfigLoader getConfigLoader() {
        return configLoader;
    }
    
    public MessageService getMessageService() {
        return messageService;
    }
    
    public ModuleRegistry getModuleRegistry() {
        return moduleRegistry;
    }
}
