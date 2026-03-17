package xyz.lokili.loutils.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.commands.base.CommandBase;

import java.util.List;

/**
 * Команда для тестирования цветов
 * Временная команда для отладки проблем с hex-цветами
 */
public class ColorTestCommand extends CommandBase {
    
    public ColorTestCommand(LoUtils plugin) {
        super(plugin);
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, 
                            @NotNull String label, @NotNull String[] args) {
        
        sendRawMessage(sender, "&f=== Тест форматов цветов ===");
        
        // Тестируем legacy коды
        sendRawMessage(sender, "&aLegacy зеленый (&a)");
        sendRawMessage(sender, "&cLegacy красный (&c)");
        sendRawMessage(sender, "&eLegacy желтый (&e)");
        
        // Тестируем hex формат &#RRGGBB
        sendRawMessage(sender, "&#3BA8FFHex синий (&#3BA8FF)");
        sendRawMessage(sender, "&#FF6B6BHex красный (&#FF6B6B)");
        sendRawMessage(sender, "&#FFD700Hex золотой (&#FFD700)");
        
        // Тестируем MiniMessage формат
        sendRawMessage(sender, "<#3BA8FF>MiniMessage синий (<#3BA8FF>)");
        sendRawMessage(sender, "<gradient:#3BA8FF:#FF6B6B>Градиент</gradient>");
        sendRawMessage(sender, "<rainbow>Радуга</rainbow>");
        
        // Тестируем комбинации
        sendRawMessage(sender, "&#3BA8FFHex &aи &clegacy &eвместе");
        sendRawMessage(sender, "&7Серый &#3BA8FFс hex &#FF6B6Bцветами");
        
        sendRawMessage(sender, "&f=== Сообщения из конфига ===");
        
        // Тестируем сообщения из конфига
        sendMessage(sender, "config-reloaded");
        sendMessage(sender, "whitelist.enabled");
        sendMessage(sender, "whitelist.player-added", "{player}", "TestPlayer");
        
        return true;
    }
    
    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command,
                                                @NotNull String alias, @NotNull String[] args) {
        return List.of();
    }
}