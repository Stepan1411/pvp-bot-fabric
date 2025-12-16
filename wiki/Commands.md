# 🎮 Commands

All PVP Bot commands start with `/pvpbot`. Requires permission level 2 (operator).

---

## 📋 Table of Contents

- [Bot Management](#-bot-management)
- [Combat Commands](#-combat-commands)
- [Faction Commands](#-faction-commands)
- [Kit Commands](#-kit-commands)
- [Settings](#-settings)

---

## 🤖 Bot Management

| Command | Description |
|---------|-------------|
| `/pvpbot spawn <name>` | Create a new bot |
| `/pvpbot remove <name>` | Remove a bot |
| `/pvpbot removeall` | Remove all bots |
| `/pvpbot list` | List all active bots |
| `/pvpbot inventory <name>` | Show bot's inventory |

### Examples

```mcfunction
# Create a bot named "Guard1"
/pvpbot spawn Guard1

# Remove the bot
/pvpbot remove Guard1

# See all bots
/pvpbot list
```

---

## ⚔️ Combat Commands

| Command | Description |
|---------|-------------|
| `/pvpbot attack <bot> <target>` | Order bot to attack a player/entity |
| `/pvpbot stop <bot>` | Stop bot from attacking |
| `/pvpbot target <bot>` | Show bot's current target |

### Examples

```mcfunction
# Make Bot1 attack player Steve
/pvpbot attack Bot1 Steve

# Stop the attack
/pvpbot stop Bot1

# Check who Bot1 is targeting
/pvpbot target Bot1
```

---

## 👥 Faction Commands

| Command | Description |
|---------|-------------|
| `/pvpbot faction create <name>` | Create a new faction |
| `/pvpbot faction delete <name>` | Delete a faction |
| `/pvpbot faction add <faction> <player>` | Add player/bot to faction |
| `/pvpbot faction remove <faction> <player>` | Remove from faction |
| `/pvpbot faction hostile <f1> <f2> [true/false]` | Set factions as hostile |
| `/pvpbot faction addnear <faction> <radius>` | Add all nearby bots |
| `/pvpbot faction give <faction> <item>` | Give item to all members |
| `/pvpbot faction givekit <faction> <kit>` | Give kit to all members |
| `/pvpbot faction list` | List all factions |
| `/pvpbot faction info <faction>` | Show faction details |

### Examples

```mcfunction
# Create two factions
/pvpbot faction create RedTeam
/pvpbot faction create BlueTeam

# Add bots to factions
/pvpbot faction add RedTeam Bot1
/pvpbot faction add BlueTeam Bot2

# Make them enemies
/pvpbot faction hostile RedTeam BlueTeam

# Give swords to everyone in RedTeam
/pvpbot faction give RedTeam diamond_sword
```

---

## 🎒 Kit Commands

| Command | Description |
|---------|-------------|
| `/pvpbot createkit <name>` | Create kit from your inventory |
| `/pvpbot deletekit <name>` | Delete a kit |
| `/pvpbot kits` | List all kits |
| `/pvpbot givekit <bot> <kit>` | Give kit to a bot |
| `/pvpbot faction givekit <faction> <kit>` | Give kit to faction |

### Examples

```mcfunction
# Put items in your inventory, then:
/pvpbot createkit warrior

# Give kit to a bot
/pvpbot givekit Bot1 warrior

# Give kit to entire faction
/pvpbot faction givekit RedTeam warrior
```

---

## ⚙️ Settings

Use `/pvpbot settings` to see all current settings.

Use `/pvpbot settings <setting>` to see current value.

Use `/pvpbot settings <setting> <value>` to change a setting.

See [Settings](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Settings) page for full list of all settings.

### Quick Examples

```mcfunction
# Enable auto-targeting
/pvpbot settings autotarget true

# Set miss chance to 20%
/pvpbot settings misschance 20

# Enable bunny hop
/pvpbot settings bhop true
```
