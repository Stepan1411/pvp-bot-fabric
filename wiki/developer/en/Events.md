# Events

PVP Bot API provides an event system for tracking bot lifecycle.
Or use [example mod](https://github.com/Stepan1411/pvpbot-example-mod)

## Registering Handlers

All handlers are registered through `BotEventManager`:

```java
BotEventManager eventManager = PvpBotAPI.getEventManager();
```

## Available Events

### BotSpawnHandler

Called when a bot spawns on the server.

```java
eventManager.registerSpawnHandler(bot -> {
    System.out.println("Bot spawned: " + bot.getName().getString());
    
    // You can modify inventory, position, etc.
    bot.setHealth(20.0f);
});
```

**Parameters:**
- `bot` - ServerPlayerEntity of the bot

### BotDeathHandler

Called when a bot dies.

```java
eventManager.registerDeathHandler(bot -> {
    System.out.println("Bot died: " + bot.getName().getString());
    
    // You can save statistics, send messages, etc.
});
```

**Parameters:**
- `bot` - ServerPlayerEntity of the bot

### BotAttackHandler

Called when a bot attacks an entity. Can cancel the attack.

```java
eventManager.registerAttackHandler((bot, target) -> {
    System.out.println(bot.getName().getString() + " attacks " + target.getName().getString());
    
    // Cancel attack on specific entities
    if (target instanceof VillagerEntity) {
        return true; // true = cancel attack
    }
    
    return false; // false = allow attack
});
```

**Parameters:**
- `bot` - ServerPlayerEntity of the bot
- `target` - Entity being attacked

**Return value:**
- `true` - cancel attack
- `false` - allow attack

### BotDamageHandler

Called when a bot takes damage. Can cancel the damage.

```java
eventManager.registerDamageHandler((bot, attacker, damage) -> {
    System.out.println(bot.getName().getString() + " took " + damage + " damage");
    
    // Make bot immune to specific sources
    if (attacker instanceof CreeperEntity) {
        return true; // true = cancel damage
    }
    
    return false; // false = allow damage
});
```

**Parameters:**
- `bot` - ServerPlayerEntity of the bot
- `attacker` - Entity that attacked (can be null)
- `damage` - amount of damage

**Return value:**
- `true` - cancel damage
- `false` - allow damage

### BotTickHandler

Called every tick for each bot (20 times per second).

```java
eventManager.registerTickHandler(bot -> {
    // Executed every tick
    
    // Example: heal bot every 100 ticks
    if (bot.age % 100 == 0 && bot.getHealth() < bot.getMaxHealth()) {
        bot.setHealth(bot.getHealth() + 1.0f);
    }
});
```

**Parameters:**
- `bot` - ServerPlayerEntity of the bot

**⚠️ Warning:** This handler is called very frequently. Avoid heavy operations!

## Usage Examples

### Achievement System

```java
public class BotAchievements {
    private final Map<String, Integer> kills = new HashMap<>();
    
    public void register() {
        PvpBotAPI.getEventManager().registerAttackHandler((bot, target) -> {
            if (target.isDead()) {
                String botName = bot.getName().getString();
                kills.put(botName, kills.getOrDefault(botName, 0) + 1);
                
                int killCount = kills.get(botName);
                if (killCount == 10) {
                    bot.sendMessage(Text.literal("Achievement: 10 kills!"));
                }
            }
            return false;
        });
    }
}
```

### Auto Respawn

```java
public class AutoRespawn {
    public void register() {
        PvpBotAPI.getEventManager().registerDeathHandler(bot -> {
            String botName = bot.getName().getString();
            MinecraftServer server = bot.getServer();
            
            // Respawn after 5 seconds (100 ticks)
            server.execute(() -> {
                scheduleRespawn(server, botName, 100);
            });
        });
    }
    
    private void scheduleRespawn(MinecraftServer server, String name, int delay) {
        if (delay <= 0) {
            server.getCommandManager().getDispatcher()
                .execute("pvpbot spawn " + name, server.getCommandSource());
        } else {
            server.execute(() -> scheduleRespawn(server, name, delay - 1));
        }
    }
}
```

### Damage Protection

```java
public class DamageProtection {
    private final Set<String> protectedBots = new HashSet<>();
    
    public void register() {
        PvpBotAPI.getEventManager().registerDamageHandler((bot, attacker, damage) -> {
            String botName = bot.getName().getString();
            
            // Protect bots with low HP
            if (bot.getHealth() < 5.0f) {
                protectedBots.add(botName);
                return true; // Cancel damage
            }
            
            return protectedBots.contains(botName);
        });
    }
}
```

## Execution Order

Handlers are executed in registration order. If one handler cancels an event (returns `true`), others will still be called.

## Error Handling

All exceptions in handlers are caught and logged. An error in one handler won't affect others.
