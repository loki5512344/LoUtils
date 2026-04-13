package xyz.lokili.loutils.utils;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import dev.lolib.utils.Colors;

import java.util.Map;
import java.util.regex.Pattern;
import java.util.regex.Matcher;

/**
 * Color utility: MiniMessage + legacy + {@code &#RRGGBB} hex (LoLib {@link Colors} не покрывает все кейсы конфигов).
 */
public class ColorUtil {

    private static final Pattern HEX_PATTERN = Pattern.compile("&#([0-9A-Fa-f]{6})");
    private static final MiniMessage MINI_MESSAGE = MiniMessage.miniMessage();
    private static final LegacyComponentSerializer LEGACY = LegacyComponentSerializer.legacyAmpersand();

    private static final Map<String, String> LEGACY_TO_MINI = Map.ofEntries(
        Map.entry("&0", "<black>"),
        Map.entry("&1", "<dark_blue>"),
        Map.entry("&2", "<dark_green>"),
        Map.entry("&3", "<dark_aqua>"),
        Map.entry("&4", "<dark_red>"),
        Map.entry("&5", "<dark_purple>"),
        Map.entry("&6", "<gold>"),
        Map.entry("&7", "<gray>"),
        Map.entry("&8", "<dark_gray>"),
        Map.entry("&9", "<blue>"),
        Map.entry("&a", "<green>"),
        Map.entry("&b", "<aqua>"),
        Map.entry("&c", "<red>"),
        Map.entry("&d", "<light_purple>"),
        Map.entry("&e", "<yellow>"),
        Map.entry("&f", "<white>"),
        Map.entry("&k", "<obfuscated>"),
        Map.entry("&l", "<bold>"),
        Map.entry("&m", "<strikethrough>"),
        Map.entry("&n", "<underlined>"),
        Map.entry("&o", "<italic>"),
        Map.entry("&r", "<reset>")
    );
    
    /**
     * Parse and colorize message using MiniMessage format
     * Supports: legacy codes (&a), hex (&#RRGGBB), and MiniMessage tags
     */
    public static Component colorize(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }

        try {
            String processed = convertHexToMiniMessage(message);
            processed = convertLegacyToMiniMessage(processed);
            return MINI_MESSAGE.deserialize(processed);
        } catch (Exception e) {
            // Fallback на legacy serializer
            return LEGACY.deserialize(message.replace('&', '§'));
        }
    }

    /**
     * Конвертирует legacy коды в MiniMessage теги
     */
    private static String convertLegacyToMiniMessage(String message) {
        for (Map.Entry<String, String> entry : LEGACY_TO_MINI.entrySet()) {
            message = message.replace(entry.getKey(), entry.getValue());
        }
        return message;
    }
    
    /**
     * Конвертирует &#RRGGBB в <#RRGGBB> для MiniMessage
     */
    private static String convertHexToMiniMessage(String message) {
        if (message == null || message.isEmpty()) {
            return message;
        }

        Matcher matcher = HEX_PATTERN.matcher(message);
        StringBuffer result = new StringBuffer();

        while (matcher.find()) {
            String hexColor = matcher.group(1);
            matcher.appendReplacement(result, "<#" + hexColor + ">");
        }
        matcher.appendTail(result);

        return result.toString();
    }

    /**
     * Colorize and convert to plain string (for legacy support)
     */
    public static String colorizeToString(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }

        Component component = colorize(message);
        return LEGACY.serialize(component);
    }
}
