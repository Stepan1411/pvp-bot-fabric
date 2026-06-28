# Einstellungen

Alle Einstellungen werden pro Welt gespeichert`config/pvpbot/worlds/<worldname>/settings.json`.

## Anzeigen und Ändern

```
/pvpbot settings                # List all settings
/pvpbot settings combat         # View a single setting
/pvpbot settings combat false   # Change a setting
```

## Geräteeinstellungen

| Einstellung | Geben Sie | ein Standard | Reichweite | Beschreibung |
|---------|------|---------|-------|-------------|
| automatische Panzerung | bool | wahr | - | Rüste die beste Rüstung aus dem Inventar aus |
| automatische Waffe | bool | wahr | - | Rüste Hotbar mit der besten Waffe aus |
| Drop-Rüstung | bool | falsch | - | Schlechtere Rüstung beim Ersetzen fallen lassen |
| Drop-Waffe | bool | falsch | - | Schlechtere Waffen beim Ersetzen fallen lassen |
| Fallhöhe | doppelt | 3,0 | 1,0 - 10,0 | Entfernung zum Ablegen von Gegenständen |
| Intervall | int | 20 | 1 - 100 | Geräteprüfintervall (Ticks) |

## Kampfeinstellungen

| Einstellung | Geben Sie | ein Standard | Reichweite | Beschreibung |
|---------|------|---------|-------|-------------|
| Kampf | bool | wahr | - | Kampfsystem aktivieren |
| Rache | bool | wahr | - | Letzter Schaden automatisch angreifen |
| Automatisches Ziel | bool | falsch | - | Nächstes Ziel automatisch erfassen |
| Zielspieler | bool | wahr | - | Echte Spieler ansprechen |
| Zielmobs | bool | falsch | - | Nehmen Sie feindliche Mobs ins Visier |
| Ziel-Bots | bool | falsch | - | Andere Bots ins Visier nehmen |
| Sichtweite | doppelt | 64,0 | 5,0 - 128,0 | Maximaler Zielsuchbereich |
| Angriff-unbesiegbar | bool | falsch | - | Kreativ-/Zuschauerspieler angreifen |
| Angriffs-Abklingzeit | int | 10 | 1 - 40 | Ticks zwischen Anfällen |
| Kritik | bool | wahr | - | Springe für kritische Treffer |
| kritische-fall-ticks | int | 6 | 1 - 10 | Fallende Ticks für Krit erforderlich |
| Nahkampfreichweite | doppelt | 3,5 | 2,0 - 6,0 | Nahkampfreichweite |
| Bewegungsgeschwindigkeit | doppelt | 1,0 | 0,1 - 2,0 | Bewegungsgeschwindigkeitsmultiplikator |
| Fehlschlag | int | 0 | 0 - 100 | % Chance, Angriffe zu verpassen |
| Fehler-Chance | int | 0 | 0 - 100 | % Wahrscheinlichkeit, falsch zu zielen |
| Zielgeschwindigkeit | doppelt | 90,0 | 3,0 - 90,0 | Rotationsgeschwindigkeit (Grad/Sek.) |

## Waffeneinstellungen

| Einstellung | Geben Sie | ein Standard | Reichweite | Beschreibung |
|---------|------|---------|-------|-------------|
| Fernkampf | bool | wahr | - | Bogen-/Armbrustkampf aktivieren |
| Streitkolben | bool | wahr | - | Streitkolbenkampf aktivieren |
| Speer | bool | falsch | - | Speerkampf aktivieren |
| crystalpvp | bool | wahr | - | Kristall-PVP aktivieren |
| Ankerpvp | bool | wahr | - | Anker-PVP aktivieren |
| lieber-Schwert | bool | wahr | - | Bevorzugen Sie Schwerter gegenüber Äxten |
| Schildstreitkolben | bool | wahr | - | Automatischer Schutz gegen Streitkolbenangriffe |
| Sondernamen | bool | falsch | - | Liste spezieller Namen verwenden |
| Streitkolben-Bereich | doppelt | 6,0 | 3,0 - 10,0 | Streitkolben-Angriffsreichweite |
| Speerreichweite | doppelt | 4,5 | 2,0 - 8,0 | Speerangriffsreichweite |
| Speerladungsreichweite | doppelt | 12,0 | 5,0 - 20,0 | Startreichweite der Speerladung |

### Fernkampf

