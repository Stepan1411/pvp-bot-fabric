# Navigation

Das Navigationssystem verwaltet die Bewegung des Bots mit Hinderniserkennung und sanfter Drehung.

## Bewegungsarten

### B-Hop (Standard)
- Springt automatisch während der Bewegung, um Hasen zu hüpfen
- Konfigurierbar über`bhop`Einstellung
- Wird automatisch bei einer Bewegungsgeschwindigkeit >= 1,0 verwendet

### Sprint
- Standard-Sprintbewegung
– Wird verwendet, wenn Bhop deaktiviert ist

### Gehen
- Kein Sprinten, langsamere Bewegung
- Einstellung über Pfad-Gehtyp

## Bewegungsfunktionen

| Funktion | Beschreibung |
|----------|-------------|
| `moveToward(bot, target, speed)`| Auf eine Entität zubewegen |
| `moveAway(bot, target, speed)`| Sich von einer Entität entfernen |
| `moveTowardPosition(bot, pos, speed)`| Zu einer Position bewegen |
| `moveTowardCombat(bot, pos, speed, strafe)`| Combat-Strafing-Bewegung |

## Glattes Aussehen

Rotation verwendet konfigurierbar`aim-speed`(Grad pro Tick) für eine reibungslose Verfolgung:
- `lookAt(bot, entity)`— Entität verfolgen
- `lookAtPosition(bot, pos)`— Position verfolgen
- `lookAway(bot, entity)`– Wegsehen (Zurückweichen)

## Hinderniserkennung

Der Bot erkennt und verarbeitet:

| Hindernis | Verhalten |
|----------|----------|
| **Feste Blöcke** | Springt zum Löschen |
| **Wände** | Ausweichen (Richtungswechsel vermeiden) |
| **Löcher** | Springt über |
| **Leitern / Ranken** | Steigt automatisch (Sprinten deaktiviert) |
| **Wasser** | Schwimmt mit Richtungskontrolle |

## Feststeckerkennung

Wenn der Bot sich am Boden für mehr als 10 Ticks weniger als 0,05 Blöcke bewegt:
1. Ändert die Ausweichrichtung
2. Springt, um zu entkommen
3. Versucht, Hindernissen auszuweichen

## Knockback-Handhabung

Wenn die horizontale Geschwindigkeit 0,35 (Rückstoß) überschreitet, hört der Bot auf zu sprinten und widersetzt sich der Bewegung bis zu 10 Ticks.

## Leerlauf

Im Leerlauf (kein Ziel) und`idle`Wenn die Einstellung aktiviert ist, wandern Bots zufällig darin umher`idle-radius`(Standard 10 Blöcke) von der Spawn-Position.

## W-Tippen

Nach dem Angriff lässt der Bot kurz den Sprint los (W-Tap), um die Rückstoßdistanz des Gegners zurückzusetzen.

## Combat Strafe

Im Kampfmodus bewegen sich Bots mit zufälliger Richtung nach links/rechts und wechseln alle 8–18 Ticks.

## Einstellungen

| Einstellung | Standard | Beschreibung |
|---------|---------|-------------|
| bhop | wahr | Hasenhüpfen aktivieren |
| untätig | falsch | Leerlauf aktivieren |
| Leerlaufradius | 10 | Wanderradius vom Spawn |
| Bewegungsgeschwindigkeit | 1,0 | Bewegungsgeschwindigkeitsmultiplikator |
| Zielgeschwindigkeit | 60 | Rotationsgeschwindigkeit (Grad/Sek.) |
