package xyz.lokili.loutils.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import xyz.lokili.loutils.LoUtils;

public class VanishListener implements Listener {
    
    private final LoUtils plugin;
    
    public VanishListener(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Handle vanish on join
        plugin.getVanishManager().handleJoin(player);
        
        // Silent join for vanished players
        if (plugin.getVanishManager().isVanished(player)) {
            if (plugin.getVanishManager().getConfig().getBoolean("silent_join_quit", true)) {
                event.joinMessage(null);
            }
        }
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Cleanup TPSBar
        plugin.getTPSBarManager().handleQuit(player);
        
        // Silent quit for vanished players
        if (plugin.getVanishManager().isVanished(player)) {
            if (plugin.getVanishManager().getConfig().getBoolean("silent_join_quit", true)) {
                event.quitMessage(null);
            }
        }
    }
}