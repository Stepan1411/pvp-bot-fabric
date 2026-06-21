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

### To an entire faction
```
/pvpbot faction give-kit Red warrior
```

## Managing Kits
```
/pvpbot kit kits                  # List all kits
/pvpbot kit delete-kit warrior    # Delete a kit
```

## Notes

- Kits completely clear the target's inventory before applying
- Kit names are case-insensitive
- Kits are global (shared across all worlds)
- Supported items include any Minecraft item with full NBT data (enchantments, damage, etc.)
