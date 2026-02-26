# 🔗 Интеграция с LoLib 2.0

## 📦 Установка

1. Скачайте `LoLib-2.0.0.jar` из релизов
2. Поместите в папку `libs/` проекта
3. Пересоберите проект: `./gradlew build`

## 🎯 Что можно использовать

### ✅ Уже готово к использованию:

#### 1. **LoPlugin** - базовый класс плагина
```java
public class LoUtils extends LoPlugin {
    @Override
    protected void enable() {
        loLogger().info("LoUtils запущен!");
    }
}
```

#### 2. **Scheduler** - Folia-safe планировщик
```java
Scheduler scheduler = Scheduler.get(plugin);
scheduler.runAsync(() -> { /* код */ });
scheduler.runAtEntity(player, () -> { /* код */ });
```

#### 3. **ItemBuilder** - замена нашего ItemBuilder
```java
ItemStack item = ItemStack.of(Material.DIAMOND_SWORD)
    .setData(DataComponentTypes.LORE, 
        ItemLore.lore().addLine(Component.text("Крутой меч!")).build());
```

#### 4. **Colors** - парсинг цветов
```java
Component msg = Colors.parse("<red>Red</red> &#00FF00Green &aLegacy");
```

#### 5. **TPSMonitor** - мониторинг TPS
```java
TPSMonitor monitor = TPSMonitor.get(plugin);
double tps = monitor.getCurrentTPS();
```

### 🔄 Что нужно мигрировать:

1. **LoUtils.java** → наследовать от `LoPlugin`
2. **SchedulerUtil** → использовать `Scheduler` из LoLib
3. **ColorUtil** → использовать `Colors` из LoLib
4. **ItemBuilder** → использовать DataComponent API из LoLib
5. **PerformanceProfiler** → использовать `TPSMonitor` из LoLib

### 📊 Преимущества миграции:

- ✅ Меньше кода (удалим ~500 строк утилит)
- ✅ Лучшая производительность
- ✅ Folia-safe из коробки
- ✅ Поддержка новых API (DataComponents, Dialog)
- ✅ Готовые решения (Database, Redis, GUI)

## 🚀 План миграции

### Этап 1: Базовая интеграция
- [ ] Изменить LoUtils extends JavaPlugin → extends LoPlugin
- [ ] Заменить SchedulerUtil на Scheduler
- [ ] Заменить ColorUtil на Colors

### Этап 2: Улучшения
- [ ] Использовать TPSMonitor вместо PerformanceProfiler
- [ ] Использовать ItemBuilder из LoLib
- [ ] Добавить @Command аннотации для команд

### Этап 3: Новые возможности
- [ ] Добавить Database поддержку (опционально)
- [ ] Использовать GUI API для InvSee
- [ ] Добавить Dialog API для форм

## 📝 Примеры использования

### Scheduler (замена SchedulerUtil)

```java
// Было:
SchedulerUtil.runAsync(plugin, () -> { /* код */ });

// Стало:
Scheduler.get(plugin).runAsync(() -> { /* код */ });
```

### Colors (замена ColorUtil)

```java
// Было:
String colored = ColorUtil.colorize("&aHello");

// Стало:
Component colored = Colors.parse("<green>Hello</green>");
```

### TPSMonitor (замена PerformanceProfiler)

```java
// Было:
double tps = Bukkit.getTPS()[0];

// Стало:
TPSMonitor monitor = TPSMonitor.get(plugin);
double tps = monitor.getCurrentTPS();
```

## 🔧 Конфигурация

Добавлено в `plugin.yml`:
```yaml
depend:
  - LoLib
```

Добавлено в `build.gradle.kts`:
```kotlin
compileOnly(files("libs/LoLib-2.0.0.jar"))
```

## 📚 Документация

- [Quick Reference](api_docs/QUICK_REFERENCE.md)
- [API Documentation](api_docs/API.md)
- [Guides](api_docs/guides/)

---

**Статус**: Готово к интеграции  
**Версия LoLib**: 2.0.0  
**Дата**: 26.02.2026
