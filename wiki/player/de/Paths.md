# Pfadsystem

Erstellen Sie Wegpunktpfade, denen Bots folgen können. Pfade werden pro Welt gespeichert`config/pvpbot/worlds/<worldname>/paths.json`.

## Befehle

### Pfade erstellen
```
/pvpbot bot-management path create MyPath
```

### Wegpunkte hinzufügen
Stellen Sie sich an die gewünschte Stelle und führen Sie Folgendes aus:
```
/pvpbot bot-management path add-point MyPath
```
Die Pfadvisualisierung (Partikel) ist automatisch aktiviert.

### Wegpunkte verwalten
```
/pvpbot bot-management path remove-point MyPath      # Remove last
/pvpbot bot-management path remove-point MyPath 0    # Remove by index
/pvpbot bot-management path clear MyPath             # Clear all points
/pvpbot bot-management path info MyPath              # List all points
/pvpbot bot-management path list                     # List all paths
```

### Den Pfaden folgen
```
/pvpbot bot-management path start Bot1 MyPath
/pvpbot bot-management path stop Bot1
/pvpbot bot-management path start-near MyPath 20     # Start for bots within 20 blocks
/pvpbot bot-management path stop-all MyPath          # Stop all bots on path
```

### Verteilung
Verteilen Sie Bots gleichmäßig entlang der Pfadpunkte:
```
/pvpbot bot-management path distribute MyPath
```

### Geharten
```
/pvpbot bot-management path walk-type MyPath bhop    # Bunny hop (default)
/pvpbot bot-management path walk-type MyPath sprint  # Sprint
/pvpbot bot-management path walk-type MyPath walk    # Walk
```

### Loop-Modus
```
/pvpbot bot-management path loop MyPath true
/pvpbot bot-management path loop MyPath false
```

Im Loop-Modus kehren die Bots am Ende die Richtung um (Ping-Pong). Im Nicht-Loop-Modus starten Bots von vorne.

### Visualisierung
```
/pvpbot bot-management path show MyPath true
/pvpbot bot-management path show MyPath false
```

Zeigt Pfadwegpunkte als Partikel an (WAX_ON + grüne Staublinien).

## Fraktionspfade

Kontrolliere alle Bots einer Fraktion gleichzeitig:
```
/pvpbot faction path start RedFaction MyPath
/pvpbot faction path stop RedFaction
```

## Eigenschaften

| Eigentum | Optionen | Standard |
|----------|---------|---------|
| Gehtyp | bhop / sprint / gehen | bhop |
| Schleife | wahr / falsch | falsch |
| Angriff | wahr / falsch | wahr |
| Punkte | Variable (Vec3d) | - |
