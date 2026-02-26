# LoUtils

Многофункциональный плагин для Paper/Folia 1.21.3+

**Автор:** loki  
**Версия:** 2.2.0

## ✨ Что нового в v2.2.0

**Новые механики (Gameplay улучшения):**
- 🔥 **CauldronCrafting** - Котел как станция переработки
  - Очистка бетона: цемент + вода → бетон (мгновенно, с эффектами)
  - Стирка: покрашенная броня/флаги/кровать → дефолтный цвет
- 🐑 **VillagerLeash** - Поводок для жителей + приманка изумрудом
- 🍃 **FastLeafDecay** - Листва исчезает за 2 секунды после рубки дерева
- 😴 **SleepPercentage** - Пропуск ночи при 30% спящих игроков (настраивается)

**Технические улучшения:**
- ✅ Все механики event-based (Folia-friendly)
- ✅ Минимальный код (KISS принцип)
- ✅ Конфиги для каждой механики

## ✨ Что нового в v2.1.1

**Hotfix:**
- 🐛 Исправлен NPE при сохранении миров в Folia (AutoRestartManager)
- 🐛 Исправлено дублирование предупреждений о рестарте

## ✨ Что нового в v2.1.0

**Рефакторинг (SOLID/DRY/KISS):**
- 📉 Уменьшение кода на ~40%
- ✅ Соблюдение SOLID принципов
- ✅ Устранение дублирования кода
- ✅ Упрощение архитектуры

**Новые функции:**
- 🎨 PlaceholderAPI: `%loutils_tps_colored%` - TPS с цветом для TAB
- 🐛 Исправлен AutoRestartManager warning timing
- 📊 PerformanceProfiler - мониторинг TPS с Discord вебхуком

## Установка

1. Установите **Java 21** (обязательно!)
   - Скачать: https://adoptium.net/temurin/releases/?version=21
2. Скачайте `LoUtils-2.2.0.jar`
3. Поместите в папку `plugins/`
4. Перезапустите сервер

## Зависимости

- **Paper/Folia 1.21.3+** (обязательно)
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
| `%loutils_tps%` | TPS (число) |
| `%loutils_tps_colored%` | TPS с цветом (зелёный ≥19, жёлтый ≥17, оранжевый ≥14, красный <14) |

**Пример использования в TAB:**
```yaml
# TAB plugin config
header: "%loutils_tps_colored% TPS"
```

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
- **CustomWorldHeight** — кастомная высота мира
- **FastLeafDecay** — быстрое гниение листвы после рубки дерева
- **SleepPercentage** — пропуск ночи при 30% спящих игроков
- **VillagerLeash** — поводок для жителей + приманка изумрудом
- **CauldronCrafting** — котел как станция переработки (бетон + стирка)
- **PerformanceProfiler** — мониторинг TPS с отправкой отчётов в Discord вебхук

## Цвета

Поддержка: `&#3BA8FF` (hex) и `&c` (legacy)

## Performance Profiler

Автоматический мониторинг производительности сервера с отправкой отчётов в Discord:

- Проверка TPS каждые 30 секунд (настраивается)
- Отправка отчёта в Discord вебхук при падении TPS ниже порога
- Детальная информация:
  - Текущий TPS и MSPT
  - Список энтити по типам (топ 20)
  - Список игроков с координатами (топ 10)
  - Анализ чанков с большим количеством энтити
- Cooldown между отчётами (300 сек по умолчанию)

**Настройка:**
1. Создайте Discord вебхук в настройках канала
2. Вставьте URL в `conf/performance.yml` → `webhook-url`
3. Настройте порог TPS (`tps-threshold: 15.0`)
4. Перезапустите сервер или `/loutils reload`

## Сборка

```bash
gradlew.bat clean build
```

JAR: `build/libs/LoUtils-2.2.0.jar`

## Тестирование

```bash
gradlew.bat test
```

Включены базовые unit тесты для:
- ColorUtil - конвертация цветов
- ConfigConstants - проверка констант

## Архитектура

Проект следует принципам SOLID/DRY/KISS:
- **ConfigLoader** - загрузка конфигов
- **MessageService** - работа с сообщениями
- **CommandBase** - базовый класс команд
- **BaseStorageManager** - базовый класс хранилищ

Подробнее: [ARCHITECTURE.md](ARCHITECTURE.md)
