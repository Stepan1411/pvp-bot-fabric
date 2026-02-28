# 🎒 Sistema de kit

¡Guarda ajustes preestablecidos de equipos y equipa bots rápidamente!

---

## 📖 Descripción general

Los kits le permiten:
- Guarde su inventario actual como plantilla
- Entrega rápidamente equipo a los bots.
- Equipa facciones enteras a la vez

---

## 📦 Creación de kits

1. Pon artículos en tu inventario (armadura, armas, comida, etc.)
2. Ejecute el comando crear

```mcfunction
/pvpbot createkit <name>
```

### Lo que se guarda
- ✅ Artículos de la barra de acceso rápido (espacios 0-8)
- ✅ Artículos de inventario
- ✅ Piezas de armadura
- ✅ Artículo improvisado
- ✅ Encantamientos de artículos
- ✅ Durabilidad del artículo
- ✅ Tamaños de pila

### Ejemplo
```mcfunction
# Put diamond armor, sword, bow, arrows, golden apples in your inventory
# Then save it:
/pvpbot createkit pvp_warrior
```

---

## 📋 Gestión de kits

### Kits de lista
```mcfunction
/pvpbot kits
```

### Eliminar equipo
```mcfunction
/pvpbot deletekit pvp_warrior
```

---

## 🎁 Kits de donación

### A un solo robot
```mcfunction
/pvpbot givekit Bot1 pvp_warrior
```

### A toda la facción
```mcfunction
/pvpbot faction givekit RedTeam pvp_warrior
```

---

## 💡 Ideas de kits

### ⚔️ Luchador cuerpo a cuerpo
- Espada de diamante/netherita
- Armadura de diamante completa
- Blindaje
- manzanas doradas
- Tótem de lo inmortal (de improviso)

### 🏹 Arquero
- Arco (Poder V, Infinito)
- Flecha (1 pila)
- Armadura de cuero/cadena
- manzanas doradas

### 🔨 Tanque
- Armadura Netherite (Protección IV)
- Blindaje
- Hacha (para romper escudo)
- Muchas manzanas doradas
- Múltiples tótems

### 💨 Luchador de velocidad
- Armadura ligera (cuero/cadena)
- Espada de diamante (Nitidez V)
- pociones de velocidad
- manzanas doradas

---

## 📋 Ejemplo completo

```mcfunction
# Step 1: Prepare your inventory with items you want

# Step 2: Create the kit
/pvpbot createkit soldier

# Step 3: Spawn bots
/pvpbot spawn Soldier1
/pvpbot spawn Soldier2
/pvpbot spawn Soldier3

# Step 4: Give kit to all bots
/pvpbot givekit Soldier1 soldier
/pvpbot givekit Soldier2 soldier
/pvpbot givekit Soldier3 soldier

# Or create a faction and give kit to all at once:
/pvpbot faction create Army
/pvpbot faction add Army Soldier1
/pvpbot faction add Army Soldier2
/pvpbot faction add Army Soldier3
/pvpbot faction givekit Army soldier
```

---

## 💾 Almacenamiento de datos

Los datos del kit se guardan en:
```
config/pvp_bot_kits.json
```

Los kits persisten tras los reinicios del servidor.

---

## ⚠️ Notas

- Los robots equiparán automáticamente la armadura del kit.
- Los bots seleccionarán automáticamente la mejor arma.
- Los elementos existentes en el inventario de bots NO se borran
- Si el inventario del bot está lleno, es posible que no se entreguen algunos artículos.
