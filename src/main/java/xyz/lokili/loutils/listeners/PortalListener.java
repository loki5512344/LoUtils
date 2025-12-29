package xyz.lokili.loutils.listeners;

import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
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
        
        if (plugin.getDimensionLockManager().isLocked(targetEnv)) {
            event.setCancelled(true);
            
            Player player = event.getPlayer();
            String dimension = environmentToDimension(targetEnv);
            
            // Отправляем ActionBar
            player.sendActionBar(plugin.getDimensionLockManager().getLockedActionBar(dimension));
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerTeleport(PlayerTeleportEvent event) {
        // Блокируем телепортацию командами в закрытые измерения
        if (event.getCause() != PlayerTeleportEvent.TeleportCause.COMMAND &&
            event.getCause() != PlayerTeleportEvent.TeleportCause.PLUGIN) {
            return;
        }
        
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
            player.sendActionBar(plugin.getDimensionLockManager().getLockedActionBar(dimension));
        }
    }
    
    private String environmentToDimension(World.Environment environment) {
        return switch (environment) {
            case NETHER -> "nether";
            case THE_END -> "end";
            default -> null;
        };
    }
}
