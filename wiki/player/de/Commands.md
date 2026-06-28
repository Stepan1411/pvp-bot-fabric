# Befehle

Alle Befehle verwenden die`/pvpbot`Präfix.

## Bot-Management

| Befehl | Beschreibung |
|---------|-------------|
| `/pvpbot spawn [name]`| Einen Bot erzeugen (zufälliger Name, falls weggelassen) |
| `/pvpbot remove <name>`| Einen bestimmten Bot entfernen |
| `/pvpbot removeall`| Alle Bots entfernen |
| `/pvpbot reload`| Alle Konfigurationen (Einstellungen, Kits, Pfade, Bots) neu laden |
| `/pvpbot bot-management list`| Alle aktiven Bots auflisten |
| `/pvpbot bot-management inventory <botname>`| Bot-Inventar und Statistiken anzeigen |
| `/pvpbot bot-management mass-spawn <1-50>`| Mehrere Bots erzeugen |

## Kampfkontrolle

| Befehl | Beschreibung |
|---------|-------------|
| `/pvpbot bot-management attack <botname> <target>`| Bot zwingen, ein Ziel anzugreifen |
| `/pvpbot bot-management stop-attack <botname>`| Hör auf anzugreifen |

## Einstellungen

| Befehl | Beschreibung |
|---------|-------------|
| `/pvpbot settings`| Alle aktuellen Einstellungen auflisten |
| `/pvpbot settings <name>`| Eine bestimmte Einstellung anzeigen |
| `/pvpbot settings <name> <value>`| Legen Sie einen Einstellwert fest |

Alle verfügbaren Optionen finden Sie unter [Einstellungen](Einstellungen).

## Pfade

| Befehl | Beschreibung |
|---------|-------------|
| `/pvpbot bot-management path create <name>`| Erstellen Sie einen neuen Pfad |
| `/pvpbot bot-management path delete <name>`| Einen Pfad löschen |
| `/pvpbot bot-management path add-point <name>`| Aktuelle Position als Wegpunkt hinzufügen |
| `/pvpbot bot-management path remove-point <name> [index]`| Punkt entfernen (letzter oder nach Index) |
| `/pvpbot bot-management path clear <name>`| Alle Punkte löschen |
| `/pvpbot bot-management path loop <name> <true/false>`| Schleife umschalten |
| `/pvpbot bot-management path start <bot> <path>`| Starten Sie den Bot und folgen Sie dem Pfad |
| `/pvpbot bot-management path stop <bot>`| Bot stoppen, dem Pfad zu folgen |
| `/pvpbot bot-management path list`| Alle Pfade auflisten |
| `/pvpbot bot-management path show <name> <true/false>`| Pfadvisualisierung umschalten |
| `/pvpbot bot-management path info <name>`| Pfaddetails anzeigen |
| `/pvpbot bot-management path distribute <path>`| Bots gleichmäßig entlang des Pfades verteilen |
| `/pvpbot bot-management path start-near <path> <radius>`| Startpfad für Bots in der Nähe |
| `/pvpbot bot-management path stop-all <path>`| Stoppen Sie alle Bots auf Pfad |
| `/pvpbot bot-management path walk-type <name> <type>`| Lauftyp einstellen (Bhop/Sprint/Walk) |

Ausführliche Informationen zur Verwendung finden Sie unter [Pfade](Pfade).

## Bausätze

| Befehl | Beschreibung |
|---------|-------------|
| `/pvpbot kit create-kit <name>`| Speichern Sie Ihr Inventar als Bausatz |
| `/pvpbot kit delete-kit <name>`| Ein Kit löschen |
| `/pvpbot kit give-kit <player> <kitname>`| Gib dem Spieler/Bot das Kit |
| `/pvpbot kit kits`| Alle Kits auflisten |
| `/pvpbot kit give-kit-near <kitname> [radius]`| Kit an Bots im Umkreis weitergeben (Standard: 10) |
| `/pvpbot kit give-kit-near-random <radius> <kit1> <w1>% [<kit2> <w2>% ...]`| Geben Sie Bots im Umkreis ein zufällig gewichtetes Kit |

Detaillierte Informationen zur Verwendung finden Sie unter [Kits](Kits).

## Fraktionen

| Befehl | Beschreibung |
|---------|-------------|
| `/pvpbot faction list`| Alle Fraktionen auflisten |
| `/pvpbot faction create <name>`| Erstelle eine Fraktion |
| `/pvpbot faction delete <name>`| Eine Fraktion löschen |
| `/pvpbot faction add <faction> <player>`| Spieler/Bot zur Fraktion hinzufügen |
| `/pvpbot faction remove <faction> <player>`| Spieler/Bot aus Fraktion entfernen |
| `/pvpbot faction hostile <f1> <f2> [true/false]`| Feindliche Beziehungen einstellen |
| `/pvpbot faction info <name>`| Fraktionsinfo anzeigen |
| `/pvpbot faction add-near <faction> <radius>`| Bots in der Nähe zur Fraktion hinzufügen |
| `/pvpbot faction add-all <faction>`| Alle Bots zur Fraktion hinzufügen |
| `/pvpbot faction give <faction> <item>`| Gegenstände an alle Mitglieder verschenken |
| `/pvpbot faction attack <faction> <target>`| Alle Mitglieder greifen Ziel an |
| `/pvpbot faction path start <faction> <path>`| Alle Mitglieder folgen dem Pfad |
| `/pvpbot faction path stop <faction>`| Stoppen Sie alle Mitglieder auf Pfad |
| `/pvpbot faction tp <faction> <x y z\|player>`| Ganze Fraktion nach und nach teleportieren |

### Fraktionskit-Befehle

| Befehl | Beschreibung |
|---------|-------------|
| `/pvpbot faction kit give-kit <faction> <kitname>`| Kit an alle Mitglieder weitergeben |
| `/pvpbot faction kit give-kit-random <faction> <kit1> <w1>% [<kit2> <w2>% ...]`| Geben Sie den Fraktionsmitgliedern ein zufällig gewichtetes Kit |

Weitere Informationen zur Verwendung finden Sie unter [Fraktionen](Fraktionen).
