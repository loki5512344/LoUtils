package xyz.lokili.loutils.listeners;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.player.PlayerAdvancementDoneEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.lokili.loutils.LoUtils;

public class VanishListener implements Listener {
    
    private final LoUtils plugin;
    
    public VanishListener(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Handle vanish on join
        plugin.getVanishManager().handleJoin(player);
        
        // Silent join for vanished players
        if (plugin.getVanishManager().isVanished(player)) {
            if (plugin.getConfigManager().getVanishConfig().getBoolean("silent_join_quit", true)) {
                event.joinMessage(null);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Silent quit for vanished players
        if (plugin.getVanishManager().isVanished(player)) {
            if (plugin.getConfigManager().getVanishConfig().getBoolean("silent_join_quit", true)) {
                event.quitMessage(null);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onAdvancement(PlayerAdvancementDoneEvent event) {
        Player player = event.getPlayer();
        
        // Block advancement messages for vanished players
        if (plugin.getVanishManager().isVanished(player)) {
            if (plugin.getConfigManager().getVanishConfig().getBoolean("block_advancements", true)) {
                event.message(null);
            }
        }
    }
    
    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        if (event.getTarget() instanceof Player player) {
            if (plugin.getVanishManager().isVanished(player)) {
                event.setCancelled(true);
            }
        }
    }
    
    @EventHandler
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        // Prevent vanished players from taking damage from mobs
        if (event.getEntity() instanceof Player player) {
            if (plugin.getVanishManager().isVanished(player)) {
                if (!(event.getDamager() instanceof Player)) {
                    event.setCancelled(true);
                }
            }
        }
    }
    
    @EventHandler
    public void onItemPickup(EntityPickupItemEvent event) {
        // Prevent vanished players from picking up items (optional)
        if (event.getEntity() instanceof Player player) {
            if (plugin.getVanishManager().isVanished(player)) {
                if (plugin.getConfigManager().getVanishConfig().getBoolean("block_pickup", false)) {
                    event.setCancelled(true);
                }
            }
        }
    }
}
