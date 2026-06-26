# Commands

All commands use the `/pvpbot` prefix.

## Bot Management

| Command | Description |
|---------|-------------|
| `/pvpbot spawn [name]` | Spawn a bot (random name if omitted) |
| `/pvpbot remove <name>` | Remove a specific bot |
| `/pvpbot removeall` | Remove all bots |
| `/pvpbot reload` | Reload all configurations (settings, kits, paths, bots) |
| `/pvpbot bot-management list` | List all active bots |
| `/pvpbot bot-management inventory <botname>` | View bot inventory and stats |
| `/pvpbot bot-management mass-spawn <1-50>` | Spawn multiple bots |

## Combat Control

| Command | Description |
|---------|-------------|
| `/pvpbot bot-management attack <botname> <target>` | Force bot to attack a target |
| `/pvpbot bot-management stop-attack <botname>` | Stop attacking |

## Settings

| Command | Description |
|---------|-------------|
| `/pvpbot settings` | List all current settings |
| `/pvpbot settings <name>` | View a specific setting |
| `/pvpbot settings <name> <value>` | Set a setting value |

See [Settings](Settings) for all available options.

## Paths

| Command | Description |
|---------|-------------|
| `/pvpbot bot-management path create <name>` | Create a new path |
| `/pvpbot bot-management path delete <name>` | Delete a path |
| `/pvpbot bot-management path add-point <name>` | Add current position as waypoint |
| `/pvpbot bot-management path remove-point <name> [index]` | Remove point (last or by index) |
| `/pvpbot bot-management path clear <name>` | Clear all points |
| `/pvpbot bot-management path loop <name> <true/false>` | Toggle looping |
| `/pvpbot bot-management path start <bot> <path>` | Start bot following path |
| `/pvpbot bot-management path stop <bot>` | Stop bot following path |
| `/pvpbot bot-management path list` | List all paths |
| `/pvpbot bot-management path show <name> <true/false>` | Toggle path visualization |
| `/pvpbot bot-management path info <name>` | View path details |
| `/pvpbot bot-management path distribute <path>` | Spread bots evenly along path |
| `/pvpbot bot-management path start-near <path> <radius>` | Start path for nearby bots |
| `/pvpbot bot-management path stop-all <path>` | Stop all bots on path |
| `/pvpbot bot-management path walk-type <name> <type>` | Set walk type (bhop/sprint/walk) |

See [Paths](Paths) for detailed usage.

## Kits

| Command | Description |
|---------|-------------|
| `/pvpbot kit create-kit <name>` | Save your inventory as a kit |
| `/pvpbot kit delete-kit <name>` | Delete a kit |
| `/pvpbot kit give-kit <player> <kitname>` | Give kit to player/bot |
| `/pvpbot kit kits` | List all kits |
| `/pvpbot kit give-kit-near <kitname> [radius]` | Give kit to bots within radius (default: 10) |
| `/pvpbot kit give-kit-near-random <radius> <kit1> <w1>% [<kit2> <w2>% ...]` | Give random weighted kit to bots within radius |

See [Kits](Kits) for detailed usage.

## Factions

| Command | Description |
|---------|-------------|
| `/pvpbot faction list` | List all factions |
| `/pvpbot faction create <name>` | Create a faction |
| `/pvpbot faction delete <name>` | Delete a faction |
| `/pvpbot faction add <faction> <player>` | Add player/bot to faction |
| `/pvpbot faction remove <faction> <player>` | Remove player/bot from faction |
| `/pvpbot faction hostile <f1> <f2> [true/false]` | Set hostile relations |
| `/pvpbot faction info <name>` | View faction info |
| `/pvpbot faction add-near <faction> <radius>` | Add nearby bots to faction |
| `/pvpbot faction add-all <faction>` | Add all bots to faction |
| `/pvpbot faction give <faction> <item>` | Give items to all members |
| `/pvpbot faction attack <faction> <target>` | All members attack target |
| `/pvpbot faction path start <faction> <path>` | All members follow path |
| `/pvpbot faction path stop <faction>` | Stop all members on path |
| `/pvpbot faction tp <faction> <x y z\|player>` | Teleport entire faction gradually |

### Faction Kit Commands

| Command | Description |
|---------|-------------|
| `/pvpbot faction kit give-kit <faction> <kitname>` | Give kit to all members |
| `/pvpbot faction kit give-kit-random <faction> <kit1> <w1>% [<kit2> <w2>% ...]` | Give random weighted kit to faction members |

See [Factions](Factions) for detailed usage.
