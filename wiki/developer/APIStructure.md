# API Structure

This document provides an architectural overview of the PVP Bot API.

## Architecture Overview

PVP Bot follows a modular, event-driven architecture with clear separation between public API and internal implementation:

```
┌─────────────────────────────────────────┐
│         Your Addon/Mod                  │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         PvpBotAPI (Facade)              │
│  - getEventManager()                    │
│  - getAllBots()                         │
│  - getBot()                             │
│  - getBotSettings()                     │
└──────────────┬──────────────────────────┘
               │
    ┌──────────┼──────────┬───────────┐
    │          │          │           │
┌───▼───┐  ┌──▼──┐  ┌────▼────┐  ┌──▼──────┐
│Events │  │Bots │  │Settings │  │Strategy │
│Manager│  │     │  │         │  │Registry │
└───────┘  └─────┘  └─────────┘  └─────────┘
```

## Core Components

### 1. PvpBotAPI (Facade)

**Location:** `org.stepan1411.pvp_bot.api.PvpBotAPI`

Main entry point for all API interactions. Provides static methods for:

- **Bot Management:** Query bots, check if player is bot, get bot count
- **Event System:** Access to event manager
- **Configuration:** Access to bot settings
- **Statistics:** Get spawn/kill counts

**Key Methods:**
```java
// Bot queries
Set<String> getAllBots()
ServerPlayerEntity getBot(MinecraftServer server, String name)
boolean isBot(String playerName)
int getBotCount()

// System access
BotEventManager getEventManager()
BotSettings getBotSettings()

// Statistics
int getTotalBotsSpawned()
int getTotalBotsKilled()

// Version
String getApiVersion()
```

### 2. Event System

**Location:** `org.stepan1411.pvp_bot.api.event`

Event-driven architecture for reacting to bot lifecycle and actions.

**Components:**
- `BotEventManager` - Central event dispatcher (singleton)
- Event handler interfaces (functional interfaces for lambda support)

**Event Types:**

| Event | Handler Interface | Cancellable | Description |
|-------|------------------|-------------|-------------|
| Spawn | `BotSpawnHandler` | No | Bot spawned in world |
| Death | `BotDeathHandler` | No | Bot died |
| Attack | `BotAttackHandler` | Yes | Bot attacks entity |
| Damage | `BotDamageHandler` | Yes | Bot receives damage |
| Tick | `BotTickHandler` | No | Every tick per bot |

**Event Flow:**
```
Bot Action → Event Manager → Registered Handlers → Result
                                    ↓
                            (Exception handling)
```

### 3. Combat Strategy System

**Location:** `org.stepan1411.pvp_bot.api.combat`

Pluggable combat behavior system using Strategy pattern.

**Components:**
- `CombatStrategyRegistry` - Strategy manager (singleton)
- `CombatStrategy` - Interface for custom strategies

**Strategy Lifecycle:**
```
1. Register → 2. Sort by Priority → 3. Check canUse() → 4. Execute()
                                           ↓
                                    Wait for cooldown
```

**Strategy Interface:**
```java
interface CombatStrategy {
    String getName();
    int getPriority();
    boolean canUse(ServerPlayerEntity bot, Entity target, BotSettings settings);
    boolean execute(ServerPlayerEntity bot, Entity target, BotSettings settings, MinecraftServer server);
    int getCooldown(); // default: 20 ticks
}
```

### 4. Configuration System

**Location:** `org.stepan1411.pvp_bot.bot.BotSettings`

Centralized configuration management with 50+ settings.

**Categories:**
- Equipment (auto-equip armor/weapons)
- Combat (targeting, ranges, cooldowns)
- Weapons (melee, ranged, mace, spear, crystal PvP, anchor PvP)
- Utilities (totem, food, shield, potions, mending)
- Factions (team system)
- Navigation (pathfinding)

**Access Pattern:**
```java
BotSettings settings = PvpBotAPI.getBotSettings();
boolean combatEnabled = settings.isCombatEnabled();
double meleeRange = settings.getMeleeRange();
```

## Package Structure

