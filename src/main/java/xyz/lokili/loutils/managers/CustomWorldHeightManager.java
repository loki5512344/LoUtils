package xyz.lokili.loutils.managers;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.lokili.loutils.LoUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class CustomWorldHeightManager {
    
    private final LoUtils plugin;
    private final Map<String, WorldHeightConfig> worldConfigs;
    private final Map<Pattern, WorldHeightConfig> regexConfigs;
    
    public CustomWorldHeightManager(LoUtils plugin) {
        this.plugin = plugin;
        this.worldConfigs = new HashMap<>();
        this.regexConfigs = new HashMap<>();
        loadConfigs();
    }
    
    private void loadConfigs() {
        worldConfigs.clear();
        regexConfigs.clear();
        
        FileConfiguration config = plugin.getConfigManager().getConfig("conf/customworldheight.yml");
        if (config == null) {
            plugin.getLogger().warning("CustomWorldHeight config not found!");
            return;
        }
        
        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) continue;
            
            WorldHeightConfig heightConfig = new WorldHeightConfig(
                section.getString("world"),
                section.getString("regex"),
                section.getInt("height", 384),
                section.getInt("min-y", -64),
                section.getInt("logical-height", 384),
                section.getString("cloud-height", "default"),
                section.getString("dimension-type", "custom")
            );
            
            // Если указан regex, добавляем в regex configs
            if (heightConfig.regex != null && !heightConfig.regex.isEmpty()) {
                try {
                    Pattern pattern = Pattern.compile(heightConfig.regex);
                    regexConfigs.put(pattern, heightConfig);
                } catch (Exception e) {
                    plugin.getLogger().warning("Invalid regex pattern: " + heightConfig.regex);
                }
            }
            
            // Если указано точное имя мира, добавляем в world configs
            if (heightConfig.worldName != null && !heightConfig.worldName.isEmpty()) {
                worldConfigs.put(heightConfig.worldName, heightConfig);
            }
        }
        
        plugin.getLogger().info("Loaded " + worldConfigs.size() + " world height configs and " 
                + regexConfigs.size() + " regex patterns");
    }
    
    public WorldHeightConfig getConfig(String worldName) {
        // Сначала проверяем точное совпадение
        if (worldConfigs.containsKey(worldName)) {
            return worldConfigs.get(worldName);
        }
        
        // Затем проверяем regex паттерны
        for (Map.Entry<Pattern, WorldHeightConfig> entry : regexConfigs.entrySet()) {
            if (entry.getKey().matcher(worldName).matches()) {
                return entry.getValue();
            }
        }
        
        return null;
    }
    
    public void reload() {
        loadConfigs();
    }
    
    public static class WorldHeightConfig {
        public final String worldName;
        public final String regex;
        public final int height;
        public final int minY;
        public final int logicalHeight;
        public final String cloudHeight;
        public final String dimensionType;
        
        public WorldHeightConfig(String worldName, String regex, int height, int minY, 
                                int logicalHeight, String cloudHeight, String dimensionType) {
            this.worldName = worldName;
            this.regex = regex;
            this.height = height;
            this.minY = minY;
            this.logicalHeight = logicalHeight;
            this.cloudHeight = cloudHeight;
            this.dimensionType = dimensionType;
        }
    }
}
