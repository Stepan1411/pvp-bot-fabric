# Developer Notes - PVP Bot API v1.1.0

## Что было исправлено

Эта версия содержит критические исправления трех основных проблем API:

1. ✅ **Combat Strategies теперь работают** - стратегии вызываются в цикле боя
2. ✅ **BotAttackHandler корректно отменяет атаки** - возврат `true` теперь работает
3. ✅ **Документация обновлена** - убраны несуществующие методы типа `bot.getServer()`

## Быстрый старт для тестирования

### 1. Проверка Combat Strategies

```java
// Создайте простую стратегию
public class LogStrategy implements CombatStrategy {
    @Override
    public String getName() { return "LogStrategy"; }
    
    @Override
    public int getPriority() { return 100; }
    
    @Override
    public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        System.out.println("[LOG] canUse called for " + bot.getName().getString());
        return true;
    }
    
    @Override
    public boolean execute(ServerPlayerEntity bot, Entity target, 
                          BotSettings settings, MinecraftServer server) {
        System.out.println("[LOG] execute called - strategy works!");
        return false; // Позволить стандартному бою продолжиться
    }
}

// Зарегистрируйте в onInitialize()
CombatStrategyRegistry.getInstance().register(new LogStrategy());
```

**Ожидаемый результат:** В консоли появятся сообщения когда бот атакует.

### 2. Проверка Attack Handler

```java
// В onInitialize()
PvpBotAPI.getEventManager().registerAttackHandler((bot, target) -> {
    System.out.println("[TEST] Attack handler called");
    System.out.println("[TEST] Bot: " + bot.getName().getString());
    System.out.println("[TEST] Target: " + target.getName().getString());
    
    // Отменить все атаки для теста
    System.out.println("[TEST] Cancelling attack");
    return true;
});
```

**Ожидаемый результат:** Бот машет рукой но не наносит урон.

### 3. Проверка правильного использования API

```java
public class MyStrategy implements CombatStrategy {
    @Override
    public boolean execute(ServerPlayerEntity bot, Entity target, 
                          BotSettings settings, MinecraftServer server) {
        // ✅ ПРАВИЛЬНО - используем параметр server
        ServerWorld world = (ServerWorld) bot.getEntityWorld();
        
        // ❌ НЕПРАВИЛЬНО - этот метод не существует
        // MinecraftServer server = bot.getServer();
        
        return true;
    }
}
```

**Ожидаемый результат:** Код компилируется без ошибок.

## Структура изменений

### Измененные файлы

#### Код
- `src/main/java/org/stepan1411/pvp_bot/bot/BotCombat.java`
  - Добавлена интеграция Combat Strategies (строка ~240)
  - Добавлена проверка отмены атаки (метод `attackWithCarpet()`)

#### Документация
- `wiki/developer/Examples.md` - исправлен пример FireballStrategy
- `wiki/developer/FAQ.md` - добавлен раздел "Errors" с решениями
- `BUGFIXES.md` - подробное описание всех исправлений
- `FIXES_SUMMARY.md` - краткое описание на русском

#### Тестовые примеры
- `test-addon-example/TestCombatStrategy.java` - пример стратегии
- `test-addon-example/TestAddonWithFixes.java` - полный тестовый аддон

## Как проверить что исправления работают

### Метод 1: Автоматический тест

```bash
# 1. Скомпилировать
./gradlew build

# 2. Запустить игру
./gradlew runClient

# 3. В игре выполнить
/pvpbot spawn TestBot

# 4. Дать боту цель (например, спавнить моба рядом)

# 5. Проверить консоль на наличие сообщений от стратегий
```

### Метод 2: Использовать тестовый аддон

1. Скопируйте файлы из `test-addon-example/` в ваш мод
2. Зарегистрируйте `TestAddonWithFixes` как `ModInitializer`
3. Запустите игру
4. Следуйте инструкциям в консоли

### Метод 3: Ручная проверка

1. Создайте свою стратегию с логированием
2. Зарегистрируйте её с высоким приоритетом (200+)
3. Спавните бота и дайте ему цель
4. Проверьте что `canUse()` и `execute()` вызываются

## Технические детали

### Combat Strategy Integration

**Где:** `BotCombat.update()`, после проверки `isMending`  
**Что делает:**
1. Получает все зарегистрированные стратегии (уже отсортированы по приоритету)
2. Для каждой стратегии вызывает `canUse()`
3. Если `canUse()` возвращает `true`, вызывает `execute()`
4. Если `execute()` возвращает `true`, прекращает обработку (стратегия обработала бой)
5. Если `execute()` возвращает `false`, продолжает со следующей стратегией
6. Если ни одна стратегия не вернула `true`, выполняется стандартная логика боя

**Обработка ошибок:**
- Все исключения перехватываются и логируются
- Ошибка в одной стратегии не влияет на другие

### Attack Cancellation

**Где:** `BotCombat.attackWithCarpet()`, в начале метода  
**Что делает:**
1. Вызывает `BotAPIIntegration.fireAttackEvent(bot, target)`
2. Если хотя бы один обработчик вернул `true`, атака отменяется
3. При отмене бот только машет рукой (`bot.swingHand()`)
4. Если атака не отменена, выполняется обычная логика

**Важно:**
- Событие вызывается ДО выполнения атаки
- Проверка friendlyfire выполняется ПОСЛЕ проверки обработчиков
- Это позволяет обработчикам переопределить любую логику

