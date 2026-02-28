# 🚶 Sistema de navegación

PVP Bot presenta mecánicas de movimiento y búsqueda de caminos inteligentes.

---

## 🧭 Búsqueda de caminos

### Evitación de obstáculos
Los bots detectan y evitan automáticamente:
- **Paredes** - Gira a la izquierda/derecha para rodear
- **Agujeros** - Saltar sobre huecos
- **Obstáculos de 1 bloque** - Saltar

### Escalada
Los bots pueden escalar:
- escaleras
- Vides
- Andamios
- Enredaderas retorcidas/lloronas

---

## 🐰 Salto del conejito (Bhop)

Los robots pueden saltar para moverse más rápido: saltar mientras corren para aumentar la velocidad.

```mcfunction
# Enable/disable bhop
/pvpbot settings bhop true

# Set cooldown between jumps (5-30 ticks)
/pvpbot settings bhopcooldown 12

# Add extra jump height (0.0-0.5)
/pvpbot settings jumpboost 0.1
```

### Cuando se activa Bhop
- La velocidad es >= 1,0
- No hay obstáculos por delante
- No escalar
- En el suelo

---

## 😴 Vagabundo inactivo

Cuando los robots no tienen un objetivo, deambulan por su punto de aparición.

```mcfunction
# Enable/disable idle wandering
/pvpbot settings idle true

# Set wander radius (3-50 blocks)
/pvpbot settings idleradius 10
```

### Comportamiento
- Los robots caminan lentamente (no corren)
- Manténgase dentro del radio del punto de generación
- Elige destinos aleatorios
- Evita los obstáculos mientras deambulas.

---

## 🏃 Velocidades de movimiento

Diferentes situaciones utilizan diferentes velocidades:

| Situación | Velocidad | Bhop |
|-----------|---------------|------|
| Vagabundo inactivo | 0,5 | ❌ |
| Acercándose al objetivo | 1.0 | ✅ |
| Comer (retirándose) | 1.2 | ✅ |
| Retiro de HP bajo | 1.5 | ✅ |

---

## ⚙️ Configuración de navegación

| Configuración | Gama | Predeterminado | Descripción |
|---------|-------|---------|-------------|
| `bhop` | verdadero/falso | verdadero | Habilitar salto de conejo |
| `bhopcooldown` | 5-30 | 12 | Tics entre saltos |
| `impulso de salto` | 0,0-0,5 | 0.0 | Altura de salto adicional |
| `inactivo` | verdadero/falso | verdadero | Habilitar deambulación inactiva |
| `idleradio` | 3-50 | 10 | Radio de desplazamiento |
| `velocidad de movimiento` | 0,1-2,0 | 1.0 | Velocidad de movimiento base |

---

## 🔧 Solución de problemas

### El robot se atasca
- Los bots intentan despegarse automáticamente
- Saltan y cambian de dirección después de 10 tics sin movimiento.
- Si aún estás atascado, intenta teletransportar el robot.

### El robot no sube
- Asegúrese de que la escalera/enredadera esté colocada correctamente
- El robot debe estar de cara al bloque escalable.

### Bot cae en agujeros
- Los robots intentan saltar espacios de 2 bloques.
- Los espacios más grandes pueden provocar caídas.
- Considere la posibilidad de construir puentes para caminos importantes.
