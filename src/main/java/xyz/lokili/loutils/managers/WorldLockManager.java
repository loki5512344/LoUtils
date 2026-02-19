package xyz.lokili.loutils.managers;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.IWorldLockManager;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class WorldLockManager implements IWorldLockManager {
    
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
        saveConfig();
    }
    
    @Override
    public void saveConfig() {
        FileConfiguration config = plugin.getConfigManager().getConfig("conf/worldlock.yml");
        config.set("locked-worlds", List.copyOf(lockedWorlds));
        plugin.getConfigManager().saveConfig("conf/worldlock.yml");
    }
    
    @Override
    public boolean isLocked(String worldName) {
        return lockedWorlds.contains(worldName);
    }
    
    @Override
    public boolean addWorld(String worldName) {
        if (lockedWorlds.contains(worldName)) {
            return false;
        }
        lockedWorlds.add(worldName);
        saveLockedWorlds();
        return true;
    }
    
    @Override
    public boolean removeWorld(String worldName) {
        if (!lockedWorlds.contains(worldName)) {
            return false;
        }
        lockedWorlds.remove(worldName);
        saveLockedWorlds();
        return true;
    }
    
    @Override
    public Set<String> getLockedWorlds() {
        return new HashSet<>(lockedWorlds);
    }
    
    @Override
    public void reload() {
        loadLockedWorlds();
    }
}
