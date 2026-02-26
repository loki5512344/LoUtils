package xyz.lokili.loutils.utils;

import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Утилита для создания ItemStack с meta
 * Применяет DRY принцип - избегает дублирования кода создания предметов
 */
public class ItemBuilder {
    
    private final ItemStack item;
    private final ItemMeta meta;
    
    private ItemBuilder(Material material) {
        this.item = new ItemStack(material);
        this.meta = item.getItemMeta();
    }
    
    /**
     * Создать builder для материала
     */
    public static ItemBuilder of(Material material) {
        return new ItemBuilder(material);
    }
    
    /**
     * Установить отображаемое имя
     */
    public ItemBuilder name(String name) {
        if (meta != null) {
            meta.displayName(Component.text(ColorUtil.colorizeToString(name)));
        }
        return this;
    }
    
    /**
     * Установить lore (список строк)
     */
    public ItemBuilder lore(List<String> lore) {
        if (meta != null && lore != null) {
            List<Component> components = lore.stream()
                .map(ColorUtil::colorizeToString)
                .map(Component::text)
                .toList();
            meta.lore(components);
        }
        return this;
    }
    
    /**
     * Добавить одну строку в lore
     */
    public ItemBuilder addLoreLine(String line) {
        if (meta != null) {
            List<Component> currentLore = meta.lore();
            if (currentLore == null) {
                currentLore = new java.util.ArrayList<>();
            } else {
                currentLore = new java.util.ArrayList<>(currentLore);
            }
            currentLore.add(Component.text(ColorUtil.colorizeToString(line)));
            meta.lore(currentLore);
        }
        return this;
    }
    
    /**
     * Построить ItemStack
     */
    public ItemStack build() {
        if (meta != null) {
            item.setItemMeta(meta);
        }
        return item;
    }
    
    /**
     * Быстрое создание предмета с именем
     */
    public static ItemStack create(Material material, String name) {
        return of(material).name(name).build();
    }
    
    /**
     * Быстрое создание предмета с именем и lore
     */
    public static ItemStack create(Material material, String name, List<String> lore) {
        return of(material).name(name).lore(lore).build();
    }
}
