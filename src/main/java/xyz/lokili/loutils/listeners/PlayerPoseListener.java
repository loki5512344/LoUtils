package xyz.lokili.loutils.listeners;

import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.listeners.base.BaseListener;
import xyz.lokili.loutils.managers.pose.PoseManager;
import xyz.lokili.loutils.managers.pose.PoseType;
import xyz.lokili.loutils.utils.ColorUtil;
import xyz.lokili.loutils.utils.SchedulerUtil;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class PlayerPoseListener extends BaseListener {

    private final PoseManager poseManager;
    private final Map<UUID, Long> lastSnoringTime = new HashMap<>();

    public PlayerPoseListener(LoUtils plugin, xyz.lokili.loutils.api.IConfigManager configManager) {
        super(plugin, configManager, ConfigConstants.Modules.POSES, ConfigConstants.POSES_CONFIG);
        this.poseManager = plugin.getContainer().getPoseManager();

        if (config != null) {
            startSnoringTask();
        } else {
            plugin.loLogger().warn("PlayerPoseListener: config is null — poses module disabled");
        }
    }

    // ── ПКМ по блоку ─────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (!checkEnabled() || config == null) return;
        if (!config.getBoolean("sit.enabled", true)) return;
        if (!config.getBoolean("sit.right-click-enabled", true)) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        if (!player.hasPermission("loutils.sit")) return;
        if (poseManager.isInPose(player)) return;
        // Только пустая рука
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) return;

        Block block = event.getClickedBlock();
        if (block == null) return;

        // Проверяем разрешённые блоки (частичное совпадение имени)
        List<String> allowedBlocks = config.getStringList("sit.right-click-blocks");
        String blockTypeName = block.getType().name();

        boolean isAllowed = allowedBlocks.stream().anyMatch(blockTypeName::contains);
        if (!isAllowed) return;

        // ВАЖНО: передаём локацию самого блока — PoseManager сам возьмёт getBlock()
        // Старый код делал block.getLocation().add(0.5, 1, 0.5) — это был воздух!
        double sitHeight = config.getDouble("sit.sit-height", 0.0);
        boolean success = poseManager.sitPlayer(player, block.getLocation(), sitHeight);

        if (success) {
            player.sendActionBar(ColorUtil.colorize(config.getString("general.messages.sit-start", "&#9878C9Вы сели")));
            event.setCancelled(true);
        }
    }
    
    // ── ПКМ по игроку (сесть на голову) ──────────────────────────────────────
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerInteractEntity(org.bukkit.event.player.PlayerInteractEntityEvent event) {
        if (!checkEnabled() || config == null) return;
        if (!config.getBoolean("sit.enabled", true)) return;
        if (!config.getBoolean("sit.player-sit-enabled", true)) return;
        
        Player player = event.getPlayer();
        if (!player.hasPermission("loutils.sit.player")) return;
        if (poseManager.isInPose(player)) return;
        
        // Только пустая рука
        if (player.getInventory().getItemInMainHand().getType() != Material.AIR) return;
        
        // Проверяем что кликнули по игроку
        if (!(event.getRightClicked() instanceof Player target)) return;
        
        // Нельзя сесть на игрока в позе
        if (poseManager.isInPose(target)) return;
        
        // Сажаем игрока на голову цели
        target.addPassenger(player);
        
        player.sendActionBar(ColorUtil.colorize(config.getString("general.messages.sit-on-player", "&#9878C9Вы сели на игрока")));
        
        event.setCancelled(true);
    }

    // ── Движение (только для ползания) ───────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerMove(PlayerMoveEvent event) {
        if (!checkEnabled() || config == null) return;
        if (!config.getBoolean("general.cancel-on-move", true)) return;

        Player player = event.getPlayer();
        if (!poseManager.isInPose(player)) return;

        // Только ползание отменяется при движении
        if (poseManager.getPoseType(player) != PoseType.CRAWL) return;

        // Игнорируем повороты головы без смены позиции
        if (event.getFrom().distanceSquared(event.getTo()) < 0.01) return;

        poseManager.removePlayerPose(player);
        player.sendActionBar(ColorUtil.colorize(config.getString("general.messages.crawl-stop", "&#9878C9Вы перестали ползти")));
    }

    // ── Shift — встать из позы ────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        if (!checkEnabled() || config == null) return;

        Player player = event.getPlayer();
        if (!poseManager.isInPose(player)) return;
        if (!event.isSneaking()) return; // только нажатие Shift, не отпускание

        PoseType poseType = poseManager.getPoseType(player);
        poseManager.removePlayerPose(player);

        player.sendActionBar(ColorUtil.colorize(stopMessage(poseType)));
    }

    // ── Урон ─────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!checkEnabled() || config == null) return;
        if (!config.getBoolean("general.cancel-on-damage", true)) return;
        if (!(event.getEntity() instanceof Player player)) return;
        if (!poseManager.isInPose(player)) return;

        PoseType poseType = poseManager.getPoseType(player);

        // При ползании — снижаем урон, но из позы не выбрасываем
        if (poseType == PoseType.CRAWL) {
            double reduction = config.getDouble("crawl.damage-reduction", 0.5);
            event.setDamage(event.getDamage() * (1.0 - reduction));
            return;
        }

        poseManager.removePlayerPose(player);

        String msg = stopMessage(poseType);
        if (!msg.isEmpty()) {
            player.sendActionBar(ColorUtil.colorize(msg));
        }
    }

    // ── Выход с сервера ───────────────────────────────────────────────────────

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        if (poseManager.isInPose(player)) {
            poseManager.removePlayerPose(player);
        }
        lastSnoringTime.remove(player.getUniqueId());
    }

    // ── Храп ─────────────────────────────────────────────────────────────────

    private void startSnoringTask() {
        if (!config.getBoolean("lay.snoring-sound", true)) return;

        // interval в тиках
        int intervalTicks = config.getInt("lay.snoring-interval", 30) * 20;

        SchedulerUtil.runGlobalTimer(plugin, (task) -> {
            if (!checkEnabled() || config == null) return;

            long now = System.currentTimeMillis();
            long intervalMs = (long) intervalTicks * 50L;

            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (!poseManager.isInPose(player)) continue;
                if (poseManager.getPoseType(player) != PoseType.LAY) continue;

                UUID uuid = player.getUniqueId();
                Long lastTime = lastSnoringTime.get(uuid);

                if (lastTime == null || (now - lastTime) >= intervalMs) {
                    player.getWorld().playSound(
                            player.getLocation(), Sound.ENTITY_PIG_AMBIENT, 0.5f, 0.8f);
                    lastSnoringTime.put(uuid, now);
                }
            }
        }, intervalTicks, intervalTicks);
    }

    // ── Вспомогательные ──────────────────────────────────────────────────────

    /** Сообщение об остановке позы (с fallback). */
    private String stopMessage(PoseType poseType) {
        if (config == null || poseType == null) return "";
        return switch (poseType) {
            case SIT   -> config.getString("general.messages.sit-stop",   "&#9878C9Вы встали");
            case LAY   -> config.getString("general.messages.lay-stop",   "&#9878C9Вы встали");
            case CRAWL -> config.getString("general.messages.crawl-stop", "&#9878C9Вы перестали ползти");
        };
    }
}
