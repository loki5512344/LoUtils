package xyz.lokili.loutils.listeners.player;

import dev.lolib.utils.Colors;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Particle;
import org.bukkit.attribute.Attribute;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityMountEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerTeleportEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.IConfigManager;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Кандалы (наручники): связь «коварь → скованный», блок ходьбы и атак у скованного,
 * визуал поводка частицами, подтягивание при отходе ковавшего.
 */
public class HandcuffsListener extends BaseListener {

    private final NamespacedKey itemKey;
    /** анти-спам сообщения «нельзя бить» */
    private final Map<UUID, Long> lastCannotAttackHint = new ConcurrentHashMap<>();
    /** скованный → кто сковал */
    private final Map<UUID, UUID> detaineeToDetainer = new ConcurrentHashMap<>();
    /** кто сковал → скованный (один активный на игрока) */
    private final Map<UUID, UUID> detainerToDetainee = new ConcurrentHashMap<>();

    public HandcuffsListener(LoUtils plugin, IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.HANDCUFFS, ConfigConstants.HANDCUFFS_CONFIG);
        this.itemKey = new NamespacedKey(plugin, "handcuffs_item");
        registerRecipe();
    }

    private boolean isHandcuffStack(ItemStack stack) {
        if (stack == null || stack.getType() != Material.LEAD || !stack.hasItemMeta()) {
            return false;
        }
        return stack.getItemMeta().getPersistentDataContainer().has(itemKey, PersistentDataType.BYTE);
    }

    private ItemStack buildHandcuffItem() {
        org.bukkit.configuration.file.YamlConfiguration empty = new org.bukkit.configuration.file.YamlConfiguration();
        return plugin.getContainer().getItemFactory().createHandcuffs(moduleConfig() != null ? moduleConfig() : empty);
    }

    private void registerRecipe() {
        if (moduleConfig() == null || !moduleConfig().getBoolean("crafting-enabled", true)) {
            return;
        }
        ItemStack result = buildHandcuffItem();
        ShapedRecipe recipe = new ShapedRecipe(new NamespacedKey(plugin, "handcuffs"), result);
        recipe.shape("NNN", "NIN", "NNN");
        recipe.setIngredient('N', Material.IRON_NUGGET);
        recipe.setIngredient('I', Material.IRON_INGOT);
        try {
            plugin.getServer().addRecipe(recipe);
        } catch (IllegalStateException ignored) {
        }
    }

    public boolean isDetained(Player p) {
        return detaineeToDetainer.containsKey(p.getUniqueId());
    }

    public UUID getDetainerId(UUID detainee) {
        return detaineeToDetainer.get(detainee);
    }

    private void cuff(Player detainer, Player target) {
        UUID d = detainer.getUniqueId();
        UUID t = target.getUniqueId();

        uncuffByDetainer(d);
        if (detaineeToDetainer.containsKey(t)) {
            detainer.sendMessage(Colors.parse(moduleConfig().getString("messages.already-cuffed", "&cУже в наручниках.")));
            return;
        }

        detainerToDetainee.put(d, t);
        detaineeToDetainer.put(t, d);

        String msg = moduleConfig().getString("messages.cuffed", "&eСковали &f{target}");
        detainer.sendMessage(Colors.parse(msg.replace("{target}", target.getName())));
        target.sendMessage(Colors.parse(moduleConfig().getString("messages.target-notify", "&cВас сковали.")));
    }

    private void uncuffPair(Player detainer, Player target) {
        UUID d = detainer.getUniqueId();
        UUID t = target.getUniqueId();
        detainerToDetainee.remove(d);
        detaineeToDetainer.remove(t);
        detainer.sendMessage(Colors.parse(moduleConfig().getString("messages.uncuffed", "&aСнято.").replace("{target}", target.getName())));
        target.sendMessage(Colors.parse(moduleConfig().getString("messages.uncuff-target-notify", "&aСняли наручники.")));
    }

    private void uncuffByDetainer(UUID detainerId) {
        UUID detaineeId = detainerToDetainee.remove(detainerId);
        if (detaineeId != null) {
            detaineeToDetainer.remove(detaineeId);
        }
    }

    private void uncuffByDetainee(UUID detaineeId) {
        UUID detainerId = detaineeToDetainer.remove(detaineeId);
        if (detainerId != null) {
            detainerToDetainee.remove(detainerId);
        }
    }

    /** Наручники в основной или второй руке */
    private boolean holdingHandcuffs(Player p) {
        return isHandcuffStack(p.getInventory().getItemInMainHand())
                || isHandcuffStack(p.getInventory().getItemInOffHand());
    }

    /**
     * ЛКМ/ПКМ по игроку: без Shift — сковать (или подсказка снять через Shift).
     * Со Shift — снять наручники только со своего скованного.
     *
     * @return true если событие нужно отменить (урон/взаимодействие)
     */
    private boolean handleHandcuffUse(Player detainer, Player target) {
        if (!holdingHandcuffs(detainer)) {
            return false;
        }
        if (moduleConfig().getBoolean("require-permission", true)
                && !detainer.hasPermission(ConfigConstants.Permissions.HANDCUFFS)) {
            detainer.sendMessage(Colors.parse(moduleConfig().getString("messages.no-permission", "&cНет прав.")));
            return true;
        }
        if (detainer.getUniqueId().equals(target.getUniqueId())) {
            detainer.sendMessage(Colors.parse(moduleConfig().getString("messages.cannot-cuff-self", "&cНельзя на себя.")));
            return true;
        }

        UUID d = detainer.getUniqueId();
        UUID t = target.getUniqueId();
        boolean sneaking = detainer.isSneaking();

        if (sneaking) {
            if (detaineeToDetainer.containsKey(t) && detaineeToDetainer.get(t).equals(d)) {
                uncuffPair(detainer, target);
                return true;
            }
            if (detaineeToDetainer.containsKey(t)) {
                detainer.sendMessage(Colors.parse(moduleConfig().getString("messages.uncuff-not-your-pair",
                        "&cСнять можно только со своего скованного.")));
                return true;
            }
            detainer.sendMessage(Colors.parse(moduleConfig().getString("messages.uncuff-nobody",
                    "&cНекого снимать.")));
            return true;
        }

        if (detaineeToDetainer.containsKey(t) && detaineeToDetainer.get(t).equals(d)) {
            detainer.sendMessage(Colors.parse(moduleConfig().getString("messages.uncuff-hint-sneak",
                    "&7Присядьте (Shift) и нажмите, чтобы снять.")));
            return true;
        }
        if (detaineeToDetainer.containsKey(t)) {
            detainer.sendMessage(Colors.parse(moduleConfig().getString("messages.already-cuffed", "&cУже в наручниках.")));
            return true;
        }

        int hpThreshold = moduleConfig().getInt("cuff-only-below-health-percent", 50);
        if (hpThreshold > 0 && hpThreshold <= 100 && !detainer.hasPermission(ConfigConstants.Permissions.HANDCUFFS_BYPASS)) {
            var attr = target.getAttribute(Attribute.MAX_HEALTH);
            double maxHp = attr != null ? attr.getValue() : 20.0;
            if (maxHp <= 0) {
                maxHp = 20.0;
            }
            double ratio = target.getHealth() / maxHp;
            if (ratio * 100.0 >= hpThreshold) {
                String msg = moduleConfig().getString("messages.cannot-cuff-too-much-health",
                        "&cСковать можно только если у цели меньше &f{percent}% &cздоровья.");
                detainer.sendMessage(Colors.parse(msg.replace("{percent}", String.valueOf(hpThreshold))));
                return true;
            }
        }

        cuff(detainer, target);
        return true;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onInteractPlayer(PlayerInteractEntityEvent event) {
        if (!checkEnabled() || moduleConfig() == null) return;
        if (!(event.getRightClicked() instanceof Player target)) return;
        Player detainer = event.getPlayer();
        if (!holdingHandcuffs(detainer)) return;
        if (handleHandcuffUse(detainer, target)) {
            event.setCancelled(true);
        }
    }

    /**
     * Скованный не может наносить урон (рука, лук, трезубец и т.д.).
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDetaineeDealDamage(EntityDamageByEntityEvent event) {
        if (!checkEnabled() || moduleConfig() == null) return;
        if (!moduleConfig().getBoolean("prevent-attack-when-detained", true)) return;
        Player attacker = resolveDamagingPlayer(event.getDamager());
        if (attacker == null) return;
        if (!isDetained(attacker)) return;
        if (attacker.hasPermission(ConfigConstants.Permissions.HANDCUFFS_BYPASS)) return;
        event.setCancelled(true);
        long now = System.currentTimeMillis();
        long cooldownMs = moduleConfig().getLong("cannot-attack-message-cooldown-ms", 2500L);
        if (lastCannotAttackHint.getOrDefault(attacker.getUniqueId(), 0L) + cooldownMs > now) {
            return;
        }
        lastCannotAttackHint.put(attacker.getUniqueId(), now);
        attacker.sendMessage(Colors.parse(moduleConfig().getString("messages.cannot-attack-while-detained",
                "&cВ наручниках нельзя атаковать.")));
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onAttackPlayer(EntityDamageByEntityEvent event) {
        if (!checkEnabled() || moduleConfig() == null) return;
        if (!(event.getDamager() instanceof Player detainer)) return;
        if (!(event.getEntity() instanceof Player target)) return;
        EntityDamageEvent.DamageCause cause = event.getCause();
        if (cause != EntityDamageEvent.DamageCause.ENTITY_ATTACK
                && cause != EntityDamageEvent.DamageCause.ENTITY_SWEEP_ATTACK) {
            return;
        }
        if (!holdingHandcuffs(detainer)) return;
        if (handleHandcuffUse(detainer, target)) {
            event.setCancelled(true);
        }
    }

    private static Player resolveDamagingPlayer(org.bukkit.entity.Entity damager) {
        if (damager instanceof Player p) {
            return p;
        }
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) {
            return p;
        }
        return null;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMove(PlayerMoveEvent event) {
        if (!checkEnabled()) return;
        Player p = event.getPlayer();
        if (!isDetained(p)) return;
        if (p.hasPermission(ConfigConstants.Permissions.HANDCUFFS_BYPASS)) return;

        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onTeleport(PlayerTeleportEvent event) {
        if (!checkEnabled()) return;
        Player p = event.getPlayer();
        if (!isDetained(p)) return;
        if (p.hasPermission(ConfigConstants.Permissions.HANDCUFFS_BYPASS)) return;

        if (event.getCause() == PlayerTeleportEvent.TeleportCause.PLUGIN) {
            return;
        }
        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onMount(EntityMountEvent event) {
        if (!checkEnabled()) return;
        if (!(event.getEntity() instanceof Player rider)) return;
        if (!isDetained(rider)) return;
        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        uncuffByDetainer(id);
        uncuffByDetainee(id);
        lastCannotAttackHint.remove(id);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!checkEnabled()) return;
        if (!moduleConfig().getBoolean("release-on-death", true)) return;
        Player p = event.getEntity();
        uncuffByDetainer(p.getUniqueId());
        uncuffByDetainee(p.getUniqueId());
    }

    /**
     * Подтягивание и частицы — вызывается из {@link LoUtils} после регистрации (таймер).
     */
    public void startTickTask() {
        dev.lolib.scheduler.Scheduler.get(plugin).runTimer(() -> {
            if (!checkEnabled() || moduleConfig() == null) return;

            double maxDist = moduleConfig().getDouble("max-leash-blocks", 4.0);
            boolean particles = moduleConfig().getBoolean("leash-particles", true);
            double step = moduleConfig().getDouble("particle-step", 0.35);
            boolean diffWorldRelease = moduleConfig().getBoolean("release-on-different-world", true);

            for (Map.Entry<UUID, UUID> e : List.copyOf(detainerToDetainee.entrySet())) {
                UUID dk = e.getKey();
                UUID tk = e.getValue();
                Player detainer = plugin.getServer().getPlayer(dk);
                Player detainee = plugin.getServer().getPlayer(tk);
                if (detainer == null || !detainer.isOnline() || detainee == null || !detainee.isOnline()) {
                    detainerToDetainee.remove(dk);
                    detaineeToDetainer.remove(tk);
                    continue;
                }

                if (diffWorldRelease && !detainer.getWorld().equals(detainee.getWorld())) {
                    uncuffPair(detainer, detainee);
                    continue;
                }

                Location dLoc = detainer.getLocation();
                Location tLoc = detainee.getLocation();
                double dist = dLoc.distance(tLoc);
                if (dist > maxDist + 1e-3) {
                    Vector offset = tLoc.toVector().subtract(dLoc.toVector());
                    if (offset.lengthSquared() < 1e-6) {
                        offset = new Vector(0, 0, 1);
                    }
                    offset.normalize().multiply(maxDist);
                    Location pull = dLoc.clone().add(offset);
                    pull.setYaw(tLoc.getYaw());
                    pull.setPitch(tLoc.getPitch());
                    detainee.teleportAsync(pull);
                }

                if (particles) {
                    Location a = detainer.getEyeLocation();
                    Location b = detainee.getEyeLocation();
                    Vector between = b.toVector().subtract(a.toVector());
                    double len = between.length();
                    if (len < 1e-3) continue;
                    between.normalize();
                    for (double i = 0; i <= len; i += step) {
                        Location p = a.clone().add(between.clone().multiply(i));
                        detainer.getWorld().spawnParticle(
                                Particle.DUST,
                                p,
                                1,
                                new Particle.DustOptions(Color.fromRGB(100, 100, 110), 0.85f));
                    }
                }
            }
        }, 2L, 2L);
    }
}
