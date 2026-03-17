package xyz.lokili.loutils.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.base.CommandBase;
import dev.lolib.utils.Colors;

public class SitCommand extends CommandBase {
    
    public SitCommand(LoUtils plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Эта команда только для игроков");
            return true;
        }
        
        if (!player.hasPermission("loutils.sit")) {
            plugin.getMessageUtil().send(player, "no-permission");
            return true;
        }
        
        var poseManager = plugin.getContainer().getPoseManager();
        var config = plugin.getConfigManager().getConfig(xyz.lokili.loutils.constants.ConfigConstants.POSES_CONFIG);
        
        if (config == null) {
            player.sendMessage(Colors.parse("&#FF6B6BМодуль поз не настроен"));
            return true;
        }
        
        if (!config.getBoolean("sit.enabled", true)) {
            player.sendMessage(Colors.parse("&#FF6B6BСидение отключено"));
            return true;
        }
        
        if (poseManager.isInPose(player)) {
            player.sendMessage(Colors.parse(config.getString("general.messages.already-in-pose", "&#FF6B6BВы уже в позе")));
            return true;
        }
        
        // Получаем блок под ногами игрока (логика из GSit)
        org.bukkit.Location playerLoc = player.getLocation();
        org.bukkit.block.Block block = playerLoc.getBlock().isPassable() 
            ? playerLoc.subtract(0, 0.0625, 0).getBlock() 
            : playerLoc.getBlock();
        
        double sitHeight = config.getDouble("sit.sit-height", 0.0);
        boolean success = poseManager.sitPlayer(player, block.getLocation(), sitHeight);
        
        if (success) {
            player.sendActionBar(Colors.parse(config.getString("general.messages.sit-start", "&#9878C9Вы сели")));
        } else {
            player.sendActionBar(Colors.parse(config.getString("general.messages.cannot-sit-here", "&#FF6B6BЗдесь нельзя сесть")));
        }
        
        return true;
    }
}
