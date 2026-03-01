# Инструкция по публикации v0.0.12A

## ✅ Что уже сделано:
1. API полностью интегрирован
2. Версия обновлена до 0.0.12A
3. Добавлен `jitpack.yml` для корректной сборки на JitPack
4. Добавлена конфигурация `maven-publish` в `build.gradle`
5. Локальная сборка успешна
6. JAR файл создан: `build/libs/PVP_bot-0.0.12A.jar`

## 🚀 Что нужно сделать:

### 1. Закоммитить и запушить изменения
```bash
git add .
git commit -m "Add public API and JitPack support (v0.0.12A)"
git push origin main
```

### 2. Создать тег
```bash
git tag v0.0.12A
git push origin v0.0.12A
```

### 3. Создать GitHub Release
1. Зайди на https://github.com/Stepan1411/pvp-bot-fabric/releases
2. Нажми "Create a new release"
3. Заполни:
   - **Tag**: `v0.0.12A`
   - **Title**: `PVP Bot 0.0.12A - Public API`
   - **Description**:
```markdown
## 🎉 Public API для разработчиков аддонов!

Теперь другие моды могут интегрироваться с PVP Bot через публичный API.

### ✨ Что добавлено:
- ✅ **События**: spawn, death, attack, damage, tick
- ✅ **Система боевых стратегий** для кастомного поведения ботов
- ✅ **Полная документация** на английском в `wiki/developer/`
- ✅ **JitPack поддержка** для простого подключения

### 📦 Для разработчиков аддонов:

**Добавьте зависимость в `build.gradle`:**
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    modImplementation "com.github.Stepan1411:pvp-bot-fabric:v0.0.12A"
}
```

**Пример использования:**
```java
import org.stepan1411.pvp_bot.api.PvpBotAPI;

// Выдать оружие при спавне бота
PvpBotAPI.getEventManager().registerSpawnHandler(bot -> {
    bot.giveItemStack(new ItemStack(Items.DIAMOND_SWORD));
});
```

### 📚 Документация:
- [Quick Start](https://github.com/Stepan1411/pvp-bot-fabric/blob/main/wiki/developer/QuickStart.md)
- [API Reference](https://github.com/Stepan1411/pvp-bot-fabric/blob/main/wiki/developer/APIReference.md)
- [Events System](https://github.com/Stepan1411/pvp-bot-fabric/blob/main/wiki/developer/Events.md)
- [Combat Strategies](https://github.com/Stepan1411/pvp-bot-fabric/blob/main/wiki/developer/CombatStrategies.md)
- [Examples](https://github.com/Stepan1411/pvp-bot-fabric/blob/main/wiki/developer/Examples.md)

### 🔗 Ссылки:
- JitPack: https://jitpack.io/#Stepan1411/pvp-bot-fabric/v0.0.12A
- Документация: [wiki/developer/](https://github.com/Stepan1411/pvp-bot-fabric/tree/main/wiki/developer)

---

**Для игроков:** Эта версия полностью совместима с 0.0.12, все функции работают как прежде.
```

4. Прикрепи файлы:
   - `build/libs/PVP_bot-0.0.12A.jar`
   - `build/libs/PVP_bot-0.0.12A-sources.jar`

5. Нажми **"Publish release"**

### 4. Проверить JitPack (через 5-10 минут)
1. Зайди на https://jitpack.io/#Stepan1411/pvp-bot-fabric
2. Найди версию `v0.0.12A`
3. Нажми "Get it" - JitPack начнёт сборку
4. Дождись зелёной галочки ✅

### 5. Ответить разработчику Just Enough Guns
Скопируй текст из `RESPONSE_TO_DEVELOPER.md` и замени все упоминания `0.0.12` на `0.0.12A`:

```markdown
Привет! Проблема решена! ✅

API теперь доступен в версии **v0.0.12A**!

**Зависимость:**
```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    modImplementation "com.github.Stepan1411:pvp-bot-fabric:v0.0.12A"
}
```

**Быстрый пример:**
```java
import org.stepan1411.pvp_bot.api.PvpBotAPI;

PvpBotAPI.getEventManager().registerSpawnHandler(bot -> {
    bot.giveItemStack(new ItemStack(ModItems.ASSAULT_RIFLE));
    bot.giveItemStack(new ItemStack(ModItems.RIFLE_AMMO, 192));
});
```

**Документация:** https://github.com/Stepan1411/pvp-bot-fabric/tree/main/wiki/developer

Скачай JAR из релиза: https://github.com/Stepan1411/pvp-bot-fabric/releases/tag/v0.0.12A

Если будут вопросы - пиши! 🚀
```

## 📝 Важные файлы для проверки:
- ✅ `jitpack.yml` - конфигурация для JitPack
- ✅ `build.gradle` - добавлен maven-publish
- ✅ `gradle.properties` - версия 0.0.12A
- ✅ `build/libs/PVP_bot-0.0.12A.jar` - готовый мод

## 🎯 После публикации:
Разработчики смогут использовать твой API через JitPack без необходимости скачивать JAR вручную!
