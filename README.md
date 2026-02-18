# LoUtils

Многофункциональный плагин для Paper/Folia 1.21.3+

**Автор:** loki  
**Версия:** 2.0.0

## Установка

1. Установите **Java 21** (обязательно!)
   - Скачать: https://adoptium.net/temurin/releases/?version=21
2. Скачайте `LoUtils-2.0.0.jar`
3. Поместите в папку `plugins/`
4. Перезапустите сервер

## Зависимости

- **Paper/Folia 1.21.3 - 1.21.11** (обязательно)
- **Java 21** (обязательно!)
- **PlaceholderAPI** (опционально)
- **LuckPerms** (опционально)

## Команды

| Команда | Описание | Право |
|---------|----------|-------|
| `/lw <add\|remove\|list\|enable\|disable\|reload>` | Whitelist | `loutils.whitelist` |
| `/lar <start\|stop\|status\|reload>` | Авто-рестарт | `loutils.autorestart` |
| `/lspawnmob <mob> <amount>` | Спавн мобов | `loutils.spawnmob` |
| `/linvsee <player>` | Просмотр инвентаря | `loutils.invsee` |
| `/lenchant <enchant> <level>` | Зачарование | `loutils.enchant` |
| `/lfly [player]` | Fly toggle | `loutils.fly` |
| `/lflyspeed <0-10> [player]` | Fly speed | `loutils.flyspeed` |
| `/ltpsbar [on\|off]` | TPS BossBar | `loutils.tpsbar` |
| `/ltps` | Алиас для TPS BossBar | `loutils.tpsbar` |
| `/worldlock <add\|remove\|list\|reload> [world]` | Управление блокировкой миров | `loutils.worldlock` |
| `/loutils reload` | Перезагрузка | `loutils.admin` |

## Права

Все права по умолчанию доступны только **OP**.

| Право | Описание |
|-------|----------|
| `loutils.whitelist` | Доступ к /lw |
| `loutils.autorestart` | Доступ к /lar |
| `loutils.spawnmob` | Доступ к /lspawnmob |
| `loutils.invsee` | Доступ к /linvsee |
| `loutils.enchant` | Доступ к /lenchant |
| `loutils.fly` | Доступ к /lfly |
| `loutils.flyspeed` | Доступ к /lflyspeed |
| `loutils.tpsbar` | Доступ к /ltpsbar |
| `loutils.worldlock` | Доступ к /worldlock |
| `loutils.worldlock.bypass.*` | Обход блокировки всех миров |
| `loutils.admin` | Доступ к /loutils |

## PlaceholderAPI

| Плейсхолдер | Описание |
|-------------|----------|
| `%loutils_online%` | Онлайн игроков |
| `%loutils_online_total%` | Полный онлайн |

## Модули

- **Whitelist** — независимый от vanilla, хранит по никнеймам
- **AutoRestart** — рестарт по таймеру/времени с предупреждениями
- **Death Messages** — кастомные сообщения смерти
- **Enchant** — зачарование на любой уровень
- **TPSBar** — BossBar с TPS (Folia compatible)
- **InvSee** — просмотр инвентаря с бронёй и эффектами (интерактивный, обновляется в реальном времени)
- **SpawnMob** — спавн мобов
- **Fly** — режим полёта и скорость
- **WorldLock** — блокировка доступа к мирам

## Цвета

Поддержка: `&#3BA8FF` (hex) и `&c` (legacy)

## Сборка

```bash
gradle build
```

JAR: `build/libs/LoUtils-1.6.1.jar`
