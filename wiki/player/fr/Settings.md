# ⚙️ Paramètres

Liste complète de toutes les options de configuration.

---

## 📋 Commandes

```mcfunction
# Show all settings
/pvpbot settings

# Show specific setting
/pvpbot settings <name>

# Change setting
/pvpbot settings <name> <value>
```

---

## ⚔️ Paramètres de combat

| Paramètre | Tapez | Gamme | Par défaut | Descriptif |
|---------|------|-------|---------|-------------|
| `combat` | booléen | - | vrai | Activer/désactiver le système de combat |
| « vengeance » | booléen | - | vrai | Attaquez les entités qui endommagent le bot |
| `cible automatique` | booléen | - | faux | Rechercher automatiquement les ennemis |
| `joueurs cibles` | booléen | - | vrai | Peut cibler les joueurs |
| `cibler les foules` | booléen | - | faux | Peut cibler des foules hostiles |
| `bots cibles` | booléen | - | faux | Peut cibler d'autres robots |
| `critiques` | booléen | - | vrai | Effectuer des coups critiques |
| `à distance` | booléen | - | vrai | Utiliser des arcs/arbalètes |
| `masse` | booléen | - | vrai | Utiliser une masse avec des charges de vent |
| `lance` | booléen | - | faux | Utiliser la lance (désactivée en raison d'un bug de tapis) |
| `refroidissement de l'attaque` | entier | 1-40 | 10 | Tiques entre les attaques |
| `mêlée` | double | 2-6 | 3.5 | Distance d'attaque en mêlée |
| `vitesse de déplacement` | double | 0,1-2,0 | 1.0 | Multiplicateur de vitesse de déplacement |
| `distance de vue` | double | 5-128 | 64 | Portée maximale de détection de cible |
| `retraite` | booléen | - | vrai | Retraite lorsque les PV sont faibles |
| `retraite` | double | 0,1-0,9 | 0,3 | Pourcentage de HP pour commencer la retraite (30%) |

---

## 🧪 Paramètres des potions

| Paramètre | Tapez | Gamme | Par défaut | Descriptif |
|---------|------|-------|---------|-------------|
| `autopotion` | booléen | - | vrai | Potions de guérison/buff à utilisation automatique |
| `toile d'araignée` | booléen | - | vrai | Utilisez des toiles d'araignées pour ralentir les ennemis |

Les robots utilisent automatiquement :
- **Potions de guérison** lorsque les PV sont faibles
- **Potions de force** en entrant en combat
- **Potions de vitesse** en entrant en combat
- **Potions de résistance au feu** en entrant en combat
- **Toiles d'araignées** pour ralentir les ennemis (lors de la retraite ou de la charge de l'ennemi)

Toutes les potions de buff sont lancées en même temps lorsque le combat commence ou lorsque les effets expirent.

---

## 🚶 Paramètres de navigation

| Paramètre | Tapez | Gamme | Par défaut | Descriptif |
|---------|------|-------|---------|-------------|
| `bhop` | booléen | - | vrai | Activer le lapin hop |
| `bhopcooldown` | entier | 5-30 | 12 | Tiques entre les sauts bhop |
| `jumpboost` | double | 0,0-0,5 | 0,0 | Hauteur de saut supplémentaire |
| `inactif` | booléen | - | vrai | Promenez-vous quand aucune cible |
| `radius vide` | double | 3-50 | 10 | Rayon de dérapage au ralenti |

---

## 🛡️ Paramètres de l'équipement

| Paramètre | Tapez | Gamme | Par défaut | Descriptif |
|---------|------|-------|---------|-------------|
| `armure automatique` | booléen | - | vrai | Équiper automatiquement la meilleure armure |
| `arme automatique` | booléen | - | vrai | Équiper automatiquement la meilleure arme |
| `autototem` | booléen | - | vrai | Totem à équiper automatiquement en main levée |
| `bouclier automatique` | booléen | - | vrai | Bouclier à utilisation automatique lors du blocage |
| `préfère l'épée` | booléen | - | vrai | Préférez l’épée à la hache |
| `brise-bouclier` | booléen | - | vrai | Passez à la hache pour briser le bouclier ennemi |
| `droparmor` | booléen | - | faux | Lâchez des pièces d'armure pires |
| `arme largable` | booléen | - | faux | Lâchez les pires armes |
| `distance de chute` | double | 1-10 | 3.0 | Distance de retrait des articles |
| `intervalle` | entier | 1-100 | 20 | Intervalle de contrôle de l'équipement (tiques) |
| `niveau minarmor` | entier | 0-100 | 0 | Niveau d'armure minimum pour équiper |

### Niveaux d'armure
| Niveau | Type d'armure |
|-------|------------|
| 0 | N'importe quelle armure |
| 20 | Cuir+ |
| 40 | Or+ |
| 50 | Chaîne+ |
| 60 | Fer+ |
| 80 | Diamant+ |
| 100 | Netherite uniquement |

---

## 🎭 Paramètres de réalisme

| Paramètre | Tapez | Gamme | Par défaut | Descriptif |
|---------|------|-------|---------|-------------|
| `malchance` | entier | 0-100 | 10 | Chance de rater des attaques (%) |
| `erreur de chance` | entier | 0-100 | 5 | Chance d'attaquer dans la mauvaise direction (%) |
| `délai de réaction` | entier | 0-20 | 0 | Délai avant de réagir (tiques) |

---

## 👥 Autres paramètres

| Paramètre | Tapez | Gamme | Par défaut | Descriptif |
|---------|------|-------|---------|-------------|
| `factions` | booléen | - | vrai | Activer le système de faction |

---

## 💾 Fichiers de configuration

Les paramètres sont enregistrés dans :
```
config/pvp_bot.json
```

Les données du bot (positions, dimensions, modes de jeu) sont enregistrées dans :
```
config/pvp_bot_bots.json
```

Les paramètres et les robots persistent lors des redémarrages du serveur. Les robots sont automatiquement restaurés au démarrage du serveur.

---

## 📋 Exemples

### Rendre les robots plus réalistes
```mcfunction
/pvpbot settings misschance 15
/pvpbot settings mistakechance 10
/pvpbot settings reactiondelay 5
```

### Rendre les robots agressifs
```mcfunction
/pvpbot settings autotarget true
/pvpbot settings targetplayers true
/pvpbot settings targetbots true
/pvpbot settings revenge true
```

### Désactiver le combat à distance
```mcfunction
/pvpbot settings ranged false
/pvpbot settings mace false
```

### Mouvement rapide
```mcfunction
/pvpbot settings bhop true
/pvpbot settings bhopcooldown 8
/pvpbot settings jumpboost 0.2
/pvpbot settings movespeed 1.5
```

### Gardes fixes
```mcfunction
/pvpbot settings idle false
/pvpbot settings bhop false
```
