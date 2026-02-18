# LoUtils - TODO

## Информация
- **Название:** LoUtils
- **Автор:** loki
- **Платформа:** Folia 1.21.8
- **Версия:** 1.0.0

## Модули

### Whitelist (/lw) ✅
- [x] Хранение по никнеймам в YAML
- [x] add/remove/list/enable/disable/reload

### AutoRestart (/lar) ✅
- [x] Таймер с интервалом или точным временем
- [x] Предупреждения перед рестартом

### Dimension Lock (/ll) ✅
- [x] Блокировка nether/end на время
- [x] Голограмма с таймером (ArmorStand)
- [x] ActionBar уведомления

### Vanish (/lv) ✅
- [x] Полная невидимость
- [x] Скрытие из TAB
- [x] Тихий вход/выход
- [x] Блокировка достижений
- [x] Тихие контейнеры (без анимации/звука)
- [x] Сохранение состояния
- [x] PvP конфиг опции

### Stats (/lstats) ✅
- [x] Playtime, kills, deaths, KDR

### Party (/lparty) ✅
- [x] Создание групп
- [x] Цветные суффиксы

### Death Messages ✅
- [x] Кастомные сообщения для PvP/мобов/окружения
- [x] Особые сообщения при убийстве невидимым

### Enchant (/lenchant) ✅
- [x] Зачарование на любой уровень
- [x] Unsafe enchants

### TPSBar (/ltpsbar) ✅
- [x] BossBar с Region TPS (Folia)
- [x] Global TPS и MSPT
- [x] Цветовая индикация

### InvSee (/linvsee) ✅
- [x] Просмотр инвентаря
- [x] Броня, эффекты, статус

### SpawnMob (/lspawnmob) ✅
- [x] Спавн мобов с количеством

### Общее ✅
- [x] PlaceholderAPI интеграция
- [x] Все права default: op
- [x] Поддержка цветов &#RRGGBB и &
- [x] Folia schedulers

## Релиз 1.0.0 ✅
- [x] Все модули реализованы
- [x] Билд успешен
- [x] Git push

---

## Architecture Refactoring (v2.1.0) - Priority: HIGH

### SOLID Improvements
- [ ] Split ConfigManager into separate services:
  - [ ] ConfigLoader - config loading/saving
  - [ ] MessageService - message handling
  - [ ] ModuleRegistry - module management
- [ ] Create interfaces for all managers:
  - [ ] ICustomWorldHeightManager
  - [ ] IWorldLockManager
  - [ ] IConfigManager
- [ ] Use interfaces in dependencies (Dependency Inversion):
  - [ ] Update LoUtils.java to use interfaces
  - [ ] Update commands to depend on interfaces
- [ ] Create InvSeeHolder interface instead of reflection
- [ ] Split InvSeeListener responsibilities:
  - [ ] InvSeeEventHandler - event handling
  - [ ] InvSeeUpdateTask - update management
  - [ ] InvSeeSynchronizer - inventory sync

### DRY Improvements
- [ ] Move duplicate message logic to MessageUtil:
  - [ ] sendConfigMessage() from commands
  - [ ] Permission check messages
- [ ] Create ConfigConstants class for paths:
  - [ ] Config file paths
  - [ ] Message keys
  - [ ] Permission nodes
- [ ] Extract duplicate permission checks to utility
- [ ] Create SchedulerUtil for Folia scheduler calls

### KISS Improvements
- [ ] Simplify CustomWorldHeightListener (remove fragile reflection)
- [ ] Reduce complexity in InvSeeListener
- [ ] Simplify command structure

### Code Quality
- [ ] Add unit tests for managers
- [ ] Add integration tests
- [ ] Improve error handling
- [ ] Add logging framework
- [ ] Document public APIs
