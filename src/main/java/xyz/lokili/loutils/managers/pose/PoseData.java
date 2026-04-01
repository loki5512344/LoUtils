package xyz.lokili.loutils.managers.pose;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;

import java.util.UUID;

/**
 * Данные о текущей позе игрока.
 */
public class PoseData {
    private final PoseType type;
    private final ArmorStand armorStand;
    private final Block block;
    private final Location seatLocation;
    private final Location returnLocation;
    /** Для {@link PoseType#SIT_ON_PLAYER} — игрок, на котором сидят. */
    private final UUID carrierUuid;

    public PoseData(PoseType type, ArmorStand armorStand, Block block,
                    Location seatLocation, Location returnLocation) {
        this(type, armorStand, block, seatLocation, returnLocation, null);
    }

    public PoseData(PoseType type, ArmorStand armorStand, Block block,
                    Location seatLocation, Location returnLocation, UUID carrierUuid) {
        this.type = type;
        this.armorStand = armorStand;
        this.block = block;
        this.seatLocation = seatLocation;
        this.returnLocation = returnLocation;
        this.carrierUuid = carrierUuid;
    }

    public PoseType getType() {
        return type;
    }

    public ArmorStand getArmorStand() {
        return armorStand;
    }

    public Block getBlock() {
        return block;
    }

    public Location getSeatLocation() {
        return seatLocation;
    }

    public Location getReturnLocation() {
        return returnLocation;
    }

    public UUID getCarrierUuid() {
        return carrierUuid;
    }
}
