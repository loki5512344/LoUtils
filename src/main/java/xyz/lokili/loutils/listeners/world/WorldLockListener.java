package xyz.lokili.loutils.listeners.world;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerPortalEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.listeners.base.BaseListener;
import xyz.lokili.loutils.utils.ColorUtil;

/**
 * WorldLockListener - Блокировка доступа к мирам
 * Всегда включен (не зависит от модуля)
 */
public class WorldLockListener extends BaseListener {
    
    public WorldLockListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, null, "conf/worldlock.yml");
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onWorldChange(PlayerChangedWorldEvent event) {
        Player player = event.getPlayer();
        World toWorld = player.getWorld();
        
        checkWorldLock(player, toWorld);
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPortal(PlayerPortalEvent event) {
        Player player = event.getPlayer();
        Location to = event.getTo();
        
        if (to == null) return;
        
        World toWorld = to.getWorld();
        if (toWorld == null) return;
        
        if (plugin.getContainer().getWorldLockManager().isLocked(toWorld.getName())) {
            if (player.hasPermission("loutils.worldlock.bypass." + toWorld.getName())) {
                return;
            }
            
            event.setCancelled(true);
            sendLockMessage(player, toWorld.getName());
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onTeleport(PlayerTeleportEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();
        
        if (to == null) return;
        
        World fromWorld = from.getWorld();
        World toWorld = to.getWorld();
        
        if (fromWorld == null || toWorld == null) return;
        if (fromWorld.equals(toWorld)) return;
        
        if (plugin.getContainer().getWorldLockManager().isLocked(toWorld.getName())) {
            if (player.hasPermission("loutils.worldlock.bypass." + toWorld.getName())) {
                return;
            }
            
            event.setCancelled(true);
            sendLockMessage(player, toWorld.getName());
        }
    }
    
    private void checkWorldLock(Player player, World world) {
        if (plugin.getContainer().getWorldLockManager().isLocked(world.getName())) {
            if (player.hasPermission("loutils.worldlock.bypass." + world.getName())) {
                return;
            }
            
            // Телепортируем обратно в главный мир
            World mainWorld = plugin.getServer().getWorlds().get(0);
            player.teleport(mainWorld.getSpawnLocation());
            sendLockMessage(player, world.getName());
        }
    }
    
    private void sendLockMessage(Player player, String worldName) {
        String prefix = configManager.getPrefix();
        String message = moduleConfig().getString("messages.world-locked", "&cЭтот мир заблокирован!");
        player.sendMessage(ColorUtil.colorize(prefix + message));
    }
}
