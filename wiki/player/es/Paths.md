# 🛤️ Sistema de ruta

El sistema de ruta le permite crear rutas predefinidas para que las sigan los robots. Los robots pueden patrullar áreas, moverse entre ubicaciones y, opcionalmente, participar en combates mientras siguen caminos.

---

## 📋 Tabla de contenidos

- [Resumen](#resumen)
- [Creando rutas](#creando-rutas)
- [Gestión de waypoints](#managing-waypoints)
- [Control de bots](#bot-control)
- [Configuración de ruta] (#configuración de ruta)
- [Visualización](#visualización)
- [Ejemplos](#ejemplos)

---

## 🎯 Descripción general

Las rutas son secuencias de puntos de referencia que los robots pueden seguir. Cada camino tiene:
- **Nombre** - Identificador único
- **Waypoints** - Lista de posiciones (x, y, z)
- **Modo de bucle** - Cómo se mueve el robot a través de los puntos de referencia
- **Modo de ataque** - Si el bot se detiene para el combate
- **Visualización** - Efectos de partículas que muestran el camino

Las rutas se guardan por mundo en `config/pvpbot/worlds/{world}/paths.json`

---

## 🆕 Creando Caminos

### Crear una nueva ruta
```
/pvpbot path create <name>
```
Crea una ruta vacía con el nombre de pila.

**Ejemplo:**
```
/pvpbot path create patrol_route
```

### Eliminar una ruta
```
/pvpbot path delete <name>
```
Elimina la ruta y evita que todos los bots la sigan.

**Ejemplo:**
```
/pvpbot path delete patrol_route
```

### Listar todas las rutas
```
/pvpbot path list
```
Muestra todas las rutas disponibles en el mundo actual.

### Ver detalles de la ruta
```
/pvpbot path info <name>
```
Muestra información de ruta:
- Número de puntos de referencia
- Estado del modo bucle
- Estado del modo de ataque
- Lista de todas las coordenadas de los puntos de referencia.

**Ejemplo:**
```
/pvpbot path info patrol_route
```

---

## 📍 Gestión de waypoints

### Agregar punto de referencia
```
/pvpbot path add <name>
```
Agrega su posición actual como un nuevo punto de ruta a la ruta.

**Ejemplo:**
```
/pvpbot path add patrol_route
```
Párese en cada ubicación que desee que visite el bot y ejecute este comando.

### Eliminar punto de referencia
```
/pvpbot path remove <name> <index>
```
Elimina un waypoint específico por su índice (comenzando desde 0).

**Ejemplo:**
```
/pvpbot path remove patrol_route 2
```
Elimina el tercer punto de ruta del camino.

### Borrar todos los puntos de referencia
```
/pvpbot path clear <name>
```
Elimina todos los puntos de referencia de la ruta (mantiene la ruta en sí).

**Ejemplo:**
```
/pvpbot path clear patrol_route
```

---

## 🤖 Control de robots

### Empezar a seguir el camino
```
/pvpbot path follow <bot> <path>
```
Hace que un bot comience a seguir la ruta especificada.

**Ejemplo:**
```
/pvpbot path follow Guard1 patrol_route
```

### Deja de seguir el camino
```
/pvpbot path stop <bot>
```
Impide que el bot siga su ruta actual.

**Ejemplo:**
```
/pvpbot path stop Guard1
```

---

## ⚙️ Configuración de ruta

### Modo bucle
```
/pvpbot path loop <name> <true/false>
```

Controla cómo se mueve el bot a través de los puntos de referencia:
- **falso** (predeterminado) - Circular: 1→2→3→1→2→3...
- **verdadero** - De ida y vuelta: 1→2→3→2→1→2→3...

**Ejemplo:**
```
/pvpbot path loop patrol_route true
```

### Modo de ataque
```
/pvpbot path attack <name> <true/false>
```

Controla el comportamiento de combate mientras sigues el camino:
- **verdadero** (predeterminado): el robot se detiene en el punto de ruta actual para luchar y luego continúa
- **falso** - Bot ignora el combate y sigue moviéndose (BotCombat deshabilitado)

**Ejemplo:**
```
/pvpbot path attack patrol_route false
```

---

## 👁️ Visualización

### Alternar visualización de ruta
```
/pvpbot path show <name> <true/false>
```

Muestra/oculta efectos de partículas para el camino:
- **Puntos de ruta** - Partículas de cera en cada punto
- **Líneas** - Puntos de conexión de partículas de polvo verde

La visualización se habilita automáticamente cuando:
- Creando un camino
- Agregar un punto de referencia
- Empezar a seguir un camino

**Ejemplo:**
```
/pvpbot path show patrol_route true
```

Para desactivar la visualización:
```
/pvpbot path show patrol_route false
```

---

## 💡 Ejemplos

### Ruta de patrulla básica
```
# Create path
/pvpbot path create base_patrol

# Add waypoints (stand at each location)
/pvpbot path add base_patrol  # Point 1
/pvpbot path add base_patrol  # Point 2
/pvpbot path add base_patrol  # Point 3
/pvpbot path add base_patrol  # Point 4

# Make bot follow
/pvpbot path follow Guard1 base_patrol
```

### Guardia con combate
```
# Create path
/pvpbot path create guard_post

# Add waypoints
/pvpbot path add guard_post  # Position 1
/pvpbot path add guard_post  # Position 2

# Enable back-and-forth movement
/pvpbot path loop guard_post true

# Enable combat (default, but explicit)
/pvpbot path attack guard_post true

# Assign bot
/pvpbot path follow Guard1 guard_post
```

### Mensajero pacífico
```
# Create path
/pvpbot path create delivery_route

# Add waypoints
/pvpbot path add delivery_route  # Start
/pvpbot path add delivery_route  # Checkpoint 1
/pvpbot path add delivery_route  # Checkpoint 2
/pvpbot path add delivery_route  # End

# Disable combat (bot won't fight)
/pvpbot path attack delivery_route false

# Assign bot
/pvpbot path follow Courier1 delivery_route
```

### Varios bots en el mismo camino
```
# Create path
/pvpbot path create wall_patrol

# Add waypoints
/pvpbot path add wall_patrol  # Corner 1
/pvpbot path add wall_patrol  # Corner 2
/pvpbot path add wall_patrol  # Corner 3
/pvpbot path add wall_patrol  # Corner 4

# Assign multiple bots
/pvpbot path follow Guard1 wall_patrol
/pvpbot path follow Guard2 wall_patrol
/pvpbot path follow Guard3 wall_patrol
```

---

## 📝 Notas

- Las rutas se guardan automáticamente cuando se modifican.
- Cada mundo tiene su propio conjunto de caminos.
- Los robots miran el siguiente punto de referencia mientras se mueven.
- Cuando el modo de ataque es verdadero, los robots regresan al punto de ruta al que se dirigían después del combate.
- La visualización de la ruta es visible para todos los jugadores.
- Los robots alcanzan un punto de referencia cuando se encuentran a 1,5 cuadras de él.

---

## 🔗 Páginas relacionadas

- [Comandos](Commands.md) - Todos los comandos disponibles
- [Navegación] (Navigation.md) - Configuración de movimiento del bot
- [Combate] (Combat.md) - Detalles del sistema de combate
