package xyz.lokili.loutils.managers;

import dev.lolib.performance.TPSMonitor;
import dev.lolib.scheduler.Scheduler;
import dev.lolib.scheduler.ScheduledTask;
import dev.lolib.utils.Colors;
import dev.lolib.utils.NumberFormatter;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.ITPSBarManager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TPSBarManager implements ITPSBarManager {
    
    private final LoUtils plugin;
    private final TPSMonitor tpsMonitor; // Может быть null на Folia
    private final Map<UUID, BossBar> playerBars;
    private final Map<UUID, ScheduledTask> playerTasks;
    
    public TPSBarManager(LoUtils plugin, TPSMonitor tpsMonitor) {
        this.plugin = plugin;
        this.tpsMonitor = tpsMonitor;
        this.playerBars = new HashMap<>();
        this.playerTasks = new HashMap<>();
    }
    
    public void enableTPSBar(Player player) {
        if (tpsMonitor == null) {
            player.sendMessage(Colors.parse("&#FF5555TPS Bar недоступен на Folia"));
            return;
        }
        
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
        
        // Запускаем обновление для этого игрока используя Scheduler из LoLib
        Scheduler scheduler = Scheduler.get(plugin);
        ScheduledTask task = scheduler.runTimerAtEntity(player, () -> {
            updateTPSBar(player);
        }, 20L, 20L);
        
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
        toggleBar(player);
    }
    
    private void updateTPSBar(Player player) {
        if (!player.isOnline()) {
            disableTPSBar(player);
            return;
        }
        
        BossBar bar = playerBars.get(player.getUniqueId());
        if (bar == null) return;
        
        // Получаем TPS и MSPT через TPSMonitor из LoLib
        double currentTPS = tpsMonitor.getCurrentTPS();
        double mspt = tpsMonitor.getTickTime();
        
        // Форматируем текст
        String format = plugin.getConfigManager().getConfig("conf/tpsbar.yml")
                .getString("format", "&7TPS: {tps} &8| &7MSPT: {mspt}");
        
        String text = format
                .replace("{tps}", formatTPS(currentTPS))
                .replace("{region_tps}", formatTPS(currentTPS))
                .replace("{global_tps}", formatTPS(currentTPS))
                .replace("{mspt}", formatMSPT(mspt))
                .replace("{world}", player.getWorld().getName())
                .replace("{x}", String.valueOf(player.getLocation().getBlockX()))
                .replace("{z}", String.valueOf(player.getLocation().getBlockZ()));
        
        bar.name(Colors.parse(text));
        
        // Обновляем цвет и прогресс
        float progress = (float) Math.min(1.0, currentTPS / 20.0);
        bar.progress(progress);
        bar.color(getColorForTPS(currentTPS));
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
        // Используем NumberFormatter из LoLib
        String formatted = NumberFormatter.formatDecimal(Math.min(20.0, tps), "#.#");
        return color + formatted;
    }
    
    private String formatMSPT(double mspt) {
        String color;
        if (mspt <= 40.0) {
            color = "&#55FF55";
        } else if (mspt <= 50.0) {
            color = "&#FFFF55";
        } else if (mspt <= 60.0) {
            color = "&#FFAA00";
        } else {
            color = "&#FF5555";
        }
        // Используем NumberFormatter из LoLib
        String formatted = NumberFormatter.formatDecimal(mspt, "#.#");
        return color + formatted + "ms";
    }
    
    private BossBar.Color getColorForTPS(double tps) {
        if (tps >= 19.0) return BossBar.Color.GREEN;
        if (tps >= 17.0) return BossBar.Color.YELLOW;
        if (tps >= 14.0) return BossBar.Color.YELLOW;
        return BossBar.Color.RED;
    }
    
    @Override
    public void toggleBar(Player player) {
        if (hasBar(player)) {
            hideBar(player);
        } else {
            showBar(player);
        }
    }
    
    @Override
    public void showBar(Player player) {
        enableTPSBar(player);
    }
    
    @Override
    public void hideBar(Player player) {
        disableTPSBar(player);
    }
    
    @Override
    public boolean hasBar(Player player) {
        return hasTPSBar(player);
    }
    
    @Override
    public void handleQuit(Player player) {
        disableTPSBar(player);
    }
    
    @Override
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
}
