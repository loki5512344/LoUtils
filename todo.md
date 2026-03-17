# TODO - План улучшений LoUtils

## 🚨 Критические баги

### 1. ~~FastLeafDecay - исправить алгоритм~~ ✅ ИСПРАВЛЕНО
~~**Проблема:** При рубке дерева удаляется листва соседних деревьев~~
- ✅ Добавлена проверка типа дерева (OAK, BIRCH и т.д.)
- ✅ Удаляется только листва срубленного дерева
- ✅ Правильный дроп саженцев и яблок

### 2. ~~LightBlock - добавить возможность удаления~~ ✅ ИСПРАВЛЕНО
~~**Проблема:** Блок света можно поставить и настроить, но нельзя убрать обычным способом~~
- ✅ Добавлена настройка `allow-normal-break: true` в конфиг
- ✅ Можно ломать обычным кликом (если включено)
- ✅ Shift + ЛКМ всегда работает

## 🆕 Новые фичи

### 3. ~~Player Poses (GSit аналог)~~ ✅ РЕАЛИЗОВАНО
~~**Описание:** Система поз для игроков - сидеть, лежать, ползать~~
- ✅ Команды: /sit, /lay, /crawl, /pose stop
- ✅ ПКМ по ступенькам/плитам/коврам = автосидение
- ✅ Звук храпа для лежащих игроков
- ✅ Ползание с уменьшенным хитбоксом
- ✅ Отмена при движении/уроне
- ✅ Снижение урона при ползании

### 4. Улучшенные мотыги (Enhanced Hoes)
**Описание:** Разные мотыги дают разное количество дропа при сборе урожая
**Механика:**
- Рука: 1 предмет (как обычно)
- Деревянная мотыга: 1-2 предмета
- Каменная мотыга: 1-3 предмета  
- Железная мотыга: 2-4 предмета
- Золотая мотыга: 2-5 предметов (быстрее ломается)
- Алмазная мотыга: 3-5 предметов
- Незеритовая мотыга: 4-6 предметов

**Настройки:**
- Включение/отключение модуля
- Настройка множителей для каждого типа мотыги
- Список блоков на которые действует (пшеница, морковь, картофель, свёкла, и т.д.)

### 4. Зачарование "Автопосадка" (Auto Replant)
**Описание:** Зачарование для мотыг, которое автоматически сажает растение обратно
**Механика:**
- Работает только с мотыгами
- При сборе урожая автоматически сажает семена/саженцы обратно
- Тратит 1 семя из инвентаря игрока
- Если семян нет - просто собирает без посадки
- Совместимо с улучшенными мотыгами

**Настройки:**
- Уровни зачарования (I-III)
- Шанс автопосадки (50%/75%/100%)
- Список поддерживаемых культур
- Требование наличия семян в инвентаре

### 5. Позы игроков (Player Poses) - аналог GSit
**Описание:** Система поз для игроков - сидеть, лежать, ползать
**Команды:**
- `/sit` - сесть на месте или на блоке
- `/lay` или `/bellyflop` - лечь на блок
- `/crawl` - ползать (уменьшенный хитбокс)
- `/pose stop` - встать

**Механики:**
- ПКМ по ступенькам/плитам/коврам = автоматически сесть
- Настраиваемый список блоков для сидения
- Звук храпа для лежащих игроков (без ресурс-пака)
- Ползание через 1-блочные проходы
- Защита от урона при ползании

**Настройки:**
```yaml
poses:
  enabled: true
  sit:
    enabled: true
    right-click-blocks: [STAIRS, SLAB, CARPET]
    allow-on-any-block: true
  lay:
    enabled: true
    snoring-sound: true
    snoring-interval: 30 # секунд
  crawl:
    enabled: true
    speed-multiplier: 0.3
    damage-reduction: 0.5
```

### 6. Балансировка коров (Cow Milking Balance)
**Описание:** Ограничение на доение коров для предотвращения фарма
**Механика:**
- Корову можно доить раз в X минут
- После доения корова получает "усталость" (NBT тег)
- Визуальный индикатор готовности (частицы/название)
- Кормление пшеницей ускоряет восстановление

**Настройки:**
```yaml
cow-milking:
  enabled: true
  cooldown-minutes: 10 # время между доениями
  wheat-speedup: true # пшеница ускоряет восстановление
  wheat-speedup-minutes: 2 # на сколько ускоряет
  visual-indicator: true # показывать готовность
  particle-effect: HEART # частицы готовой коровы
```

**Механика восстановления:**
- Базовое время: 10 минут
- Кормление пшеницей: -2 минуты (до минимума 2 минуты)
- Визуальные эффекты: частицы сердечек когда готова

