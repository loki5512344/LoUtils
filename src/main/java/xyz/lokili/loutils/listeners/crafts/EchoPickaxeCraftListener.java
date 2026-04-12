package xyz.lokili.loutils.listeners.crafts;

import dev.lolib.utils.Colors;
import dev.lolib.scheduler.Scheduler;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.PrepareItemCraftEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.recipe.CraftingBookCategory;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.List;

/**
 * Крафт: 3 осколка эха / палка / палка → деревянная кирка «Эхо-кирка» (500 прочн., Эффективность 8).
 */
public class EchoPickaxeCraftListener extends BaseListener {

    public EchoPickaxeCraftListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.CUSTOM_CRAFTS, ConfigConstants.CUSTOM_CRAFTS_CONFIG);
        Scheduler.get(plugin).runLater(this::registerEchoPickaxeRecipe, 1L);
    }

    public void registerEchoPickaxeRecipe() {
        if (!checkEnabled()) return;
        if (moduleConfig() == null) return;
        if (!moduleConfig().getBoolean("echo-pickaxe.enabled", true)) return;

        NamespacedKey key = new NamespacedKey(plugin, "echo_pickaxe");
        Bukkit.removeRecipe(key);

        ShapedRecipe recipe = new ShapedRecipe(key, createEchoPickaxe());
        recipe.shape("EEE", " S ", " S ");
        recipe.setIngredient('E', Material.ECHO_SHARD);
        recipe.setIngredient('S', Material.STICK);
        recipe.setCategory(CraftingBookCategory.EQUIPMENT);

        if (!Bukkit.addRecipe(recipe)) {
            plugin.getLogger().warning("Не удалось зарегистрировать рецепт echo_pickaxe.");
        }
    }

    @EventHandler
    public void onPrepareItemCraft(PrepareItemCraftEvent event) {
        if (!checkEnabled() || moduleConfig() == null) return;
        if (!moduleConfig().getBoolean("echo-pickaxe.enabled", true)) return;

        var inv = event.getInventory();
        if (inv == null) return;

        ItemStack result = inv.getResult();
        if (result == null || result.getType() != Material.WOODEN_PICKAXE) return;

        ItemStack[] matrix = inv.getMatrix();
        if (!isEchoPickaxeCraft(matrix)) return;

        inv.setResult(createEchoPickaxe());
    }

    private boolean isEchoPickaxeCraft(ItemStack[] matrix) {
        if (matrix == null || matrix.length < 9) return false;
        return matches(matrix[0], Material.ECHO_SHARD)
                && matches(matrix[1], Material.ECHO_SHARD)
                && matches(matrix[2], Material.ECHO_SHARD)
                && empty(matrix[3])
                && matches(matrix[4], Material.STICK)
                && empty(matrix[5])
                && empty(matrix[6])
                && matches(matrix[7], Material.STICK)
                && empty(matrix[8]);
    }

    private boolean matches(ItemStack stack, Material material) {
        return stack != null && stack.getType() == material;
    }

    private boolean empty(ItemStack stack) {
        return stack == null || stack.getType() == Material.AIR;
    }

    private ItemStack createEchoPickaxe() {
        if (moduleConfig() == null) return new ItemStack(Material.WOODEN_PICKAXE);

        int maxDamage = moduleConfig().getInt("echo-pickaxe.max-damage", 500);
        if (maxDamage < 1) maxDamage = 500;
        int eff = moduleConfig().getInt("echo-pickaxe.efficiency-level", 8);
        if (eff < 1) eff = 8;

        ItemStack pick = new ItemStack(Material.WOODEN_PICKAXE);
        ItemMeta meta = pick.getItemMeta();
        if (meta == null) return pick;

        if (meta instanceof Damageable damageable) {
            damageable.setMaxDamage(maxDamage);
            damageable.setDamage(0);
        }

        String nameFormat = moduleConfig().getString("echo-pickaxe.name-format", "&bЭхо-кирка");
        meta.displayName(Colors.parse(nameFormat));

        List<String> loreList = moduleConfig().getStringList("echo-pickaxe.lore");
        if (!loreList.isEmpty()) {
            meta.lore(loreList.stream().map(ColorUtil::colorize).toList());
        }

        pick.setItemMeta(meta);
        EchoPickaxeHelper.mark(plugin, pick);
        EchoPickaxeHelper.normalizeEnchants(pick, eff);
        return pick;
    }
}
