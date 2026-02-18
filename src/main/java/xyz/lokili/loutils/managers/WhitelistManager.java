package xyz.lokili.loutils.managers;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.IWhitelistManager;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class WhitelistManager implements IWhitelistManager {
    
    private final LoUtils plugin;
    private final Set<String> whitelistedPlayers;
    private File whitelistFile;
    private FileConfiguration whitelistConfig;
    private boolean enabled;
    
    public WhitelistManager(LoUtils plugin) {
        this.plugin = plugin;
        this.whitelistedPlayers = new HashSet<>();
        loadWhitelist();
    }
    
    public void loadWhitelist() {
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
        
        whitelistedPlayers.clear();
        List<String> players = whitelistConfig.getStringList("players");
        whitelistedPlayers.addAll(players.stream().map(String::toLowerCase).toList());
        
        enabled = plugin.getConfigManager().getWhitelistConfig().getBoolean("enabled", true);
    }
    
    @Override
    public void saveWhitelist() {
        whitelistConfig.set("players", new ArrayList<>(whitelistedPlayers));
        try {
            whitelistConfig.save(whitelistFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save whitelist.yml: " + e.getMessage());
        }
    }
    
    @Override
    public boolean addPlayer(String playerName) {
        String lowerName = playerName.toLowerCase();
        if (whitelistedPlayers.contains(lowerName)) {
            return false;
        }
        whitelistedPlayers.add(lowerName);
        saveWhitelist();
        return true;
    }
    
    @Override
    public boolean removePlayer(String playerName) {
        String lowerName = playerName.toLowerCase();
        if (!whitelistedPlayers.contains(lowerName)) {
            return false;
        }
        whitelistedPlayers.remove(lowerName);
        saveWhitelist();
        return true;
    }
    
    @Override
    public boolean isWhitelisted(String playerName) {
        return whitelistedPlayers.contains(playerName.toLowerCase());
    }
    
    @Override
    public Set<String> getWhitelistedPlayers() {
        return new HashSet<>(whitelistedPlayers);
    }
    
    public int getPlayerCount() {
        return whitelistedPlayers.size();
    }
    
    @Override
    public boolean isEnabled() {
        return enabled;
    }
    
    @Override
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        // Save to whitelist config
        FileConfiguration config = plugin.getConfigManager().getWhitelistConfig();
        config.set("enabled", enabled);
        plugin.getConfigManager().saveConfig("conf/whitelist.yml");
    }
    
    @Override
    public void reload() {
        loadWhitelist();
    }
}
