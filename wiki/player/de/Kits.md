# 🎒 Kit-System

Speichern Sie Ausrüstungsvoreinstellungen und rüsten Sie Bots schnell aus!

---

## 📖 Übersicht

Mit Kits können Sie:
- Speichern Sie Ihren aktuellen Lagerbestand als Vorlage
- Geben Sie Bots schnell Ausrüstung
- Rüsten Sie ganze Fraktionen auf einmal aus

---

## 📦 Kits erstellen

1. Fügen Sie Gegenstände in Ihr Inventar ein (Rüstungen, Waffen, Lebensmittel usw.)
2. Führen Sie den Befehl create aus

```mcfunction
/pvpbot createkit <name>
```

### Was gespeichert wird
- ✅ Hotbar-Gegenstände (Slots 0-8)
- ✅ Inventargegenstände
- ✅ Rüstungsteile
- ✅ Nebenhandgegenstand
- ✅ Gegenstandsverzauberungen
- ✅ Haltbarkeit des Artikels
- ✅ Stapelgrößen

### Beispiel
```mcfunction
# Put diamond armor, sword, bow, arrows, golden apples in your inventory
# Then save it:
/pvpbot createkit pvp_warrior
```

---

## 📋 Kits verwalten

### Kits auflisten
```mcfunction
/pvpbot kits
```

### Kit löschen
```mcfunction
/pvpbot deletekit pvp_warrior
```

---

## 🎁 Spendenpakete

### An einen einzelnen Bot
```mcfunction
/pvpbot givekit Bot1 pvp_warrior
```

### An die gesamte Fraktion
```mcfunction
/pvpbot faction givekit RedTeam pvp_warrior
```

---

## 💡 Kit-Ideen

### ⚔️ Nahkämpfer
- Diamant-/Netherit-Schwert
- Vollständige Diamantrüstung
- Schild
- Goldene Äpfel
- Totem der Unvergänglichkeit (offhand)

### 🏹 Bogenschütze
- Bogen (Power V, Infinity)
- Pfeil (1 Stapel)
- Leder-/Kettenrüstung
- Goldene Äpfel

### 🔨 Panzer
- Netherit-Rüstung (Schutz IV)
- Schild
- Axt (zum Schildbrechen)
- Viele goldene Äpfel
- Mehrere Totems

### 💨 Speed ​​Fighter
- Leichte Rüstung (Leder/Kette)
- Diamantschwert (Schärfe V)
- Geschwindigkeitstränke
- Goldene Äpfel

---

## 📋 Vollständiges Beispiel

```mcfunction
# Step 1: Prepare your inventory with items you want

# Step 2: Create the kit
/pvpbot createkit soldier

# Step 3: Spawn bots
/pvpbot spawn Soldier1
/pvpbot spawn Soldier2
/pvpbot spawn Soldier3

# Step 4: Give kit to all bots
/pvpbot givekit Soldier1 soldier
/pvpbot givekit Soldier2 soldier
/pvpbot givekit Soldier3 soldier

# Or create a faction and give kit to all at once:
/pvpbot faction create Army
/pvpbot faction add Army Soldier1
/pvpbot faction add Army Soldier2
/pvpbot faction add Army Soldier3
/pvpbot faction givekit Army soldier
```

---

## 💾 Datenspeicherung

Kit-Daten werden gespeichert in:
```
config/pvp_bot_kits.json
```

Kits bleiben über Serverneustarts hinweg bestehen.

---

## ⚠️ Notizen

- Bots rüsten sich automatisch mit Rüstung aus dem Kit aus
- Bots wählen automatisch die beste Waffe aus
- Vorhandene Artikel im Bot-Inventar werden NICHT gelöscht
- Wenn das Bot-Inventar voll ist, werden einige Artikel möglicherweise nicht ausgegeben
