# 🛤️ Système de chemin

Le système de chemins vous permet de créer des itinéraires prédéfinis que les robots doivent suivre. Les robots peuvent patrouiller dans des zones, se déplacer entre des emplacements et éventuellement engager des combats tout en suivant des chemins.

---

## 📋 Table des matières

- [Aperçu](#aperçu)
- [Création de chemins](#creating-paths)
- [Gestion des waypoints](#managing-waypoints)
- [Contrôle des robots](#bot-control)
- [Paramètres du chemin](#path-settings)
- [Visualisation](#visualisation)
- [Exemples](#exemples)

---

## 🎯 Aperçu

Les chemins sont des séquences de waypoints que les robots peuvent suivre. Chaque chemin comporte :
- **Nom** - Identifiant unique
- **Waypoints** - Liste des positions (x, y, z)
- **Mode boucle** - Comment le robot se déplace à travers les waypoints
- **Mode Attaque** - Si le bot s'arrête pour le combat
- **Visualisation** - Effets de particules montrant le chemin

Les chemins sont enregistrés par monde dans `config/pvpbot/worlds/{world}/paths.json`

---

## 🆕 Créer des chemins

### Créer un nouveau chemin
```
/pvpbot path create <name>
```
Crée un chemin vide avec le nom donné.

**Exemple:**
```
/pvpbot path create patrol_route
```

### Supprimer un chemin
```
/pvpbot path delete <name>
```
Supprime le chemin et arrête tous les robots qui le suivent.

**Exemple:**
```
/pvpbot path delete patrol_route
```

### Liste tous les chemins
```
/pvpbot path list
```
Affiche tous les chemins disponibles dans le monde actuel.

### Afficher les détails du chemin
```
/pvpbot path info <name>
```
Affiche les informations sur le chemin :
- Nombre de waypoints
- État du mode boucle
- Statut du mode attaque
- Liste de toutes les coordonnées des waypoints

**Exemple:**
```
/pvpbot path info patrol_route
```

---

## 📍 Gestion des waypoints

### Ajouter un point de cheminement
```
/pvpbot path add <name>
```
Ajoute votre position actuelle en tant que nouveau waypoint au chemin.

**Exemple:**
```
/pvpbot path add patrol_route
```
Placez-vous à chaque endroit que vous souhaitez que le bot visite et exécutez cette commande.

### Supprimer le point de cheminement
```
/pvpbot path remove <name> <index>
```
Supprime un waypoint spécifique par son index (à partir de 0).

**Exemple:**
```
/pvpbot path remove patrol_route 2
```
Supprime le 3ème waypoint du chemin.

### Effacer tous les waypoints
```
/pvpbot path clear <name>
```
Supprime tous les waypoints du chemin (conserve le chemin lui-même).

**Exemple:**
```
/pvpbot path clear patrol_route
```

---

## 🤖 Contrôle des robots

### Commencez à suivre le chemin
```
/pvpbot path follow <bot> <path>
```
Fait démarrer un bot en suivant le chemin spécifié.

**Exemple:**
```
/pvpbot path follow Guard1 patrol_route
```

### Arrêter de suivre le chemin
```
/pvpbot path stop <bot>
```
Empêche le bot de suivre son chemin actuel.

**Exemple:**
```
/pvpbot path stop Guard1
```

---

## ⚙️ Paramètres du chemin

### Mode boucle
```
/pvpbot path loop <name> <true/false>
```

Contrôle la façon dont le bot se déplace à travers les waypoints :
- **faux** (par défaut) - Circulaire : 1→2→3→1→2→3...
- **vrai** - Aller-retour : 1→2→3→2→1→2→3...

**Exemple:**
```
/pvpbot path loop patrol_route true
```

### Mode d'attaque
```
/pvpbot path attack <name> <true/false>
```

Contrôle le comportement de combat tout en suivant le chemin :
- **true** (par défaut) - Le robot s'arrête au waypoint actuel pour combattre, puis continue
- **faux** - Le robot ignore le combat et continue de bouger (BotCombat désactivé)

**Exemple:**
```
/pvpbot path attack patrol_route false
```

---

## 👁️ Visualisation

### Basculer l'affichage du chemin
```
/pvpbot path show <name> <true/false>
```

Affiche/masque les effets de particules pour le chemin :
- **Waypoints** - Particules de cire à chaque point
- **Lignes** - Points de connexion des particules de poussière vertes

La visualisation s'active automatiquement lorsque :
- Création d'un chemin
- Ajout d'un waypoint
- Commencer à suivre un chemin

**Exemple:**
```
/pvpbot path show patrol_route true
```

Pour désactiver la visualisation :
```
/pvpbot path show patrol_route false
```

---

## 💡 Exemples

### Itinéraire de patrouille de base
```
# Create path
/pvpbot path create base_patrol

# Add waypoints (stand at each location)
/pvpbot path add base_patrol  # Point 1
/pvpbot path add base_patrol  # Point 2
/pvpbot path add base_patrol  # Point 3
/pvpbot path add base_patrol  # Point 4

# Make bot follow
/pvpbot path follow Guard1 base_patrol
```

### Garde avec combat
```
# Create path
/pvpbot path create guard_post

# Add waypoints
/pvpbot path add guard_post  # Position 1
/pvpbot path add guard_post  # Position 2

# Enable back-and-forth movement
/pvpbot path loop guard_post true

# Enable combat (default, but explicit)
/pvpbot path attack guard_post true

# Assign bot
/pvpbot path follow Guard1 guard_post
```

### Courrier paisible
```
# Create path
/pvpbot path create delivery_route

# Add waypoints
/pvpbot path add delivery_route  # Start
/pvpbot path add delivery_route  # Checkpoint 1
/pvpbot path add delivery_route  # Checkpoint 2
/pvpbot path add delivery_route  # End

# Disable combat (bot won't fight)
/pvpbot path attack delivery_route false

# Assign bot
/pvpbot path follow Courier1 delivery_route
```

### Plusieurs robots sur le même chemin
```
# Create path
/pvpbot path create wall_patrol

# Add waypoints
/pvpbot path add wall_patrol  # Corner 1
/pvpbot path add wall_patrol  # Corner 2
/pvpbot path add wall_patrol  # Corner 3
/pvpbot path add wall_patrol  # Corner 4

# Assign multiple bots
/pvpbot path follow Guard1 wall_patrol
/pvpbot path follow Guard2 wall_patrol
/pvpbot path follow Guard3 wall_patrol
```

---

## 📝 Remarques

- Les chemins sont enregistrés automatiquement lorsqu'ils sont modifiés
- Chaque monde a son propre ensemble de chemins
- Les robots regardent le prochain waypoint tout en se déplaçant
- Lorsque le mode attaque est vrai, les robots reviennent au waypoint vers lequel ils se dirigeaient après le combat
- La visualisation du chemin est visible par tous les joueurs
- Les robots atteignent un waypoint à moins de 1,5 pâté de maisons de celui-ci

---

## 🔗 Pages connexes

- [Commands](Commands.md) - Toutes les commandes disponibles
- [Navigation](Navigation.md) - Paramètres de mouvement du robot
- [Combat](Combat.md) - Détails du système de combat
