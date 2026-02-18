package xyz.lokili.loutils.listeners;

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
import xyz.lokili.loutils.utils.ColorUtil;

public class WorldLockListener implements Listener {
    
    private final LoUtils plugin;
    
    public WorldLockListener(LoUtils plugin) {
        this.plugin = plugin;
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
        
        if (plugin.getWorldLockManager().isLocked(toWorld.getName())) {
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
        
        if (plugin.getWorldLockManager().isLocked(toWorld.getName())) {
            if (player.hasPermission("loutils.worldlock.bypass." + toWorld.getName())) {
                return;
            }
            
            event.setCancelled(true);
            sendLockMessage(player, toWorld.getName());
        }
    }
    
    private void checkWorldLock(Player player, World world) {
        if (plugin.getWorldLockManager().isLocked(world.getName())) {
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
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getConfig("conf/worldlock.yml")
                .getString("messages.world-locked", "&cЭтот мир заблокирован!");
        player.sendMessage(ColorUtil.colorize(prefix + message));
    }
}
