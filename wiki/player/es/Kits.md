# Kits

Guarde y cargue ajustes preestablecidos de equipos para bots. Los kits se guardan globalmente en`config/pvpbot/kits.json`.

## Creando kits

1. Equipa a tu personaje con los elementos deseados.
2. Ejecute el comando de creación:

```
/pvpbot kit create-kit warrior
/pvpbot kit create-kit archer
```

Los kits guardan los 41 espacios del inventario (barra de acceso rápido, inventario principal, armadura, mano izquierda) como datos NBT.

## Kits de aplicación

### A un solo bot o jugador
```
/pvpbot kit give-kit Bot1 warrior
```

### A los bots dentro del radio
Entrega un kit a todos los bots dentro de un radio (predeterminado: 10):
```
/pvpbot kit give-kit-near warrior 15
```

### Kit ponderado aleatorio para bots dentro del radio
Entrega un kit aleatorio basado en pesos a los bots dentro de un radio:
```
/pvpbot kit give-kit-near-random 15 warrior 60% archer 30% mage 10%
```

Cada robot dentro de 15 bloques recibe un kit según la distribución del peso.

### A toda una facción
```
/pvpbot faction kit give-kit Red warrior
```

### Kit ponderado aleatorio para facción
```
/pvpbot faction kit give-kit-random Red warrior 60% archer 30% mage 10%
```

## Gestión de kits
```
/pvpbot kit kits                  # List all kits
/pvpbot kit delete-kit warrior    # Delete a kit
```

## Notas

- Los kits limpian completamente el inventario del objetivo antes de aplicarlos.
- `give-kit-near`y`give-kit-near-random`Requiere un jugador ejecutor.
- `give-kit-random`y`give-kit-near-random`trabajar desde consola o reproductor
- Los nombres de los kits no distinguen entre mayúsculas y minúsculas.
- Los kits son globales (compartidos en todos los mundos)
- Los elementos admitidos incluyen cualquier elemento de Minecraft con datos NBT completos (encantamientos, daños, etc.)
