# Paramètres

Tous les paramètres sont enregistrés par monde dans`config/pvpbot/worlds/<worldname>/settings.json`.

## Affichage et modification

```
/pvpbot settings                # List all settings
/pvpbot settings combat         # View a single setting
/pvpbot settings combat false   # Change a setting
```

## Paramètres de l'équipement

| Paramètre | Tapez | Par défaut | Gamme | Descriptif |
|--------|------|---------|-------|-------------|
| armure automatique | booléen | vrai | - | Équipez la meilleure armure de l'inventaire |
| arme automatique | booléen | vrai | - | Équipez la meilleure arme de la barre de raccourcis |
| armure tombante | booléen | faux | - | Lâchez une armure pire lors du remplacement |
| arme largable | booléen | faux | - | Lâchez les pires armes lors du remplacement |
| distance de chute | double | 3.0 | 1,0 - 10,0 | Distance pour déposer des objets |
| intervalle | entier | 20 | 1 à 100 | Intervalle de contrôle de l'équipement (tiques) |

## Paramètres de combat

| Paramètre | Tapez | Par défaut | Gamme | Descriptif |
|--------|------|---------|-------|-------------|
| combattre | booléen | vrai | - | Activer le système de combat |
| vengeance | booléen | vrai | - | Attaque automatique du dernier dommage |
| ciblage automatique | booléen | faux | - | Acquérir automatiquement la cible la plus proche |
| joueurs-cibles | booléen | vrai | - | Cibler de vrais joueurs |
| cibles-mobs | booléen | faux | - | Cibler les foules hostiles |
| robots cibles | booléen | faux | - | Cibler d'autres robots |
| vue-distance | double | 64,0 | 5,0 - 128,0 | Plage de recherche cible maximale |
| attaque-invincible | booléen | faux | - | Attaquer les joueurs créatifs/spectateurs |
| temps de recharge d'attaque | entier | 10 | 1 à 40 | Tiques entre les attaques |
| critiques | booléen | vrai | - | Sautez pour des coups critiques |
| tiques-chute-critique | entier | 6 | 1 à 10 | Tiques tombantes requises pour les critiques |
| portée de mêlée | double | 3.5 | 2,0 - 6,0 | Distance de portée en mêlée |
| vitesse de déplacement | double | 1.0 | 0,1 - 2,0 | Multiplicateur de vitesse de déplacement |
| chance manquée | entier | 0 | 0 - 100 | % de chances de rater des attaques |
| chance d'erreur | entier | 0 | 0 - 100 | % de chances de mal viser |
| vitesse de visée | double | 90,0 | 3,0 - 90,0 | Vitesse de rotation (degrés/sec) |

## Paramètres des armes

| Paramètre | Tapez | Par défaut | Gamme | Descriptif |
|--------|------|---------|-------|-------------|
| à distance | booléen | vrai | - | Activer le combat à l'arc/arbalète |
| masse | booléen | vrai | - | Activer le combat à la masse |
| lance | booléen | faux | - | Activer le combat à la lance |
| cristalpvp | booléen | vrai | - | Activer Crystal PVP |
| ancrepvp | booléen | vrai | - | Activer le PVP d'ancrage |
| préférer l'épée | booléen | vrai | - | Préférez les épées aux haches |
| bouclier-masse | booléen | vrai | - | Bouclier automatique contre les attaques à la masse |
| noms-spéciaux | booléen | faux | - | Utiliser une liste de noms spéciaux |
| gamme masse | double | 6.0 | 3,0 - 10,0 | Portée d'attaque de la masse |
| champ de tir | double | 4.5 | 2,0 - 8,0 | Portée d'attaque à la lance |
| portée de charge de lance | double | 12.0 | 5,0 - 20,0 | Plage de départ de la charge de lance |

### Combat à distance

| Paramètre | Tapez | Par défaut | Gamme | Descriptif |
|--------|------|---------|-------|-------------|
| arc-tirer-tiques | entier | 40 | 5 à 100 | Temps de tirage complet de l'arc (tics) |
| plage minimale | double | 20,0 | 3,0 - 20,0 | Distance minimale d'engagement de l'arc |
| plage optimale | double | 40,0 | 10,0 - 50,0 | Distance d'arc idéale |
| portée maximale | double | 60,0 | 15,0 - 100,0 | Plage d'engagement maximale de l'arc |
| prédiction de flèche | booléen | vrai | - | Arc prédictif visant des cibles mobiles |
| mitraillage à distance | booléen | vrai | - | Mitrailler latéralement en tirant à l'arc |
| retraite à distance | booléen | vrai | - | Retraite lorsque la cible se rapproche |

