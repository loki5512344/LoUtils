package xyz.lokili.loutils.managers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import xyz.lokili.loutils.LoUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class StatsManager {
    
    private final LoUtils plugin;
    private final Map<UUID, PlayerStats> statsCache;
    private File dataFile;
    private FileConfiguration dataConfig;
    private ScheduledTask autoSaveTask;
    private final Map<UUID, Long> sessionStart;
    
    public StatsManager(LoUtils plugin) {
        this.plugin = plugin;
        this.statsCache = new HashMap<>();
        this.sessionStart = new HashMap<>();
        loadData();
        startAutoSave();
    }
    
    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data/stats.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create stats.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        // Load all stats
        if (dataConfig.contains("players")) {
            for (String uuidStr : dataConfig.getConfigurationSection("players").getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidStr);
                    String path = "players." + uuidStr;
                    PlayerStats stats = new PlayerStats(
                            dataConfig.getLong(path + ".playtime", 0),
                            dataConfig.getInt(path + ".kills", 0),
                            dataConfig.getInt(path + ".deaths", 0)
                    );
                    statsCache.put(uuid, stats);
                } catch (IllegalArgumentException ignored) {}
            }
        }
    }
    
    public void saveData() {
        // Update playtime for online players
        for (Player player : Bukkit.getOnlinePlayers()) {
            updatePlaytime(player);
        }
        
        for (Map.Entry<UUID, PlayerStats> entry : statsCache.entrySet()) {
            String path = "players." + entry.getKey().toString();
            PlayerStats stats = entry.getValue();
            dataConfig.set(path + ".playtime", stats.playtime);
            dataConfig.set(path + ".kills", stats.kills);
            dataConfig.set(path + ".deaths", stats.deaths);
        }
        
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save stats.yml: " + e.getMessage());
        }
    }
    
    private void startAutoSave() {
        int interval = plugin.getConfigManager().getStatsConfig().getInt("autosave_interval", 5);
        autoSaveTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, (task) -> {
            saveData();
        }, interval, interval, TimeUnit.MINUTES);
    }
    
    public void shutdown() {
        if (autoSaveTask != null) {
            autoSaveTask.cancel();
        }
        saveData();
    }
    
    public PlayerStats getStats(UUID uuid) {
        return statsCache.computeIfAbsent(uuid, k -> new PlayerStats(0, 0, 0));
    }
    
    public PlayerStats getStats(Player player) {
        return getStats(player.getUniqueId());
    }
    
    public void handleJoin(Player player) {
        sessionStart.put(player.getUniqueId(), System.currentTimeMillis());
    }
    
    public void handleQuit(Player player) {
        updatePlaytime(player);
        sessionStart.remove(player.getUniqueId());
    }
    
    private void updatePlaytime(Player player) {
        Long start = sessionStart.get(player.getUniqueId());
        if (start != null) {
            long sessionTime = System.currentTimeMillis() - start;
            PlayerStats stats = getStats(player);
            stats.playtime += sessionTime;
            sessionStart.put(player.getUniqueId(), System.currentTimeMillis());
        }
    }
    
    public void addKill(Player player) {
        getStats(player).kills++;
    }
    
    public void addDeath(Player player) {
        getStats(player).deaths++;
    }
    
    public String formatPlaytime(long millis) {
        long totalSeconds = millis / 1000;
        long days = totalSeconds / 86400;
        long hours = (totalSeconds % 86400) / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        
        String format = plugin.getConfigManager().getStatsConfig().getString("time_format", "{days}д {hours}ч {minutes}м");
        return format
                .replace("{days}", String.valueOf(days))
                .replace("{hours}", String.valueOf(hours))
                .replace("{minutes}", String.valueOf(minutes));
    }
    
    public static class PlayerStats {
        public long playtime;
        public int kills;
        public int deaths;
        
        public PlayerStats(long playtime, int kills, int deaths) {
            this.playtime = playtime;
            this.kills = kills;
            this.deaths = deaths;
        }
        
        public double getKDR() {
            if (deaths == 0) return kills;
            return Math.round((double) kills / deaths * 100.0) / 100.0;
        }
    }
}
