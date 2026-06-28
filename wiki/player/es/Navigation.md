# Navegación

El sistema de navegación maneja el movimiento del robot con detección de obstáculos y rotación suave.

## Tipos de movimiento

### B-Hop (predeterminado)
- Saltos automáticos mientras te mueves para saltar como un conejito
- Configurable mediante`bhop`configuración
- Se utiliza automáticamente para velocidad de movimiento >= 1,0

### Correr
- Movimiento de sprint estándar
- Se utiliza cuando bhop está deshabilitado.

### Caminar
- Sin carreras de velocidad, movimiento más lento
- Establecer a través del tipo de ruta a pie

## Funciones de movimiento

| Función | Descripción |
|----------|-------------|
| `moveToward(bot, target, speed)`| Avanzar hacia una entidad |
| `moveAway(bot, target, speed)`| Alejarse de una entidad |
| `moveTowardPosition(bot, pos, speed)`| Avanzar hacia una posición |
| `moveTowardCombat(bot, pos, speed, strafe)`| Combatir el movimiento de ametrallamiento |

## Aspecto suave

Usos de rotación configurables`aim-speed`(grados por tick) para un seguimiento fluido:
- `lookAt(bot, entity)`— Seguimiento de entidad
- `lookAtPosition(bot, pos)`— Posición de la pista
- `lookAway(bot, entity)`— Mirar hacia otro lado (retrocediendo)

## Detección de obstáculos

El bot detecta y maneja:

| Obstáculo | Comportamiento |
|----------|----------|
| **Bloques sólidos** | Salta para borrar |
| **Paredes** | Pasos laterales (evite cambios de dirección) |
| **Agujeros** | Salta |
| **Escaleras / Enredaderas** | Sube automáticamente (sprint desactivado) |
| **Agua** | Nada con control direccional |

## Detección de atascados

Si el robot se mueve menos de 0,05 bloques mientras está en el suelo durante más de 10 tics:
1. Alterna la dirección de evitación
2. Salta para escapar
3. Intenta esquivar los obstáculos

## Manejo de retroceso

Cuando la velocidad horizontal excede 0,35 (retroceso), el robot deja de correr y resiste el movimiento por hasta 10 tics.

## Deambular inactivo

Cuando está inactivo (sin objetivo) y`idle`configuración está habilitada, los robots deambulan aleatoriamente dentro`idle-radius`(por defecto 10 bloques) desde la posición de generación.

## W-Toque

Después de atacar, el robot lanza brevemente un sprint (W-tap) para restablecer la distancia de retroceso del oponente.

## Ametrallamiento de combate

En el modo de combate, los robots atacan de izquierda a derecha con direcciones aleatorias, cambiando cada 8 a 18 tics.

## Ajustes

| Configuración | Predeterminado | Descripción |
|---------|---------|-------------|
| bhop | verdadero | Habilitar salto de conejito |
| inactivo | falso | Habilitar deambulación inactiva |
| radio inactivo | 10 | Radio de recorrido desde el desove |
| velocidad de movimiento | 1.0 | Multiplicador de velocidad de movimiento |
| velocidad de puntería | 60 | Velocidad de rotación (grados/seg) |
