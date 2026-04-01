package xyz.lokili.loutils.managers.pose;

/**
 * Типы поз игрока.
 */
public enum PoseType {
    /** Сидение на блоке */
    SIT,

    /** Сидение на игроке (через невидимый стенд-посредник) */
    SIT_ON_PLAYER,
    
    /** Лежание на блоке */
    LAY,
    
    /** Ползание */
    CRAWL
}
