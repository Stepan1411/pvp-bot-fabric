# Système de combat

L'IA de combat gère automatiquement la sélection des cibles, le changement d'arme et les décisions tactiques.

## Modes d'armes

Les robots sélectionnent automatiquement le meilleur mode d'arme en fonction de la distance et des objets disponibles :

| Mode | Arme | Gamme | Conditions |
|------|--------|-------|------------|
| MÊLÉE | Épée / Hache |`melee-range`(par défaut 3.5) | Mode par défaut |
| À DISTANCE | Arc / Arbalète |`ranged-min-range` - `ranged-optimal-range`| Nécessite des flèches |
| MACE | Masse |`mace-range`(par défaut 6.0) |`mace`paramètre activé |
| LANCE | Lance |`spear-range` / `spear-charge-range` | `spear`paramètre activé |
| CRISTAL | Cristal PvP | 2,5 - 8,0 | Voir [Combat explosif](ExplosiveCombat) |
| ANCRE | Ancre PVP | 2,0 - 8,0 | Voir [Combat explosif](ExplosiveCombat) |

## Sélection de la cible

Priorité cible (évaluée à chaque tick) :

1. **Cible forcée** - Définir via`/pvpbot bot-management attack`
2. **Vengeance** - Attaque automatiquement le dernier dommage (délai d'attente de 30 s)
3. **Faction Enemies** - Membres de faction hostiles (si les factions sont activées)
4. **Cible automatique** - Ennemi valide le plus proche à portée

Les cibles valides peuvent inclure des joueurs, des foules hostiles et d'autres robots (configurables via les paramètres).

## Fonctionnalités de combat

### Coups critiques
Les robots sautent avant d'attaquer pour infliger des coups critiques lorsque`criticals`est activé.

### Gestion du bouclier
- Prédit les attaques ennemies en fonction de la direction et de la distance du sprint
- Lève le bouclier de manière préventive en utilisant le système de prédiction
- Une santé faible déclenche le maintien du bouclier
- Bouclier scintillant pour l'imprévisibilité

### Briser le bouclier
Lorsqu'un ennemi bloque, les robots le brisent avec un bouclier de hache avec une chance configurable.

### Prédiction des attaques ennemies
Suit les changements de position de l'ennemi, l'état du sprint et la distance pour prédire le moment de l'attaque pour lever le bouclier.

### Logique de retraite
Quand la santé descend en dessous`retreat-health-percent`:
- Lève le bouclier et s'éloigne
- Utilise des potions de guérison si disponibles
- Place des toiles d'araignées pour ralentir la poursuite
- Mange de la nourriture pour se régénérer

### Défense de masse
Détecte les ennemis à l’aide de la masse (dans les airs, en chute) et lève le bouclier de manière préventive – compense le bug du bouclier vanille.

### Placement de la toile d'araignée
Les robots peuvent placer des toiles d'araignées sur les cibles pour les immobiliser (utilise les objets de l'inventaire).

## Référence des paramètres

| Paramètre | Par défaut | Descriptif |
|---------|---------|-------------|
| combattre | vrai | Activer/désactiver entièrement le combat |
| vengeance | vrai | Engager automatiquement le dernier attaquant |
| ciblage automatique | faux | Acquérir automatiquement la cible la plus proche |
| joueurs-cibles | vrai | Cibler de vrais joueurs |
| cibles-mobs | faux | Cibler les foules hostiles |
| robots cibles | faux | Cibler d'autres robots |
| critiques | vrai | Utiliser les coups critiques sautés |
| temps de recharge d'attaque | 10 | Tiques entre les attaques |
| portée de mêlée | 3.5 | Portée d'attaque en mêlée |
| chance manquée | 0% | Chance de rater des attaques |
| chance d'erreur | 0% | Chance de faire des erreurs de visée |
| attaque-invincible | faux | Attaquer les joueurs créatifs/spectateurs |
| vitesse de visée | 60 | Vitesse de rotation (degrés/sec) |
| vue-distance | 64 | Plage d'acquisition de cible maximale |
