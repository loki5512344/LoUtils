package xyz.lokili.loutils.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.base.CommandBase;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;
import java.util.List;

/**
 * Команда для исправления цветов в messages.yml
 */
public class ColorFixCommand extends CommandBase {
    
    public ColorFixCommand(LoUtils plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        
        if (!checkPermission(sender, "loutils.admin")) {
            return true;
        }
        
        if (args.length == 0) {
            sendRawMessage(sender, "&cИспользование: /lcolorfix <apply|revert|test>");
            return true;
        }
        
        String action = args[0].toLowerCase();
        
        switch (action) {
            case "apply" -> applyColorFix(sender);
            case "revert" -> revertColorFix(sender);
            case "test" -> testColors(sender);
            default -> sendRawMessage(sender, "&cИспользование: /lcolorfix <apply|revert|test>");
        }
        
        return true;
    }
    
    private void applyColorFix(CommandSender sender) {
        try {
            File dataFolder = plugin.getDataFolder();
            File originalMessages = new File(dataFolder, "messages.yml");
            File fixedMessages = new File(plugin.getDataFolder(), "../src/main/resources/messages-fixed.yml");
            File backupMessages = new File(dataFolder, "messages-backup.yml");
            
            // Создаём бэкап оригинального файла
            if (originalMessages.exists()) {
                Files.copy(originalMessages.toPath(), backupMessages.toPath(), StandardCopyOption.REPLACE_EXISTING);
                sendRawMessage(sender, "&aБэкап создан: messages-backup.yml");
            }
            
            // Копируем исправленный файл
            if (fixedMessages.exists()) {
                Files.copy(fixedMessages.toPath(), originalMessages.toPath(), StandardCopyOption.REPLACE_EXISTING);
                sendRawMessage(sender, "&aИсправленные цвета применены!");
                sendRawMessage(sender, "&eПерезагрузите плагин: /loutils reload");
            } else {
                sendRawMessage(sender, "&cФайл messages-fixed.yml не найден!");
            }
            
        } catch (IOException e) {
            sendRawMessage(sender, "&cОшибка при применении исправлений: " + e.getMessage());
        }
    }
    
    private void revertColorFix(CommandSender sender) {
        try {
            File dataFolder = plugin.getDataFolder();
            File originalMessages = new File(dataFolder, "messages.yml");
            File backupMessages = new File(dataFolder, "messages-backup.yml");
            
            if (backupMessages.exists()) {
                Files.copy(backupMessages.toPath(), originalMessages.toPath(), StandardCopyOption.REPLACE_EXISTING);
                sendRawMessage(sender, "&aВосстановлен оригинальный файл из бэкапа!");
                sendRawMessage(sender, "&eПерезагрузите плагин: /loutils reload");
            } else {
                sendRawMessage(sender, "&cБэкап не найден!");
            }
            
        } catch (IOException e) {
            sendRawMessage(sender, "&cОшибка при восстановлении: " + e.getMessage());
        }
    }
    
    private void testColors(CommandSender sender) {
        sendRawMessage(sender, "&f=== Тест цветов ===");
        sendRawMessage(sender, "&aLegacy зеленый (&a)");
        sendRawMessage(sender, "&#3BA8FFHex синий (&#3BA8FF)");
        sendRawMessage(sender, "<#3BA8FF>MiniMessage синий (<#3BA8FF>)");
        sendRawMessage(sender, "&#FF6B6BHex красный (&#FF6B6B)");
        sendRawMessage(sender, "<#FF6B6B>MiniMessage красный (<#FF6B6B>)");
        sendRawMessage(sender, "<gradient:#3BA8FF:#FF6B6B>Градиент</gradient>");
        sendRawMessage(sender, "<rainbow>Радуга</rainbow>");
        
        // Тестируем сообщения из конфига
        sendRawMessage(sender, "&f=== Сообщения из конфига ===");
        sendMessage(sender, "config-reloaded");
        sendMessage(sender, "whitelist.enabled");
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        
        if (args.length == 1) {
            return filterTabComplete(List.of("apply", "revert", "test"), args[0]);
        }
        
        return List.of();
    }
}