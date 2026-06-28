# 🤖 ПВП-бот — Wiki

Добро пожаловать в официальную документацию PVP-бота!

---

## 📖 О нас

PVP Bot — это мод Minecraft Fabric, который добавляет интеллектуальных боевых ботов на базе мода HeroBot. Создавайте армии ботов, объединяйте их во фракции и наблюдайте, как разворачиваются эпические сражения!

---

## 🚀 Быстрый старт

1. Установите [Fabric Loader](https://fabricmc.net/) и [HeroBot Mod](https://modrinth.com/mod/herobot).
2. Загрузите PVP Bot и поместите его в свой`mods`папка
3. Запустите игру и используйте`/pvpbot spawn BotName`чтобы создать своего первого бота!

---

## 📚 Документация

| Страница | Описание |
|------|-------------|
| [🎮 Команды](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Commands) | Все доступные команды |
| [⚔️ Боевая система](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Combat) | Как боты дерутся |
| [🚶 Навигация](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Navigation) | Базовый поиск пути |
| [🛤️ Пути](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Paths) | Система путей и путевые точки |
| [👥 Фракции](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Factions) | Командная система |
| [🎒 Комплекты](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Kits) | Пресеты оборудования |
| [⚙️ Настройки](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Settings) | Все варианты конфигурации |

---

## 💡 Быстрые примеры

### Создайте простого бота
```
/pvpbot spawn MyBot
```

### Заставьте две команды сражаться
```
/pvpbot spawn Red1
/pvpbot spawn Blue1
/pvpbot faction create Red
/pvpbot faction create Blue
/pvpbot faction add Red Red1
/pvpbot faction add Blue Blue1
/pvpbot faction hostile Red Blue
```

---

## 🔗 Ссылки

- [Репозиторий GitHub](https://github.com/Stepan1411/pvp-bot-fabric)
- [Страница Модринта](https://modrinth.com/mod/pvp-bot)
- [Отчеты об ошибках](https://github.com/Stepan1411/pvp-bot-fabric/issues)
