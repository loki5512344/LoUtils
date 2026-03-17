package xyz.lokili.loutils.constants;

/**
 * Константы для игровой механики
 * Устраняет magic numbers в коде
 */
public class GameplayConstants {
    
    // Cauldron
    public static final int CAULDRON_EMPTY_LEVEL = 0;
    public static final double PARTICLE_SPREAD_X = 0.3;
    public static final double PARTICLE_SPREAD_Y = 0.3;
    public static final double PARTICLE_SPREAD_Z = 0.3;
    public static final double PARTICLE_OFFSET_Y = 0.5;
    public static final double BLOCK_CENTER_OFFSET = 0.5;
    
    // Light Block
    public static final int LIGHT_MIN_LEVEL = 0;
    public static final int LIGHT_MAX_LEVEL = 15;
    public static final int LIGHT_DEFAULT_LEVEL = 15;
    
    // Sleep Percentage
    public static final int DEFAULT_SLEEP_PERCENTAGE = 30;
    public static final int MIN_SLEEP_PERCENTAGE = 1;
    public static final int MAX_SLEEP_PERCENTAGE = 100;
    
    // Fast Leaf Decay
    public static final int DEFAULT_DECAY_DELAY_TICKS = 40; // 2 seconds
    public static final int MIN_DECAY_DELAY_TICKS = 1;
    public static final int MAX_DECAY_DELAY_TICKS = 200; // 10 seconds
    
    // Villager Leash
    public static final double VILLAGER_FOLLOW_DISTANCE = 10.0;
    public static final double VILLAGER_MOVEMENT_SPEED = 0.5;
    
    // Player movement threshold
    public static final double PLAYER_MOVEMENT_THRESHOLD = 0.5;
    
    private GameplayConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
