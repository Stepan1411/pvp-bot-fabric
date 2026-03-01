# Mod Integration

This guide explains how to integrate PVP Bot with your Fabric mod.

## Prerequisites

- Java 21+
- Fabric Loader 0.16.0+
- Minecraft 1.21.10+
- Basic knowledge of Fabric mod development

## Setup

### 1. Add Repository

Add JitPack repository to your `build.gradle`:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}
```

### 2. Add Dependency

Add PVP Bot as a dependency:
[![](https://jitpack.io/v/Stepan1411/pvp-bot-fabric.svg)](https://jitpack.io/#Stepan1411/pvp-bot-fabric)

```gradle
dependencies {
    // Required: PVP Bot API
    modImplementation "com.github.Stepan1411:pvp-bot-fabric:VERSION"
    
    // Optional: Include in your JAR (if you want to bundle it)
    // include "com.github.Stepan1411:pvp-bot-fabric:VERSION"
}
```


### 3. Update fabric.mod.json

Declare PVP Bot as a dependency:

```json
{
  "schemaVersion": 1,
  "id": "your-mod-id",
  "version": "1.0.0",
  "name": "Your Mod Name",
  
  "depends": {
    "fabricloader": ">=0.16.0",
    "minecraft": ">=1.21.10",
    "fabric": "*",
    "pvp_bot": "*"
  }
}
```

## Integration Methods

### Method 1: Event-Based Integration (Recommended)

Best for: Reacting to bot actions, modifying bot behavior, statistics tracking

```java
package com.example.yourmod;

import net.fabricmc.api.ModInitializer;
import org.stepan1411.pvp_bot.api.PvpBotAPI;
import org.stepan1411.pvp_bot.api.event.BotEventManager;

public class YourMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        BotEventManager events = PvpBotAPI.getEventManager();
        
        // React to bot spawns
        events.registerSpawnHandler(bot -> {
            System.out.println("Bot spawned: " + bot.getName().getString());
            // Your logic here
        });
        
        // React to bot deaths
        events.registerDeathHandler(bot -> {
            System.out.println("Bot died: " + bot.getName().getString());
            // Your logic here
        });
        
        // Control bot attacks
        events.registerAttackHandler((bot, target) -> {
            // Return true to cancel attack
            if (shouldCancelAttack(bot, target)) {
                return true;
            }
            return false;
        });
        
        // Control bot damage
        events.registerDamageHandler((bot, attacker, damage) -> {
            // Return true to cancel damage
            if (shouldCancelDamage(bot, attacker, damage)) {
                return true;
            }
            return false;
        });
        
        // Update every tick
        events.registerTickHandler(bot -> {
            // Called every tick for each bot
            // Use sparingly - this runs frequently!
        });
    }
}
```

### Method 2: Combat Strategy Integration

Best for: Custom combat behaviors, weapon-specific logic, advanced AI

```java
package com.example.yourmod;

import net.fabricmc.api.ModInitializer;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.stepan1411.pvp_bot.api.combat.CombatStrategy;
import org.stepan1411.pvp_bot.api.combat.CombatStrategyRegistry;
import org.stepan1411.pvp_bot.bot.BotSettings;

public class YourMod implements ModInitializer {
    
    @Override
    public void onInitialize() {
        // Register custom combat strategy
        CombatStrategyRegistry.getInstance().register(new CustomStrategy());
    }
    
    static class CustomStrategy implements CombatStrategy {
        
        @Override
        public String getName() {
            return "Custom Strategy";
        }
        
        @Override
        public int getPriority() {
            return 100; // Higher = executed first
        }
        
        @Override
        public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
            // Check if strategy can be used
            // Example: only use if bot has specific item
            return bot.getMainHandStack().getItem().toString().contains("diamond_sword");
        }
        
        @Override
        public boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server) {
            // Execute your custom combat logic
            System.out.println("Executing custom strategy!");
            
            // Your combat code here
            // Return true if strategy was executed successfully
            return true;
        }
        
        @Override
        public int getCooldown() {
            return 40; // 2 seconds cooldown
        }
    }
}
```

### Method 3: Query-Based Integration

Best for: Checking bot status, getting bot information, conditional logic

```java
package com.example.yourmod;

