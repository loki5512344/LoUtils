package xyz.lokili.loutils.listeners.items;

import dev.lolib.utils.Colors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Палка проверки инвентаря: удар по игроку сканирует его инвентарь на запрещённые материалы из конфига,
 * результат пишется атакующему в чат.
 */
public class InventoryCheckStickListener extends BaseListener {

    private final NamespacedKey stickKey;

    public InventoryCheckStickListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.INVENTORY_CHECK_STICK, ConfigConstants.INVENTORY_CHECK_STICK_CONFIG);
        this.stickKey = new NamespacedKey(plugin, "inventory_check_stick");
        registerRecipe();
    }

    private void registerRecipe() {
        FileConfiguration c = moduleConfig();
        if (c == null || !c.getBoolean("crafting-enabled", true)) {
            return;
        }

        ItemStack result = buildStickItem();
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "inventory_check_stick"), result);
        recipe.shape("NNN", "NSN", "NNN");
        recipe.setIngredient('N', Material.NETHERITE_SCRAP);
        recipe.setIngredient('S', Material.STICK);

        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
            // уже зарегистрирован
        }
    }

    private boolean isOurStick(ItemStack item) {
        if (item == null || item.getType() != Material.STICK || !item.hasItemMeta()) {
            return false;
        }
        ItemMeta meta = item.getItemMeta();
        return meta.getPersistentDataContainer().has(stickKey, org.bukkit.persistence.PersistentDataType.BYTE);
    }

    private ItemStack buildStickItem() {
        org.bukkit.configuration.file.YamlConfiguration empty = new org.bukkit.configuration.file.YamlConfiguration();
        FileConfiguration c = moduleConfig() != null ? moduleConfig() : empty;
        return plugin.getContainer().getItemFactory().createInventoryCheckStick(c);
    }

    private Set<Material> loadForbiddenMaterials() {
        FileConfiguration c = moduleConfig();
        if (c == null) {
            return Collections.emptySet();
        }
        List<String> raw = c.getStringList("forbidden-materials");
        Set<Material> out = new HashSet<>();
        for (String line : raw) {
            if (line == null || line.isBlank()) {
                continue;
            }
            Material m = Material.matchMaterial(line.trim().toUpperCase());
            if (m != null && !m.isAir()) {
                out.add(m);
            } else {
                plugin.getLogger().warning("[inventory-check-stick] Неизвестный материал в конфиге: " + line);
            }
        }
        return out;
    }

    /**
     * Русское имя для чата: секция {@code material-names} в inventory-check-stick.yml.
     */
    private String displayNameRu(Material m, FileConfiguration c) {
        if (c != null) {
            String v = c.getString("material-names." + m.name());
            if (v != null && !v.isBlank()) {
                return v;
            }
        }
        return prettyMaterialFallback(m);
    }

    private static String prettyMaterialFallback(Material m) {
        String[] p = m.name().toLowerCase().split("_");
        StringBuilder sb = new StringBuilder();
        for (String s : p) {
            if (sb.length() > 0) {
                sb.append(' ');
            }
            sb.append(Character.toUpperCase(s.charAt(0))).append(s.substring(1));
        }
        return sb.toString();
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) {
            return;
        }
        if (!(event.getEntity() instanceof Player target)) {
            return;
        }
        if (!checkEnabled()) {
            return;
        }

        FileConfiguration c = moduleConfig();
        if (c == null) {
            return;
        }

        ItemStack hand = attacker.getInventory().getItemInMainHand();
        if (!isOurStick(hand)) {
            return;
        }

        if (c.getBoolean("require-permission", true)
                && !attacker.hasPermission(ConfigConstants.Permissions.INVENTORY_CHECK_STICK)) {
            attacker.sendMessage(Colors.parse(c.getString("messages.no-permission", "&cНет прав на использование палки.")));
            event.setCancelled(true);
            return;
        }

        int cooldownSec = c.getInt("cooldown-seconds", 0);
        if (cooldownSec > 0) {
            long now = System.currentTimeMillis();
            UUID id = attacker.getUniqueId();
            Long last = cooldowns.get(id);
            if (last != null && now - last < cooldownSec * 1000L) {
                event.setCancelled(true);
                return;
            }
            cooldowns.put(id, now);
        }

        if (c.getBoolean("cancel-damage", true)) {
            event.setCancelled(true);
        }

        Set<Material> forbidden = loadForbiddenMaterials();
        Map<Material, Integer> found = scanInventory(target, forbidden);

        String targetName = target.getName();
        if (found.isEmpty()) {
            String msg = c.getString("messages.none-found", "&aУ &e{target} &aнет предметов из списка.");
            attacker.sendMessage(Colors.parse(msg.replace("{target}", targetName)));
            return;
        }

        String title = c.getString("messages.found-title", "&cСписок запреток");
        String sep = c.getString("messages.found-separator", "&7--------------------------------------------------");
        String lineFmt = c.getString("messages.found-line", "&f{name} &7| &e{amount}");
        String targetLineTpl = c.getString("messages.found-target-line", "&7Игрок: &e{target}");

        attacker.sendMessage(Colors.parse(title));
        attacker.sendMessage(Colors.parse(sep));
        if (targetLineTpl != null && !targetLineTpl.isBlank() && !"none".equalsIgnoreCase(targetLineTpl.trim())) {
            attacker.sendMessage(Colors.parse(targetLineTpl.replace("{target}", targetName)));
        }

        List<Map.Entry<Material, Integer>> rows = new ArrayList<>(found.entrySet());
        rows.sort(Comparator.comparing(e -> displayNameRu(e.getKey(), c)));

        for (Map.Entry<Material, Integer> e : rows) {
            String name = displayNameRu(e.getKey(), c);
            String line = lineFmt
                    .replace("{name}", name)
                    .replace("{amount}", String.valueOf(e.getValue()));
            attacker.sendMessage(Colors.parse(line));
        }
    }

    private final Map<UUID, Long> cooldowns = new HashMap<>();

    private static Map<Material, Integer> scanInventory(Player target, Set<Material> forbidden) {
        Map<Material, Integer> found = new HashMap<>();
        if (forbidden.isEmpty()) {
            return found;
        }

        List<ItemStack> stacks = new ArrayList<>();
        Collections.addAll(stacks, target.getInventory().getContents());
        Collections.addAll(stacks, target.getInventory().getArmorContents());
        stacks.add(target.getInventory().getItemInOffHand());

        for (ItemStack stack : stacks) {
            if (stack == null || stack.getType().isAir()) {
                continue;
            }
            Material type = stack.getType();
            if (forbidden.contains(type)) {
                found.merge(type, stack.getAmount(), Integer::sum);
            }
        }

        return found;
    }
}
