package xyz.lokili.loutils.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.managers.PartyManager;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class PartyCommand implements CommandExecutor, TabCompleter {
    
    private final LoUtils plugin;
    private final List<String> subCommands = Arrays.asList(
            "create", "invite", "kick", "leave", "list", "accept", "deny", "color", "disband"
    );
    
    public PartyCommand(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command");
            return true;
        }
        
        if (args.length == 0) {
            sendMessage(player, "party.usage");
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "create" -> handleCreate(player);
            case "invite" -> handleInvite(player, args);
            case "kick" -> handleKick(player, args);
            case "leave" -> handleLeave(player);
            case "list" -> handleList(player);
            case "accept" -> handleAccept(player);
            case "deny" -> handleDeny(player);
            case "color" -> handleColor(player, args);
            case "disband" -> handleDisband(player);
            default -> sendMessage(player, "party.usage");
        }
        
        return true;
    }
    
    private void handleCreate(Player player) {
        if (plugin.getPartyManager().isInParty(player)) {
            sendMessage(player, "party.already-in-party");
            return;
        }
        
        plugin.getPartyManager().createParty(player);
        sendMessage(player, "party.created");
    }
    
    private void handleInvite(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(player, "player-required");
            return;
        }
        
        PartyManager.Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            // Auto-create party
            party = plugin.getPartyManager().createParty(player);
            sendMessage(player, "party.created");
        }
        
        if (!party.isLeader(player.getUniqueId())) {
            sendMessage(player, "party.not-leader");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(player, "player-not-found");
            return;
        }
        
        if (plugin.getPartyManager().invitePlayer(player, target)) {
            sendMessage(player, "party.invited", "{player}", target.getName());
            
            String inviteMsg = plugin.getConfigManager().getMessage("party.invite-received")
                    .replace("{player}", player.getName());
            target.sendMessage(ColorUtil.colorize(plugin.getConfigManager().getPrefix() + inviteMsg));
        } else {
            if (plugin.getPartyManager().isInParty(target)) {
                sendMessage(player, "party.already-in-party");
            } else {
                sendMessage(player, "party.party-full");
            }
        }
    }
    
    private void handleKick(Player player, String[] args) {
        if (args.length < 2) {
            sendMessage(player, "player-required");
            return;
        }
        
        Player target = Bukkit.getPlayer(args[1]);
        if (target == null) {
            sendMessage(player, "player-not-found");
            return;
        }
        
        if (plugin.getPartyManager().kickPlayer(player, target)) {
            PartyManager.Party party = plugin.getPartyManager().getParty(player);
            String msg = plugin.getConfigManager().getMessage("party.kicked")
                    .replace("{player}", target.getName());
            party.broadcast(plugin.getConfigManager().getPrefix() + msg, plugin);
            
            sendMessage(target, "party.you-kicked");
        } else {
            PartyManager.Party party = plugin.getPartyManager().getParty(player);
            if (party == null) {
                sendMessage(player, "party.not-in-party");
            } else if (!party.isLeader(player.getUniqueId())) {
                sendMessage(player, "party.not-leader");
            } else {
                sendMessage(player, "party.player-not-in-party");
            }
        }
    }
    
    private void handleLeave(Player player) {
        PartyManager.Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            sendMessage(player, "party.not-in-party");
            return;
        }
        
        String leaveMsg = plugin.getConfigManager().getMessage("party.left")
                .replace("{player}", player.getName());
        party.broadcast(plugin.getConfigManager().getPrefix() + leaveMsg, plugin);
        
        plugin.getPartyManager().leaveParty(player);
    }
    
    private void handleList(Player player) {
        PartyManager.Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            sendMessage(player, "party.not-in-party");
            return;
        }
        
        int maxSize = plugin.getConfigManager().getPartyConfig().getInt("max_size", 8);
        String header = plugin.getConfigManager().getMessage("party.list-header")
                .replace("{count}", String.valueOf(party.getSize()))
                .replace("{max}", String.valueOf(maxSize));
        player.sendMessage(ColorUtil.colorize(header));
        
        // Leader
        Player leader = Bukkit.getPlayer(party.getLeader());
        String leaderName = leader != null ? leader.getName() : "Offline";
        String leaderLine = plugin.getConfigManager().getMessage("party.list-leader")
                .replace("{player}", leaderName);
        player.sendMessage(ColorUtil.colorize(leaderLine));
        
        // Members
        for (UUID uuid : party.getMembers()) {
            Player member = Bukkit.getPlayer(uuid);
            String memberName = member != null ? member.getName() : "Offline";
            String memberLine = plugin.getConfigManager().getMessage("party.list-member")
                    .replace("{player}", memberName);
            player.sendMessage(ColorUtil.colorize(memberLine));
        }
    }
    
    private void handleAccept(Player player) {
        if (!plugin.getPartyManager().hasInvite(player)) {
            sendMessage(player, "party.no-invite");
            return;
        }
        
        PartyManager.PartyInvite invite = plugin.getPartyManager().getInvite(player);
        
        if (plugin.getPartyManager().acceptInvite(player)) {
            String joinMsg = plugin.getConfigManager().getMessage("party.joined")
                    .replace("{player}", player.getName());
            invite.party.broadcast(plugin.getConfigManager().getPrefix() + joinMsg, plugin);
        } else {
            sendMessage(player, "party.invite-expired");
        }
    }
    
    private void handleDeny(Player player) {
        if (plugin.getPartyManager().denyInvite(player)) {
            sendMessage(player, "party.invite-expired");
        } else {
            sendMessage(player, "party.no-invite");
        }
    }
    
    private void handleColor(Player player, String[] args) {
        PartyManager.Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            sendMessage(player, "party.not-in-party");
            return;
        }
        
        if (!party.isLeader(player.getUniqueId())) {
            sendMessage(player, "party.not-leader");
            return;
        }
        
        List<String> colors = plugin.getPartyManager().getAvailableColors();
        
        if (args.length < 2) {
            // Show available colors
            StringBuilder sb = new StringBuilder("&7Доступные цвета: ");
            for (int i = 0; i < colors.size(); i++) {
                sb.append(colors.get(i)).append(i + 1).append(" ");
            }
            player.sendMessage(ColorUtil.colorize(sb.toString()));
            player.sendMessage(ColorUtil.colorize("&7Используйте: /lparty color <номер>"));
            return;
        }
        
        try {
            int index = Integer.parseInt(args[1]) - 1;
            if (index < 0 || index >= colors.size()) {
                player.sendMessage(ColorUtil.colorize("&cНеверный номер цвета"));
                return;
            }
            
            String color = colors.get(index);
            plugin.getPartyManager().setPartyColor(player, color);
            
            String msg = plugin.getConfigManager().getMessage("party.color-changed");
            party.broadcast(plugin.getConfigManager().getPrefix() + msg, plugin);
        } catch (NumberFormatException e) {
            player.sendMessage(ColorUtil.colorize("&cУкажите номер цвета"));
        }
    }
    
    private void handleDisband(Player player) {
        PartyManager.Party party = plugin.getPartyManager().getParty(player);
        if (party == null) {
            sendMessage(player, "party.not-in-party");
            return;
        }
        
        if (!party.isLeader(player.getUniqueId())) {
            sendMessage(player, "party.not-leader");
            return;
        }
        
        String disbandMsg = plugin.getConfigManager().getMessage("party.disbanded");
        party.broadcast(plugin.getConfigManager().getPrefix() + disbandMsg, plugin);
        
        plugin.getPartyManager().disbandParty(player);
    }
    
    private void sendMessage(Player player, String key) {
        sendMessage(player, key, null, null);
    }
    
    private void sendMessage(Player player, String key, String placeholder, String value) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getMessage(key);
        
        if (placeholder != null && value != null) {
            message = message.replace(placeholder, value);
        }
        
        player.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            return subCommands.stream()
                    .filter(s -> s.startsWith(args[0].toLowerCase()))
                    .toList();
        }
        
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            if (sub.equals("invite") || sub.equals("kick")) {
                return Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .filter(name -> name.toLowerCase().startsWith(args[1].toLowerCase()))
                        .toList();
            }
            if (sub.equals("color")) {
                List<String> colors = plugin.getPartyManager().getAvailableColors();
                List<String> numbers = new ArrayList<>();
                for (int i = 1; i <= colors.size(); i++) {
                    numbers.add(String.valueOf(i));
                }
                return numbers;
            }
        }
        
        return new ArrayList<>();
    }
}
