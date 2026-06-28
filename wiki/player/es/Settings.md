# Ajustes

Todas las configuraciones se guardan por mundo en`config/pvpbot/worlds/<worldname>/settings.json`.

## Ver y cambiar

```
/pvpbot settings                # List all settings
/pvpbot settings combat         # View a single setting
/pvpbot settings combat false   # Change a setting
```

## Configuración del equipo

| Configuración | Tipo | Predeterminado | Gama | Descripción |
|---------|------|---------|-------|-------------|
| armadura automática | booleano | verdadero | - | Equipa la mejor armadura del inventario |
| arma automática | booleano | verdadero | - | Equipa la mejor arma en la barra de acceso rápido |
| armadura de caída | booleano | falso | - | Caiga una armadura peor al reemplazar |
| arma de lanzamiento | booleano | falso | - | Suelta armas peores al reemplazar |
| distancia de caída | doble | 3.0 | 1,0 - 10,0 | Distancia para soltar artículos |
| intervalo | entero | 20 | 1 - 100 | Intervalo de verificación del equipo (tics) |

## Configuración de combate

| Configuración | Tipo | Predeterminado | Gama | Descripción |
|---------|------|---------|-------|-------------|
| combate | booleano | verdadero | - | Habilitar sistema de combate |
| venganza | booleano | verdadero | - | Auto-ataque último daño |
| objetivo automático | booleano | falso | - | Adquirir automáticamente el objetivo más cercano |
| jugadores-objetivo | booleano | verdadero | - | Apunta a jugadores reales |
| turbas objetivo | booleano | falso | - | Apunta a turbas hostiles |
| robots-objetivo | booleano | falso | - | Apunta a otros robots |
| distancia de visión | doble | 64,0 | 5,0 - 128,0 | Rango máximo de búsqueda de objetivos |
| ataque-invencible | booleano | falso | - | Atacar a jugadores creativos/espectadores |
| enfriamiento de ataque | entero | 10 | 1 - 40 | Tics entre ataques |
| críticos | booleano | verdadero | - | Salta para golpes críticos |
| garrapatas-caída-crit | entero | 6 | 1 - 10 | Se requieren garrapatas que caen para el crítico |
| rango cuerpo a cuerpo | doble | 3.5 | 2,0 - 6,0 | Distancia de alcance cuerpo a cuerpo |
| velocidad de movimiento | doble | 1.0 | 0,1 - 2,0 | Multiplicador de velocidad de movimiento |
| oportunidad perdida | entero | 0 | 0 - 100 | % de probabilidad de fallar ataques |
| posibilidad de error | entero | 0 | 0 - 100 | % de probabilidad de apuntar incorrectamente |
| velocidad de puntería | doble | 90,0 | 3,0 - 90,0 | Velocidad de rotación (grados/seg) |

## Configuración de armas

| Configuración | Tipo | Predeterminado | Gama | Descripción |
|---------|------|---------|-------|-------------|
| a distancia | booleano | verdadero | - | Habilitar combate con arco/ballesta |
| maza | booleano | verdadero | - | Habilitar el combate con maza |
| lanza | booleano | falso | - | Habilitar el combate con lanza |
| cristalpvp | booleano | verdadero | - | Habilitar PvP de cristal |
| anclapvp | booleano | verdadero | - | Habilitar PvP ancla |
| preferir-espada | booleano | verdadero | - | Prefiero espadas a hachas |
| maza-escudo | booleano | verdadero | - | Escudo automático contra ataques de maza |
| nombres especiales | booleano | falso | - | Utilice la lista de nombres especiales |
| rango de maza | doble | 6.0 | 3,0 - 10,0 | Rango de ataque de maza |
| alcance de lanza | doble | 4.5 | 2,0 - 8,0 | Rango de ataque de lanza |
| rango de carga de lanza | doble | 12.0 | 5,0 - 20,0 | Rango de inicio de carga de lanza |

### Combate a distancia

