# Sistema de ruta

Cree rutas de puntos de referencia para que las sigan los robots. Los caminos se guardan por mundo en`config/pvpbot/worlds/<worldname>/paths.json`.

## Comandos

### Creando caminos
```
/pvpbot bot-management path create MyPath
```

### Agregar puntos de referencia
Párese en la ubicación deseada y ejecute:
```
/pvpbot bot-management path add-point MyPath
```
La visualización de rutas (partículas) se habilita automáticamente.

### Gestión de puntos de referencia
```
/pvpbot bot-management path remove-point MyPath      # Remove last
/pvpbot bot-management path remove-point MyPath 0    # Remove by index
/pvpbot bot-management path clear MyPath             # Clear all points
/pvpbot bot-management path info MyPath              # List all points
/pvpbot bot-management path list                     # List all paths
```

### Siguiendo caminos
```
/pvpbot bot-management path start Bot1 MyPath
/pvpbot bot-management path stop Bot1
/pvpbot bot-management path start-near MyPath 20     # Start for bots within 20 blocks
/pvpbot bot-management path stop-all MyPath          # Stop all bots on path
```

### Distribución
Distribuya uniformemente los robots a lo largo de los puntos del camino:
```
/pvpbot bot-management path distribute MyPath
```

### Tipos de caminata
```
/pvpbot bot-management path walk-type MyPath bhop    # Bunny hop (default)
/pvpbot bot-management path walk-type MyPath sprint  # Sprint
/pvpbot bot-management path walk-type MyPath walk    # Walk
```

### Modo bucle
```
/pvpbot bot-management path loop MyPath true
/pvpbot bot-management path loop MyPath false
```

In loop mode, bots reverse direction at the end (ping-pong). In non-loop mode, bots restart from the beginning.

### Visualización
```
/pvpbot bot-management path show MyPath true
/pvpbot bot-management path show MyPath false
```

Muestra los puntos de ruta como partículas (WAX_ON + líneas de polvo verdes).

## Caminos de facción

Controla todos los bots de una facción a la vez:
```
/pvpbot faction path start RedFaction MyPath
/pvpbot faction path stop RedFaction
```

## Propiedades

| Propiedad | Opciones | Predeterminado |
|----------|---------|---------|
| Tipo de caminata | bhop / sprint / caminata | bhop |
| Bucle | verdadero/falso | falso |
| Ataque | verdadero/falso | verdadero |
| Puntos | variables (Vec3d) | - |
