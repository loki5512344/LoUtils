package xyz.lokili.loutils.commands;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.utils.ColorUtil;

import java.util.ArrayList;
import java.util.List;

public class InvSeeCommand implements CommandExecutor, TabCompleter {
    
    private final LoUtils plugin;
    
    public InvSeeCommand(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.invsee")) {
            sendMessage(sender, "no-permission");
            return true;
        }
        
        if (!(sender instanceof Player viewer)) {
            sender.sendMessage("Only players can use this command");
            return true;
        }
        
        if (args.length < 1) {
            sendMessage(sender, "invsee.usage");
            return true;
        }
        
        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sendMessage(sender, "player-not-found");
            return true;
        }
        
        openInvSee(viewer, target);
        
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getMessage("invsee.opened")
                .replace("{player}", target.getName());
        sender.sendMessage(ColorUtil.colorize(prefix + message));
        
        return true;
    }
    
    private void openInvSee(Player viewer, Player target) {
        String title = plugin.getConfigManager().getMessage("invsee.title")
                .replace("{player}", target.getName());
        
        Inventory inv = Bukkit.createInventory(null, 54, ColorUtil.colorize(title));
        
        // Main inventory (slots 0-35)
        ItemStack[] contents = target.getInventory().getContents();
        for (int i = 0; i < 36; i++) {
            if (contents[i] != null) {
                inv.setItem(i, contents[i].clone());
            }
        }
        
        // Separator line (slot 36-44)
        ItemStack separator = createGlassPane(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 36; i < 45; i++) {
            inv.setItem(i, separator);
        }
        
        // Armor slots (45-48)
        ItemStack[] armor = target.getInventory().getArmorContents();
        inv.setItem(45, createInfoItem(Material.RED_STAINED_GLASS_PANE, "&cШлем", armor[3]));
        inv.setItem(46, createInfoItem(Material.ORANGE_STAINED_GLASS_PANE, "&6Нагрудник", armor[2]));
        inv.setItem(47, createInfoItem(Material.YELLOW_STAINED_GLASS_PANE, "&eПоножи", armor[1]));
        inv.setItem(48, createInfoItem(Material.GREEN_STAINED_GLASS_PANE, "&aБотинки", armor[0]));
        
        // Offhand (49)
        ItemStack offhand = target.getInventory().getItemInOffHand();
        inv.setItem(49, createInfoItem(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "&bВторая рука", offhand));
        
        // Effects info (50-52)
        inv.setItem(50, createEffectsItem(target));
        
        // Health/Food info (53)
        inv.setItem(53, createStatusItem(target));
        
        viewer.openInventory(inv);
    }
    
    private ItemStack createGlassPane(Material material, String name) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ColorUtil.colorize(name));
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createInfoItem(Material glass, String slotName, ItemStack content) {
        if (content != null && content.getType() != Material.AIR) {
            return content.clone();
        }
        return createGlassPane(glass, slotName + " &7(пусто)");
    }
    
    private ItemStack createEffectsItem(Player target) {
        ItemStack item = new ItemStack(Material.POTION);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ColorUtil.colorize("&dЭффекты"));
        
        List<Component> lore = new ArrayList<>();
        
        if (target.getActivePotionEffects().isEmpty()) {
            lore.add(ColorUtil.colorize("&7Нет активных эффектов"));
        } else {
            for (PotionEffect effect : target.getActivePotionEffects()) {
                String name = effect.getType().getKey().getKey().replace("_", " ");
                int level = effect.getAmplifier() + 1;
                int duration = effect.getDuration() / 20;
                lore.add(ColorUtil.colorize("&7• &f" + name + " " + level + " &7(" + duration + "с)"));
            }
        }
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createStatusItem(Player target) {
        ItemStack item = new ItemStack(Material.PLAYER_HEAD);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(ColorUtil.colorize("&aСтатус игрока"));
        
        List<Component> lore = new ArrayList<>();
        lore.add(ColorUtil.colorize("&7Здоровье: &c" + String.format("%.1f", target.getHealth()) + "/" + 
                String.format("%.1f", target.getMaxHealth())));
        lore.add(ColorUtil.colorize("&7Голод: &6" + target.getFoodLevel() + "/20"));
        lore.add(ColorUtil.colorize("&7Уровень: &a" + target.getLevel()));
        lore.add(ColorUtil.colorize("&7Опыт: &e" + String.format("%.0f%%", target.getExp() * 100)));
        lore.add(ColorUtil.colorize("&7Режим: &f" + target.getGameMode().name()));
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private void sendMessage(CommandSender sender, String key) {
        String prefix = plugin.getConfigManager().getPrefix();
        String message = plugin.getConfigManager().getMessage(key);
        sender.sendMessage(ColorUtil.colorize(prefix + message));
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission("loutils.invsee")) {
            return new ArrayList<>();
        }
        
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        
        return new ArrayList<>();
    }
}
