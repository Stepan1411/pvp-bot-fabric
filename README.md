
![MIEDTMBMBRBOG](https://cdn.modrinth.com/data/cached_images/47af968dac4bcb1e69d6ef5bfd50c13d36725f21.png)

A Minecraft Fabric mod that adds intelligent PvP bots powered by Carpet mod. Bots can fight with swords, bows, maces, eat golden apples, retreat when low HP, navigate around obstacles with bunny hop, and organize into hostile factions. Perfect for PvP training, server events, or just having fun battles!

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
- Eating golden apples while retreating

### Navigation System
- Smart obstacle avoidance (walls, holes)
- Climbing ladders and vines
- Jumping over 1-block obstacles
- Bunny hop (bhop) for faster movement
- Configurable jump boost
- Idle wandering when no target (bots walk around their spawn point)

### Kit System
- Save player inventory as a kit
- Give kits to individual bots or entire factions
- Persistent kit storage

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
/pvpbot target <bot> - Show current target
```

### Kits
```
/pvpbot createkit <name> - Create kit from your inventory
/pvpbot deletekit <name> - Delete a kit
/pvpbot kits - List all kits
/pvpbot givekit <bot> <kit> - Give kit to a bot
/pvpbot faction givekit <faction> <kit> - Give kit to entire faction
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

# Combat
/pvpbot settings combat <true/false> - Enable/disable combat
/pvpbot settings revenge <true/false> - Attack the attacker
/pvpbot settings autotarget <true/false> - Auto-search for enemies
/pvpbot settings targetplayers <true/false> - Target players
/pvpbot settings targetmobs <true/false> - Target hostile mobs
/pvpbot settings targetbots <true/false> - Target other bots
/pvpbot settings criticals <true/false> - Critical hits
/pvpbot settings ranged <true/false> - Use bow/crossbow
/pvpbot settings mace <true/false> - Use mace
/pvpbot settings attackcooldown <1-40> - Attack cooldown (ticks)
/pvpbot settings meleerange <2-6> - Melee attack range
/pvpbot settings movespeed <0.1-2.0> - Movement speed

# Navigation
/pvpbot settings bhop <true/false> - Enable bunny hop
/pvpbot settings bhopcooldown <5-30> - Bhop cooldown (ticks)
/pvpbot settings jumpboost <0.0-0.5> - Extra jump height
/pvpbot settings idle <true/false> - Enable idle wandering
/pvpbot settings idleradius <3-50> - Idle wander radius

# Equipment
/pvpbot settings autoarmor <true/false> - Auto-equip armor
/pvpbot settings autoweapon <true/false> - Auto-equip weapon
/pvpbot settings droparmor <true/false> - Drop worse armor
/pvpbot settings dropweapon <true/false> - Drop worse weapons
/pvpbot settings dropdistance <1-10> - Drop pickup distance
/pvpbot settings interval <1-100> - Check interval (ticks)
/pvpbot settings minarmorlevel <0-100> - Minimum armor level

# Realism
/pvpbot settings misschance <0-100> - Miss chance (%)
/pvpbot settings mistakechance <0-100> - Mistake chance (%)
/pvpbot settings reactiondelay <0-20> - Reaction delay (ticks)

# Other
/pvpbot settings viewdistance <5-128> - View range
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
```

### Using kits:
```
# Put items in your inventory, then:
/pvpbot createkit pvp

# Give kit to all bots in faction:
/pvpbot faction givekit Red pvp
/pvpbot faction givekit Blue pvp
```

Now Red team bots will automatically attack Blue team bots!

## Configuration

Settings are saved in:
- `config/pvp_bot.json` - main settings
- `config/pvp_bot_factions.json` - factions
- `config/pvp_bot_kits.json` - kits
