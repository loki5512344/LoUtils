package xyz.lokili.loutils.listeners.crafts;

import dev.lolib.utils.Colors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Маркер и данные крафтовых элитр (PDC): перезарядка, накопленное время полёта.
 */
public final class CustomElytraHelper {

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

    /** 1 — базовый крафт, 2 — улучшение (wind charge + алмазные блоки). */
    public static NamespacedKey keyElytraTier(Plugin plugin) {
        return new NamespacedKey(plugin, "elytra_tier");
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

    /**
     * Убирает все зачарования, кроме «Починки» I. Уровень починки принудительно 1.
     */
    public static void stripForbiddenEnchants(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasEnchants()) return;
        boolean hadMending = meta.hasEnchant(Enchantment.MENDING);
        Set<Enchantment> keys = new HashSet<>(meta.getEnchants().keySet());
        for (Enchantment e : keys) {
            if (e != Enchantment.MENDING) {
                meta.removeEnchant(e);
            }
        }
        if (hadMending) {
            meta.removeEnchant(Enchantment.MENDING);
            meta.addEnchant(Enchantment.MENDING, 1, true);
        }
        stack.setItemMeta(meta);
    }

    public static boolean hasForbiddenEnchants(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null || !meta.hasEnchants()) return false;
        for (Enchantment e : meta.getEnchants().keySet()) {
            if (e != Enchantment.MENDING) return true;
            if (meta.getEnchantLevel(Enchantment.MENDING) != 1) return true;
        }
        return false;
    }

    public static int getElytraTier(Plugin plugin, ItemStack stack) {
        if (!isCustomElytra(plugin, stack)) return 0;
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return 1;
        Byte t = meta.getPersistentDataContainer().get(keyElytraTier(plugin), PersistentDataType.BYTE);
        if (t == null) return 1;
        return Math.max(1, Math.min(2, t.intValue()));
    }

    public static void setElytraTier(Plugin plugin, ItemStack stack, int tier) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        int t = Math.max(1, Math.min(2, tier));
        meta.getPersistentDataContainer().set(keyElytraTier(plugin), PersistentDataType.BYTE, (byte) t);
        stack.setItemMeta(meta);
    }

    public static int getEffectiveFlightLimitTicks(Plugin plugin, ItemStack stack, FileConfiguration cfg) {
        if (cfg == null) return 6 * 60 * 20;
        int tier = getElytraTier(plugin, stack);
        if (tier >= 2 && cfg.getBoolean("elytra.tier2.enabled", true)) {
            int v = cfg.getInt("elytra.tier2.flight-limit-ticks", 7200);
            return v < 1 ? 7200 : v;
        }
        int v = cfg.getInt("elytra.flight-limit-ticks", 6 * 60 * 20);
        return v < 1 ? 6 * 60 * 20 : v;
    }

    public static int getEffectiveRechargeMs(Plugin plugin, ItemStack stack, FileConfiguration cfg) {
        if (cfg == null) return 180_000;
        int tier = getElytraTier(plugin, stack);
        if (tier >= 2 && cfg.getBoolean("elytra.tier2.enabled", true)) {
            int sec = cfg.getInt("elytra.tier2.recharge-seconds", 180);
            return (sec < 1 ? 180 : sec) * 1000;
        }
        int sec = cfg.getInt("elytra.recharge-seconds", 180);
        return (sec < 1 ? 180 : sec) * 1000;
    }

    public static void markAndInit(Plugin plugin, ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return;
        meta.getPersistentDataContainer().set(keyMarker(plugin), PersistentDataType.BYTE, (byte) 1);
        meta.getPersistentDataContainer().set(keyElytraTier(plugin), PersistentDataType.BYTE, (byte) 1);
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
        int limitTicks = getEffectiveFlightLimitTicks(plugin, stack, cfg);
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
        if (!pdc.has(keyElytraTier(plugin), PersistentDataType.BYTE)) {
            pdc.set(keyElytraTier(plugin), PersistentDataType.BYTE, (byte) 1);
        }

        int tier = getElytraTier(plugin, stack);
        int maxDamage = tier >= 2 && cfg.getBoolean("elytra.tier2.enabled", true)
                ? cfg.getInt("elytra.tier2.max-damage", 550)
                : cfg.getInt("elytra.max-damage", 500);
        if (maxDamage < 1) maxDamage = tier >= 2 ? 550 : 500;
        if (meta instanceof Damageable damageable) {
            damageable.setMaxDamage(maxDamage);
        }

        String nameFormat = tier >= 2 && cfg.getBoolean("elytra.tier2.enabled", true)
                ? cfg.getString("elytra.tier2.name-format", "&bКрылья фантома &7[II]")
                : cfg.getString("elytra.name-format", "&cКрылья фантома");
        meta.displayName(Colors.parse(nameFormat));

        stack.setItemMeta(meta);
        stripForbiddenEnchants(stack);
        applyElytraLore(plugin, stack, cfg);
        return true;
    }

    /**
     * Прокачка tier 1 → tier 2: сохраняет полёт/перезарядку из PDC, обновляет прочность и имя.
     */
    public static ItemStack createTier2FromTier1(Plugin plugin, ItemStack tier1, FileConfiguration cfg) {
        if (tier1 == null || tier1.getType() != Material.ELYTRA || cfg == null
                || !cfg.getBoolean("elytra.enabled", true) || !cfg.getBoolean("elytra.tier2.enabled", true)) {
            return null;
        }
        if (!isCustomElytra(plugin, tier1) || getElytraTier(plugin, tier1) != 1) {
            return null;
        }
        ItemStack out = tier1.clone();
        ItemMeta meta = out.getItemMeta();
        if (meta == null) return null;

        int maxDamage = cfg.getInt("elytra.tier2.max-damage", 550);
        if (maxDamage < 1) maxDamage = 550;
        if (meta instanceof Damageable damageable) {
            int dmg = damageable.getDamage();
            damageable.setMaxDamage(maxDamage);
            damageable.setDamage(Math.min(dmg, maxDamage));
        }

        meta.getPersistentDataContainer().set(keyElytraTier(plugin), PersistentDataType.BYTE, (byte) 2);
        String nameFormat = cfg.getString("elytra.tier2.name-format", "&bКрылья фантома &7[II]");
        meta.displayName(Colors.parse(nameFormat));
        out.setItemMeta(meta);
        stripForbiddenEnchants(out);
        applyElytraLore(plugin, out, cfg);
        return out;
    }
}
