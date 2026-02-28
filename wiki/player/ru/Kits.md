# 🎒 Система комплектов

Сохраняйте пресеты экипировки и быстро экипируйте ботов!

---

## 📖 Обзор

Комплекты позволяют:
- Сохраните текущий инвентарь в качестве шаблона.
- Быстро отдать экипировку ботам
- Экипируйте целые фракции одновременно

---

## 📦 Создание комплектов

1. Положите предметы в инвентарь (броня, оружие, еда и т.д.)
2. Запустите команду создания.

```mcfunction
/pvpbot createkit <name>
```

### Что сохраняется
- ✅ Предметы горячей панели (слоты 0–8)
- ✅ Предметы инвентаря
- ✅ Части брони
- ✅ Дополнительный предмет
- ✅ Зачарования предметов
- ✅ Долговечность предмета
- ✅ Размеры стека

### Пример
```mcfunction
# Put diamond armor, sword, bow, arrows, golden apples in your inventory
# Then save it:
/pvpbot createkit pvp_warrior
```

---

## 📋 Управление комплектами

### Список комплектов
```mcfunction
/pvpbot kits
```

### Удалить комплект
```mcfunction
/pvpbot deletekit pvp_warrior
```

---

## 🎁 Наборы для подарков

### Для одного бота
```mcfunction
/pvpbot givekit Bot1 pvp_warrior
```

### Для всей фракции
```mcfunction
/pvpbot faction givekit RedTeam pvp_warrior
```

---

## 💡 Идеи комплектов

### ⚔️ Боец ближнего боя
- Алмазный/Нетеритовый меч
- Полная алмазная броня
- Щит
- Золотые яблоки
- Тотем бессмертия (вторую руку)

### 🏹 Арчер
- Лук (Сила V, Бесконечность)
- Стрела (1 стак)
- Кожа/Кольчуга
- Золотые яблоки

### 🔨 Танк
- Незеритовая броня (Защита IV)
- Щит
- Топор (для взлома щита)
- Много золотых яблок
- Несколько тотемов

### 💨 Скоростной боец
- Легкая броня (кожа/цепь)
- Алмазный меч (Резкость V)
- Зелья скорости
- Золотые яблоки

---

## 📋 Полный пример

```mcfunction
# Step 1: Prepare your inventory with items you want

# Step 2: Create the kit
/pvpbot createkit soldier

# Step 3: Spawn bots
/pvpbot spawn Soldier1
/pvpbot spawn Soldier2
/pvpbot spawn Soldier3

# Step 4: Give kit to all bots
/pvpbot givekit Soldier1 soldier
/pvpbot givekit Soldier2 soldier
/pvpbot givekit Soldier3 soldier

# Or create a faction and give kit to all at once:
/pvpbot faction create Army
/pvpbot faction add Army Soldier1
/pvpbot faction add Army Soldier2
/pvpbot faction add Army Soldier3
/pvpbot faction givekit Army soldier
```

---

## 💾 Хранение данных

Данные комплекта сохраняются в:
```
config/pvp_bot_kits.json
```

Комплекты сохраняются после перезапуска сервера.

---

## ⚠️ Заметки

- Боты будут автоматически экипировать броню из комплекта
- Боты автоматически выберут лучшее оружие
- Существующие предметы в инвентаре бота НЕ удаляются.
- Если инвентарь бота заполнен, некоторые предметы могут не выдаваться
