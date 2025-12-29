package xyz.lokili.loutils.managers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class DimensionLockManager {
    
    private final LoUtils plugin;
    private final Map<String, Long> lockedDimensions; // dimension -> unlock time millis
    private final Map<String, ArmorStand> holograms;
    private ScheduledTask timerTask;
    
    public DimensionLockManager(LoUtils plugin) {
        this.plugin = plugin;
        this.lockedDimensions = new ConcurrentHashMap<>();
        this.holograms = new HashMap<>();
        startTimer();
    }
    
    private void startTimer() {
        timerTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, (task) -> {
            checkUnlocks();
        }, 1, 1, TimeUnit.SECONDS);
    }
    
    public void shutdown() {
        if (timerTask != null) {
            timerTask.cancel();
        }
        // Удаляем все голограммы
        Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
            for (ArmorStand hologram : holograms.values()) {
                if (hologram != null && !hologram.isDead()) {
                    hologram.remove();
                }
            }
            holograms.clear();
        });
    }
    
    private void checkUnlocks() {
        long now = System.currentTimeMillis();
        
        for (Map.Entry<String, Long> entry : lockedDimensions.entrySet()) {
            if (now >= entry.getValue()) {
                String dimension = entry.getKey();
                lockedDimensions.remove(dimension);
                
                Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
                    removeHologram(dimension);
                    broadcastUnlock(dimension);
                });
            } else {
                // Обновляем голограмму
                String dimension = entry.getKey();
                Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
                    updateHologram(dimension);
                });
            }
        }
    }
    
    public boolean lockDimension(String dimension, int minutes) {
        dimension = normalizeDimension(dimension);
        if (dimension == null) return false;
        
        long unlockTime = System.currentTimeMillis() + (minutes * 60 * 1000L);
        lockedDimensions.put(dimension, unlockTime);
        
        // Создаём голограмму
        if (plugin.getConfig().getBoolean("dimensionlock.hologram.enabled", true)) {
            createHologram(dimension);
        }
        
        return true;
    }
    
    public boolean unlockDimension(String dimension) {
        dimension = normalizeDimension(dimension);
        if (dimension == null) return false;
        
        if (!lockedDimensions.containsKey(dimension)) {
            return false;
        }
        
        lockedDimensions.remove(dimension);
        removeHologram(dimension);
        return true;
    }
    
    public boolean isLocked(String dimension) {
        dimension = normalizeDimension(dimension);
        return dimension != null && lockedDimensions.containsKey(dimension);
    }
    
    public boolean isLocked(World.Environment environment) {
        String dimension = environmentToDimension(environment);
        return dimension != null && lockedDimensions.containsKey(dimension);
    }
    
    public long getTimeRemaining(String dimension) {
        dimension = normalizeDimension(dimension);
        if (dimension == null || !lockedDimensions.containsKey(dimension)) {
            return 0;
        }
        
        long remaining = lockedDimensions.get(dimension) - System.currentTimeMillis();
        return Math.max(0, remaining);
    }
    
    public String getTimeRemainingFormatted(String dimension) {
        long remaining = getTimeRemaining(dimension);
        if (remaining <= 0) return "0:00";
        
        long minutes = remaining / 60000;
        long seconds = (remaining % 60000) / 1000;
        return String.format("%d:%02d", minutes, seconds);
    }
    
    private String normalizeDimension(String dimension) {
        if (dimension == null) return null;
        dimension = dimension.toLowerCase();
        
        return switch (dimension) {
            case "nether", "the_nether" -> "nether";
            case "end", "the_end" -> "end";
            default -> null;
        };
    }
    
    private String environmentToDimension(World.Environment environment) {
        return switch (environment) {
            case NETHER -> "nether";
            case THE_END -> "end";
            default -> null;
        };
    }
    
    private void createHologram(String dimension) {
        World world = getWorldForDimension(dimension);
        if (world == null) return;
        
        Location loc = getHologramLocation(dimension, world);
        if (loc == null) return;
        
        // Удаляем старую голограмму если есть
        removeHologram(dimension);
        
        // Создаём ArmorStand как голограмму
        Bukkit.getRegionScheduler().execute(plugin, loc, () -> {
            ArmorStand hologram = (ArmorStand) world.spawnEntity(loc, EntityType.ARMOR_STAND);
            hologram.setVisible(false);
            hologram.setGravity(false);
            hologram.setMarker(true);
            hologram.setCustomNameVisible(true);
            hologram.setInvulnerable(true);
            hologram.setPersistent(false);
            
            updateHologramText(hologram, dimension);
            holograms.put(dimension, hologram);
        });
    }
    
    private void updateHologram(String dimension) {
        ArmorStand hologram = holograms.get(dimension);
        if (hologram == null || hologram.isDead()) {
            if (lockedDimensions.containsKey(dimension)) {
                createHologram(dimension);
            }
            return;
        }
        
        updateHologramText(hologram, dimension);
    }
    
    private void updateHologramText(ArmorStand hologram, String dimension) {
        String template = plugin.getConfig().getString("dimensionlock.hologram.text", 
                "&c{dimension} откроется через {time}");
        String text = template
                .replace("{dimension}", getDimensionDisplayName(dimension))
                .replace("{time}", getTimeRemainingFormatted(dimension));
        
        hologram.customName(ColorUtil.colorize(text));
    }
    
    private void removeHologram(String dimension) {
        ArmorStand hologram = holograms.remove(dimension);
        if (hologram != null && !hologram.isDead()) {
            hologram.remove();
        }
    }
    
    private World getWorldForDimension(String dimension) {
        for (World world : Bukkit.getWorlds()) {
            if (dimension.equals("nether") && world.getEnvironment() == World.Environment.NETHER) {
                return world;
            }
            if (dimension.equals("end") && world.getEnvironment() == World.Environment.THE_END) {
                return world;
            }
        }
        return null;
    }
    
    private Location getHologramLocation(String dimension, World world) {
        // Спавн точка измерения + offset
        double offset = plugin.getConfig().getDouble("dimensionlock.hologram.offset", 3.0);
        Location spawn = world.getSpawnLocation();
        return spawn.clone().add(0, offset, 0);
    }
    
    private void broadcastUnlock(String dimension) {
        String message = plugin.getConfig().getString("messages.prefix", "") +
                plugin.getConfig().getString("messages.dimension-unlocked", "{dimension} открыт!")
                        .replace("{dimension}", getDimensionDisplayName(dimension));
        
        Bukkit.broadcast(ColorUtil.colorize(message));
    }
    
    public String getDimensionDisplayName(String dimension) {
        return switch (dimension) {
            case "nether" -> "Nether";
            case "end" -> "End";
            default -> dimension;
        };
    }
    
    public Component getLockedActionBar(String dimension) {
        String template = plugin.getConfig().getString("messages.dimension-locked-actionbar",
                "&cДоступ к {dimension} закрыт! Осталось: {time}");
        String text = template
                .replace("{dimension}", getDimensionDisplayName(dimension))
                .replace("{time}", getTimeRemainingFormatted(dimension));
        return ColorUtil.colorize(text);
    }
}
