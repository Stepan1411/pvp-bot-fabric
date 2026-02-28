# 🛤️ Pfadsystem

Mit dem Pfadsystem können Sie vordefinierte Routen erstellen, denen Bots folgen können. Bots können Gebiete patrouillieren, sich zwischen Standorten bewegen und optional an Kämpfen teilnehmen, während sie Pfaden folgen.

---

## 📋 Inhaltsverzeichnis

- [Übersicht](#Übersicht)
- [Pfade erstellen](#creating-paths)
- [Wegpunkte verwalten](#managing-waypoints)
- [Bot-Kontrolle](#bot-control)
- [Pfadeinstellungen](#path-settings)
- [Visualisierung](#visualization)
- [Beispiele](#Beispiele)

---

## 🎯 Übersicht

Pfade sind Abfolgen von Wegpunkten, denen Bots folgen können. Jeder Pfad hat:
- **Name** – Eindeutige Kennung
- **Wegpunkte** - Liste der Positionen (x, y, z)
- **Loop-Modus** – Wie sich der Bot durch Wegpunkte bewegt
- **Angriffsmodus** – Legt fest, ob der Bot zum Kampf anhält
- **Visualisierung** – Partikeleffekte zeigen den Pfad

Pfade werden pro Welt in „config/pvpbot/worlds/{world}/paths.json“ gespeichert

---

## 🆕 Wege erstellen

### Erstellen Sie einen neuen Pfad
```
/pvpbot path create <name>
```
Erstellt einen leeren Pfad mit dem angegebenen Namen.

**Beispiel:**
```
/pvpbot path create patrol_route
```

### Einen Pfad löschen
```
/pvpbot path delete <name>
```
Entfernt den Pfad und stoppt alle Bots, die ihm folgen.

**Beispiel:**
```
/pvpbot path delete patrol_route
```

### Alle Pfade auflisten
```
/pvpbot path list
```
Zeigt alle verfügbaren Pfade in der aktuellen Welt an.

### Pfaddetails anzeigen
```
/pvpbot path info <name>
```
Zeigt Pfadinformationen an:
- Anzahl der Wegpunkte
- Status des Schleifenmodus
- Status des Angriffsmodus
- Liste aller Wegpunktkoordinaten

**Beispiel:**
```
/pvpbot path info patrol_route
```

---

## 📍 Wegpunkte verwalten

### Wegpunkt hinzufügen
```
/pvpbot path add <name>
```
Fügt Ihre aktuelle Position als neuen Wegpunkt zum Pfad hinzu.

**Beispiel:**
```
/pvpbot path add patrol_route
```
Stellen Sie sich an jeden Ort, den der Bot besuchen soll, und führen Sie diesen Befehl aus.

### Wegpunkt entfernen
```
/pvpbot path remove <name> <index>
```
Entfernt einen bestimmten Wegpunkt anhand seines Index (beginnend bei 0).

**Beispiel:**
```
/pvpbot path remove patrol_route 2
```
Entfernt den 3. Wegpunkt aus dem Pfad.

### Alle Wegpunkte löschen
```
/pvpbot path clear <name>
```
Entfernt alle Wegpunkte vom Pfad (behält den Pfad selbst).

**Beispiel:**
```
/pvpbot path clear patrol_route
```

---

## 🤖 Bot-Kontrolle

### Beginnen Sie, dem Pfad zu folgen
```
/pvpbot path follow <bot> <path>
```
Lässt einen Bot beginnen, dem angegebenen Pfad zu folgen.

**Beispiel:**
```
/pvpbot path follow Guard1 patrol_route
```

### Hören Sie auf, dem Pfad zu folgen
```
/pvpbot path stop <bot>
```
Verhindert, dass der Bot seinem aktuellen Pfad folgt.

**Beispiel:**
```
/pvpbot path stop Guard1
```

---

## ⚙️ Pfadeinstellungen

### Loop-Modus
```
/pvpbot path loop <name> <true/false>
```

Steuert, wie sich der Bot durch Wegpunkte bewegt:
- **false** (Standard) - Zirkular: 1→2→3→1→2→3...
- **wahr** - Hin und her: 1→2→3→2→1→2→3...

**Beispiel:**
```
/pvpbot path loop patrol_route true
```

### Angriffsmodus
```
/pvpbot path attack <name> <true/false>
```

Steuert das Kampfverhalten, während dem Pfad gefolgt wird:
- **true** (Standard) – Bot stoppt am aktuellen Wegpunkt, um zu kämpfen, und fährt dann fort
– **false** – Bot ignoriert den Kampf und bewegt sich weiter (BotCombat deaktiviert)

**Beispiel:**
```
/pvpbot path attack patrol_route false
```

---

## 👁️ Visualisierung

### Pfadanzeige umschalten
```
/pvpbot path show <name> <true/false>
```

Zeigt/versteckt Partikeleffekte für den Pfad:
- **Wegpunkte** – Wachspartikel an jedem Punkt
- **Linien** – Verbindungspunkte grüner Staubpartikel

Die Visualisierung wird automatisch aktiviert, wenn:
- Erstellen eines Pfades
- Einen Wegpunkt hinzufügen
- Ich beginne, einem Weg zu folgen

**Beispiel:**
```
/pvpbot path show patrol_route true
```

So deaktivieren Sie die Visualisierung:
```
/pvpbot path show patrol_route false
```

---

## 💡 Beispiele

### Grundlegende Patrouillenroute
```
# Create path
/pvpbot path create base_patrol

# Add waypoints (stand at each location)
/pvpbot path add base_patrol  # Point 1
/pvpbot path add base_patrol  # Point 2
/pvpbot path add base_patrol  # Point 3
/pvpbot path add base_patrol  # Point 4

# Make bot follow
/pvpbot path follow Guard1 base_patrol
```

### Wache mit Kampf
```
# Create path
/pvpbot path create guard_post

# Add waypoints
/pvpbot path add guard_post  # Position 1
/pvpbot path add guard_post  # Position 2

# Enable back-and-forth movement
/pvpbot path loop guard_post true

# Enable combat (default, but explicit)
/pvpbot path attack guard_post true

# Assign bot
/pvpbot path follow Guard1 guard_post
```

### Friedlicher Kurier
```
# Create path
/pvpbot path create delivery_route

# Add waypoints
/pvpbot path add delivery_route  # Start
/pvpbot path add delivery_route  # Checkpoint 1
/pvpbot path add delivery_route  # Checkpoint 2
/pvpbot path add delivery_route  # End

# Disable combat (bot won't fight)
/pvpbot path attack delivery_route false

# Assign bot
/pvpbot path follow Courier1 delivery_route
```

### Mehrere Bots auf demselben Pfad
```
# Create path
/pvpbot path create wall_patrol

# Add waypoints
/pvpbot path add wall_patrol  # Corner 1
/pvpbot path add wall_patrol  # Corner 2
/pvpbot path add wall_patrol  # Corner 3
/pvpbot path add wall_patrol  # Corner 4

# Assign multiple bots
/pvpbot path follow Guard1 wall_patrol
/pvpbot path follow Guard2 wall_patrol
/pvpbot path follow Guard3 wall_patrol
```

---

## 📝 Notizen

- Pfade werden bei Änderung automatisch gespeichert
- Jede Welt hat ihre eigenen Wege
- Bots schauen während der Bewegung auf den nächsten Wegpunkt
- Wenn der Angriffsmodus aktiviert ist, kehren Bots nach dem Kampf zu dem Wegpunkt zurück, zu dem sie unterwegs waren
- Die Pfadvisualisierung ist für alle Spieler sichtbar
- Bots erreichen einen Wegpunkt, wenn sie sich innerhalb von 1,5 Blocks davon befinden

---

## 🔗 Verwandte Seiten

- [Befehle](Commands.md) – Alle verfügbaren Befehle
- [Navigation](Navigation.md) – Bot-Bewegungseinstellungen
- [Combat](Combat.md) – Details zum Kampfsystem
