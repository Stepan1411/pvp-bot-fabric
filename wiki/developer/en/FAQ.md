# FAQ - Frequently Asked Questions

## General Questions

### What Minecraft version is supported?

API works with Minecraft 1.21+ and Fabric Loader.

### Do I need additional mods?

Only PVP Bot Fabric and Carpet Mod (PVP Bot dependency).

### Can I use the API on a server?

Yes, the API works on both client and server.

## Development

### How do I start developing an addon?

1. Read [Quick Start]
2. Study [Examples]
3. Create your first event handler

### Where can I find code examples?

- [Examples] in documentation
- [GitHub repository](https://github.com/Stepan1411/pvp-bot-fabric)

### How do I debug my addon?

```java
// Add logging
private static final Logger LOGGER = LoggerFactory.getLogger("myaddon");

LOGGER.info("My addon loaded");
LOGGER.debug("Bot spawned: {}", bot.getName());
```

Run with `-Dmyaddon.debug=true` flag

## Events

### How do I cancel a bot attack?

```java
PvpBotAPI.getEventManager().registerAttackHandler((bot, target) -> {
    if (shouldCancelAttack(target)) {
        return true; // true = cancel
    }
    return false; // false = allow
});
```

### How often is BotTickHandler called?

20 times per second (every tick). Use modulo to reduce frequency:

```java
registerTickHandler(bot -> {
    if (bot.age % 20 == 0) { // Once per second
        // Your code
    }
});
```

### Can I change handler execution order?

Handlers execute in registration order. Register important handlers first.

## Combat Strategies

### How do I set strategy priority?

```java
@Override
public int getPriority() {
    return 150; // Higher = executes earlier
}
```

Recommended ranges:
- 200+ - critical (escape, healing)
- 100-199 - high (buffs)
- 50-99 - medium (special attacks)
- 1-49 - low (basic attacks)

### How does cooldown work?

```java
@Override
public int getCooldown() {
    return 100; // 100 ticks = 5 seconds
}
```

Strategy won't be used again until cooldown expires.

### Can I use multiple strategies simultaneously?

Yes, strategies execute by priority. If `canUse()` returns true, strategy executes.

## Performance

### My handler is lagging the game. What should I do?

1. Reduce execution frequency:
```java
if (bot.age % 100 == 0) { // Once every 5 seconds
    // Heavy operation
}
```

2. Use caching
3. Optimize algorithms

### How many bots can the API handle?

API is optimized for hundreds of bots. Limit depends on your code.

## Compatibility

### Does the API work with other mods?

Yes! See [Mod Integration] for examples.

### What if my mod conflicts with another?

1. Check dependency versions
2. Use soft dependencies
3. Handle exceptions

### How do I check if another mod is installed?

```java
try {
    Class.forName("com.example.OtherMod");
    // Mod is installed
} catch (ClassNotFoundException e) {
    // Mod is not installed
}
```

## Errors

### "Cannot find symbol: PvpBotAPI"

Make sure you added the dependency in `build.gradle`:

```gradle
dependencies {
    modImplementation "com.github.Stepan1411:pvp-bot-fabric:VERSION"
}
```

### "NoSuchMethodError" at runtime

API version mismatch. Update dependency to latest version.

### Bot doesn't respond to my events

1. Check that handler is registered
2. Add logging for debugging
3. Ensure conditions in `canUse()` are met

### Combat Strategy not being called

**Problem:** Your `CombatStrategy` is registered but `canUse()` and `execute()` are never called.

**Solution:** This was fixed in version 1.1.0+. Make sure you're using the latest version. The combat strategies are now properly integrated into the bot combat loop.

**Verification:**
```java
CombatStrategyRegistry.getInstance().register(myStrategy);
System.out.println("Strategy registered: " + myStrategy.getName());
```

### BotAttackHandler returns true but attack still happens

**Problem:** Returning `true` from `BotAttackHandler.onBotAttack()` doesn't cancel the attack.

**Solution:** This was fixed in version 1.1.0+. The attack cancellation now works correctly. The handler is called before the attack is executed, and returning `true` will prevent the attack.

**Example:**
```java
PvpBotAPI.getEventManager().registerAttackHandler((bot, target) -> {
    if (target.getName().getString().equals("FriendlyPlayer")) {
        return true; // Attack will be cancelled
    }
    return false; // Attack will proceed
});
```

### "Cannot resolve method getServer()" in bot code

**Problem:** Documentation shows `bot.getServer()` but this method doesn't exist in Minecraft 1.21.1.

**Solution:** Use the `server` parameter passed to your method, or get the world:

```java
// WRONG (doesn't exist):
MinecraftServer server = bot.getServer();

// CORRECT (use parameter):
public boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server) {
    // server is already available as parameter
}

// CORRECT (get world):
ServerWorld world = (ServerWorld) bot.getEntityWorld();
```

## Publishing

### How do I publish my addon?

1. Create GitHub repository
2. Publish on Modrinth or CurseForge
3. Add README and screenshots
4. Specify PVP Bot Fabric dependency

### Do I need a license?

Recommended to use MIT or Apache 2.0 for compatibility.

### How do I update my addon?

1. Follow SemVer
2. Maintain CHANGELOG
3. Test with new API versions

## Support

### Where can I get help?

- [GitHub Issues](https://github.com/Stepan1411/pvp-bot-fabric/issues)

### How do I report a bug?

1. Verify bug is reproducible
2. Create issue on GitHub
3. Attach logs and code
4. Describe reproduction steps

### Can I contribute to the API?

Yes! Pull requests are welcome:

1. Fork repository
2. Create feature branch
3. Write tests
4. Submit PR

## Licensing

### What license is the API under?

See LICENSE file in repository.

### Can I use the API in commercial projects?

Yes, if allowed by the license.
