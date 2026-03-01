# PVP Bot API - Developer Documentation

Welcome to the PVP Bot Fabric API documentation! This API allows you to create addons and extensions for the mod.

📑 [Full Documentation Index](INDEX.md) - quick access to all sections

## 📚 Contents

- [Quick Start](QuickStart.md) - start here
- [API Structure](APIStructure.md) - architecture overview
- [API Reference](APIReference.md) - complete method documentation
- [Events](Events.md) - event system
- [Combat Strategies](CombatStrategies.md) - custom combat logic
- [Examples](Examples.md) - ready-to-use code examples
- [Mod Integration](Integration.md) - working with other mods
- [Best Practices](BestPractices.md) - recommendations
- [FAQ](FAQ.md) - frequently asked questions

## 🎯 API Features

### Bot Events
- **Spawn** - when bot spawns
- **Death** - when bot dies
- **Attack** - when bot attacks
- **Damage** - when bot takes damage
- **Tick** - every tick for each bot

### Combat Strategies
- Create custom combat strategies
- Register them with priority
- Integrate with existing combat system

### Utilities
- Get list of bots
- Check if player is a bot
- Access bot settings
- Statistics

## 📦 Installation

Add dependency to your `build.gradle`:
[![](https://jitpack.io/v/Stepan1411/pvp-bot-fabric.svg)](https://jitpack.io/#Stepan1411/pvp-bot-fabric)

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    modImplementation "com.github.Stepan1411:pvp-bot-fabric:VERSION"
}
```

## 🚀 Quick Example

```java
import org.stepan1411.pvp_bot.api.PvpBotAPI;
import org.stepan1411.pvp_bot.api.event.BotEventManager;

public class MyAddon implements ModInitializer {
    @Override
    public void onInitialize() {
        // Register spawn event handler
        PvpBotAPI.getEventManager().registerSpawnHandler(bot -> {
            System.out.println("Bot spawned: " + bot.getName().getString());
        });
        
        // Register attack event handler
        PvpBotAPI.getEventManager().registerAttackHandler((bot, target) -> {
            System.out.println(bot.getName().getString() + " attacks " + target.getName().getString());
            return false; // false = don't cancel attack
        });
    }
}
```

## 📖 Additional Information

- [GitHub Repository](https://github.com/Stepan1411/pvp-bot-fabric)
- [Issues](https://github.com/Stepan1411/pvp-bot-fabric/issues)
- [Modrinth](https://modrinth.com/mod/pvp-bot-fabric)

## 📝 API Version

Current API version: **1.0.0**

Check version programmatically:
```java
String version = PvpBotAPI.getApiVersion();
```
