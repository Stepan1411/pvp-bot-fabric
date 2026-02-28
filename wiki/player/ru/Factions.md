# 👥 Система фракций

Организуйте ботов и игроков в команды, которые смогут сражаться друг с другом!

---

## 📖 Обзор

Фракции — это группы ботов и игроков. Ты можешь:
- Создавайте команды ботов
- Установить фракции как враждебные друг другу
- Боты автоматически атакуют врагов враждебных фракций.

---

## 🏗️ Создание фракций

```mcfunction
# Create a faction
/pvpbot faction create RedTeam

# Delete a faction
/pvpbot faction delete RedTeam

# List all factions
/pvpbot faction list

# Get faction info
/pvpbot faction info RedTeam
```

---

## 👤 Управление участниками

### Добавить участников
```mcfunction
# Add a bot to faction
/pvpbot faction add RedTeam Bot1

# Add a player to faction
/pvpbot faction add RedTeam Steve

# Add all nearby bots (within 20 blocks)
/pvpbot faction addnear RedTeam 20
```

### Удаление участников
```mcfunction
/pvpbot faction remove RedTeam Bot1
```

---

## ⚔️ Враждебные отношения

Сделайте фракции врагами — их члены будут автоматически атаковать друг друга!

```mcfunction
# Make factions hostile
/pvpbot faction hostile RedTeam BlueTeam

# Make factions neutral again
/pvpbot faction hostile RedTeam BlueTeam false
```

### Как это работает
1. Бот от RedTeam видит игрока/бота от BlueTeam
2. Если фракции враждебны, бот автоматически нацеливается на них.
3. Бой начинается!

> **Примечание.** Для автоматического таргетинга необходимо включить автотаргетинг.

---

## 🎁 Дарение предметов

### Отдать предметы
```mcfunction
# Give diamond sword to all faction members
/pvpbot faction give RedTeam diamond_sword

# Give multiple items
/pvpbot faction give RedTeam diamond_sword 1
/pvpbot faction give RedTeam golden_apple 16
```

### Дарите наборы
```mcfunction
# Give a saved kit to entire faction
/pvpbot faction givekit RedTeam warrior
```

---

## 📋 Полный пример

Создайте две команды и заставьте их сражаться:

```mcfunction
# Create bots
/pvpbot spawn Red1
/pvpbot spawn Red2
/pvpbot spawn Red3
/pvpbot spawn Blue1
/pvpbot spawn Blue2
/pvpbot spawn Blue3

# Create factions
/pvpbot faction create Red
/pvpbot faction create Blue

# Add bots to factions
/pvpbot faction add Red Red1
/pvpbot faction add Red Red2
/pvpbot faction add Red Red3
/pvpbot faction add Blue Blue1
/pvpbot faction add Blue Blue2
/pvpbot faction add Blue Blue3

# Make them enemies
/pvpbot faction hostile Red Blue

# Give equipment
/pvpbot faction give Red diamond_sword
/pvpbot faction give Blue diamond_sword
/pvpbot faction give Red diamond_chestplate
/pvpbot faction give Blue diamond_chestplate

# Enable auto-targeting
/pvpbot settings autotarget true

# Watch the battle!
```

---

## ⚙️ Настройки

```mcfunction
# Enable/disable faction system
/pvpbot settings factions true
```

---

## 💾 Хранение данных

Данные фракции сохраняются в:
```
config/pvp_bot_factions.json
```

Этот файл сохраняется после перезапуска сервера.
