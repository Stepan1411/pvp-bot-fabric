# Combat System

The combat AI automatically manages target selection, weapon switching, and tactical decisions.

## Weapon Modes

Bots automatically select the best weapon mode based on distance and available items:

| Mode | Weapon | Range | Conditions |
|------|--------|-------|------------|
| MELEE | Sword / Axe | `melee-range` (default 3.5) | Default mode |
| RANGED | Bow / Crossbow | `ranged-min-range` - `ranged-optimal-range` | Requires arrows |
| MACE | Mace | `mace-range` (default 6.0) | `mace` setting enabled |
| SPEAR | Spear | `spear-range` / `spear-charge-range` | `spear` setting enabled |
| CRYSTAL | Crystal PVP | 2.5 - 8.0 | See [Explosive Combat](ExplosiveCombat) |
| ANCHOR | Anchor PVP | 2.0 - 8.0 | See [Explosive Combat](ExplosiveCombat) |

## Target Selection

Target priority (evaluated each tick):

1. **Forced Target** - Set via `/pvpbot bot-management attack`
2. **Revenge** - Auto-attacks last damager (30s timeout)
3. **Faction Enemies** - Hostile faction members (if factions enabled)
4. **Auto Target** - Nearest valid enemy in range

Valid targets can include players, hostile mobs, and other bots (configurable via settings).

## Combat Features

### Critical Hits
Bots jump before attacking to land critical hits when `criticals` is enabled.

### Shield Management
- Predicts enemy attacks based on sprint direction and distance
- Raises shield preemptively using prediction system
- Low health triggers shield holding
- Shield flickering for unpredictability

### Shield Breaking
When an enemy blocks, bots axe-shield-break them with configurable chance.

### Enemy Attack Prediction
Tracks enemy position changes, sprint state, and distance to predict attack timing for shield raising.

### Retreat Logic
When health drops below `retreat-health-percent`:
- Raises shield and moves away
- Uses healing potions if available
- Places cobwebs to slow pursuit
- Eats food to regenerate

### Mace Defense
Detects enemies using mace (in air, falling) and preemptively raises shield — compensates for the vanilla shield bug.

### Cobweb Placement
Bots can place cobwebs on targets to immobilize them (uses items from inventory).

## Settings Reference

| Setting | Default | Description |
|---------|---------|-------------|
| combat | true | Enable/disable combat entirely |
| revenge | true | Auto-engage last attacker |
| auto-target | false | Auto-acquire nearest target |
| target-players | true | Target real players |
| target-mobs | false | Target hostile mobs |
| target-bots | false | Target other bots |
| criticals | true | Use jumping critical hits |
| attack-cooldown | 10 | Ticks between attacks |
| melee-range | 3.5 | Melee attack range |
| miss-chance | 0% | Chance to miss attacks |
| mistake-chance | 0% | Chance to make aiming mistakes |
| attack-invincible | false | Attack creative/spectator players |
| aim-speed | 60 | Rotation speed (degrees/sec) |
| view-distance | 64 | Max target acquisition range |
