package xyz.lokili.loutils.listeners.crafts;

import dev.lolib.scheduler.Scheduler;
import dev.lolib.utils.Colors;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Эхо-кирка: только разрешённые «каменные» блоки, руды нельзя; без посторонних чар.
 */
public class EchoPickaxeMechanicsListener extends BaseListener {

    public EchoPickaxeMechanicsListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.CUSTOM_CRAFTS, ConfigConstants.CUSTOM_CRAFTS_CONFIG);
        startEnchantMaintenanceTask();
    }

    private void startEnchantMaintenanceTask() {
        Scheduler.get(plugin).runTimer(() -> {
            if (!checkEnabled() || moduleConfig() == null) return;
            if (!moduleConfig().getBoolean("echo-pickaxe.enabled", true)) return;
            int eff = moduleConfig().getInt("echo-pickaxe.efficiency-level", 8);
            if (eff < 1) eff = 8;

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                for (ItemStack stack : player.getInventory().getContents()) {
                    if (stack == null || !EchoPickaxeHelper.isEchoPickaxe(plugin, stack)) continue;
                    EchoPickaxeHelper.normalizeEnchants(stack, eff);
                }
            }
        }, 40L, 40L);
    }

    private int efficiencyLevel() {
        FileConfiguration c = moduleConfig();
        if (c == null) return 8;
        int eff = c.getInt("echo-pickaxe.efficiency-level", 8);
        return eff < 1 ? 8 : eff;
    }

    private Set<Material> allowedMaterials() {
        Set<Material> set = new HashSet<>();
        FileConfiguration c = moduleConfig();
        if (c != null) {
            for (String line : c.getStringList("echo-pickaxe.allowed-materials")) {
                if (line == null || line.isBlank()) continue;
                try {
                    set.add(Material.valueOf(line.trim().toUpperCase(Locale.ROOT)));
                } catch (IllegalArgumentException ignored) {
                }
            }
        }
        if (!set.isEmpty()) {
            return set;
        }
        return defaultAllowedStone();
    }

    private static Set<Material> defaultAllowedStone() {
        Set<Material> set = new HashSet<>();
        // «Камень и родственные» — не руды
        Material[] m = {
                Material.STONE, Material.DIORITE, Material.ANDESITE, Material.GRANITE,
                Material.COBBLESTONE, Material.DEEPSLATE, Material.COBBLED_DEEPSLATE,
                Material.POLISHED_DIORITE, Material.POLISHED_ANDESITE, Material.POLISHED_GRANITE,
                Material.POLISHED_DEEPSLATE, Material.DEEPSLATE_TILES, Material.DEEPSLATE_BRICKS,
                Material.CRACKED_DEEPSLATE_TILES, Material.CRACKED_DEEPSLATE_BRICKS,
                Material.CHISELED_DEEPSLATE, Material.SMOOTH_BASALT, Material.CALCITE, Material.TUFF,
                Material.STONE_BRICKS, Material.MOSSY_STONE_BRICKS, Material.CRACKED_STONE_BRICKS,
                Material.CHISELED_STONE_BRICKS, Material.DEEPSLATE_TILE_SLAB, Material.DEEPSLATE_BRICK_SLAB,
                Material.SANDSTONE, Material.CHISELED_SANDSTONE, Material.CUT_SANDSTONE, Material.SMOOTH_SANDSTONE,
                Material.RED_SANDSTONE, Material.CHISELED_RED_SANDSTONE, Material.CUT_RED_SANDSTONE, Material.SMOOTH_RED_SANDSTONE,
                Material.NETHERRACK, Material.BASALT, Material.POLISHED_BASALT, Material.SMOOTH_BASALT,
                Material.BLACKSTONE, Material.POLISHED_BLACKSTONE, Material.POLISHED_BLACKSTONE_BRICKS,
                Material.CRACKED_POLISHED_BLACKSTONE_BRICKS, Material.CHISELED_POLISHED_BLACKSTONE,
                Material.END_STONE, Material.END_STONE_BRICKS,
                Material.PRISMARINE, Material.PRISMARINE_BRICKS, Material.DARK_PRISMARINE,
                Material.MUD_BRICKS, Material.PACKED_MUD,
                Material.BRICKS, Material.QUARTZ_BLOCK, Material.CHISELED_QUARTZ_BLOCK,
                Material.PURPUR_BLOCK, Material.PURPUR_PILLAR,
                Material.OBSIDIAN, Material.CRYING_OBSIDIAN,
                Material.AMETHYST_BLOCK, Material.BUDDING_AMETHYST,
                Material.ICE, Material.PACKED_ICE, Material.BLUE_ICE,
                Material.SNOW_BLOCK,
        };
        for (Material x : m) {
            set.add(x);
        }
        return set;
    }

    private static boolean isOreLike(Material type) {
        String n = type.name();
        if (n.contains("ORE")) return true;
        if (type == Material.ANCIENT_DEBRIS) return true;
        if (type == Material.NETHER_GOLD_ORE) return true;
        if (type == Material.GILDED_BLACKSTONE) return true;
        return false;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        if (!checkEnabled() || moduleConfig() == null) return;
        if (!moduleConfig().getBoolean("echo-pickaxe.enabled", true)) return;

        Player player = event.getPlayer();
        ItemStack tool = player.getInventory().getItemInMainHand();
        if (!EchoPickaxeHelper.isEchoPickaxe(plugin, tool)) return;

        EchoPickaxeHelper.normalizeEnchants(tool, efficiencyLevel());

        Block block = event.getBlock();
        Material type = block.getType();

        if (isOreLike(type)) {
            event.setCancelled(true);
            sendWrongBlock(player);
            return;
        }

        if (!allowedMaterials().contains(type)) {
            event.setCancelled(true);
            sendWrongBlock(player);
        }
    }

    private void sendWrongBlock(Player player) {
        if (moduleConfig() == null) return;
        String raw = moduleConfig().getString("echo-pickaxe.messages.wrong-block");
        if (raw != null && !raw.isEmpty()) {
            player.sendActionBar(Colors.parse(raw));
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!checkEnabled() || moduleConfig() == null) return;
        if (!moduleConfig().getBoolean("echo-pickaxe.enabled", true)) return;

        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);
        boolean f = EchoPickaxeHelper.isEchoPickaxe(plugin, first);
        boolean s = EchoPickaxeHelper.isEchoPickaxe(plugin, second);
        if (!f && !s) return;

        if (second != null && second.getType() == Material.ENCHANTED_BOOK) {
            event.setResult(null);
            return;
        }
        if (f && second != null && second.getType() == Material.WOODEN_PICKAXE) {
            event.setResult(null);
            return;
        }
        if (s && first != null && first.getType() == Material.WOODEN_PICKAXE) {
            event.setResult(null);
            return;
        }

        ItemStack result = event.getResult();
        if (result == null) return;

        if (f && result.getType() == Material.WOODEN_PICKAXE) {
            EchoPickaxeHelper.mark(plugin, result);
            EchoPickaxeHelper.normalizeEnchants(result, efficiencyLevel());
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        if (!checkEnabled() || moduleConfig() == null) return;
        if (!moduleConfig().getBoolean("echo-pickaxe.enabled", true)) return;
        if (EchoPickaxeHelper.isEchoPickaxe(plugin, event.getItem())) {
            event.setCancelled(true);
        }
    }
}
