package xyz.lokili.loutils.listeners.items;

import dev.lolib.utils.Colors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

/**
 * DebugStickListener - Дебаг палка
 * 
 * Механики:
 * 1. Крафт: 1 палка + 8 лазурита
 * 2. Работает как ванильная дебаг палка
 * 3. Запрещена в аду (Nether)
 * 4. Требует permission loutils.debugstick
 */
public class DebugStickListener extends BaseListener {
    private final NamespacedKey debugStickKey;
    
    public DebugStickListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.DEBUG_STICK, ConfigConstants.DEBUG_STICK_CONFIG);
        this.debugStickKey = new NamespacedKey(plugin, "debug_stick");
        registerRecipe();
    }
    
    /**
     * Регистрация крафта: 1 палка + 8 лазурита
     */
    private void registerRecipe() {
        if (!moduleConfig().getBoolean("crafting-enabled", true)) return;
        
        ItemStack result = buildDebugStick();
        
        ShapedRecipe recipe = new ShapedRecipe(
            new NamespacedKey(plugin, "debug_stick"),
            result
        );
        
        recipe.shape("LLL", "LSL", "LLL");
        recipe.setIngredient('L', Material.LAPIS_LAZULI);
        recipe.setIngredient('S', Material.STICK);
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException e) {
            // Recipe already exists
        }
    }
    
    private ItemStack buildDebugStick() {
        org.bukkit.configuration.file.YamlConfiguration empty = new org.bukkit.configuration.file.YamlConfiguration();
        return plugin.getContainer().getItemFactory().createDebugStick(moduleConfig() != null ? moduleConfig() : empty);
    }
    
    /**
     * Использование дебаг палки - запрещаем в аду
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!checkEnabled()) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() != Material.DEBUG_STICK) return;
        if (!item.hasItemMeta()) return;
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(debugStickKey, org.bukkit.persistence.PersistentDataType.BYTE)) {
            return; // Это ванильная дебаг палка
        }
        
        // Проверка permission
        if (moduleConfig().getBoolean("require-permission", true)) {
            if (!player.hasPermission(ConfigConstants.Permissions.DEBUG_STICK)) {
                event.setCancelled(true);
                player.sendMessage(Colors.parse("§cУ вас нет прав на использование дебаг палки!"));
                return;
            }
        }
        
        // Запрещаем использование в аду (Nether)
        if (player.getWorld().getEnvironment() == org.bukkit.World.Environment.NETHER) {
            event.setCancelled(true);
            player.sendMessage(Colors.parse("§cДебаг палка не работает в аду!"));
            return;
        }
        
        // В обычном мире разрешаем всё, включая waterlogged
    }
}
