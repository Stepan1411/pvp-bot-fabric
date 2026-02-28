# 👥 Système de factions

Organisez les robots et les joueurs en équipes qui peuvent s'affronter !

---

## 📖 Aperçu

Les factions sont des groupes de robots et de joueurs. Tu peux:
- Créer des équipes de robots
- Définir les factions comme hostiles les unes aux autres
- Les robots attaquent automatiquement les ennemis des factions hostiles

---

## 🏗️ Créer des factions

```mcfunction
# Create a faction
/pvpbot faction create RedTeam

# Delete a faction
/pvpbot faction delete RedTeam

# List all factions
/pvpbot faction list

# Get faction info
/pvpbot faction info RedTeam
```

---

## 👤 Gestion des membres

### Ajouter des membres
```mcfunction
# Add a bot to faction
/pvpbot faction add RedTeam Bot1

# Add a player to faction
/pvpbot faction add RedTeam Steve

# Add all nearby bots (within 20 blocks)
/pvpbot faction addnear RedTeam 20
```

### Supprimer des membres
```mcfunction
/pvpbot faction remove RedTeam Bot1
```

---

## ⚔️ Relations hostiles

Faites des factions des ennemies - leurs membres s'attaqueront automatiquement les uns les autres !

```mcfunction
# Make factions hostile
/pvpbot faction hostile RedTeam BlueTeam

# Make factions neutral again
/pvpbot faction hostile RedTeam BlueTeam false
```

### Comment ça marche
1. Le bot de RedTeam voit le joueur/bot de BlueTeam
2. Si les factions sont hostiles, le bot les cible automatiquement
3. Le combat commence !

> **Remarque :** Nécessite que « autotarget » soit activé pour le ciblage automatique.

---

## 🎁 Donner des objets

### Donner des objets
```mcfunction
# Give diamond sword to all faction members
/pvpbot faction give RedTeam diamond_sword

# Give multiple items
/pvpbot faction give RedTeam diamond_sword 1
/pvpbot faction give RedTeam golden_apple 16
```

### Offrez des kits
```mcfunction
# Give a saved kit to entire faction
/pvpbot faction givekit RedTeam warrior
```

---

## 📋 Exemple complet

Créez deux équipes et faites-les combattre :

```mcfunction
# Create bots
/pvpbot spawn Red1
/pvpbot spawn Red2
/pvpbot spawn Red3
/pvpbot spawn Blue1
/pvpbot spawn Blue2
/pvpbot spawn Blue3

# Create factions
/pvpbot faction create Red
/pvpbot faction create Blue

# Add bots to factions
/pvpbot faction add Red Red1
/pvpbot faction add Red Red2
/pvpbot faction add Red Red3
/pvpbot faction add Blue Blue1
/pvpbot faction add Blue Blue2
/pvpbot faction add Blue Blue3

# Make them enemies
/pvpbot faction hostile Red Blue

# Give equipment
/pvpbot faction give Red diamond_sword
/pvpbot faction give Blue diamond_sword
/pvpbot faction give Red diamond_chestplate
/pvpbot faction give Blue diamond_chestplate

# Enable auto-targeting
/pvpbot settings autotarget true

# Watch the battle!
```

---

## ⚙️ Paramètres

```mcfunction
# Enable/disable faction system
/pvpbot settings factions true
```

---

## 💾 Stockage des données

Les données de faction sont enregistrées dans :
```
config/pvp_bot_factions.json
```

Ce fichier persiste lors des redémarrages du serveur.
