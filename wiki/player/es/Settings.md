# ⚙️ Configuración

Lista completa de todas las opciones de configuración.

---

## 📋 Comandos

```mcfunction
# Show all settings
/pvpbot settings

# Show specific setting
/pvpbot settings <name>

# Change setting
/pvpbot settings <name> <value>
```

---

## ⚔️ Configuración de combate

| Configuración | Tipo | Gama | Predeterminado | Descripción |
|---------|------|-------|---------|-------------|
| `combate` | booleano | - | verdadero | Activar/desactivar sistema de combate |
| `venganza` | booleano | - | verdadero | Atacan entidades que dañan el bot |
| `objetivo automático` | booleano | - | falso | Buscar enemigos automáticamente |
| `jugadores objetivo` | booleano | - | verdadero | Puede apuntar a jugadores |
| `objetivos` | booleano | - | falso | Puede apuntar a turbas hostiles |
| `objetivos` | booleano | - | falso | Puede apuntar a otros robots |
| `críticos` | booleano | - | verdadero | Realizar golpes críticos |
| `a distancia` | booleano | - | verdadero | Utilice arcos/ballestas |
| `maza` | booleano | - | verdadero | Utilice maza con cargas de viento |
| `lanza` | booleano | - | falso | Usar lanza (deshabilitado debido a un error en la alfombra) |
| `ataquecooldown` | entero | 1-40 | 10 | Tics entre ataques |
| `cuerpo a cuerpo` | doble | 2-6 | 3.5 | Distancia de ataque cuerpo a cuerpo |
| `velocidad de movimiento` | doble | 0,1-2,0 | 1.0 | Multiplicador de velocidad de movimiento |
| `distancia de visualización` | doble | 5-128 | 64 | Rango máximo de detección de objetivos |
| `retirada` | booleano | - | verdadero | Retirarse cuando el HP esté bajo |
| `retirada` | doble | 0,1-0,9 | 0,3 | Porcentaje de HP para iniciar la retirada (30%) |

---

## 🧪 Configuración de pociones

| Configuración | Tipo | Gama | Predeterminado | Descripción |
|---------|------|-------|---------|-------------|
| `autopoción` | booleano | - | verdadero | Uso automático de pociones curativas/mejoradoras |
| `telaraña` | booleano | - | verdadero | Usa telarañas para ralentizar a los enemigos |

Los bots usan automáticamente:
- **Pociones curativas** cuando el HP es bajo
- **Pociones de fuerza** al entrar en combate
- **Pociones de velocidad** al entrar en combate
- **Pociones de resistencia al fuego** al entrar en combate
- **Telarañas** para ralentizar a los enemigos (cuando se retiran o el enemigo carga)

Todas las pociones de mejora se lanzan a la vez cuando comienza el combate o cuando expiran los efectos.

---

## 🚶 Configuración de navegación

| Configuración | Tipo | Gama | Predeterminado | Descripción |
|---------|------|-------|---------|-------------|
| `bhop` | booleano | - | verdadero | Habilitar salto de conejo |
| `bhopcooldown` | entero | 5-30 | 12 | Tics entre saltos de bhop |
| `impulso de salto` | doble | 0,0-0,5 | 0.0 | Altura de salto adicional |
| `inactivo` | booleano | - | verdadero | Deambular cuando no hay objetivo |
| `idleradio` | doble | 3-50 | 10 | Radio de desplazamiento inactivo |

---

## 🛡️ Configuración del equipo

| Configuración | Tipo | Gama | Predeterminado | Descripción |
|---------|------|-------|---------|-------------|
| `autoarmadura` | booleano | - | verdadero | Autoequipar la mejor armadura |
| `arma automática` | booleano | - | verdadero | Autoequipar la mejor arma |
| `autotótem` | booleano | - | verdadero | Autoequipar tótem de improviso |
| `escudo automático` | booleano | - | verdadero | Escudo de uso automático al bloquear |
| `prefiere la palabra` | booleano | - | verdadero | Prefiero la espada al hacha |
| `rotura de escudo` | booleano | - | verdadero | Cambia a hacha para romper el escudo enemigo |
| `arroja` | booleano | - | falso | Suelta piezas de armadura peores |
| `arma de lanzamiento` | booleano | - | falso | Tirar armas peores |
| `distancia de caída` | doble | 1-10 | 3.0 | Distancia de recogida del artículo |
| `intervalo` | entero | 1-100 | 20 | Intervalo de verificación del equipo (tics) |
| `nivel minarmor` | entero | 0-100 | 0 | Nivel mínimo de armadura para equipar |

### Niveles de armadura
| Nivel | Tipo de armadura |
|-------|------------|
| 0 | Cualquier armadura |
| 20 | Cuero+ |
| 40 | Oro+ |
| 50 | Cadena+ |
| 60 | Hierro+ |
| 80 | Diamante+ |
| 100 | Sólo Netherita |

---

## 🎭 Configuración de realismo

| Configuración | Tipo | Gama | Predeterminado | Descripción |
|---------|------|-------|---------|-------------|
| `oportunidad perdida` | entero | 0-100 | 10 | Posibilidad de fallar ataques (%) |
| `error` | entero | 0-100 | 5 | Posibilidad de atacar en dirección equivocada (%) |
| `retraso de reacción` | entero | 0-20 | 0 | Retraso antes de reaccionar (tics) |

---

## 👥 Otras configuraciones

| Configuración | Tipo | Gama | Predeterminado | Descripción |
|---------|------|-------|---------|-------------|
| `facciones` | booleano | - | verdadero | Habilitar sistema de facciones |

---

## 💾 Archivos de configuración

La configuración se guarda en:
```
config/pvp_bot.json
```

Los datos del bot (posiciones, dimensiones, modos de juego) se guardan en:
```
config/pvp_bot_bots.json
```

Tanto la configuración como los bots persisten tras los reinicios del servidor. Los bots se restauran automáticamente cuando se inicia el servidor.

---

## 📋 Ejemplos

### Haz que los bots sean más realistas
```mcfunction
/pvpbot settings misschance 15
/pvpbot settings mistakechance 10
/pvpbot settings reactiondelay 5
```

### Hacer que los bots sean agresivos
```mcfunction
/pvpbot settings autotarget true
/pvpbot settings targetplayers true
/pvpbot settings targetbots true
/pvpbot settings revenge true
```

### Desactivar el combate a distancia
```mcfunction
/pvpbot settings ranged false
/pvpbot settings mace false
```

### Movimiento rápido
```mcfunction
/pvpbot settings bhop true
/pvpbot settings bhopcooldown 8
/pvpbot settings jumpboost 0.2
/pvpbot settings movespeed 1.5
```

### Guardias estacionarias
```mcfunction
/pvpbot settings idle false
/pvpbot settings bhop false
```
