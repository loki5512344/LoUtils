package xyz.lokili.loutils.placeholders;

import dev.lolib.performance.TPSMonitor;
import dev.lolib.utils.NumberFormatter;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;

public class LoUtilsExpansion extends PlaceholderExpansion {
    
    private final LoUtils plugin;
    private final TPSMonitor tpsMonitor; // Может быть null на Folia
    
    public LoUtilsExpansion(LoUtils plugin) {
        this.plugin = plugin;
        // TPSMonitor может быть null на Folia
        this.tpsMonitor = plugin.getDependencies().getTPSMonitor();
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
        return plugin.getPluginMeta().getVersion();
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
            // Используем NumberFormatter из LoLib
            return NumberFormatter.formatDecimal(Math.min(20.0, tps), "#.#");
        }
        
        return null;
    }
    
    private String getColoredTPS() {
        if (tpsMonitor == null) {
            return "&#AAAAAA20.0"; // Серый цвет если TPSMonitor недоступен
        }
        
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
        
        // Используем NumberFormatter из LoLib
        String formatted = NumberFormatter.formatDecimal(Math.min(20.0, tps), "#.#");
        return color + formatted;
    }
    
    private double getGlobalTPS() {
        return tpsMonitor != null ? tpsMonitor.getCurrentTPS() : 20.0;
    }
}
