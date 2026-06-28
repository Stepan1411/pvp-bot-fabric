# Comandos

Todos los comandos utilizan el`/pvpbot`prefijo.

## Gestión de robots

| Comando | Descripción |
|---------|-------------|
| `/pvpbot spawn [name]`| Generar un bot (nombre aleatorio si se omite) |
| `/pvpbot remove <name>`| Eliminar un bot específico |
| `/pvpbot removeall`| Eliminar todos los robots |
| `/pvpbot reload`| Recargar todas las configuraciones (configuraciones, kits, rutas, bots) |
| `/pvpbot bot-management list`| Listar todos los bots activos |
| `/pvpbot bot-management inventory <botname>`| Ver inventario y estadísticas de bots |
| `/pvpbot bot-management mass-spawn <1-50>`| Generar múltiples bots |

## Control de combate

| Comando | Descripción |
|---------|-------------|
| `/pvpbot bot-management attack <botname> <target>`| Forzar al robot a atacar a un objetivo |
| `/pvpbot bot-management stop-attack <botname>`| Deja de atacar |

## Ajustes

| Comando | Descripción |
|---------|-------------|
| `/pvpbot settings`| Listar todas las configuraciones actuales |
| `/pvpbot settings <name>`| Ver una configuración específica |
| `/pvpbot settings <name> <value>`| Establecer un valor de configuración |

Consulte [Configuración](Configuración) para conocer todas las opciones disponibles.

## Caminos

| Comando | Descripción |
|---------|-------------|
| `/pvpbot bot-management path create <name>`| Crear una nueva ruta |
| `/pvpbot bot-management path delete <name>`| Eliminar una ruta |
| `/pvpbot bot-management path add-point <name>`| Agregar posición actual como waypoint |
| `/pvpbot bot-management path remove-point <name> [index]`| Eliminar punto (último o por índice) |
| `/pvpbot bot-management path clear <name>`| Borrar todos los puntos |
| `/pvpbot bot-management path loop <name> <true/false>`| Alternar bucle |
| `/pvpbot bot-management path start <bot> <path>`| Iniciar bot siguiendo la ruta |
| `/pvpbot bot-management path stop <bot>`| Dejar que el bot siga la ruta |
| `/pvpbot bot-management path list`| Listar todas las rutas |
| `/pvpbot bot-management path show <name> <true/false>`| Alternar visualización de ruta |
| `/pvpbot bot-management path info <name>`| Ver detalles de la ruta |
| `/pvpbot bot-management path distribute <path>`| Distribuya los robots de manera uniforme a lo largo del camino |
| `/pvpbot bot-management path start-near <path> <radius>`| Ruta de inicio para bots cercanos |
| `/pvpbot bot-management path stop-all <path>`| Detener todos los robots en el camino |
| `/pvpbot bot-management path walk-type <name> <type>`| Establecer tipo de caminata (bhop/sprint/walk) |

Consulte [Rutas](Rutas) para conocer el uso detallado.

## Equipos

| Comando | Descripción |
|---------|-------------|
| `/pvpbot kit create-kit <name>`| Guarde su inventario como un kit |
| `/pvpbot kit delete-kit <name>`| Eliminar un kit |
| `/pvpbot kit give-kit <player> <kitname>`| Entregar el kit al jugador/bot |
| `/pvpbot kit kits`| Listar todos los kits |
| `/pvpbot kit give-kit-near <kitname> [radius]`| Dar kit a los bots dentro del radio (predeterminado: 10) |
| `/pvpbot kit give-kit-near-random <radius> <kit1> <w1>% [<kit2> <w2>% ...]`| Dar un kit ponderado aleatorio a los bots dentro del radio |

Consulte [Kits](Kits) para conocer su uso detallado.

## facciones

| Comando | Descripción |
|---------|-------------|
| `/pvpbot faction list`| Listar todas las facciones |
| `/pvpbot faction create <name>`| Crear una facción |
| `/pvpbot faction delete <name>`| Eliminar una facción |
| `/pvpbot faction add <faction> <player>`| Agregar jugador/bot a la facción |
| `/pvpbot faction remove <faction> <player>`| Eliminar jugador/bot de la facción |
| `/pvpbot faction hostile <f1> <f2> [true/false]`| Establecer relaciones hostiles |
| `/pvpbot faction info <name>`| Ver información de facción |
| `/pvpbot faction add-near <faction> <radius>`| Agregar bots cercanos a la facción |
| `/pvpbot faction add-all <faction>`| Agregar todos los bots a la facción |
| `/pvpbot faction give <faction> <item>`| Dar artículos a todos los miembros |
| `/pvpbot faction attack <faction> <target>`| Todos los miembros atacan al objetivo |
| `/pvpbot faction path start <faction> <path>`| Todos los miembros siguen el camino |
| `/pvpbot faction path stop <faction>`| Detener a todos los miembros en el camino |
| `/pvpbot faction tp <faction> <x y z\|player>`| Teletransporta a toda la facción gradualmente |

### Comandos del kit de facción

| Comando | Descripción |
|---------|-------------|
| `/pvpbot faction kit give-kit <faction> <kitname>`| Entregar kit a todos los miembros |
| `/pvpbot faction kit give-kit-random <faction> <kit1> <w1>% [<kit2> <w2>% ...]`| Entregar un kit ponderado aleatorio a los miembros de la facción |

Consulta [Factions](Factions) para conocer su uso detallado.
