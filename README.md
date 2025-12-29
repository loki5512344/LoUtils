# LoUtils

Многофункциональный плагин для Folia 1.21.8

**Автор:** loki

## Установка

1. Скачайте `LoUtils-1.0.0.jar`
2. Поместите в папку `plugins/`
3. Перезапустите сервер

## Зависимости

- **Folia 1.21.8** (обязательно)
- **PlaceholderAPI** (опционально)
- **LuckPerms** (опционально)

## Команды

| Команда | Описание | Право |
|---------|----------|-------|
| `/lw <add\|remove\|list\|enable\|disable\|reload> [player]` | Управление whitelist | `loutils.whitelist` |
| `/lar <start\|stop\|status\|reload>` | Управление авто-рестартом | `loutils.autorestart` |
| `/ll <lock\|unlock\|status\|reload> [dimension] [time]` | Блокировка измерений | `loutils.lock` |
| `/lv [player]` | Включить/выключить vanish | `loutils.vanish` |
| `/lstats [player]` | Статистика игрока | `loutils.stats` |
| `/lspawnmob <mob> <amount>` | Спавн мобов | `loutils.spawnmob` |
| `/linvsee <player>` | Просмотр инвентаря | `loutils.invsee` |
| `/lparty <create\|invite\|kick\|leave\|list\|accept\|deny\|color\|disband>` | Управление пати | `loutils.party` |
| `/loutils reload` | Перезагрузка конфигов | `loutils.admin` |

## Права

| Право | Описание |
|-------|----------|
| `loutils.whitelist` | Доступ к /lw |
| `loutils.autorestart` | Доступ к /lar |
| `loutils.lock` | Доступ к /ll |
| `loutils.lock.bypass` | Обход блокировки измерений |
| `loutils.vanish` | Доступ к /lv |
| `loutils.vanish.see` | Видеть игроков в vanish |
| `loutils.stats` | Доступ к /lstats |
| `loutils.stats.others` | Смотреть статистику других |
| `loutils.spawnmob` | Доступ к /lspawnmob |
| `loutils.invsee` | Доступ к /linvsee |
| `loutils.party` | Доступ к /lparty |
| `loutils.admin` | Доступ к /loutils |

> ⚠️ Все права по умолчанию **отключены**. Выдайте нужные права через LuckPerms или другой плагин.

## PlaceholderAPI

| Плейсхолдер | Описание |
|-------------|----------|
| `%loutils_online%` | Онлайн без vanish игроков |
| `%loutils_online_total%` | Полный онлайн |
| `%loutils_vanished_count%` | Количество в vanish |
| `%loutils_vanished%` | В vanish ли игрок (true/false) |
| `%loutils_kills%` | Убийств игрока |
| `%loutils_deaths%` | Смертей игрока |
| `%loutils_kdr%` | K/D ratio |
| `%loutils_playtime%` | Время на сервере |
| `%loutils_playtime_hours%` | Время в часах |
| `%loutils_playtime_minutes%` | Время в минутах |
| `%loutils_party_suffix%` | Суффикс пати |
| `%loutils_party_size%` | Размер пати |
| `%loutils_in_party%` | В пати ли игрок (true/false) |

## Конфигурация

```
plugins/LoUtils/
├── config.yml          # Главные настройки
├── messages.yml        # Все сообщения
├── conf/
│   ├── whitelist.yml   # Настройки whitelist
│   ├── autorestart.yml # Настройки авто-рестарта
│   ├── dimensionlock.yml # Настройки блокировки измерений
│   ├── vanish.yml      # Настройки vanish
│   ├── stats.yml       # Настройки статистики
│   ├── party.yml       # Настройки пати
│   └── deathmessages.yml # Кастомные сообщения смерти
└── data/
    ├── whitelist.yml   # Список игроков whitelist
    ├── vanish.yml      # Сохранённые vanish состояния
    └── stats.yml       # Статистика игроков
```

## Цвета

Поддерживаются все форматы цветов:
- HEX: `&#3BA8FF`
- Legacy: `&c`, `&a`, `&l` и т.д.

## Модули

### Whitelist
Независимый от vanilla whitelist. Хранит игроков по никнеймам.

### AutoRestart
Автоматический рестарт сервера по таймеру или в определённое время с предупреждениями.

### Dimension Lock
Блокировка Nether/End с голограммой-таймером и ActionBar уведомлениями.

### Vanish
Полная невидимость: скрытие из TAB, тихий вход/выход, блокировка достижений, сохранение состояния.

### Stats
Отслеживание времени на сервере, убийств, смертей, K/D.

### Party
Система групп с цветными суффиксами в чате.

### Death Messages
Кастомные сообщения смерти для PvP, мобов, окружения. Особые сообщения при убийстве невидимым игроком.

### InvSee
Просмотр инвентаря игрока с бронёй, эффектами и статусом.

## Сборка

```bash
./gradlew build
```

JAR файл будет в `build/libs/LoUtils-1.0.0.jar`

## Лицензия

MIT
