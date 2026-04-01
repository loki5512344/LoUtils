package xyz.lokili.loutils.listeners.crafts;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;

import java.util.Set;

/**
 * Маркер «Эхо-кирки»: только Эффективность (уровень из конфига), без других чар.
 */
public final class EchoPickaxeHelper {

    private EchoPickaxeHelper() {}

    public static NamespacedKey keyMarker(Plugin plugin) {
        return new NamespacedKey(plugin, "echo_pickaxe");
    }

    public static boolean isEchoPickaxe(Plugin plugin, ItemStack stack) {
        if (stack == null || stack.getType() != Material.WOODEN_PICKAXE) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(keyMarker(plugin), PersistentDataType.BYTE);
    }

    public static void mark(Plugin plugin, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(keyMarker(plugin), PersistentDataType.BYTE, (byte) 1);
        stack.setItemMeta(meta);
    }

    /**
     * Оставляет только эффективность нужного уровня, снимает всё остальное.
     * @return true если мета изменилась
     */
    public static boolean normalizeEnchants(ItemStack stack, int efficiencyLevel) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        boolean changed = false;
        if (meta.hasEnchants()) {
            for (Enchantment e : Set.copyOf(meta.getEnchants().keySet())) {
                if (e != Enchantment.EFFICIENCY) {
                    meta.removeEnchant(e);
                    changed = true;
                }
            }
        }
        stack.setItemMeta(meta);
        int eff = stack.getEnchantmentLevel(Enchantment.EFFICIENCY);
        if (eff != efficiencyLevel) {
            stack.removeEnchantment(Enchantment.EFFICIENCY);
            stack.addUnsafeEnchantment(Enchantment.EFFICIENCY, efficiencyLevel);
            changed = true;
        }
        return changed;
    }
}
