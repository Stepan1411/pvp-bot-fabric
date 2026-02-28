# ⚔️ Sistema de combate

PVP Bot presenta una IA de combate avanzada que puede usar diferentes armas y tácticas.

---

## 🗡️ Tipos de armas

### Combate cuerpo a cuerpo
- **Espadas** - Ataques rápidos, buen daño
- **Hachas** - Más lento pero puede romper escudos
- Los bots cambian automáticamente a cuerpo a cuerpo cuando los enemigos están cerca

### Combate a distancia
- **Arcos** - Dibuja y suelta flechas
- **Ballestas** - Cargar y disparar virotes
- Los robots mantienen una distancia óptima (8-20 bloques)

### Combate con maza
- **Maza + Carga de viento** - Ataques de salto que causan daño masivo
- Los robots usan cargas de viento para lanzarse al aire.
- Devastadores ataques de caída

---

## 🎯 Orientación

### Modo venganza
Cuando un robot recibe daño, automáticamente apunta al atacante.
```mcfunction
/pvpbot settings revenge true
```

### Orientación automática
Los bots buscan automáticamente enemigos dentro de la distancia de visión.
```mcfunction
/pvpbot settings autotarget true
```

### Objetivo manual
Obligar a un robot a atacar a un objetivo específico.
```mcfunction
/pvpbot attack BotName TargetName
```

### Filtros de destino
Elija a qué pueden apuntar los bots:
```mcfunction
/pvpbot settings targetplayers true   # Target players
/pvpbot settings targetmobs true      # Target hostile mobs
/pvpbot settings targetbots true      # Target other bots
```

---

## 🛡️ Defensa

### Escudo automático
Los robots levantan escudos automáticamente cuando los enemigos atacan.
```mcfunction
/pvpbot settings autoshield true
```

### Rompiendo escudo
Los robots usan hachas para desactivar los escudos enemigos.
```mcfunction
/pvpbot settings shieldbreak true
```

### Auto-Tótem
Los robots mantienen tótems de inmortalidad a la ligera.
```mcfunction
/pvpbot settings autototem true
```

---

## 🍎 Curación

### Comer automáticamente
Los robots comen comida cuando:
- La salud es baja (< 30%)
- El hambre está por debajo del umbral

```mcfunction
/pvpbot settings autoeat true
/pvpbot settings minhunger 14
```

### Pociones automáticas
Los bots usan pociones automáticamente:
- **Pociones curativas** - cuando el HP es bajo (salpicaduras o bebibles)
- **Pociones de fuerza** - al entrar en combate
- **Pociones de velocidad** - al entrar en combate
- **Pociones de resistencia al fuego** - al entrar en combate

Todas las pociones de mejora se lanzan a la vez cuando comienza el combate. Los robots vuelven a aplicar mejoras cuando los efectos expiran (quedan menos de 5 segundos).

```mcfunction
/pvpbot settings autopotion true
```

### Retiro
Cuando la salud es baja, los robots se retiran mientras comen o se curan.
La retirada está desactivada si el robot no tiene comida (lucha hasta la muerte).

```mcfunction
/pvpbot settings retreat true
/pvpbot settings retreathp 0.3  # 30% HP
```

---

## 💥 Golpes críticos

Los robots pueden realizar golpes críticos cronometrando sus ataques con saltos.
```mcfunction
/pvpbot settings criticals true
```

---

## 🕸️ Tácticas de telaraña

Los bots pueden utilizar las telarañas estratégicamente:
- **Al retirarse**: coloca una telaraña debajo del enemigo que lo persigue para ralentizarlo.
- **En combate cuerpo a cuerpo** - coloca una telaraña debajo del enemigo que carga

```mcfunction
/pvpbot settings cobweb true
```

---

## ⚙️ Configuración de combate

| Configuración | Gama | Predeterminado | Descripción |
|---------|-------|---------|-------------|
| `combate` | verdadero/falso | verdadero | Habilitar combate |
| `venganza` | verdadero/falso | verdadero | Ataque a quien te atacó |
| `objetivo automático` | verdadero/falso | falso | Encontrar enemigos automáticamente |
| `críticos` | verdadero/falso | verdadero | Golpes críticos |
| `a distancia` | verdadero/falso | verdadero | Utilice arcos |
| `maza` | verdadero/falso | verdadero | Utilice maza |
| `lanza` | verdadero/falso | falso | Utilice lanza (buggy) |
| `autopoción` | verdadero/falso | verdadero | Pociones de uso automático |
| `telaraña` | verdadero/falso | verdadero | Utilice telarañas |
| `retirada` | verdadero/falso | verdadero | Retirarse cuando el HP esté bajo |
| `retirada` | 0,1-0,9 | 0,3 | % de HP para retirarse |
| `ataquecooldown` | 1-40 | 10 | Tics entre ataques |
| `cuerpo a cuerpo` | 2-6 | 3.5 | Distancia de ataque cuerpo a cuerpo |
| `distancia de visualización` | 5-128 | 64 | Rango de búsqueda objetivo |
