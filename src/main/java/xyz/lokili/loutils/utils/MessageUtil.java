package xyz.lokili.loutils.utils;

import org.bukkit.command.CommandSender;
import xyz.lokili.loutils.LoUtils;

public class MessageUtil {
    
    private final LoUtils plugin;
    
    public MessageUtil(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    public void send(CommandSender sender, String key) {
        send(sender, key, new String[0]);
    }
    
    public void send(CommandSender sender, String key, String... replacements) {
        String prefix = plugin.getContainer().getConfigManager().getPrefix();
        String message = plugin.getContainer().getConfigManager().getMessage(key);
        
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        
        // Используем улучшенный ColorUtil
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    public void sendRaw(CommandSender sender, String message) {
        sender.sendMessage(ColorUtil.colorize(message));
    }
    
    /**
     * Отправляет сообщение без префикса (для отладки цветов)
     */
    public void sendWithoutPrefix(CommandSender sender, String key, String... replacements) {
        String message = plugin.getContainer().getConfigManager().getMessage(key);
        
        for (int i = 0; i < replacements.length - 1; i += 2) {
            message = message.replace(replacements[i], replacements[i + 1]);
        }
        
        sender.sendMessage(ColorUtil.colorize(message));
    }
}
