package xyz.lokili.loutils.listeners;

import dev.lolib.utils.Colors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;

/**
 * DebugStickListener - Дебаг палка
 * 
 * Механики:
 * 1. Крафт: 1 палка + 8 лазурита
 * 2. Работает как ванильная дебаг палка
 * 3. НЕ может делать блоки waterlogged
 * 4. Требует permission loutils.debugstick
 */
public class DebugStickListener implements Listener {
    
    private final LoUtils plugin;
    private final NamespacedKey debugStickKey;
    
    public DebugStickListener(LoUtils plugin) {
        this.plugin = plugin;
        this.debugStickKey = new NamespacedKey(plugin, "debug_stick");
        registerRecipe();
    }
    
    /**
     * Регистрация крафта: 1 палка + 8 лазурита
     */
    private void registerRecipe() {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.DEBUG_STICK_CONFIG);
        if (!config.getBoolean("crafting-enabled", true)) return;
        
        ItemStack result = createDebugStick();
        
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
    
    /**
     * Создание дебаг палки
     */
    private ItemStack createDebugStick() {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.DEBUG_STICK_CONFIG);
        
        ItemStack item = new ItemStack(Material.DEBUG_STICK);
        ItemMeta meta = item.getItemMeta();
        
        // Маркер кастомной дебаг палки
        meta.getPersistentDataContainer().set(debugStickKey, org.bukkit.persistence.PersistentDataType.BYTE, (byte) 1);
        
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
     * Использование дебаг палки - блокируем waterlogging
     */
    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!plugin.getConfigManager().isModuleEnabled(ConfigConstants.Modules.DEBUG_STICK)) return;
        
        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() != Material.DEBUG_STICK) return;
        if (!item.hasItemMeta()) return;
        
        ItemMeta meta = item.getItemMeta();
        if (!meta.getPersistentDataContainer().has(debugStickKey, org.bukkit.persistence.PersistentDataType.BYTE)) {
            return; // Это ванильная дебаг палка
        }
        
        // Проверка permission
        var config = plugin.getConfigManager().getConfig(ConfigConstants.DEBUG_STICK_CONFIG);
        if (config.getBoolean("require-permission", true)) {
            if (!player.hasPermission(ConfigConstants.Permissions.DEBUG_STICK)) {
                event.setCancelled(true);
                return;
            }
        }
        
        // Блокируем waterlogging
        if (event.getAction() == Action.LEFT_CLICK_BLOCK || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            Block block = event.getClickedBlock();
            if (block != null) {
                BlockData blockData = block.getBlockData();
                
                // Если блок может быть waterlogged, блокируем изменение этого свойства
                if (blockData instanceof Waterlogged) {
                    // Дебаг палка не может изменять waterlogged свойство
                    // Мы не можем полностью заблокировать, но можем предупредить
                    // Ванильная механика дебаг палки работает на клиенте
                    // Поэтому просто не даём использовать на waterlogged блоках
                    event.setCancelled(true);
                    player.sendMessage(Colors.parse("§cДебаг палка не может изменять waterlogged блоки!"));
                }
            }
        }
    }
}
