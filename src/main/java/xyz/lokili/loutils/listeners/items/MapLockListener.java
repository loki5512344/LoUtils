package xyz.lokili.loutils.listeners.items;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

public class MapLockListener extends BaseListener {

    private final NamespacedKey lockedKey;

    public MapLockListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, "map-locking", ConfigConstants.MAP_LOCKING_CONFIG);
        this.lockedKey = new NamespacedKey(plugin, "map_locked_by");
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onMapCraft(PrepareItemCraftEvent event) {
        if (!checkEnabled()) return;
        if (moduleConfig() == null || !moduleConfig().getBoolean("prevent-copying", true)) return;

        var inv = event.getInventory();
        if (inv == null) return;

        ItemStack result = inv.getResult();
        if (result == null || result.getType() != Material.FILLED_MAP) return;

        // Проверяем все ингредиенты на наличие заблокированной карты
        ItemStack[] matrix = inv.getMatrix();
        if (matrix == null) return;

        for (ItemStack item : matrix) {
            if (item == null || item.getType() != Material.FILLED_MAP) continue;
            
            ItemMeta meta = item.getItemMeta();
            if (meta == null) continue;
            
            if (meta.getPersistentDataContainer().has(lockedKey, PersistentDataType.STRING)) {
                inv.setResult(null);
                return;
            }
        }
    }
}
