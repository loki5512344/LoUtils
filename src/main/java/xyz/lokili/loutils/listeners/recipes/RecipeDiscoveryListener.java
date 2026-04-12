package xyz.lokili.loutils.listeners.recipes;

import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import xyz.lokili.loutils.LoUtils;

import java.util.ArrayList;
import java.util.List;

/**
 * RecipeDiscoveryListener — при входе может открыть кастомные рецепты в книге.
 * <p>Включение: {@code config.yml → recipes.discover-custom-on-join} (по умолчанию false).
 */
public class RecipeDiscoveryListener implements Listener {
    
    private final LoUtils plugin;
    private final List<NamespacedKey> customRecipes;
    
    public RecipeDiscoveryListener(LoUtils plugin) {
        this.plugin = plugin;
        this.customRecipes = new ArrayList<>();
        
        // Регистрируем все кастомные рецепты
        registerCustomRecipes();
    }
    
    private void registerCustomRecipes() {
        // Custom Crafts
        customRecipes.add(new NamespacedKey(plugin, "bell"));
        customRecipes.add(new NamespacedKey(plugin, "red_mushroom_block"));
        customRecipes.add(new NamespacedKey(plugin, "brown_mushroom_block"));
        customRecipes.add(new NamespacedKey(plugin, "cobweb"));
        customRecipes.add(new NamespacedKey(plugin, "scaffolding"));
        customRecipes.add(new NamespacedKey(plugin, "custom_quartz"));
        customRecipes.add(new NamespacedKey(plugin, "elytra_craft"));
        customRecipes.add(new NamespacedKey(plugin, "elytra_upgrade_t2"));
        customRecipes.add(new NamespacedKey(plugin, "echo_pickaxe"));
        
        // Light Block
        customRecipes.add(new NamespacedKey(plugin, "light_block"));
        
        // Debug Stick
        customRecipes.add(new NamespacedKey(plugin, "debug_stick"));
        
        // Invisible Frame
        customRecipes.add(new NamespacedKey(plugin, "invisible_frame"));
        
        // Firework Level 4
        customRecipes.add(new NamespacedKey(plugin, "firework_level_4"));
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("recipes.discover-custom-on-join", false)) {
            return;
        }
        Player player = event.getPlayer();
        dev.lolib.scheduler.Scheduler.get(plugin).runLaterAtEntity(player, () -> {
            discoverRecipes(player);
        }, 20L);
    }
    
    private void discoverRecipes(Player player) {
        if (!plugin.getConfig().getBoolean("recipes.discover-custom-on-join", false)) {
            return;
        }
        List<NamespacedKey> toDiscover = new ArrayList<>();
        
        for (NamespacedKey key : customRecipes) {
            // Проверяем, существует ли рецепт на сервере
            if (Bukkit.getRecipe(key) != null) {
                // Проверяем, не открыт ли уже рецепт
                if (!player.hasDiscoveredRecipe(key)) {
                    toDiscover.add(key);
                }
            }
        }
        
        if (!toDiscover.isEmpty()) {
            player.discoverRecipes(toDiscover);
            plugin.loLogger().debug("Discovered " + toDiscover.size() + " recipes for " + player.getName());
        }
    }
}
