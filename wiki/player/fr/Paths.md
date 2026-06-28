# Système de chemin

Créez des chemins de points de cheminement que les robots pourront suivre. Les chemins sont enregistrés par monde dans`config/pvpbot/worlds/<worldname>/paths.json`.

## Commandes

### Création de chemins
```
/pvpbot bot-management path create MyPath
```

### Ajout de points de cheminement
Placez-vous à l'endroit souhaité et exécutez :
```
/pvpbot bot-management path add-point MyPath
```
La visualisation du chemin (particules) est automatiquement activée.

### Gestion des waypoints
```
/pvpbot bot-management path remove-point MyPath      # Remove last
/pvpbot bot-management path remove-point MyPath 0    # Remove by index
/pvpbot bot-management path clear MyPath             # Clear all points
/pvpbot bot-management path info MyPath              # List all points
/pvpbot bot-management path list                     # List all paths
```

### Suivre les chemins
```
/pvpbot bot-management path start Bot1 MyPath
/pvpbot bot-management path stop Bot1
/pvpbot bot-management path start-near MyPath 20     # Start for bots within 20 blocks
/pvpbot bot-management path stop-all MyPath          # Stop all bots on path
```

### Distribution
Espacez uniformément les robots le long des points de chemin :
```
/pvpbot bot-management path distribute MyPath
```

### Types de marche
```
/pvpbot bot-management path walk-type MyPath bhop    # Bunny hop (default)
/pvpbot bot-management path walk-type MyPath sprint  # Sprint
/pvpbot bot-management path walk-type MyPath walk    # Walk
```

### Mode boucle
```
/pvpbot bot-management path loop MyPath true
/pvpbot bot-management path loop MyPath false
```

En mode boucle, les robots inversent la direction à la fin (ping-pong). En mode sans boucle, les robots redémarrent depuis le début.

### Visualisation
```
/pvpbot bot-management path show MyPath true
/pvpbot bot-management path show MyPath false
```

Affiche les waypoints du chemin sous forme de particules (WAX_ON + lignes de poussière vertes).

## Chemins de faction

Contrôlez tous les robots d'une faction à la fois :
```
/pvpbot faction path start RedFaction MyPath
/pvpbot faction path stop RedFaction
```

## Propriétés

| Propriété | Options | Par défaut |
|----------|---------|---------|
| Type de marche | bhop / sprint / marche | bhop |
| Boucle | vrai / faux | faux |
| Attaque | vrai / faux | vrai |
| Points | variable (Vec3d) | - |
