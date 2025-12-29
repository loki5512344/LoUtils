package xyz.lokili.loutils.placeholders;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.managers.PartyManager;
import xyz.lokili.loutils.managers.StatsManager;

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
        
        Player player = offlinePlayer.getPlayer();
        
        // Vanish status
        if (params.equalsIgnoreCase("vanished")) {
            return plugin.getVanishManager().isVanished(offlinePlayer.getUniqueId()) ? "true" : "false";
        }
        
        // Stats
        StatsManager.PlayerStats stats = plugin.getStatsManager().getStats(offlinePlayer.getUniqueId());
        
        if (params.equalsIgnoreCase("kills")) {
            return String.valueOf(stats.kills);
        }
        
        if (params.equalsIgnoreCase("deaths")) {
            return String.valueOf(stats.deaths);
        }
        
        if (params.equalsIgnoreCase("kdr")) {
            return String.valueOf(stats.getKDR());
        }
        
        if (params.equalsIgnoreCase("playtime")) {
            return plugin.getStatsManager().formatPlaytime(stats.playtime);
        }
        
        if (params.equalsIgnoreCase("playtime_hours")) {
            return String.valueOf(stats.playtime / 3600000);
        }
        
        if (params.equalsIgnoreCase("playtime_minutes")) {
            return String.valueOf(stats.playtime / 60000);
        }
        
        // Party
        if (player != null) {
            if (params.equalsIgnoreCase("party_suffix")) {
                return plugin.getPartyManager().getPartySuffix(player);
            }
            
            if (params.equalsIgnoreCase("party_size")) {
                PartyManager.Party party = plugin.getPartyManager().getParty(player);
                return party != null ? String.valueOf(party.getSize()) : "0";
            }
            
            if (params.equalsIgnoreCase("in_party")) {
                return plugin.getPartyManager().isInParty(player) ? "true" : "false";
            }
            
            // Nick
            if (params.equalsIgnoreCase("nick")) {
                return plugin.getNickManager().getDisplayName(player);
            }
            
            if (params.equalsIgnoreCase("has_nick")) {
                return plugin.getNickManager().hasNick(player) ? "true" : "false";
            }
        }
        
        return null;
    }
}
