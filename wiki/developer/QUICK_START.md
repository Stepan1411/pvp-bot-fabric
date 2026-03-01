# Quick Start - PVP Bot API v1.1.0 Fixes

## ✅ Что исправлено

1. Combat Strategies теперь работают
2. BotAttackHandler корректно отменяет атаки
3. Документация обновлена

## 🚀 Быстрый тест (5 минут)

### Шаг 1: Запустить игру
```bash
./gradlew runClient
```

### Шаг 2: Создать тестовый мод

Создайте файл `TestMod.java`:

```java
package com.example.test;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.stepan1411.pvp_bot.api.PvpBotAPI;
import org.stepan1411.pvp_bot.api.combat.CombatStrategy;
import org.stepan1411.pvp_bot.api.combat.CombatStrategyRegistry;
import org.stepan1411.pvp_bot.bot.BotSettings;

public class TestMod implements ModInitializer {
    @Override
    public void onInitialize() {
        // Тест 1: Combat Strategy
        CombatStrategyRegistry.getInstance().register(new CombatStrategy() {
            @Override
            public String getName() { return "TestStrategy"; }
            
            @Override
            public int getPriority() { return 100; }
            
            @Override
            public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
                System.out.println("✅ TEST 1 PASSED: canUse() called!");
                return true;
            }
            
            @Override
            public boolean execute(ServerPlayerEntity bot, Entity target, 
                                  BotSettings settings, MinecraftServer server) {
                System.out.println("✅ TEST 1 PASSED: execute() called!");
                return false;
            }
        });
        
        // Тест 2: Attack Handler
        PvpBotAPI.getEventManager().registerAttackHandler((bot, target) -> {
            System.out.println("✅ TEST 2 PASSED: Attack handler called!");
            return false; // Разрешить атаку
        });
        
        System.out.println("=================================");
        System.out.println("✅ Test mod loaded successfully!");
        System.out.println("=================================");
    }
}
```

### Шаг 3: В игре

```
/pvpbot spawn TestBot
```

Дайте боту цель (спавните моба рядом).

### Шаг 4: Проверить консоль

Вы должны увидеть:
```
✅ TEST 1 PASSED: canUse() called!
✅ TEST 1 PASSED: execute() called!
✅ TEST 2 PASSED: Attack handler called!
```

## ✅ Если тесты прошли

Все исправления работают! Можете использовать API.

## ❌ Если тесты не прошли

1. Проверьте версию: должна быть 1.1.0+
2. Проверьте что мод загружен
3. Проверьте консоль на ошибки
4. См. `TESTING_CHECKLIST.md` для подробной диагностики

## 📖 Дальнейшие шаги

- **Примеры:** `test-addon-example/`
- **Документация:** `FIXES_README.md`
- **Для разработчиков:** `DEVELOPER_NOTES.md`
- **Тестирование:** `TESTING_CHECKLIST.md`

## 🎯 Готовые примеры

### Пример 1: Отмена атак на дружественных целей

```java
PvpBotAPI.getEventManager().registerAttackHandler((bot, target) -> {
    if (target.getName().getString().startsWith("Friend")) {
        return true; // Отменить атаку
    }
    return false;
});
```

### Пример 2: Кастомная стратегия стрельбы

```java
public class ShootStrategy implements CombatStrategy {
    @Override
    public String getName() { return "Shoot"; }
    
    @Override
    public int getPriority() { return 150; }
    
    @Override
    public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        return bot.distanceTo(target) > 10 && hasRangedWeapon(bot);
    }
    
    @Override
    public boolean execute(ServerPlayerEntity bot, Entity target, 
                          BotSettings settings, MinecraftServer server) {
        // Ваша логика стрельбы
        return true;
    }
}
```

## 💡 Советы

1. **Приоритет стратегий:** Выше = выполняется раньше
2. **Возврат true из execute():** Останавливает дальнейшую обработку
3. **Возврат true из handler:** Отменяет действие
4. **Логирование:** Добавляйте `System.out.println()` для отладки

## 🐛 Частые проблемы

### Стратегия не вызывается
- Проверьте что она зарегистрирована
- Проверьте условия в `canUse()`
- Добавьте логирование

### Handler не отменяет атаку
- Убедитесь что возвращаете `true`
- Проверьте что handler зарегистрирован
- Проверьте консоль на ошибки

### Ошибка компиляции
- Проверьте что используете правильные методы API
- Не используйте `bot.getServer()` - используйте параметр `server`
- См. `wiki/developer/FAQ.md`

---

**Время на тест: ~5 минут**  
**Сложность: Легко**  
**Результат: Проверка что все исправления работают**
