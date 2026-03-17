package xyz.lokili.loutils.managers.pose;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.type.Stairs;

import static xyz.lokili.loutils.managers.pose.PoseConstants.*;

/**
 * Калькулятор позиций для сидения на ступеньках.
 * Логика портирована 1:1 с GSit createStairSeatForEntity.
 */
public class StairSeatCalculator {

    /**
     * Данные о позиции сиденья на ступеньке.
     */
    public static class StairSeatPosition {
        public final double xOffset;
        public final double yOffset;
        public final double zOffset;
        public final float yaw;
        public final boolean isValid;

        public StairSeatPosition(double xOffset, double yOffset, double zOffset, float yaw) {
            this.xOffset = xOffset;
            this.yOffset = yOffset;
            this.zOffset = zOffset;
            this.yaw = yaw;
            this.isValid = true;
        }

        public StairSeatPosition() {
            this.xOffset = 0d;
            this.yOffset = 0d;
            this.zOffset = 0d;
            this.yaw = 0f;
            this.isValid = false;
        }
    }

    /**
     * Вычисляет позицию сиденья на ступеньке.
     * Учитывает facing, shape (STRAIGHT/угловые) и half (BOTTOM/TOP).
     *
     * @param block блок ступеньки
     * @param playerYaw yaw игрока (используется для верхних ступенек)
     * @return позиция сиденья или null если не удалось вычислить
     */
    public static StairSeatPosition calculateStairSeat(Block block, float playerYaw) {
        if (!(block.getBlockData() instanceof Stairs stairData)) {
            // Не ступенька — используем обычную позицию
            return new StairSeatPosition(0d, 0d, 0d, playerYaw);
        }

        // Верхняя половина ступеньки — сидим как на обычном блоке
        if (stairData.getHalf() != Bisected.Half.BOTTOM) {
            return new StairSeatPosition(0d, 0d, 0d, playerYaw);
        }

        BlockFace facing = stairData.getFacing().getOppositeFace();

        // ── Прямые ступеньки ──────────────────────────────────────────────
        if (stairData.getShape() == Stairs.Shape.STRAIGHT) {
            return switch (facing) {
                case EAST  -> new StairSeatPosition( STAIR_XZ_OFFSET, -STAIR_Y_OFFSET,             0d, -90f);
                case SOUTH -> new StairSeatPosition(            0d, -STAIR_Y_OFFSET,  STAIR_XZ_OFFSET,   0f);
                case WEST  -> new StairSeatPosition(-STAIR_XZ_OFFSET, -STAIR_Y_OFFSET,             0d,  90f);
                case NORTH -> new StairSeatPosition(            0d, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET, 180f);
                default    -> new StairSeatPosition(); // неизвестное направление
            };
        }

        // ── Угловые ступеньки ─────────────────────────────────────────────
        Stairs.Shape shape = stairData.getShape();

        if ((facing == BlockFace.NORTH && shape == Stairs.Shape.OUTER_RIGHT)
         || (facing == BlockFace.EAST  && shape == Stairs.Shape.OUTER_LEFT)
         || (facing == BlockFace.NORTH && shape == Stairs.Shape.INNER_RIGHT)
         || (facing == BlockFace.EAST  && shape == Stairs.Shape.INNER_LEFT)) {
            return new StairSeatPosition( STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET, -135f);
        }
        if ((facing == BlockFace.NORTH && shape == Stairs.Shape.OUTER_LEFT)
         || (facing == BlockFace.WEST  && shape == Stairs.Shape.OUTER_RIGHT)
         || (facing == BlockFace.NORTH && shape == Stairs.Shape.INNER_LEFT)
         || (facing == BlockFace.WEST  && shape == Stairs.Shape.INNER_RIGHT)) {
            return new StairSeatPosition(-STAIR_XZ_OFFSET, -STAIR_Y_OFFSET, -STAIR_XZ_OFFSET,  135f);
        }
        if ((facing == BlockFace.SOUTH && shape == Stairs.Shape.OUTER_RIGHT)
         || (facing == BlockFace.WEST  && shape == Stairs.Shape.OUTER_LEFT)
         || (facing == BlockFace.SOUTH && shape == Stairs.Shape.INNER_RIGHT)
         || (facing == BlockFace.WEST  && shape == Stairs.Shape.INNER_LEFT)) {
            return new StairSeatPosition(-STAIR_XZ_OFFSET, -STAIR_Y_OFFSET,  STAIR_XZ_OFFSET,   45f);
        }
        if ((facing == BlockFace.SOUTH && shape == Stairs.Shape.OUTER_LEFT)
         || (facing == BlockFace.EAST  && shape == Stairs.Shape.OUTER_RIGHT)
         || (facing == BlockFace.SOUTH && shape == Stairs.Shape.INNER_LEFT)
         || (facing == BlockFace.EAST  && shape == Stairs.Shape.INNER_RIGHT)) {
            return new StairSeatPosition( STAIR_XZ_OFFSET, -STAIR_Y_OFFSET,  STAIR_XZ_OFFSET,  -45f);
        }

        return new StairSeatPosition(); // неизвестная комбинация
    }
}
