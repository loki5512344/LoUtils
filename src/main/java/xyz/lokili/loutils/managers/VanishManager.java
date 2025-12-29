package xyz.lokili.loutils.managers;

import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
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
            hidePlayer(player);
            applyEffects(player);
        } else {
            vanishedPlayers.remove(player.getUniqueId());
            showPlayer(player);
            removeEffects(player);
        }
        
        if (plugin.getConfigManager().getVanishConfig().getBoolean("save_state", true)) {
            saveData();
        }
    }
    
    public void toggleVanish(Player player) {
        setVanished(player, !isVanished(player));
    }
    
    private void hidePlayer(Player player) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (!online.hasPermission("loutils.vanish.see")) {
                online.hidePlayer(plugin, player);
            }
        }
    }
    
    private void showPlayer(Player player) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            online.showPlayer(plugin, player);
        }
    }
    
    private void applyEffects(Player player) {
        if (plugin.getConfigManager().getVanishConfig().getBoolean("effects.night_vision", true)) {
            player.addPotionEffect(new PotionEffect(PotionEffectType.NIGHT_VISION, Integer.MAX_VALUE, 0, false, false));
        }
    }
    
    private void removeEffects(Player player) {
        player.removePotionEffect(PotionEffectType.NIGHT_VISION);
    }
    
    public void handleJoin(Player player) {
        // Restore vanish state
        if (isVanished(player)) {
            hidePlayer(player);
            applyEffects(player);
        }
        
        // Hide vanished players from this player
        if (!player.hasPermission("loutils.vanish.see")) {
            for (UUID uuid : vanishedPlayers) {
                Player vanished = Bukkit.getPlayer(uuid);
                if (vanished != null && vanished.isOnline()) {
                    player.hidePlayer(plugin, vanished);
                }
            }
        }
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
}
