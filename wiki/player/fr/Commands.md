# 🎮 Commandes

Toutes les commandes PVP Bot commencent par `/pvpbot`. Nécessite le niveau d'autorisation 2 (opérateur).

---

## 📋 Table des matières

- [Gestion des robots](#-bot-management)
- [Commandes de combat](#-combat-commands)
- [Commandes de faction](#-faction-commands)
- [Commandes du kit](#-kit-commandes)
- [Commandes de chemin](#-path-commands)
- [Paramètres](#-paramètres)

---

## 🤖 Gestion des robots

| Commande | Descriptif |
|---------|-------------|
| `/pvpbot spawn [nom]` | Créer un nouveau bot (nom aléatoire si non spécifié) |
| `/pvpbot massspawn <nombre>` | Générer plusieurs robots avec des noms aléatoires (1-50) |
| `/pvpbot supprime <nom>` | Supprimer un robot |
| `/pvpbot supprimer tout` | Supprimer tous les robots |
| `/liste de robots pvp` | Lister tous les robots actifs |
| `/inventaire pvpbot <nom>` | Afficher l'inventaire du bot |

### Exemples

```mcfunction
# Create a bot with random name
/pvpbot spawn

# Create a bot named "Guard1"
/pvpbot spawn Guard1

# Spawn 10 bots with random names (10% chance for special names)
/pvpbot massspawn 10

# Remove the bot
/pvpbot remove Guard1

# See all bots
/pvpbot list
```

**Remarque :** Lors de la génération de robots avec des noms aléatoires, il y a 10 % de chances qu'ils obtiennent un nom spécial (`nantag` ou `Stepan1411`) au lieu d'un nom généré.

---

## ⚔️ Commandes de combat

| Commande | Descriptif |
|---------|-------------|
| `/pvpbot attaque <bot> <cible>` | Ordonner au bot d'attaquer un joueur/une entité |
| `/pvpbot arrête <bot>` | Empêcher le bot d'attaquer |
| `/pvpbot cible <bot>` | Afficher la cible actuelle du bot |

### Exemples

```mcfunction
# Make Bot1 attack player Steve
/pvpbot attack Bot1 Steve

# Stop the attack
/pvpbot stop Bot1

# Check who Bot1 is targeting
/pvpbot target Bot1
```

---

## 👥 Commandes de faction

| Commande | Descriptif |
|---------|-------------|
| `/pvpbot faction crée <nom>` | Créer une nouvelle faction |
| `/pvpbot faction supprimer <nom>` | Supprimer une faction |
| `/pvpbot faction ajoute <faction> <joueur>` | Ajouter un joueur/bot à la faction |
| `/pvpbot faction supprime <faction> <joueur>` | Retirer de la faction |
| `/pvpbot faction hostile <f1> <f2> [true/false]` | Définir les factions comme hostiles |
| `/pvpbot faction addnear <faction> <rayon>` | Ajouter tous les robots à proximité |
| `/pvpbot faction donne <faction> <item>` | Donner l'article à tous les membres |
| `/pvpbot faction givekit <faction> <kit>` | Offrez un kit à tous les membres |
| `/attaque de faction pvpbot <faction> <cible>` | Tous les robots de la faction attaquent la cible |
| `/liste des factions pvpbot` | Liste toutes les factions |
| `/informations sur la faction pvpbot <faction>` | Afficher les détails de la faction |

### Exemples

```mcfunction
# Create two factions
/pvpbot faction create RedTeam
/pvpbot faction create BlueTeam

# Add bots to factions
/pvpbot faction add RedTeam Bot1
/pvpbot faction add BlueTeam Bot2

# Make them enemies
/pvpbot faction hostile RedTeam BlueTeam

# Order entire faction to attack
/pvpbot faction attack RedTeam Steve

# Give swords to everyone in RedTeam
/pvpbot faction give RedTeam diamond_sword
```

---

## 🎒 Commandes du kit

| Commande | Descriptif |
|---------|-------------|
| `/pvpbot createkit <nom>` | Créez un kit à partir de votre inventaire |
| `/pvpbot deletekit <nom>` | Supprimer un kit |
| `/kits pvpbot` | Liste de tous les kits |
| `/pvpbot givekit <bot> <kit>` | Donner un kit à un bot |
| `/pvpbot faction givekit <faction> <kit>` | Donner le kit à la faction |

### Exemples

```mcfunction
# Put items in your inventory, then:
/pvpbot createkit warrior

# Give kit to a bot
/pvpbot givekit Bot1 warrior

# Give kit to entire faction
/pvpbot faction givekit RedTeam warrior
```

---

## 🛤️ Commandes de chemin

| Commande | Descriptif |
|---------|-------------|
| `/chemin pvpbot créer <nom>` | Créer un nouveau chemin |
| `/chemin pvpbot supprimer <nom>` | Supprimer un chemin |
| `/chemin pvpbot ajouter <nom>` | Ajouter la position actuelle comme waypoint |
| `/chemin pvpbot supprimer <nom> <index>` | Supprimer un waypoint par index |
| `/chemin pvpbot effacer <nom>` | Supprimer tous les waypoints |
| `/liste des chemins pvpbot` | Liste tous les chemins |
| `/informations sur le chemin pvpbot <nom>` | Afficher les informations sur le chemin |
| `/pvpbot chemin suivre <bot> <chemin>` | Faire en sorte que le bot suive le chemin |
| `/pvpbot chemin arrêter <bot>` | Empêcher le bot de suivre le chemin |
| `/boucle de chemin pvpbot <nom> <true/false>` | Basculer le mode boucle |
| `/attaque de chemin pvpbot <nom> <true/false>` | Basculer le mode combat |
| `/chemin pvpbot show <nom> <true/false>` | Basculer la visualisation du chemin |

### Exemples

```mcfunction
# Create a patrol route
/pvpbot path create patrol

# Add waypoints (stand at each location)
/pvpbot path add patrol
/pvpbot path add patrol
/pvpbot path add patrol

# Make bot follow the path
/pvpbot path follow Guard1 patrol

# Enable back-and-forth movement
/pvpbot path loop patrol true

# Disable combat while patrolling
/pvpbot path attack patrol false

# Show path with particles
/pvpbot path show patrol true
```

Voir la page [Chemins](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Paths) pour un guide détaillé.

---

## ⚙️ Paramètres

Utilisez `/pvpbot settings` pour voir tous les paramètres actuels.

Utilisez `/pvpbot settings <setting>` pour voir la valeur actuelle.

Utilisez `/pvpbot settings <setting> <value>` pour modifier un paramètre.

Voir la page [Paramètres](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Settings) pour la liste complète de tous les paramètres.

### Exemples rapides

```mcfunction
# Enable auto-targeting
/pvpbot settings autotarget true

# Set miss chance to 20%
/pvpbot settings misschance 20

# Enable bunny hop
/pvpbot settings bhop true
```
