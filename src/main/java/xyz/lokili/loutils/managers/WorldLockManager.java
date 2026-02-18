package xyz.lokili.loutils.managers;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.lokili.loutils.LoUtils;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WorldLockManager {
    
    private final LoUtils plugin;
    private final Set<String> lockedWorlds;
    
    public WorldLockManager(LoUtils plugin) {
        this.plugin = plugin;
        this.lockedWorlds = ConcurrentHashMap.newKeySet();
        loadLockedWorlds();
    }
    
    private void loadLockedWorlds() {
        FileConfiguration config = plugin.getConfigManager().getConfig("conf/worldlock.yml");
        List<String> worlds = config.getStringList("locked-worlds");
        lockedWorlds.clear();
        lockedWorlds.addAll(worlds);
        plugin.getLogger().info("Loaded " + lockedWorlds.size() + " locked worlds");
    }
    
    public void saveLockedWorlds() {
        FileConfiguration config = plugin.getConfigManager().getConfig("conf/worldlock.yml");
        config.set("locked-worlds", List.copyOf(lockedWorlds));
        plugin.getConfigManager().saveConfig("conf/worldlock.yml");
    }
    
    public boolean isLocked(String worldName) {
        return lockedWorlds.contains(worldName);
    }
    
    public boolean addWorld(String worldName) {
        if (lockedWorlds.contains(worldName)) {
            return false;
        }
        lockedWorlds.add(worldName);
        saveLockedWorlds();
        return true;
    }
    
    public boolean removeWorld(String worldName) {
        if (!lockedWorlds.contains(worldName)) {
            return false;
        }
        lockedWorlds.remove(worldName);
        saveLockedWorlds();
        return true;
    }
    
    public Set<String> getLockedWorlds() {
        return new HashSet<>(lockedWorlds);
    }
    
    public void reload() {
        loadLockedWorlds();
    }
}
