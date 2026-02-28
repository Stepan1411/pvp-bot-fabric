# 🤖 Bot PVP - Wiki

Bienvenue dans la documentation officielle du Bot PVP !

---

## 📖 À propos

PVP Bot est un mod Minecraft Fabric qui ajoute des robots de combat intelligents alimentés par le mod Carpet. Créez des armées de robots, organisez-les en factions et regardez des batailles épiques se dérouler !

---

## 🚀 Démarrage rapide

1. Installez [Fabric Loader](https://fabricmc.net/) et [Carpet Mod](https://github.com/gnembon/fabric-carpet)
2. Téléchargez PVP Bot et placez-le dans votre dossier « mods »
3. Démarrez le jeu et utilisez `/pvpbot spawn BotName` pour créer votre premier bot !

---

## 📚Documentation

| Pages | Descriptif |
|------|-------------|
| [🎮 Commandes](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Commands) | Toutes les commandes disponibles |
| [⚔️ Système de combat](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Combat) | Comment les robots se battent |
| [🚶Navigation](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Navigation) | Mouvement et orientation |
| [🛤️ Chemins](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Paths) | Système de chemins et waypoints |
| [👥 Factions](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Factions) | Système d'équipe |
| [🎒Kits](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Kits) | Préréglages d'équipement |
| [⚙️ Paramètres](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Settings) | Toutes les options de configuration |

---

## 💡 Exemples rapides

### Créer un bot simple
```
/pvpbot spawn MyBot
```

### Faites combattre deux équipes
```
/pvpbot spawn Red1
/pvpbot spawn Blue1
/pvpbot faction create Red
/pvpbot faction create Blue
/pvpbot faction add Red Red1
/pvpbot faction add Blue Blue1
/pvpbot faction hostile Red Blue
```

---

## 🔗 Liens

- [Dépôt GitHub](https://github.com/Stepan1411/pvp-bot-fabric)
- [Page Modrinth](https://modrinth.com/mod/pvp-bot)
- [Rapports de bugs](https://github.com/Stepan1411/pvp-bot-fabric/issues)