```
org.stepan1411.pvp_bot
├── api/                          # Public API
│   ├── PvpBotAPI.java           # Main facade
│   ├── BotAPIIntegration.java   # Integration helper
│   ├── event/                   # Event system
│   │   ├── BotEventManager.java
│   │   ├── BotSpawnHandler.java
│   │   ├── BotDeathHandler.java
│   │   ├── BotAttackHandler.java
│   │   ├── BotDamageHandler.java
│   │   └── BotTickHandler.java
│   └── combat/                  # Combat strategies
│       ├── CombatStrategyRegistry.java
│       └── CombatStrategy.java
├── bot/                         # Internal implementation
│   ├── BotManager.java          # Bot lifecycle
│   ├── BotSettings.java         # Configuration
│   ├── BotTicker.java           # Update loop
│   ├── BotCombat.java           # Combat logic
│   ├── BotNavigation.java       # Movement
│   ├── BotEquipment.java        # Auto-equip
│   ├── BotUtils.java            # Utilities
│   ├── BotFaction.java          # Faction system
│   ├── BotPath.java             # Path following
│   ├── BotKits.java             # Equipment kits
│   ├── BotCrystalPvp.java       # Crystal PvP
│   ├── BotAnchorPvp.java        # Anchor PvP
│   └── BotDamageHandler.java    # Damage interception
├── command/                     # Command system
│   └── BotCommand.java
├── config/                      # Configuration
│   └── WorldConfigHelper.java
├── stats/                       # Statistics
│   └── StatsReporter.java
└── mixin/                       # Minecraft integration
    ├── ServerConfigHandlerMixin.java
    └── ServerPlayerEntityMixin.java
```

## Data Flow

### Bot Spawn Flow
```
Command → BotManager.spawn()
    ↓
Create fake player (Carpet)
    ↓
Initialize bot state
    ↓
Fire spawn event → Your handlers
    ↓
Add to active bots
    ↓
Start ticking
```

### Combat Flow
```
Tick → BotCombat.update()
    ↓
Find target (if auto-target enabled)
    ↓
Check custom strategies (priority order)
    ↓
Execute strategy OR default combat
    ↓
Fire attack event → Your handlers (can cancel)
    ↓
Perform attack
```

### Event Flow
```
Bot action → Internal code
    ↓
BotEventManager.fireXxxEvent()
    ↓
Iterate registered handlers
    ↓
Try-catch each handler (error isolation)
    ↓
Return result (for cancellable events)
```

## Lifecycle Management

### Server Lifecycle
```
SERVER_STARTED
    ↓
WorldConfigHelper.init()
    ↓
BotManager.init()
    ↓
Load bots from JSON
    ↓
Restore bots (if enabled)
    ↓
Start ticker
    ↓
Start stats reporter

SERVER_STOPPING
    ↓
Save bot data
    ↓
Stop stats reporter
    ↓
Reset state
```

### Bot Lifecycle
```
Spawn → Active → Death → Cleanup
  ↓       ↓        ↓        ↓
Event   Tick    Event    Remove
        Event            from set
```

## Thread Safety

- **Event Manager:** Thread-safe (handlers stored in ArrayList, modified only during registration)
- **Bot Manager:** Server thread only (uses Minecraft's server executor)
- **Settings:** Thread-safe (immutable after load, modifications synchronized)

## Performance Considerations

- **Tick Budget:** Bots update based on configurable intervals (default: 20 ticks)
- **Event Handlers:** Exceptions caught to prevent cascade failures
- **Auto-save:** Batched every 60 seconds (not per-change)
- **Dead Bot Cleanup:** Every 20 ticks (1 second)
- **Strategy Evaluation:** Short-circuits on first successful strategy

## Design Patterns Used

- **Facade Pattern:** PvpBotAPI simplifies complex subsystems
- **Singleton Pattern:** EventManager, StrategyRegistry, Settings, BotManager
- **Observer Pattern:** Event system with handler registration
- **Strategy Pattern:** Pluggable combat behaviors
- **State Pattern:** Combat state tracking
- **Factory Pattern:** Bot creation and initialization

## Integration Points

### For Addon Developers

1. **Event-Based Integration** (Recommended)
   - Register handlers for bot lifecycle events
   - React to bot actions
   - Cancel attacks/damage conditionally

2. **Strategy-Based Integration**
   - Register custom combat strategies
   - Control bot behavior in combat
   - Priority-based execution

3. **Query-Based Integration**
   - Check if player is bot
   - Get bot entities
   - Access bot statistics

4. **Configuration Integration**
   - Read bot settings
   - Adapt behavior based on config

### For Mod Developers

1. **Dependency Declaration**
   ```gradle
   repositories {
       maven { url 'https://jitpack.io' }
   }
   dependencies {
       modImplementation "com.github.Stepan1411:pvp-bot-fabric:VERSION"
   }
   ```

2. **Mod Metadata**
   ```json
   {
     "depends": {
       "pvp-bot-fabric": "*"
     }
   }
   ```

3. **Initialization**
   ```java
   public class MyMod implements ModInitializer {
       @Override
       public void onInitialize() {
           // Register handlers
           PvpBotAPI.getEventManager().registerSpawnHandler(...);
       }
   }
   ```

## Version Compatibility

- **API Version:** 1.0.0
- **Minecraft:** 1.21.10+
- **Fabric Loader:** 0.16.0+
- **Java:** 21+

Check API version at runtime:
```java
String version = PvpBotAPI.getApiVersion();
```

## Next Steps

- [API Reference] - Complete method documentation
- [Events] - Detailed event system guide
- [Combat Strategies] - Create custom combat logic
- [Examples] - Ready-to-use code examples
