# Система путей

Создавайте пути к точкам, по которым будут следовать боты. Пути сохраняются для каждого мира в`config/pvpbot/worlds/<worldname>/paths.json`.

## Команды

### Создание путей
```
/pvpbot bot-management path create MyPath
```

### Добавление путевых точек
Встаньте в нужное место и бегите:
```
/pvpbot bot-management path add-point MyPath
```
Визуализация пути (частиц) включается автоматически.

### Управление путевыми точками
```
/pvpbot bot-management path remove-point MyPath      # Remove last
/pvpbot bot-management path remove-point MyPath 0    # Remove by index
/pvpbot bot-management path clear MyPath             # Clear all points
/pvpbot bot-management path info MyPath              # List all points
/pvpbot bot-management path list                     # List all paths
```

### Следование по путям
```
/pvpbot bot-management path start Bot1 MyPath
/pvpbot bot-management path stop Bot1
/pvpbot bot-management path start-near MyPath 20     # Start for bots within 20 blocks
/pvpbot bot-management path stop-all MyPath          # Stop all bots on path
```

### Распределение
Равномерно расположите ботов вдоль точек пути:
```
/pvpbot bot-management path distribute MyPath
```

### Типы прогулок
```
/pvpbot bot-management path walk-type MyPath bhop    # Bunny hop (default)
/pvpbot bot-management path walk-type MyPath sprint  # Sprint
/pvpbot bot-management path walk-type MyPath walk    # Walk
```

### Режим цикла
```
/pvpbot bot-management path loop MyPath true
/pvpbot bot-management path loop MyPath false
```

В циклическом режиме боты в конце меняют направление (пинг-понг). В режиме без цикла боты перезапускаются с самого начала.

### Визуализация
```
/pvpbot bot-management path show MyPath true
/pvpbot bot-management path show MyPath false
```

Показывает путевые точки пути в виде частиц (WAX_ON + линии зеленой пыли).

## Пути фракций

Управляйте всеми ботами во фракции одновременно:
```
/pvpbot faction path start RedFaction MyPath
/pvpbot faction path stop RedFaction
```

## Характеристики

| Недвижимость | Опции | По умолчанию |
|----------|---------|---------|
| Тип прогулки | хоп/спринт/ходьба | бхоп |
| Петля | правда/ложь | ложный |
| Атака | правда/ложь | правда |
| Очки | переменная (Vec3d) | - |
