package xyz.lokili.loutils.listeners.crafts;

import dev.lolib.utils.Colors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.FireworkMeta;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.List;

/**
 * FireworkCraftListener - Крафт фейерверка 4 уровня
 * 
 * Рецепт: бумага + огненный порошок / 3 пороха
 * Результат: фейерверк с Flight: 4
 */
public class FireworkCraftListener extends BaseListener {
    
    public FireworkCraftListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.CUSTOM_CRAFTS, ConfigConstants.CUSTOM_CRAFTS_CONFIG);
        registerFireworkCraft();
    }
    
    /**
     * Регистрация крафта фейерверка
     */
    private void registerFireworkCraft() {
        if (!checkEnabled()) return;
        if (config == null) {
            plugin.getLogger().warning("Custom crafts config not loaded for firework!");
            return;
        }
        if (!config.getBoolean("firework-level-4.enabled", true)) return;
        
        ItemStack result = new ItemStack(Material.FIREWORK_ROCKET);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "firework_level_4"), result);
        
        recipe.shape("BF ", "XXX", "   ");
        recipe.setIngredient('B', Material.PAPER);
        recipe.setIngredient('F', Material.BLAZE_POWDER);
        recipe.setIngredient('X', Material.GUNPOWDER);
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
        }
    }
    
    /**
     * Модификация результата крафта - добавляем NBT
     */
    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (!checkEnabled()) return;
        if (config == null) return;
        if (!config.getBoolean("firework-level-4.enabled", true)) return;
        
        ItemStack result = event.getInventory().getResult();
        if (result == null || result.getType() != Material.FIREWORK_ROCKET) return;
        
        // Проверяем что это наш крафт (бумага + огненный порошок + 3 пороха)
        ItemStack[] matrix = event.getInventory().getMatrix();
        if (!isFireworkLevel4Craft(matrix)) return;
        
        // Создаём фейерверк с кастомным NBT
        ItemStack customFirework = createCustomFirework();
        event.getInventory().setResult(customFirework);
    }
    
    /**
     * Проверка что это наш крафт
     */
    private boolean isFireworkLevel4Craft(ItemStack[] matrix) {
        int paperCount = 0;
        int blazePowderCount = 0;
        int gunpowderCount = 0;
        
        for (ItemStack item : matrix) {
            if (item == null || item.getType() == Material.AIR) continue;
            
            if (item.getType() == Material.PAPER) paperCount++;
            else if (item.getType() == Material.BLAZE_POWDER) blazePowderCount++;
            else if (item.getType() == Material.GUNPOWDER) gunpowderCount++;
        }
        
        return paperCount == 1 && blazePowderCount == 1 && gunpowderCount == 3;
    }
    
    /**
     * Создание фейерверка с кастомным NBT
     */
    private ItemStack createCustomFirework() {
        if (config == null) return new ItemStack(Material.FIREWORK_ROCKET);
        
        ItemStack firework = new ItemStack(Material.FIREWORK_ROCKET);
        FireworkMeta meta = (FireworkMeta) firework.getItemMeta();
        
        // Устанавливаем длительность полёта
        int flightDuration = config.getInt("firework-level-4.flight-duration", 4);
        meta.setPower(flightDuration);
        
        // Название
        String nameFormat = config.getString("firework-level-4.name-format", "§eФейерверк §7[§f%level%§7]");
        meta.displayName(Colors.parse(nameFormat.replace("%level%", String.valueOf(flightDuration))));
        
        // Описание
        List<String> loreList = config.getStringList("firework-level-4.lore");
        if (!loreList.isEmpty()) {
            List<net.kyori.adventure.text.Component> lore = loreList.stream()
                .map(line -> line.replace("%level%", String.valueOf(flightDuration)))
                .map(ColorUtil::colorize)
                .toList();
            meta.lore(lore);
        }
        
        firework.setItemMeta(meta);
        return firework;
    }
}
