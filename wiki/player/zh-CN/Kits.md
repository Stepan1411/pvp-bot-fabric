# 🎒 套件系统

保存装备预设并快速装备机器人！

---

## 📖 概述

套件让您能够：
- 将当前库存保存为模板
- 快速给机器人提供装备
- 立即装备整个派系

---

## 📦 创建套件

1. 将物品放入库存中（盔甲、武器、食物等）
2. 运行创建命令

```mcfunction
/pvpbot createkit <name>
```

### 保存什么
- ✅ 热键栏物品（插槽 0-8）
- ✅ 库存物品
- ✅ 盔甲碎片
- ✅ 副手物品
- ✅ 物品附魔
- ✅ 物品耐用性
- ✅ 堆栈大小

＃＃＃ 例子
```mcfunction
# Put diamond armor, sword, bow, arrows, golden apples in your inventory
# Then save it:
/pvpbot createkit pvp_warrior
```

---

## 📋 管理套件

### 列出套件
```mcfunction
/pvpbot kits
```

### 删除套件
```mcfunction
/pvpbot deletekit pvp_warrior
```

---

## 🎁 赠送套件

### 到单个机器人
```mcfunction
/pvpbot givekit Bot1 pvp_warrior
```

### 致整个派系
```mcfunction
/pvpbot faction givekit RedTeam pvp_warrior
```

---

## 💡 套件创意

### ⚔️ 近战战士
- 钻石/下界合金剑
- 全钻石盔甲
- 盾
- 金苹果
- 不死图腾（副手）

### 🏹 弓箭手
- 弓（力量V，无限）
- 箭头（1 层）
- 皮革/链甲
- 金苹果

### 🔨 坦克
- 下界合金盔甲（防护 IV）
- 盾
- 斧头（用于破坏盾牌）
- 很多金苹果
- 多个图腾

### 💨 速度战士
- 轻甲（皮革/链甲）
- 钻石剑（锋利V）
- 速度药水
- 金苹果

---

## 📋 完整示例

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

## 💾 数据存储

套件数据保存在：
```
config/pvp_bot_kits.json
```

套件在服务器重新启动后仍然存在。

---

## ⚠️ 注释

- 机器人将自动装备套件中的盔甲
- 机器人将自动选择最佳武器
- 机器人库存中的现有物品不会被清除
- 如果机器人库存已满，则可能无法提供某些物品
