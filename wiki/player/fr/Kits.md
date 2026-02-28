# 🎒 Système de kits

Enregistrez les préréglages d’équipement et équipez rapidement les robots !

---

## 📖 Aperçu

Les kits vous permettent de :
- Enregistrez votre inventaire actuel comme modèle
- Donnez rapidement de l'équipement aux robots
- Équipez des factions entières à la fois

---

## 📦Création de kits

1. Mettez des objets dans votre inventaire (armures, armes, nourriture, etc.)
2. Exécutez la commande de création

```mcfunction
/pvpbot createkit <name>
```

### Ce qui est enregistré
- ✅ Éléments de la barre de raccourcis (emplacements 0 à 8)
- ✅ Articles d'inventaire
- ✅ Pièces d'armure
- ✅ Objet secondaire
- ✅ Enchantements d'objets
- ✅ Durabilité de l'article
- ✅ Tailles de pile

### Exemple
```mcfunction
# Put diamond armor, sword, bow, arrows, golden apples in your inventory
# Then save it:
/pvpbot createkit pvp_warrior
```

---

## 📋 Gestion des kits

### Liste des kits
```mcfunction
/pvpbot kits
```

### Supprimer le kit
```mcfunction
/pvpbot deletekit pvp_warrior
```

---

## 🎁 Donner des kits

### Vers un seul robot
```mcfunction
/pvpbot givekit Bot1 pvp_warrior
```

### À toute la faction
```mcfunction
/pvpbot faction givekit RedTeam pvp_warrior
```

---

## 💡 Idées de kits

### ⚔️ Combattant de mêlée
- Épée diamant/Netherite
- Armure entièrement en diamant
- Bouclier
- Pommes dorées
- Totem de l'immortalité (désinvolte)

### 🏹Archer
- Arc (Power V, Infinity)
- Flèche (1 pile)
- Armure en cuir/chaîne
- Pommes dorées

### 🔨 Réservoir
- Armure Netherite (Protection IV)
- Bouclier
- Hache (pour briser le bouclier)
- Beaucoup de pommes dorées
- Plusieurs totems

### 💨 Combattant de vitesse
- Armure légère (cuir/chaîne)
- Épée de diamant (Netteté V)
- Potions de vitesse
- Pommes dorées

---

## 📋 Exemple complet

```mcfunction
# Step 1: Prepare your inventory with items you want

# Step 2: Create the kit
/pvpbot createkit soldier

# Step 3: Spawn bots
/pvpbot spawn Soldier1
/pvpbot spawn Soldier2
/pvpbot spawn Soldier3

# Step 4: Give kit to all bots
/pvpbot givekit Soldier1 soldier
/pvpbot givekit Soldier2 soldier
/pvpbot givekit Soldier3 soldier

# Or create a faction and give kit to all at once:
/pvpbot faction create Army
/pvpbot faction add Army Soldier1
/pvpbot faction add Army Soldier2
/pvpbot faction add Army Soldier3
/pvpbot faction givekit Army soldier
```

---

## 💾 Stockage des données

Les données du kit sont enregistrées dans :
```
config/pvp_bot_kits.json
```

Les kits persistent lors des redémarrages du serveur.

---

## ⚠️ Remarques

- Les robots équiperont automatiquement l'armure du kit
- Les robots sélectionneront automatiquement la meilleure arme
- Les éléments existants dans l'inventaire du bot ne sont PAS effacés
- Si l'inventaire du robot est plein, certains objets peuvent ne pas être donnés