## 📋 Структура реализации

### LightBlock Fix  
```
src/main/java/xyz/lokili/loutils/listeners/LightBlockListener.java
- Добавить обработчик обычного BlockBreakEvent
- Настройка allow-normal-break в conf/light-block.yml
```

### Enhanced Hoes
```
src/main/java/xyz/lokili/loutils/listeners/EnhancedHoeListener.java
src/main/resources/conf/enhanced-hoes.yml
- Обработчик BlockBreakEvent для культур
- Проверка типа мотыги в руке
- Расчёт дополнительного дропа
```

### Auto Replant Enchantment
```
src/main/java/xyz/lokili/loutils/listeners/AutoReplantListener.java  
src/main/resources/conf/auto-replant.yml
- Кастомное зачарование через NBT
- Обработчик BlockBreakEvent
- Автоматическая посадка семян
```

### Player Poses (GSit аналог)
```
src/main/java/xyz/lokili/loutils/listeners/PlayerPoseListener.java
src/main/java/xyz/lokili/loutils/commands/SitCommand.java
src/main/java/xyz/lokili/loutils/commands/LayCommand.java  
src/main/java/xyz/lokili/loutils/commands/CrawlCommand.java
src/main/java/xyz/lokili/loutils/managers/PoseManager.java
src/main/resources/conf/poses.yml
- Система управления позами через ArmorStand
- Команды для смены поз
- ПКМ по блокам для автосидения
- Звуковые эффекты
```

### Cow Milking Balance
```
src/main/java/xyz/lokili/loutils/listeners/CowMilkingListener.java
src/main/java/xyz/lokili/loutils/managers/CowCooldownManager.java
src/main/resources/conf/cow-milking.yml
- PlayerInteractEntityEvent для доения
- NBT теги для отслеживания кулдауна
- Визуальные эффекты готовности
- Ускорение через кормление
```

## 🎯 Приоритеты
1. ~~**Высокий:** LightBlock Fix (UX проблема)~~ ✅ ГОТОВО
2. ~~**Средний:** Player Poses (популярная фича)~~ ✅ ГОТОВО
3. **Средний:** Enhanced Hoes (новая фича)
4. **Низкий:** Cow Milking Balance (баланс)
5. **Низкий:** Auto Replant (дополнительная фича)

## 📝 Заметки
- Все новые модули должны быть отключаемыми через config.yml
- Добавить права доступа для новых фич
- Обновить README.md после реализации
- Протестировать совместимость с другими плагинами
- Player Poses требует работы с ArmorStand entities
- Cow Milking использует NBT теги для персистентности


## 🆕 Новые фичи (добавлено)

### 7. Блокировка карт (Map Locking)
**Описание:** Защита карт от копирования с сохранением информации о владельце
**Команды:**
- `/map lock` - заблокировать карту (только владелец может разблокировать)
- `/map unlock` - разблокировать карту (только владелец)

**Механика:**
- При блокировке карты в её NBT сохраняется UUID владельца
- В описании карты добавляется строка с именем владельца (цветная)
- Попытка разблокировать чужую карту показывает сообщение в action bar
- Заблокированную карту нельзя скопировать в картографическом столе
- Защита мапартов от нежелательного копирования

**Сообщения (action bar):**
- `§aКарта заблокирована` - при успешной блокировке
- `§aКарта разблокирована` - при успешной разблокировке
- `§cВы не можете разблокировать карту, заблокированную другим игроком` - при попытке разблокировать чужую

**Настройки:**
```yaml
map-locking:
  enabled: true
  prevent-copying: true # запретить копирование заблокированных карт
  lore-format: "§7Заблокировано: §e%player%"
```

### 8. Блокировка рамок мёдом (Frame Locking)
**Описание:** Защита рамок от поворота и разрушения с помощью мёда
**Механика:**
- ПКМ по рамке с мёдом в руке = заблокировать рамку
- ПКМ по заблокированной рамке с кистью = разблокировать
- Заблокированную рамку нельзя:
  - Повернуть предмет внутри
  - Достать предмет
  - Сломать саму рамку
- Визуальный эффект: частицы мёда при блокировке
- Звук: ITEM_HONEYCOMB_WAX_ON при блокировке, ITEM_AXE_WAX_OFF при разблокировке

**Сообщения (action bar):**
- `§aРамка заблокирована` - при блокировке мёдом
- `§aРамка разблокирована` - при разблокировке кистью
- `§cЭта рамка заблокирована` - при попытке взаимодействия

**Настройки:**
```yaml
frame-locking:
  enabled: true
  lock-item: HONEYCOMB # предмет для блокировки
  unlock-item: BRUSH # предмет для разблокировки
  prevent-rotation: true
  prevent-item-removal: true
  prevent-break: true
  particles: true
  sounds: true
```

