# 🤖 Bot PvP - Wiki

¡Bienvenido a la documentación oficial de PVP Bot!

---

## 📖 Acerca de

PVP Bot es un mod de Minecraft Fabric que agrega robots de combate inteligentes impulsados ​​por el mod HeroBot. ¡Crea ejércitos de robots, organízalos en facciones y observa cómo se desarrollan batallas épicas!

---

## 🚀 Inicio rápido

1. Instale [Fabric Loader](https://fabricmc.net/) y [HeroBot Mod](https://modrinth.com/mod/herobot)
2. Descarga PVP Bot y colócalo en tu`mods`carpeta
3. Inicie el juego y use`/pvpbot spawn BotName`para crear tu primer bot!

---

## 📚 Documentación

| Página | Descripción |
|------|-------------|
| [🎮 Comandos](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Commands) | Todos los comandos disponibles |
| [⚔️ Sistema de combate](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Combat) | Cómo luchan los robots |
| [🚶 Navegación](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Navigation) | Búsqueda de caminos básicos |
| [🛤️ Rutas](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Paths) | Sistema de rutas y puntos de referencia |
| [👥 Facciones](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Factions) | Sistema de equipo |
| [🎒 Kits](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Kits) | Preajustes de equipos |
| [⚙️ Configuración](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Settings) | Todas las opciones de configuración |

---

## 💡 Ejemplos rápidos

### Crea un bot simple
```
/pvpbot spawn MyBot
```

### Haz que dos equipos peleen
```
/pvpbot spawn Red1
/pvpbot spawn Blue1
/pvpbot faction create Red
/pvpbot faction create Blue
/pvpbot faction add Red Red1
/pvpbot faction add Blue Blue1
/pvpbot faction hostile Red Blue
```

---

## 🔗 Enlaces

- [Repositorio de GitHub] (https://github.com/Stepan1411/pvp-bot-fabric)
- [Página de Modrinth](https://modrinth.com/mod/pvp-bot)
- [Informes de errores](https://github.com/Stepan1411/pvp-bot-fabric/issues)
