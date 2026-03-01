# Quick Start

This guide will help you start developing addons for PVP Bot Fabric.

## Requirements

- Java 21+
- Fabric Loader
- Minecraft 1.21+
- PVP Bot Fabric mod

## Project Setup

### 1. Create a new Fabric mod

Use [Fabric Template Mod](https://github.com/FabricMC/fabric-example-mod) as a base.

### 2. Add dependency

In `build.gradle`:

```gradle
repositories {
    maven { url 'https://jitpack.io' }
}

dependencies {
    modImplementation "com.github.Stepan1411:pvp-bot-fabric:VERSION"
}
```

### 3. Update fabric.mod.json

Add PVP Bot dependency:

```json
{
  "depends": {
    "pvp-bot-fabric": "*"
  }
}
```

## First Addon

Create initialization class:

```java
package com.example.myaddon;

import net.fabricmc.api.ModInitializer;
import org.stepan1411.pvp_bot.api.PvpBotAPI;

public class MyAddon implements ModInitializer {
    
    @Override
    public void onInitialize() {
        System.out.println("My PVP Bot Addon loaded!");
        
        // Register spawn handler
        PvpBotAPI.getEventManager().registerSpawnHandler(bot -> {
            System.out.println("Bot " + bot.getName().getString() + " spawned!");
        });
        
        // Register death handler
        PvpBotAPI.getEventManager().registerDeathHandler(bot -> {
            System.out.println("Bot " + bot.getName().getString() + " died!");
        });
    }
}
```

## Testing

1. Build your mod: `./gradlew build`
2. Place JAR in `mods` folder
3. Launch Minecraft
4. Spawn a bot: `/pvpbot spawn TestBot`
5. Check logs - you should see messages from your addon

## Next Steps

- [Events] - learn about all available events
- [Combat Strategies] - create custom combat logic
- [API Reference] - complete API documentation
- [Examples] - ready-to-use code examples
