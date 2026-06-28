# Navigation

Le système de navigation gère les mouvements du robot avec détection d’obstacles et rotation fluide.

## Types de mouvements

### B-Hop (par défaut)
- Saute automatiquement en se déplaçant pour sauter du lapin
- Configurable via`bhop`paramètre
- Utilisé automatiquement pour une vitesse de déplacement >= 1,0

### Sprint
- Mouvement de sprint standard
- Utilisé lorsque bhop est désactivé

### Marcher
- Pas de sprint, mouvement plus lent
- Définir via un chemin de type marche

## Fonctions de mouvement

| Fonction | Descriptif |
|--------------|-------------|
| `moveToward(bot, target, speed)`| Aller vers une entité |
| `moveAway(bot, target, speed)`| S'éloigner d'une entité |
| `moveTowardPosition(bot, pos, speed)`| Se déplacer vers une position |
| `moveTowardCombat(bot, pos, speed, strafe)`| Mouvement de mitraillage de combat |

## Aspect lisse

Utilisations de rotation configurables`aim-speed`(degrés par tick) pour un suivi fluide :
- `lookAt(bot, entity)`— Suivre l'entité
- `lookAtPosition(bot, pos)`— Position de la piste
- `lookAway(bot, entity)`— Détourne le regard (se retire)

## Détection d'obstacles

Le bot détecte et gère :

| Obstacles | Comportement |
|----------|----------|
| **Blocs solides** | Saute pour effacer |
| **Murs** | Faire des pas de côté (éviter les changements de direction) |
| **Trous** | Saute par-dessus |
| **Échelles / Vignes** | Grimpe automatiquement (sprint désactivé) |
| **Eau** | Nage avec contrôle directionnel |

## Détection bloquée

Si le robot se déplace de moins de 0,05 bloc alors qu'il est au sol pendant plus de 10 ticks :
1. Direction d'évitement alternative
2. Saute pour s'échapper
3. Essaie de contourner les obstacles

## Gestion des reculs

Lorsque la vitesse horizontale dépasse 0,35 (recul), le robot arrête de sprinter et résiste au mouvement pendant 10 ticks maximum.

## Promenade inactive

Au repos (pas de cible) et`idle`Le paramètre est activé, les robots errent de manière aléatoire à l'intérieur`idle-radius`(10 blocs par défaut) à partir de la position d'apparition.

## W-Tap

Après avoir attaqué, le bot relâche brièvement le sprint (W-tap) pour réinitialiser la distance de recul de l'adversaire.

## Mitraillage de combat

En mode combat, les robots mitraillent vers la gauche/droite dans une direction aléatoire, en changeant tous les 8 à 18 ticks.

## Paramètres

| Paramètre | Par défaut | Descriptif |
|---------|---------|-------------|
| bhop | vrai | Activer le saut de lapin |
| inactif | faux | Activer l'errance inactive |
| rayon de ralenti | 10 | Rayon d'errance depuis le point d'apparition |
| vitesse de déplacement | 1.0 | Multiplicateur de vitesse de déplacement |
| vitesse de visée | 60 | Vitesse de rotation (degrés/sec) |
