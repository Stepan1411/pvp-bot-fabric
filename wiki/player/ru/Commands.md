# 🎮 Команды

Все команды PVP-бота начинаются с `/pvpbot`. Требуется уровень разрешений 2 (оператор).

---

## 📋 Содержание

- [Управление ботами](#-bot-management)
- [Боевые команды](#-боевые-команды)
- [Команды фракции](#-faction-commands)
- [Команды набора](#-kit-commands)
- [Команды пути](#-команды пути)
- [Настройки](#-настройки)

---

## 🤖 Управление ботами

| Команда | Описание |
|---------|-------------|
| `/pvpbot spawn [имя]` | Создать нового бота (случайное имя, если не указано) |
| `/pvpbot Massspawn <count>` | Создавать несколько ботов со случайными именами (1-50) |
| `/pvpbot удалить <имя>` | Удалить бота |
| `/pvpbot удалить все` | Удалить всех ботов |
| `/pvpbot список` | Список всех активных ботов |
| `/pvpbot инвентарь <имя>` | Показать инвентарь бота |

### Примеры

```mcfunction
# Create a bot with random name
/pvpbot spawn

# Create a bot named "Guard1"
/pvpbot spawn Guard1

# Spawn 10 bots with random names (10% chance for special names)
/pvpbot massspawn 10

# Remove the bot
/pvpbot remove Guard1

# See all bots
/pvpbot list
```

**Примечание.** При создании ботов со случайными именами существует 10 % вероятность, что они получат специальное имя («nantag» или «Stepan1411») вместо сгенерированного имени.

---

## ⚔️ Боевые команды

| Команда | Описание |
|---------|-------------|
| `/pvpbot Attack <бот> <цель>` | Приказать боту атаковать игрока/сущность |
| `/pvpbot остановить <бот>` | Не дать боту атаковать |
| `/pvpbot target <бот>` | Показать текущую цель бота |

### Примеры

```mcfunction
# Make Bot1 attack player Steve
/pvpbot attack Bot1 Steve

# Stop the attack
/pvpbot stop Bot1

# Check who Bot1 is targeting
/pvpbot target Bot1
```

---

## 👥 Команды фракций

| Команда | Описание |
|---------|-------------|
| `/pvpbot фракция создать <имя>` | Создать новую фракцию |
| `/pvpbot фракция удалить <имя>` | Удалить фракцию |
| `/pvpbot фракция добавить <фракцию> <игрок>` | Добавить игрока/бота во фракцию |
| `/pvpbot фракция удалить <фракцию> <игрок>` | Удалить из фракции |
| `/pvpbot фракция враждебная <f1> <f2> [true/false]` | Установить фракции как враждебные |
| `/pvpbot фракция addnear <фракция> <радиус>` | Добавить всех ближайших ботов |
| `/pvpbot фракция дать <фракцию> <предмет>` | Раздать предмет всем участникам |
| `/pvpbot фракция Givekit <фракция> <комплект>` | Раздайте комплект всем участникам |
| `/pvpbot Fraction Attack <фракция> <цель>` | Все боты фракции атакуют цель |
| `/pvpbot список фракций` | Список всех фракций |
| `/pvpbot информация о фракции <фракция>` | Показать детали фракции |

### Примеры

```mcfunction
# Create two factions
/pvpbot faction create RedTeam
/pvpbot faction create BlueTeam

# Add bots to factions
/pvpbot faction add RedTeam Bot1
/pvpbot faction add BlueTeam Bot2

# Make them enemies
/pvpbot faction hostile RedTeam BlueTeam

# Order entire faction to attack
/pvpbot faction attack RedTeam Steve

# Give swords to everyone in RedTeam
/pvpbot faction give RedTeam diamond_sword
```

---

## 🎒 Команды набора

| Команда | Описание |
|---------|-------------|
| `/pvpbot createkit <имя>` | Создать комплект из своего инвентаря |
| `/pvpbot deletekit <имя>` | Удалить комплект |
| `/pvpbot комплекты` | Список всех комплектов |
| `/pvpbot Givekit <bot> <kit>` | Отдать комплект боту |
| `/pvpbot фракция Givekit <фракция> <комплект>` | Отдать комплект фракции |

### Примеры

```mcfunction
# Put items in your inventory, then:
/pvpbot createkit warrior

# Give kit to a bot
/pvpbot givekit Bot1 warrior

# Give kit to entire faction
/pvpbot faction givekit RedTeam warrior
```

---

## 🛤️ Команды пути

| Команда | Описание |
|---------|-------------|
| `/pvpbot путь создания <имя>` | Создать новый путь |
| `/pvpbot path delete <имя>` | Удалить путь |
| `/pvpbot путь add <имя>` | Добавить текущую позицию в качестве путевой точки |
| `/pvpbot path удалить <имя> <индекс>` | Удалить путевую точку по индексу |
| `/pvpbot путь очистить <имя>` | Удалить все путевые точки |
| `/pvpbot список путей` | Список всех путей |
| `/pvpbot информация о пути <имя>` | Показать информацию о пути |
| `/pvpbot путь следовать <bot> <path>` | Заставить бота следовать по пути |
| `/pvpbot путь остановки <bot>` | Не дать боту следовать по пути |
| `/pvpbot path Loop <имя> <true/false>` | Переключить режим цикла |
| `/pvpbot path Attack <имя> <true/false>` | Переключить боевой режим |
| `/pvpbot path show <имя> <true/false>` | Переключить визуализацию пути |

### Примеры

```mcfunction
# Create a patrol route
/pvpbot path create patrol

# Add waypoints (stand at each location)
/pvpbot path add patrol
/pvpbot path add patrol
/pvpbot path add patrol

# Make bot follow the path
/pvpbot path follow Guard1 patrol

# Enable back-and-forth movement
/pvpbot path loop patrol true

# Disable combat while patrolling
/pvpbot path attack patrol false

# Show path with particles
/pvpbot path show patrol true
```

Подробное руководство см. на странице [Пути](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Paths).

---

## ⚙️ Настройки

Используйте `/pvpbot settings`, чтобы увидеть все текущие настройки.

Используйте `/pvpbot settings <setting>`, чтобы увидеть текущее значение.

Используйте `/pvpbot settings <setting> <value>`, чтобы изменить настройку.

Полный список всех настроек см. на странице [Настройки](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Settings).

### Быстрые примеры

```mcfunction
# Enable auto-targeting
/pvpbot settings autotarget true

# Set miss chance to 20%
/pvpbot settings misschance 20

# Enable bunny hop
/pvpbot settings bhop true
```