### 9. Улучшенная костная мука (Enhanced Bone Meal)
**Описание:** Костная мука теперь работает на тростнике и кактусах (порт с Bedrock Edition)
**Механика:**
- ПКМ костной мукой по тростнику = вырастает на 1-3 блока (до макс. высоты)
- ПКМ костной мукой по кактусу = вырастает на 1-3 блока (до макс. высоты)
- Эффект: зелёные частицы (VILLAGER_HAPPY) на блоке
- Звук: ITEM_BONE_MEAL_USE
- Шанс успеха: 75% (как у других растений)
- Максимальная высота: 3 блока (как в ванилле)

**Сообщения:**
- Нет сообщений, только визуальные эффекты

**Настройки:**
```yaml
enhanced-bone-meal:
  enabled: true
  sugar-cane:
    enabled: true
    max-height: 3
    growth-amount: 1-3 # случайное количество блоков
    success-chance: 0.75
  cactus:
    enabled: true
    max-height: 3
    growth-amount: 1-3
    success-chance: 0.75
  particles: true
  sounds: true
```

### 10. Починка наковален (Anvil Repair)
**Описание:** Починка повреждённых наковален железными блоками
**Механика:**
- ПКМ по повреждённой наковальне с железным блоком в руке = починить на 1 уровень
- Chipped Anvil (слегка повреждённая) + железный блок = Anvil (новая)
- Damaged Anvil (сильно повреждённая) + железный блок = Chipped Anvil
- Забирает 1 железный блок из руки
- Звук: BLOCK_ANVIL_USE
- Эффект: частицы железа (ITEM_CRACK с железным блоком)

**Сообщения (action bar):**
- `§aНаковальня починена` - при успешной починке
- `§cНаковальня не нуждается в починке` - если наковальня уже новая

**Настройки:**
```yaml
anvil-repair:
  enabled: true
  repair-item: IRON_BLOCK
  particles: true
  sounds: true
```

### 11. Срезание бирок (Name Tag Removal)
**Описание:** Удаление бирок с мобов с помощью ножниц
**Механика:**
- ПКМ ножницами по мобу с биркой = снять бирку
- Бирка дропается на землю (как предмет с именем)
- Звук: ENTITY_SHEEP_SHEAR
- Работает на всех мобах с кастомным именем
- Не работает на игроках и боссах

**Сообщения (action bar):**
- `§aБирка снята` - при успешном снятии
- `§cУ этого моба нет бирки` - если у моба нет имени

**Настройки:**
```yaml
name-tag-removal:
  enabled: true
  drop-tag: true # дропать бирку как предмет
  sounds: true
  blacklist: # мобы с которых нельзя снять бирку
    - ENDER_DRAGON
    - WITHER
```

## 📋 Структура реализации (новые фичи)

### Map Locking
```
src/main/java/xyz/lokili/loutils/commands/MapCommand.java
src/main/java/xyz/lokili/loutils/listeners/MapLockListener.java
src/main/resources/conf/map-locking.yml
- Команды /map lock и /map unlock
- NBT теги для хранения UUID владельца
- Обработчик PrepareItemCraftEvent для блокировки копирования
- Action bar сообщения
```

### Frame Locking
```
src/main/java/xyz/lokili/loutils/listeners/FrameLockListener.java
src/main/resources/conf/frame-locking.yml
- PlayerInteractEntityEvent для блокировки/разблокировки
- EntityDamageByEntityEvent для защиты от разрушения
- NBT теги для хранения состояния блокировки
- Частицы и звуки
```

### Enhanced Bone Meal
```
src/main/java/xyz/lokili/loutils/listeners/EnhancedBoneMealListener.java
src/main/resources/conf/enhanced-bone-meal.yml
- PlayerInteractEvent для использования костной муки
- Проверка типа блока (тростник/кактус)
- Рост блоков с проверкой максимальной высоты
- Частицы и звуки
```

### Anvil Repair
```
src/main/java/xyz/lokili/loutils/listeners/AnvilRepairListener.java
src/main/resources/conf/anvil-repair.yml
- PlayerInteractEvent для починки
- Проверка типа наковальни (Chipped/Damaged)
- Замена блока на менее повреждённый
- Частицы и звуки
```

### Name Tag Removal
```
src/main/java/xyz/lokili/loutils/listeners/NameTagRemovalListener.java
src/main/resources/conf/name-tag-removal.yml
- PlayerInteractEntityEvent для снятия бирки
- Проверка наличия кастомного имени
- Дроп бирки с сохранённым именем
- Звуки
```

