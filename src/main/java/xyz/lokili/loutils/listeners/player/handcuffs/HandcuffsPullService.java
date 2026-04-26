package xyz.lokili.loutils.listeners.player.handcuffs;

import dev.lolib.scheduler.Scheduler;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.lokili.loutils.LoUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Подтягивание скованного игрока при отходе ковавшего
 */
public class HandcuffsPullService {

    private final LoUtils plugin;
    private final HandcuffsBindingManager bindingManager;

    public HandcuffsPullService(LoUtils plugin, HandcuffsBindingManager bindingManager) {
        this.plugin = plugin;
        this.bindingManager = bindingManager;
    }

    /**
     * Запуск repeating task для подтягивания
     */
    public void startPullTask(YamlConfiguration config) {
        Scheduler.get(plugin).runTimer(() -> {
            if (config == null) return;

            double maxDist = config.getDouble("max-leash-blocks", 4.0);
            boolean diffWorldRelease = config.getBoolean("release-on-different-world", true);

            for (Map.Entry<UUID, UUID> entry : List.copyOf(bindingManager.getAllBindings().entrySet())) {
                UUID detainerId = entry.getKey();
                UUID detaineeId = entry.getValue();

                Player detainer = plugin.getServer().getPlayer(detainerId);
                Player detainee = plugin.getServer().getPlayer(detaineeId);

                if (detainer == null || !detainer.isOnline() || detainee == null || !detainee.isOnline()) {
                    bindingManager.unbindByDetainer(detainerId);
                    continue;
                }

                if (diffWorldRelease && !detainer.getWorld().equals(detainee.getWorld())) {
                    bindingManager.unbindPair(detainer, detainee, config);
                    continue;
                }

                pullDetaineeIfTooFar(detainer, detainee, maxDist);
            }
        }, 2L, 2L);
    }

    /**
     * Подтянуть скованного, если он слишком далеко
     */
    private void pullDetaineeIfTooFar(Player detainer, Player detainee, double maxDist) {
        Location dLoc = detainer.getLocation();
        Location tLoc = detainee.getLocation();
        double dist = dLoc.distance(tLoc);

        if (dist > maxDist + 1e-3) {
            Vector offset = tLoc.toVector().subtract(dLoc.toVector());

            if (offset.lengthSquared() < 1e-6) {
                offset = new Vector(0, 0, 1);
            }

            offset.normalize().multiply(maxDist);
            Location pull = dLoc.clone().add(offset);
            pull.setYaw(tLoc.getYaw());
            pull.setPitch(tLoc.getPitch());

            detainee.teleportAsync(pull);
        }
    }
}
