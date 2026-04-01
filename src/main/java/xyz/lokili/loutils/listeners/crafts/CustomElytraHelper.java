package xyz.lokili.loutils.listeners.crafts;

import dev.lolib.utils.Colors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Маркер и данные крафтовых элитр (PDC): перезарядка, накопленное время полёта.
 */
public final class CustomElytraHelper {

    private static final int VANILLA_ELYTRA_MAX_DAMAGE = 432;

    private CustomElytraHelper() {}

    public static NamespacedKey keyMarker(Plugin plugin) {
        return new NamespacedKey(plugin, "custom_crafted_elytra");
    }

    public static NamespacedKey keyRechargeUntil(Plugin plugin) {
        return new NamespacedKey(plugin, "elytra_recharge_until_ms");
    }

    public static NamespacedKey keyFlightTicks(Plugin plugin) {
        return new NamespacedKey(plugin, "elytra_flight_ticks");
    }

    public static boolean isCustomElytra(Plugin plugin, ItemStack stack) {
        if (stack == null || stack.getType() != Material.ELYTRA) return false;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;
        return meta.getPersistentDataContainer().has(keyMarker(plugin), PersistentDataType.BYTE);
    }

    public static long getRechargeUntilMs(Plugin plugin, ItemStack stack) {
        if (!isCustomElytra(plugin, stack)) return 0L;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return 0L;
        Long v = meta.getPersistentDataContainer().get(keyRechargeUntil(plugin), PersistentDataType.LONG);
        return v != null ? v : 0L;
    }

    public static boolean isRecharging(Plugin plugin, ItemStack stack) {
        long until = getRechargeUntilMs(plugin, stack);
        return until > 0L && System.currentTimeMillis() < until;
    }

    public static long getFlightTicks(Plugin plugin, ItemStack stack) {
        if (!isCustomElytra(plugin, stack)) return 0L;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return 0L;
        Long v = meta.getPersistentDataContainer().get(keyFlightTicks(plugin), PersistentDataType.LONG);
        return v != null ? v : 0L;
    }

