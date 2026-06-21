# Path System

Create waypoint paths for bots to follow. Paths are saved per-world in `config/pvpbot/worlds/<worldname>/paths.json`.

## Commands

### Creating Paths
```
/pvpbot bot-management path create MyPath
```

### Adding Waypoints
Stand at the desired location and run:
```
/pvpbot bot-management path add-point MyPath
```
Path visualization (particles) is automatically enabled.

### Managing Waypoints
```
/pvpbot bot-management path remove-point MyPath      # Remove last
/pvpbot bot-management path remove-point MyPath 0    # Remove by index
/pvpbot bot-management path clear MyPath             # Clear all points
/pvpbot bot-management path info MyPath              # List all points
/pvpbot bot-management path list                     # List all paths
```

### Following Paths
```
/pvpbot bot-management path start Bot1 MyPath
/pvpbot bot-management path stop Bot1
/pvpbot bot-management path start-near MyPath 20     # Start for bots within 20 blocks
/pvpbot bot-management path stop-all MyPath          # Stop all bots on path
```

### Distribution
Evenly space bots along path points:
```
/pvpbot bot-management path distribute MyPath
```

### Walk Types
```
/pvpbot bot-management path walk-type MyPath bhop    # Bunny hop (default)
/pvpbot bot-management path walk-type MyPath sprint  # Sprint
/pvpbot bot-management path walk-type MyPath walk    # Walk
```

### Loop Mode
```
/pvpbot bot-management path loop MyPath true
/pvpbot bot-management path loop MyPath false
```

In loop mode, bots reverse direction at the end (ping-pong). In non-loop mode, bots restart from the beginning.

### Visualization
```
/pvpbot bot-management path show MyPath true
/pvpbot bot-management path show MyPath false
```

Shows path waypoints as particles (WAX_ON + green dust lines).

## Faction Paths

Control all bots in a faction at once:
```
/pvpbot faction path start RedFaction MyPath
/pvpbot faction path stop RedFaction
```

## Properties

| Property | Options | Default |
|----------|---------|---------|
| Walk Type | bhop / sprint / walk | bhop |
| Loop | true / false | false |
| Attack | true / false | true |
| Points | variable (Vec3d) | - |
