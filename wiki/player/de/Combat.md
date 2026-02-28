# ⚔️ Kampfsystem

PVP Bot verfügt über eine fortschrittliche Kampf-KI, die verschiedene Waffen und Taktiken einsetzen kann.

---

## 🗡️ Waffentypen

### Nahkampf
- **Schwerter** – Schnelle Angriffe, guter Schaden
- **Äxte** – Langsamer, kann aber Schilde brechen
- Bots wechseln automatisch in den Nahkampf, wenn Feinde in der Nähe sind

### Fernkampf
- **Bögen** – Pfeile ziehen und loslassen
- **Armbrüste** – Bolzen laden und abfeuern
- Bots halten optimalen Abstand (8-20 Blöcke)

### Streitkolbenkampf
- **Streitkolben + Windladung** – Sprungangriffe für massiven Schaden
- Bots nutzen Windladungen, um in die Luft zu fliegen
- Verheerende Sturzangriffe

---

## 🎯 Targeting

### Rachemodus
Wenn ein Bot Schaden erleidet, zielt er automatisch auf den Angreifer.
```mcfunction
/pvpbot settings revenge true
```

### Automatisches Ziel
Bots suchen automatisch nach Feinden in Sichtweite.
```mcfunction
/pvpbot settings autotarget true
```

### Manuelles Ziel
Zwingen Sie einen Bot, ein bestimmtes Ziel anzugreifen.
```mcfunction
/pvpbot attack BotName TargetName
```

### Zielfilter
Wählen Sie aus, worauf Bots abzielen können:
```mcfunction
/pvpbot settings targetplayers true   # Target players
/pvpbot settings targetmobs true      # Target hostile mobs
/pvpbot settings targetbots true      # Target other bots
```

---

## 🛡️ Verteidigung

### Auto-Shield
Bots erhöhen automatisch Schilde, wenn Feinde angreifen.
```mcfunction
/pvpbot settings autoshield true
```

### Schild brechen
Bots verwenden Äxte, um feindliche Schilde zu deaktivieren.
```mcfunction
/pvpbot settings shieldbreak true
```

### Auto-Totem
Bots behalten Totems der Unsterblichen in der Nebenhand.
```mcfunction
/pvpbot settings autototem true
```

---

## 🍎 Heilung

### Auto-Essen
Bots fressen Nahrung, wenn:
- Gesundheit ist niedrig (< 30 %)
- Der Hunger liegt unter der Schwelle

```mcfunction
/pvpbot settings autoeat true
/pvpbot settings minhunger 14
```

### Auto-Tränke
Bots verwenden automatisch Tränke:
- **Heiltränke** – wenn die HP niedrig sind (Spritzer oder trinkbar)
- **Stärketränke** – beim Eintritt in den Kampf
- **Geschwindigkeitstränke** – beim Eintritt in den Kampf
- **Feuerwiderstandstränke** – beim Eintritt in den Kampf

Alle Buff-Tränke werden auf einmal gewürfelt, wenn der Kampf beginnt. Bots wenden Buffs erneut an, wenn die Effekte ablaufen (< 5 Sekunden verbleibend).

```mcfunction
/pvpbot settings autopotion true
```

### Rückzug
Wenn die Gesundheit niedrig ist, ziehen sich Bots zurück, während sie fressen/heilen.
Der Rückzug ist deaktiviert, wenn der Bot keine Nahrung hat (Kämpfe bis zum Tod).

```mcfunction
/pvpbot settings retreat true
/pvpbot settings retreathp 0.3  # 30% HP
```

---

## 💥 Kritische Treffer

Bots können kritische Treffer ausführen, indem sie ihre Angriffe mit Sprüngen zeitlich festlegen.
```mcfunction
/pvpbot settings criticals true
```

---

## 🕸️ Spinnennetz-Taktik

Bots können Spinnweben strategisch nutzen:
- **Beim Rückzug** – platziert ein Spinnennetz unter dem verfolgenden Feind, um ihn zu verlangsamen
- **Im Nahkampf** – platziert ein Spinnennetz unter dem angreifenden Feind

```mcfunction
/pvpbot settings cobweb true
```

---

## ⚙️ Kampfeinstellungen

| Einstellung | Reichweite | Standard | Beschreibung |
|---------|-------|---------|-------------|
| „Kampf“ | wahr/falsch | wahr | Kampf aktivieren |
| „Rache“ | wahr/falsch | wahr | Greife an, wer dich angegriffen hat |
| `autotarget` | wahr/falsch | falsch | Feinde automatisch finden |
| „Kritisch“ | wahr/falsch | wahr | Kritische Treffer |
| „Fernkampf“ | wahr/falsch | wahr | Benutze Bögen |
| „Streitkolben“ | wahr/falsch | wahr | Benutze Streitkolben |
| `Speer` | wahr/falsch | falsch | Verwenden Sie Speer (Buggy) |
| „Autopotion“ | wahr/falsch | wahr | Tränke automatisch verwenden |
| „Spinnennetz“ | wahr/falsch | wahr | Verwenden Sie Spinnweben |
| „Rückzug“ | wahr/falsch | wahr | Rückzug bei niedrigen HP |
| `Rückzug` | 0,1-0,9 | 0,3 | HP % zum Rückzug |
| `attackcooldown` | 1-40 | 10 | Ticks zwischen Anfällen |
| „Nahkampf“ | 2-6 | 3,5 | Nahkampfangriffsdistanz |
| `viewdistance` | 5-128 | 64 | Zielsuchbereich |
