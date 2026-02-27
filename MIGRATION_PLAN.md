# 🔄 План миграции на LoLib 2.0

## 📋 Этапы миграции

### Этап 1: Базовая интеграция (КРИТИЧНО)
1. ✅ Обновить build.gradle.kts - добавить lolib-2.0.0.jar
2. ✅ Обновить plugin.yml - добавить depend: LoLib
3. ⏳ Изменить LoUtils extends JavaPlugin → extends LoPlugin
4. ⏳ Заменить onEnable/onDisable → enable/disable

### Этап 2: Замена утилит (ВЫСОКИЙ ПРИОРИТЕТ)
5. ⏳ Заменить SchedulerUtil → Scheduler из LoLib
6. ⏳ Заменить ColorUtil → Colors из LoLib
7. ⏳ Заменить TimeUtil → TimeFormatter из LoLib
8. ⏳ Удалить ItemBuilder (использовать ItemBuilder из LoLib)

### Этап 3: Улучшение производительности (СРЕДНИЙ ПРИОРИТЕТ)
9. ⏳ Заменить PerformanceProfiler → TPSMonitor из LoLib
10. ⏳ Использовать NumberFormatter для форматирования чисел
11. ⏳ Использовать AsyncExecutor вместо Bukkit.getAsyncScheduler()

### Этап 4: Улучшение команд (НИЗКИЙ ПРИОРИТЕТ)
12. ⏳ Добавить @Command аннотации для команд
13. ⏳ Использовать @Arg для аргументов команд
14. ⏳ Упростить регистрацию команд

### Этап 5: Новые возможности (ОПЦИОНАЛЬНО)
15. ⏳ Использовать ItemConfig для создания предметов из YAML
16. ⏳ Добавить Database поддержку (опционально)
17. ⏳ Использовать GUI API для InvSee

## 🎯 Ожидаемые результаты

- Удаление ~800 строк кода (утилиты, scheduler, colors)
- Улучшение производительности (TPSMonitor, AsyncExecutor)
- Folia-safe из коробки
- Современный API (DataComponents, Dialog)
- Упрощение команд (@Command аннотации)

## 📊 Метрики

### Удаляемые файлы:
- SchedulerUtil.java (~100 строк)
- ColorUtil.java (~50 строк)
- TimeUtil.java (~80 строк)
- ItemBuilder.java (~100 строк)
- MessageUtil.java (~50 строк) - частично
- PerformanceProfiler.java (~150 строк) - заменить на TPSMonitor

### Упрощаемые файлы:
- LoUtils.java (150 → 80 строк)
- Все команды (упрощение через @Command)
- InvSeeCommand (использовать GUI API)

**Итого**: ~800-1000 строк кода можно удалить/упростить
