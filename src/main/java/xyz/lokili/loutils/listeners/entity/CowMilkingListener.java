package xyz.lokili.loutils.listeners.entity;

import dev.lolib.utils.Colors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

public class CowMilkingListener extends BaseListener {

    private final NamespacedKey cooldownKey;
    private final NamespacedKey lastFedKey;

    public CowMilkingListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.COW_MILKING, ConfigConstants.COW_MILKING_CONFIG);
        this.cooldownKey = new NamespacedKey(plugin, "milk_cooldown");
        this.lastFedKey = new NamespacedKey(plugin, "last_fed");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onCowInteract(PlayerInteractEntityEvent event) {
        if (!checkEnabled()) return;
        FileConfiguration c = moduleConfig();
        if (c == null) return;
        if (!(event.getRightClicked() instanceof Cow cow)) return;
        if (cow.getType() != EntityType.COW) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getType() == Material.WHEAT && c.getBoolean("wheat-speedup", true)) {
            handleWheatFeeding(cow, player, c);
            return;
        }

        if (item.getType() == Material.BUCKET) {
            handleMilking(cow, player, event, c);
        }
    }

    private void handleWheatFeeding(Cow cow, Player player, FileConfiguration c) {
        long currentTime = System.currentTimeMillis();
        Long cooldownEnd = cow.getPersistentDataContainer().get(cooldownKey, PersistentDataType.LONG);

        if (cooldownEnd == null || currentTime >= cooldownEnd) {
            return;
        }

        int speedupMinutes = c.getInt("wheat-speedup-minutes", 2);
        long speedupMillis = speedupMinutes * 60 * 1000L;
        long newCooldown = Math.max(currentTime, cooldownEnd - speedupMillis);

        cow.getPersistentDataContainer().set(cooldownKey, PersistentDataType.LONG, newCooldown);
        cow.getPersistentDataContainer().set(lastFedKey, PersistentDataType.LONG, currentTime);

        cow.getWorld().spawnParticle(Particle.HEART, cow.getEyeLocation(), 5, 0.3, 0.3, 0.3, 0);
        cow.getWorld().playSound(cow.getLocation(), Sound.ENTITY_COW_AMBIENT, 1.0f, 1.2f);

        long remainingMinutes = (newCooldown - currentTime) / 60000;
        player.sendActionBar(Colors.parse(
            c.getString("messages.fed")
                .replace("%minutes%", String.valueOf(remainingMinutes))
        ));
    }

    private void handleMilking(Cow cow, Player player, PlayerInteractEntityEvent event, FileConfiguration c) {
        long currentTime = System.currentTimeMillis();
        Long cooldownEnd = cow.getPersistentDataContainer().get(cooldownKey, PersistentDataType.LONG);

        if (cooldownEnd != null && currentTime < cooldownEnd) {
            event.setCancelled(true);

            long remainingMinutes = (cooldownEnd - currentTime) / 60000;
            player.sendActionBar(Colors.parse(
                c.getString("messages.on-cooldown")
                    .replace("%minutes%", String.valueOf(remainingMinutes + 1))
            ));

            if (c.getBoolean("visual-indicator", true)) {
                cow.getWorld().spawnParticle(Particle.SMOKE,
                    cow.getEyeLocation(), 3, 0.2, 0.2, 0.2, 0);
            }
            return;
        }

        int cooldownMinutes = c.getInt("cooldown-minutes", 10);
        long newCooldownEnd = currentTime + (cooldownMinutes * 60 * 1000L);
        cow.getPersistentDataContainer().set(cooldownKey, PersistentDataType.LONG, newCooldownEnd);

        player.sendActionBar(Colors.parse(c.getString("messages.milked")));
    }

    public void startParticleTask() {
        FileConfiguration initial = moduleConfig();
        if (initial == null) {
            plugin.loLogger().warn("Config for cow-milking is null, skipping particle task");
            return;
        }

        if (!initial.getBoolean("visual-indicator", true)) return;
        if (!initial.getBoolean("enabled", true)) return;

        int radius = 32;

        dev.lolib.scheduler.Scheduler.get(plugin).runTimer(() -> {
            FileConfiguration c = moduleConfig();
            if (c == null || !c.getBoolean("enabled", true)) return;
            if (!c.getBoolean("visual-indicator", true)) return;

            String particleType = c.getString("particle-effect", "HEART");

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                dev.lolib.scheduler.Scheduler.get(plugin).runAtEntity(player, () -> {
                    player.getWorld().getNearbyEntities(player.getLocation(), radius, radius, radius).forEach(entity -> {
                        if (!(entity instanceof Cow cow)) return;
                        if (cow.getType() != EntityType.COW) return;

                        Long cooldownEnd = cow.getPersistentDataContainer().get(cooldownKey, PersistentDataType.LONG);
                        if (cooldownEnd == null || System.currentTimeMillis() >= cooldownEnd) {
                            try {
                                Particle particle = Particle.valueOf(particleType);
                                cow.getWorld().spawnParticle(particle,
                                    cow.getEyeLocation().add(0, 0.5, 0),
                                    1, 0.2, 0.2, 0.2, 0);
                            } catch (IllegalArgumentException ignored) {
                            }
                        }
                    });
                });
            }
        }, 100L, 60L);
    }
}
