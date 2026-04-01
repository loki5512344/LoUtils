package xyz.lokili.loutils.commands.admin;

import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.base.CommandBase;
import xyz.lokili.loutils.constants.ConfigConstants;
import xyz.lokili.loutils.factories.ItemFactory;
import xyz.lokili.loutils.listeners.crafts.CustomElytraHelper;

import java.util.List;
import java.util.Locale;

public class LoUtilsCommand extends CommandBase {

    private static final List<String> SUBCOMMANDS = List.of("reload", "give", "fixelytra");

    /** Алиасы для выдачи (нижний регистр) */
    private static final List<String> TAB_GIVE_ITEMS = List.of(
            "inventory-check-stick", "check-stick", "inv-stick",
            "debug-stick", "debugstick",
            "handcuffs", "cuffs"
    );

    public LoUtilsCommand(LoUtils plugin) {
        super(plugin);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                               @NotNull String label, @NotNull String[] args) {

        if (!checkPermission(sender, ConfigConstants.Permissions.ADMIN)) {
            return true;
        }

        if (args.length == 0) {
            sendRawMessage(sender, "&#3BA8FF&lLoUtils &7v" + plugin.getPluginMeta().getVersion());
            sendRawMessage(sender, "&7Автор: &floki");
            sendMessage(sender, "loutils.usage");
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);

        if (sub.equals("reload")) {
            plugin.reload();
            sendMessage(sender, "config-reloaded");
            return true;
        }

        if (sub.equals("give")) {
            return handleGive(sender, args);
        }

        if (sub.equals("fixelytra")) {
            return handleFixElytra(sender);
        }

        sendMessage(sender, "loutils.usage");
        return true;
    }

    private boolean handleGive(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sendMessage(sender, "player-only");
            return true;
        }

        if (args.length < 2) {
            sendMessage(sender, "loutils.give-usage");
            return true;
        }

        String id = args[1].toLowerCase(Locale.ROOT);
        int amount = 1;
        if (args.length >= 3) {
            try {
                amount = Integer.parseInt(args[2]);
            } catch (NumberFormatException e) {
                sendMessage(sender, "invalid-number");
                return true;
            }
            if (amount < 1 || amount > 64) {
                sendMessage(sender, "invalid-number");
                return true;
            }
        }

        ItemFactory factory = plugin.getContainer().getItemFactory();
        ItemStack stack = resolveGiveItem(id, factory);
        if (stack == null) {
            sendMessage(sender, "loutils.give-invalid-item");
            return true;
        }

        stack.setAmount(amount);
        var overflow = player.getInventory().addItem(stack);
        if (!overflow.isEmpty()) {
            for (ItemStack drop : overflow.values()) {
                player.getWorld().dropItemNaturally(player.getLocation(), drop);
            }
        }

        String label = stack.getType().name().toLowerCase(Locale.ROOT);
        sendMessage(sender, "loutils.give-received", "{amount}", String.valueOf(amount), "{label}", label);
        return true;
    }

    private boolean handleFixElytra(CommandSender sender) {
        Player player = requirePlayer(sender);
        if (player == null) {
            return true;
        }

        ItemStack hand = player.getInventory().getItemInMainHand();
        if (hand.getType() != Material.ELYTRA) {
            hand = player.getInventory().getItemInOffHand();
        }
        if (hand.getType() != Material.ELYTRA) {
            sendMessage(sender, "loutils.fixelytra-not-elytra");
            return true;
        }

        var cfg = plugin.getConfigManager().getConfig(ConfigConstants.CUSTOM_CRAFTS_CONFIG);
        if (!cfg.getBoolean("enabled", true) || !cfg.getBoolean("elytra.enabled", true)) {
            sendMessage(sender, "loutils.fixelytra-disabled");
            return true;
        }

        if (!CustomElytraHelper.upgradeLegacyElytra(plugin, hand, cfg)) {
            sendMessage(sender, "loutils.fixelytra-fail");
            return true;
        }

        sendMessage(sender, "loutils.fixelytra-success");
        return true;
    }

    private @Nullable ItemStack resolveGiveItem(String id, ItemFactory factory) {
        var cfg = plugin.getConfigManager();
        if (isInventoryCheckStick(id)) {
            return factory.createInventoryCheckStick(
                    cfg.getConfig(ConfigConstants.INVENTORY_CHECK_STICK_CONFIG));
        }
        if (isDebugStick(id)) {
            return factory.createDebugStick(cfg.getConfig(ConfigConstants.DEBUG_STICK_CONFIG));
        }
        if (isHandcuffs(id)) {
            return factory.createHandcuffs(cfg.getConfig(ConfigConstants.HANDCUFFS_CONFIG));
        }
        return null;
    }

    private static boolean isInventoryCheckStick(String id) {
        return id.equals("inventory-check-stick") || id.equals("check-stick") || id.equals("inv-stick");
    }

    private static boolean isDebugStick(String id) {
        return id.equals("debug-stick") || id.equals("debugstick");
    }

    private static boolean isHandcuffs(String id) {
        return id.equals("handcuffs") || id.equals("cuffs");
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        if (!sender.hasPermission(ConfigConstants.Permissions.ADMIN)) {
            return List.of();
        }

        if (args.length == 1) {
            return filterTabComplete(SUBCOMMANDS, args[0]);
        }

        if (args.length == 2 && args[0].equalsIgnoreCase("give")) {
            return filterTabComplete(TAB_GIVE_ITEMS, args[1]);
        }

        return List.of();
    }
}
