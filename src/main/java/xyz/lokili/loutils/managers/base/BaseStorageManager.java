package xyz.lokili.loutils.managers.base;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Base class for managers that store Set<String> data
 * Eliminates code duplication between WhitelistManager and WorldLockManager
 * Single Responsibility: Set-based storage operations
 */
public abstract class BaseStorageManager {
    
    protected final Plugin plugin;
    protected final Set<String> storage;
    protected final String configPath;
    protected final String storageKey;
    
    /**
     * @param plugin Plugin instance
     * @param configPath Path to config file (e.g., "conf/whitelist.yml")
     * @param storageKey Key in config for the list (e.g., "players", "locked-worlds")
     * @param concurrent Whether to use ConcurrentHashSet for thread-safety
     */
    protected BaseStorageManager(Plugin plugin, String configPath, String storageKey, boolean concurrent) {
        this.plugin = plugin;
        this.configPath = configPath;
        this.storageKey = storageKey;
        this.storage = concurrent ? ConcurrentHashMap.newKeySet() : new HashSet<>();
    }
    
    /**
     * Load data from config
     */
    protected void load(FileConfiguration config) {
        List<String> items = config.getStringList(storageKey);
        storage.clear();
        storage.addAll(processLoadedItems(items));
        plugin.getLogger().info("Loaded " + storage.size() + " items from " + configPath);
    }
    
    /**
     * Save data to config
     */
    protected void save(FileConfiguration config) {
        config.set(storageKey, List.copyOf(storage));
        saveConfig(config);
    }
    
    /**
     * Add item to storage
     */
    public boolean add(String item) {
        String processed = processItem(item);
        if (storage.contains(processed)) {
            return false;
        }
        storage.add(processed);
        saveToConfig();
        return true;
    }
    
    /**
     * Remove item from storage
     */
    public boolean remove(String item) {
        String processed = processItem(item);
        if (!storage.contains(processed)) {
            return false;
        }
        storage.remove(processed);
        saveToConfig();
        return true;
    }
    
    /**
     * Check if item exists in storage
     */
    public boolean contains(String item) {
        return storage.contains(processItem(item));
    }
    
    /**
     * Get all items
     */
    public Set<String> getAll() {
        return new HashSet<>(storage);
    }
    
    /**
     * Get count of items
     */
    public int size() {
        return storage.size();
    }
    
    /**
     * Clear all items
     */
    public void clear() {
        storage.clear();
        saveToConfig();
    }
    
    // === Abstract methods for customization ===
    
    /**
     * Get config instance
     */
    protected abstract FileConfiguration getConfig();
    
    /**
     * Save config to disk
     */
    protected abstract void saveConfig(FileConfiguration config);
    
    /**
     * Process item before storage (e.g., toLowerCase for player names)
     */
    protected abstract String processItem(String item);
    
    /**
     * Process loaded items (e.g., toLowerCase for player names)
     */
    protected List<String> processLoadedItems(List<String> items) {
        return items.stream().map(this::processItem).toList();
    }
    
    /**
     * Reload from config
     */
    public void reload() {
        load(getConfig());
    }
    
    private void saveToConfig() {
        save(getConfig());
    }
}
