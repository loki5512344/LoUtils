package xyz.lokili.loutils.placeholders;

import dev.lolib.performance.TPSMonitor;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;

public class LoUtilsExpansion extends PlaceholderExpansion {
    
    private final LoUtils plugin;
    private final TPSMonitor tpsMonitor;
    
    public LoUtilsExpansion(LoUtils plugin) {
        this.plugin = plugin;
        this.tpsMonitor = TPSMonitor.get(plugin);
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
        // Online count
        if (params.equalsIgnoreCase("online")) {
            return String.valueOf(plugin.getServer().getOnlinePlayers().size());
        }
        
        if (params.equalsIgnoreCase("online_total")) {
            return String.valueOf(plugin.getServer().getOnlinePlayers().size());
        }
        
        // TPS with colors
        if (params.equalsIgnoreCase("tps_colored")) {
            return getColoredTPS();
        }
        
        if (params.equalsIgnoreCase("tps")) {
            double tps = getGlobalTPS();
            return String.format("%.1f", Math.min(20.0, tps));
        }
        
        return null;
    }
    
    private String getColoredTPS() {
        double tps = tpsMonitor.getCurrentTPS();
        String color;
        
        if (tps >= 19.0) {
            color = "&#55FF55"; // Зелёный
        } else if (tps >= 17.0) {
            color = "&#FFFF55"; // Жёлтый
        } else if (tps >= 14.0) {
            color = "&#FFAA00"; // Оранжевый
        } else {
            color = "&#FF5555"; // Красный
        }
        
        return color + String.format("%.1f", Math.min(20.0, tps));
    }
    
    private double getGlobalTPS() {
        return tpsMonitor.getCurrentTPS();
    }
}
