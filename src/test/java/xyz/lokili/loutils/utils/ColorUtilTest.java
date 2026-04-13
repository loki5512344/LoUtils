package xyz.lokili.loutils.utils;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ColorUtil
 */
class ColorUtilTest {

    @Test
    void testColorize_LegacyCodes() {
        Component result = ColorUtil.colorize("&aЗелёный &cКрасный");
        assertNotNull(result);
    }

    @Test
    void testColorize_HexColors() {
        Component result = ColorUtil.colorize("&#FF5733Оранжевый");
        assertNotNull(result);
    }

    @Test
    void testColorize_MixedFormats() {
        Component result = ColorUtil.colorize("&a&#FF5733Смешанный &lЖирный");
        assertNotNull(result);
    }

    @Test
    void testColorize_EmptyString() {
        Component result = ColorUtil.colorize("");
        assertEquals(Component.empty(), result);
    }

    @Test
    void testColorize_NullString() {
        Component result = ColorUtil.colorize(null);
        assertEquals(Component.empty(), result);
    }

    @Test
    void testColorizeToString_LegacyCodes() {
        String result = ColorUtil.colorizeToString("&aТест");
        assertNotNull(result);
        assertFalse(result.isEmpty());
    }

    @Test
    void testColorizeToString_EmptyString() {
        String result = ColorUtil.colorizeToString("");
        assertEquals("", result);
    }

    @Test
    void testColorizeToString_NullString() {
        String result = ColorUtil.colorizeToString(null);
        assertEquals("", result);
    }

    @Test
    void testColorize_AllLegacyCodes() {
        String input = "&0&1&2&3&4&5&6&7&8&9&a&b&c&d&e&f&k&l&m&n&o&rТест";
        Component result = ColorUtil.colorize(input);
        assertNotNull(result);
    }

    @Test
    void testColorize_MultipleHexColors() {
        String input = "&#FF0000Красный &#00FF00Зелёный &#0000FFСиний";
        Component result = ColorUtil.colorize(input);
        assertNotNull(result);
    }
}