## 🎯 Обновлённые приоритеты
1. ~~**Высокий:** LightBlock Fix~~ ✅ ГОТОВО
2. ~~**Средний:** Player Poses~~ ✅ ГОТОВО
3. **Средний:** Map Locking (защита мапартов)
4. **Средний:** Frame Locking (защита от гриферов)
5. **Средний:** Enhanced Bone Meal (порт с Bedrock)
6. **Средний:** Anvil Repair (экономия ресурсов)
7. **Низкий:** Name Tag Removal (QoL фича)
8. **Низкий:** Enhanced Hoes
9. **Низкий:** Cow Milking Balance
10. **Низкий:** Auto Replant

## 📝 Дополнительные заметки
- Все сообщения через action bar (не в чат)
- Использовать NBT теги для персистентности данных
- Добавить частицы и звуки для визуального фидбека
- Все модули отключаемые через конфиг
- Тестировать на Folia (региональные задачи)


## 🛠️ Кастомные крафты

### 12. Колокол (Bell)
**Описание:** Крафт колокола в верстаке
**Рецепт:**
```
[Камень]  [Палка]       [Камень]
[Золото]  [Золото]      [Золото]
[Самородок] [Золото]    [Самородок]
```
**Ингредиенты:**
- 2x Stone (камень)
- 1x Stick (палка)
- 4x Gold Ingot (золотой слиток)
- 2x Gold Nugget (золотой самородок)

**Результат:** 1x Bell (колокол)

**Настройки:**
```yaml
custom-crafts:
  bell:
    enabled: true
```

---

### 13. Красный гриб блок (Red Mushroom Block)
**Описание:** Крафт блока красного гриба
**Рецепт:**
```
[Красный гриб] [Красный гриб] [ ]
[ ]            [ ]            [ ]
[Красный гриб] [Красный гриб] [ ]
```
**Ингредиенты:**
- 4x Red Mushroom (красный гриб)

**Результат:** 1x Red Mushroom Block (блок красного гриба)

**Настройки:**
```yaml
custom-crafts:
  red-mushroom-block:
    enabled: true
```

---

### 14. Коричневый гриб блок (Brown Mushroom Block)
**Описание:** Крафт блока коричневого гриба
**Рецепт:**
```
[Коричневый гриб] [Коричневый гриб] [ ]
[ ]               [ ]                [ ]
[Коричневый гриб] [Коричневый гриб] [ ]
```
**Ингредиенты:**
- 4x Brown Mushroom (коричневый гриб)

**Результат:** 1x Brown Mushroom Block (блок коричневого гриба)

**Настройки:**
```yaml
custom-crafts:
  brown-mushroom-block:
    enabled: true
```

---

### 15. Паутина (Cobweb)
**Описание:** Крафт паутины из ниток
**Рецепт:**
```
[Нить] [ ]     [Нить]
[ ]    [Нить] [ ]
[Нить] [ ]     [Нить]
```
**Ингредиенты:**
- 5x String (нить)

**Результат:** 1x Cobweb (паутина)

**Настройки:**
```yaml
custom-crafts:
  cobweb:
    enabled: true
```

---

### 16. Подмостки (Scaffolding) x4
**Описание:** Альтернативный крафт подмостков из палок (вместо бамбука)
**Рецепт:**
```
[Палка] [Нить]  [Палка]
[Палка] [ ]     [Палка]
[Палка] [ ]     [Палка]
```
**Ингредиенты:**
- 6x Stick (палка)
- 1x String (нить)

**Результат:** 4x Scaffolding (подмостки)

**Настройки:**
```yaml
custom-crafts:
  scaffolding:
    enabled: true
    amount: 4 # количество в результате
```

---

### 17. Фейерверк 4 уровня (Flight Duration 4)
**Описание:** Крафт фейерверка с длительностью полёта 4 (кастомный предмет)
**Рецепт:**
```
[Бумага]  [Огненный порошок] [ ]
[Порох]   [Порох]            [Порох]
[ ]       [ ]                [ ]
```
**Ингредиенты:**
- 1x Paper (бумага)
- 1x Blaze Powder (огненный порошок)
- 3x Gunpowder (порох)

**Результат:** 1x Firework Rocket (фейерверк)
- NBT: `Flight: 4` (длительность полёта 4)
- Название: `§eФейерверк §7[§f4§7]`
- Описание: `§7Длительность полёта: §f4`

**Настройки:**
```yaml
custom-crafts:
  firework-level-4:
    enabled: true
    flight-duration: 4
    name-format: "§eФейерверк §7[§f%level%§7]"
    lore:
      - "§7Длительность полёта: §f%level%"
```

---

## 📋 Структура реализации (кастомные крафты)

