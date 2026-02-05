
![MIEDTMBMBRBOG](https://cdn.modrinth.com/data/cached_images/47af968dac4bcb1e69d6ef5bfd50c13d36725f21.png)

# PVP Bot Fabric

> **An advanced mod for Minecraft Fabric that adds AI bots for PvP**

This mod adds bots to the server with advanced combat AI, a faction system, automatic equipment, and navigation. Bots can fight players, mobs, and each other, using a variety of weapons and tactics.

---

## 🎮 Main Features

### 🤖 Bot Management
- **Bot spawn** with unique names (random name generation)
- **Mass spawn** of up to 50 bots simultaneously
- **Automatic restoration** of bots after a server restart
- **Synchronization** of the bot list with the server

### ⚔️ Combat System
- **Advanced combat AI** with various tactics
- **Critical hits** when jumping
- **Many weapon types**:
- Melee: swords, axes
- Ranged: bows, crossbows (IN DEVELOPMENT)
- Mace with jumping attacks
- Crystal PVP (obsidian + crystals)
- Anchor PVP (respawn anchor + glowstone)
- Spear - 1.21.11+ (In IN DEVELOPMENT)

### 🛡️ Utilities and Survival
- **Auto-equip** armor and weapons
- **Auto-totem** offhand
- **Auto-shield** on low HP
- **Auto-food**
- **Auto-potions** healing, strength, speed, fire resistance
- **Auto-repair** armor with Mending XP bottles
- **Shield down**
- **Web use**

### 🚶 Navigation and Movement
- **Bunny hop**
- **Adjustable speed**
- **Wander mode** when no target
- **Retreat** on low HP
- **Smart navigation** to targets (and items IN DEVELOPMENT)

### 👥 Faction System
- **Faction creation** for bots
- **Allies and enemies** - bots do not attack allies
- **Member Management**
- **Friendly Fire**

### 🎒 Kit System
- **Creating Sets**
- **Giving Out kits**
- **Saving kits**

### 🎯 Realism and Bugs
- **Miss Chance**
- **Error Chance**
- **Reaction Latency**

### 📊 Statistics
- **Anonymous Statistics**
- **Website** https://stepan1411.github.io/pvpbot-stats/

---

## 📋 Commands

### Basic Commands
```
/pvpbot spawn [name] - Spawn a bot (no name = random)
```
```
/pvpbot massspawn <count> - Spawn multiple bots (1-50)
```
```
/pvpbot remove <name> - Remove bot
```
```
/pvpbot removeall - Remove all bots
```
```
/pvpbot list - List all bots
```
```
/pvpbot menu - Open the test menu
```

### Settings
```
/pvpbot settings - Show all settings
```
```
/pvpbot settings autoarmor [true/false] - Auto-equip armor
```
```
/pvpbot settings autoweapon [true/false] - Auto-equip weapons
```
```
/pvpbot settings droparmor [true/false] - Drop inferior armor
```
```
/pvpbot settings dropweapon [true/false] - Drop inferior weapons
```
```
/pvpbot settings dropdistance <1-10> - Item pickup distance
```
```
/pvpbot settings minarmorlevel <0-100> - Minimum armor level
```
```
/pvpbot settings interval <1-100> - Check interval (ticks)
```

### Combat
```
/pvpbot settings combat [true/false] - Enable combat
```
```
/pvpbot settings revenge [true/false] - Revenge mode
```
```
/pvpbot settings autotarget [true/false] - Automatic target search
```
```
/pvpbot settings targetplayers [true/false] - Attack players
```
```
/pvpbot settings targetmobs [true/false] - Attack mobs
```
```
/pvpbot settings targetbots [true/false] - Attack other bots
```
```
/pvpbot settings criticals [true/false] - Critical hits
```
```
/pvpbot settings ranged [true/false] - Use bow/crossbow
```
```
/pvpbot settings mace [true/false] - Use mace
```
```
/pvpbot settings attackcooldown <1-40> - Attack cooldown (ticks)
```
```
/pvpbot settings meleerange <2-6> - Melee range
```
```
/pvpbot settings movespeed <0.1-2.0> - Movement speed
```

### Utilities
```
/pvpbot settings autoshield [true/false] - Autoshield
```
```
/pvpbot settings shieldbreak [true/false] - Break shields with an axe
```
```
/pvpbot settings prefersword [true/false] - Prefer sword over axe
```

### Navigation
```
/pvpbot settings bhop [true/false] - Bunny hop
```
```
/pvpbot settings bhopcooldown <5-30> - Jump cooldown (ticks)
```
```
/pvpbot settings jumpboost <0-0.5> - Height Jump
```
```
/pvpbot settings idle [true/false] - Wander without a target
```
```
/pvpbot settings idleradius <3-50> - Wander radius
```

### Realism
```
/pvpbot settings friendlyfire [true/false] - Damage to allies
```
```
/pvpbot settings misschance <0-100> - Miss chance (%)
```
```
/pvpbot settings mistakechance <0-100> - Error chance (%)
```
```
/pvpbot settings reactiondelay <0-20> - Reaction delay (ticks)
```

### Factions
```
/pvpbot faction create <name> - Create a faction
```
```
/pvpbot faction delete <name> - Delete a faction
```
```
/pvpbot faction join <faction> <player> - Add a player/bot
```
```
/pvpbot faction leave <faction> <player> - Delete player/bot
```
```
/pvpbot faction list - List of factions
```
```
/pvpbot faction info <name> - Faction information
```
```
/pvpbot faction ally <faction1> <faction2> - Create
```

## 🖼️ Screenshots
> - Bots in battle
<img width="1920" height="1009" alt="2026-02-04_22 48 36" src="https://github.com/user-attachments/assets/168fd083-5157-491b-a129-fb256f351681" />

 - Settings GUI
<img width="205" height="201" alt="изображение" src="https://github.com/user-attachments/assets/bba55564-ac0d-4b49-af04-076da8c97df1" />
<img width="525" height="380" alt="изображение" src="https://github.com/user-attachments/assets/389e15c0-ae95-4a51-b3d5-901424bbad63" />
<img width="525" height="381" alt="изображение" src="https://github.com/user-attachments/assets/acac0102-aeb5-4ca1-98e2-fb71925effb7" />

 - Faction system
<img width="1920" height="1009" alt="изображение" src="https://github.com/user-attachments/assets/a11d896f-f454-4333-a4b0-cf328ff9f437" />
<img width="1920" height="1009" alt="изображение" src="https://github.com/user-attachments/assets/06d4bf94-824d-4307-b463-bf8e4de0fd4e" />

 - Crystal PVP in action
<img width="1920" height="1009" alt="изображение" src="https://github.com/user-attachments/assets/e63bb460-97e5-45f7-bc94-8db54e299222" />
<img width="1920" height="1009" alt="изображение" src="https://github.com/user-attachments/assets/522a5072-5a28-4e60-abd3-53a183039a1c" />

> - Mass bot spawn
<img width="1691" height="888" alt="изображение" src="https://github.com/user-attachments/assets/ecfcd7c4-f430-4765-8391-09d3d4761cb6" />



## 🔗 Links

- **Modrinth**: https://modrinth.com/mod/pvp-bot-fabric
- **GitHub**: https://github.com/Stepan1411/pvp-bot-fabric
- **Issues**: https://github.com/Stepan1411/pvp-bot-fabric/issues
- **Wiki**: https://github.com/Stepan1411/pvp-bot-fabric/wiki
- **Statistics**: https://stepan1411.github.io/pvp-bot-fabric/

**Have a nice game! 🎮**
