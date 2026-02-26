package xyz.lokili.loutils.utils;

import net.kyori.adventure.text.Component;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ColorUtilTest {
    
    @Test
    void testComponentConversion() {
        Component component = ColorUtil.colorize("&cTest");
        assertNotNull(component);
    }
    
    @Test
    void testComponentWithHex() {
        Component component = ColorUtil.colorize("&#FF5555Test");
        assertNotNull(component);
    }
    
    @Test
    void testColorizeToString() {
        String input = "&cTest";
        String result = ColorUtil.colorizeToString(input);
        assertNotNull(result);
        assertTrue(result.contains("Test"));
    }
    
    @Test
    void testColorizeToStringWithHex() {
        String input = "&#FF5555Red Text";
        String result = ColorUtil.colorizeToString(input);
        assertNotNull(result);
        assertTrue(result.contains("Red Text"));
    }
    
    @Test
    void testEmptyString() {
        Component result = ColorUtil.colorize("");
        assertNotNull(result);
    }
}
