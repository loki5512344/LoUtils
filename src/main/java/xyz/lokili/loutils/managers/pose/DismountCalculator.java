package xyz.lokili.loutils.managers.pose;

import org.bukkit.Location;
import org.bukkit.Tag;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.constants.ConfigConstants;

import static xyz.lokili.loutils.managers.pose.PoseConstants.*;

/**
 * Калькулятор позиций для безопасного dismount (вставания из позы).
 * Логика портирована 1:1 с GSit handleSafeSeatDismount.
 */
public class DismountCalculator {

    private final LoUtils plugin;

    public DismountCalculator(LoUtils plugin) {
        this.plugin = plugin;
    }

    /**
     * Вычисляет позицию для безопасного dismount (1:1 с GSit handleSafeSeatDismount).
     * Формула: seatLocation + baseOffset + (stairOffset если ступенька) - configOffset
     *
     * @param data данные о позе
     * @return позиция для телепортации игрока
     */
    public Location calculateDismountLocation(PoseData data) {
        Location seatLocation = data.getSeatLocation();

        // SIT_ON_PLAYER: позиция вставания считается в PoseManager по текущей локации сидящего

        // Сидение / лежание на блоке: вставать там, где стоял до позы, а не в центре сиденья
        if (data.getType() == PoseType.SIT || data.getType() == PoseType.LAY) {
            Location ret = data.getReturnLocation();
            if (ret != null && ret.getWorld() != null
                    && ret.getWorld().equals(seatLocation.getWorld())) {
                Location back = ret.clone();
                back.add(0d, 0.05d, 0d);
                return back;
            }
        }

        Block block = data.getBlock();
        if (block == null) {
            return seatLocation.clone().add(0d, 1.0d, 0d);
        }

        // Определяем configOffset в зависимости от типа позы
        FileConfiguration config = plugin.getContainer().getConfigManager().getConfig(ConfigConstants.POSES_CONFIG);
        double configOffset = (data.getType() == PoseType.SIT) 
            ? config.getDouble("sit.sit-height", 0.0)
            : config.getDouble("lay.lay-height", -0.5);
        
        // Вычисляем Y-offset для возврата на верх блока
        double yOffset = BASE_OFFSET;
        
        // Для ступенек добавляем STAIR_Y_OFFSET
        if (Tag.STAIRS.isTagged(block.getType())) {
            yOffset += STAIR_Y_OFFSET;
        }
        
        // Вычитаем configOffset (он был добавлен при посадке)
        yOffset -= configOffset;
        
        // Возвращаем позицию на 1 блок выше сиденья с учётом всех офсетов
        return seatLocation.clone().add(0d, yOffset, 0d);
    }
}
