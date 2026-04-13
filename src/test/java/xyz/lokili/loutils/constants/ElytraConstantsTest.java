package xyz.lokili.loutils.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Тесты для ElytraConstants
 */
class ElytraConstantsTest {

    @Test
    void testTierConstants() {
        assertEquals(1, ElytraConstants.TIER_1);
        assertEquals(2, ElytraConstants.TIER_2);
        assertEquals(1, ElytraConstants.MIN_TIER);
        assertEquals(2, ElytraConstants.MAX_TIER);
    }

    @Test
    void testDefaultFlightValues() {
        assertEquals(6 * 60 * 20, ElytraConstants.DEFAULT_FLIGHT_LIMIT_TICKS);
        assertEquals(180, ElytraConstants.DEFAULT_RECHARGE_SECONDS);
        assertEquals(500, ElytraConstants.DEFAULT_MAX_DAMAGE);
    }

    @Test
    void testTier2DefaultValues() {
        assertEquals(7200, ElytraConstants.DEFAULT_TIER2_FLIGHT_LIMIT_TICKS);
        assertEquals(180, ElytraConstants.DEFAULT_TIER2_RECHARGE_SECONDS);
        assertEquals(550, ElytraConstants.DEFAULT_TIER2_MAX_DAMAGE);
    }

    @Test
    void testFlightBarConstants() {
        assertEquals(18, ElytraConstants.DEFAULT_FLIGHT_BAR_SEGMENTS);
        assertEquals(4, ElytraConstants.MIN_FLIGHT_BAR_SEGMENTS);
    }

    @Test
    void testConversionConstants() {
        assertEquals(20, ElytraConstants.TICKS_PER_SECOND);
        assertEquals(60, ElytraConstants.SECONDS_PER_MINUTE);
        assertEquals(1000, ElytraConstants.MS_PER_SECOND);
    }

    @Test
    void testDefaultColors() {
        assertNotNull(ElytraConstants.DEFAULT_HEAT_COLORS);
        assertEquals(4, ElytraConstants.DEFAULT_HEAT_COLORS.length);
        assertEquals("&c", ElytraConstants.DEFAULT_HEAT_COLORS[0]);
        assertEquals("&6", ElytraConstants.DEFAULT_HEAT_COLORS[1]);
        assertEquals("&e", ElytraConstants.DEFAULT_HEAT_COLORS[2]);
        assertEquals("&a", ElytraConstants.DEFAULT_HEAT_COLORS[3]);
    }

    @Test
    void testDefaultBarElements() {
        assertEquals("&8|", ElytraConstants.DEFAULT_EMPTY_COLOR);
        assertEquals("&8[", ElytraConstants.DEFAULT_BAR_PREFIX);
        assertEquals("&8]", ElytraConstants.DEFAULT_BAR_SUFFIX);
        assertEquals("|", ElytraConstants.DEFAULT_BAR_PIPE);
    }

    @Test
    void testDefaultMessages() {
        assertNotNull(ElytraConstants.DEFAULT_NAME_FORMAT);
        assertNotNull(ElytraConstants.DEFAULT_TIER2_NAME_FORMAT);
        assertNotNull(ElytraConstants.DEFAULT_TIME_LINE);
        assertNotNull(ElytraConstants.DEFAULT_RECHARGE_LINE);

        assertTrue(ElytraConstants.DEFAULT_TIME_LINE.contains("{used}"));
        assertTrue(ElytraConstants.DEFAULT_TIME_LINE.contains("{limit}"));
        assertTrue(ElytraConstants.DEFAULT_RECHARGE_LINE.contains("{seconds}"));
    }

    @Test
    void testFlightLimitCalculation() {
        // 6 минут = 6 * 60 секунд * 20 тиков
        int expectedTicks = 6 * ElytraConstants.SECONDS_PER_MINUTE * ElytraConstants.TICKS_PER_SECOND;
        assertEquals(expectedTicks, ElytraConstants.DEFAULT_FLIGHT_LIMIT_TICKS);
    }

    @Test
    void testRechargeTimeCalculation() {
        // 180 секунд = 180000 миллисекунд
        int expectedMs = ElytraConstants.DEFAULT_RECHARGE_SECONDS * ElytraConstants.MS_PER_SECOND;
        assertEquals(180000, expectedMs);
    }
}
