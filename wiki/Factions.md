# 👥 Faction System

Organize bots and players into teams that can fight each other!

---

## 📖 Overview

Factions are groups of bots and players. You can:
- Create teams of bots
- Set factions as hostile to each other
- Bots automatically attack enemies from hostile factions

---

## 🏗️ Creating Factions

```mcfunction
# Create a faction
/pvpbot faction create RedTeam

# Delete a faction
/pvpbot faction delete RedTeam

# List all factions
/pvpbot faction list

# Get faction info
/pvpbot faction info RedTeam
```

---

## 👤 Managing Members

### Add Members
```mcfunction
# Add a bot to faction
/pvpbot faction add RedTeam Bot1

# Add a player to faction
/pvpbot faction add RedTeam Steve

# Add all nearby bots (within 20 blocks)
/pvpbot faction addnear RedTeam 20
```

### Remove Members
```mcfunction
/pvpbot faction remove RedTeam Bot1
```

---

## ⚔️ Hostile Relations

Make factions enemies - their members will automatically attack each other!

```mcfunction
# Make factions hostile
/pvpbot faction hostile RedTeam BlueTeam

# Make factions neutral again
/pvpbot faction hostile RedTeam BlueTeam false
```

### How It Works
1. Bot from RedTeam sees player/bot from BlueTeam
2. If factions are hostile, bot automatically targets them
3. Combat begins!

> **Note:** Requires `autotarget` to be enabled for automatic targeting.

---

## 🎁 Giving Items

### Give Items
```mcfunction
# Give diamond sword to all faction members
/pvpbot faction give RedTeam diamond_sword

# Give multiple items
/pvpbot faction give RedTeam diamond_sword 1
/pvpbot faction give RedTeam golden_apple 16
```

### Give Kits
```mcfunction
# Give a saved kit to entire faction
/pvpbot faction givekit RedTeam warrior
```

---

## 📋 Complete Example

Create two teams and make them fight:

```mcfunction
# Create bots
/pvpbot spawn Red1
/pvpbot spawn Red2
/pvpbot spawn Red3
/pvpbot spawn Blue1
/pvpbot spawn Blue2
/pvpbot spawn Blue3

# Create factions
/pvpbot faction create Red
/pvpbot faction create Blue

# Add bots to factions
/pvpbot faction add Red Red1
/pvpbot faction add Red Red2
/pvpbot faction add Red Red3
/pvpbot faction add Blue Blue1
/pvpbot faction add Blue Blue2
/pvpbot faction add Blue Blue3

# Make them enemies
/pvpbot faction hostile Red Blue

# Give equipment
/pvpbot faction give Red diamond_sword
/pvpbot faction give Blue diamond_sword
/pvpbot faction give Red diamond_chestplate
/pvpbot faction give Blue diamond_chestplate

# Enable auto-targeting
/pvpbot settings autotarget true

# Watch the battle!
```

---

## ⚙️ Settings

```mcfunction
# Enable/disable faction system
/pvpbot settings factions true
```

---

## 💾 Data Storage

Faction data is saved in:
```
config/pvp_bot_factions.json
```

This file persists across server restarts.
