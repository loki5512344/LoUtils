package xyz.lokili.loutils.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.ColorUtil;

public class PlayerJoinListener implements Listener {
    
    private final LoUtils plugin;
    
    public PlayerJoinListener(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!plugin.getWhitelistManager().isEnabled()) {
            return;
        }
        
        String playerName = event.getName();
        
        if (!plugin.getWhitelistManager().isWhitelisted(playerName)) {
            String kickMessage = plugin.getConfigManager().getWhitelistConfig().getString("kick-message", 
                "&cВы не в белом списке сервера.");
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
                ColorUtil.colorize(kickMessage)
            );
        }
    }
}
