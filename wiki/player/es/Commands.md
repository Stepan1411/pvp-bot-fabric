# 🎮 Comandos

Todos los comandos de PVP Bot comienzan con `/pvpbot`. Requiere nivel de permiso 2 (operador).

---

## 📋 Tabla de contenidos

- [Gestión de bots](#-administración de bots)
- [Comandos de combate](#-comandos-de-combate)
- [Comandos de facción](#-comandos-de-faccion)
- [Comandos del kit](#-comandos-kit)
- [Comandos de ruta](#-comandos-de-ruta)
- [Configuración] (#-configuración)

---

## 🤖 Gestión de bots

| Comando | Descripción |
|---------|-------------|
| `/pvpbot genera [nombre]` | Crear un nuevo bot (nombre aleatorio si no se especifica) |
| `/pvpbot massspawn <recuento>` | Genera múltiples bots con nombres aleatorios (1-50) |
| `/pvpbot eliminar <nombre>` | Eliminar un robot |
| `/pvpbot eliminartodo` | Eliminar todos los robots |
| `/ lista pvpbot` | Listar todos los bots activos |
| `/pvpbot inventario <nombre>` | Mostrar el inventario del bot |

### Ejemplos

```mcfunction
# Create a bot with random name
/pvpbot spawn

# Create a bot named "Guard1"
/pvpbot spawn Guard1

# Spawn 10 bots with random names (10% chance for special names)
/pvpbot massspawn 10

# Remove the bot
/pvpbot remove Guard1

# See all bots
/pvpbot list
```

**Nota:** Al generar bots con nombres aleatorios, hay un 10 % de posibilidades de que obtengan un nombre especial ("nantag" o "Stepan1411") en lugar de un nombre generado.

---

## ⚔️ Comandos de combate

| Comando | Descripción |
|---------|-------------|
| `/pvpbot ataque <bot> <objetivo>` | Ordenar al robot que ataque a un jugador/entidad |
| `/pvpbot detener <bot>` | Evite que el robot ataque |
| `/pvpbot objetivo <bot>` | Mostrar el objetivo actual del bot |

### Ejemplos

```mcfunction
# Make Bot1 attack player Steve
/pvpbot attack Bot1 Steve

# Stop the attack
/pvpbot stop Bot1

# Check who Bot1 is targeting
/pvpbot target Bot1
```

---

## 👥 Comandos de facción

| Comando | Descripción |
|---------|-------------|
| `/pvpbot facción crea <nombre>` | Crea una nueva facción |
| `/pvpbot facción eliminar <nombre>` | Eliminar una facción |
| `/pvpbot facción agregar <facción> <jugador>` | Agregar jugador/bot a la facción |
| `/pvpbot facción eliminar <facción> <jugador>` | Eliminar de la facción |
| `/pvpbot facción hostil <f1> <f2> [verdadero/falso]` | Establecer facciones como hostiles |
| `/pvpbot facción addnear <facción> <radio>` | Agregar todos los bots cercanos |
| `/pvpbot facción da <facción> <elemento>` | Dar artículo a todos los miembros |
| `/pvpbot facción Givekit <facción> <kit>` | Entregar kit a todos los miembros |
| `/pvpbot ataque de facción <facción> <objetivo>` | Todos los robots de la facción atacan al objetivo |
| `/lista de facciones pvpbot` | Listar todas las facciones |
| `/pvpbot información de facción <facción>` | Mostrar detalles de la facción |

### Ejemplos

```mcfunction
# Create two factions
/pvpbot faction create RedTeam
/pvpbot faction create BlueTeam

# Add bots to factions
/pvpbot faction add RedTeam Bot1
/pvpbot faction add BlueTeam Bot2

# Make them enemies
/pvpbot faction hostile RedTeam BlueTeam

# Order entire faction to attack
/pvpbot faction attack RedTeam Steve

# Give swords to everyone in RedTeam
/pvpbot faction give RedTeam diamond_sword
```

---

## 🎒 Comandos del kit

| Comando | Descripción |
|---------|-------------|
| `/pvpbot createkit <nombre>` | Crea kit desde tu inventario |
| `/pvpbot deletekit <nombre>` | Eliminar un kit |
| `/kits pvpbot` | Listar todos los kits |
| `/pvpbot darkit <bot> <kit>` | Dar kit a un bot |
| `/pvpbot facción Givekit <facción> <kit>` | Entregar kit a facción |

### Ejemplos

```mcfunction
# Put items in your inventory, then:
/pvpbot createkit warrior

# Give kit to a bot
/pvpbot givekit Bot1 warrior

# Give kit to entire faction
/pvpbot faction givekit RedTeam warrior
```

---

## 🛤️ Comandos de ruta

| Comando | Descripción |
|---------|-------------|
| `/pvpbot ruta crear <nombre>` | Crear una nueva ruta |
| `/ ruta pvpbot eliminar <nombre>` | Eliminar una ruta |
| `/pvpbot ruta agregar <nombre>` | Agregar posición actual como waypoint |
| `/pvpbot ruta eliminar <nombre> <índice>` | Eliminar waypoint por índice |
| `/pvpbot ruta clara <nombre>` | Eliminar todos los puntos de referencia |
| `/lista de rutas pvpbot` | Listar todas las rutas |
| `/pvpbot información de ruta <nombre>` | Mostrar información de ruta |
| `/pvpbot ruta sigue <bot> <ruta>` | Hacer que el bot siga el camino |
| `/pvpbot ruta parada <bot>` | Evite que el robot siga la ruta |
| `/pvpbot bucle de ruta <nombre> <verdadero/falso>` | Alternar modo de bucle |
| `/pvpbot ruta de ataque <nombre> <verdadero/falso>` | Alternar modo de combate |
| `/pvpbot ruta show <nombre> <verdadero/falso>` | Alternar visualización de ruta |

### Ejemplos

```mcfunction
# Create a patrol route
/pvpbot path create patrol

# Add waypoints (stand at each location)
/pvpbot path add patrol
/pvpbot path add patrol
/pvpbot path add patrol

# Make bot follow the path
/pvpbot path follow Guard1 patrol

# Enable back-and-forth movement
/pvpbot path loop patrol true

# Disable combat while patrolling
/pvpbot path attack patrol false

# Show path with particles
/pvpbot path show patrol true
```

Consulte la página [Paths](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Paths) para obtener una guía detallada.

---

## ⚙️ Configuración

Utilice `/pvpbot settings` para ver todas las configuraciones actuales.

Utilice `/pvpbot settings <configuración>` para ver el valor actual.

Utilice `/pvpbot settings <configuración> <valor>` para cambiar una configuración.

Consulte la página [Configuración](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Settings) para obtener una lista completa de todas las configuraciones.

### Ejemplos rápidos

```mcfunction
# Enable auto-targeting
/pvpbot settings autotarget true

# Set miss chance to 20%
/pvpbot settings misschance 20

# Enable bunny hop
/pvpbot settings bhop true
```