### Custom Crafts System
```
src/main/java/xyz/lokili/loutils/listeners/CustomCraftsListener.java
src/main/java/xyz/lokili/loutils/managers/CustomCraftManager.java
src/main/resources/conf/custom-crafts.yml
- Регистрация ShapedRecipe для каждого крафта
- PrepareItemCraftEvent для кастомных NBT (фейерверк)
- Отключаемые крафты через конфиг
```

**Особенности:**
- Все крафты регистрируются через Bukkit API
- Фейерверк 4 уровня требует NBT модификацию
- Каждый крафт можно отключить отдельно
- Поддержка кастомных названий и описаний

## 🎯 Обновлённые приоритеты (с крафтами)
1. ~~**Высокий:** LightBlock Fix~~ ✅ ГОТОВО
2. ~~**Средний:** Player Poses~~ ✅ ГОТОВО
3. **Средний:** Custom Crafts (все 6 крафтов)
4. **Средний:** Map Locking
5. **Средний:** Frame Locking
6. **Средний:** Enhanced Bone Meal
7. **Средний:** Anvil Repair
8. **Низкий:** Name Tag Removal
9. **Низкий:** Enhanced Hoes
10. **Низкий:** Cow Milking Balance
11. **Низкий:** Auto Replant


---

# 🏗️ Архитектура проекта (SOLID, KISS, DRY)

## Принципы разработки
- **Максимум 200 строк** на файл
- **Максимум 9 файлов** в одной папке
- **SOLID** - Single Responsibility, Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion
- **KISS** - Keep It Simple, Stupid
- **DRY** - Don't Repeat Yourself

---

## 📁 Структура проекта

