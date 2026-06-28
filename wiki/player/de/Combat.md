# Kampfsystem

Die Kampf-KI verwaltet automatisch die Zielauswahl, den Waffenwechsel und taktische Entscheidungen.

## Waffenmodi

Bots wählen automatisch den besten Waffenmodus basierend auf Entfernung und verfügbaren Gegenständen aus:

| Modus | Waffe | Reichweite | Bedingungen |
|------|--------|-------|------------|
| Nahkampf | Schwert / Axt |`melee-range`(Standard 3.5) | Standardmodus |
| Fernkampf | Bogen / Armbrust |`ranged-min-range` - `ranged-optimal-range`| Erfordert Pfeile |
| MACE | Streitkolben |`mace-range`(Standard 6.0) |`mace`Einstellung aktiviert |
| SPEER | Speer |`spear-range` / `spear-charge-range` | `spear`Einstellung aktiviert |
| KRISTALL | Kristall-PVP | 2,5 - 8,0 | Siehe [Explosiver Kampf](ExplosiveCombat) |
| ANKER | Anker-PVP | 2,0 - 8,0 | Siehe [Explosiver Kampf](ExplosiveCombat) |

## Zielauswahl

Zielpriorität (wird bei jedem Tick bewertet):

1. **Erzwungenes Ziel** – Festlegen über`/pvpbot bot-management attack`
2. **Rache** – greift automatisch den letzten Schaden an (30 Sekunden Zeitüberschreitung)
3. **Fraktionsfeinde** – Feindliche Fraktionsmitglieder (sofern Fraktionen aktiviert)
4. **Automatisches Ziel** – Nächster gültiger Feind in Reichweite

Gültige Ziele können Spieler, feindliche Mobs und andere Bots sein (konfigurierbar über Einstellungen).

## Kampffunktionen

### Kritische Treffer
Bots springen, bevor sie angreifen, um kritische Treffer zu landen`criticals`ist aktiviert.

### Schildverwaltung
- Prognostiziert feindliche Angriffe basierend auf Sprintrichtung und Distanz
- Erhöht den Schild präventiv mithilfe des Vorhersagesystems
- Geringe Gesundheit löst das Halten des Schildes aus
- Schildflackern für Unvorhersehbarkeit

### Schild brechen
Wenn ein Feind blockt, brechen Bots ihn mit einer Axt und einem Schild mit konfigurierbarer Chance.

### Vorhersage feindlicher Angriffe
Verfolgt Positionsänderungen, Sprintzustand und Distanz des Gegners, um den Angriffszeitpunkt für die Schilderhebung vorherzusagen.

### Rückzugslogik
Wenn die Gesundheit unterschritten wird`retreat-health-percent`:
- Hebt den Schild und bewegt sich weg
- Verwendet Heiltränke, sofern verfügbar
- Platziert Spinnweben, um die Verfolgung zu verlangsamen
- Isst Nahrung, um sich zu regenerieren

### Streitkolbenverteidigung
Erkennt Feinde mit Streitkolben (in der Luft, fallend) und erhöht präventiv den Schild – gleicht den Vanilla-Schildfehler aus.

### Spinnennetz-Platzierung
Bots können Spinnweben auf Zielen platzieren, um sie bewegungsunfähig zu machen (verwendet Gegenstände aus dem Inventar).

## Einstellungsreferenz

| Einstellung | Standard | Beschreibung |
|---------|---------|-------------|
| Kampf | wahr | Kampf vollständig aktivieren/deaktivieren |
| Rache | wahr | Letzten Angreifer automatisch angreifen |
| Automatisches Ziel | falsch | Nächstes Ziel automatisch erfassen |
| Zielspieler | wahr | Echte Spieler ansprechen |
| Zielmobs | falsch | Nehmen Sie feindliche Mobs ins Visier |
| Ziel-Bots | falsch | Andere Bots ins Visier nehmen |
| Kritik | wahr | Verwende springende kritische Treffer |
| Angriffs-Abklingzeit | 10 | Ticks zwischen Anfällen |
| Nahkampfreichweite | 3,5 | Nahkampfangriffsreichweite |
| Fehlschlag | 0% | Chance, Angriffe zu verpassen |
| Fehler-Chance | 0% | Chance, Zielfehler zu machen |
| Angriff-unbesiegbar | falsch | Kreativ-/Zuschauerspieler angreifen |
| Zielgeschwindigkeit | 60 | Rotationsgeschwindigkeit (Grad/Sek.) |
| Sichtweite | 64 | Maximale Zielerfassungsreichweite |