import net.minecraft.server.MinecraftServer;
import net.minecraft.server.network.ServerPlayerEntity;
import org.stepan1411.pvp_bot.api.PvpBotAPI;

import java.util.Set;

public class BotHelper {
    
    public static void checkBots(MinecraftServer server) {
        // Get all bot names
        Set<String> bots = PvpBotAPI.getAllBots();
        System.out.println("Active bots: " + bots.size());
        
        // Check if player is a bot
        boolean isBot = PvpBotAPI.isBot("PlayerName");
        
        // Get bot entity
        ServerPlayerEntity bot = PvpBotAPI.getBot(server, "BotName");
        if (bot != null) {
            System.out.println("Bot health: " + bot.getHealth());
            System.out.println("Bot position: " + bot.getPos());
        }
        
        // Get statistics
        int spawned = PvpBotAPI.getTotalBotsSpawned();
        int killed = PvpBotAPI.getTotalBotsKilled();
        System.out.println("Stats: " + spawned + " spawned, " + killed + " killed");
    }
}
```

### Method 4: Configuration Integration

Best for: Adapting to bot settings, reading configuration

```java
package com.example.yourmod;

import org.stepan1411.pvp_bot.api.PvpBotAPI;
import org.stepan1411.pvp_bot.bot.BotSettings;

public class ConfigHelper {
    
    public static void readSettings() {
        BotSettings settings = PvpBotAPI.getBotSettings();
        
        // Read combat settings
        boolean combatEnabled = settings.isCombatEnabled();
        double meleeRange = settings.getMeleeRange();
        int attackCooldown = settings.getAttackCooldown();
        
        // Read equipment settings
        boolean autoEquipArmor = settings.isAutoEquipArmor();
        boolean autoEquipWeapon = settings.isAutoEquipWeapon();
        
        // Read utility settings
        boolean autoTotem = settings.isAutoTotemEnabled();
        boolean autoEat = settings.isAutoEatEnabled();
        
        // Adapt your mod's behavior based on settings
        if (combatEnabled) {
            // Enable combat features
        }
    }
}
```

## Common Integration Patterns

### Pattern 1: Bot Protection System

Prevent bots from being attacked by certain entities:

```java
events.registerDamageHandler((bot, attacker, damage) -> {
    if (attacker != null && isProtectedEntity(attacker)) {
        return true; // Cancel damage
    }
    return false;
});
```

### Pattern 2: Bot Reward System

Give rewards when bots kill players:

```java
events.registerAttackHandler((bot, target) -> {
    if (target instanceof ServerPlayerEntity player) {
        if (player.getHealth() - calculateDamage(bot) <= 0) {
            giveReward(bot);
        }
    }
    return false;
});
```

### Pattern 3: Bot Statistics Tracking

Track custom statistics:

```java
private final Map<String, Integer> botKills = new HashMap<>();

events.registerDeathHandler(bot -> {
    String name = bot.getName().getString();
    botKills.put(name, botKills.getOrDefault(name, 0) + 1);
});
```

### Pattern 4: Bot Team System

Prevent friendly fire between team members:

```java
events.registerAttackHandler((bot, target) -> {
    if (target instanceof ServerPlayerEntity targetPlayer) {
        if (isSameTeam(bot, targetPlayer)) {
            return true; // Cancel attack
        }
    }
    return false;
});
```

### Pattern 5: Custom Bot AI

Add custom behavior to bots:

```java
events.registerTickHandler(bot -> {
    // Only run every 20 ticks (1 second)
    if (bot.age % 20 == 0) {
        // Check conditions
        if (shouldFlee(bot)) {
            makeFleeFromDanger(bot);
        } else if (shouldHeal(bot)) {
            useHealingItem(bot);
        }
    }
});
```

## Advanced Integration

### Accessing Internal Bot Systems

While the API provides most functionality, you can access internal systems if needed:

```java
import org.stepan1411.pvp_bot.bot.BotManager;
import org.stepan1411.pvp_bot.bot.BotFaction;
import org.stepan1411.pvp_bot.bot.BotPath;

