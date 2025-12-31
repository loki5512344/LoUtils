package xyz.lokili.loutils.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import xyz.lokili.loutils.LoUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishManager {
    
    private final LoUtils plugin;
    private final Set<UUID> vanishedPlayers;
    private File dataFile;
    private FileConfiguration dataConfig;
    
    public VanishManager(LoUtils plugin) {
        this.plugin = plugin;
        this.vanishedPlayers = new HashSet<>();
        loadData();
    }
    
    private void loadData() {
        dataFile = new File(plugin.getDataFolder(), "data/vanish.yml");
        if (!dataFile.exists()) {
            dataFile.getParentFile().mkdirs();
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create vanish.yml: " + e.getMessage());
            }
        }
        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        
        // Load saved vanished players
        for (String uuidStr : dataConfig.getStringList("vanished")) {
            try {
                vanishedPlayers.add(UUID.fromString(uuidStr));
            } catch (IllegalArgumentException ignored) {}
        }
    }
    
    public void saveData() {
        dataConfig.set("vanished", vanishedPlayers.stream().map(UUID::toString).toList());
        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save vanish.yml: " + e.getMessage());
        }
    }
    
    public boolean isVanished(Player player) {
        return vanishedPlayers.contains(player.getUniqueId());
    }
    
    public boolean isVanished(UUID uuid) {
        return vanishedPlayers.contains(uuid);
    }
    
    public void setVanished(Player player, boolean vanished) {
        if (vanished) {
            vanishedPlayers.add(player.getUniqueId());
            hidePlayerFromAll(player);
        } else {
            vanishedPlayers.remove(player.getUniqueId());
            showPlayerToAll(player);
        }
        saveData();
    }
    
    public void toggleVanish(Player player) {
        setVanished(player, !isVanished(player));
    }
    
    /**
     * Скрывает игрока от всех, кто не может видеть ванишнутых
     */
    private void hidePlayerFromAll(Player vanishedPlayer) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            if (viewer.equals(vanishedPlayer)) continue;
            
            if (!viewer.hasPermission("loutils.vanish.see")) {
                viewer.hidePlayer(plugin, vanishedPlayer);
            }
        }
    }
    
    /**
     * Показывает игрока всем
     */
    private void showPlayerToAll(Player player) {
        for (Player viewer : Bukkit.getOnlinePlayers()) {
            viewer.showPlayer(plugin, player);
        }
    }
    
    /**
     * Обновляет видимость всех ванишнутых для конкретного игрока
     */
    public void updateVanishedPlayersVisibility(Player viewer) {
        boolean canSee = viewer.hasPermission("loutils.vanish.see");
        
        for (UUID uuid : vanishedPlayers) {
            Player vanished = Bukkit.getPlayer(uuid);
            if (vanished != null && vanished.isOnline() && !vanished.equals(viewer)) {
                if (canSee) {
                    viewer.showPlayer(plugin, vanished);
                } else {
                    viewer.hidePlayer(plugin, vanished);
                }
            }
        }
    }
    
    /**
     * Обработка входа игрока
     */
    public void handleJoin(Player player) {
        // Если игрок был в ванише - скрываем его от других
        if (isVanished(player)) {
            hidePlayerFromAll(player);
        }
        
        // Обновляем видимость ванишнутых для этого игрока
        updateVanishedPlayersVisibility(player);
    }
    
    public int getOnlineCountWithoutVanished() {
        int count = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (!isVanished(player)) {
                count++;
            }
        }
        return count;
    }
    
    public Set<UUID> getVanishedPlayers() {
        return new HashSet<>(vanishedPlayers);
    }
    
    public FileConfiguration getConfig() {
        return plugin.getConfigManager().getVanishConfig();
    }
}