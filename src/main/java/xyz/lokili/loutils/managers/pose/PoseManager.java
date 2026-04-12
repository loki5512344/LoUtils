package xyz.lokili.loutils.managers.pose;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.SchedulerUtil;

import java.util.ArrayList;
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
    /** Носитель (игрок) → сидящий на нём игрок (после посадки через стенд). */
    private final Map<UUID, UUID> carrierToSitter = new HashMap<>();
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
     * Сесть на игрока: носитель → невидимый стенд → сидящий (чтобы не блокировать удары/интеракции носителя).
     */
    public boolean sitPlayerOnPlayer(Player sitter, Player carrier) {
        if (isInPose(sitter)) return false;
        if (isInPose(carrier)) return false;
        if (carrierToSitter.containsKey(carrier.getUniqueId())) return false;

        Location seatLoc = carrier.getLocation().clone().add(0d, carrier.getHeight() - 0.25d, 0d);
        seatLoc.setYaw(sitter.getLocation().getYaw());
        seatLoc.setPitch(0f);

        ArmorStand bridge = SeatFactory.createSeat(seatLoc);
        carrier.addPassenger(bridge);
        bridge.addPassenger(sitter);

        carrierToSitter.put(carrier.getUniqueId(), sitter.getUniqueId());
        activePoses.put(sitter.getUniqueId(),
                new PoseData(PoseType.SIT_ON_PLAYER, bridge, null, seatLoc.clone(),
                        sitter.getLocation().clone(), carrier.getUniqueId()));
        return true;
    }

    /**
     * Shift носителя — сбросить сидящего на нём игрока.
     */
    public boolean kickPassengerFromCarrier(Player carrier) {
        UUID sitterId = carrierToSitter.get(carrier.getUniqueId());
        if (sitterId == null) return false;
        Player sitter = Bukkit.getPlayer(sitterId);
        if (sitter == null || !sitter.isOnline()) {
            carrierToSitter.remove(carrier.getUniqueId());
            return false;
        }
        return removePlayerPose(sitter);
    }

    /** Носитель вышел с сервера — снять сидящего с позы. */
    public void onCarrierQuit(Player carrier) {
        UUID sitterId = carrierToSitter.remove(carrier.getUniqueId());
        if (sitterId == null) return;
        Player sitter = Bukkit.getPlayer(sitterId);
        if (sitter != null && sitter.isOnline()) {
            removePlayerPose(sitter);
        }
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

        if (data.getType() == PoseType.SIT_ON_PLAYER && data.getCarrierUuid() != null) {
            carrierToSitter.remove(data.getCarrierUuid());
        }

        if (data.getArmorStand() != null) {
            ArmorStand stand = data.getArmorStand();

            // Сидеть на игроке: носитель мог уйти — вставать там, где сидящий сейчас (на голове), не в старой returnLocation
            Location upLocation;
            if (data.getType() == PoseType.SIT_ON_PLAYER) {
                upLocation = player.getLocation().clone();
                upLocation.add(0d, 0.05d, 0d);
            } else {
                upLocation = dismountCalculator.calculateDismountLocation(data);
            }

            Location currentLoc = player.getLocation();
            upLocation.setYaw(currentLoc.getYaw());
            upLocation.setPitch(currentLoc.getPitch());

            // removePassenger + teleportAsync в одной entity-задаче
            SchedulerUtil.runForEntity(plugin, player, () -> {
                stand.removePassenger(player);
                if (data.getType() == PoseType.SIT_ON_PLAYER) {
                    Player carrier = data.getCarrierUuid() != null
                            ? Bukkit.getPlayer(data.getCarrierUuid()) : null;
                    if (carrier != null && carrier.isValid()) {
                        carrier.removePassenger(stand);
                    }
                }
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
        for (UUID uuid : new ArrayList<>(activePoses.keySet())) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                removePlayerPose(p);
            } else {
                PoseData data = activePoses.remove(uuid);
                if (data == null) continue;
                if (data.getType() == PoseType.SIT_ON_PLAYER && data.getCarrierUuid() != null) {
                    carrierToSitter.remove(data.getCarrierUuid());
                }
                ArmorStand stand = data.getArmorStand();
                if (stand != null) {
                    Player carrier = data.getCarrierUuid() != null
                            ? Bukkit.getPlayer(data.getCarrierUuid()) : null;
                    if (carrier != null && carrier.isValid()) {
                        carrier.removePassenger(stand);
                    }
                    stand.remove();
                }
            }
        }
        carrierToSitter.clear();
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
