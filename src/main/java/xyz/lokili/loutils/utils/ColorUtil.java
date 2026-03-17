package xyz.lokili.loutils.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import dev.lolib.utils.Colors;

import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Color utility wrapper for LoLib Colors with enhanced hex support
 * 
 * @deprecated Use {@link Colors} from LoLib directly
 */
@Deprecated
public class ColorUtil {
    
    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();
    
    /**
     * Parse and colorize message using MiniMessage format
     * Supports: legacy codes (&a), hex (&#RRGGBB), and MiniMessage tags
     */
    public static Component colorize(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        
        // Используем собственную обработку вместо LoLib
        // LoLib не всегда корректно обрабатывает &#RRGGBB формат
        return parseWithFallback(message);
    }
    
    /**
     * Fallback метод для обработки цветов если LoLib не работает
     */
    private static Component parseWithFallback(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        
        try {
            // Конвертируем &#RRGGBB в <#RRGGBB>
            String processed = convertHexToMiniMessage(message);
            
            // Конвертируем legacy коды &a в <green> и т.д.
            processed = convertLegacyToMiniMessage(processed);
            
            // Пробуем MiniMessage
            return MINI_MESSAGE.deserialize(processed);
        } catch (Exception e) {
            try {
                // Если не получается, используем legacy serializer
                String processed = convertHexToLegacy(message);
                return LEGACY.deserialize(processed);
            } catch (Exception ex) {
                // В крайнем случае возвращаем как есть
                return Component.text(message);
            }
        }
    }
    
    /**
     * Конвертирует legacy коды в MiniMessage теги
     */
    private static String convertLegacyToMiniMessage(String message) {
        // Заменяем legacy коды на MiniMessage теги
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
        return message;
    }
    
    /**
     * Конвертирует hex в legacy формат (для старых версий)
     */
    private static String convertHexToLegacy(String message) {
        // Просто заменяем & на §
        return message.replace('&', '§');
    }
    
    /**
     * Конвертирует &#RRGGBB в <#RRGGBB> для MiniMessage
     */
    private static String convertHexToMiniMessage(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }
        
        try {
            Matcher matcher = HEX_PATTERN.matcher(message);
            StringBuffer result = new StringBuffer();
            
            while (matcher.find()) {
                String hexColor = matcher.group(1);
                matcher.appendReplacement(result, "<#" + hexColor + ">");
            }
            matcher.appendTail(result);
            
            return result.toString();
        } catch (Exception e) {
            return message; // Возвращаем оригинал при ошибке
        }
    }
    
    /**
     * Colorize and convert to plain string (for legacy support)
     */
    public static String colorizeToString(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        
        // Используем собственную обработку
        Component component = parseWithFallback(message);
        return LEGACY.serialize(component);
    }
}
