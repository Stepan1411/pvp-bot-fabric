# facciones

Organiza bots en equipos/facciones para batallas coordinadas. Las facciones se guardan por mundo en`config/pvpbot/worlds/<worldname>/factions.json`.

## Comandos básicos

### Crear y eliminar
```
/pvpbot faction create Red
/pvpbot faction delete Red
```

### Administrar miembros
```
/pvpbot faction add Red Bot1
/pvpbot faction remove Red Bot1
/pvpbot faction add-near Red 30          # Add all bots within 30 blocks
/pvpbot faction add-all Red              # Add all existing bots
```

### Verificar información
```
/pvpbot faction list              # List all factions
/pvpbot faction info Red          # Show members and enemies
```

## Relaciones hostiles

Establece facciones como enemigas. Los bots atacarán automáticamente a los miembros de la facción enemiga.

```
/pvpbot faction hostile Red Blue
/pvpbot faction hostile Red Neutral false
```

**Nota:** Los enemigos siempre son mutuos: establecer que Rojo sea hostil hacia Azul también hace que Azul sea hostil hacia Rojo.

## Acciones Coordinadas

### Ataque
Todos los miembros de la facción atacan a un objetivo:
```
/pvpbot faction attack Red GreenPlayer
```

### Dar artículos
Entrega artículos a todos los miembros de la facción:
```
/pvpbot faction give Red diamond_sword 1
```

### Dar kit
Equipa a todos los miembros con un kit:
```
/pvpbot faction kit give-kit Red MyKit
```

### Dar kit aleatorio
Equipa a los miembros de la facción con un kit ponderado aleatorio:
```
/pvpbot faction kit give-kit-random Red warrior 60% archer 30% mage 10%
```

Cada miembro recibe un kit según la distribución del peso.

### Teletransportarse
Teletransporta a toda la facción gradualmente (5 bots cada 100 ms):
```
/pvpbot faction tp Red 100 64 -200
/pvpbot faction tp Red ~ ~ ~
/pvpbot faction tp Red PlayerName
```

Soporta coordenadas absolutas, relativas`~`, o apuntar a un jugador/bot por su nombre.

### Seguimiento de ruta
Todos los miembros siguen un camino:
```
/pvpbot faction path start Red MyPath
/pvpbot faction path stop Red
```

## Ajustes

| Configuración | Predeterminado | Descripción |
|---------|---------|-------------|
| facciones | verdadero | Habilitar sistema de facciones |
| fuego amigo | falso | Permitir atacar a miembros de la propia facción |
