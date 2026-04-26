package xyz.lokili.loutils.listeners.player.handcuffs;

import dev.lolib.scheduler.Scheduler;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.util.Vector;
import xyz.lokili.loutils.LoUtils;

import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Визуализация поводка частицами между ковавшим и скованным
 */
public class HandcuffsVisualService {

    private final LoUtils plugin;
    private final HandcuffsBindingManager bindingManager;

    public HandcuffsVisualService(LoUtils plugin, HandcuffsBindingManager bindingManager) {
        this.plugin = plugin;
        this.bindingManager = bindingManager;
    }

    /**
     * Запуск repeating task для отображения частиц
     */
    public void startParticleTask(YamlConfiguration config) {
        Scheduler.get(plugin).runTimer(() -> {
            if (config == null) return;

            boolean particles = config.getBoolean("leash-particles", true);
            if (!particles) return;

            double step = config.getDouble("particle-step", 0.35);

            for (Map.Entry<UUID, UUID> entry : List.copyOf(bindingManager.getAllBindings().entrySet())) {
                UUID detainerId = entry.getKey();
                UUID detaineeId = entry.getValue();

                Player detainer = plugin.getServer().getPlayer(detainerId);
                Player detainee = plugin.getServer().getPlayer(detaineeId);

                if (detainer == null || !detainer.isOnline() || detainee == null || !detainee.isOnline()) {
                    continue;
                }

                showLeashParticles(detainer, detainee, step);
            }
        }, 2L, 2L);
    }

    /**
     * Отображение частиц поводка между двумя игроками
     */
    private void showLeashParticles(Player detainer, Player detainee, double step) {
        Location a = detainer.getEyeLocation();
        Location b = detainee.getEyeLocation();

        Vector between = b.toVector().subtract(a.toVector());
        double len = between.length();

        if (len < 1e-3) return;

        between.normalize();

        for (double i = 0; i <= len; i += step) {
            Location p = a.clone().add(between.clone().multiply(i));
            detainer.getWorld().spawnParticle(
                Particle.DUST,
                p,
                1,
                new Particle.DustOptions(Color.fromRGB(100, 100, 110), 0.85f)
            );
        }
    }
}