## Paramètres de l'utilitaire

| Paramètre | Tapez | Par défaut | Gamme | Descriptif |
|--------|------|---------|-------|-------------|
| totem automatique | booléen | vrai | - | Totem d'immortalité à équiper automatiquement |
| priorité totem | booléen | vrai | - | Gardez le totem à portée de main sur le bouclier |
| manger automatiquement | booléen | vrai | - | Manger automatiquement quand on a faim |
| bouclier automatique | booléen | vrai | - | Bouclier à levée automatique en cas de besoin |
| auto-potion | booléen | vrai | - | Utilisez des pots de guérison lorsque votre santé est faible |
| réparation automatique | booléen | vrai | - | Utilisez des bouteilles XP pour réparer du matériel |
| brise-bouclier | booléen | vrai | - | Brise-bouclier de hache bloquant les ennemis |
| toile d'araignée | booléen | vrai | - | Placez des toiles d'araignées sur les cibles |
| min-faim | entier | 14 | 1 à 20 | Niveau de faim pour commencer à manger |
| seuil de réparation | double | 0,25 | 0,1 - 0,9 | % de durabilité pour déclencher la réparation |
| bouclier-santé | double | 0,5 | 0,1 - 1,0 | % de santé pour tenir le bouclier |

## Paramètres de retraite

| Paramètre | Tapez | Par défaut | Gamme | Descriptif |
|--------|------|---------|-------|-------------|
| retraite | booléen | vrai | - | Activer le comportement de retraite |
| retraite-santé | double | 0,3 | 0,1 - 0,9 | % de santé pour commencer à reculer |
| santé critique | double | 0,15 | 0,05 - 0,5 | % de santé pour la retraite critique |

## Paramètres du bouclier

| Paramètre | Tapez | Par défaut | Gamme | Descriptif |
|--------|------|---------|-------|-------------|
| bouclier-hold-tiques | entier | 60 | 10-200 | Tiques pour maintenir le bouclier levé |
| bouclier-lever-tiques | entier | 12 | 2 à 40 | Tiques pour lever le bouclier avant le coup prévu |
| chance de rupture de bouclier | entier | 40 | 0 - 100 | % de chances de briser le bouclier ennemi par coup |

## Paramètres de navigation

| Paramètre | Tapez | Par défaut | Gamme | Descriptif |
|--------|------|---------|-------|-------------|
| bhop | booléen | vrai | - | Activer le saut de lapin |
| inactif | booléen | faux | - | Errance au ralenti quand aucune cible |
| rayon de ralenti | double | 10,0 | 3,0 - 50,0 | Rayon de dérapage au ralenti |

## Paramètres des factions

| Paramètre | Tapez | Par défaut | Gamme | Descriptif |
|--------|------|---------|-------|-------------|
| factions | booléen | vrai | - | Activer le système de faction |
| tir ami | booléen | faux | - | Autoriser les alliés attaquants |

## Paramètres divers

| Paramètre | Tapez | Par défaut | Gamme | Descriptif |
|--------|------|---------|-------|-------------|
| bot-congé-à-la-mort | booléen | vrai | - | Supprimer le bot à la mort |
| attaque-invincible | booléen | faux | - | Attaque chez le créateur/spectateur |
| bots-relogs | booléen | vrai | - | Restaurer les robots au redémarrage du serveur |
| spawn en toute sécurité | booléen | vrai | - | Décalage aléatoire (± 0,1-0,5 blocs) lors de l'apparition pour éviter la suffocation |
| effacer à la suppression | booléen | vrai | - | Effacer l'inventaire avant de supprimer le bot |
| profil-lagg-fix | booléen | vrai | - | Pré-remplir le cache de profil pour éviter le décalage lors de l'apparition du bot |
| max-mass-spawn | entier | 1000 | 50 à 10 000 | Nombre maximum de robots autorisés par commande d'apparition en masse |
