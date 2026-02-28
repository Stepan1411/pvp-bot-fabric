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

### Auto-Potions
Bots automatically use potions:
- **Healing potions** - when HP is low (splash or drinkable)
- **Strength potions** - when entering combat
- **Speed potions** - when entering combat
- **Fire resistance potions** - when entering combat

All buff potions are thrown at once when combat starts. Bots re-apply buffs when effects expire (< 5 seconds remaining).

```mcfunction
/pvpbot settings autopotion true
```

### Retreat
When health is low, bots retreat while eating/healing.
Retreat is disabled if bot has no food (fights to the death).

```mcfunction
/pvpbot settings retreat true
/pvpbot settings retreathp 0.3  # 30% HP
```

---

## 💥 Critical Hits

Bots can perform critical hits by timing their attacks with jumps.
```mcfunction
/pvpbot settings criticals true
```

---

## 🕸️ Cobweb Tactics

Bots can use cobwebs strategically:
- **When retreating** - places cobweb under chasing enemy to slow them down
- **In melee combat** - places cobweb under charging enemy

```mcfunction
/pvpbot settings cobweb true
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
| `spear` | true/false | false | Use spear (buggy) |
| `autopotion` | true/false | true | Auto-use potions |
| `cobweb` | true/false | true | Use cobwebs |
| `retreat` | true/false | true | Retreat when low HP |
| `retreathp` | 0.1-0.9 | 0.3 | HP % to retreat |
| `attackcooldown` | 1-40 | 10 | Ticks between attacks |
| `meleerange` | 2-6 | 3.5 | Melee attack distance |
| `viewdistance` | 5-128 | 64 | Target search range |
