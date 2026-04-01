package xyz.lokili.loutils.listeners.crafts;

import dev.lolib.scheduler.Scheduler;
import dev.lolib.utils.Colors;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.entity.Pose;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityToggleGlideEvent;
import org.bukkit.event.inventory.PrepareAnvilEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Крафтовые элитры: лимит полёта, перезарядка, запрет полёта в дождь, блок зачарований и снятие чар.
 * Тик выполняется в потоке сущности (Folia), таймер полёта накапливается в PDC и не сбрасывается при приземлении.
 */
public class ElytraMechanicsListener extends BaseListener {

    private static final long MESSAGE_COOLDOWN_MS = 2500L;

    private final Map<UUID, Long> lastMessageAt = new ConcurrentHashMap<>();
    private long elytraGlobalTick;

    public ElytraMechanicsListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.CUSTOM_CRAFTS, ConfigConstants.CUSTOM_CRAFTS_CONFIG);
        startTickTask();
    }

    private void startTickTask() {
        Scheduler.get(plugin).runTimer(() -> {
            if (!checkEnabled() || moduleConfig() == null) return;
            if (!moduleConfig().getBoolean("elytra.enabled", true)) return;

            elytraGlobalTick++;
            final FileConfiguration cfg = moduleConfig();
            int loreInterval = Math.max(1, cfg.getInt("elytra.lore-update-interval-ticks", 10));
            final boolean updateLore = (elytraGlobalTick % loreInterval) == 0;

            int fl = cfg.getInt("elytra.flight-limit-ticks", 3 * 60 * 20);
            final int flightLimitTicks = fl < 1 ? 3 * 60 * 20 : fl;
            int rms = cfg.getInt("elytra.recharge-seconds", 180) * 1000;
            final int rechargeMs = rms < 1000 ? 180_000 : rms;

            for (Player player : Bukkit.getOnlinePlayers()) {
                Scheduler.get(plugin).runAtEntity(player, () -> tickElytra(player, cfg, flightLimitTicks, rechargeMs, updateLore));
            }
        }, 0L, 1L);
    }

    private void tickElytra(Player player, FileConfiguration cfg, int flightLimitTicks, int rechargeMs, boolean updateLore) {
        ItemStack chest = player.getInventory().getChestplate();
        if (!CustomElytraHelper.isCustomElytra(plugin, chest)) return;

        CustomElytraHelper.stripAllEnchants(chest);
        CustomElytraHelper.clearRechargeIfDone(plugin, chest);

        boolean gliding = isElytraFlight(player);

        if (gliding) {
            if (isRainingOn(player)) {
                player.setGliding(false);
                sendThrottled(player, "elytra.messages.rain");
                if (updateLore) CustomElytraHelper.applyElytraLore(plugin, chest, cfg);
                player.getInventory().setChestplate(chest);
                return;
            }
            if (CustomElytraHelper.isRecharging(plugin, chest)) {
                player.setGliding(false);
                sendThrottled(player, "elytra.messages.recharge");
                if (updateLore) CustomElytraHelper.applyElytraLore(plugin, chest, cfg);
                player.getInventory().setChestplate(chest);
                return;
            }

            long ticks = CustomElytraHelper.getFlightTicks(plugin, chest) + 1L;
            if (ticks >= flightLimitTicks) {
                CustomElytraHelper.setFlightTicks(plugin, chest, 0L);
                CustomElytraHelper.setRechargeUntil(plugin, chest, System.currentTimeMillis() + rechargeMs);
                player.setGliding(false);
                sendThrottled(player, "elytra.messages.overheated");
            } else {
                CustomElytraHelper.setFlightTicks(plugin, chest, ticks);
            }
        }

        if (updateLore) {
            CustomElytraHelper.applyElytraLore(plugin, chest, cfg);
        }
        player.getInventory().setChestplate(chest);
    }

    /**
     * isGliding() иногда кратковременно false в воздухе; Pose.FALL_FLYING дополняет проверку.
     */
    private boolean isElytraFlight(Player player) {
        return player.isGliding() || player.getPose() == Pose.FALL_FLYING;
    }

    private boolean isRainingOn(Player player) {
        World w = player.getWorld();
        if (!w.hasStorm()) return false;
        if (w.getEnvironment() != World.Environment.NORMAL) return false;
        if (player.getLocation().getBlock().getLightFromSky() < 10) return false;
        String biomeKey = player.getLocation().getBlock().getBiome().getKey().getKey();
        if (biomeKey.contains("desert") || biomeKey.contains("badlands") || biomeKey.contains("nether") || biomeKey.contains("end")) {
            return false;
        }
        return true;
    }

    private void sendThrottled(Player player, String configPath) {
        if (moduleConfig() == null) return;
        String raw = moduleConfig().getString(configPath);
        if (raw == null || raw.isEmpty()) return;
        UUID id = player.getUniqueId();
        long now = System.currentTimeMillis();
        Long last = lastMessageAt.get(id);
        if (last != null && now - last < MESSAGE_COOLDOWN_MS) return;
        lastMessageAt.put(id, now);
        player.sendActionBar(Colors.parse(raw));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityToggleGlide(EntityToggleGlideEvent event) {
        if (!checkEnabled() || moduleConfig() == null) return;
        if (!moduleConfig().getBoolean("elytra.enabled", true)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!event.isGliding()) return;

        ItemStack chest = player.getInventory().getChestplate();
        if (!CustomElytraHelper.isCustomElytra(plugin, chest)) return;

        if (CustomElytraHelper.isRecharging(plugin, chest)) {
            event.setCancelled(true);
            sendThrottled(player, "elytra.messages.recharge");
            return;
        }

        if (isRainingOn(player)) {
            event.setCancelled(true);
            sendThrottled(player, "elytra.messages.rain");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPrepareAnvil(PrepareAnvilEvent event) {
        if (!checkEnabled() || moduleConfig() == null) return;
        if (!moduleConfig().getBoolean("elytra.enabled", true)) return;

        ItemStack first = event.getInventory().getItem(0);
        ItemStack second = event.getInventory().getItem(1);
        boolean firstCustom = CustomElytraHelper.isCustomElytra(plugin, first);
        boolean secondCustom = CustomElytraHelper.isCustomElytra(plugin, second);
        if (!firstCustom && !secondCustom) return;

        if (second != null && second.getType() == Material.ENCHANTED_BOOK) {
            event.setResult(null);
            return;
        }

        if (firstCustom && second != null && second.getType() == Material.ELYTRA) {
            event.setResult(null);
            return;
        }
        if (secondCustom && first != null && first.getType() == Material.ELYTRA) {
            event.setResult(null);
            return;
        }

        ItemStack result = event.getResult();
        if (result != null && result.getType() == Material.ELYTRA && CustomElytraHelper.isCustomElytra(plugin, first)) {
            ItemMeta rm = result.getItemMeta();
            if (rm != null && rm.hasEnchants()) {
                event.setResult(null);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEnchantItem(EnchantItemEvent event) {
        if (!checkEnabled() || moduleConfig() == null) return;
        if (!moduleConfig().getBoolean("elytra.enabled", true)) return;
        ItemStack item = event.getItem();
        if (CustomElytraHelper.isCustomElytra(plugin, item)) {
            event.setCancelled(true);
        }
    }
}
