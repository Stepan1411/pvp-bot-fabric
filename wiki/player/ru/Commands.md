# Команды

Все команды используют`/pvpbot`префикс.

## Управление ботами

| Команда | Описание |
|---------|-------------|
| `/pvpbot spawn [name]`| Создать бота (случайное имя, если опущено) |
| `/pvpbot remove <name>`| Удалить конкретного бота |
| `/pvpbot removeall`| Удалить всех ботов |
| `/pvpbot reload`| Перезагрузить все конфигурации (настройки, комплекты, пути, боты) |
| `/pvpbot bot-management list`| Список всех активных ботов |
| `/pvpbot bot-management inventory <botname>`| Просмотр инвентаря и статистики ботов |
| `/pvpbot bot-management mass-spawn <1-50>`| Создать несколько ботов |

## Боевой контроль

| Команда | Описание |
|---------|-------------|
| `/pvpbot bot-management attack <botname> <target>`| Заставить бота атаковать цель |
| `/pvpbot bot-management stop-attack <botname>`| Хватит атаковать |

## Настройки

| Команда | Описание |
|---------|-------------|
| `/pvpbot settings`| Список всех текущих настроек |
| `/pvpbot settings <name>`| Просмотр конкретной настройки |
| `/pvpbot settings <name> <value>`| Установите значение параметра |

См. [Настройки](Настройки) для просмотра всех доступных опций.

## Пути

| Команда | Описание |
|---------|-------------|
| `/pvpbot bot-management path create <name>`| Создать новый путь |
| `/pvpbot bot-management path delete <name>`| Удалить путь |
| `/pvpbot bot-management path add-point <name>`| Добавить текущую позицию в качестве путевой точки |
| `/pvpbot bot-management path remove-point <name> [index]`| Удалить точку (последнюю или по индексу) |
| `/pvpbot bot-management path clear <name>`| Очистить все точки |
| `/pvpbot bot-management path loop <name> <true/false>`| Переключить цикл |
| `/pvpbot bot-management path start <bot> <path>`| Запустить бота по пути |
| `/pvpbot bot-management path stop <bot>`| Остановить бота, следуя по пути |
| `/pvpbot bot-management path list`| Список всех путей |
| `/pvpbot bot-management path show <name> <true/false>`| Переключить визуализацию пути |
| `/pvpbot bot-management path info <name>`| Посмотреть детали пути |
| `/pvpbot bot-management path distribute <path>`| Распределите ботов равномерно по пути |
| `/pvpbot bot-management path start-near <path> <radius>`| Начальный путь для ближайших ботов |
| `/pvpbot bot-management path stop-all <path>`| Остановить всех ботов на пути |
| `/pvpbot bot-management path walk-type <name> <type>`| Установить тип ходьбы (подпрыгивание/спринт/ходьба) |

Подробную информацию об использовании см. в разделе [Пути](Пути).

## Комплекты

| Команда | Описание |
|---------|-------------|
| `/pvpbot kit create-kit <name>`| Сохраните свой инвентарь как комплект |
| `/pvpbot kit delete-kit <name>`| Удалить комплект |
| `/pvpbot kit give-kit <player> <kitname>`| Отдать комплект игроку/боту |
| `/pvpbot kit kits`| Список всех комплектов |
| `/pvpbot kit give-kit-near <kitname> [radius]`| Раздать комплект ботам в радиусе действия (по умолчанию: 10) |
| `/pvpbot kit give-kit-near-random <radius> <kit1> <w1>% [<kit2> <w2>% ...]`| Выдать случайно взвешенный комплект ботам в радиусе |

Подробную информацию об использовании см. в разделе [Наборы](Наборы).

## Фракции

| Команда | Описание |
|---------|-------------|
| `/pvpbot faction list`| Список всех фракций |
| `/pvpbot faction create <name>`| Создать фракцию |
| `/pvpbot faction delete <name>`| Удалить фракцию |
| `/pvpbot faction add <faction> <player>`| Добавить игрока/бота во фракцию |
| `/pvpbot faction remove <faction> <player>`| Удалить игрока/бота из фракции |
| `/pvpbot faction hostile <f1> <f2> [true/false]`| Установить враждебные отношения |
| `/pvpbot faction info <name>`| Посмотреть информацию о фракции |
| `/pvpbot faction add-near <faction> <radius>`| Добавить ближайших ботов во фракцию |
| `/pvpbot faction add-all <faction>`| Добавить всех ботов во фракцию |
| `/pvpbot faction give <faction> <item>`| Раздайте предметы всем участникам |
| `/pvpbot faction attack <faction> <target>`| Все участники атакуют цель |
| `/pvpbot faction path start <faction> <path>`| Все участники следуют по пути |
| `/pvpbot faction path stop <faction>`| Остановить всех участников на пути |
| `/pvpbot faction tp <faction> <x y z\|player>`| Постепенно телепортируйте всю фракцию |

### Команды набора фракций

| Команда | Описание |
|---------|-------------|
| `/pvpbot faction kit give-kit <faction> <kitname>`| Раздайте комплект всем участникам |
| `/pvpbot faction kit give-kit-random <faction> <kit1> <w1>% [<kit2> <w2>% ...]`| Раздайте членам фракции случайно взвешенный комплект |

Подробное описание см. в разделе [Фракции](Фракции).
