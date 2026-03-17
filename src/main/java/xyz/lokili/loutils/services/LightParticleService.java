package xyz.lokili.loutils.services;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import xyz.lokili.loutils.utils.SchedulerUtil;

import java.util.HashSet;
import java.util.Set;

/**
 * LightParticleService - Сервис отображения частиц для блоков света
 * 
 * Кэширует позиции блоков света и показывает частицы игрокам в радиусе 16 блоков
 * Частицы показываются только когда игрок держит блок света в руке
 */
public class LightParticleService {
    
    private final Set<Location> lightBlocks = new HashSet<>();
    private final Plugin plugin;
    
    public LightParticleService(Plugin plugin) {
        this.plugin = plugin;
        startParticleTask();
    }
    
    /**
     * Добавить блок света в кэш
     */
    public void add(Location loc) {
        lightBlocks.add(loc);
    }
    
    /**
     * Удалить блок света из кэша
     */
    public void remove(Location loc) {
        lightBlocks.remove(loc);
    }
    
    /**
     * Запуск задачи показа частиц
     */
    private void startParticleTask() {
        SchedulerUtil.runGlobalTimer(plugin, (task) -> {
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                // Креатив и спектатор видят блоки света нативно
                if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) {
                    continue;
                }
                
                // Проверяем: держит ли игрок блок света в руке
                boolean hasLightBlock = player.getInventory().getItemInMainHand().getType() == Material.LIGHT
                    || player.getInventory().getItemInOffHand().getType() == Material.LIGHT;
                
                if (!hasLightBlock) {
                    continue;
                }
                
                // Запускаем региональную задачу для этого игрока
                SchedulerUtil.runForEntity(plugin, player, () -> {
                    showParticlesAroundPlayer(player);
                });
            }
        }, 10L, 10L); // Каждые 0.5 секунды
    }
    
    /**
     * Показать частицы вокруг игрока
     */
    private void showParticlesAroundPlayer(Player player) {
        try {
            Location playerLoc = player.getLocation();
            
            // Сканируем блоки вокруг игрока и добавляем в кэш
            int radius = 16;
            int minX = playerLoc.getBlockX() - radius;
            int maxX = playerLoc.getBlockX() + radius;
            int minY = Math.max(player.getWorld().getMinHeight(), playerLoc.getBlockY() - radius);
            int maxY = Math.min(player.getWorld().getMaxHeight(), playerLoc.getBlockY() + radius);
            int minZ = playerLoc.getBlockZ() - radius;
            int maxZ = playerLoc.getBlockZ() + radius;
            
            for (int x = minX; x <= maxX; x++) {
                for (int y = minY; y <= maxY; y++) {
                    for (int z = minZ; z <= maxZ; z++) {
                        org.bukkit.block.Block block = player.getWorld().getBlockAt(x, y, z);
                        if (block.getType() == Material.LIGHT) {
                            Location loc = block.getLocation();
                            lightBlocks.add(loc);
                            showParticles(player, loc);
                        }
                    }
                }
            }
        } catch (Exception ignored) {
        }
    }
    
    /**
     * Показать частицы вокруг блока света (8 углов куба)
     */
    private void showParticles(Player player, Location loc) {
        double x = loc.getX() + 0.5;
        double y = loc.getY() + 0.5;
        double z = loc.getZ() + 0.5;
        double o = 0.4;
        
        player.spawnParticle(Particle.GLOW, x - o, y - o, z - o, 1, 0, 0, 0, 0);
        player.spawnParticle(Particle.GLOW, x + o, y - o, z - o, 1, 0, 0, 0, 0);
        player.spawnParticle(Particle.GLOW, x - o, y + o, z - o, 1, 0, 0, 0, 0);
        player.spawnParticle(Particle.GLOW, x + o, y + o, z - o, 1, 0, 0, 0, 0);
        player.spawnParticle(Particle.GLOW, x - o, y - o, z + o, 1, 0, 0, 0, 0);
        player.spawnParticle(Particle.GLOW, x + o, y - o, z + o, 1, 0, 0, 0, 0);
        player.spawnParticle(Particle.GLOW, x - o, y + o, z + o, 1, 0, 0, 0, 0);
        player.spawnParticle(Particle.GLOW, x + o, y + o, z + o, 1, 0, 0, 0, 0);
    }
}
