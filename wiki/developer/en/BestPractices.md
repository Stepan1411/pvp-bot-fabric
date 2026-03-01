# Best Practices

This guide provides recommendations for developing high-quality addons for PVP Bot.

## Code Organization

### Use Proper Package Structure

```
com.example.yourmod/
├── YourMod.java              # Main entry point
├── integration/
│   └── PvpBotIntegration.java  # PVP Bot integration
├── handler/
│   ├── BotSpawnHandler.java
│   ├── BotDeathHandler.java
│   └── BotCombatHandler.java
├── strategy/
│   └── CustomStrategy.java
└── util/
    └── BotHelper.java
```

### Separate Integration Logic

Keep PVP Bot integration separate from your main mod logic:

```java
// Good
public class YourMod implements ModInitializer {
    @Override
    public void onInitialize() {
        initCore();
        if (isPvpBotLoaded()) {
            PvpBotIntegration.init();
        }
    }
}

// Bad - mixing concerns
public class YourMod implements ModInitializer {
    @Override
    public void onInitialize() {
        initCore();
        PvpBotAPI.getEventManager().registerSpawnHandler(...);
        PvpBotAPI.getEventManager().registerDeathHandler(...);
        // ... lots of integration code
    }
}
```

## Event Handling

### Keep Handlers Lightweight

Event handlers should be fast and non-blocking:

```java
// Good - fast operation
events.registerSpawnHandler(bot -> {
    botStats.increment(bot.getName().getString());
});

// Bad - slow operation
events.registerSpawnHandler(bot -> {
    // Don't do heavy I/O in handlers!
    saveToDatabase(bot);
    sendWebhook(bot);
    generateReport(bot);
});
```

### Use Tick Handlers Sparingly

Tick events run 20 times per second per bot:

```java
// Good - throttled updates
events.registerTickHandler(bot -> {
    if (bot.age % 100 == 0) { // Every 5 seconds
        updateBotStatus(bot);
    }
});

// Bad - runs every tick
events.registerTickHandler(bot -> {
    updateBotStatus(bot); // Called 20 times/second!
});
```

### Handle Exceptions Gracefully

Always wrap risky operations:

```java
// Good
events.registerAttackHandler((bot, target) -> {
    try {
        return customAttackLogic(bot, target);
    } catch (Exception e) {
        System.err.println("Error in attack handler: " + e.getMessage());
        return false; // Don't cancel on error
    }
});

// Bad - can crash event system
events.registerAttackHandler((bot, target) -> {
    return riskyOperation(bot, target); // May throw exception
});
```

### Return Correct Values

For cancellable events, return `true` only when you want to cancel:

```java
// Good
events.registerAttackHandler((bot, target) -> {
    if (shouldPreventAttack(bot, target)) {
        return true; // Cancel attack
    }
    return false; // Allow attack
});

// Bad - always cancels
events.registerAttackHandler((bot, target) -> {
    logAttack(bot, target);
    return true; // Oops! Cancels all attacks
});
```

## Combat Strategies

### Set Appropriate Priorities

Higher priority strategies execute first:

```java
// Critical strategies: 100+
public int getPriority() { return 150; }

// Normal strategies: 50-100
public int getPriority() { return 75; }

// Fallback strategies: 0-50
public int getPriority() { return 25; }
```

### Implement Proper Checks

Use `canUse()` to validate conditions:

```java
@Override
public boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings) {
    // Check settings
    if (!settings.isCombatEnabled()) return false;
    
    // Check distance
    double distance = bot.distanceTo(target);
    if (distance > settings.getMeleeRange()) return false;
    
    // Check inventory
    if (!hasRequiredItem(bot)) return false;
    
    // Check cooldown
    if (isOnCooldown(bot)) return false;
    
    return true;
}
```

### Use Reasonable Cooldowns

Prevent strategy spam:

```java
// Good - reasonable cooldowns
@Override
public int getCooldown() {
    return 40; // 2 seconds
}

// Bad - too short
@Override
public int getCooldown() {
    return 1; // Spams every tick!
}

// Bad - too long
@Override
public int getCooldown() {
    return 6000; // 5 minutes is too long
}
```

### Return Accurate Results

Return `true` only if strategy executed successfully:

```java
@Override
public boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server) {
    if (performAttack(bot, target)) {
        return true; // Success
    }
    return false; // Failed - try next strategy
}
```

## Performance Optimization

### Cache Expensive Queries

Don't query repeatedly:

```java
// Good - cache results
private final Set<String> botCache = new HashSet<>();

public void updateCache() {
    botCache.clear();
    botCache.addAll(PvpBotAPI.getAllBots());
}

public boolean isBotCached(String name) {
    return botCache.contains(name);
}

// Bad - queries every time
public boolean isBot(String name) {
    return PvpBotAPI.getAllBots().contains(name); // Expensive!
}
```

