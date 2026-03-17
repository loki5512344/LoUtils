package xyz.lokili.loutils.listeners;

import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.AsyncPlayerPreLoginEvent;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.listeners.base.BaseListener;
import xyz.lokili.loutils.utils.ColorUtil;

/**
 * PlayerJoinListener - Проверка whitelist при входе
 * Всегда включен (не зависит от модуля)
 */
public class PlayerJoinListener extends BaseListener {
    
    public PlayerJoinListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, null, null); // Всегда включен
    }
    
    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerPreLogin(AsyncPlayerPreLoginEvent event) {
        if (!plugin.getWhitelistManager().isEnabled()) {
            return;
        }
        
        String playerName = event.getName();
        
        if (!plugin.getWhitelistManager().isWhitelisted(playerName)) {
            String kickMessage = configManager.getWhitelistConfig().getString("kick-message", 
                "<#3BA8FF><bold>LoUtils Whitelist</bold>\n\n<gray>Вы не в белом списке сервера.\n<gray>Обратитесь к администрации.");
            
            // Обрабатываем многострочное сообщение
            kickMessage = kickMessage.replace("\n", "\n");
            
            event.disallow(
                AsyncPlayerPreLoginEvent.Result.KICK_WHITELIST,
                ColorUtil.colorize(kickMessage)
            );
        }
    }
}