// Check bot faction
String faction = BotFaction.getFaction("BotName");

// Check if bots are enemies
boolean enemies = BotFaction.areEnemies("Bot1", "Bot2");

// Access path system
// (Use with caution - internal API may change)
```

**Warning:** Internal APIs are not guaranteed to be stable. Prefer using the public API when possible.

### Mixin Integration

If you need deeper integration, you can use Mixins:

```java
@Mixin(ServerPlayerEntity.class)
public class BotMixin {
    
    @Inject(method = "tick", at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        ServerPlayerEntity player = (ServerPlayerEntity)(Object)this;
        if (PvpBotAPI.isBot(player.getName().getString())) {
            // Custom bot logic
        }
    }
}
```

## Compatibility Considerations

### Version Checking

Check API version at runtime:

```java
String apiVersion = PvpBotAPI.getApiVersion();
if (!apiVersion.equals("1.0.0")) {
    System.err.println("Warning: Unexpected API version: " + apiVersion);
}
```

### Graceful Degradation

Handle cases where PVP Bot is not installed:

```java
public class YourMod implements ModInitializer {
    
    private static boolean pvpBotLoaded = false;
    
    @Override
    public void onInitialize() {
        try {
            Class.forName("org.stepan1411.pvp_bot.api.PvpBotAPI");
            pvpBotLoaded = true;
            initPvpBotIntegration();
        } catch (ClassNotFoundException e) {
            System.out.println("PVP Bot not found - integration disabled");
        }
    }
    
    private void initPvpBotIntegration() {
        // Your integration code
    }
}
```

### Soft Dependencies

Make PVP Bot optional in `fabric.mod.json`:

```json
{
  "depends": {
    "fabricloader": ">=0.16.0",
    "minecraft": ">=1.21.10"
  },
  "suggests": {
    "pvp_bot": "*"
  }
}
```

## Testing Your Integration

### 1. Build Your Mod

```bash
./gradlew build
```

### 2. Test in Development

Place both mods in `run/mods/`:
- Your mod JAR
- PVP Bot JAR

### 3. Test Bot Spawning

```
/pvpbot spawn TestBot
```

### 4. Check Logs

Look for your integration messages in the console.

### 5. Test Events

- Spawn bot → Check spawn event
- Attack bot → Check damage event
- Bot attacks → Check attack event
- Kill bot → Check death event

## Troubleshooting

### Issue: ClassNotFoundException

**Cause:** PVP Bot not in classpath

**Solution:** Check `build.gradle` dependency and rebuild

### Issue: Events Not Firing

**Cause:** Handler registered too late

**Solution:** Register handlers in `onInitialize()`, not later

### Issue: Strategy Not Executing

**Cause:** Lower priority than existing strategies

**Solution:** Increase priority value (higher = first)

### Issue: Bot Not Found

**Cause:** Bot name incorrect or bot not spawned

**Solution:** Use `PvpBotAPI.getAllBots()` to check active bots

## Example Projects

[example mod](https://github.com/Stepan1411/pvpbot-example-mod)

## Performance Tips

1. **Avoid Heavy Tick Handlers:** Tick events run every tick (20 times/second per bot)
2. **Use Cooldowns:** Add delays between expensive operations
3. **Cache Results:** Don't query repeatedly in tight loops
4. **Batch Operations:** Group multiple operations together
5. **Profile Your Code:** Use profiler to find bottlenecks

## Security Considerations

1. **Validate Input:** Check bot/target validity before operations
2. **Handle Exceptions:** Wrap handlers in try-catch
3. **Limit Permissions:** Don't give bots excessive permissions
4. **Rate Limiting:** Prevent spam from bot actions

## Next Steps

- [API Reference] - Complete method documentation
- [Events] - Detailed event system guide
- [Combat Strategies] - Create custom combat logic
- [Examples] - Ready-to-use code examples
- [Best Practices] - Recommendations for addon development