### Batch Operations

Group multiple operations:

```java
// Good - batch processing
events.registerTickHandler(bot -> {
    if (bot.age % 20 == 0) { // Every second
        List<Task> tasks = getPendingTasks(bot);
        tasks.forEach(task -> task.execute(bot));
    }
});

// Bad - scattered operations
events.registerTickHandler(bot -> {
    checkHealth(bot);
    checkHunger(bot);
    checkArmor(bot);
    checkWeapon(bot);
    // ... many small checks every tick
});
```

### Use Efficient Data Structures

Choose appropriate collections:

```java
// Good - O(1) lookup
private final Map<String, BotData> botData = new HashMap<>();

// Bad - O(n) lookup
private final List<BotData> botData = new ArrayList<>();
```

### Avoid Memory Leaks

Clean up resources:

```java
// Good - cleanup on death
events.registerDeathHandler(bot -> {
    String name = bot.getName().getString();
    botData.remove(name);
    activeStrategies.remove(name);
    cooldowns.remove(name);
});

// Bad - never cleanup
events.registerSpawnHandler(bot -> {
    botData.put(bot.getName().getString(), new BotData());
    // Never removed - memory leak!
});
```

## Error Handling

### Validate Input

Always check for null and invalid values:

```java
// Good
public void processBot(MinecraftServer server, String name) {
    if (server == null || name == null || name.isEmpty()) {
        System.err.println("Invalid input");
        return;
    }
    
    ServerPlayerEntity bot = PvpBotAPI.getBot(server, name);
    if (bot == null) {
        System.err.println("Bot not found: " + name);
        return;
    }
    
    // Process bot
}

// Bad
public void processBot(MinecraftServer server, String name) {
    ServerPlayerEntity bot = PvpBotAPI.getBot(server, name);
    bot.getHealth(); // NullPointerException if bot not found!
}
```

### Log Errors Properly

Use appropriate log levels:

```java
// Good - informative logging
try {
    executeStrategy(bot, target);
} catch (Exception e) {
    System.err.println("[YourMod] Error executing strategy for " + 
                       bot.getName().getString() + ": " + e.getMessage());
    e.printStackTrace();
}

// Bad - silent failure
try {
    executeStrategy(bot, target);
} catch (Exception e) {
    // Ignored - hard to debug!
}
```

### Fail Gracefully

Don't crash the game:

```java
// Good - graceful degradation
events.registerAttackHandler((bot, target) -> {
    try {
        return customLogic(bot, target);
    } catch (Exception e) {
        System.err.println("Error in attack handler: " + e.getMessage());
        return false; // Allow default behavior
    }
});

// Bad - propagates exception
events.registerAttackHandler((bot, target) -> {
    return riskyOperation(bot, target); // May crash game!
});
```

## Configuration

### Respect Bot Settings

Check settings before overriding behavior:

```java
// Good - respects settings
if (settings.isCombatEnabled() && settings.isAutoTargetEnabled()) {
    findAndAttackTarget(bot);
}

// Bad - ignores settings
findAndAttackTarget(bot); // Forces combat even if disabled
```

### Provide Configuration Options

Make your addon configurable:

```java
public class YourModConfig {
    private boolean enableBotIntegration = true;
    private int updateInterval = 20;
    private double customRange = 10.0;
    
    // Getters and setters
}
```

### Use Reasonable Defaults

Choose sensible default values:

```java
// Good - reasonable defaults
private int checkInterval = 20; // 1 second
private double maxDistance = 32.0; // 32 blocks
private int maxBots = 100; // Reasonable limit

// Bad - extreme defaults
private int checkInterval = 1; // Every tick - too frequent!
private double maxDistance = 1000.0; // 1000 blocks - too far!
private int maxBots = Integer.MAX_VALUE; // No limit - dangerous!
```

## Testing

### Test All Event Types

Ensure all handlers work:

```java
@Override
public void onInitialize() {
    if (isDevEnvironment()) {
        testEventHandlers();
    }
}

private void testEventHandlers() {
    System.out.println("Testing PVP Bot integration...");
    
    // Register test handlers
    PvpBotAPI.getEventManager().registerSpawnHandler(bot -> {
        System.out.println("✓ Spawn event works");
    });
    
    // ... test other events
}
```

### Test Edge Cases

Handle unusual situations:

```java
// Test with null values
events.registerDamageHandler((bot, attacker, damage) -> {
    if (attacker == null) {
        // Handle environmental damage
    }
    return false;
});

// Test with extreme values
if (damage > 1000.0f) {
    // Handle one-shot kills
}

// Test with dead bots
if (!bot.isAlive()) {
    return false;
}
```

### Use Debug Logging

Add debug output during development:

```java
private static final boolean DEBUG = false;

events.registerAttackHandler((bot, target) -> {
    if (DEBUG) {
        System.out.println("Attack: " + bot.getName().getString() + 
                          " -> " + target.getName().getString());
    }
    return false;
});
```