| Einstellung | Geben Sie | ein Standard | Reichweite | Beschreibung |
|---------|------|---------|-------|-------------|
| Bow-Draw-Ticks | int | 40 | 5 - 100 | Zeit zum vollständigen Ausziehen des Bogens (Ticks) |
| ranged-min-range | doppelt | 20,0 | 3,0 - 20,0 | Mindesteingriffsabstand des Bogens |
| ranged-optimal-range | doppelt | 40,0 | 10,0 - 50,0 | Idealer Bogenabstand |
| Fernkampf-maximale Reichweite | doppelt | 60,0 | 15,0 - 100,0 | Maximaler Bogeneingriffsbereich |
| Pfeilvorhersage | bool | wahr | - | Vorausschauender Bogen, der auf sich bewegende Ziele zielt |
| Fernkampf | bool | wahr | - | Seitwärts schiessen, während man mit dem Bogen schießt |
| Fernkampf-Rückzug | bool | wahr | - | Rückzug, wenn sich das Ziel nähert |

## Dienstprogrammeinstellungen

| Einstellung | Geben Sie | ein Standard | Reichweite | Beschreibung |
|---------|------|---------|-------|-------------|
| Auto-Totem | bool | wahr | - | Totem der Unsterblichen automatisch ausrüsten |
| Totem-Priorität | bool | wahr | - | Halte das Totem in der Nebenhand über dem Schild |
| automatisch essen | bool | wahr | - | Automatisches Essen bei Hunger |
| Auto-Schild | bool | wahr | - | Schild bei Bedarf automatisch anheben |
| Auto-Trank | bool | wahr | - | Benutze Heiltöpfe bei geringer Gesundheit |
| automatisch reparieren | bool | wahr | - | Verwenden Sie XP-Flaschen, um Ausrüstung zu reparieren |
| Schildbruch | bool | wahr | - | Axt-Schild-Brecher blockiert Feinde |
| Spinnennetz | bool | wahr | - | Spinnweben auf Zielscheiben platzieren |
| Min-Hunger | int | 14 | 1 - 20 | Hungerlevel, um mit dem Essen zu beginnen |
| Reparaturschwelle | doppelt | 0,25 | 0,1 - 0,9 | Haltbarkeit % zum Auslösen der Reparatur |
| Schildgesundheit | doppelt | 0,5 | 0,1 - 1,0 | Gesundheit % zum Halten des Schildes |

## Rückzugseinstellungen

| Einstellung | Geben Sie | ein Standard | Reichweite | Beschreibung |
|---------|------|---------|-------|-------------|
| Rückzug | bool | wahr | - | Rückzugsverhalten aktivieren |
| Retreat-Gesundheit | doppelt | 0,3 | 0,1 - 0,9 | Gesundheit % beginnt mit dem Rückzug |
| kritische Gesundheit | doppelt | 0,15 | 0,05 - 0,5 | Gesundheit % für kritischen Rückzug |

## Schildeinstellungen

| Einstellung | Geben Sie | ein Standard | Reichweite | Beschreibung |
|---------|------|---------|-------|-------------|
| Schild-Hold-Ticks | int | 60 | 10 - 200 | Zecken, um den Schild hochzuhalten |
| Shield-Raise-Ticks | int | 12 | 2 - 40 | Ticks, um den Schild vor dem vorhergesagten Treffer anzuheben |
| Schildbruch-Chance | int | 40 | 0 - 100 | % Chance, den gegnerischen Schild pro Treffer zu durchbrechen |

## Navigationseinstellungen

| Einstellung | Geben Sie | ein Standard | Reichweite | Beschreibung |
|---------|------|---------|-------|-------------|
| bhop | bool | wahr | - | Hasenhüpfen aktivieren |
| untätig | bool | falsch | - | Leerlauf, wenn kein Ziel vorhanden ist |
| Leerlaufradius | doppelt | 10,0 | 3,0 - 50,0 | Leerlaufradius |

## Fraktionseinstellungen

| Einstellung | Geben Sie | ein Standard | Reichweite | Beschreibung |
|---------|------|---------|-------|-------------|
| Fraktionen | bool | wahr | - | Fraktionssystem aktivieren |
| Friendly-Fire | bool | falsch | - | Angreifende Verbündete zulassen |

## Sonstige Einstellungen

| Einstellung | Geben Sie | ein Standard | Reichweite | Beschreibung |
|---------|------|---------|-------|-------------|
| bot-verlassen-bei-tod | bool | wahr | - | Bot bei Tod entfernen |
| Angriff-unbesiegbar | bool | falsch | - | Angriff im Creative/Zuschauer |
| Bots-Relogs | bool | wahr | - | Bots beim Serverneustart wiederherstellen |
| sicherer Spawn | bool | wahr | - | Zufälliger Offset (±0,1–0,5 Blöcke) beim Spawnen, um Erstickung zu verhindern |
| Clear-on-Remove | bool | wahr | - | Löschen Sie das Inventar, bevor Sie den Bot entfernen |
| Profil-Lag-Fix | bool | wahr | - | Profil-Cache vorab füllen, um Verzögerungen beim Bot-Spawn zu verhindern |
| max-mass-spawn | int | 1000 | 50 - 10000 | Maximal zulässige Bots pro Massenspawn-Befehl |
