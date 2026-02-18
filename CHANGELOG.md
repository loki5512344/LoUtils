# Changelog

## [2.0.0] - 2025

### Изменения:
- ✅ Обновлено на Paper/Folia 1.21.3 (совместимо с 1.21.5)
- ✅ Требуется Java 21
- ✅ Исправлен критический NPE баг в EnchantCommand (ItemMeta null check)
- ✅ Исправлен InvSeeCommand - не работал из-за неправильного использования ColorUtil
- ✅ Добавлен ColorUtil.colorizeToString() для legacy поддержки
- ✅ Добавлен MessageUtil для уменьшения дублирования кода
- ✅ Удалены модули Vanish и Dimension Lock
- ✅ Созданы интерфейсы для всех менеджеров (IAutoRestartManager, ITPSBarManager, IWhitelistManager)
- ✅ Все менеджеры имплементируют интерфейсы (SOLID - Dependency Inversion Principle)

### Исправленные Баги:
- 🐛 EnchantCommand - NPE при null ItemMeta
- 🐛 InvSeeCommand - не работал из-за Component vs String конфликта
- 🐛 InvSeeCommand - не было синхронизации изменений (теперь интерактивный)
- 🐛 ColorUtil - добавлен метод colorizeToString() для legacy API

### Удалённые Модули:
- ❌ Vanish - удалён
- ❌ Dimension Lock - удалён

### Активные Модули:
- ✅ Whitelist
- ✅ AutoRestart
- ✅ Enchant
- ✅ Death Messages
- ✅ InvSee
- ✅ Fly/FlySpeed
- ✅ SpawnMob
- ✅ TPSBar
- ✅ PlaceholderAPI

### Известные Проблемы:
- ⚠️ TPSBarManager - Region TPS не работает (fallback на global TPS)
- ⚠️ AutoRestartManager - возможна неточность в timing предупреждений
- ⚠️ Stats/Party модули не реализованы (только в документации)

### Требования:
- Paper/Folia 1.21.3+
- Java 21 (обязательно!)

### Сборка:
```bash
gradlew.bat clean build
```

Для сборки нужна Java 21. Скачать: https://adoptium.net/temurin/releases/?version=21

---

## [1.6.1] - Предыдущая версия

### Реализовано:
- Whitelist
- AutoRestart
- Dimension Lock
- Vanish
- Enchant
- Death Messages
- InvSee
- Fly/FlySpeed
- SpawnMob
- TPSBar (частично)
- PlaceholderAPI
