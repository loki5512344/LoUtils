package xyz.lokili.loutils.services;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.utils.validation.ConfigValidatorV2;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * Handles loading and saving of configuration files
 * Single Responsibility: Config file I/O operations
 */
public class ConfigLoader {
    
    private final Plugin plugin;
    private final Map<String, FileConfiguration> configs;
    private final Map<String, File> configFiles;
    private final ConfigValidatorV2 validator;
    
    public ConfigLoader(Plugin plugin) {
        this.plugin = plugin;
        this.configs = new HashMap<>();
        this.configFiles = new HashMap<>();
        this.validator = new ConfigValidatorV2(plugin);
    }
    
    public void loadAllConfigs() {
        // Main config
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        
        // Load all module configs
        loadConfig(ConfigConstants.MESSAGES_CONFIG);
        loadConfig(ConfigConstants.WHITELIST_CONFIG);
        loadConfig(ConfigConstants.AUTORESTART_CONFIG);
        loadConfig(ConfigConstants.DEATH_MESSAGES_CONFIG);
        loadConfig(ConfigConstants.ENCHANT_CONFIG);
        loadConfig(ConfigConstants.TPSBAR_CONFIG);
        loadConfig(ConfigConstants.WORLDLOCK_CONFIG);
        loadConfig(ConfigConstants.CUSTOMWORLDHEIGHT_CONFIG);
        loadConfig("conf/sleeppercentage.yml");
        loadConfig("conf/fastleafdecay.yml");
        loadConfig("conf/cauldron.yml");
        loadConfig("conf/villagerleash.yml");
        loadConfig(ConfigConstants.LIGHT_BLOCK_CONFIG);
        loadConfig(ConfigConstants.DEBUG_STICK_CONFIG);
        loadConfig(ConfigConstants.INVISIBLE_FRAMES_CONFIG);
        loadConfig(ConfigConstants.POSES_CONFIG);
        loadConfig(ConfigConstants.CUSTOM_CRAFTS_CONFIG);
        loadConfig(ConfigConstants.ANVIL_REPAIR_CONFIG);
        loadConfig(ConfigConstants.ENHANCED_BONE_MEAL_CONFIG);
        loadConfig(ConfigConstants.FRAME_LOCKING_CONFIG);
        loadConfig(ConfigConstants.MAP_LOCKING_CONFIG);
        loadConfig(ConfigConstants.NAME_TAG_REMOVAL_CONFIG);
        loadConfig(ConfigConstants.ENHANCED_HOES_CONFIG);
        loadConfig(ConfigConstants.COW_MILKING_CONFIG);
        loadConfig(ConfigConstants.INVENTORY_CHECK_STICK_CONFIG);
        loadConfig(ConfigConstants.HANDCUFFS_CONFIG);
        
        // Validate all configs after loading
        validator.validateAll(configs);
    }
    
    public FileConfiguration loadConfig(String path) {
        if (path == null || path.isEmpty()) {
            plugin.getLogger().warning("Attempted to load config with null/empty path");
            return null;
        }
        
        File file = new File(plugin.getDataFolder(), path);
        
        if (!file.exists()) {
            file.getParentFile().mkdirs();
            try {
                plugin.saveResource(path, false);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Could not save default config for " + path + ": " + e.getMessage());
            }
        }
        
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        
        // Load defaults from jar
        boolean hasDefaults = false;
        try (InputStream defaultStream = plugin.getResource(path)) {
            if (defaultStream != null) {
                YamlConfiguration defaultConfig = YamlConfiguration.loadConfiguration(
                        new InputStreamReader(defaultStream, StandardCharsets.UTF_8));
                config.setDefaults(defaultConfig);
                hasDefaults = true;
            }
        } catch (IOException e) {
            plugin.getLogger().warning("Could not load defaults for " + path + ": " + e.getMessage());
        }
        
        // Автоматически добавлять недостающие ключи из дефолтов
        if (hasDefaults) {
            config.options().copyDefaults(true);
            
            // Сохраняем конфиг если были добавлены новые ключи
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().warning("Could not save updated config " + path + ": " + e.getMessage());
            }
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
        if (path == null) {
            return null;
        }
        return configs.get(path);
    }
    
    public void saveConfig(String path) {
        if (path == null) {
            plugin.getLogger().warning("Attempted to save config with null path");
            return;
        }
        
        FileConfiguration config = configs.get(path);
        File file = configFiles.get(path);
        if (config != null && file != null) {
            try {
                config.save(file);
            } catch (IOException e) {
                plugin.getLogger().severe("Could not save " + path + ": " + e.getMessage());
            }
        } else {
            plugin.getLogger().warning("Config or file not found for path: " + path);
        }
    }
    
    public Map<String, FileConfiguration> getAllConfigs() {
        return new HashMap<>(configs);
    }
}
