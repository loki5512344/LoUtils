package xyz.lokili.loutils.constants;

/**
 * Константы для кастомных элитр
 */
public final class ElytraConstants {

    private ElytraConstants() {}

    // Тиры элитр
    public static final int TIER_1 = 1;
    public static final int TIER_2 = 2;
    public static final int MIN_TIER = 1;
    public static final int MAX_TIER = 2;

    // Дефолтные значения для Tier 1
    public static final int DEFAULT_FLIGHT_LIMIT_TICKS = 6 * 60 * 20; // 6 минут
    public static final int DEFAULT_RECHARGE_SECONDS = 180; // 3 минуты
    public static final int DEFAULT_MAX_DAMAGE = 500;

    // Дефолтные значения для Tier 2
    public static final int DEFAULT_TIER2_FLIGHT_LIMIT_TICKS = 7200; // 6 минут
    public static final int DEFAULT_TIER2_RECHARGE_SECONDS = 180; // 3 минуты
    public static final int DEFAULT_TIER2_MAX_DAMAGE = 550;

    // Полоска прогресса
    public static final int DEFAULT_FLIGHT_BAR_SEGMENTS = 18;
    public static final int MIN_FLIGHT_BAR_SEGMENTS = 4;

    // Конверсия
    public static final int TICKS_PER_SECOND = 20;
    public static final int SECONDS_PER_MINUTE = 60;
    public static final int MS_PER_SECOND = 1000;

    // Дефолтные цвета для полоски
    public static final String[] DEFAULT_HEAT_COLORS = {"&c", "&6", "&e", "&a"};
    public static final String DEFAULT_EMPTY_COLOR = "&8|";
    public static final String DEFAULT_BAR_PREFIX = "&8[";
    public static final String DEFAULT_BAR_SUFFIX = "&8]";
    public static final String DEFAULT_BAR_PIPE = "|";

    // Дефолтные сообщения
    public static final String DEFAULT_NAME_FORMAT = "&cКрылья фантома";
    public static final String DEFAULT_TIER2_NAME_FORMAT = "&bКрылья фантома &7[II]";
    public static final String DEFAULT_TIME_LINE = "&7Полёт: &f{used} &7/ &f{limit}";
    public static final String DEFAULT_RECHARGE_LINE = "&cПерезарядка: &f{seconds} &7сек";
}
