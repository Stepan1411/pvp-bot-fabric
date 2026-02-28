# 🛤️ Path System

The Path System allows you to create predefined routes for bots to follow. Bots can patrol areas, move between locations, and optionally engage in combat while following paths.

---

## 📋 Table of Contents

- [Overview](#overview)
- [Creating Paths](#creating-paths)
- [Managing Waypoints](#managing-waypoints)
- [Bot Control](#bot-control)
- [Path Settings](#path-settings)
- [Visualization](#visualization)
- [Examples](#examples)

---

## 🎯 Overview

Paths are sequences of waypoints that bots can follow. Each path has:
- **Name** - Unique identifier
- **Waypoints** - List of positions (x, y, z)
- **Loop Mode** - How the bot moves through waypoints
- **Attack Mode** - Whether bot stops for combat
- **Visualization** - Particle effects showing the path

Paths are saved per-world in `config/pvpbot/worlds/{world}/paths.json`

---

## 🆕 Creating Paths

### Create a new path
```
/pvpbot path create <name>
```
Creates an empty path with the given name.

**Example:**
```
/pvpbot path create patrol_route
```

### Delete a path
```
/pvpbot path delete <name>
```
Removes the path and stops all bots following it.

**Example:**
```
/pvpbot path delete patrol_route
```

### List all paths
```
/pvpbot path list
```
Shows all available paths in the current world.

### View path details
```
/pvpbot path info <name>
```
Displays path information:
- Number of waypoints
- Loop mode status
- Attack mode status
- List of all waypoint coordinates

**Example:**
```
/pvpbot path info patrol_route
```

---

## 📍 Managing Waypoints

### Add waypoint
```
/pvpbot path add <name>
```
Adds your current position as a new waypoint to the path.

**Example:**
```
/pvpbot path add patrol_route
```
Stand at each location you want the bot to visit and run this command.

### Remove waypoint
```
/pvpbot path remove <name> <index>
```
Removes a specific waypoint by its index (starting from 0).

**Example:**
```
/pvpbot path remove patrol_route 2
```
Removes the 3rd waypoint from the path.

### Clear all waypoints
```
/pvpbot path clear <name>
```
Removes all waypoints from the path (keeps the path itself).

**Example:**
```
/pvpbot path clear patrol_route
```

---

## 🤖 Bot Control

### Start following path
```
/pvpbot path follow <bot> <path>
```
Makes a bot start following the specified path.

**Example:**
```
/pvpbot path follow Guard1 patrol_route
```

### Stop following path
```
/pvpbot path stop <bot>
```
Stops the bot from following its current path.

**Example:**
```
/pvpbot path stop Guard1
```

---

## ⚙️ Path Settings

### Loop Mode
```
/pvpbot path loop <name> <true/false>
```

Controls how the bot moves through waypoints:
- **false** (default) - Circular: 1→2→3→1→2→3...
- **true** - Back-and-forth: 1→2→3→2→1→2→3...

**Example:**
```
/pvpbot path loop patrol_route true
```

### Attack Mode
```
/pvpbot path attack <name> <true/false>
```

Controls combat behavior while following path:
- **true** (default) - Bot stops at current waypoint to fight, then continues
- **false** - Bot ignores combat and keeps moving (BotCombat disabled)

**Example:**
```
/pvpbot path attack patrol_route false
```

---

## 👁️ Visualization

### Toggle path display
```
/pvpbot path show <name> <true/false>
```

Shows/hides particle effects for the path:
- **Waypoints** - Wax particles at each point
- **Lines** - Green dust particles connecting points

Visualization automatically enables when:
- Creating a path
- Adding a waypoint
- Starting to follow a path

**Example:**
```
/pvpbot path show patrol_route true
```

To disable visualization:
```
/pvpbot path show patrol_route false
```

---

## 💡 Examples

### Basic patrol route
```
# Create path
/pvpbot path create base_patrol

# Add waypoints (stand at each location)
/pvpbot path add base_patrol  # Point 1
/pvpbot path add base_patrol  # Point 2
/pvpbot path add base_patrol  # Point 3
/pvpbot path add base_patrol  # Point 4

# Make bot follow
/pvpbot path follow Guard1 base_patrol
```

### Guard with combat
```
# Create path
/pvpbot path create guard_post

# Add waypoints
/pvpbot path add guard_post  # Position 1
/pvpbot path add guard_post  # Position 2

# Enable back-and-forth movement
/pvpbot path loop guard_post true

# Enable combat (default, but explicit)
/pvpbot path attack guard_post true

# Assign bot
/pvpbot path follow Guard1 guard_post
```

### Peaceful courier
```
# Create path
/pvpbot path create delivery_route

# Add waypoints
/pvpbot path add delivery_route  # Start
/pvpbot path add delivery_route  # Checkpoint 1
/pvpbot path add delivery_route  # Checkpoint 2
/pvpbot path add delivery_route  # End

# Disable combat (bot won't fight)
/pvpbot path attack delivery_route false

# Assign bot
/pvpbot path follow Courier1 delivery_route
```

### Multiple bots on same path
```
# Create path
/pvpbot path create wall_patrol

# Add waypoints
/pvpbot path add wall_patrol  # Corner 1
/pvpbot path add wall_patrol  # Corner 2
/pvpbot path add wall_patrol  # Corner 3
/pvpbot path add wall_patrol  # Corner 4

# Assign multiple bots
/pvpbot path follow Guard1 wall_patrol
/pvpbot path follow Guard2 wall_patrol
/pvpbot path follow Guard3 wall_patrol
```

---

## 📝 Notes

- Paths are saved automatically when modified
- Each world has its own set of paths
- Bots look at the next waypoint while moving
- When attack mode is true, bots return to the waypoint they were heading to after combat
- Path visualization is visible to all players
- Bots reach a waypoint when within 1.5 blocks of it

---

## 🔗 Related Pages

- [Commands](Commands.md) - All available commands
- [Navigation](Navigation.md) - Bot movement settings
- [Combat](Combat.md) - Combat system details
