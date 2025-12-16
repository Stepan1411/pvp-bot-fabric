# ⚔️ Combat System

PVP Bot features an advanced combat AI that can use different weapons and tactics.

---

## 🗡️ Weapon Types

### Melee Combat
- **Swords** - Fast attacks, good damage
- **Axes** - Slower but can break shields
- Bots automatically switch to melee when enemies are close

### Ranged Combat
- **Bows** - Draw and release arrows
- **Crossbows** - Load and fire bolts
- Bots keep optimal distance (8-20 blocks)

### Mace Combat
- **Mace + Wind Charge** - Jump attacks for massive damage
- Bots use wind charges to launch into the air
- Devastating falling attacks

---

## 🎯 Targeting

### Revenge Mode
When a bot takes damage, it automatically targets the attacker.
```mcfunction
/pvpbot settings revenge true
```

### Auto-Target
Bots automatically search for enemies within view distance.
```mcfunction
/pvpbot settings autotarget true
```

### Manual Target
Force a bot to attack a specific target.
```mcfunction
/pvpbot attack BotName TargetName
```

### Target Filters
Choose what bots can target:
```mcfunction
/pvpbot settings targetplayers true   # Target players
/pvpbot settings targetmobs true      # Target hostile mobs
/pvpbot settings targetbots true      # Target other bots
```

---

## 🛡️ Defense

### Auto-Shield
Bots automatically raise shields when enemies attack.
```mcfunction
/pvpbot settings autoshield true
```

### Shield Breaking
Bots use axes to disable enemy shields.
```mcfunction
/pvpbot settings shieldbreak true
```

### Auto-Totem
Bots keep totems of undying in offhand.
```mcfunction
/pvpbot settings autototem true
```

---

## 🍎 Healing

### Auto-Eat
Bots eat food when:
- Health is low (< 30%)
- Hunger is below threshold

```mcfunction
/pvpbot settings autoeat true
/pvpbot settings minhunger 14
```

### Retreat
When health is low, bots retreat while eating golden apples.

---

## 💥 Critical Hits

Bots can perform critical hits by timing their attacks with jumps.
```mcfunction
/pvpbot settings criticals true
```

---

## ⚙️ Combat Settings

| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| `combat` | true/false | true | Enable combat |
| `revenge` | true/false | true | Attack who attacked you |
| `autotarget` | true/false | false | Auto-find enemies |
| `criticals` | true/false | true | Critical hits |
| `ranged` | true/false | true | Use bows |
| `mace` | true/false | true | Use mace |
| `attackcooldown` | 1-40 | 10 | Ticks between attacks |
| `meleerange` | 2-6 | 3.5 | Melee attack distance |
| `viewdistance` | 5-128 | 64 | Target search range |
