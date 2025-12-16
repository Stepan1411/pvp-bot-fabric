# 🚶 Navigation System

PVP Bot features smart pathfinding and movement mechanics.

---

## 🧭 Pathfinding

### Obstacle Avoidance
Bots automatically detect and avoid:
- **Walls** - Turn left/right to go around
- **Holes** - Jump over gaps
- **1-block obstacles** - Jump up

### Climbing
Bots can climb:
- Ladders
- Vines
- Scaffolding
- Twisting/Weeping vines

---

## 🐰 Bunny Hop (Bhop)

Bots can bunny hop for faster movement - jumping while sprinting for speed boost.

```mcfunction
# Enable/disable bhop
/pvpbot settings bhop true

# Set cooldown between jumps (5-30 ticks)
/pvpbot settings bhopcooldown 12

# Add extra jump height (0.0-0.5)
/pvpbot settings jumpboost 0.1
```

### When Bhop Activates
- Speed is >= 1.0
- No obstacles ahead
- Not climbing
- On ground

---

## 😴 Idle Wandering

When bots have no target, they wander around their spawn point.

```mcfunction
# Enable/disable idle wandering
/pvpbot settings idle true

# Set wander radius (3-50 blocks)
/pvpbot settings idleradius 10
```

### Behavior
- Bots walk slowly (not sprint)
- Stay within radius of spawn point
- Pick random destinations
- Avoid obstacles while wandering

---

## 🏃 Movement Speeds

Different situations use different speeds:

| Situation | Speed | Bhop |
|-----------|-------|------|
| Idle wandering | 0.5 | ❌ |
| Approaching target | 1.0 | ✅ |
| Eating (retreating) | 1.2 | ✅ |
| Low HP retreat | 1.5 | ✅ |

---

## ⚙️ Navigation Settings

| Setting | Range | Default | Description |
|---------|-------|---------|-------------|
| `bhop` | true/false | true | Enable bunny hop |
| `bhopcooldown` | 5-30 | 12 | Ticks between jumps |
| `jumpboost` | 0.0-0.5 | 0.0 | Extra jump height |
| `idle` | true/false | true | Enable idle wandering |
| `idleradius` | 3-50 | 10 | Wander radius |
| `movespeed` | 0.1-2.0 | 1.0 | Base movement speed |

---

## 🔧 Troubleshooting

### Bot gets stuck
- Bots automatically try to unstick themselves
- They jump and change direction after 10 ticks of no movement
- If still stuck, try teleporting the bot

### Bot won't climb
- Make sure the ladder/vine is properly placed
- Bot needs to be facing the climbable block

### Bot falls into holes
- Bots try to jump over 2-block gaps
- Larger gaps may cause falls
- Consider building bridges for important paths
