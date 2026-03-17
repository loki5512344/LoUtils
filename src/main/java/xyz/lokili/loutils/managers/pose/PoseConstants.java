package xyz.lokili.loutils.managers.pose;

/**
 * Константы для системы поз (1:1 с GSit для 1.20.2+).
 */
public final class PoseConstants {
    
    /** Базовый Y-offset сиденья. GSit: -0.05d для 1.20.2+, 0.2d для старых версий. */
    public static final double BASE_OFFSET = -0.05d;
    
    /** Опускаем игрока на ступеньке ниже на полблока. */
    public static final double STAIR_Y_OFFSET = 0.5d;
    
    /** XZ-сдвиг к «спинке» ступеньки. */
    public static final double STAIR_XZ_OFFSET = 0.123d;
    
    private PoseConstants() {
        throw new UnsupportedOperationException("Utility class");
    }
}
