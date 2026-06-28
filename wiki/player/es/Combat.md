# Sistema de combate

La IA de combate gestiona automáticamente la selección de objetivos, el cambio de armas y las decisiones tácticas.

## Modos de armas

Los bots seleccionan automáticamente el mejor modo de arma según la distancia y los elementos disponibles:

| Modo | Arma | Gama | Condiciones |
|------|--------|-------|------------|
| CUERPO A CUERPO | Espada/Hacha |`melee-range`(predeterminado 3.5) | Modo predeterminado |
| A DISTANCIA | Arco / Ballesta |`ranged-min-range` - `ranged-optimal-range`| Requiere flechas |
| MAZA | Maza |`mace-range`(predeterminado 6.0) |`mace`configuración habilitada |
| LANZA | Lanza |`spear-range` / `spear-charge-range` | `spear`configuración habilitada |
| CRISTAL | JcJ de cristal | 2,5 - 8,0 | Ver [Combate explosivo](Combate explosivo) |
| ANCLA | PvP ancla | 2,0 - 8,0 | Ver [Combate explosivo](Combate explosivo) |

## Selección de objetivo

Prioridad objetivo (evaluada en cada tick):

1. **Objetivo forzado** - Establecer mediante`/pvpbot bot-management attack`
2. **Venganza**: ataca automáticamente al último atacante (tiempo de espera de 30 segundos)
3. **Facción enemiga**: miembros de facción hostil (si las facciones están habilitadas)
4. **Objetivo automático**: enemigo válido más cercano dentro del alcance

Los objetivos válidos pueden incluir jugadores, mobs hostiles y otros robots (configurables a través de la configuración).

## Funciones de combate

### Golpes críticos
Los robots saltan antes de atacar para dar golpes críticos cuando`criticals`está habilitado.

### Gestión de escudos
- Predice ataques enemigos según la dirección y la distancia del sprint
- Levanta el escudo de forma preventiva utilizando el sistema de predicción.
- La baja salud provoca que se mantenga el escudo.
- Escudo parpadeante para mayor imprevisibilidad.

### Rompiendo escudo
Cuando un enemigo bloquea, los robots lo rompen con hachas y escudos con posibilidades configurables.

### Predicción del ataque enemigo
Realiza un seguimiento de los cambios de posición del enemigo, el estado de sprint y la distancia para predecir el momento del ataque para levantar el escudo.

### Lógica de retirada
Cuando la salud cae por debajo`retreat-health-percent`:
- Levanta el escudo y se aleja.
- Utiliza pociones curativas si están disponibles.
- Coloca telarañas para frenar la persecución.
- Come alimentos para regenerarse.

### Defensa de maza
Detecta enemigos usando maza (en el aire, cayendo) y levanta el escudo de forma preventiva: compensa el error del escudo vainilla.

### Colocación de telaraña
Los robots pueden colocar telarañas sobre los objetivos para inmovilizarlos (utiliza elementos del inventario).

## Referencia de configuración

| Configuración | Predeterminado | Descripción |
|---------|---------|-------------|
| combate | verdadero | Activar/desactivar el combate por completo |
| venganza | verdadero | Atacar automáticamente al último atacante |
| objetivo automático | falso | Adquirir automáticamente el objetivo más cercano |
| jugadores-objetivo | verdadero | Apunta a jugadores reales |
| turbas objetivo | falso | Apunta a turbas hostiles |
| robots-objetivo | falso | Apunta a otros robots |
| críticos | verdadero | Utilice golpes críticos con salto |
| enfriamiento de ataque | 10 | Tics entre ataques |
| rango cuerpo a cuerpo | 3.5 | Rango de ataque cuerpo a cuerpo |
| oportunidad perdida | 0% | Posibilidad de fallar ataques |
| posibilidad de error | 0% | Posibilidad de cometer errores de puntería |
| ataque-invencible | falso | Atacar a jugadores creativos/espectadores |
| velocidad de puntería | 60 | Velocidad de rotación (grados/seg) |
| distancia de visión | 64 | Rango máximo de adquisición de objetivos |
