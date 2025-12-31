package xyz.lokili.loutils.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;

public class LoUtilsExpansion extends PlaceholderExpansion {
    
    private final LoUtils plugin;
    
    public LoUtilsExpansion(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public @NotNull String getIdentifier() {
        return "loutils";
    }
    
    @Override
    public @NotNull String getAuthor() {
        return "loki";
    }
    
    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }
    
    @Override
    public boolean persist() {
        return true;
    }
    
    @Override
    public @Nullable String onRequest(OfflinePlayer offlinePlayer, @NotNull String params) {
        // Online count without vanished
        if (params.equalsIgnoreCase("online")) {
            return String.valueOf(plugin.getVanishManager().getOnlineCountWithoutVanished());
        }
        
        if (params.equalsIgnoreCase("online_total")) {
            return String.valueOf(plugin.getServer().getOnlinePlayers().size());
        }
        
        if (params.equalsIgnoreCase("vanished_count")) {
            return String.valueOf(plugin.getVanishManager().getVanishedPlayers().size());
        }
        
        // Player-specific placeholders
        if (offlinePlayer == null) return null;
        
        // Vanish status
        if (params.equalsIgnoreCase("vanished")) {
            return plugin.getVanishManager().isVanished(offlinePlayer.getUniqueId()) ? "true" : "false";
        }
        
        return null;
    }
}
