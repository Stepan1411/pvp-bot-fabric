# 🛤️ Система путей

Система путей позволяет создавать заранее определенные маршруты, по которым будут следовать боты. Боты могут патрулировать территории, перемещаться между локациями и, при необходимости, участвовать в бою, следуя по путям.

---

## 📋 Содержание

- [Обзор](#обзор)
- [Создание путей](#creating-paths)
- [Управление путевыми точками](#managing-waypoints)
- [Управление ботом](#bot-control)
- [Настройки пути](#path-settings)
- [Визуализация](#визуализация)
- [Примеры](#examples)

---

## 🎯 Обзор

Пути — это последовательности путевых точек, по которым могут следовать боты. Каждый путь имеет:
- **Имя** – Уникальный идентификатор.
- **Путевые точки** - Список позиций (x, y, z)
- **Режим цикла**. Как бот перемещается по путевым точкам.
- **Режим атаки** - останавливается ли бот для боя.
- **Визуализация** — Эффекты частиц, показывающие путь.

Пути сохраняются для каждого мира в `config/pvpbot/worlds/{world}/paths.json`

---

## 🆕 Создание путей

### Создайте новый путь
```
/pvpbot path create <name>
```
Создает пустой путь с заданным именем.

**Пример:**
```
/pvpbot path create patrol_route
```

### Удалить путь
```
/pvpbot path delete <name>
```
Удаляет путь и останавливает всех ботов, следующих по нему.

**Пример:**
```
/pvpbot path delete patrol_route
```

### Вывести список всех путей
```
/pvpbot path list
```
Показывает все доступные пути в текущем мире.

### Просмотр сведений о пути
```
/pvpbot path info <name>
```
Отображает информацию о пути:
- Количество путевых точек
- Статус режима цикла
- Статус режима атаки
- Список всех координат путевых точек

**Пример:**
```
/pvpbot path info patrol_route
```

---

## 📍 Управление путевыми точками

### Добавить путевую точку
```
/pvpbot path add <name>
```
Добавляет вашу текущую позицию в качестве новой путевой точки на путь.

**Пример:**
```
/pvpbot path add patrol_route
```
Встаньте в каждом месте, которое вы хотите, чтобы бот посетил, и выполните эту команду.

### Удалить путевую точку
```
/pvpbot path remove <name> <index>
```
Удаляет конкретную путевую точку по ее индексу (начиная с 0).

**Пример:**
```
/pvpbot path remove patrol_route 2
```
Удаляет третью путевую точку с пути.

### Очистить все путевые точки
```
/pvpbot path clear <name>
```
Удаляет все путевые точки с пути (сохраняет сам путь).

**Пример:**
```
/pvpbot path clear patrol_route
```

---

## 🤖 Управление ботами

### Начать следовать по пути
```
/pvpbot path follow <bot> <path>
```
Заставляет бота следовать по указанному пути.

**Пример:**
```
/pvpbot path follow Guard1 patrol_route
```

### Прекратить следовать по пути
```
/pvpbot path stop <bot>
```
Не позволяет боту следовать по текущему пути.

**Пример:**
```
/pvpbot path stop Guard1
```

---

## ⚙️ Настройки пути

### Режим цикла
```
/pvpbot path loop <name> <true/false>
```

Управляет перемещением бота по путевым точкам:
- **false** (по умолчанию) - Круговой: 1→2→3→1→2→3...
- **true** - Туда-сюда: 1→2→3→2→1→2→3...

**Пример:**
```
/pvpbot path loop patrol_route true
```

### Режим атаки
```
/pvpbot path attack <name> <true/false>
```

Управляет боевым поведением при следовании по пути:
- **true** (по умолчанию) — бот останавливается на текущей точке маршрута, чтобы сражаться, а затем продолжает бой.
- **false** — бот игнорирует бой и продолжает двигаться (BotCombat отключен)

**Пример:**
```
/pvpbot path attack patrol_route false
```

---

## 👁️ Визуализация

### Переключить отображение пути
```
/pvpbot path show <name> <true/false>
```

Показывает/скрывает эффекты частиц для пути:
- **Точки** — Частицы воска в каждой точке.
- **Линии** — Точки соединения частиц зеленой пыли.

Визуализация автоматически включается, когда:
- Создание пути
- Добавление путевой точки
- Начинаем следовать по пути

**Пример:**
```
/pvpbot path show patrol_route true
```

Чтобы отключить визуализацию:
```
/pvpbot path show patrol_route false
```

---

## 💡 Примеры

### Основной маршрут патрулирования
```
# Create path
/pvpbot path create base_patrol

# Add waypoints (stand at each location)
/pvpbot path add base_patrol  # Point 1
/pvpbot path add base_patrol  # Point 2
/pvpbot path add base_patrol  # Point 3
/pvpbot path add base_patrol  # Point 4

# Make bot follow
/pvpbot path follow Guard1 base_patrol
```

### Страж с боем
```
# Create path
/pvpbot path create guard_post

# Add waypoints
/pvpbot path add guard_post  # Position 1
/pvpbot path add guard_post  # Position 2

# Enable back-and-forth movement
/pvpbot path loop guard_post true

# Enable combat (default, but explicit)
/pvpbot path attack guard_post true

# Assign bot
/pvpbot path follow Guard1 guard_post
```

### Мирный курьер
```
# Create path
/pvpbot path create delivery_route

# Add waypoints
/pvpbot path add delivery_route  # Start
/pvpbot path add delivery_route  # Checkpoint 1
/pvpbot path add delivery_route  # Checkpoint 2
/pvpbot path add delivery_route  # End

# Disable combat (bot won't fight)
/pvpbot path attack delivery_route false

# Assign bot
/pvpbot path follow Courier1 delivery_route
```

### Несколько ботов на одном пути
```
# Create path
/pvpbot path create wall_patrol

# Add waypoints
/pvpbot path add wall_patrol  # Corner 1
/pvpbot path add wall_patrol  # Corner 2
/pvpbot path add wall_patrol  # Corner 3
/pvpbot path add wall_patrol  # Corner 4

# Assign multiple bots
/pvpbot path follow Guard1 wall_patrol
/pvpbot path follow Guard2 wall_patrol
/pvpbot path follow Guard3 wall_patrol
```

---

## 📝 Заметки

- Пути сохраняются автоматически при изменении.
- В каждом мире есть свой набор путей
- Боты смотрят на следующую точку во время движения.
- Когда режим атаки активен, боты возвращаются к той точке, куда они направлялись после боя.
- Визуализация пути видна всем игрокам.
- Боты достигают путевой точки, когда находятся в пределах 1,5 блоков от нее.

---

## 🔗 Похожие страницы

- [Команды](Commands.md) - Все доступные команды
- [Навигация](Navigation.md) - Настройки движения бота
- [Бой](Combat.md) - Детали боевой системы
