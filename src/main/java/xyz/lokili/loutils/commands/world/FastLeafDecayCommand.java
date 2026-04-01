package xyz.lokili.loutils.commands.world;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.base.CommandBase;
import xyz.lokili.loutils.constants.ConfigConstants;

import java.util.List;

/**
 * Команда для управления FastLeafDecay
 */
public class FastLeafDecayCommand extends CommandBase {
    
    public FastLeafDecayCommand(LoUtils plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        
        if (!checkPermission(sender, "loutils.admin")) {
            return true;
        }
        
        if (args.length == 0) {
            showStatus(sender);
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "enable" -> enableModule(sender);
            case "disable" -> disableModule(sender);
            case "status" -> showStatus(sender);
            case "reload" -> reloadConfig(sender);
            case "set" -> handleSet(sender, args);
            default -> showUsage(sender);
        }
        
        return true;
    }
    
    private void enableModule(CommandSender sender) {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.FASTLEAFDECAY_CONFIG);
        config.set("enabled", true);
        plugin.getConfigManager().saveConfig(ConfigConstants.FASTLEAFDECAY_CONFIG);
        
        sendRawMessage(sender, "&aFastLeafDecay включён!");
    }
    
    private void disableModule(CommandSender sender) {
        var config = plugin.getConfigManager().getConfig(ConfigConstants.FASTLEAFDECAY_CONFIG);
        config.set("enabled", false);
        plugin.getConfigManager().saveConfig(ConfigConstants.FASTLEAFDECAY_CONFIG);
        
        sendRawMessage(sender, "&cFastLeafDecay отключён!");
    }
    
    private void showStatus(CommandSender sender) {
        boolean moduleEnabled = plugin.getConfigManager().isModuleEnabled(ConfigConstants.Modules.FASTLEAFDECAY);
        var config = plugin.getConfigManager().getConfig(ConfigConstants.FASTLEAFDECAY_CONFIG);
        boolean configEnabled = config.getBoolean("enabled", true);
        int delay = config.getInt("decay-delay", 40);
        int radius = config.getInt("search-radius", 5);
        int animationDelay = config.getInt("animation-delay", 2);
        boolean smartDecay = config.getBoolean("smart-decay", true);
        
        sendRawMessage(sender, "&f=== FastLeafDecay Status ===");
        sendRawMessage(sender, "&7Модуль в config.yml: " + (moduleEnabled ? "&aвключён" : "&cотключён"));
        sendRawMessage(sender, "&7Enabled в fastleafdecay.yml: " + (configEnabled ? "&aвключён" : "&cотключён"));
        sendRawMessage(sender, "&7Итоговый статус: " + (moduleEnabled && configEnabled ? "&aработает" : "&cне работает"));
        sendRawMessage(sender, "&7Задержка: &f" + delay + " &7тиков (&f" + (delay / 20.0) + "с&7)");
        sendRawMessage(sender, "&7Радиус поиска: &f" + radius + " &7блоков");
        sendRawMessage(sender, "&7Анимация: &f" + animationDelay + " &7тиков между листьями");
        sendRawMessage(sender, "&7Умный алгоритм: " + (smartDecay ? "&aвключён" : "&cотключён"));
    }
    
    private void reloadConfig(CommandSender sender) {
        plugin.getConfigManager().reloadAll();
        sendRawMessage(sender, "&aКонфигурация FastLeafDecay перезагружена!");
    }
    
    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sendRawMessage(sender, "&cИспользование: /lfastleaf set <delay|radius|animation|smart> <value>");
            return;
        }
        
        String setting = args[1].toLowerCase();
        String valueStr = args[2];
        var config = plugin.getConfigManager().getConfig(ConfigConstants.FASTLEAFDECAY_CONFIG);
        
        switch (setting) {
            case "delay" -> {
                try {
                    int value = Integer.parseInt(valueStr);
                    if (value < 1 || value > 200) {
                        sendRawMessage(sender, "&cЗадержка должна быть от 1 до 200 тиков!");
                        return;
                    }
                    config.set("decay-delay", value);
                    sendRawMessage(sender, "&aЗадержка установлена: &f" + value + " &7тиков (&f" + (value / 20.0) + "с&7)");
                } catch (NumberFormatException e) {
                    sendRawMessage(sender, "&cНеверное число: " + valueStr);
                    return;
                }
            }
            case "radius" -> {
                try {
                    int value = Integer.parseInt(valueStr);
                    if (value < 1 || value > 20) {
                        sendRawMessage(sender, "&cРадиус должен быть от 1 до 20 блоков!");
                        return;
                    }
                    config.set("search-radius", value);
                    sendRawMessage(sender, "&aРадиус установлен: &f" + value + " &7блоков");
                } catch (NumberFormatException e) {
                    sendRawMessage(sender, "&cНеверное число: " + valueStr);
                    return;
                }
            }
            case "animation" -> {
                try {
                    int value = Integer.parseInt(valueStr);
                    if (value < 0 || value > 20) {
                        sendRawMessage(sender, "&cЗадержка анимации должна быть от 0 до 20 тиков!");
                        return;
                    }
                    config.set("animation-delay", value);
                    sendRawMessage(sender, "&aЗадержка анимации установлена: &f" + value + " &7тиков");
                } catch (NumberFormatException e) {
                    sendRawMessage(sender, "&cНеверное число: " + valueStr);
                    return;
                }
            }
            case "smart" -> {
                boolean value = Boolean.parseBoolean(valueStr);
                config.set("smart-decay", value);
                sendRawMessage(sender, "&aУмный алгоритм: " + (value ? "&aвключён" : "&cотключён"));
            }
            default -> {
                sendRawMessage(sender, "&cНеизвестная настройка. Используйте: delay, radius, animation, smart");
                return;
            }
        }
        
        plugin.getConfigManager().saveConfig(ConfigConstants.FASTLEAFDECAY_CONFIG);
    }
    
    private void showUsage(CommandSender sender) {
        sendRawMessage(sender, "&f=== FastLeafDecay Commands ===");
        sendRawMessage(sender, "&7/lfastleaf &f- показать статус");
        sendRawMessage(sender, "&7/lfastleaf enable &f- включить модуль");
        sendRawMessage(sender, "&7/lfastleaf disable &f- отключить модуль");
        sendRawMessage(sender, "&7/lfastleaf status &f- подробный статус");
        sendRawMessage(sender, "&7/lfastleaf reload &f- перезагрузить конфиг");
        sendRawMessage(sender, "&7/lfastleaf set delay <1-200> &f- задержка в тиках");
        sendRawMessage(sender, "&7/lfastleaf set radius <1-20> &f- радиус поиска");
        sendRawMessage(sender, "&7/lfastleaf set animation <0-20> &f- задержка анимации");
        sendRawMessage(sender, "&7/lfastleaf set smart <true|false> &f- умный алгоритм");
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        
        if (!sender.hasPermission("loutils.admin")) {
            return List.of();
        }
        
        if (args.length == 1) {
            return filterTabComplete(List.of("enable", "disable", "status", "reload", "set"), args[0]);
        }
        
        if (args.length == 2 && args[0].equalsIgnoreCase("set")) {
            return filterTabComplete(List.of("delay", "radius", "animation", "smart"), args[1]);
        }
        
        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            return switch (args[1].toLowerCase()) {
                case "delay" -> List.of("40", "60", "80", "100");
                case "radius" -> List.of("3", "5", "7", "10");
                case "animation" -> List.of("0", "2", "5", "10");
                case "smart" -> List.of("true", "false");
                default -> List.of();
            };
        }
        
        return List.of();
    }
}