package xyz.lokili.loutils.commands.player;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.base.CommandBase;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.managers.pose.PoseType;
import dev.lolib.utils.Colors;

public class PoseCommand extends CommandBase {

    public PoseCommand(LoUtils plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эта команда только для игроков");
            return true;
        }

        if (args.length == 0 || !args[0].equalsIgnoreCase("stop")) {
            player.sendMessage(Colors.parse("&#FF6B6BИспользование: /pose stop"));
            return true;
        }

        var poseManager = plugin.getContainer().getPoseManager();

        if (!poseManager.isInPose(player)) {
            player.sendMessage(Colors.parse("&#FF6B6BВы не в позе"));
            return true;
        }

        PoseType poseType = poseManager.getPoseType(player);
        poseManager.removePlayerPose(player);

        // Получаем конфиг только для сообщения (может быть null)
        var config = plugin.getContainer().getConfigManager().getConfig(ConfigConstants.POSES_CONFIG);

        String message = switch (poseType) {
            case SIT, SIT_ON_PLAYER -> config != null
                    ? config.getString("general.messages.sit-stop",   "&#9878C9Вы встали")
                    : "&#9878C9Вы встали";
            case LAY   -> config != null
                    ? config.getString("general.messages.lay-stop",   "&#9878C9Вы встали")
                    : "&#9878C9Вы встали";
            case CRAWL -> config != null
                    ? config.getString("general.messages.crawl-stop", "&#9878C9Вы перестали ползти")
                    : "&#9878C9Вы перестали ползти";
        };

        player.sendMessage(Colors.parse(message));
        return true;
    }
}