```
src/main/java/xyz/lokili/loutils/
│
├── LoUtils.java                          # Главный класс плагина (50 строк)
│
├── api/                                  # Интерфейсы (API)
│   ├── IConfigManager.java               # Управление конфигами
│   ├── IWhitelistManager.java            # Управление вайтлистом
│   ├── IAutoRestartManager.java          # Управление автоперезагрузкой
│   ├── ITPSBarManager.java               # Управление TPS баром
│   ├── IWorldLockManager.java            # Управление блокировкой миров
│   ├── ICustomWorldHeightManager.java    # Управление высотой миров
│   ├── IPoseManager.java                 # Управление позами игроков
│   └── IMapLockManager.java              # Управление блокировкой карт
│
├── core/                                 # Ядро системы
│   ├── DependencyContainer.java          # DI контейнер (150 строк)
│   ├── ConfigValidator.java              # Валидация конфигов (180 строк)
│   └── PluginMetrics.java                # Метрики плагина (100 строк)
│
├── registry/                             # Регистрация компонентов
│   ├── CommandRegistry.java              # Регистрация команд (120 строк)
│   ├── ListenerRegistry.java             # Регистрация листенеров (150 строк)
│   └── RecipeRegistry.java               # Регистрация крафтов (180 строк)
│
├── commands/                             # Команды (макс 9 файлов)
│   ├── base/
│   │   └── BaseCommand.java              # Базовый класс команд (80 строк)
│   ├── SitCommand.java                   # /sit (50 строк)
│   ├── LayCommand.java                   # /lay (50 строк)
│   ├── CrawlCommand.java                 # /crawl (50 строк)
│   ├── PoseStopCommand.java              # /pose stop (40 строк)
│   ├── MapCommand.java                   # /map lock|unlock (120 строк)
│   ├── WhitelistCommand.java             # /whitelist (150 строк)
│   ├── InvSeeCommand.java                # /invsee (100 строк)
│   └── ReloadCommand.java                # /loutils reload (60 строк)
│
├── listeners/                            # Листенеры (разбиты по модулям)
│   ├── base/
│   │   └── BaseListener.java             # Базовый класс листенеров (100 строк)
│   │
│   ├── gameplay/                         # Игровые механики (макс 9)
│   │   ├── PlayerPoseListener.java       # Позы игроков (180 строк)
│   │   ├── EnhancedBoneMealListener.java # Улучшенная костная мука (120 строк)
│   │   ├── AnvilRepairListener.java      # Починка наковален (100 строк)
│   │   ├── NameTagRemovalListener.java   # Снятие бирок (80 строк)
│   │   ├── FastLeafDecayListener.java    # Быстрое гниение листвы (150 строк)
│   │   ├── SleepPercentageListener.java  # Процент сна (100 строк)
│   │   ├── VillagerLeashListener.java    # Поводок для жителей (80 строк)
│   │   └── CauldronListener.java         # Котёл (120 строк)
│   │
│   ├── protection/                       # Защита (макс 9)
│   │   ├── MapLockListener.java          # Блокировка карт (150 строк)
│   │   ├── FrameLockListener.java        # Блокировка рамок (180 строк)
│   │   ├── WorldLockListener.java        # Блокировка миров (120 строк)
│   │   └── WhitelistListener.java        # Вайтлист (100 строк)
│   │
│   ├── blocks/                           # Блоки (макс 9)
│   │   ├── LightBlockListener.java       # Блоки света (150 строк)
│   │   ├── InvisibleFrameListener.java   # Невидимые рамки (180 строк)
│   │   ├── DebugStickListener.java       # Debug stick (120 строк)
│   │   └── CustomWorldHeightListener.java # Кастомная высота (150 строк)
│   │
│   ├── player/                           # Игроки (макс 9)
│   │   ├── PlayerJoinListener.java       # Вход игрока (120 строк)
│   │   ├── DeathMessageListener.java     # Сообщения смерти (100 строк)
│   │   └── InvSeeListener.java           # Просмотр инвентаря (180 строк)
│   │
│   └── crafts/                           # Крафты (макс 9)
│       ├── BellCraftListener.java        # Крафт колокола (80 строк)
│       ├── MushroomBlockCraftListener.java # Крафт грибов (80 строк)
│       ├── CobwebCraftListener.java      # Крафт паутины (80 строк)
│       ├── ScaffoldingCraftListener.java # Крафт подмостков (80 строк)
│       └── FireworkCraftListener.java    # Крафт фейерверка 4 lvl (120 строк)
│
├── managers/                             # Менеджеры (бизнес-логика)
│   ├── config/
│   │   └── ConfigManager.java            # Управление конфигами (180 строк)
│   │
│   ├── pose/                             # Система поз (макс 9)
│   │   ├── PoseManager.java              # Главный менеджер (150 строк)
│   │   ├── PoseType.java                 # Enum типов поз (30 строк)
│   │   ├── PoseData.java                 # Модель данных позы (50 строк)
│   │   ├── PoseConstants.java            # Константы (40 строк)
│   │   ├── SeatFactory.java              # Создание сидений (100 строк)
│   │   ├── StairSeatCalculator.java      # Расчёт позиции на ступеньках (80 строк)
│   │   └── DismountCalculator.java       # Расчёт позиции слезания (80 строк)
│   │
│   ├── map/
│   │   └── MapLockManager.java           # Управление блокировкой карт (150 строк)
│   │
│   ├── whitelist/
│   │   └── WhitelistManager.java         # Управление вайтлистом (180 строк)
│   │
│   ├── autorestart/
│   │   └── AutoRestartManager.java       # Управление автоперезагрузкой (180 строк)
│   │
│   ├── tpsbar/
│   │   └── TPSBarManager.java            # Управление TPS баром (150 строк)
│   │
│   ├── worldlock/
│   │   └── WorldLockManager.java         # Управление блокировкой миров (150 строк)
│   │
│   └── worldheight/
│       └── CustomWorldHeightManager.java # Управление высотой миров (180 строк)
│
├── services/                             # Сервисы (вспомогательная логика)
│   ├── LightParticleService.java         # Частицы для блоков света (150 строк)
│   ├── ConfigLoader.java                 # Загрузка конфигов (120 строк)
│   ├── MessageService.java               # Отправка сообщений (100 строк)
│   └── RecipeService.java                # Регистрация рецептов (180 строк)
│
├── utils/                                # Утилиты (макс 9)
│   ├── MessageUtil.java                  # Утилиты сообщений (150 строк)
│   ├── ColorUtil.java                    # Утилиты цветов (80 строк)
│   ├── SchedulerUtil.java                # Утилиты планировщика (150 строк)
│   ├── ItemUtil.java                     # Утилиты предметов (120 строк)
│   ├── LocationUtil.java                 # Утилиты локаций (100 строк)
│   ├── NBTUtil.java                      # Утилиты NBT (150 строк)
│   └── PermissionUtil.java               # Утилиты прав (80 строк)
│
├── constants/                            # Константы (макс 9)
│   ├── ConfigConstants.java              # Константы конфигов (100 строк)
│   ├── PermissionConstants.java          # Константы прав (80 строк)
│   ├── MessageConstants.java             # Константы сообщений (120 строк)
│   └── GameplayConstants.java            # Константы геймплея (100 строк)
│
└── placeholders/
    └── LoUtilsExpansion.java             # PlaceholderAPI (150 строк)
```

---

## 📋 Конфигурационные файлы

