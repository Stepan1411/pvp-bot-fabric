# ⚙️ Settings

Complete list of all configuration options.

---

## 📋 Commands

```mcfunction
# Show all settings
/pvpbot settings

# Show specific setting
/pvpbot settings <name>

# Change setting
/pvpbot settings <name> <value>
```

---

## ⚔️ Combat Settings

| Setting | Type | Range | Default | Description |
|---------|------|-------|---------|-------------|
| `combat` | bool | - | true | Enable/disable combat system |
| `revenge` | bool | - | true | Attack entities that damage the bot |
| `autotarget` | bool | - | false | Automatically search for enemies |
| `targetplayers` | bool | - | true | Can target players |
| `targetmobs` | bool | - | false | Can target hostile mobs |
| `targetbots` | bool | - | false | Can target other bots |
| `criticals` | bool | - | true | Perform critical hits |
| `ranged` | bool | - | true | Use bows/crossbows |
| `mace` | bool | - | true | Use mace with wind charges |
| `attackcooldown` | int | 1-40 | 10 | Ticks between attacks |
| `meleerange` | double | 2-6 | 3.5 | Melee attack distance |
| `movespeed` | double | 0.1-2.0 | 1.0 | Movement speed multiplier |
| `viewdistance` | double | 5-128 | 64 | Maximum target detection range |

---

## 🚶 Navigation Settings

| Setting | Type | Range | Default | Description |
|---------|------|-------|---------|-------------|
| `bhop` | bool | - | true | Enable bunny hop |
| `bhopcooldown` | int | 5-30 | 12 | Ticks between bhop jumps |
| `jumpboost` | double | 0.0-0.5 | 0.0 | Extra jump height |
| `idle` | bool | - | true | Wander when no target |
| `idleradius` | double | 3-50 | 10 | Idle wander radius |

---

## 🛡️ Equipment Settings

| Setting | Type | Range | Default | Description |
|---------|------|-------|---------|-------------|
| `autoarmor` | bool | - | true | Auto-equip best armor |
| `autoweapon` | bool | - | true | Auto-equip best weapon |
| `droparmor` | bool | - | false | Drop worse armor pieces |
| `dropweapon` | bool | - | false | Drop worse weapons |
| `dropdistance` | double | 1-10 | 3.0 | Item pickup distance |
| `interval` | int | 1-100 | 20 | Equipment check interval (ticks) |
| `minarmorlevel` | int | 0-100 | 0 | Minimum armor level to equip |

### Armor Levels
| Level | Armor Type |
|-------|------------|
| 0 | Any armor |
| 20 | Leather+ |
| 40 | Gold+ |
| 50 | Chain+ |
| 60 | Iron+ |
| 80 | Diamond+ |
| 100 | Netherite only |

---

## 🎭 Realism Settings

| Setting | Type | Range | Default | Description |
|---------|------|-------|---------|-------------|
| `misschance` | int | 0-100 | 10 | Chance to miss attacks (%) |
| `mistakechance` | int | 0-100 | 5 | Chance to attack wrong direction (%) |
| `reactiondelay` | int | 0-20 | 0 | Delay before reacting (ticks) |

---

## 👥 Other Settings

| Setting | Type | Range | Default | Description |
|---------|------|-------|---------|-------------|
| `factions` | bool | - | true | Enable faction system |

---

## 💾 Configuration File

All settings are saved in:
```
config/pvp_bot.json
```

Settings persist across server restarts.

---

## 📋 Examples

### Make bots more realistic
```mcfunction
/pvpbot settings misschance 15
/pvpbot settings mistakechance 10
/pvpbot settings reactiondelay 5
```

### Make bots aggressive
```mcfunction
/pvpbot settings autotarget true
/pvpbot settings targetplayers true
/pvpbot settings targetbots true
/pvpbot settings revenge true
```

### Disable ranged combat
```mcfunction
/pvpbot settings ranged false
/pvpbot settings mace false
```

### Fast movement
```mcfunction
/pvpbot settings bhop true
/pvpbot settings bhopcooldown 8
/pvpbot settings jumpboost 0.2
/pvpbot settings movespeed 1.5
```

### Stationary guards
```mcfunction
/pvpbot settings idle false
/pvpbot settings bhop false
```
