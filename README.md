
![MIEDTMBMBRBOG](https://cdn.modrinth.com/data/cached_images/47af968dac4bcb1e69d6ef5bfd50c13d36725f21.png)

A mod for Minecraft Fabric that adds smart PvP bots based on the Carpet mod.

## Requirements

- Minecraft 1.21+
- Fabric Loader
- Carpet Mod

## Features

### Combat System
- Melee (swords, axes)
- Ranged (bows, crossbows)
- Mace with Wind Charge for high jumps
- Critical hits
- Shield knockdown with an axe
- Automatic retreat when HP is low

### Faction System
- Create factions for bots and players
- Hostile relations between factions
- Bots automatically attack enemies from hostile factions

### Automatic Features
- Auto-equip the best armor
- Auto-totem
- Auto-eat when HP is low or hungry
- Auto-shield when attacking an enemy

### Realism
- Adjustable miss chance
- Error chance (attacking the wrong way)
- Reaction delay

## Commands

### Controls Bots
```
/pvpbot spawn <name> - Create a bot
/pvpbot remove <name> - Remove a bot
/pvpbot removeall - Remove all bots
/pvpbot list - List of bots
/pvpbot inventory <name> - Show a bot's inventory
```

### Battle Commands
```
/pvpbot attack <bot> <target> - Order a bot to attack
/pvpbot stop <bot> - Stop an attack
```

### Factions
```
/pvpbot faction create <name> - Create a faction
/pvpbot faction delete <name> - Remove a faction
/pvpbot faction add <faction> <player> - Add to faction
/pvpbot faction remove <faction> <player> - Remove from faction
/pvpbot faction hostile <f1> <f2> [true/false] - Make factions hostile
/pvpbot faction addnear <faction> <radius> - Add all bots within range
/pvpbot faction give <faction> <item> - Give an item to the entire faction
/pvpbot faction list - List of factions
/pvpbot faction info <faction> - Faction information
```

### Settings
```
/pvpbot settings - Show all settings
/pvpbot settings viewdistance <5-128> - View range
/pvpbot settings combat <true/false> - Enable/disable combat
/pvpbot settings revenge <true/false> - Attack the attacker
/pvpbot settings autotarget <true/false> - Auto-search for enemies
/pvpbot settings criticals <true/false> - Critical blows
/pvpbot settings ranged <true/false> - Use bow
/pvpbot settings mace <true/false> - Use a mace
```

## Usage example

### Creating two warring teams:
```
/pvpbot spawn Red1
/pvpbot spawn Red2
/pvpbot spawn Blue1
/pvpbot spawn Blue2

/pvpbot faction create Red
/pvpbot faction create Blue

/pvpbot faction add Red Red1
/pvpbot faction add Red Red2
/pvpbot faction add Blue Blue1
/pvpbot faction add Blue Blue2

/pvpbot faction hostile Red Blue

/pvpbot faction give Red diamond_sword
/pvpbot faction give Blue diamond_sword
/pvpbot faction give Red diamond_chestplate
/pvpbot faction Give Blue diamond_chestplate
```

Now Red team bots will automatically attack Blue team bots!

## Configuration

Settings are saved in:
- `config/pvp_bot.json` - main settings
- `config/pvp_bot_factions.json` - factions
