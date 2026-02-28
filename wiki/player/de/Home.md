# 🤖 PVP-Bot – Wiki

Willkommen zur offiziellen PVP-Bot-Dokumentation!

---

## 📖 Ungefähr

PVP Bot ist ein Minecraft Fabric-Mod, der intelligente Kampfbots hinzufügt, die vom Carpet-Mod unterstützt werden. Erstelle Armeen aus Bots, organisiere sie in Fraktionen und sieh zu, wie sich epische Schlachten entfalten!

---

## 🚀 Schnellstart

1. Installieren Sie [Fabric Loader] (https://fabricmc.net/) und [Carpet Mod] (https://github.com/gnembon/fabric-carpet).
2. Laden Sie PVP Bot herunter und legen Sie es in Ihrem „Mods“-Ordner ab
3. Starten Sie das Spiel und erstellen Sie mit „/pvpbot spawn BotName“ Ihren ersten Bot!

---

## 📚 Dokumentation

| Seite | Beschreibung |
|------|-------------|
| [🎮 Befehle](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Commands) | Alle verfügbaren Befehle |
| [⚔️ Kampfsystem](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Combat) | Wie Bots kämpfen |
| [🚶 Navigation](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Navigation) | Bewegung und Wegfindung |
| [🛤️ Pfade](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Paths) | Pfadsystem und Wegpunkte |
| [👥 Fraktionen](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Factions) | Teamsystem |
| [🎒 Kits](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Kits) | Gerätevoreinstellungen |
| [⚙️ Einstellungen](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Settings) | Alle Konfigurationsoptionen |

---

## 💡 Schnelle Beispiele

### Erstellen Sie einen einfachen Bot
```
/pvpbot spawn MyBot
```

### Lass zwei Teams kämpfen
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

## 🔗 Links

- [GitHub-Repository](https://github.com/Stepan1411/pvp-bot-fabric)
- [Modrinth-Seite](https://modrinth.com/mod/pvp-bot)
- [Fehlerberichte](https://github.com/Stepan1411/pvp-bot-fabric/issues)
