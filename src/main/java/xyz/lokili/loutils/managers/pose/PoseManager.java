package xyz.lokili.loutils.managers.pose;

import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.SchedulerUtil;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static xyz.lokili.loutils.managers.pose.PoseConstants.*;

/**
 * PoseManager — управление позами игроков.
 * Логика сидения портирована 1:1 с GSit (SitService / createStairSeatForEntity).
 */
public class PoseManager {

    private final LoUtils plugin;
    private final Map<UUID, PoseData> activePoses = new HashMap<>();
    private final DismountCalculator dismountCalculator;

    public PoseManager(LoUtils plugin) {
        this.plugin = plugin;
        this.dismountCalculator = new DismountCalculator(plugin);
    }

    // ── Public API ────────────────────────────────────────────────────────────

    /**
     * Посадить игрока.
     *
     * @param player       игрок
     * @param location     локация БЛОКА (не воздуха над ним!)
     * @param heightOffset per-material Y-offset из конфига (sit-height)
     */
    public boolean sitPlayer(Player player, Location location, double heightOffset) {
        if (isInPose(player)) return false;

        Block block = location.getBlock();

        if (Tag.STAIRS.isTagged(block.getType())) {
            return sitPlayerOnStair(player, block, heightOffset);
        }

        return createSeatAt(player, block, 0d, 0d, 0d, player.getLocation().getYaw(), heightOffset);
    }

    /**
     * Положить игрока.
     *
     * @param player       игрок
     * @param location     локация блока под игроком
     * @param heightOffset per-material Y-offset из конфига (lay-height)
     */
    public boolean layPlayer(Player player, Location location, double heightOffset) {
        if (isInPose(player)) return false;

        Block block = location.getBlock();
        double additionalOffset = SeatFactory.calculateAdditionalOffset(block, heightOffset);

        // Для лежания используем другой offset - игрок должен быть ниже
        Location seatLocation = block.getLocation().add(0.5d, -BASE_OFFSET + additionalOffset - 0.5, 0.5d);
        seatLocation.setYaw(player.getLocation().getYaw());
        seatLocation.setPitch(90f); // Лежим горизонтально

        ArmorStand stand = SeatFactory.createSeat(seatLocation);
        stand.addPassenger(player);

        activePoses.put(player.getUniqueId(),
                new PoseData(PoseType.LAY, stand, block, seatLocation.clone(), player.getLocation().clone()));
        return true;
    }

    /**
     * Перевести игрока в режим ползания.
     */
    public boolean crawlPlayer(Player player) {
        if (isInPose(player)) return false;

        activePoses.put(player.getUniqueId(),
                new PoseData(PoseType.CRAWL, null, null, null, player.getLocation().clone()));
        player.setSwimming(true);
        return true;
    }

    /**
     * Убрать игрока из любой позы.
     * Безопасный dismount: снимаем пассажира + телепортируем в одной entity-задаче,
     * потом удаляем стенд — иначе vanilla-dismount бросает игрока в случайную точку.
     */
    public boolean removePlayerPose(Player player) {
        PoseData data = activePoses.remove(player.getUniqueId());
        if (data == null) return false;

        if (data.getType() == PoseType.CRAWL) {
            player.setSwimming(false);
            return true;
        }

        if (data.getArmorStand() != null) {
            ArmorStand stand = data.getArmorStand();

            // Вычисляем правильную позицию для возврата (1:1 с GSit handleSafeSeatDismount)
            Location upLocation = dismountCalculator.calculateDismountLocation(data);
            
            // Сохраняем текущий поворот головы игрока
            Location currentLoc = player.getLocation();
            upLocation.setYaw(currentLoc.getYaw());
            upLocation.setPitch(currentLoc.getPitch());

            // removePassenger + teleportAsync в одной entity-задаче
            SchedulerUtil.runForEntity(plugin, player, () -> {
                stand.removePassenger(player);
                player.teleportAsync(upLocation);
            });

            // Удаляем стенд отдельно (небольшая задержка не критична)
            SchedulerUtil.runForEntity(plugin, stand, stand::remove);
        }

        return true;
    }

    /** Находится ли игрок в любой позе. */
    public boolean isInPose(Player player) {
        return activePoses.containsKey(player.getUniqueId());
    }

    /** Тип текущей позы или null. */
    public PoseType getPoseType(Player player) {
        PoseData data = activePoses.get(player.getUniqueId());
        return data != null ? data.getType() : null;
    }

    /** Принудительно сбросить все позы (onDisable). */
    public void clearAll() {
        for (PoseData data : activePoses.values()) {
            if (data.getArmorStand() != null) {
                data.getArmorStand().remove();
            }
        }
        activePoses.clear();
    }

    // ── Внутренняя логика ────────────────────────────────────────────────────

    /**
     * Сидение на ступеньке — точная копия GSit createStairSeatForEntity.
     * Учитывает facing, shape (STRAIGHT/угловые) и half (BOTTOM/TOP).
     */
    private boolean sitPlayerOnStair(Player player, Block block, double heightOffset) {
        StairSeatCalculator.StairSeatPosition position = 
            StairSeatCalculator.calculateStairSeat(block, player.getLocation().getYaw());
        
        if (!position.isValid) {
            return false;
        }

        return createSeatAt(player, block, 
            position.xOffset, position.yOffset, position.zOffset, 
            position.yaw, heightOffset);
    }

    /**
     * Создать арморстенд-сиденье и посадить игрока.
     * Формула Y: yOffset - BASE_OFFSET + additionalOffset (1:1 с GSit getSeatLocation).
     *
     * @param xOffset      смещение по X от центра блока
     * @param yOffset      чистый Y-offset (напр. -STAIR_Y_OFFSET для ступенек, 0 для обычных)
     * @param zOffset      смещение по Z от центра блока
     * @param yaw          поворот сиденья (игрок смотрит «к спинке»)
     * @param configOffset per-material offset из конфига (sit-height / lay-height)
     */
    private boolean createSeatAt(Player player, Block block,
                                  double xOffset, double yOffset, double zOffset,
                                  float yaw, double configOffset) {
        double additionalOffset = SeatFactory.calculateAdditionalOffset(block, configOffset);

        Location seatLocation = block.getLocation().add(
                0.5d + xOffset,
                yOffset - BASE_OFFSET + additionalOffset,
                0.5d + zOffset
        );
        seatLocation.setYaw(yaw);
        seatLocation.setPitch(0f);

        ArmorStand stand = SeatFactory.createSeat(seatLocation);
        stand.addPassenger(player);

        activePoses.put(player.getUniqueId(),
                new PoseData(PoseType.SIT, stand, block, seatLocation.clone(), player.getLocation().clone()));
        return true;
    }
}
