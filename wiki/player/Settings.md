# Settings

All settings are saved per-world in `config/pvpbot/worlds/<worldname>/settings.json`.

## Viewing and Changing

```
/pvpbot settings                # List all settings
/pvpbot settings combat         # View a single setting
/pvpbot settings combat false   # Change a setting
```

## Equipment Settings

| Setting | Type | Default | Range | Description |
|---------|------|---------|-------|-------------|
| auto-armor | bool | true | - | Equip best armor from inventory |
| auto-weapon | bool | true | - | Equip best weapon to hotbar |
| drop-armor | bool | false | - | Drop worse armor when replacing |
| drop-weapon | bool | false | - | Drop worse weapons when replacing |
| drop-distance | double | 3.0 | 1.0 - 10.0 | Distance to drop items |
| interval | int | 20 | 1 - 100 | Equipment check interval (ticks) |

## Combat Settings

| Setting | Type | Default | Range | Description |
|---------|------|---------|-------|-------------|
| combat | bool | true | - | Enable combat system |
| revenge | bool | true | - | Auto-attack last damager |
| auto-target | bool | false | - | Auto-acquire nearest target |
| target-players | bool | true | - | Target real players |
| target-mobs | bool | false | - | Target hostile mobs |
| target-bots | bool | false | - | Target other bots |
| view-distance | double | 64.0 | 5.0 - 128.0 | Max target search range |
| attack-invincible | bool | false | - | Attack creative/spectator players |
| attack-cooldown | int | 10 | 1 - 40 | Ticks between attacks |
| criticals | bool | true | - | Jump for critical hits |
| melee-range | double | 3.5 | 2.0 - 6.0 | Melee reach distance |
| move-speed | double | 1.0 | 0.1 - 2.0 | Movement speed multiplier |
| miss-chance | int | 0 | 0 - 100 | % chance to miss attacks |
| mistake-chance | int | 0 | 0 - 100 | % chance to aim incorrectly |
| aim-speed | double | 60.0 | 3.0 - 90.0 | Rotation speed (deg/sec) |

## Weapon Settings

| Setting | Type | Default | Range | Description |
|---------|------|---------|-------|-------------|
| ranged | bool | true | - | Enable bow/crossbow combat |
| mace | bool | true | - | Enable mace combat |
| spear | bool | false | - | Enable spear combat |
| crystalpvp | bool | true | - | Enable Crystal PVP |
| anchorpvp | bool | true | - | Enable Anchor PVP |
| prefer-sword | bool | true | - | Prefer swords over axes |
| shield-mace | bool | true | - | Auto-shield against mace attacks |
| special-names | bool | false | - | Use special names list |
| bow-min-draw | int | 15 | 5 - 30 | Min bow draw ticks |
| mace-range | double | 6.0 | 3.0 - 10.0 | Mace attack range |
| spear-range | double | 4.5 | 2.0 - 8.0 | Spear attack range |
| spear-charge-range | double | 12.0 | 5.0 - 20.0 | Spear charge start range |
| ranged-min-range | double | 8.0 | 3.0 - 20.0 | Min ranged engagement distance |
| ranged-optimal | double | 20.0 | 10.0 - 50.0 | Optimal ranged distance |

## Utility Settings

| Setting | Type | Default | Range | Description |
|---------|------|---------|-------|-------------|
| auto-totem | bool | true | - | Auto-equip totem of undying |
| totem-priority | bool | true | - | Keep totem in offhand over shield |
| auto-eat | bool | true | - | Auto-eat when hungry |
| auto-shield | bool | true | - | Auto-raise shield when needed |
| auto-potion | bool | true | - | Use healing pots at low health |
| auto-mend | bool | true | - | Use XP bottles to repair gear |
| shield-break | bool | true | - | Axe-shield-break blocking enemies |
| cobweb | bool | true | - | Place cobwebs on targets |
| min-hunger | int | 14 | 1 - 20 | Hunger level to start eating |
| mend-threshold | double | 0.25 | 0.1 - 0.9 | Durability % to trigger mending |
| shield-health | double | 0.5 | 0.1 - 1.0 | Health % to hold shield |

## Retreat Settings

| Setting | Type | Default | Range | Description |
|---------|------|---------|-------|-------------|
| retreat | bool | true | - | Enable retreat behavior |
| retreat-health | double | 0.3 | 0.1 - 0.9 | Health % to start retreating |
| critical-health | double | 0.15 | 0.05 - 0.5 | Health % for critical retreat |

## Shield Settings

| Setting | Type | Default | Range | Description |
|---------|------|---------|-------|-------------|
| shield-hold-ticks | int | 60 | 10 - 200 | Ticks to hold shield raised |
| shield-raise-ticks | int | 12 | 2 - 40 | Ticks to raise shield |
| shield-break-chance | int | 40 | 0 - 100 | % chance to break enemy shield per hit |

## Navigation Settings

| Setting | Type | Default | Range | Description |
|---------|------|---------|-------|-------------|
| bhop | bool | true | - | Enable bunny hopping |
| idle | bool | false | - | Idle wandering when no target |
| idle-radius | double | 10.0 | 3.0 - 50.0 | Idle wander radius |

## Faction Settings

| Setting | Type | Default | Range | Description |
|---------|------|---------|-------|-------------|
| factions | bool | true | - | Enable faction system |
| friendly-fire | bool | false | - | Allow attacking allies |

## Misc Settings

| Setting | Type | Default | Range | Description |
|---------|------|---------|-------|-------------|
| bot-leave-on-death | bool | true | - | Remove bot on death |
| attack-invincible | bool | false | - | Attack in creative/spectator |
| bots-relogs | bool | true | - | Restore bots on server restart |
