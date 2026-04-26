package xyz.lokili.loutils.utils.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * Базовый helper для работы с кастомными предметами через PDC
 */
public abstract class CustomItemHelper {

    protected final Plugin plugin;
    protected final String markerKey;
    protected final Material itemType;

    protected CustomItemHelper(Plugin plugin, String markerKey, Material itemType) {
        this.plugin = plugin;
        this.markerKey = markerKey;
        this.itemType = itemType;
    }

    /**
     * Получить ключ маркера для PDC
     */
    protected NamespacedKey getMarkerKey() {
        return new NamespacedKey(plugin, markerKey);
    }

    /**
     * Проверяет, является ли предмет кастомным
     */
    public boolean isCustomItem(ItemStack stack) {
        if (stack == null || stack.getType() != itemType) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return false;
        }
        return meta.getPersistentDataContainer().has(getMarkerKey(), PersistentDataType.BYTE);
    }

    /**
     * Помечает предмет как кастомный
     */
    public void markAsCustom(ItemStack stack) {
        if (stack == null || stack.getType() != itemType) {
            return;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.getPersistentDataContainer().set(getMarkerKey(), PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);
    }

    /**
     * Получить значение из PDC
     */
    protected <T, Z> Z getPDCValue(ItemStack stack, NamespacedKey key, PersistentDataType<T, Z> type, Z defaultValue) {
        if (!isCustomItem(stack)) {
            return defaultValue;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return defaultValue;
        }
        Z value = meta.getPersistentDataContainer().get(key, type);
        return value != null ? value : defaultValue;
    }

    /**
     * Установить значение в PDC
     */
    protected <T, Z> void setPDCValue(ItemStack stack, NamespacedKey key, PersistentDataType<T, Z> type, Z value) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        if (value == null) {
            meta.getPersistentDataContainer().remove(key);
        } else {
            meta.getPersistentDataContainer().set(key, type, value);
        }
        stack.setItemMeta(meta);
    }

    /**
     * Удалить значение из PDC
     */
    protected void removePDCValue(ItemStack stack, NamespacedKey key) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }
        meta.getPersistentDataContainer().remove(key);
        stack.setItemMeta(meta);
    }
}
