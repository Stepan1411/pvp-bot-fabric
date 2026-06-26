# Kits

Save and load equipment presets for bots. Kits are saved globally in `config/pvpbot/kits.json`.

## Creating Kits

1. Equip your character with the desired items
2. Run the create command:

```
/pvpbot kit create-kit warrior
/pvpbot kit create-kit archer
```

Kits save all 41 inventory slots (hotbar, main inventory, armor, offhand) as NBT data.

## Applying Kits

### To a single bot or player
```
/pvpbot kit give-kit Bot1 warrior
```

### To bots within radius
Give a kit to all bots within a radius (default: 10):
```
/pvpbot kit give-kit-near warrior 15
```

### Random weighted kit to bots within radius
Give a random kit based on weights to bots within a radius:
```
/pvpbot kit give-kit-near-random 15 warrior 60% archer 30% mage 10%
```

Each bot within 15 blocks gets one kit based on the weight distribution.

### To an entire faction
```
/pvpbot faction kit give-kit Red warrior
```

### Random weighted kit to faction
```
/pvpbot faction kit give-kit-random Red warrior 60% archer 30% mage 10%
```

## Managing Kits
```
/pvpbot kit kits                  # List all kits
/pvpbot kit delete-kit warrior    # Delete a kit
```

## Notes

- Kits completely clear the target's inventory before applying
- `give-kit-near` and `give-kit-near-random` require a player executor
- `give-kit-random` and `give-kit-near-random` work from console or player
- Kit names are case-insensitive
- Kits are global (shared across all worlds)
- Supported items include any Minecraft item with full NBT data (enchantments, damage, etc.)
