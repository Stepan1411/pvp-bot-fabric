# 🤖 PVP 机器人 - 维基

欢迎来到官方 PVP 机器人文档！

---

## 📖 关于

PVP Bot 是一个 Minecraft Fabric 模组，添加了由 Carpet 模组提供支持的智能战斗机器人。创建机器人大军，将他们组织成派系，观看史诗般的战斗展开！

---

## 🚀 快速入门

1.安装[Fabric Loader](https://fabricmc.net/)和[Carpet Mod](https://github.com/gnembon/fabric-carpet)
2.下载PVP Bot并将其放入您的“mods”文件夹中
3. 开始游戏并使用 `/pvpbot spawn BotName` 创建您的第一个机器人！

---

## 📚 文档

|页 |描述 |
|------|-------------|
| [🎮 命令](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Commands) |所有可用命令 |
| [⚔️战斗系统](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Combat) |机器人如何战斗 |
| [🚶 导航](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Navigation) |运动和寻路|
| [🛤️ 路径](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Paths) |路径系统和航路点|
| [👥 派系](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Factions) |团队体系|
| [🎒 套件](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Kits) |设备预设|
| [⚙️ 设置](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Settings) |所有配置选项|

---

## 💡 简单示例

### 创建一个简单的机器人
```
/pvpbot spawn MyBot
```

### 让两队战斗
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

## 🔗 链接

- [GitHub 存储库](https://github.com/Stepan1411/pvp-bot-fabric)
- [莫德林斯页面](https://modrinth.com/mod/pvp-bot)
- [错误报告](https://github.com/Stepan1411/pvp-bot-fabric/issues)
