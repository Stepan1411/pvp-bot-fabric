# 👥 派系系统

将机器人和玩家组织成可以互相战斗的团队！

---

## 📖 概述

派系是由机器人和玩家组成的群体。你可以：
- 创建机器人团队
- 将派系设置为彼此敌对
- 机器人自动攻击敌对派系的敌人

---

## 🏗️ 创建派系

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

## 👤 管理会员

### 添加成员
```mcfunction
# Add a bot to faction
/pvpbot faction add RedTeam Bot1

# Add a player to faction
/pvpbot faction add RedTeam Steve

# Add all nearby bots (within 20 blocks)
/pvpbot faction addnear RedTeam 20
```

### 删除成员
```mcfunction
/pvpbot faction remove RedTeam Bot1
```

---

## ⚔️ 敌对关系

让派系成为敌人——他们的成员会自动互相攻击！

```mcfunction
# Make factions hostile
/pvpbot faction hostile RedTeam BlueTeam

# Make factions neutral again
/pvpbot faction hostile RedTeam BlueTeam false
```

### 它是如何运作的
1. 红队的机器人看到蓝队的玩家/机器人
2. 如果派系存在敌对，机器人会自动瞄准他们
3.战斗开始！

> **注意：** 需要启用“autotarget”才能自动定位。

---

## 🎁 给予物品

### 给予物品
```mcfunction
# Give diamond sword to all faction members
/pvpbot faction give RedTeam diamond_sword

# Give multiple items
/pvpbot faction give RedTeam diamond_sword 1
/pvpbot faction give RedTeam golden_apple 16
```

### 赠送套件
```mcfunction
# Give a saved kit to entire faction
/pvpbot faction givekit RedTeam warrior
```

---

## 📋 完整示例

创建两支队伍并让他们战斗：

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

## ⚙️ 设置

```mcfunction
# Enable/disable faction system
/pvpbot settings factions true
```

---

## 💾 数据存储

派系数据保存在：
```
config/pvp_bot_factions.json
```

该文件在服务器重新启动后仍然存在。
