# 🎮 Befehle

Alle PVP-Bot-Befehle beginnen mit „/pvpbot“. Erfordert Berechtigungsstufe 2 (Bediener).

---

## 📋 Inhaltsverzeichnis

- [Bot-Management](#-bot-management)
- [Kampfbefehle](#-combat-commands)
- [Fraktionsbefehle](#-fraktionsbefehle)
- [Kit-Befehle](#-kit-commands)
- [Pfadbefehle](#-pfadbefehle)
- [Einstellungen](#-Einstellungen)

---

## 🤖 Bot-Management

| Befehl | Beschreibung |
|---------|-------------|
| `/pvpbot spawn [Name]` | Erstellen Sie einen neuen Bot (zufälliger Name, falls nicht angegeben) |
| `/pvpbot massspawn <count>` | Erzeuge mehrere Bots mit zufälligen Namen (1-50) |
| `/pvpbot Remove <Name>` | Einen Bot entfernen |
| `/pvpbot removeall` | Alle Bots entfernen |
| `/pvpbot list` | Alle aktiven Bots auflisten |
| `/pvpbot inventory <name>` | Bot-Inventar anzeigen |

### Beispiele

```mcfunction
# Create a bot with random name
/pvpbot spawn

# Create a bot named "Guard1"
/pvpbot spawn Guard1

# Spawn 10 bots with random names (10% chance for special names)
/pvpbot massspawn 10

# Remove the bot
/pvpbot remove Guard1

# See all bots
/pvpbot list
```

**Hinweis:** Beim Spawnen von Bots mit zufälligen Namen besteht eine Chance von 10 %, dass sie einen speziellen Namen („nantag“ oder „Stepan1411“) anstelle eines generierten Namens erhalten.

---

## ⚔️ Kampfbefehle

| Befehl | Beschreibung |
|---------|-------------|
| `/pvpbot attack <bot> <target>` | Bot befehlen, einen Spieler/eine Entität anzugreifen |
| `/pvpbot stop <bot>` | Stoppen Sie den Angriff des Bots |
| `/pvpbot target <bot>` | Aktuelles Ziel des Bots anzeigen |

### Beispiele

```mcfunction
# Make Bot1 attack player Steve
/pvpbot attack Bot1 Steve

# Stop the attack
/pvpbot stop Bot1

# Check who Bot1 is targeting
/pvpbot target Bot1
```

---

## 👥 Fraktionsbefehle

| Befehl | Beschreibung |
|---------|-------------|
| `/pvpbot faction create <name>` | Erstelle eine neue Fraktion |
| `/pvpbot Fraktion löschen <Name>` | Eine Fraktion löschen |
| `/pvpbot Fraktion add <Fraktion> <Spieler>` | Spieler/Bot zur Fraktion hinzufügen |
| `/pvpbot Fraktion entferne <Fraktion> <Spieler>` | Aus Fraktion entfernen |
| `/pvpbot faction feindselig <f1> <f2> [wahr/falsch]` | Fraktionen als feindlich festlegen |
| `/pvpbot faction addnear <faction> <radius>` | Alle Bots in der Nähe hinzufügen |
| `/pvpbot Fraktion gib <Fraktion> <Gegenstand>` | Geben Sie den Artikel allen Mitgliedern |
| `/pvpbot Fraktion Givekit <Fraktion> <Kit>` | Kit an alle Mitglieder weitergeben |
| `/pvpbot Fraktionsangriff <Fraktion> <Ziel>` | Alle Bots in der Fraktion greifen Ziel an |
| `/pvpbot Fraktionsliste` | Alle Fraktionen auflisten |
| `/pvpbot Fraktionsinfo <Fraktion>` | Fraktionsdetails anzeigen |

### Beispiele

```mcfunction
# Create two factions
/pvpbot faction create RedTeam
/pvpbot faction create BlueTeam

# Add bots to factions
/pvpbot faction add RedTeam Bot1
/pvpbot faction add BlueTeam Bot2

# Make them enemies
/pvpbot faction hostile RedTeam BlueTeam

# Order entire faction to attack
/pvpbot faction attack RedTeam Steve

# Give swords to everyone in RedTeam
/pvpbot faction give RedTeam diamond_sword
```

---

## 🎒 Kit-Befehle

| Befehl | Beschreibung |
|---------|-------------|
| `/pvpbot createkit <Name>` | Erstellen Sie ein Kit aus Ihrem Inventar |
| `/pvpbot deletekit <Name>` | Ein Kit löschen |
| `/pvpbot kits` | Alle Kits auflisten |
| `/pvpbot Givekit <bot> <kit>` | Kit einem Bot geben |
| `/pvpbot Fraktion Givekit <Fraktion> <Kit>` | Kit an Fraktion weitergeben |

### Beispiele

```mcfunction
# Put items in your inventory, then:
/pvpbot createkit warrior

# Give kit to a bot
/pvpbot givekit Bot1 warrior

# Give kit to entire faction
/pvpbot faction givekit RedTeam warrior
```

---

## 🛤️ Pfadbefehle

| Befehl | Beschreibung |
|---------|-------------|
| `/pvpbot path create <name>` | Erstellen Sie einen neuen Pfad |
| `/pvpbot path delete <name>` | Einen Pfad löschen |
| `/pvpbot path add <name>` | Aktuelle Position als Wegpunkt hinzufügen |
| `/pvpbot Pfad entfernen <Name> <Index>` | Wegpunkt nach Index entfernen |
| `/pvpbot path clear <name>` | Alle Wegpunkte entfernen |
| `/pvpbot-Pfadliste` | Alle Pfade auflisten |
| `/pvpbot path info <name>` | Pfadinformationen anzeigen |
| `/pvpbot path follow <bot> <path>` | Bot dem Pfad folgen lassen |
| `/pvpbot path stop <bot>` | Bot daran hindern, dem Pfad zu folgen |
| `/pvpbot path loop <name> <true/false>` | Loop-Modus umschalten |
| `/pvpbot path attack <name> <true/false>` | Kampfmodus umschalten |
| `/pvpbot path show <name> <true/false>` | Pfadvisualisierung umschalten |

### Beispiele

```mcfunction
# Create a patrol route
/pvpbot path create patrol

# Add waypoints (stand at each location)
/pvpbot path add patrol
/pvpbot path add patrol
/pvpbot path add patrol

# Make bot follow the path
/pvpbot path follow Guard1 patrol

# Enable back-and-forth movement
/pvpbot path loop patrol true

# Disable combat while patrolling
/pvpbot path attack patrol false

# Show path with particles
/pvpbot path show patrol true
```

Eine ausführliche Anleitung finden Sie auf der Seite [Pfade](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Paths).

---

## ⚙️ Einstellungen

Verwenden Sie „/pvpbot-Einstellungen“, um alle aktuellen Einstellungen anzuzeigen.

Verwenden Sie „/pvpbot Settings <setting>“, um den aktuellen Wert anzuzeigen.

Verwenden Sie „/pvpbot Settings <setting> <value>“, um eine Einstellung zu ändern.

Eine vollständige Liste aller Einstellungen finden Sie auf der Seite [Einstellungen](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Settings).

### Schnelle Beispiele

```mcfunction
# Enable auto-targeting
/pvpbot settings autotarget true

# Set miss chance to 20%
/pvpbot settings misschance 20

# Enable bunny hop
/pvpbot settings bhop true
```
