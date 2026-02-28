# 🎮 命令

所有 PVP Bot 命令均以“/pvpbot”开头。需要 2 级权限（操作员）。

---

## 📋 目录

- [机器人管理](#-bot-management)
- [战斗命令](#-combat-commands)
- [派系命令](#-faction-commands)
- [套件命令](#-kit-commands)
- [路径命令](#-path-commands)
- [设置](#-设置)

---

## 🤖 机器人管理

|命令 |描述 |
|---------|-------------|
| `/pvpbot 生成 [名称]` |创建一个新机器人（如果未指定，则为随机名称）|
| `/pvpbot Massspawn <计数>` |生成多个具有随机名称的机器人 (1-50) |
| `/pvpbot 删除 <名称>` |删除机器人 |
| `/pvpbot 全部删除` |删除所有机器人 |
| `/pvpbot 列表` |列出所有活跃的机器人 |
| `/pvpbot 库存<名称>` |显示机器人的库存 |

### 示例

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

**注意：** 当生成具有随机名称的机器人时，它们有 10% 的机会获得特殊名称（“nantag”或“Stepan1411”）而不是生成的名称。

---

## ⚔️ 战斗命令

|命令 |描述 |
|---------|-------------|
| `/pvpbot 攻击 <机器人> <目标>` |命令机器人攻击玩家/实体 |
| `/pvpbot 停止 <机器人>` |阻止机器人攻击 |
| `/pvpbot 目标 <bot>` |显示机器人的当前目标 |

### 示例

```mcfunction
# Make Bot1 attack player Steve
/pvpbot attack Bot1 Steve

# Stop the attack
/pvpbot stop Bot1

# Check who Bot1 is targeting
/pvpbot target Bot1
```

---

## 👥 派系命令

|命令 |描述 |
|---------|-------------|
| `/pvpbot 派系创建<名称>` |创建新派系|
| `/pvpbot 派别删除<名称>` |删除派别 |
| `/pvpbot 派系添加 <派系> <玩家>` |将玩家/机器人添加到派系 |
| `/pvpbot 派系删除 <派系> <玩家>` |从派系中删除 |
| `/pvpbot 派系敌对 <f1> <f2> [真/假]` |将派系设置为敌对 |
| `/pvpbot 派系 addnear <派系> <半径>` |添加所有附近的机器人 |
| `/pvpbot 派系给予 <派系> <项目>` |向所有会员赠送物品|
| `/pvpbot 派系 Givekit <派系> <套件>` |向所有会员赠送套件 |
| `/pvpbot 派系攻击 <派系> <目标>` |派系中的所有机器人都攻击目标 |
| `/pvpbot 派系列表` |列出所有派别 |
| `/pvpbot 派系信息 <派系>` |显示派系详细信息 |

### 示例

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

## 🎒 套件命令

|命令 |描述 |
|---------|-------------|
| `/pvpbot createkit <名称>` |从您的库存中创建套件 |
| `/pvpbot deletekit <名称>` |删除套件 |
| `/pvpbot 套件` |列出所有套件 |
| `/pvpbot Givekit <bot> <kit>` |向机器人提供套件 |
| `/pvpbot 派系 Givekit <派系> <套件>` |向派系提供套件 |

### 示例

```mcfunction
# Put items in your inventory, then:
/pvpbot createkit warrior

# Give kit to a bot
/pvpbot givekit Bot1 warrior

# Give kit to entire faction
/pvpbot faction givekit RedTeam warrior
```

---

## 🛤️ 路径命令

|命令 |描述 |
|---------|-------------|
| `/pvpbot 路径创建 <名称>` |创建新路径 |
| `/pvpbot 路径删除 <名称>` |删除路径 |
| `/pvpbot 路径添加 <名称>` |添加当前位置作为航点 |
| `/pvpbot 路径删除 <名称> <索引>` |按索引删除航路点 |
| `/pvpbot 路径清除 <名称>` |删除所有航点 |
| `/pvpbot 路径列表` |列出所有路径 |
| `/pvpbot 路径信息 <名称>` |显示路径信息 |
| `/pvpbot 路径遵循 <bot> <path>` |让机器人遵循路径 |
| `/pvpbot 路径停止 <bot>` |阻止机器人遵循以下路径 |
| `/pvpbot 路径循环 <名称> <真/假>` |切换循环模式 |
| `/pvpbot 路径攻击 <名称> <真/假>` |切换战斗模式 |
| `/pvpbot 路径显示 <名称> <真/假>` |切换路径可视化|

### 示例

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

有关详细指南，请参阅[路径](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Paths)页面。

---

## ⚙️ 设置

使用“/pvpbot settings”查看所有当前设置。

使用“/pvpbot settings <setting>”查看当前值。

使用“/pvpbot settings <设置> <值>”更改设置。

有关所有设置的完整列表，请参阅[设置](https://github.com/Stepan1411/pvp-bot-fabric/wiki/Settings)页面。

### 简单示例

```mcfunction
# Enable auto-targeting
/pvpbot settings autotarget true

# Set miss chance to 20%
/pvpbot settings misschance 20

# Enable bunny hop
/pvpbot settings bhop true
```
