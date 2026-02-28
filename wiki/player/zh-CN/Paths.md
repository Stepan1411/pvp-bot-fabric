# 🛤️ 路径系统

路径系统允许您创建预定义的路线供机器人遵循。机器人可以巡逻区域、在不同地点之间移动，还可以选择沿着路径进行战斗。

---

## 📋 目录

- [概述](#概述)
- [创建路径](#creating-paths)
- [管理航点](#managing-waypoints)
- [机器人控制](#bot-control)
- [路径设置](#path-settings)
- [可视化](#可视化)
- [示例](#examples)

---

## 🎯 概述

路径是机器人可以遵循的路径点序列。每条路径有：
- **名称** - 唯一标识符
- **航点** - 位置列表（x、y、z）
- **循环模式** - 机器人如何通过航路点移动
- **攻击模式** - 机器人是否停止战斗
- **可视化** - 显示路径的粒子效果

每个世界的路径保存在`config/pvpbot/worlds/{world}/paths.json`中

---

## 🆕 创建路径

### 创建一个新路径
```
/pvpbot path create <name>
```
使用给定名称创建一个空路径。

**例子：**
```
/pvpbot path create patrol_route
```

### 删除路径
```
/pvpbot path delete <name>
```
删除路径并阻止所有机器人遵循它。

**例子：**
```
/pvpbot path delete patrol_route
```

### 列出所有路径
```
/pvpbot path list
```
显示当前世界中所有可用的路径。

### 查看路径详细信息
```
/pvpbot path info <name>
```
显示路径信息：
- 航点数量
- 循环模式状态
- 攻击模式状态
- 所有航点坐标列表

**例子：**
```
/pvpbot path info patrol_route
```

---

## 📍 管理航点

### 添加航点
```
/pvpbot path add <name>
```
将您当前的位置作为新的路径点添加到路径中。

**例子：**
```
/pvpbot path add patrol_route
```
站在您希望机器人访问的每个位置并运行此命令。

### 删除航点
```
/pvpbot path remove <name> <index>
```
按索引（从 0 开始）删除特定航路点。

**例子：**
```
/pvpbot path remove patrol_route 2
```
从路径中删除第三个航点。

### 清除所有航点
```
/pvpbot path clear <name>
```
从路径中删除所有航点（保留路径本身）。

**例子：**
```
/pvpbot path clear patrol_route
```

---

## 🤖 机器人控制

### 开始以下路径
```
/pvpbot path follow <bot> <path>
```
使机器人开始遵循指定的路径。

**例子：**
```
/pvpbot path follow Guard1 patrol_route
```

### 停止跟随路径
```
/pvpbot path stop <bot>
```
阻止机器人遵循其当前路径。

**例子：**
```
/pvpbot path stop Guard1
```

---

## ⚙️ 路径设置

### 循环模式
```
/pvpbot path loop <name> <true/false>
```

控制机器人如何通过路径点移动：
- **假**（默认） - 循环：1→2→3→1→2→3...
- **真实** - 来回：1→2→3→2→1→2→3...

**例子：**
```
/pvpbot path loop patrol_route true
```

### 攻击模式
```
/pvpbot path attack <name> <true/false>
```

在遵循路径时控制战斗行为：
- **true**（默认）- 机器人在当前路径点停止战斗，然后继续
- **假** - 机器人忽略战斗并继续移动（BotCombat 已禁用）

**例子：**
```
/pvpbot path attack patrol_route false
```

---

## 👁️ 可视化

### 切换路径显示
```
/pvpbot path show <name> <true/false>
```

显示/隐藏路径的粒子效果：
- **航路点** - 每个点的蜡颗粒
- **线** - 绿色灰尘颗粒连接点

可视化在以下情况下自动启用：
- 创建路径
- 添加航点
- 开始沿着一条路走

**例子：**
```
/pvpbot path show patrol_route true
```

要禁用可视化：
```
/pvpbot path show patrol_route false
```

---

## 💡 示例

### 基本巡逻路线
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

### 战斗守卫
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

###平安快递
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

### 同一路径上的多个机器人
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

## 📝 注释

- 修改路径时自动保存
- 每个世界都有自己的一套路径
- 机器人在移动时会观察下一个路径点
- 当攻击模式为真时，机器人会在战斗后返回他们前往的航路点
- 路径可视化对所有玩家可见
- 机器人在距路径点 1.5 格以内时到达路径点

---

## 🔗 相关页面

- [命令](Commands.md) - 所有可用命令
- [Navigation](Navigation.md) - 机器人移动设置
- [战斗](Combat.md) - 战斗系统详细信息
