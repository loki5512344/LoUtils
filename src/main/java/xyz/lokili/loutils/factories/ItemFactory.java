package xyz.lokili.loutils.factories;

import dev.lolib.utils.Colors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

/**
 * Фабрика для создания кастомных предметов
 * Устраняет дублирование кода создания ItemStack
 * Применяет Factory Pattern
 */
public class ItemFactory {
    
    private final Plugin plugin;
    
    public ItemFactory(Plugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * Палка проверки инвентаря (конфиг {@code conf/inventory-check-stick.yml}).
     */
    public ItemStack createInventoryCheckStick(FileConfiguration config) {
        ItemStack item = new ItemStack(Material.STICK);
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "inventory_check_stick");
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        String name = config.getString("item-name", "&6Палка проверки");
        meta.displayName(Colors.parse(name));
        int cmd = config.getInt("custom-model-data", 0);
        if (cmd > 0) {
            meta.setCustomModelData(cmd);
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Кандалы / наручники (поводок с маркером).
     */
    public ItemStack createHandcuffs(FileConfiguration config) {
        ItemStack item = new ItemStack(Material.LEAD);
        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, "handcuffs_item");
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        meta.displayName(Colors.parse(config.getString("item-name", "&7Кандалы")));
        int cmd = config.getInt("custom-model-data", 0);
        if (cmd > 0) {
            meta.setCustomModelData(cmd);
        }
        item.setItemMeta(meta);
        return item;
    }

    /**
     * Создаёт дебаг палку
     */
    public ItemStack createDebugStick(FileConfiguration config) {
        ItemStack item = new ItemStack(Material.DEBUG_STICK);
        ItemMeta meta = item.getItemMeta();
        
        // Маркер кастомной дебаг палки
        NamespacedKey key = new NamespacedKey(plugin, "debug_stick");
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        
        // Название
        meta.displayName(Colors.parse("§6Дебаг палка"));
        
        // CustomModelData
        int customModelData = config.getInt("custom-model-data", 0);
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Создаёт невидимую рамку
     */
    public ItemStack createInvisibleFrame(FileConfiguration config) {
        ItemStack item = new ItemStack(Material.ITEM_FRAME);
        ItemMeta meta = item.getItemMeta();
        
        // Маркер невидимой рамки
        NamespacedKey key = new NamespacedKey(plugin, "invisible_frame");
        meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        
        // CustomModelData
        int customModelData = config.getInt("custom-model-data", 0);
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Создаёт блок света с указанным уровнем
     */
    public ItemStack createLightBlock(int level, FileConfiguration config) {
        ItemStack item = new ItemStack(Material.LIGHT);
        ItemMeta meta = item.getItemMeta();
        
        // Сохраняем уровень
        NamespacedKey key = new NamespacedKey(plugin, "light_level");
        meta.getPersistentDataContainer().set(key, PersistentDataType.INTEGER, level);
        
        // Название с уровнем
        String nameFormat = config.getString("name-format", "§eИсточник света §7[§f%level%§7]");
        meta.displayName(Colors.parse(nameFormat.replace("%level%", String.valueOf(level))));
        
        // CustomModelData
        int customModelData = config.getInt("custom-model-data", 0);
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        
        item.setItemMeta(meta);
        return item;
    }
    
    /**
     * Создаёт предмет с базовыми параметрами
     */
    public ItemStack createCustomItem(Material material, String name, String markerKey, FileConfiguration config) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        
        // Маркер
        if (markerKey != null) {
            NamespacedKey key = new NamespacedKey(plugin, markerKey);
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);
        }
        
        // Название
        if (name != null) {
            meta.displayName(Colors.parse(name));
        }
        
        // CustomModelData
        int customModelData = config.getInt("custom-model-data", 0);
        if (customModelData > 0) {
            meta.setCustomModelData(customModelData);
        }
        
        item.setItemMeta(meta);
        return item;
    }
}