### API Method Corrections

**Проблема:** `bot.getServer()` не существует в Minecraft 1.21.1

**Решения:**
1. Использовать параметр `server` в методах (предпочтительно)
2. Использовать `bot.getEntityWorld()` для получения мира
3. Кастовать к `ServerWorld` если нужны серверные методы

## Миграция существующего кода

### Если вы использовали TickHandler для боя

**Было:**
```java
PvpBotAPI.getEventManager().registerTickHandler(bot -> {
    if (hasTarget(bot)) {
        performCustomAttack(bot, getTarget(bot));
    }
});
```

**Стало:**
```java
public class CustomAttackStrategy implements CombatStrategy {
    @Override
    public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        return true; // Ваши условия
    }
    
    @Override
    public boolean execute(ServerPlayerEntity bot, Entity target, 
                          BotSettings settings, MinecraftServer server) {
        performCustomAttack(bot, target);
        return true; // true = обработали, false = продолжить стандартный бой
    }
    
    @Override
    public int getPriority() { return 100; }
    @Override
    public String getName() { return "CustomAttack"; }
}

// Регистрация
CombatStrategyRegistry.getInstance().register(new CustomAttackStrategy());
```

### Если вы использовали Mixin для отмены атак

**Было:**
```java
@Mixin(ServerPlayerEntity.class)
public class AttackMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Entity target, CallbackInfo ci) {
        if (shouldCancel(target)) {
            ci.cancel();
        }
    }
}
```

**Стало:**
```java
PvpBotAPI.getEventManager().registerAttackHandler((bot, target) -> {
    return shouldCancel(target); // true = отменить, false = разрешить
});
```

## Известные ограничения

1. **Стратегии выполняются последовательно** - если у вас много стратегий с тяжелыми `canUse()` проверками, это может повлиять на производительность
2. **Cooldown не реализован автоматически** - вам нужно самим отслеживать время последнего использования
3. **Нет приоритета для AttackHandler** - обработчики выполняются в порядке регистрации

## Рекомендации по производительности

### Оптимизация canUse()

```java
@Override
public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
    // ✅ ХОРОШО - быстрые проверки
    if (bot.getHealth() < 10) return false;
    if (bot.distanceTo(target) > 20) return false;
    
    // ❌ ПЛОХО - тяжелые операции
    // List<Entity> nearbyEntities = world.getEntitiesByClass(...);
    // for (Entity e : nearbyEntities) { ... }
    
    return true;
}
```

### Кэширование данных

```java
public class MyStrategy implements CombatStrategy {
    private long lastCheck = 0;
    private boolean cachedResult = false;
    
    @Override
    public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        long now = System.currentTimeMillis();
        if (now - lastCheck > 1000) { // Обновлять раз в секунду
            cachedResult = expensiveCheck(bot);
            lastCheck = now;
        }
        return cachedResult;
    }
}
```

## Отладка

### Включить подробное логирование

```java
public class DebugStrategy implements CombatStrategy {
    private static final Logger LOGGER = LoggerFactory.getLogger("MyMod");
    
    @Override
    public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        LOGGER.info("canUse called: bot={}, target={}, distance={}", 
                   bot.getName(), target.getName(), bot.distanceTo(target));
        return true;
    }
    
    @Override
    public boolean execute(ServerPlayerEntity bot, Entity target, 
                          BotSettings settings, MinecraftServer server) {
        LOGGER.info("execute called: performing custom attack");
        // Ваш код
        LOGGER.info("execute completed successfully");
        return true;
    }
}
```

### Проверить регистрацию

```java
@Override
public void onInitialize() {
    MyStrategy strategy = new MyStrategy();
    CombatStrategyRegistry.getInstance().register(strategy);
    
    // Проверить что зарегистрировалась
    List<CombatStrategy> strategies = CombatStrategyRegistry.getInstance().getStrategies();
    System.out.println("Registered strategies: " + strategies.size());
    for (CombatStrategy s : strategies) {
        System.out.println("  - " + s.getName() + " (priority: " + s.getPriority() + ")");
    }
}
```

## Поддержка

### Если что-то не работает

1. Проверьте версию API (должна быть 1.1.0+)
2. Проверьте что стратегия зарегистрирована
3. Добавьте логирование в `canUse()` и `execute()`
4. Проверьте консоль на ошибки
5. Создайте issue на GitHub с логами

### Полезные команды для отладки

```bash
# Проверить версию API
grep "api_version" gradle.properties

# Найти все стратегии в коде
grep -r "implements CombatStrategy" src/

# Проверить что исправления применены
grep -A 5 "COMBAT STRATEGIES INTEGRATION" src/main/java/org/stepan1411/pvp_bot/bot/BotCombat.java
```

## Changelog

### v1.1.0 (2026-02-28)
- ✅ Fixed: Combat Strategies now execute during bot combat
- ✅ Fixed: BotAttackHandler cancellation now works correctly
- ✅ Fixed: Documentation updated with correct API methods
- 📝 Added: Comprehensive testing examples
- 📝 Added: Migration guide from workarounds

### v1.0.0 (Initial Release)
- ❌ Combat Strategies not integrated
- ❌ Attack cancellation not working
- ❌ Documentation had incorrect method references

## Контрибьюторы

- **Stepan1411** - Основной разработчик
- **Community** - Репорты багов и тестирование
- **Kiro AI** - Анализ и документация исправлений
