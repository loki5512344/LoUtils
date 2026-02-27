package xyz.lokili.loutils.utils;

import net.kyori.adventure.text.Component;
import dev.lolib.utils.Colors;

/**
 * Color utility wrapper for LoLib Colors
 * 
 * @deprecated Use {@link Colors} from LoLib directly
 */
@Deprecated
public class ColorUtil {
    
    /**
     * Parse and colorize message using MiniMessage format
     * Supports: legacy codes (&a), hex (&#RRGGBB), and MiniMessage tags
     */
    public static Component colorize(String message) {
        if (message == null || message.isEmpty()) {
            return Component.empty();
        }
        return Colors.parse(message);
    }
    
    /**
     * Colorize and convert to plain string (for legacy support)
     */
    public static String colorizeToString(String message) {
        if (message == null || message.isEmpty()) {
            return "";
        }
        return Colors.colorize(message);
    }
}
