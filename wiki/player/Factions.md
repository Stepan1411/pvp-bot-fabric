# Factions

Organize bots into teams/factions for coordinated battles. Factions are saved per-world in `config/pvpbot/worlds/<worldname>/factions.json`.

## Basic Commands

### Create and Delete
```
/pvpbot faction create Red
/pvpbot faction delete Red
```

### Manage Members
```
/pvpbot faction add Red Bot1
/pvpbot faction remove Red Bot1
/pvpbot faction add-near Red 30          # Add all bots within 30 blocks
/pvpbot faction add-all Red              # Add all existing bots
```

### Check Info
```
/pvpbot faction list              # List all factions
/pvpbot faction info Red          # Show members and enemies
```

## Hostile Relations

Set factions as enemies. Bots will automatically attack enemy faction members.

```
/pvpbot faction hostile Red Blue
/pvpbot faction hostile Red Neutral false
```

**Note:** Enemies are always mutual — setting Red hostile to Blue also makes Blue hostile to Red.

## Coordinated Actions

### Attack
All faction members attack a target:
```
/pvpbot faction attack Red GreenPlayer
```

### Give Items
Give items to all faction members:
```
/pvpbot faction give Red diamond_sword 1
```

### Give Kit
Equip all members with a kit:
```
/pvpbot faction give-kit Red MyKit
```

### Path Following
All members follow a path:
```
/pvpbot faction path start Red MyPath
/pvpbot faction path stop Red
```

## Settings

| Setting | Default | Description |
|---------|---------|-------------|
| factions | true | Enable faction system |
| friendly-fire | false | Allow attacking own faction members |
