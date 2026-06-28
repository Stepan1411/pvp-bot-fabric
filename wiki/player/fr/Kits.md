# Kits

Enregistrez et chargez les préréglages d’équipement pour les robots. Les kits sont enregistrés globalement dans`config/pvpbot/kits.json`.

## Création de kits

1. Équipez votre personnage des objets souhaités
2. Exécutez la commande create :

```
/pvpbot kit create-kit warrior
/pvpbot kit create-kit archer
```

Les kits enregistrent les 41 emplacements d'inventaire (barre de raccourci, inventaire principal, armure, main secondaire) sous forme de données NBT.

## Application des kits

### À un seul bot ou joueur
```
/pvpbot kit give-kit Bot1 warrior
```

### Aux robots dans un rayon
Donnez un kit à tous les robots dans un rayon (par défaut : 10) :
```
/pvpbot kit give-kit-near warrior 15
```

### Kit pondéré aléatoirement pour les robots dans un rayon
Donnez un kit aléatoire basé sur des poids aux robots dans un rayon :
```
/pvpbot kit give-kit-near-random 15 warrior 60% archer 30% mage 10%
```

Chaque robot dans un rayon de 15 blocs reçoit un kit en fonction de la répartition du poids.

### À une faction entière
```
/pvpbot faction kit give-kit Red warrior
```

### Kit pondéré aléatoirement selon la faction
```
/pvpbot faction kit give-kit-random Red warrior 60% archer 30% mage 10%
```

## Gestion des kits
```
/pvpbot kit kits                  # List all kits
/pvpbot kit delete-kit warrior    # Delete a kit
```

## Remarques

- Les kits effacent complètement l'inventaire de la cible avant de postuler
- `give-kit-near`et`give-kit-near-random`exiger un exécuteur testamentaire
- `give-kit-random`et`give-kit-near-random`travailler depuis une console ou un lecteur
- Les noms des kits ne sont pas sensibles à la casse
- Les kits sont mondiaux (partagés dans tous les mondes)
- Les éléments pris en charge incluent tout élément Minecraft avec des données NBT complètes (enchantements, dégâts, etc.)
