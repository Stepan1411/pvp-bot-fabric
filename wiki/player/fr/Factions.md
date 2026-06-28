# Factions

Organisez les robots en équipes/factions pour des batailles coordonnées. Les factions sont enregistrées par monde dans`config/pvpbot/worlds/<worldname>/factions.json`.

## Commandes de base

### Créer et supprimer
```
/pvpbot faction create Red
/pvpbot faction delete Red
```

### Gérer les membres
```
/pvpbot faction add Red Bot1
/pvpbot faction remove Red Bot1
/pvpbot faction add-near Red 30          # Add all bots within 30 blocks
/pvpbot faction add-all Red              # Add all existing bots
```

### Vérifier les informations
```
/pvpbot faction list              # List all factions
/pvpbot faction info Red          # Show members and enemies
```

## Relations hostiles

Définissez les factions comme ennemis. Les robots attaqueront automatiquement les membres de la faction ennemie.

```
/pvpbot faction hostile Red Blue
/pvpbot faction hostile Red Neutral false
```

**Remarque :** Les ennemis sont toujours mutuels : rendre Rouge hostile à Bleu rend également Bleu hostile à Rouge.

## Actions coordonnées

### Attaque
Tous les membres de la faction attaquent une cible :
```
/pvpbot faction attack Red GreenPlayer
```

### Donner des objets
Donnez des objets à tous les membres de la faction :
```
/pvpbot faction give Red diamond_sword 1
```

### Donner un kit
Équipez tous les membres d’un kit :
```
/pvpbot faction kit give-kit Red MyKit
```

### Donner un kit aléatoire
Équipez les membres de la faction avec un kit pondéré aléatoirement :
```
/pvpbot faction kit give-kit-random Red warrior 60% archer 30% mage 10%
```

Chaque membre reçoit un kit en fonction de la répartition du poids.

### Téléportation
Téléportez progressivement toute la faction (5 robots toutes les 100 ms) :
```
/pvpbot faction tp Red 100 64 -200
/pvpbot faction tp Red ~ ~ ~
/pvpbot faction tp Red PlayerName
```

Prend en charge les coordonnées absolues, relatives`~`, ou cibler un joueur/bot par son nom.

### Suivi du chemin
Tous les membres suivent un chemin :
```
/pvpbot faction path start Red MyPath
/pvpbot faction path stop Red
```

## Paramètres

| Paramètre | Par défaut | Descriptif |
|---------|---------|-------------|
| factions | vrai | Activer le système de faction |
| tir ami | faux | Autoriser l'attaque des membres de votre propre faction |
