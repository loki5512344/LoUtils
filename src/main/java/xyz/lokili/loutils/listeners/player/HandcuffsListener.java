package xyz.lokili.loutils.listeners.player;

import dev.lolib.scheduler.Scheduler;
import dev.lolib.utils.Colors;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.configuration.file.YamlConfiguration;
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
import org.bukkit.persistence.PersistentDataType;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.IConfigManager;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;
import xyz.lokili.loutils.listeners.player.handcuffs.HandcuffsBindingManager;
import xyz.lokili.loutils.listeners.player.handcuffs.HandcuffsPullService;
import xyz.lokili.loutils.listeners.player.handcuffs.HandcuffsRecipeManager;
import xyz.lokili.loutils.listeners.player.handcuffs.HandcuffsVisualService;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Кандалы (наручники): связь «коварь → скованный», блок ходьбы и атак у скованного,
 * визуал поводка частицами, подтягивание при отходе ковавшего.
 */
public class HandcuffsListener extends BaseListener {

    private final HandcuffsRecipeManager recipeManager;
    private final HandcuffsBindingManager bindingManager;
    private final HandcuffsVisualService visualService;
    private final HandcuffsPullService pullService;

    /** анти-спам сообщения «нельзя бить» */
    private final Map<UUID, Long> lastCannotAttackHint = new ConcurrentHashMap<>();

    public HandcuffsListener(LoUtils plugin, IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.HANDCUFFS, ConfigConstants.HANDCUFFS_CONFIG);

        this.recipeManager = new HandcuffsRecipeManager(plugin);
        this.bindingManager = new HandcuffsBindingManager();
        this.visualService = new HandcuffsVisualService(plugin, bindingManager);
        this.pullService = new HandcuffsPullService(plugin, bindingManager);

        Scheduler.get(plugin).runLater(() -> recipeManager.registerHandcuffsRecipe(getYamlConfig()), 1L);
    }

    private YamlConfiguration getYamlConfig() {
        return (YamlConfiguration) moduleConfig();
    }

    private boolean isHandcuffStack(ItemStack stack) {
        if (stack == null || stack.getType() != Material.LEAD || !stack.hasItemMeta()) {
            return false;
        }
        return stack.getItemMeta().getPersistentDataContainer()
            .has(recipeManager.getItemKey(), PersistentDataType.BYTE);
    }

    public boolean isDetained(Player p) {
        return bindingManager.isDetained(p);
    }

    public UUID getDetainerId(UUID detainee) {
        return bindingManager.getDetainerId(detainee);
    }

    private boolean holdingHandcuffs(Player p) {
        return isHandcuffStack(p.getInventory().getItemInMainHand())
                || isHandcuffStack(p.getInventory().getItemInOffHand());
    }

    /**
     * ЛКМ/ПКМ по игроку: без Shift — сковать, со Shift — снять наручники
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
            return handleUncuff(detainer, target, d, t);
        }

        return handleCuff(detainer, target, d, t);
    }

    private boolean handleUncuff(Player detainer, Player target, UUID d, UUID t) {
        if (bindingManager.getDetainerId(t) != null && bindingManager.getDetainerId(t).equals(d)) {
            bindingManager.unbindPair(detainer, target, getYamlConfig());
            return true;
        }

        if (bindingManager.getDetainerId(t) != null) {
            detainer.sendMessage(Colors.parse(moduleConfig().getString("messages.uncuff-not-your-pair",
                    "&cСнять можно только со своего скованного.")));
            return true;
        }

        detainer.sendMessage(Colors.parse(moduleConfig().getString("messages.uncuff-nobody", "&cНекого снимать.")));
        return true;
    }

    private boolean handleCuff(Player detainer, Player target, UUID d, UUID t) {
        if (bindingManager.getDetainerId(t) != null && bindingManager.getDetainerId(t).equals(d)) {
            detainer.sendMessage(Colors.parse(moduleConfig().getString("messages.uncuff-hint-sneak",
                    "&7Присядьте (Shift) и нажмите, чтобы снять.")));
            return true;
        }

        if (bindingManager.getDetainerId(t) != null) {
            detainer.sendMessage(Colors.parse(moduleConfig().getString("messages.already-cuffed", "&cУже в наручниках.")));
            return true;
        }

        int hpThreshold = moduleConfig().getInt("cuff-only-below-health-percent", 50);
        if (hpThreshold > 0 && hpThreshold <= 100 && !detainer.hasPermission(ConfigConstants.Permissions.HANDCUFFS_BYPASS)) {
            var attr = target.getAttribute(Attribute.MAX_HEALTH);
            double maxHp = attr != null ? attr.getValue() : 20.0;
            if (maxHp <= 0) maxHp = 20.0;

            double ratio = target.getHealth() / maxHp;
            if (ratio * 100.0 >= hpThreshold) {
                String msg = moduleConfig().getString("messages.cannot-cuff-too-much-health",
                        "&cСковать можно только если у цели меньше &f{percent}% &cздоровья.");
                detainer.sendMessage(Colors.parse(msg.replace("{percent}", String.valueOf(hpThreshold))));
                return true;
            }
        }

        bindingManager.bind(detainer, target, getYamlConfig());
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

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onDetaineeDealDamage(EntityDamageByEntityEvent event) {
        if (!checkEnabled() || moduleConfig() == null) return;
        if (!moduleConfig().getBoolean("prevent-attack-when-detained", true)) return;

        Player attacker = resolveDamagingPlayer(event.getDamager());
        if (attacker == null || !bindingManager.isDetained(attacker)) return;
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
        if (!bindingManager.isDetained(p)) return;
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
        if (!bindingManager.isDetained(p)) return;
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
        if (!bindingManager.isDetained(rider)) return;

        event.setCancelled(true);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        bindingManager.unbindByDetainer(id);
        bindingManager.unbindByDetainee(id);
        lastCannotAttackHint.remove(id);
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {
        if (!checkEnabled()) return;
        if (!moduleConfig().getBoolean("release-on-death", true)) return;

        Player p = event.getEntity();
        bindingManager.unbindByDetainer(p.getUniqueId());
        bindingManager.unbindByDetainee(p.getUniqueId());
    }

    /**
     * Подтягивание и частицы — вызывается из {@link LoUtils} после регистрации (таймер).
     */
    public void startTickTask() {
        if (moduleConfig() == null) return;

        visualService.startParticleTask(getYamlConfig());
        pullService.startPullTask(getYamlConfig());
    }
}
