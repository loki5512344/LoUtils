package xyz.lokili.loutils.managers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.ColorUtil;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

public class TPSBarManager {
    
    private final LoUtils plugin;
    private final Map<UUID, BossBar> playerBars;
    private final Map<UUID, ScheduledTask> playerTasks;
    
    public TPSBarManager(LoUtils plugin) {
        this.plugin = plugin;
        this.playerBars = new HashMap<>();
        this.playerTasks = new HashMap<>();
    }
    
    public void enableTPSBar(Player player) {
        if (playerBars.containsKey(player.getUniqueId())) {
            return;
        }
        
        BossBar bar = BossBar.bossBar(
                Component.text("Loading TPS..."),
                1.0f,
                BossBar.Color.GREEN,
                BossBar.Overlay.PROGRESS
        );
        
        player.showBossBar(bar);
        playerBars.put(player.getUniqueId(), bar);
        
        // Запускаем обновление для этого игрока
        ScheduledTask task = player.getScheduler().runAtFixedRate(plugin, (t) -> {
            updateTPSBar(player);
        }, () -> {}, 20L, 20L);
        
        playerTasks.put(player.getUniqueId(), task);
    }
    
    public void disableTPSBar(Player player) {
        BossBar bar = playerBars.remove(player.getUniqueId());
        if (bar != null) {
            player.hideBossBar(bar);
        }
        
        ScheduledTask task = playerTasks.remove(player.getUniqueId());
        if (task != null) {
            task.cancel();
        }
    }
    
    public boolean hasTPSBar(Player player) {
        return playerBars.containsKey(player.getUniqueId());
    }
    
    public void toggleTPSBar(Player player) {
        if (hasTPSBar(player)) {
            disableTPSBar(player);
        } else {
            enableTPSBar(player);
        }
    }
    
    private void updateTPSBar(Player player) {
        if (!player.isOnline()) {
            disableTPSBar(player);
            return;
        }
        
        BossBar bar = playerBars.get(player.getUniqueId());
        if (bar == null) return;
        
        // Получаем TPS региона игрока
        double regionTPS = getRegionTPS(player);
        double globalTPS = getGlobalTPS();
        long mspt = getMSPT(player);
        
        // Форматируем текст
        String format = plugin.getConfigManager().getConfig("conf/tpsbar.yml")
                .getString("format", "&7Region: {region_tps} &8| &7Global: {global_tps} &8| &7MSPT: {mspt}");
        
        String text = format
                .replace("{region_tps}", formatTPS(regionTPS))
                .replace("{global_tps}", formatTPS(globalTPS))
                .replace("{mspt}", formatMSPT(mspt))
                .replace("{world}", player.getWorld().getName())
                .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                .replace("{z}", String.valueOf(player.getLocation().getBlockZ()));
        
        bar.name(ColorUtil.colorize(text));
        
        // Обновляем цвет и прогресс
        float progress = (float) Math.min(1.0, regionTPS / 20.0);
        bar.progress(progress);
        bar.color(getColorForTPS(regionTPS));
    }
    
    private double getRegionTPS(Player player) {
        try {
            // Folia API: получаем TPS региона через TickRegions
            // io.papermc.paper.threadedregions.TickRegionScheduler
            Object regionScheduler = Bukkit.getServer().getClass()
                    .getMethod("getRegionScheduler")
                    .invoke(Bukkit.getServer());
            
            // Пробуем получить TPS через reflection
            // В Folia нет прямого API для TPS региона, используем альтернативу
            
            // Метод 1: Через getCurrentRegion
            Class<?> tickRegionsClass = Class.forName("io.papermc.paper.threadedregions.TickRegions");
            Method getCurrentRegion = tickRegionsClass.getMethod("getCurrentRegion");
            Object region = getCurrentRegion.invoke(null);
            
            if (region != null) {
                // Получаем TickData
                Method getTickData = region.getClass().getMethod("getTickData");
                Object tickData = getTickData.invoke(region);
                
                if (tickData != null) {
                    Method getCurrentTickMethod = tickData.getClass().getMethod("getCurrentTick");
                    // Вычисляем TPS на основе данных
                }
            }
        } catch (Exception e) {
            // Fallback: используем глобальный TPS
        }
        
        // Fallback: возвращаем глобальный TPS
        return getGlobalTPS();
    }
    
    private double getGlobalTPS() {
        try {
            // Paper/Folia API
            double[] tps = Bukkit.getTPS();
            return tps[0]; // 1 minute average
        } catch (Exception e) {
            return 20.0;
        }
    }
    
    private long getMSPT(Player player) {
        try {
            // Paper API
            return (long) Bukkit.getAverageTickTime();
        } catch (Exception e) {
            return 50;
        }
    }
    
    private String formatTPS(double tps) {
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
    
    private String formatMSPT(long mspt) {
        String color;
        if (mspt <= 40) {
            color = "&#55FF55";
        } else if (mspt <= 50) {
            color = "&#FFFF55";
        } else if (mspt <= 60) {
            color = "&#FFAA00";
        } else {
            color = "&#FF5555";
        }
        return color + mspt + "ms";
    }
    
    private BossBar.Color getColorForTPS(double tps) {
        if (tps >= 19.0) return BossBar.Color.GREEN;
        if (tps >= 17.0) return BossBar.Color.YELLOW;
        if (tps >= 14.0) return BossBar.Color.YELLOW;
        return BossBar.Color.RED;
    }
    
    public void shutdown() {
        for (UUID uuid : playerBars.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                disableTPSBar(player);
            }
        }
        playerBars.clear();
        playerTasks.clear();
    }
    
    public void handleQuit(Player player) {
        disableTPSBar(player);
    }
}
