# 👥 Fraktionssystem

Organisieren Sie Bots und Spieler in Teams, die gegeneinander kämpfen können!

---

## 📖 Übersicht

Fraktionen sind Gruppen von Bots und Spielern. Du kannst:
- Erstellen Sie Teams aus Bots
- Stellen Sie Fraktionen als feindselig zueinander ein
- Bots greifen automatisch Feinde feindlicher Fraktionen an

---

## 🏗️ Fraktionen erstellen

```mcfunction
# Create a faction
/pvpbot faction create RedTeam

# Delete a faction
/pvpbot faction delete RedTeam

# List all factions
/pvpbot faction list

# Get faction info
/pvpbot faction info RedTeam
```

---

## 👤 Mitglieder verwalten

### Mitglieder hinzufügen
```mcfunction
# Add a bot to faction
/pvpbot faction add RedTeam Bot1

# Add a player to faction
/pvpbot faction add RedTeam Steve

# Add all nearby bots (within 20 blocks)
/pvpbot faction addnear RedTeam 20
```

### Mitglieder entfernen
```mcfunction
/pvpbot faction remove RedTeam Bot1
```

---

## ⚔️ Feindliche Beziehungen

Machen Sie Fraktionen zu Feinden – ihre Mitglieder greifen sich automatisch gegenseitig an!

```mcfunction
# Make factions hostile
/pvpbot faction hostile RedTeam BlueTeam

# Make factions neutral again
/pvpbot faction hostile RedTeam BlueTeam false
```

### Wie es funktioniert
1. Bot von RedTeam sieht Spieler/Bot von BlueTeam
2. Wenn Fraktionen feindselig sind, nimmt der Bot sie automatisch ins Visier
3. Der Kampf beginnt!

> **Hinweis:** Für die automatische Ausrichtung muss „Autotarget“ aktiviert sein.

---

## 🎁 Gegenstände verschenken

### Gegenstände verschenken
```mcfunction
# Give diamond sword to all faction members
/pvpbot faction give RedTeam diamond_sword

# Give multiple items
/pvpbot faction give RedTeam diamond_sword 1
/pvpbot faction give RedTeam golden_apple 16
```

### Geben Sie Kits
```mcfunction
# Give a saved kit to entire faction
/pvpbot faction givekit RedTeam warrior
```

---

## 📋 Vollständiges Beispiel

Bilden Sie zwei Teams und lassen Sie sie kämpfen:

```mcfunction
# Create bots
/pvpbot spawn Red1
/pvpbot spawn Red2
/pvpbot spawn Red3
/pvpbot spawn Blue1
/pvpbot spawn Blue2
/pvpbot spawn Blue3

# Create factions
/pvpbot faction create Red
/pvpbot faction create Blue

# Add bots to factions
/pvpbot faction add Red Red1
/pvpbot faction add Red Red2
/pvpbot faction add Red Red3
/pvpbot faction add Blue Blue1
/pvpbot faction add Blue Blue2
/pvpbot faction add Blue Blue3

# Make them enemies
/pvpbot faction hostile Red Blue

# Give equipment
/pvpbot faction give Red diamond_sword
/pvpbot faction give Blue diamond_sword
/pvpbot faction give Red diamond_chestplate
/pvpbot faction give Blue diamond_chestplate

# Enable auto-targeting
/pvpbot settings autotarget true

# Watch the battle!
```

---

## ⚙️ Einstellungen

```mcfunction
# Enable/disable faction system
/pvpbot settings factions true
```

---

## 💾 Datenspeicherung

Fraktionsdaten werden gespeichert in:
```
config/pvp_bot_factions.json
```

Diese Datei bleibt über Serverneustarts hinweg bestehen.