    public static void setFlightTicks(Plugin plugin, ItemStack stack, long ticks) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(keyFlightTicks(plugin), PersistentDataType.LONG, ticks);
        stack.setItemMeta(meta);
    }

    public static void setRechargeUntil(Plugin plugin, ItemStack stack, long epochMs) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        if (epochMs <= 0L) {
            meta.getPersistentDataContainer().remove(keyRechargeUntil(plugin));
        } else {
            meta.getPersistentDataContainer().set(keyRechargeUntil(plugin), PersistentDataType.LONG, epochMs);
        }
        stack.setItemMeta(meta);
    }

    public static void clearRechargeIfDone(Plugin plugin, ItemStack stack) {
        long until = getRechargeUntilMs(plugin, stack);
        if (until > 0L && System.currentTimeMillis() >= until) {
            setRechargeUntil(plugin, stack, 0L);
            setFlightTicks(plugin, stack, 0L);
        }
    }

    public static void stripAllEnchants(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasEnchants()) return;
        for (var e : meta.getEnchants().keySet()) {
            meta.removeEnchant(e);
        }
        stack.setItemMeta(meta);
    }

    public static void markAndInit(Plugin plugin, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(keyMarker(plugin), PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().remove(keyRechargeUntil(plugin));
        meta.getPersistentDataContainer().remove(keyFlightTicks(plugin));
        stack.setItemMeta(meta);
    }

    /**
     * Обновляет описание: статический лор из конфига + строка времени + полоска + перезарядка.
     */
    public static void applyElytraLore(Plugin plugin, ItemStack stack, FileConfiguration cfg) {
        if (!isCustomElytra(plugin, stack) || cfg == null) return;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;

        long flightTicks = getFlightTicks(plugin, stack);
        int limitTicks = cfg.getInt("elytra.flight-limit-ticks", 3 * 60 * 20);
        if (limitTicks < 1) limitTicks = 3 * 60 * 20;
        long rechargeUntil = getRechargeUntilMs(plugin, stack);
        long now = System.currentTimeMillis();

        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();
        for (String line : cfg.getStringList("elytra.lore")) {
            if (line != null && !line.isBlank()) {
                lore.add(ColorUtil.colorize(line));
            }
        }

        String timeLine = cfg.getString("elytra.lore-time-line", "&7Полёт: &f{used} &7/ &f{limit}")
                .replace("{used}", formatMmSs(flightTicks))
                .replace("{limit}", formatMmSs(limitTicks));
        lore.add(ColorUtil.colorize(timeLine));

        int segments = cfg.getInt("elytra.flight-bar.segments", 18);
        if (segments < 4) segments = 18;
        lore.add(Colors.parse(buildFlightBar(flightTicks, limitTicks, segments, cfg)));

        if (rechargeUntil > now) {
            long sec = (rechargeUntil - now + 999L) / 1000L;
            String rechargeLine = cfg.getString("elytra.lore-recharge-line", "&cПерезарядка: &f{seconds} &7сек")
                    .replace("{seconds}", String.valueOf(sec));
            lore.add(ColorUtil.colorize(rechargeLine));
        }

        meta.lore(lore);
        stack.setItemMeta(meta);
    }

    private static String formatMmSs(long ticks) {
        long totalSec = Math.max(0L, ticks) / 20L;
        long m = totalSec / 60L;
        long s = totalSec % 60L;
        return String.format("%d:%02d", m, s);
    }

    private static String buildFlightBar(long flightTicks, int limitTicks, int segments, FileConfiguration cfg) {
        if (limitTicks < 1) limitTicks = 1;
        int filled = (int) Math.min(segments, (flightTicks * segments) / limitTicks);
        String[] heat = cfg.getStringList("elytra.flight-bar.filled-colors").toArray(String[]::new);
        if (heat.length == 0) {
            heat = new String[]{"&c", "&6", "&e", "&a"};
        }
        String empty = cfg.getString("elytra.flight-bar.empty", "&8|");
        StringBuilder sb = new StringBuilder();
        sb.append(cfg.getString("elytra.flight-bar.prefix", "&8["));
        for (int i = 0; i < segments; i++) {
            if (i < filled) {
                String c = heat[(i * heat.length) / Math.max(1, segments)];
                sb.append(c).append(cfg.getString("elytra.flight-bar.pipe", "|"));
            } else {
                sb.append(empty);
            }
        }
        sb.append(cfg.getString("elytra.flight-bar.suffix", "&8]"));
        return sb.toString();
    }

    /**
     * Обновляет старые элитры: маркер, прочность, имя из конфига, сброс чаров, актуальное описание.
     */
    public static boolean upgradeLegacyElytra(Plugin plugin, ItemStack stack, FileConfiguration cfg) {
        if (stack == null || stack.getType() != Material.ELYTRA || cfg == null || !cfg.getBoolean("elytra.enabled", true)) {
            return false;
        }
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return false;

        var pdc = meta.getPersistentDataContainer();
        if (!pdc.has(keyMarker(plugin), PersistentDataType.BYTE)) {
            pdc.set(keyMarker(plugin), PersistentDataType.BYTE, (byte) 1);
        }
        if (!pdc.has(keyFlightTicks(plugin), PersistentDataType.LONG)) {
            pdc.set(keyFlightTicks(plugin), PersistentDataType.LONG, 0L);
        }

        int maxDamage = cfg.getInt("elytra.max-damage", VANILLA_ELYTRA_MAX_DAMAGE / 2);
        if (maxDamage < 1) maxDamage = VANILLA_ELYTRA_MAX_DAMAGE / 2;
        if (meta instanceof Damageable damageable) {
            damageable.setMaxDamage(maxDamage);
        }

        String nameFormat = cfg.getString("elytra.name-format", "&cКрылья фантома");
        meta.displayName(Colors.parse(nameFormat));

        stack.setItemMeta(meta);
        stripAllEnchants(stack);
        applyElytraLore(plugin, stack, cfg);
        return true;
    }
}
