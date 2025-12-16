# 🎒 Kit System

Save equipment presets and quickly equip bots!

---

## 📖 Overview

Kits allow you to:
- Save your current inventory as a template
- Quickly give equipment to bots
- Equip entire factions at once

---

## 📦 Creating Kits

1. Put items in your inventory (armor, weapons, food, etc.)
2. Run the create command

```mcfunction
/pvpbot createkit <name>
```

### What Gets Saved
- ✅ Hotbar items (slots 0-8)
- ✅ Inventory items
- ✅ Armor pieces
- ✅ Offhand item
- ✅ Item enchantments
- ✅ Item durability
- ✅ Stack sizes

### Example
```mcfunction
# Put diamond armor, sword, bow, arrows, golden apples in your inventory
# Then save it:
/pvpbot createkit pvp_warrior
```

---

## 📋 Managing Kits

### List Kits
```mcfunction
/pvpbot kits
```

### Delete Kit
```mcfunction
/pvpbot deletekit pvp_warrior
```

---

## 🎁 Giving Kits

### To Single Bot
```mcfunction
/pvpbot givekit Bot1 pvp_warrior
```

### To Entire Faction
```mcfunction
/pvpbot faction givekit RedTeam pvp_warrior
```

---

## 💡 Kit Ideas

### ⚔️ Melee Fighter
- Diamond/Netherite sword
- Full diamond armor
- Shield
- Golden apples
- Totem of undying (offhand)

### 🏹 Archer
- Bow (Power V, Infinity)
- Arrow (1 stack)
- Leather/Chain armor
- Golden apples

### 🔨 Tank
- Netherite armor (Protection IV)
- Shield
- Axe (for shield breaking)
- Lots of golden apples
- Multiple totems

### 💨 Speed Fighter
- Light armor (leather/chain)
- Diamond sword (Sharpness V)
- Speed potions
- Golden apples

---

## 📋 Complete Example

```mcfunction
# Step 1: Prepare your inventory with items you want

# Step 2: Create the kit
/pvpbot createkit soldier

# Step 3: Spawn bots
/pvpbot spawn Soldier1
/pvpbot spawn Soldier2
/pvpbot spawn Soldier3

# Step 4: Give kit to all bots
/pvpbot givekit Soldier1 soldier
/pvpbot givekit Soldier2 soldier
/pvpbot givekit Soldier3 soldier

# Or create a faction and give kit to all at once:
/pvpbot faction create Army
/pvpbot faction add Army Soldier1
/pvpbot faction add Army Soldier2
/pvpbot faction add Army Soldier3
/pvpbot faction givekit Army soldier
```

---

## 💾 Data Storage

Kit data is saved in:
```
config/pvp_bot_kits.json
```

Kits persist across server restarts.

---

## ⚠️ Notes

- Bots will auto-equip armor from the kit
- Bots will auto-select best weapon
- Existing items in bot inventory are NOT cleared
- If bot inventory is full, some items may not be given