```
src/main/resources/
│
├── plugin.yml                            # Описание плагина
├── config.yml                            # Главный конфиг
├── messages.yml                          # Сообщения
│
├── conf/                                 # Модульные конфиги
│   ├── autorestart.yml
│   ├── cauldron.yml
│   ├── customworldheight.yml
│   ├── deathmessages.yml
│   ├── debug-stick.yml
│   ├── enchant.yml
│   ├── fastleafdecay.yml
│   ├── invisible-frames.yml
│   ├── light-block.yml
│   ├── performance.yml
│   ├── poses.yml
│   ├── sleeppercentage.yml
│   ├── tpsbar.yml
│   ├── villagerleash.yml
│   ├── whitelist.yml
│   ├── worldlock.yml
│   ├── map-locking.yml
│   ├── frame-locking.yml
│   ├── enhanced-bone-meal.yml
│   ├── anvil-repair.yml
│   ├── name-tag-removal.yml
│   └── custom-crafts.yml
│
└── data/                                 # Данные
    └── whitelist.yml
```

---

## 🎯 Принципы организации кода

### 1. Single Responsibility Principle (SRP)
- Каждый класс отвечает за одну задачу
- Листенеры только слушают события
- Менеджеры только управляют логикой
- Сервисы только предоставляют вспомогательные функции

### 2. Open/Closed Principle (OCP)
- Базовые классы: `BaseListener`, `BaseCommand`
- Расширение через наследование, а не модификацию
- Интерфейсы для всех менеджеров

### 3. Liskov Substitution Principle (LSP)
- Все листенеры наследуют `BaseListener`
- Все команды наследуют `BaseCommand`
- Можно заменить любой менеджер на его интерфейс

### 4. Interface Segregation Principle (ISP)
- Интерфейсы в папке `api/`
- Каждый интерфейс содержит только нужные методы
- Нет "толстых" интерфейсов

### 5. Dependency Inversion Principle (DIP)
- `DependencyContainer` управляет зависимостями
- Классы зависят от интерфейсов, а не от реализаций
- Инъекция зависимостей через конструктор

### 6. KISS (Keep It Simple, Stupid)
- Простые и понятные имена классов
- Минимум вложенности (макс 3 уровня)
- Один метод = одна задача

### 7. DRY (Don't Repeat Yourself)
- Общая логика в базовых классах
- Утилиты для повторяющихся операций
- Константы вместо магических чисел

---

## 📊 Метрики качества кода

### Ограничения на файл:
- Максимум 200 строк кода
- Максимум 20 методов
- Максимум 5 полей
- Максимум 3 уровня вложенности

### Ограничения на папку:
- Максимум 9 файлов в одной папке
- Если больше - создать подпапки

### Ограничения на метод:
- Максимум 30 строк
- Максимум 5 параметров
- Максимум 3 уровня вложенности

---

## 🔄 Процесс разработки новой фичи

### Шаг 1: Создать интерфейс (если нужен менеджер)
```java
// api/IMapLockManager.java
public interface IMapLockManager {
    boolean lockMap(ItemStack map, Player owner);
    boolean unlockMap(ItemStack map, Player player);
    boolean isLocked(ItemStack map);
    UUID getOwner(ItemStack map);
}
```

### Шаг 2: Создать менеджер (бизнес-логика)
```java
// managers/map/MapLockManager.java (макс 150 строк)
public class MapLockManager implements IMapLockManager {
    // Логика блокировки карт
}
```

### Шаг 3: Создать листенер (обработка событий)
```java
// listeners/protection/MapLockListener.java (макс 180 строк)
public class MapLockListener extends BaseListener {
    private final IMapLockManager mapLockManager;
    
    // Обработка событий
}
```

### Шаг 4: Создать команду (если нужна)
```java
// commands/MapCommand.java (макс 120 строк)
public class MapCommand extends BaseCommand {
    private final IMapLockManager mapLockManager;
    
    // Обработка команд
}
```

### Шаг 5: Создать конфиг
```yaml
# conf/map-locking.yml
enabled: true
prevent-copying: true
lore-format: "§7Заблокировано: §e%player%"
```

### Шаг 6: Зарегистрировать в Registry
```java
// registry/ListenerRegistry.java
pm.registerEvents(new MapLockListener(plugin, configManager, mapLockManager), plugin);

// registry/CommandRegistry.java
plugin.getCommand("map").setExecutor(new MapCommand(plugin, mapLockManager));
```

---

## ✅ Чеклист перед коммитом

- [ ] Файл не больше 200 строк
- [ ] В папке не больше 9 файлов
- [ ] Нет дублирования кода (DRY)
- [ ] Класс отвечает за одну задачу (SRP)
- [ ] Используются интерфейсы (DIP)
- [ ] Код простой и понятный (KISS)
- [ ] Есть JavaDoc для публичных методов
- [ ] Нет магических чисел (используются константы)
- [ ] Нет вложенности больше 3 уровней
- [ ] Методы не больше 30 строк