## Documentation

### Document Your API

Add JavaDoc to public methods:

```java
/**
 * Checks if the bot should flee from combat
 * 
 * @param bot The bot to check
 * @return true if bot should flee
 */
public boolean shouldFlee(ServerPlayerEntity bot) {
    return bot.getHealth() < bot.getMaxHealth() * 0.3;
}
```

### Provide Examples

Include usage examples:

```java
/**
 * Custom combat strategy example
 * 
 * Usage:
 * <pre>
 * CombatStrategyRegistry.getInstance().register(new FleeStrategy());
 * </pre>
 */
public class FleeStrategy implements CombatStrategy {
    // Implementation
}
```

### Write README

Document your addon:

```markdown
# Your Addon Name

## Features
- Feature 1
- Feature 2

## Installation
1. Download PVP Bot
2. Download this addon
3. Place both in mods folder

## Configuration
Edit config/yourmod.json

## API Usage
See examples/ directory
```

## Version Compatibility

### Check API Version

Verify compatibility at runtime:

```java
String apiVersion = PvpBotAPI.getApiVersion();
String[] parts = apiVersion.split("\\.");
int major = Integer.parseInt(parts[0]);

if (major < 1) {
    System.err.println("Warning: Unsupported API version: " + apiVersion);
}
```

### Handle API Changes

Prepare for future changes:

```java
try {
    // Try new API
    PvpBotAPI.newMethod();
} catch (NoSuchMethodError e) {
    // Fall back to old API
    PvpBotAPI.oldMethod();
}
```

### Document Requirements

Specify version requirements:

```json
{
  "depends": {
    "pvp_bot": ">=0.0.1"
  }
}
```

## Security

### Validate Bot Identity

Confirm bot is legitimate:

```java
public boolean isValidBot(ServerPlayerEntity player) {
    String name = player.getName().getString();
    return PvpBotAPI.isBot(name) && 
           PvpBotAPI.getAllBots().contains(name);
}
```

### Limit Permissions

Don't give bots excessive access:

```java
// Good - limited permissions
if (PvpBotAPI.isBot(player.getName().getString())) {
    // Bots can't use admin commands
    return false;
}

// Bad - bots have full access
// (No checks)
```

### Sanitize Input

Clean user-provided data:

```java
public void processBotName(String name) {
    // Remove dangerous characters
    name = name.replaceAll("[^a-zA-Z0-9_]", "");
    
    // Limit length
    if (name.length() > 16) {
        name = name.substring(0, 16);
    }
    
    // Process safe name
}
```

## Common Pitfalls

### ❌ Don't Modify Bot Directly

```java
// Bad - bypasses bot systems
bot.setHealth(20.0f);
bot.getInventory().clear();

// Good - use events to react
events.registerDamageHandler((bot, attacker, damage) -> {
    // React to damage, don't modify directly
    return false;
});
```

### ❌ Don't Block Server Thread

```java
// Bad - blocks server
events.registerSpawnHandler(bot -> {
    Thread.sleep(1000); // Freezes server!
});

// Good - async operation
events.registerSpawnHandler(bot -> {
    CompletableFuture.runAsync(() -> {
        // Long operation
    });
});
```

### ❌ Don't Ignore Return Values

```java
// Bad - ignores cancellation
events.registerAttackHandler((bot, target) -> {
    logAttack(bot, target);
    // Forgot to return value!
});

// Good - returns properly
events.registerAttackHandler((bot, target) -> {
    logAttack(bot, target);
    return false; // Allow attack
});
```

### ❌ Don't Leak Memory

```java
// Bad - never cleaned up
private final Map<String, List<Event>> botEvents = new HashMap<>();

events.registerSpawnHandler(bot -> {
    botEvents.put(bot.getName().getString(), new ArrayList<>());
});

// Good - cleanup on death
events.registerDeathHandler(bot -> {
    botEvents.remove(bot.getName().getString());
});
```

## Checklist

Before releasing your addon:

- [ ] All event handlers have exception handling
- [ ] Tick handlers are throttled appropriately
- [ ] Resources are cleaned up on bot death
- [ ] Input is validated before use
- [ ] Configuration has reasonable defaults
- [ ] Code is documented with JavaDoc
- [ ] README includes installation instructions
- [ ] Tested with multiple bots
- [ ] Tested edge cases (null values, dead bots)
- [ ] No memory leaks
- [ ] No server thread blocking
- [ ] Compatible with latest PVP Bot version
- [ ] fabric.mod.json declares dependencies
- [ ] Version number follows semantic versioning

## Next Steps

- [API Reference] - Complete method documentation
- [Events] - Detailed event system guide
- [Combat Strategies] - Create custom combat logic
- [Examples] - Ready-to-use code examples
- [Mod Integration] - Integration guide
