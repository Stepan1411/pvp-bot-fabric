# 👥 Sistema de facciones

¡Organiza robots y jugadores en equipos que puedan luchar entre sí!

---

## 📖 Descripción general

Las facciones son grupos de bots y jugadores. Puede:
- Crear equipos de bots.
- Establecer facciones como hostiles entre sí.
- Los bots atacan automáticamente a enemigos de facciones hostiles.

---

## 🏗️ Creando facciones

```mcfunction
# Create a faction
/pvpbot faction create RedTeam

# Delete a faction
/pvpbot faction delete RedTeam

# List all factions
/pvpbot faction list

# Get faction info
/pvpbot faction info RedTeam
```

---

## 👤 Administrador de miembros

### Agregar miembros
```mcfunction
# Add a bot to faction
/pvpbot faction add RedTeam Bot1

# Add a player to faction
/pvpbot faction add RedTeam Steve

# Add all nearby bots (within 20 blocks)
/pvpbot faction addnear RedTeam 20
```

### Eliminar miembros
```mcfunction
/pvpbot faction remove RedTeam Bot1
```

---

## ⚔️ Relaciones hostiles

Haz que las facciones sean enemigas: ¡sus miembros se atacarán automáticamente entre sí!

```mcfunction
# Make factions hostile
/pvpbot faction hostile RedTeam BlueTeam

# Make factions neutral again
/pvpbot faction hostile RedTeam BlueTeam false
```

### Cómo funciona
1. El bot de RedTeam ve al jugador/bot de BlueTeam
2. Si las facciones son hostiles, el bot las ataca automáticamente.
3. ¡Empieza el combate!

> **Nota:** Requiere que "autotarget" esté habilitado para la orientación automática.

---

## 🎁 Dar artículos

### Dar artículos
```mcfunction
# Give diamond sword to all faction members
/pvpbot faction give RedTeam diamond_sword

# Give multiple items
/pvpbot faction give RedTeam diamond_sword 1
/pvpbot faction give RedTeam golden_apple 16
```

### Regalar kits
```mcfunction
# Give a saved kit to entire faction
/pvpbot faction givekit RedTeam warrior
```

---

## 📋 Ejemplo completo

Crea dos equipos y hazlos luchar:

```mcfunction
# Create bots
/pvpbot spawn Red1
/pvpbot spawn Red2
/pvpbot spawn Red3
/pvpbot spawn Blue1
/pvpbot spawn Blue2
/pvpbot spawn Blue3

# Create factions
/pvpbot faction create Red
/pvpbot faction create Blue

# Add bots to factions
/pvpbot faction add Red Red1
/pvpbot faction add Red Red2
/pvpbot faction add Red Red3
/pvpbot faction add Blue Blue1
/pvpbot faction add Blue Blue2
/pvpbot faction add Blue Blue3

# Make them enemies
/pvpbot faction hostile Red Blue

# Give equipment
/pvpbot faction give Red diamond_sword
/pvpbot faction give Blue diamond_sword
/pvpbot faction give Red diamond_chestplate
/pvpbot faction give Blue diamond_chestplate

# Enable auto-targeting
/pvpbot settings autotarget true

# Watch the battle!
```

---

## ⚙️ Configuración

```mcfunction
# Enable/disable faction system
/pvpbot settings factions true
```

---

## 💾 Almacenamiento de datos

Los datos de las facciones se guardan en:
```
config/pvp_bot_factions.json
```

Este archivo persiste después de que se reinicia el servidor.
