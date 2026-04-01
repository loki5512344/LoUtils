package xyz.lokili.loutils.commands.player;

import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.attribute.Attribute;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.base.CommandBase;
import xyz.lokili.loutils.api.InvSeeTargetHolder;
import xyz.lokili.loutils.constants.ConfigConstants;
import dev.lolib.utils.Colors;

import java.util.ArrayList;
import java.util.List;

public class InvSeeCommand extends CommandBase {
    
    public InvSeeCommand(LoUtils plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                            @NotNull String label, @NotNull String[] args) {
        
        if (!checkPermission(sender, ConfigConstants.Permissions.INVSEE)) return true;
        
        Player viewer = requirePlayer(sender);
        if (viewer == null) return true;
        
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
        sendMessage(sender, "invsee.opened", "{player}", target.getName());
        return true;
    }
    
    private void openInvSee(Player viewer, Player target) {
        String title = plugin.getConfigManager().getMessage("invsee.title").replace("{player}", target.getName());
        var holder = new InvSeeHolder(target);
        Inventory inv = Bukkit.createInventory(holder, 54, Colors.parse(title));
        
        // Main inventory (0-35)
        var contents = target.getInventory().getContents();
        for (int i = 0; i < 36; i++) {
            if (contents[i] != null) inv.setItem(i, contents[i].clone());
        }
        
        // Separator (36-44)
        var sep = createPane(Material.GRAY_STAINED_GLASS_PANE, " ");
        for (int i = 36; i < 45; i++) inv.setItem(i, sep);
        
        // Armor (45-48)
        var armor = target.getInventory().getArmorContents();
        inv.setItem(45, createSlot(Material.RED_STAINED_GLASS_PANE, "&cШлем", armor[3]));
        inv.setItem(46, createSlot(Material.ORANGE_STAINED_GLASS_PANE, "&6Нагрудник", armor[2]));
        inv.setItem(47, createSlot(Material.YELLOW_STAINED_GLASS_PANE, "&eПоножи", armor[1]));
        inv.setItem(48, createSlot(Material.GREEN_STAINED_GLASS_PANE, "&aБотинки", armor[0]));
        
        // Offhand (49)
        inv.setItem(49, createSlot(Material.LIGHT_BLUE_STAINED_GLASS_PANE, "&bВторая рука", 
                target.getInventory().getItemInOffHand()));
        
        // Effects (50)
        inv.setItem(50, createEffects(target));
        
        // Status (53)
        inv.setItem(53, createStatus(target));
        
        viewer.openInventory(inv);
        plugin.getInvSeeListener().startAutoUpdate(viewer, inv, holder);
    }
    
    private static class InvSeeHolder implements InvSeeTargetHolder {
        private final Player target;

        public InvSeeHolder(Player target) {
            this.target = target;
        }

        @Override
        public @NotNull Player getInvSeeTarget() {
            return target;
        }

        @Override
        public @NotNull Inventory getInventory() {
            return target.getInventory();
        }
    }
    
    private ItemStack createPane(Material mat, String name) {
        var item = new ItemStack(mat);
        var meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(Colors.parse(name));
            item.setItemMeta(meta);
        }
        return item;
    }
    
    private ItemStack createSlot(Material glass, String name, ItemStack content) {
        if (content != null && content.getType() != Material.AIR) return content.clone();
        return createPane(glass, name + " &7(пусто)");
    }
    
    private ItemStack createEffects(Player target) {
        var item = new ItemStack(Material.POTION);
        var meta = item.getItemMeta();
        if (meta == null) return item;
        
        meta.displayName(Colors.parse("&dЭффекты"));
        
        List<Component> lore = new ArrayList<>();
        if (target.getActivePotionEffects().isEmpty()) {
            lore.add(Colors.parse("&7Нет активных эффектов"));
        } else {
            target.getActivePotionEffects().forEach(e -> {
                String name = e.getType().getKey().getKey().replace("_", " ");
                int lvl = e.getAmplifier() + 1;
                int dur = e.getDuration() / 20;
                lore.add(Colors.parse("&7• &f" + name + " " + lvl + " &7(" + dur + "с)"));
            });
        }
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    private ItemStack createStatus(Player target) {
        var item = new ItemStack(Material.PLAYER_HEAD);
        var meta = item.getItemMeta();
        if (meta == null) return item;
        
        meta.displayName(Colors.parse("&aСтатус игрока"));
        
        // Используем Paper API для получения максимального здоровья
        var maxHealthAttr = target.getAttribute(Attribute.MAX_HEALTH);
        double maxHealth = maxHealthAttr != null ? maxHealthAttr.getValue() : 20.0;
        
        List<Component> lore = new ArrayList<>();
        lore.add(Colors.parse("&7Здоровье: &c" + String.format("%.1f/%.1f", target.getHealth(), maxHealth)));
        lore.add(Colors.parse("&7Голод: &6" + target.getFoodLevel() + "/20"));
        lore.add(Colors.parse("&7Уровень: &a" + target.getLevel()));
        lore.add(Colors.parse("&7Опыт: &e" + String.format("%.0f%%", target.getExp() * 100)));
        lore.add(Colors.parse("&7Режим: &f" + target.getGameMode().name()));
        
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                 @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(ConfigConstants.Permissions.INVSEE)) return List.of();
        
        if (args.length == 1) {
            return Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .filter(name -> name.toLowerCase().startsWith(args[0].toLowerCase()))
                    .toList();
        }
        
        return List.of();
    }
}
