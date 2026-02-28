# ⚙️ Einstellungen

Vollständige Liste aller Konfigurationsoptionen.

---

## 📋 Befehle

```mcfunction
# Show all settings
/pvpbot settings

# Show specific setting
/pvpbot settings <name>

# Change setting
/pvpbot settings <name> <value>
```

---

## ⚔️ Kampfeinstellungen

| Einstellung | Geben Sie | ein Reichweite | Standard | Beschreibung |
|---------|------|-------|---------|-------------|
| „Kampf“ | bool | - | wahr | Kampfsystem aktivieren/deaktivieren |
| „Rache“ | bool | - | wahr | Angriffseinheiten, die dem Bot Schaden zufügen |
| `autotarget` | bool | - | falsch | Automatisch nach Feinden suchen |
| `Zielspieler` | bool | - | wahr | Kann auf Spieler abzielen |
| `targetmobs` | bool | - | falsch | Kann feindliche Mobs angreifen |
| `targetbots` | bool | - | falsch | Kann andere Bots angreifen |
| „Kritisch“ | bool | - | wahr | Kritische Treffer ausführen |
| „Fernkampf“ | bool | - | wahr | Benutze Bögen/Armbrüste |
| „Streitkolben“ | bool | - | wahr | Benutze Streitkolben mit Windladungen |
| `Speer` | bool | - | falsch | Speer verwenden (wegen Teppichfehler deaktiviert) |
| `attackcooldown` | int | 1-40 | 10 | Ticks zwischen Anfällen |
| „Nahkampf“ | doppelt | 2-6 | 3,5 | Nahkampfangriffsdistanz |
| „Bewegungsgeschwindigkeit“ | doppelt | 0,1-2,0 | 1,0 | Bewegungsgeschwindigkeitsmultiplikator |
| `viewdistance` | doppelt | 5-128 | 64 | Maximale Zielerfassungsreichweite |
| „Rückzug“ | bool | - | wahr | Rückzug bei niedrigen HP |
| `Rückzug` | doppelt | 0,1-0,9 | 0,3 | HP-Prozent zum Beginn des Rückzugs (30 %) |

---

## 🧪 Trankeinstellungen

| Einstellung | Geben Sie | ein Reichweite | Standard | Beschreibung |
|---------|------|-------|---------|-------------|
| „Autopotion“ | bool | - | wahr | Heil-/Buff-Tränke automatisch verwenden |
| „Spinnennetz“ | bool | - | wahr | Verwenden Sie Spinnweben, um Feinde zu verlangsamen |

Bots verwenden automatisch:
- **Heiltränke**, wenn die HP niedrig sind
- **Stärketränke** beim Eintritt in den Kampf
- **Geschwindigkeitstränke** beim Eintritt in den Kampf
- **Feuerwiderstandstränke** beim Eintritt in den Kampf
- **Spinnenweben**, um Feinde zu verlangsamen (beim Rückzug oder beim Angriff des Feindes)

Alle Buff-Tränke werden auf einmal ausgegeben, wenn der Kampf beginnt oder wenn die Effekte ablaufen.

---

## 🚶 Navigationseinstellungen

| Einstellung | Geben Sie | ein Reichweite | Standard | Beschreibung |
|---------|------|-------|---------|-------------|
| `bhop` | bool | - | wahr | Bunny Hop aktivieren |
| `bhopcooldown` | int | 5-30 | 12 | Ticks zwischen Bhop-Sprüngen |
| `jumpboost` | doppelt | 0,0-0,5 | 0,0 | Zusätzliche Sprunghöhe |
| „untätig“ | bool | - | wahr | Wandern, wenn kein Ziel |
| `Leerlaufradius` | doppelt | 3-50 | 10 | Leerlaufradius |

---

## 🛡️ Geräteeinstellungen

| Einstellung | Geben Sie | ein Reichweite | Standard | Beschreibung |
|---------|------|-------|---------|-------------|
| `Autopanzerung` | bool | - | wahr | Beste Rüstung automatisch ausrüsten |
| „Autoweapon“ | bool | - | wahr | Beste Waffe automatisch ausrüsten |
| `autototem` | bool | - | wahr | Totem automatisch in der Nebenhand ausrüsten |
| `autoshield` | bool | - | wahr | Schild beim Blockieren automatisch verwenden |
| `prefersword` | bool | - | wahr | Lieber Schwert als Axt |
| `Schildbruch` | bool | - | wahr | Wechseln Sie zur Axt, um den gegnerischen Schild zu durchbrechen |
| `Droparmor` | bool | - | falsch | Schlechtere Rüstungsteile fallen lassen |
| `Dropweapon` | bool | - | falsch | Schlimmere Waffen fallen lassen |
| `dropdistance` | doppelt | 1-10 | 3,0 | Artikelabholentfernung |
| `Intervall` | int | 1-100 | 20 | Geräteprüfintervall (Ticks) |
| `minarmorlevel` | int | 0-100 | 0 | Mindestrüstungsstufe zum Ausrüsten |

### Rüstungsstufen
| Ebene | Rüstungstyp |
|-------|------------|
| 0 | Jede Rüstung |
| 20 | Leder+ |
| 40 | Gold+ |
| 50 | Kette+ |
| 60 | Eisen+ |
| 80 | Diamant+ |
| 100 | Nur Netherit |

---

## 🎭 Realismus-Einstellungen

| Einstellung | Geben Sie | ein Reichweite | Standard | Beschreibung |
|---------|------|-------|---------|-------------|
| „missance“ | int | 0-100 | 10 | Chance, Angriffe zu verpassen (%) |
| `FehlerChance` | int | 0-100 | 5 | Chance, in die falsche Richtung anzugreifen (%) |
| `Reaktionsverzögerung` | int | 0-20 | 0 | Verzögerung bis zur Reaktion (Ticks) |

---

## 👥 Andere Einstellungen

| Einstellung | Geben Sie | ein Reichweite | Standard | Beschreibung |
|---------|------|-------|---------|-------------|
| „Fraktionen“ | bool | - | wahr | Fraktionssystem aktivieren |

---

## 💾 Konfigurationsdateien

Einstellungen werden gespeichert in:
```
config/pvp_bot.json
```

Bot-Daten (Positionen, Abmessungen, Spielmodi) werden gespeichert in:
```
config/pvp_bot_bots.json
```

Sowohl die Einstellungen als auch die Bots bleiben bei Serverneustarts bestehen. Bots werden automatisch wiederhergestellt, wenn der Server startet.

---

## 📋 Beispiele

### Machen Sie Bots realistischer
```mcfunction
/pvpbot settings misschance 15
/pvpbot settings mistakechance 10
/pvpbot settings reactiondelay 5
```

### Machen Sie Bots aggressiv
```mcfunction
/pvpbot settings autotarget true
/pvpbot settings targetplayers true
/pvpbot settings targetbots true
/pvpbot settings revenge true
```

### Fernkampf deaktivieren
```mcfunction
/pvpbot settings ranged false
/pvpbot settings mace false
```

### Schnelle Bewegung
```mcfunction
/pvpbot settings bhop true
/pvpbot settings bhopcooldown 8
/pvpbot settings jumpboost 0.2
/pvpbot settings movespeed 1.5
```

### Stationäre Wachen
```mcfunction
/pvpbot settings idle false
/pvpbot settings bhop false
```
