# Bausätze

Speichern und laden Sie Ausrüstungsvoreinstellungen für Bots. Kits werden global gespeichert in`config/pvpbot/kits.json`.

## Bausätze erstellen

1. Rüste deinen Charakter mit den gewünschten Gegenständen aus
2. Führen Sie den Befehl create aus:

```
/pvpbot kit create-kit warrior
/pvpbot kit create-kit archer
```

Kits speichern alle 41 Inventarplätze (Hotbar, Hauptinventar, Rüstung, Nebenhand) als NBT-Daten.

## Anbringen von Kits

### An einen einzelnen Bot oder Spieler
```
/pvpbot kit give-kit Bot1 warrior
```

### An Bots im Umkreis
Geben Sie allen Bots in einem Umkreis ein Kit (Standard: 10):
```
/pvpbot kit give-kit-near warrior 15
```

### Zufällig gewichtetes Kit für Bots im Umkreis
Geben Sie Bots in einem Umkreis ein zufälliges Kit basierend auf Gewichten:
```
/pvpbot kit give-kit-near-random 15 warrior 60% archer 30% mage 10%
```

Jeder Bot innerhalb von 15 Blöcken erhält ein Kit basierend auf der Gewichtsverteilung.

### An eine ganze Fraktion
```
/pvpbot faction kit give-kit Red warrior
```

### Zufällig gewichtetes Kit für die Fraktion
```
/pvpbot faction kit give-kit-random Red warrior 60% archer 30% mage 10%
```

## Kits verwalten
```
/pvpbot kit kits                  # List all kits
/pvpbot kit delete-kit warrior    # Delete a kit
```

## Notizen

- Kits leeren das Inventar des Ziels vollständig, bevor sie angewendet werden
- `give-kit-near`Und`give-kit-near-random`erfordern einen Player-Executor
- `give-kit-random`Und`give-kit-near-random`Arbeiten Sie von der Konsole oder dem Player aus
- Bei Kit-Namen wird die Groß-/Kleinschreibung nicht beachtet
- Kits sind global (über alle Welten hinweg geteilt)
- Zu den unterstützten Gegenständen gehören alle Minecraft-Gegenstände mit vollständigen NBT-Daten (Verzauberungen, Schaden usw.).
