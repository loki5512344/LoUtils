package xyz.lokili.loutils.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.IWhitelistManager;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.managers.base.BaseStorageManager;

import java.io.File;
import java.io.IOException;
import java.util.Set;

/**
 * Manages whitelist using BaseStorageManager (DRY principle)
 */
public class WhitelistManager extends BaseStorageManager implements IWhitelistManager {
    
    private final LoUtils plugin;
    private File whitelistFile;
    private FileConfiguration whitelistConfig;
    private boolean enabled;
    
    public WhitelistManager(LoUtils plugin) {
        super(plugin, "data/whitelist.yml", "players", false);
        this.plugin = plugin;
        loadWhitelist();
    }
    
    private void loadWhitelist() {
        whitelistFile = new File(plugin.getDataFolder(), "data/whitelist.yml");
        
        if (!whitelistFile.exists()) {
            whitelistFile.getParentFile().mkdirs();
            try {
                whitelistFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create whitelist.yml: " + e.getMessage());
            }
        }
        
        whitelistConfig = YamlConfiguration.loadConfiguration(whitelistFile);
        load(whitelistConfig);
        
        enabled = plugin.getConfigManager().getWhitelistConfig().getBoolean("enabled", true);
    }
    
    @Override
    protected FileConfiguration getConfig() {
        return whitelistConfig;
    }
    
    @Override
    protected void saveConfig(FileConfiguration config) {
        try {
            config.save(whitelistFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save whitelist.yml: " + e.getMessage());
        }
    }
    
    @Override
    protected String processItem(String item) {
        return item.toLowerCase(); // Player names are case-insensitive
    }
    
    // === IWhitelistManager Implementation ===
    
    @Override
    public void saveWhitelist() {
        save(whitelistConfig);
    }
    
    @Override
    public boolean addPlayer(String playerName) {
        return add(playerName);
    }
    
    @Override
    public boolean removePlayer(String playerName) {
        return remove(playerName);
    }
    
    @Override
    public boolean isWhitelisted(String playerName) {
        return contains(playerName);
    }
    
    @Override
    public Set<String> getWhitelistedPlayers() {
        return getAll();
    }
    
    public int getPlayerCount() {
        return size();
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        FileConfiguration config = plugin.getConfigManager().getWhitelistConfig();
        config.set("enabled", enabled);
        plugin.getConfigManager().saveConfig(ConfigConstants.WHITELIST_CONFIG);
    }
    
    @Override
    public void reload() {
        loadWhitelist();
    }
}
