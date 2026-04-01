package xyz.lokili.loutils.listeners.player;

import dev.lolib.scheduler.Scheduler;
import dev.lolib.scheduler.ScheduledTask;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import xyz.lokili.loutils.LoUtils;
import xyz.lokili.loutils.api.InvSeeTargetHolder;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class InvSeeListener implements Listener {
    
    private final LoUtils plugin;
    private final Map<UUID, ScheduledTask> updateTasks = new ConcurrentHashMap<>();
    
    public InvSeeListener(LoUtils plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.NORMAL)
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof InvSeeTargetHolder)) {
            return;
        }

        // Блокируем клики по информационным слотам (36-53)
        int slot = event.getRawSlot();
        if (slot >= 36 && slot <= 53) {
            event.setCancelled(true);
            return;
        }
        
        // Разрешаем изменения в основном инвентаре (0-35)
        // Синхронизируем изменения сразу после клика
        Scheduler.get(plugin).runLater(() -> {
            syncInventory(event.getInventory(), holder);
        }, 1L);
    }
    
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        if (!(event.getPlayer() instanceof Player viewer)) {
            return;
        }
        
        InventoryHolder holder = event.getInventory().getHolder();
        if (!(holder instanceof InvSeeTargetHolder)) {
            return;
        }

        // Останавливаем задачу обновления
        ScheduledTask task = updateTasks.remove(viewer.getUniqueId());
        if (task != null) {
            task.cancel();
        }
        
        // Финальная синхронизация при закрытии
        syncInventory(event.getInventory(), holder);
    }
    
    /**
     * Начинает автоматическое обновление InvSee инвентаря
     */
    public void startAutoUpdate(Player viewer, Inventory invSee, InventoryHolder holder) {
        // Останавливаем предыдущую задачу если есть
        ScheduledTask oldTask = updateTasks.remove(viewer.getUniqueId());
        if (oldTask != null) {
            oldTask.cancel();
        }
        
        // Создаём новую задачу обновления каждые 10 тиков (0.5 секунды)
        Scheduler scheduler = Scheduler.get(plugin);
        ScheduledTask task = scheduler.runTimerAsync(() -> {
            if (!viewer.isOnline()) {
                updateTasks.remove(viewer.getUniqueId());
                return;
            }

            if (!(holder instanceof InvSeeTargetHolder invHolder)) {
                updateTasks.remove(viewer.getUniqueId());
                return;
            }

            Player target = invHolder.getInvSeeTarget();

            if (!target.isOnline()) {
                scheduler.run(viewer::closeInventory);
                updateTasks.remove(viewer.getUniqueId());
                return;
            }

            scheduler.run(() -> updateInventoryDisplay(invSee, target));
        }, 10L, 10L);
        
        updateTasks.put(viewer.getUniqueId(), task);
    }
    
    /**
     * Обновляет отображение инвентаря (только чтение из target)
     */
    private void updateInventoryDisplay(Inventory invSee, Player target) {
        // Обновляем основной инвентарь (слоты 0-35)
        ItemStack[] contents = target.getInventory().getContents();
        for (int i = 0; i < 36; i++) {
            ItemStack current = invSee.getItem(i);
            ItemStack actual = contents[i];
            
            // Обновляем только если изменилось
            if (!isSameItem(current, actual)) {
                invSee.setItem(i, actual != null ? actual.clone() : null);
            }
        }
    }
    
    /**
     * Синхронизирует изменения из InvSee в реальный инвентарь target
     */
    private void syncInventory(Inventory invSee, InventoryHolder holder) {
        if (!(holder instanceof InvSeeTargetHolder invHolder)) {
            return;
        }
        Player target = invHolder.getInvSeeTarget();
        if (!target.isOnline()) {
            return;
        }

        ItemStack[] contents = new ItemStack[36];
        for (int i = 0; i < 36; i++) {
            contents[i] = invSee.getItem(i);
        }
        target.getInventory().setContents(contents);
    }
    
    /**
     * Проверяет, одинаковые ли предметы
     */
    private boolean isSameItem(ItemStack item1, ItemStack item2) {
        if (item1 == null && item2 == null) return true;
        if (item1 == null || item2 == null) return false;
        return item1.isSimilar(item2) && item1.getAmount() == item2.getAmount();
    }
    
    /**
     * Очистка при выключении плагина
     */
    public void shutdown() {
        updateTasks.values().forEach(ScheduledTask::cancel);
        updateTasks.clear();
    }
}
