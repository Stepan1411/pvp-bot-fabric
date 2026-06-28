# Commandes

Toutes les commandes utilisent le`/pvpbot`préfixe.

## Gestion des robots

| Commande | Descriptif |
|---------|-------------|
| `/pvpbot spawn [name]`| Générer un bot (nom aléatoire si omis) |
| `/pvpbot remove <name>`| Supprimer un bot spécifique |
| `/pvpbot removeall`| Supprimer tous les robots |
| `/pvpbot reload`| Recharger toutes les configurations (paramètres, kits, chemins, bots) |
| `/pvpbot bot-management list`| Lister tous les robots actifs |
| `/pvpbot bot-management inventory <botname>`| Afficher l'inventaire et les statistiques des robots |
| `/pvpbot bot-management mass-spawn <1-50>`| Générer plusieurs robots |

## Contrôle des combats

| Commande | Descriptif |
|---------|-------------|
| `/pvpbot bot-management attack <botname> <target>`| Forcer le bot à attaquer une cible |
| `/pvpbot bot-management stop-attack <botname>`| Arrêtez d'attaquer |

## Paramètres

| Commande | Descriptif |
|---------|-------------|
| `/pvpbot settings`| Répertorier tous les paramètres actuels |
| `/pvpbot settings <name>`| Afficher un paramètre spécifique |
| `/pvpbot settings <name> <value>`| Définir une valeur de paramètre |

Voir [Paramètres](Paramètres) pour toutes les options disponibles.

## Chemins

| Commande | Descriptif |
|---------|-------------|
| `/pvpbot bot-management path create <name>`| Créer un nouveau chemin |
| `/pvpbot bot-management path delete <name>`| Supprimer un chemin |
| `/pvpbot bot-management path add-point <name>`| Ajouter la position actuelle comme waypoint |
| `/pvpbot bot-management path remove-point <name> [index]`| Supprimer le point (dernier ou par index) |
| `/pvpbot bot-management path clear <name>`| Effacer tous les points |
| `/pvpbot bot-management path loop <name> <true/false>`| Activer/désactiver la boucle |
| `/pvpbot bot-management path start <bot> <path>`| Démarrer le bot en suivant le chemin |
| `/pvpbot bot-management path stop <bot>`| Arrêter le robot de suivre le chemin |
| `/pvpbot bot-management path list`| Liste tous les chemins |
| `/pvpbot bot-management path show <name> <true/false>`| Basculer la visualisation du chemin |
| `/pvpbot bot-management path info <name>`| Afficher les détails du chemin |
| `/pvpbot bot-management path distribute <path>`| Répartissez les robots uniformément le long du chemin |
| `/pvpbot bot-management path start-near <path> <radius>`| Chemin de départ pour les robots à proximité |
| `/pvpbot bot-management path stop-all <path>`| Arrêtez tous les robots sur le chemin |
| `/pvpbot bot-management path walk-type <name> <type>`| Définir le type de marche (bhop/sprint/walk) |

Voir [Chemins](Chemins) pour une utilisation détaillée.

## Trousses

| Commande | Descriptif |
|---------|-------------|
| `/pvpbot kit create-kit <name>`| Enregistrez votre inventaire sous forme de kit |
| `/pvpbot kit delete-kit <name>`| Supprimer un kit |
| `/pvpbot kit give-kit <player> <kitname>`| Donner le kit au joueur/bot |
| `/pvpbot kit kits`| Liste de tous les kits |
| `/pvpbot kit give-kit-near <kitname> [radius]`| Donner un kit aux robots dans le rayon (par défaut : 10) |
| `/pvpbot kit give-kit-near-random <radius> <kit1> <w1>% [<kit2> <w2>% ...]`| Donnez un kit pondéré aléatoirement aux robots dans un rayon |

Voir [Kits](Kits) pour une utilisation détaillée.

## Factions

| Commande | Descriptif |
|---------|-------------|
| `/pvpbot faction list`| Liste toutes les factions |
| `/pvpbot faction create <name>`| Créer une faction |
| `/pvpbot faction delete <name>`| Supprimer une faction |
| `/pvpbot faction add <faction> <player>`| Ajouter un joueur/bot à la faction |
| `/pvpbot faction remove <faction> <player>`| Supprimer le joueur/bot de la faction |
| `/pvpbot faction hostile <f1> <f2> [true/false]`| Définir des relations hostiles |
| `/pvpbot faction info <name>`| Afficher les informations sur les factions |
| `/pvpbot faction add-near <faction> <radius>`| Ajouter des robots à proximité à la faction |
| `/pvpbot faction add-all <faction>`| Ajouter tous les robots à la faction |
| `/pvpbot faction give <faction> <item>`| Offrez des objets à tous les membres |
| `/pvpbot faction attack <faction> <target>`| Tous les membres attaquent la cible |
| `/pvpbot faction path start <faction> <path>`| Tous les membres suivent le chemin |
| `/pvpbot faction path stop <faction>`| Arrêter tous les membres sur le chemin |
| `/pvpbot faction tp <faction> <x y z\|player>`| Téléportez progressivement toute la faction |

### Commandes du kit de faction

| Commande | Descriptif |
|---------|-------------|
| `/pvpbot faction kit give-kit <faction> <kitname>`| Offrez un kit à tous les membres |
| `/pvpbot faction kit give-kit-random <faction> <kit1> <w1>% [<kit2> <w2>% ...]`| Donnez un kit pondéré aléatoirement aux membres de la faction |

Voir [Factions](Factions) pour une utilisation détaillée.
