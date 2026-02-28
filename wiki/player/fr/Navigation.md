# 🚶 Système de navigation

PVP Bot propose des mécanismes d'orientation et de mouvement intelligents.

---

## 🧭 Orientation

### Évitement d'obstacles
Les robots détectent et évitent automatiquement :
- **Murs** - Tourner à gauche/droite pour faire le tour
- **Trous** - Sauter par-dessus les espaces
- **Obstacles à 1 bloc** - Sauter

### Escalade
Les robots peuvent grimper :
- Échelles
- Vignes
- Échafaudage
- Vignes tordues/pleureuses

---

## 🐰 Lapin Hop (Bhop)

Les robots peuvent sauter en lapin pour se déplacer plus rapidement – ​​en sautant tout en sprintant pour augmenter leur vitesse.

```mcfunction
# Enable/disable bhop
/pvpbot settings bhop true

# Set cooldown between jumps (5-30 ticks)
/pvpbot settings bhopcooldown 12

# Add extra jump height (0.0-0.5)
/pvpbot settings jumpboost 0.1
```

### Quand Bhop s'active
- La vitesse est >= 1,0
- Aucun obstacle à venir
- Ne pas grimper
- Au sol

---

## 😴 Errance inactive

Lorsque les robots n’ont pas de cible, ils errent autour de leur point d’apparition.

```mcfunction
# Enable/disable idle wandering
/pvpbot settings idle true

# Set wander radius (3-50 blocks)
/pvpbot settings idleradius 10
```

### Comportement
- Les robots marchent lentement (pas de sprint)
- Restez dans le rayon du point d'apparition
- Choisissez des destinations aléatoires
- Évitez les obstacles en errant

---

## 🏃 Vitesses de déplacement

Différentes situations utilisent des vitesses différentes :

| Situation | Vitesse | Bhop |
|---------------|-------|------|
| Errance oisive | 0,5 | ❌ |
| Objectif en approche | 1.0 | ✅ |
| Manger (se retirer) | 1.2 | ✅ |
| Retraite à faible HP | 1.5 | ✅ |

---

## ⚙️ Paramètres de navigation

| Paramètre | Gamme | Par défaut | Descriptif |
|---------|-------|---------|-------------|
| `bhop` | vrai/faux | vrai | Activer le lapin hop |
| `bhopcooldown` | 5-30 | 12 | Tiques entre les sauts |
| `jumpboost` | 0,0-0,5 | 0,0 | Hauteur de saut supplémentaire |
| `inactif` | vrai/faux | vrai | Activer l'errance inactive |
| `radius vide` | 3-50 | 10 | Rayon d'errance |
| `vitesse de déplacement` | 0,1-2,0 | 1.0 | Vitesse de déplacement de base |

---

## 🔧 Dépannage

### Le robot reste bloqué
- Les robots essaient automatiquement de se décoller
- Ils sautent et changent de direction après 10 ticks sans mouvement
- Si toujours bloqué, essayez de téléporter le bot

### Le robot ne grimpe pas
- Assurez-vous que l'échelle/vigne est correctement placée
- Le bot doit être face au bloc grimpable

### Le robot tombe dans des trous
- Les robots tentent de sauter par-dessus des espaces de 2 blocs
- Des espaces plus grands peuvent provoquer des chutes
- Envisagez de construire des ponts pour les chemins importants
