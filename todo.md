# LoUtils - TODO

## Информация о проекте
- **Название:** LoUtils
- **Автор:** loki
- **Платформа:** Folia 1.21.8
- **Система сборки:** Gradle

## Модули

### Whitelist (/lw) ✅
- [x] Хранение игроков по никнеймам в YAML
- [x] `/lw add/remove/list/enable/disable/reload`
- [x] Право: `loutils.whitelist.command`

### AutoRestart (/lar) ✅
- [x] Таймер с интервалом или точным временем
- [x] Предупреждения (30, 15, 10, 5, 3, 1 мин + 10 сек)
- [x] `/lar start/stop/status/reload`
- [x] Право: `loutils.autorestart.command`

### Dimension Lock (/ll) ✅
- [x] Блокировка nether/end на время
- [x] Голограмма с таймером (ArmorStand)
- [x] ActionBar при попытке входа
- [x] Блокировка порталов и телепортации
- [x] `/ll lock/unlock/status/reload`
- [x] Право: `loutils.lock.command`
- [x] Bypass: `loutils.lock.bypass`

### Общее ✅
- [x] Полная кастомизация сообщений
- [x] Поддержка цветов: `&#RRGGBB` и `&`
- [x] Folia-совместимые schedulers

## Структура
```
src/main/java/xyz/lokili/loutils/
├── LoUtils.java
├── commands/
│   ├── WhitelistCommand.java
│   ├── AutoRestartCommand.java
│   └── DimensionLockCommand.java
├── managers/
│   ├── WhitelistManager.java
│   ├── AutoRestartManager.java
│   └── DimensionLockManager.java
├── listeners/
│   ├── PlayerJoinListener.java
│   └── PortalListener.java
└── utils/
    └── ColorUtil.java
```
