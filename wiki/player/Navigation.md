# Navigation

The navigation system handles bot movement with obstacle detection and smooth rotation.

## Movement Types

### B-Hop (default)
- Auto-jumps while moving for bunny hopping
- Configurable via `bhop` setting
- Used automatically for movement speed >= 1.0

### Sprint
- Standard sprint movement
- Used when bhop is disabled

### Walk
- No sprinting, slower movement
- Set via path walk-type

## Movement Functions

| Function | Description |
|----------|-------------|
| `moveToward(bot, target, speed)` | Move toward an entity |
| `moveAway(bot, target, speed)` | Move away from an entity |
| `moveTowardPosition(bot, pos, speed)` | Move toward a position |
| `moveTowardCombat(bot, pos, speed, strafe)` | Combat strafing movement |

## Smooth Looking

Rotation uses configurable `aim-speed` (degrees per tick) for smooth tracking:
- `lookAt(bot, entity)` — Track entity
- `lookAtPosition(bot, pos)` — Track position
- `lookAway(bot, entity)` — Look away (retreating)

## Obstacle Detection

The bot detects and handles:

| Obstacle | Behavior |
|----------|----------|
| **Solid blocks** | Jumps to clear |
| **Walls** | Side-steps around (avoid direction alternates) |
| **Holes** | Jumps over |
| **Ladders / Vines** | Climbs automatically (sprinting disabled) |
| **Water** | Swims with directional control |

## Stuck Detection

If the bot moves less than 0.05 blocks while on ground for 10+ ticks:
1. Alternates avoidance direction
2. Jumps to escape
3. Tries to side-step around obstacles

## Knockback Handling

When horizontal velocity exceeds 0.35 (knockback), the bot stops sprinting and resists movement for up to 10 ticks.

## Idle Wander

When idle (no target) and `idle` setting is enabled, bots wander randomly within `idle-radius` (default 10 blocks) from spawn position.

## W-Tap

After attacking, the bot briefly releases sprint (W-tap) to reset opponent's knockback distance.

## Combat Strafe

In combat mode, bots strafe left/right with randomized direction, switching every 8-18 ticks.

## Settings

| Setting | Default | Description |
|---------|---------|-------------|
| bhop | true | Enable bunny hopping |
| idle | false | Enable idle wandering |
| idle-radius | 10 | Wander radius from spawn |
| move-speed | 1.0 | Movement speed multiplier |
| aim-speed | 60 | Rotation speed (degrees/sec) |
