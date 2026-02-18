# LoUtils - Архитектура и Проблемы

## Версия 2.0.0 - Изменения

### Обновления:
- ✅ Обновлено на Paper/Folia 1.21.3
- ✅ Java 21
- ✅ Исправлен критический баг с NPE в EnchantCommand (ItemMeta null check)
- ✅ Добавлен MessageUtil для уменьшения дублирования кода
- ✅ Удалены модули Vanish и Dimension Lock
- ✅ Созданы интерфейсы для всех менеджеров (SOLID - Dependency Inversion)
- ✅ Все менеджеры теперь имплементируют интерфейсы

---

## Текущая Архитектура

### Модули:
- **Whitelist** - кастомный whitelist (работает)
- **AutoRestart** - авторестарт по таймеру (работает)
- **Enchant** - зачарование предметов (работает, баг исправлен)
- **Death Messages** - кастомные сообщения смерти (работает)
- **InvSee** - просмотр инвентаря (работает)
- **Fly/FlySpeed** - полёт и скорость (работает)
- **SpawnMob** - спавн мобов (работает)
- **TPSBar** - показ TPS (работает, но region TPS fallback на global)
- **PlaceholderAPI** - плейсхолдеры (работает)

### Интерфейсы (API):
```
src/main/java/xyz/lokili/loutils/api/
├── IAutoRestartManager.java
├── ITPSBarManager.java
└── IWhitelistManager.java
```

### Структура:
```
src/main/java/xyz/lokili/loutils/
├── api/              # Интерфейсы менеджеров (NEW!)
├── commands/         # Command executors
├── listeners/        # Event listeners
├── managers/         # Business logic managers (implement api interfaces)
├── utils/           # Utility classes (ColorUtil, MessageUtil)
├── placeholders/    # PlaceholderAPI integration
└── LoUtils.java     # Main plugin class
```

---

## Улучшения SOLID

### ✅ Dependency Inversion Principle (DIP):
- Созданы интерфейсы для всех менеджеров
- Менеджеры имплементируют интерфейсы
- Команды теперь могут зависеть от интерфейсов вместо конкретных классов

### ✅ Single Responsibility (частично):
- `MessageUtil` выделен из команд
- Каждый менеджер отвечает за свой модуль

### ⚠️ Ещё нужно:
- Разделить `ConfigManager` на `ConfigLoader` и `MessageService`
- Создать `SchedulerUtil` для унификации scheduler calls
- Добавить `ConfigConstants` для путей

---

## Известные Проблемы

### ⚠️ Средние:

1. **TPSBarManager - Region TPS**
   - Метод `getRegionTPS()` незавершён
   - Fallback на global TPS
   - Нужно: завершить reflection для Folia API

2. **AutoRestartManager - Точность предупреждений**
   - Проверка `remainingSeconds == 0` может пропустить warnings
   - Нужно: использовать диапазон

### 📝 Низкие:

3. **Отсутствующие модули**
   - Stats/Party - задокументированы, но не реализованы
   - Нужно: удалить из документации или реализовать

---

## Рекомендации

### Высокий Приоритет:
1. ✅ Создать интерфейсы для менеджеров (DONE)
2. ✅ Мигрировать команды на MessageUtil (DONE)
3. Завершить TPSBarManager.getRegionTPS()
4. Исправить AutoRestartManager warning timing

### Средний Приоритет:
1. Рефакторинг ConfigManager (разделить на части)
2. Создать SchedulerUtil
3. Добавить ConfigConstants
4. Proper error handling

### Низкий Приоритет:
1. Реализовать Stats/Party или удалить из docs
2. Unit tests
3. Event-based architecture
4. Metrics/logging framework

---

## Совместимость с Folia

### ✅ Правильно:
- Использование `Bukkit.getGlobalRegionScheduler()`
- Использование `Bukkit.getRegionScheduler()`
- Использование `Bukkit.getAsyncScheduler()`

### ⚠️ Требует Внимания:
- Region TPS API не полностью реализован

---

## Сборка

Требуется **Java 21**!

```bash
gradlew.bat clean build
```

JAR: `build/libs/LoUtils-2.0.0.jar`
