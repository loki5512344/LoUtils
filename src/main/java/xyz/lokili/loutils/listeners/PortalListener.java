package xyz.lokili.loutils.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import xyz.lokili.loutils.LoUtils;

public class PortalListener implements Listener {
    
    private final LoUtils plugin;
    
    public PortalListener(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPortal(PlayerPortalEvent event) {
        if (event.getTo() == null || event.getTo().getWorld() == null) return;
        
        World.Environment targetEnv = event.getTo().getWorld().getEnvironment();
        Player player = event.getPlayer();
        
        // Пропускаем игроков с правами
        if (player.hasPermission("loutils.lock.bypass")) {
            return;
        }
        
        if (plugin.getDimensionLockManager().isLocked(targetEnv)) {
            event.setCancelled(true);
            
            String dimension = environmentToDimension(targetEnv);
            
            // Отправляем ActionBar
            player.sendActionBar(plugin.getDimensionLockManager().getLockedActionBar(dimension));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        if (event.getTo() == null || event.getTo().getWorld() == null) return;
        if (event.getFrom() == null || event.getFrom().getWorld() == null) return;

        World.Environment targetEnv = event.getTo().getWorld().getEnvironment();
        World.Environment fromEnv = event.getFrom().getWorld().getEnvironment();
        Player player = event.getPlayer();

        // Телепорт внутри того же измерения не блокируем
        if (fromEnv == targetEnv) {
            return;
        }
        
        // Пропускаем игроков с правами
        if (player.hasPermission("loutils.lock.bypass")) {
            return;
        }
        
        if (plugin.getDimensionLockManager().isLocked(targetEnv)) {
            event.setCancelled(true);
            
            String dimension = environmentToDimension(targetEnv);
            player.sendActionBar(plugin.getDimensionLockManager().getLockedActionBar(dimension));
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerChangedWorld(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World toWorld = player.getWorld();

        // Пропускаем игроков с правами
        if (player.hasPermission("loutils.lock.bypass")) {
            return;
        }

        if (!plugin.getDimensionLockManager().isLocked(toWorld.getEnvironment())) {
            return;
        }

        String dimension = environmentToDimension(toWorld.getEnvironment());
        player.sendActionBar(plugin.getDimensionLockManager().getLockedActionBar(dimension));

        Location returnLocation = getReturnLocation(event.getFrom());
        Bukkit.getRegionScheduler().execute(plugin, player.getLocation(), () -> player.teleportAsync(returnLocation));
    }

    private Location getReturnLocation(World fromWorld) {
        if (fromWorld != null && fromWorld.getEnvironment() == World.Environment.NORMAL) {
            return fromWorld.getSpawnLocation();
        }

        for (World world : Bukkit.getWorlds()) {
            if (world.getEnvironment() == World.Environment.NORMAL) {
                return world.getSpawnLocation();
            }
        }

        // Фолбек: если по какой-то причине нормального мира нет
        return Bukkit.getWorlds().get(0).getSpawnLocation();
    }
    
    private String environmentToDimension(World.Environment environment) {
        return switch (environment) {
            case NETHER -> "nether";
            case THE_END -> "end";
            default -> null;
        };
    }
}
