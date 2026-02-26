package xyz.lokili.loutils.constants;

/**
 * Константы для InvSee инвентаря
 * Избегаем magic numbers
 */
public class InvSeeConstants {
    
    // Размеры инвентаря
    public static final int INVENTORY_SIZE = 54;
    public static final int MAIN_INVENTORY_SIZE = 36;
    
    // Слоты разделителей
    public static final int SEPARATOR_START = 36;
    public static final int SEPARATOR_END = 45;
    
    // Слоты брони
    public static final int HELMET_SLOT = 45;
    public static final int CHESTPLATE_SLOT = 46;
    public static final int LEGGINGS_SLOT = 47;
    public static final int BOOTS_SLOT = 48;
    
    // Дополнительные слоты
    public static final int OFFHAND_SLOT = 49;
    public static final int EFFECTS_SLOT = 50;
    public static final int STATUS_SLOT = 53;
    
    // Индексы брони в массиве
    public static final int ARMOR_HELMET_INDEX = 3;
    public static final int ARMOR_CHESTPLATE_INDEX = 2;
    public static final int ARMOR_LEGGINGS_INDEX = 1;
    public static final int ARMOR_BOOTS_INDEX = 0;
    
    private InvSeeConstants() {
        // Utility class
    }
}
