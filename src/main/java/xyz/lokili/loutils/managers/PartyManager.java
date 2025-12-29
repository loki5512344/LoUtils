package xyz.lokili.loutils.managers;

import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class PartyManager {
    
    private final LoUtils plugin;
    private final Map<UUID, Party> playerParties; // player -> party
    private final Map<UUID, PartyInvite> pendingInvites; // invited player -> invite
    private ScheduledTask cleanupTask;
    
    public PartyManager(LoUtils plugin) {
        this.plugin = plugin;
        this.playerParties = new ConcurrentHashMap<>();
        this.pendingInvites = new ConcurrentHashMap<>();
        startCleanupTask();
    }
    
    private void startCleanupTask() {
        cleanupTask = Bukkit.getAsyncScheduler().runAtFixedRate(plugin, (task) -> {
            long now = System.currentTimeMillis();
            int timeout = plugin.getConfigManager().getPartyConfig().getInt("invite_timeout", 60) * 1000;
            
            pendingInvites.entrySet().removeIf(entry -> 
                    now - entry.getValue().timestamp > timeout);
        }, 10, 10, TimeUnit.SECONDS);
    }
    
    public void shutdown() {
        if (cleanupTask != null) {
            cleanupTask.cancel();
        }
    }
    
    public Party createParty(Player leader) {
        if (isInParty(leader)) return null;
        
        Party party = new Party(leader.getUniqueId());
        playerParties.put(leader.getUniqueId(), party);
        return party;
    }
    
    public boolean invitePlayer(Player inviter, Player invited) {
        Party party = getParty(inviter);
        if (party == null || !party.isLeader(inviter.getUniqueId())) return false;
        if (isInParty(invited)) return false;
        
        int maxSize = plugin.getConfigManager().getPartyConfig().getInt("max_size", 8);
        if (party.getSize() >= maxSize) return false;
        
        pendingInvites.put(invited.getUniqueId(), new PartyInvite(party, inviter.getUniqueId()));
        return true;
    }
    
    public boolean acceptInvite(Player player) {
        PartyInvite invite = pendingInvites.remove(player.getUniqueId());
        if (invite == null) return false;
        
        int timeout = plugin.getConfigManager().getPartyConfig().getInt("invite_timeout", 60) * 1000;
        if (System.currentTimeMillis() - invite.timestamp > timeout) return false;
        
        invite.party.addMember(player.getUniqueId());
        playerParties.put(player.getUniqueId(), invite.party);
        return true;
    }
    
    public boolean denyInvite(Player player) {
        return pendingInvites.remove(player.getUniqueId()) != null;
    }
    
    public boolean leaveParty(Player player) {
        Party party = getParty(player);
        if (party == null) return false;
        
        playerParties.remove(player.getUniqueId());
        
        if (party.isLeader(player.getUniqueId())) {
            // Disband or transfer leadership
            if (party.getMembers().isEmpty()) {
                return true;
            }
            UUID newLeader = party.getMembers().iterator().next();
            party.setLeader(newLeader);
            party.removeMember(newLeader);
        } else {
            party.removeMember(player.getUniqueId());
        }
        
        return true;
    }
    
    public boolean kickPlayer(Player leader, Player target) {
        Party party = getParty(leader);
        if (party == null || !party.isLeader(leader.getUniqueId())) return false;
        if (!party.hasMember(target.getUniqueId())) return false;
        
        party.removeMember(target.getUniqueId());
        playerParties.remove(target.getUniqueId());
        return true;
    }
    
    public boolean disbandParty(Player leader) {
        Party party = getParty(leader);
        if (party == null || !party.isLeader(leader.getUniqueId())) return false;
        
        playerParties.remove(leader.getUniqueId());
        for (UUID member : party.getMembers()) {
            playerParties.remove(member);
        }
        
        return true;
    }
    
    public boolean setPartyColor(Player leader, String color) {
        Party party = getParty(leader);
        if (party == null || !party.isLeader(leader.getUniqueId())) return false;
        
        party.setColor(color);
        return true;
    }
    
    public Party getParty(Player player) {
        return playerParties.get(player.getUniqueId());
    }
    
    public boolean isInParty(Player player) {
        return playerParties.containsKey(player.getUniqueId());
    }
    
    public boolean hasInvite(Player player) {
        PartyInvite invite = pendingInvites.get(player.getUniqueId());
        if (invite == null) return false;
        
        int timeout = plugin.getConfigManager().getPartyConfig().getInt("invite_timeout", 60) * 1000;
        return System.currentTimeMillis() - invite.timestamp <= timeout;
    }
    
    public PartyInvite getInvite(Player player) {
        return pendingInvites.get(player.getUniqueId());
    }
    
    public String getPartySuffix(Player player) {
        Party party = getParty(player);
        if (party == null) return "";
        
        if (!plugin.getConfigManager().getPartyConfig().getBoolean("suffix.enabled", true)) {
            return "";
        }
        
        String format = plugin.getConfigManager().getPartyConfig().getString("suffix.format", " &7[{color}Party&7]");
        return format.replace("{color}", party.getColor());
    }
    
    public List<String> getAvailableColors() {
        return plugin.getConfigManager().getPartyConfig().getStringList("suffix.colors");
    }
    
    public static class Party {
        private UUID leader;
        private final Set<UUID> members;
        private String color;
        
        public Party(UUID leader) {
            this.leader = leader;
            this.members = new HashSet<>();
            this.color = "&#3BA8FF";
        }
        
        public UUID getLeader() { return leader; }
        public void setLeader(UUID leader) { this.leader = leader; }
        public Set<UUID> getMembers() { return members; }
        public String getColor() { return color; }
        public void setColor(String color) { this.color = color; }
        
        public void addMember(UUID uuid) { members.add(uuid); }
        public void removeMember(UUID uuid) { members.remove(uuid); }
        public boolean hasMember(UUID uuid) { return members.contains(uuid) || leader.equals(uuid); }
        public boolean isLeader(UUID uuid) { return leader.equals(uuid); }
        public int getSize() { return members.size() + 1; }
        
        public Set<UUID> getAllMembers() {
            Set<UUID> all = new HashSet<>(members);
            all.add(leader);
            return all;
        }
        
        public void broadcast(String message, LoUtils plugin) {
            for (UUID uuid : getAllMembers()) {
                Player player = Bukkit.getPlayer(uuid);
                if (player != null) {
                    player.sendMessage(ColorUtil.colorize(message));
                }
            }
        }
    }
    
    public static class PartyInvite {
        public final Party party;
        public final UUID inviter;
        public final long timestamp;
        
        public PartyInvite(Party party, UUID inviter) {
            this.party = party;
            this.inviter = inviter;
            this.timestamp = System.currentTimeMillis();
        }
    }
}
