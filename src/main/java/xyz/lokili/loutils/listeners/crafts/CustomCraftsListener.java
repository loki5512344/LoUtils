package xyz.lokili.loutils.listeners.crafts;

import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

/**
 * CustomCraftsListener - Регистрация кастомных крафтов
 * 
 * Крафты:
 * 1. Колокол (bell)
 * 2. Красный гриб блок (red mushroom block)
 * 3. Коричневый гриб блок (brown mushroom block)
 * 4. Паутина (cobweb)
 * 5. Подмостки x4 (scaffolding)
 * 6. Фейерверк 4 уровня (firework level 4)
 */
public class CustomCraftsListener extends BaseListener {
    
    public CustomCraftsListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.CUSTOM_CRAFTS, ConfigConstants.CUSTOM_CRAFTS_CONFIG);
        registerCrafts();
    }
    
    private void registerCrafts() {
        if (!checkEnabled()) return;
        if (config == null) {
            plugin.getLogger().warning("Custom crafts config not loaded!");
            return;
        }
        
        if (config.getBoolean("bell.enabled", true)) {
            registerBellCraft();
        }
        
        if (config.getBoolean("red-mushroom-block.enabled", true)) {
            registerRedMushroomBlockCraft();
        }
        
        if (config.getBoolean("brown-mushroom-block.enabled", true)) {
            registerBrownMushroomBlockCraft();
        }
        
        if (config.getBoolean("cobweb.enabled", true)) {
            registerCobwebCraft();
        }
        
        if (config.getBoolean("scaffolding.enabled", true)) {
            registerScaffoldingCraft();
        }
    }
    
    /**
     * Колокол: камень + палка + камень / золото x3 / самородок + золото + самородок
     */
    private void registerBellCraft() {
        ItemStack result = new ItemStack(Material.BELL);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "bell"), result);
        
        recipe.shape("STS", "GGG", "IGI");
        recipe.setIngredient('S', Material.STONE);
        recipe.setIngredient('T', Material.STICK);
        recipe.setIngredient('G', Material.GOLD_INGOT);
        recipe.setIngredient('I', Material.GOLD_NUGGET);
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
        }
    }
    
    /**
     * Красный гриб блок: 4 красных гриба
     */
    private void registerRedMushroomBlockCraft() {
        ItemStack result = new ItemStack(Material.RED_MUSHROOM_BLOCK);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "red_mushroom_block"), result);
        
        recipe.shape("MM ", "  ", "MM ");
        recipe.setIngredient('M', Material.RED_MUSHROOM);
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
        }
    }
    
    /**
     * Коричневый гриб блок: 4 коричневых гриба
     */
    private void registerBrownMushroomBlockCraft() {
        ItemStack result = new ItemStack(Material.BROWN_MUSHROOM_BLOCK);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "brown_mushroom_block"), result);
        
        recipe.shape("MM ", "  ", "MM ");
        recipe.setIngredient('M', Material.BROWN_MUSHROOM);
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
        }
    }
    
    /**
     * Паутина: 5 ниток
     */
    private void registerCobwebCraft() {
        ItemStack result = new ItemStack(Material.COBWEB);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "cobweb"), result);
        
        recipe.shape("S S", " S ", "S S");
        recipe.setIngredient('S', Material.STRING);
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
        }
    }
    
    /**
     * Подмостки x4: 6 палок + нить
     */
    private void registerScaffoldingCraft() {
        if (config == null) return;
        int amount = config.getInt("scaffolding.amount", 4);
        ItemStack result = new ItemStack(Material.SCAFFOLDING, amount);
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "scaffolding"), result);
        
        recipe.shape("TNT", "T T", "T T");
        recipe.setIngredient('T', Material.STICK);
        recipe.setIngredient('N', Material.STRING);
        
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
        }
    }
}
