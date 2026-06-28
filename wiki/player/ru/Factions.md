# Фракции

Организуйте ботов в команды/фракции для скоординированных сражений. Фракции сохраняются для каждого мира в`config/pvpbot/worlds/<worldname>/factions.json`.

## Основные команды

### Создание и удаление
```
/pvpbot faction create Red
/pvpbot faction delete Red
```

### Управление участниками
```
/pvpbot faction add Red Bot1
/pvpbot faction remove Red Bot1
/pvpbot faction add-near Red 30          # Add all bots within 30 blocks
/pvpbot faction add-all Red              # Add all existing bots
```

### Проверить информацию
```
/pvpbot faction list              # List all factions
/pvpbot faction info Red          # Show members and enemies
```

## Враждебные отношения

Назначьте фракции врагами. Боты будут автоматически атаковать членов вражеской фракции.

```
/pvpbot faction hostile Red Blue
/pvpbot faction hostile Red Neutral false
```

**Примечание.** Враги всегда взаимны: установка красного враждебного синему также делает синего враждебным красному.

## Скоординированные действия

### Атака
Все члены фракции атакуют цель:
```
/pvpbot faction attack Red GreenPlayer
```

### Отдать предметы
Раздайте предметы всем членам фракции:
```
/pvpbot faction give Red diamond_sword 1
```

### Отдать комплект
Обеспечьте всех участников комплектом:
```
/pvpbot faction kit give-kit Red MyKit
```

### Выдать случайный комплект
Снабдите членов фракции случайным взвешенным комплектом:
```
/pvpbot faction kit give-kit-random Red warrior 60% archer 30% mage 10%
```

Каждый участник получает один комплект в зависимости от распределения веса.

### Телепорт
Телепортируйте всю фракцию постепенно (5 ботов за 100 мс):
```
/pvpbot faction tp Red 100 64 -200
/pvpbot faction tp Red ~ ~ ~
/pvpbot faction tp Red PlayerName
```

Поддерживает абсолютные координаты, относительные`~`или нацеливаясь на игрока/бота по имени.

### Следование по пути
Все участники следуют по пути:
```
/pvpbot faction path start Red MyPath
/pvpbot faction path stop Red
```

## Настройки

| Настройка | По умолчанию | Описание |
|---------|---------|-------------|
| фракции | правда | Включить систему фракций |
| дружественный огонь | ложный | Разрешить атаковать членов собственной фракции |
