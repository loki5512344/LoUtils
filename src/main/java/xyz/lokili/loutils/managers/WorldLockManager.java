package xyz.lokili.loutils.managers;

import org.bukkit.configuration.file.FileConfiguration;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.IWorldLockManager;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.managers.base.BaseStorageManager;

import java.util.Set;

/**
 * Manages world locks using BaseStorageManager (DRY principle)
 */
public class WorldLockManager extends BaseStorageManager implements IWorldLockManager {
    
    private final LoUtils plugin;
    
    public WorldLockManager(LoUtils plugin) {
        super(plugin, ConfigConstants.WORLDLOCK_CONFIG, "locked-worlds", true);
        this.plugin = plugin;
        reload();
    }
    
    @Override
    protected FileConfiguration getConfig() {
        return plugin.getConfigManager().getConfig(ConfigConstants.WORLDLOCK_CONFIG);
    }
    
    @Override
    protected void saveConfig(FileConfiguration config) {
        plugin.getConfigManager().saveConfig(ConfigConstants.WORLDLOCK_CONFIG);
    }
    
    @Override
    protected String processItem(String item) {
        return item; // World names are case-sensitive
    }
    
    // === IWorldLockManager Implementation ===
    
    @Override
    public void saveConfig() {
        save(getConfig());
    }
    
    public void saveLockedWorlds() {
        saveConfig();
    }
    
    @Override
    public boolean isLocked(String worldName) {
        return contains(worldName);
    }
    
    @Override
    public boolean addWorld(String worldName) {
        return add(worldName);
    }
    
    @Override
    public boolean removeWorld(String worldName) {
        return remove(worldName);
    }
    
    @Override
    public Set<String> getLockedWorlds() {
        return getAll();
    }
}
