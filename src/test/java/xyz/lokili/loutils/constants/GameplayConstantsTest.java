package xyz.lokili.loutils.constants;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameplayConstantsTest {
    
    @Test
    void testCauldronConstants() {
        assertEquals(0, GameplayConstants.CAULDRON_EMPTY_LEVEL);
        assertEquals(0.3, GameplayConstants.PARTICLE_SPREAD_X);
        assertEquals(0.3, GameplayConstants.PARTICLE_SPREAD_Y);
        assertEquals(0.3, GameplayConstants.PARTICLE_SPREAD_Z);
        assertEquals(0.5, GameplayConstants.BLOCK_CENTER_OFFSET);
    }
    
    @Test
    void testLightBlockConstants() {
        assertEquals(0, GameplayConstants.LIGHT_MIN_LEVEL);
        assertEquals(15, GameplayConstants.LIGHT_MAX_LEVEL);
        assertEquals(15, GameplayConstants.LIGHT_DEFAULT_LEVEL);
        
        assertTrue(GameplayConstants.LIGHT_MIN_LEVEL < GameplayConstants.LIGHT_MAX_LEVEL);
    }
    
    @Test
    void testSleepConstants() {
        assertEquals(30, GameplayConstants.DEFAULT_SLEEP_PERCENTAGE);
        assertEquals(1, GameplayConstants.MIN_SLEEP_PERCENTAGE);
        assertEquals(100, GameplayConstants.MAX_SLEEP_PERCENTAGE);
        
        assertTrue(GameplayConstants.DEFAULT_SLEEP_PERCENTAGE >= GameplayConstants.MIN_SLEEP_PERCENTAGE);
        assertTrue(GameplayConstants.DEFAULT_SLEEP_PERCENTAGE <= GameplayConstants.MAX_SLEEP_PERCENTAGE);
    }
    
    @Test
    void testLeafDecayConstants() {
        assertEquals(40, GameplayConstants.DEFAULT_DECAY_DELAY_TICKS);
        assertEquals(1, GameplayConstants.MIN_DECAY_DELAY_TICKS);
        assertEquals(200, GameplayConstants.MAX_DECAY_DELAY_TICKS);
        
        assertTrue(GameplayConstants.DEFAULT_DECAY_DELAY_TICKS >= GameplayConstants.MIN_DECAY_DELAY_TICKS);
        assertTrue(GameplayConstants.DEFAULT_DECAY_DELAY_TICKS <= GameplayConstants.MAX_DECAY_DELAY_TICKS);
    }
    
    @Test
    void testVillagerConstants() {
        assertEquals(10.0, GameplayConstants.VILLAGER_FOLLOW_DISTANCE);
        assertEquals(0.5, GameplayConstants.VILLAGER_MOVEMENT_SPEED);
        
        assertTrue(GameplayConstants.VILLAGER_FOLLOW_DISTANCE > 0);
        assertTrue(GameplayConstants.VILLAGER_MOVEMENT_SPEED > 0);
    }
    
    @Test
    void testPlayerMovementThreshold() {
        assertEquals(0.5, GameplayConstants.PLAYER_MOVEMENT_THRESHOLD);
        assertTrue(GameplayConstants.PLAYER_MOVEMENT_THRESHOLD > 0);
    }
    
    @Test
    void testConstantsArePositive() {
        assertTrue(GameplayConstants.PARTICLE_SPREAD_X >= 0);
        assertTrue(GameplayConstants.PARTICLE_SPREAD_Y >= 0);
        assertTrue(GameplayConstants.PARTICLE_SPREAD_Z >= 0);
        assertTrue(GameplayConstants.BLOCK_CENTER_OFFSET >= 0);
    }
}
