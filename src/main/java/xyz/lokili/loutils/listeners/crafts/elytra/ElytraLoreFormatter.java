package xyz.lokili.loutils.listeners.crafts.elytra;

import dev.lolib.utils.Colors;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import xyz.lokili.loutils.constants.ElytraConstants;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.ArrayList;
import java.util.List;

/**
 * Форматирование лора для кастомных элитр
 * Single Responsibility: только отображение информации
 */
public class ElytraLoreFormatter {

    private final ElytraDataManager dataManager;

    public ElytraLoreFormatter(ElytraDataManager dataManager) {
        this.dataManager = dataManager;
    }

    /**
     * Обновляет лор элитры: статический текст + время + полоска + перезарядка
     */
    public void applyElytraLore(ItemStack stack, FileConfiguration cfg) {
        if (!dataManager.isCustomItem(stack) || cfg == null) {
            return;
        }

        ItemMeta meta = stack.getItemMeta();
        if (meta == null) {
            return;
        }

        long flightTicks = dataManager.getFlightTicks(stack);
        int limitTicks = dataManager.getEffectiveFlightLimitTicks(stack, cfg);
        long rechargeUntil = dataManager.getRechargeUntilMs(stack);
        long now = System.currentTimeMillis();

        List<Component> lore = new ArrayList<>();

        // Статический лор из конфига
        for (String line : cfg.getStringList("elytra.lore")) {
            if (line != null && !line.isBlank()) {
                lore.add(ColorUtil.colorize(line));
            }
        }

        // Строка времени
        String timeLine = cfg.getString("elytra.lore-time-line", ElytraConstants.DEFAULT_TIME_LINE)
                .replace("{used}", formatTime(flightTicks))
                .replace("{limit}", formatTime(limitTicks));
        lore.add(ColorUtil.colorize(timeLine));

        // Полоска прогресса
        int segments = cfg.getInt("elytra.flight-bar.segments", ElytraConstants.DEFAULT_FLIGHT_BAR_SEGMENTS);
        if (segments < ElytraConstants.MIN_FLIGHT_BAR_SEGMENTS) {
            segments = ElytraConstants.DEFAULT_FLIGHT_BAR_SEGMENTS;
        }
        lore.add(Colors.parse(buildFlightBar(flightTicks, limitTicks, segments, cfg)));

        // Строка перезарядки (если активна)
        if (rechargeUntil > now) {
            long seconds = (rechargeUntil - now + 999L) / ElytraConstants.MS_PER_SECOND;
            String rechargeLine = cfg.getString("elytra.lore-recharge-line", ElytraConstants.DEFAULT_RECHARGE_LINE)
                    .replace("{seconds}", String.valueOf(seconds));
            lore.add(ColorUtil.colorize(rechargeLine));
        }

        meta.lore(lore);
        stack.setItemMeta(meta);
    }

    /**
     * Форматирует время в формате MM:SS
     */
    private String formatTime(long ticks) {
        long totalSeconds = Math.max(0L, ticks) / ElytraConstants.TICKS_PER_SECOND;
        long minutes = totalSeconds / ElytraConstants.SECONDS_PER_MINUTE;
        long seconds = totalSeconds % ElytraConstants.SECONDS_PER_MINUTE;
        return String.format("%d:%02d", minutes, seconds);
    }

    /**
     * Строит полоску прогресса полёта
     */
    private String buildFlightBar(long flightTicks, int limitTicks, int segments, FileConfiguration cfg) {
        if (limitTicks < 1) {
            limitTicks = 1;
        }

        int filled = (int) Math.min(segments, (flightTicks * segments) / limitTicks);

        String[] heatColors = cfg.getStringList("elytra.flight-bar.filled-colors").toArray(String[]::new);
        if (heatColors.length == 0) {
            heatColors = ElytraConstants.DEFAULT_HEAT_COLORS;
        }

        String emptyColor = cfg.getString("elytra.flight-bar.empty", ElytraConstants.DEFAULT_EMPTY_COLOR);
        String prefix = cfg.getString("elytra.flight-bar.prefix", ElytraConstants.DEFAULT_BAR_PREFIX);
        String suffix = cfg.getString("elytra.flight-bar.suffix", ElytraConstants.DEFAULT_BAR_SUFFIX);
        String pipe = cfg.getString("elytra.flight-bar.pipe", ElytraConstants.DEFAULT_BAR_PIPE);

        StringBuilder bar = new StringBuilder();
        bar.append(prefix);

        for (int i = 0; i < segments; i++) {
            if (i < filled) {
                String color = heatColors[(i * heatColors.length) / Math.max(1, segments)];
                bar.append(color).append(pipe);
            } else {
                bar.append(emptyColor);
            }
        }

        bar.append(suffix);
        return bar.toString();
    }
}
