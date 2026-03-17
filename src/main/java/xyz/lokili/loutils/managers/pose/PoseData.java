package xyz.lokili.loutils.managers.pose;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;

/**
 * Данные о текущей позе игрока.
 */
public class PoseData {
    private final PoseType type;
    private final ArmorStand armorStand;
    private final Block block;
    private final Location seatLocation;
    private final Location returnLocation;

    public PoseData(PoseType type, ArmorStand armorStand, Block block,
                    Location seatLocation, Location returnLocation) {
        this.type = type;
        this.armorStand = armorStand;
        this.block = block;
        this.seatLocation = seatLocation;
        this.returnLocation = returnLocation;
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
}
