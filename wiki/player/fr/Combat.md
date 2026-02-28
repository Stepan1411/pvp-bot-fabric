# ⚔️ Système de combat

PVP Bot dispose d'une IA de combat avancée qui peut utiliser différentes armes et tactiques.

---

## 🗡️ Types d'armes

### Combat au corps à corps
- **Épées** - Attaques rapides, bons dégâts
- **Haches** - Plus lentes mais peuvent briser les boucliers
- Les robots passent automatiquement en mêlée lorsque les ennemis sont proches

### Combat à distance
- **Arcs** - Dessinez et relâchez les flèches
- **Arbalètes** - Chargement et carreaux de tir
- Les robots gardent une distance optimale (8 à 20 blocs)

### Combat à la masse
- **Mace + Wind Charge** - Attaques sautées pour des dégâts massifs
- Les robots utilisent des charges de vent pour se lancer dans les airs
- Attaques de chute dévastatrices

---

## 🎯 Ciblage

### Mode Vengeance
Lorsqu'un robot subit des dégâts, il cible automatiquement l'attaquant.
```mcfunction
/pvpbot settings revenge true
```

### Ciblage automatique
Les robots recherchent automatiquement les ennemis à portée de vue.
```mcfunction
/pvpbot settings autotarget true
```

### Cible manuelle
Forcer un bot à attaquer une cible spécifique.
```mcfunction
/pvpbot attack BotName TargetName
```

### Filtres cibles
Choisissez quels robots peuvent cibler :
```mcfunction
/pvpbot settings targetplayers true   # Target players
/pvpbot settings targetmobs true      # Target hostile mobs
/pvpbot settings targetbots true      # Target other bots
```

---

## 🛡️ Défense

### Bouclier automatique
Les robots lèvent automatiquement leurs boucliers lorsque les ennemis attaquent.
```mcfunction
/pvpbot settings autoshield true
```

### Briser le bouclier
Les robots utilisent des haches pour désactiver les boucliers ennemis.
```mcfunction
/pvpbot settings shieldbreak true
```

### Auto-Totem
Les robots gardent les totems éternels à portée de main.
```mcfunction
/pvpbot settings autototem true
```

---

## 🍎 Guérison

### Manger automatiquement
Les robots mangent de la nourriture quand :
- La santé est faible (< 30%)
- La faim est en dessous du seuil

```mcfunction
/pvpbot settings autoeat true
/pvpbot settings minhunger 14
```

### Potions automatiques
Les robots utilisent automatiquement des potions :
- **Potions de guérison** - lorsque les HP sont faibles (splash ou buvable)
- **Potions de force** - lors de l'entrée en combat
- **Potions de vitesse** - lors de l'entrée en combat
- **Potions de résistance au feu** - en entrant en combat

Toutes les potions de buff sont lancées en même temps lorsque le combat commence. Les robots réappliquent les buffs lorsque les effets expirent (< 5 secondes restantes).

```mcfunction
/pvpbot settings autopotion true
```

### Retraite
Lorsque la santé est faible, les robots reculent tout en mangeant/soignant.
La retraite est désactivée si le bot n'a pas de nourriture (se bat jusqu'à la mort).

```mcfunction
/pvpbot settings retreat true
/pvpbot settings retreathp 0.3  # 30% HP
```

---

## 💥 Coups critiques

Les robots peuvent effectuer des coups critiques en chronométrant leurs attaques avec des sauts.
```mcfunction
/pvpbot settings criticals true
```

---

## 🕸️ Tactiques de toile d'araignée

Les robots peuvent utiliser les toiles d'araignées de manière stratégique :
- **Lors de la retraite** - place une toile d'araignée sous la poursuite de l'ennemi pour le ralentir
- **En combat au corps à corps** - place la toile d'araignée sous l'ennemi en charge

```mcfunction
/pvpbot settings cobweb true
```

---

## ⚙️ Paramètres de combat

| Paramètre | Gamme | Par défaut | Descriptif |
|---------|-------|---------|-------------|
| `combat` | vrai/faux | vrai | Activer le combat |
| « vengeance » | vrai/faux | vrai | Attaque qui vous a attaqué |
| `cible automatique` | vrai/faux | faux | Recherche automatique des ennemis |
| `critiques` | vrai/faux | vrai | Coups critiques |
| `à distance` | vrai/faux | vrai | Utilisez des arcs |
| `masse` | vrai/faux | vrai | Utiliser la masse |
| `lance` | vrai/faux | faux | Utiliser la lance (buggy) |
| `autopotion` | vrai/faux | vrai | Potions à usage automatique |
| `toile d'araignée` | vrai/faux | vrai | Utiliser des toiles d'araignées |
| `retraite` | vrai/faux | vrai | Retraite lorsque les PV sont faibles |
| `retraite` | 0,1-0,9 | 0,3 | % de PV à battre en retraite |
| `refroidissement de l'attaque` | 1-40 | 10 | Tiques entre les attaques |
| `mêlée` | 2-6 | 3.5 | Distance d'attaque en mêlée |
| `distance de vue` | 5-128 | 64 | Plage de recherche cible |
