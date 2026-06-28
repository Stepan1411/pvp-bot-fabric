# Fraktionen

Organisieren Sie Bots in Teams/Fraktionen für koordinierte Schlachten. Fraktionen werden pro Welt gespeichert`config/pvpbot/worlds/<worldname>/factions.json`.

## Grundlegende Befehle

### Erstellen und Löschen
```
/pvpbot faction create Red
/pvpbot faction delete Red
```

### Mitglieder verwalten
```
/pvpbot faction add Red Bot1
/pvpbot faction remove Red Bot1
/pvpbot faction add-near Red 30          # Add all bots within 30 blocks
/pvpbot faction add-all Red              # Add all existing bots
```

### Informationen prüfen
```
/pvpbot faction list              # List all factions
/pvpbot faction info Red          # Show members and enemies
```

## Feindliche Beziehungen

Legen Sie Fraktionen als Feinde fest. Bots greifen automatisch gegnerische Fraktionsmitglieder an.

```
/pvpbot faction hostile Red Blue
/pvpbot faction hostile Red Neutral false
```

**Hinweis:** Feinde beruhen immer auf Gegenseitigkeit – wenn Rot gegen Blau feindlich eingestellt wird, wird Blau auch gegen Rot feindselig.

## Koordinierte Aktionen

### Angriff
Alle Fraktionsmitglieder greifen ein Ziel an:
```
/pvpbot faction attack Red GreenPlayer
```

### Gegenstände verschenken
Gib allen Fraktionsmitgliedern Gegenstände:
```
/pvpbot faction give Red diamond_sword 1
```

### Kit abgeben
Rüsten Sie alle Mitglieder mit einem Kit aus:
```
/pvpbot faction kit give-kit Red MyKit
```

### Gib ein zufälliges Kit
Rüsten Sie Fraktionsmitglieder mit einem zufällig gewichteten Kit aus:
```
/pvpbot faction kit give-kit-random Red warrior 60% archer 30% mage 10%
```

Jedes Mitglied erhält je nach Gewichtsverteilung ein Kit.

### Teleport
Die gesamte Fraktion nach und nach teleportieren (5 Bots pro 100 ms):
```
/pvpbot faction tp Red 100 64 -200
/pvpbot faction tp Red ~ ~ ~
/pvpbot faction tp Red PlayerName
```

Unterstützt absolute und relative Koordinaten`~`, oder einen Spieler/Bot namentlich ansprechen.

### Pfadverfolgung
Alle Mitglieder folgen einem Pfad:
```
/pvpbot faction path start Red MyPath
/pvpbot faction path stop Red
```

## Einstellungen

| Einstellung | Standard | Beschreibung |
|---------|---------|-------------|
| Fraktionen | wahr | Fraktionssystem aktivieren |
| Friendly-Fire | falsch | Angriffe auf eigene Fraktionsmitglieder zulassen |
