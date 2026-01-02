# LoUtils

Многофункциональный плагин для Folia 1.21.8

**Автор:** loki  
**Версия:** 1.6.1

## Установка

1. Скачайте `LoUtils-1.6.1.jar`
2. Поместите в папку `plugins/`
3. Перезапустите сервер

## Зависимости

- **Folia 1.21.8** (обязательно)
- **PlaceholderAPI** (опционально)
- **LuckPerms** (опционально)

## Команды

| Команда | Описание | Право |
|---------|----------|-------|
| `/lw <add\|remove\|list\|enable\|disable\|reload>` | Whitelist | `loutils.whitelist` |
| `/lar <start\|stop\|status\|reload>` | Авто-рестарт | `loutils.autorestart` |
| `/ll <lock\|unlock\|status\|reload> [dimension] [time]` | Блокировка измерений | `loutils.lock` |
| `/lv [player]` | Vanish | `loutils.vanish` |
| `/lspawnmob <mob> <amount>` | Спавн мобов | `loutils.spawnmob` |
| `/linvsee <player>` | Просмотр инвентаря | `loutils.invsee` |
| `/lenchant <enchant> <level>` | Зачарование | `loutils.enchant` |
| `/lfly [player]` | Fly toggle | `loutils.fly` |
| `/lflyspeed <0-10> [player]` | Fly speed | `loutils.flyspeed` |
| `/ltpsbar [on\|off]` | TPS BossBar | `loutils.tpsbar` |
| `/ltps` | Алиас для TPS BossBar | `loutils.tpsbar` |
| `/loutils reload` | Перезагрузка | `loutils.admin` |

## Права

Все права по умолчанию доступны только **OP**.

| Право | Описание |
|-------|----------|
| `loutils.whitelist` | Доступ к /lw |
| `loutils.autorestart` | Доступ к /lar |
| `loutils.lock` | Доступ к /ll |
| `loutils.lock.bypass` | Обход блокировки измерений |
| `loutils.vanish` | Доступ к /lv |
| `loutils.vanish.see` | Видеть игроков в vanish |
| `loutils.spawnmob` | Доступ к /lspawnmob |
| `loutils.invsee` | Доступ к /linvsee |
| `loutils.enchant` | Доступ к /lenchant |
| `loutils.fly` | Доступ к /lfly |
| `loutils.flyspeed` | Доступ к /lflyspeed |
| `loutils.tpsbar` | Доступ к /ltpsbar |
| `loutils.admin` | Доступ к /loutils |

## PlaceholderAPI

| Плейсхолдер | Описание |
|-------------|----------|
| `%loutils_online%` | Онлайн без vanish |
| `%loutils_online_total%` | Полный онлайн |
| `%loutils_vanished%` | В vanish (true/false) |
| `%loutils_vanished_count%` | Кол-во в vanish |
| `%loutils_kills%` | Убийства |
| `%loutils_deaths%` | Смерти |
| `%loutils_kdr%` | K/D ratio |
| `%loutils_playtime%` | Время на сервере |
| `%loutils_party_suffix%` | Суффикс пати |

## Модули

- **Whitelist** — независимый от vanilla, хранит по никнеймам
- **AutoRestart** — рестарт по таймеру/времени с предупреждениями
- **Dimension Lock** — блокировка Nether/End с голограммой
- **Vanish** — полная невидимость, тихие контейнеры, сохранение
- **Death Messages** — кастомные сообщения смерти
- **Enchant** — зачарование на любой уровень
- **TPSBar** — BossBar с TPS региона (Folia)
- **InvSee** — просмотр инвентаря с бронёй и эффектами
- **SpawnMob** — спавн мобов

## Цвета

Поддержка: `&#3BA8FF` (hex) и `&c` (legacy)

## Сборка

```bash
gradle build
```

JAR: `build/libs/LoUtils-1.6.1.jar`
