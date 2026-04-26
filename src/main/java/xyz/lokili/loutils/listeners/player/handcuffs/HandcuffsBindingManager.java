package xyz.lokili.loutils.listeners.player.handcuffs;

import dev.lolib.utils.Colors;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Управление связями между игроками (кто кого сковал)
 */
public class HandcuffsBindingManager {

    /** скованный → кто сковал */
    private final Map<UUID, UUID> detaineeToDetainer = new ConcurrentHashMap<>();
    /** кто сковал → скованный (один активный на игрока) */
    private final Map<UUID, UUID> detainerToDetainee = new ConcurrentHashMap<>();

    /**
     * Проверка, скован ли игрок
     */
    public boolean isDetained(Player player) {
        return detaineeToDetainer.containsKey(player.getUniqueId());
    }

    /**
     * Получить UUID того, кто сковал игрока
     */
    public UUID getDetainerId(UUID detaineeId) {
        return detaineeToDetainer.get(detaineeId);
    }

    /**
     * Получить UUID скованного игрока
     */
    public UUID getDetaineeId(UUID detainerId) {
        return detainerToDetainee.get(detainerId);
    }

    /**
     * Сковать игрока
     */
    public void bind(Player detainer, Player target, YamlConfiguration config) {
        UUID d = detainer.getUniqueId();
        UUID t = target.getUniqueId();

        unbindByDetainer(d);

        if (detaineeToDetainer.containsKey(t)) {
            detainer.sendMessage(Colors.parse(config.getString("messages.already-cuffed", "&cУже в наручниках.")));
            return;
        }

        detainerToDetainee.put(d, t);
        detaineeToDetainer.put(t, d);

        String msg = config.getString("messages.cuffed", "&eСковали &f{target}");
        detainer.sendMessage(Colors.parse(msg.replace("{target}", target.getName())));
        target.sendMessage(Colors.parse(config.getString("messages.target-notify", "&cВас сковали.")));
    }

    /**
     * Снять наручники с пары
     */
    public void unbindPair(Player detainer, Player target, YamlConfiguration config) {
        UUID d = detainer.getUniqueId();
        UUID t = target.getUniqueId();

        detainerToDetainee.remove(d);
        detaineeToDetainer.remove(t);

        detainer.sendMessage(Colors.parse(config.getString("messages.uncuffed", "&aСнято.").replace("{target}", target.getName())));
        target.sendMessage(Colors.parse(config.getString("messages.uncuff-target-notify", "&aСняли наручники.")));
    }

    /**
     * Снять наручники по ID ковавшего
     */
    public void unbindByDetainer(UUID detainerId) {
        UUID detaineeId = detainerToDetainee.remove(detainerId);
        if (detaineeId != null) {
            detaineeToDetainer.remove(detaineeId);
        }
    }

    /**
     * Снять наручники по ID скованного
     */
    public void unbindByDetainee(UUID detaineeId) {
        UUID detainerId = detaineeToDetainer.remove(detaineeId);
        if (detainerId != null) {
            detainerToDetainee.remove(detainerId);
        }
    }

    /**
     * Получить все активные связи (для тикового таска)
     */
    public Map<UUID, UUID> getAllBindings() {
        return detainerToDetainee;
    }
}
