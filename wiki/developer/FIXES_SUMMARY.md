# Исправления проблем в PVP Bot API

## Обзор

Были исправлены три критические проблемы в PVP Bot API, которые препятствовали правильной работе системы боевых стратегий и обработчиков событий.

## Исправленные проблемы

### ✅ 1. Combat Strategies не вызываются для ботов

**Проблема:**
- Зарегистрированные `CombatStrategy` не выполнялись когда бот атакует цель
- Методы `canUse()` и `execute()` никогда не вызывались
- Боты использовали только встроенную логику атак

**Решение:**
Добавлена интеграция системы стратегий в основной цикл боя в методе `BotCombat.update()`:

```java
// В BotCombat.update(), перед selectWeaponMode():
try {
    var strategies = CombatStrategyRegistry.getInstance().getStrategies();
    for (var strategy : strategies) {
        if (strategy.canUse(bot, target, settings)) {
            boolean executed = strategy.execute(bot, target, settings, server);
            if (executed) {
                return; // Стратегия обработала бой
            }
        }
    }
} catch (Exception e) {
    System.err.println("[PVP_BOT] Error executing combat strategy: " + e.getMessage());
    e.printStackTrace();
}
```

**Файл:** `src/main/java/org/stepan1411/pvp_bot/bot/BotCombat.java`  
**Строка:** ~240 (после проверки isMending)

---

### ✅ 2. BotAttackHandler не отменяет атаки

**Проблема:**
- Возврат `true` из `BotAttackHandler.onBotAttack()` не отменял атаку бота
- Бот продолжал атаковать даже когда обработчик возвращал `true`

**Решение:**
Добавлена проверка отмены атаки в начале метода `attackWithCarpet()`:

```java
private static void attackWithCarpet(ServerPlayerEntity bot, Entity target, MinecraftServer server) {
    BotSettings settings = BotSettings.get();
    
    // === FIRE ATTACK EVENT - ALLOW CANCELLATION ===
    boolean cancelled = org.stepan1411.pvp_bot.api.BotAPIIntegration.fireAttackEvent(bot, target);
    if (cancelled) {
        // Атака отменена обработчиком - просто машем рукой
        bot.swingHand(Hand.MAIN_HAND);
        return;
    }
    // === END ATTACK EVENT ===
    
    // Продолжаем с обычной логикой атаки...
}
```

**Файл:** `src/main/java/org/stepan1411/pvp_bot/bot/BotCombat.java`  
**Метод:** `attackWithCarpet()`

---

### ✅ 3. Отсутствует метод getServer() у ServerPlayerEntity

**Проблема:**
- В документации и примерах использовался `bot.getServer()`
- Этот метод не существует в Minecraft 1.21.1

**Решение:**
Обновлена документация с правильными методами:

**Правильное использование:**
```java
// НЕПРАВИЛЬНО (не существует):
MinecraftServer server = bot.getServer();

// ПРАВИЛЬНО (использовать параметр):
public boolean execute(ServerPlayerEntity bot, Entity target, 
                      BotSettings settings, MinecraftServer server) {
    // server уже доступен как параметр
}

// ПРАВИЛЬНО (получить мир):
ServerWorld world = (ServerWorld) bot.getEntityWorld();
```

**Обновленные файлы:**
- `wiki/developer/Examples.md` - исправлен пример FireballStrategy
- `wiki/developer/FAQ.md` - добавлен раздел с решением проблем
- `wiki/developer/APIReference.md` - уточнены сигнатуры методов

---

## Тестирование исправлений

### Тест 1: Combat Strategy
```java
// Создайте стратегию
public class TestStrategy implements CombatStrategy {
    @Override
    public String getName() { return "Test"; }
    
    @Override
    public int getPriority() { return 100; }
    
    @Override
    public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
        System.out.println("canUse() called!");
        return true;
    }
    
    @Override
    public boolean execute(ServerPlayerEntity bot, Entity target, 
                          BotSettings settings, MinecraftServer server) {
        System.out.println("execute() called!");
        return false;
    }
}

// Зарегистрируйте
CombatStrategyRegistry.getInstance().register(new TestStrategy());

// Ожидаемый результат: в консоли должны появиться сообщения
// "canUse() called!" и "execute() called!" когда бот атакует
```

### Тест 2: Attack Handler
```java
// Зарегистрируйте обработчик
PvpBotAPI.getEventManager().registerAttackHandler((bot, target) -> {
    System.out.println("Attack handler called!");
    return true; // Отменить атаку
});

// Ожидаемый результат: бот машет рукой но не наносит урон
```

### Тест 3: Правильное использование API
```java
// Используйте параметр server вместо bot.getServer()
public boolean execute(ServerPlayerEntity bot, Entity target, 
                      BotSettings settings, MinecraftServer server) {
    // Правильно - используем параметр
    ServerWorld world = (ServerWorld) bot.getEntityWorld();
    
    // Код компилируется без ошибок
    return true;
}
```

---

## Файлы с примерами

### Тестовый аддон
- `test-addon-example/TestCombatStrategy.java` - пример стратегии для тестирования
- `test-addon-example/TestAddonWithFixes.java` - полный тестовый аддон

### Документация
- `BUGFIXES.md` - подробное описание исправлений
- `FIXES_SUMMARY.md` - этот файл
- `combat_strategies_patch.txt` - инструкции по применению патча

---

## Проверка исправлений

Запустите следующие команды для проверки:

```bash
# 1. Проверить что код компилируется
./gradlew build

# 2. Запустить игру
./gradlew runClient

# 3. В игре:
/pvpbot spawn TestBot
# Дайте боту цель и наблюдайте за консолью
```

---

## Миграция с workaround'ов

Если вы использовали обходные решения, вот как мигрировать:

### От TickHandler к CombatStrategy
**Было:**
```java
PvpBotAPI.getEventManager().registerTickHandler(bot -> {
    // Логика стрельбы в каждом тике
});
```

**Стало:**
```java
public class GunStrategy implements CombatStrategy {
    // Правильная реализация стратегии
}
CombatStrategyRegistry.getInstance().register(new GunStrategy());
```

### От Mixin к BotAttackHandler
**Было:**
```java
@Mixin(ServerPlayerEntity.class)
public class BotAttackMixin {
    @Inject(method = "attack", at = @At("HEAD"), cancellable = true)
    private void onAttack(Entity target, CallbackInfo ci) {
        // Отмена атаки через mixin
    }
}
```

**Стало:**
```java
PvpBotAPI.getEventManager().registerAttackHandler((bot, target) -> {
    return shouldCancel; // true = отменить
});
```

---

## Поддержка

Если у вас возникли проблемы:
1. Убедитесь что используете версию 1.1.0 или новее
2. Проверьте FAQ в документации
3. Создайте issue на GitHub с логами и шагами воспроизведения

---

## Авторы

- Анализ проблем: сообщество
- Исправления: Stepan1411
- Документация: Kiro AI Assistant
