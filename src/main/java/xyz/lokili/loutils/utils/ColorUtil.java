package xyz.lokili.loutils.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ColorUtil {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern LEGACY_PATTERN = Pattern.compile("&([0-9a-fk-orA-FK-OR])");
    
    public static Component colorize(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        
        // Конвертируем &#RRGGBB в MiniMessage формат <#RRGGBB>
        Matcher hexMatcher = HEX_PATTERN.matcher(message);
        StringBuilder sb = new StringBuilder();
        while (hexMatcher.find()) {
            hexMatcher.appendReplacement(sb, "<#" + hexMatcher.group(1) + ">");
        }
        hexMatcher.appendTail(sb);
        message = sb.toString();
        
        // Конвертируем legacy коды &c в MiniMessage
        message = convertLegacyToMiniMessage(message);
        
        return MiniMessage.miniMessage().deserialize(message);
    }
    
    private static String convertLegacyToMiniMessage(String message) {
        // Маппинг legacy кодов на MiniMessage теги
        message = message.replace("&0", "<black>");
        message = message.replace("&1", "<dark_blue>");
        message = message.replace("&2", "<dark_green>");
        message = message.replace("&3", "<dark_aqua>");
        message = message.replace("&4", "<dark_red>");
        message = message.replace("&5", "<dark_purple>");
        message = message.replace("&6", "<gold>");
        message = message.replace("&7", "<gray>");
        message = message.replace("&8", "<dark_gray>");
        message = message.replace("&9", "<blue>");
        message = message.replace("&a", "<green>");
        message = message.replace("&b", "<aqua>");
        message = message.replace("&c", "<red>");
        message = message.replace("&d", "<light_purple>");
        message = message.replace("&e", "<yellow>");
        message = message.replace("&f", "<white>");
        message = message.replace("&k", "<obfuscated>");
        message = message.replace("&l", "<bold>");
        message = message.replace("&m", "<strikethrough>");
        message = message.replace("&n", "<underlined>");
        message = message.replace("&o", "<italic>");
        message = message.replace("&r", "<reset>");
        
        // Uppercase варианты
        message = message.replace("&A", "<green>");
        message = message.replace("&B", "<aqua>");
        message = message.replace("&C", "<red>");
        message = message.replace("&D", "<light_purple>");
        message = message.replace("&E", "<yellow>");
        message = message.replace("&F", "<white>");
        message = message.replace("&K", "<obfuscated>");
        message = message.replace("&L", "<bold>");
        message = message.replace("&M", "<strikethrough>");
        message = message.replace("&N", "<underlined>");
        message = message.replace("&O", "<italic>");
        message = message.replace("&R", "<reset>");
        
        return message;
    }
}