| Configuración | Tipo | Predeterminado | Gama | Descripción |
|---------|------|---------|-------|-------------|
| garrapatas de arco | entero | 40 | 5 - 100 | Tiempo completo de extracción del arco (ticks) |
| rango mínimo de rango | doble | 20.0 | 3,0 - 20,0 | Distancia mínima de compromiso del arco |
| rango-óptimo-rango | doble | 40,0 | 10,0 - 50,0 | Distancia ideal del arco |
| rango-máximo-rango | doble | 60,0 | 15,0 - 100,0 | Rango máximo de compromiso del arco |
| predicción de flecha | booleano | verdadero | - | Arco predictivo apuntando a objetivos en movimiento |
| ametrallamiento a distancia | booleano | verdadero | - | Ametrallarse de lado mientras dispara con el arco |
| retirada a distancia | booleano | verdadero | - | Retirarse cuando el objetivo se acerque |

## Configuración de utilidades

| Configuración | Tipo | Predeterminado | Gama | Descripción |
|---------|------|---------|-------|-------------|
| auto-tótem | booleano | verdadero | - | Autoequipar tótem de inmortalidad |
| prioridad tótem | booleano | verdadero | - | Mantenga el tótem al azar sobre el escudo |
| comer automáticamente | booleano | verdadero | - | Comer automáticamente cuando tengas hambre |
| escudo automático | booleano | verdadero | - | Escudo de elevación automática cuando sea necesario |
| poción automática | booleano | verdadero | - | Utilice vasijas curativas con poca salud |
| reparación automática | booleano | verdadero | - | Utilice botellas de XP para reparar equipos |
| rotura de escudo | booleano | verdadero | - | Enemigos que bloquean Axe-shield-break |
| telaraña | booleano | verdadero | - | Coloque telarañas en los objetivos |
| hambre mínima | entero | 14 | 1 - 20 | Nivel de hambre para empezar a comer |
| umbral de reparación | doble | 0,25 | 0,1 - 0,9 | % de durabilidad para activar la reparación |
| escudo-salud | doble | 0,5 | 0,1 - 1,0 | % de salud para sostener el escudo |

## Configuración del retiro

| Configuración | Tipo | Predeterminado | Gama | Descripción |
|---------|------|---------|-------|-------------|
| retiro | booleano | verdadero | - | Habilitar comportamiento de retirada |
| retiro-salud | doble | 0,3 | 0,1 - 0,9 | % de salud para empezar a retroceder |
| salud-critica | doble | 0,15 | 0,05 - 0,5 | % de salud para retirada crítica |

## Configuración de escudo

| Configuración | Tipo | Predeterminado | Gama | Descripción |
|---------|------|---------|-------|-------------|
| escudo-hold-garrapatas | entero | 60 | 10 - 200 | Garrapatas para mantener el escudo levantado |
| escudo-levantar-garrapatas | entero | 12 | 2 - 40 | Garrapatas para levantar el escudo antes del golpe previsto |
| oportunidad de rotura de escudo | entero | 40 | 0 - 100 | % de probabilidad de romper el escudo enemigo por golpe |

## Configuración de navegación

| Configuración | Tipo | Predeterminado | Gama | Descripción |
|---------|------|---------|-------|-------------|
| bhop | booleano | verdadero | - | Habilitar salto de conejito |
| inactivo | booleano | falso | - | Deambular inactivo cuando no hay objetivo |
| radio inactivo | doble | 10.0 | 3,0 - 50,0 | Radio de desplazamiento inactivo |

## Configuración de facción

| Configuración | Tipo | Predeterminado | Gama | Descripción |
|---------|------|---------|-------|-------------|
| facciones | booleano | verdadero | - | Habilitar sistema de facciones |
| fuego amigo | booleano | falso | - | Permitir atacar a los aliados |

## Configuraciones varias

| Configuración | Tipo | Predeterminado | Gama | Descripción |
|---------|------|---------|-------|-------------|
| bot-dejar-al-muerte | booleano | verdadero | - | Eliminar bot al morir |
| ataque-invencible | booleano | falso | - | Ataque en creativo/espectador |
| registros-bots | booleano | verdadero | - | Restaurar bots al reiniciar el servidor |
| desove seguro | booleano | verdadero | - | Desplazamiento aleatorio (±0,1-0,5 bloques) en el desove para evitar la asfixia |
| claro al quitar | booleano | verdadero | - | Limpiar el inventario antes de eliminar el bot |
| corrección de retraso de perfil | booleano | verdadero | - | Rellene previamente la caché del perfil para evitar retrasos en la aparición del bot |
| desove de masa máxima | entero | 1000 | 50-10000 | Máximo de bots permitidos por comando de generación masiva |
