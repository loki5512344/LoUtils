package xyz.lokili.loutils.managers.pose;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;

/**
 * Фабрика для создания ArmorStand-сидений.
 */
public class SeatFactory {

    /**
     * Создать и настроить невидимый ArmorStand-стул.
     */
    public static ArmorStand createSeat(Location location) {
        ArmorStand stand = (ArmorStand) location.getWorld().spawnEntity(location, EntityType.ARMOR_STAND);
        stand.setVisible(false);
        stand.setGravity(false);
        stand.setInvulnerable(true);
        stand.setMarker(true);
        stand.setSmall(true);
        stand.setBasePlate(false);
        stand.setCollidable(false);
        stand.setPersistent(false);
        stand.setRotation(location.getYaw(), 0f);
        return stand;
    }

    /**
     * Вычисляет additionalOffset — точная копия формулы из GSit getSeatLocation
     * при sitInBlockCenter=true и S_SITMATERIALS.getOrDefault(type, 0d) = configOffset.
     */
    public static double calculateAdditionalOffset(Block block, double configOffset) {
        double offset = block.getBoundingBox().getMinY() + block.getBoundingBox().getHeight();
        return (offset == 0d ? 1d : offset - block.getY()) + configOffset;
    }
}