---

## 🎓 Примеры хорошего кода

### ✅ Хорошо (SOLID, KISS, DRY)
```java
// Простой, понятный, один класс = одна задача
public class MapLockListener extends BaseListener {
    private final IMapLockManager manager;
    
    @EventHandler
    public void onMapCraft(PrepareItemCraftEvent event) {
        if (!checkEnabled()) return;
        
        ItemStack result = event.getInventory().getResult();
        if (result == null || result.getType() != Material.FILLED_MAP) return;
        
        if (manager.isLocked(result)) {
            event.getInventory().setResult(null);
        }
    }
}
```

### ❌ Плохо (нарушает принципы)
```java
// Слишком много ответственности, нет разделения
public class MapListener implements Listener {
    @EventHandler
    public void onMapCraft(PrepareItemCraftEvent event) {
        ItemStack result = event.getInventory().getResult();
        if (result != null && result.getType() == Material.FILLED_MAP) {
            ItemMeta meta = result.getItemMeta();
            if (meta != null && meta.getPersistentDataContainer().has(new NamespacedKey(plugin, "locked"), PersistentDataType.STRING)) {
                String owner = meta.getPersistentDataContainer().get(new NamespacedKey(plugin, "locked"), PersistentDataType.STRING);
                if (owner != null && !owner.isEmpty()) {
                    event.getInventory().setResult(null);
                    // ... ещё 150 строк логики
                }
            }
        }
    }
}
```

---

Эта структура обеспечивает:
- ✅ Читаемость кода
- ✅ Лёгкость поддержки
- ✅ Простоту тестирования
- ✅ Масштабируемость
- ✅ Соблюдение best practices


---

# ⏱️ Оценка времени реализации

## Приоритет 1: Кастомные крафты (3-4 часа)
- ✅ Колокол - 30 мин
- ✅ Красный гриб блок - 20 мин
- ✅ Коричневый гриб блок - 20 мин
- ✅ Паутина - 20 мин
- ✅ Подмостки x4 - 20 мин
- ✅ Фейерверк 4 уровня (с NBT) - 1 час
- ✅ Тестирование - 1 час

## Приоритет 2: Map Locking (4-5 часов)
- Команды /map lock|unlock - 1 час
- MapLockManager с NBT - 1.5 часа
- MapLockListener (блокировка копирования) - 1 час
- Конфиг и сообщения - 30 мин
- Тестирование - 1 час

## Приоритет 3: Frame Locking (3-4 часа)
- FrameLockListener (мёд/кисть) - 1.5 часа
- Защита от взаимодействия - 1 час
- Частицы и звуки - 30 мин
- Конфиг - 30 мин
- Тестирование - 30 мин

## Приоритет 4: Enhanced Bone Meal (2-3 часа)
- EnhancedBoneMealListener - 1 час
- Логика роста тростника/кактуса - 1 час
- Частицы и звуки - 30 мин
- Конфиг - 30 мин

## Приоритет 5: Anvil Repair (2-3 часа)
- AnvilRepairListener - 1 час
- Логика починки - 1 час
- Частицы и звуки - 30 мин
- Конфиг - 30 мин

## Приоритет 6: Name Tag Removal (2 часа)
- NameTagRemovalListener - 1 час
- Дроп бирки с именем - 30 мин
- Конфиг - 30 мин

## Приоритет 7: Enhanced Hoes (4-5 часов)
- EnhancedHoeListener - 2 часа
- Расчёт дропа по типу мотыги - 1 час
- Конфиг с множителями - 1 час
- Тестирование - 1 час

## Приоритет 8: Cow Milking Balance (3-4 часа)
- CowMilkingListener - 1.5 часа
- CowCooldownManager с NBT - 1 час
- Визуальные эффекты - 1 час
- Конфиг - 30 мин

## Приоритет 9: Auto Replant (5-6 часов)
- Кастомное зачарование - 2 часа
- AutoReplantListener - 2 часа
- Логика автопосадки - 1 час
- Конфиг - 1 час

---

## 📊 Итоговая оценка

**Всего времени:** 28-36 часов чистой работы

**По приоритетам:**
- Высокий (крафты, map/frame locking): 10-13 часов
- Средний (bone meal, anvil, name tag): 6-8 часов
- Низкий (hoes, cow, replant): 12-15 часов

**Реалистичный срок:** 1-2 недели при работе по 3-4 часа в день

---

# 🚀 Начинаем реализацию

## Текущая задача: Кастомные крафты (Приоритет 1)

Начинаю с самого простого - крафты. Они не требуют сложной логики и быстро реализуются.
