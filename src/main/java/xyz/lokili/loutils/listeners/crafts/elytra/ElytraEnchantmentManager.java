package xyz.lokili.loutils.listeners.crafts.elytra;

import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashSet;
import java.util.Set;

/**
 * Управление зачарованиями кастомных элитр
 * Single Responsibility: только работа с enchantments
 */
public class ElytraEnchantmentManager {

    /**
     * Убирает все зачарования с предмета
     */
    public void stripAllEnchants(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasEnchants()) {
            return;
        }

        for (Enchantment enchant : meta.getEnchants().keySet()) {
            meta.removeEnchant(enchant);
        }

        stack.setItemMeta(meta);
    }

    /**
     * Убирает все зачарования, кроме Починки I
     * Уровень починки принудительно устанавливается в 1
     */
    public void stripForbiddenEnchants(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasEnchants()) {
            return;
        }

        boolean hadMending = meta.hasEnchant(Enchantment.MENDING);
        Set<Enchantment> enchants = new HashSet<>(meta.getEnchants().keySet());

        for (Enchantment enchant : enchants) {
            if (enchant != Enchantment.MENDING) {
                meta.removeEnchant(enchant);
            }
        }

        if (hadMending) {
            meta.removeEnchant(Enchantment.MENDING);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        }

        stack.setItemMeta(meta);
    }

    /**
     * Проверяет, есть ли запрещённые зачарования
     * Разрешена только Починка I
     */
    public boolean hasForbiddenEnchants(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasEnchants()) {
            return false;
        }

        for (Enchantment enchant : meta.getEnchants().keySet()) {
            if (enchant != Enchantment.MENDING) {
                return true;
            }
            if (meta.getEnchantLevel(Enchantment.MENDING) != 1) {
                return true;
            }
        }

        return false;
    }
}
