# 派系

将机器人组织成团队/派系以进行协调战斗。每个世界的派系都保存在`config/pvpbot/worlds/<worldname>/factions.json`。

## 基本命令

### 创建和删除
```
/pvpbot faction create Red
/pvpbot faction delete Red
```

### 管理会员
```
/pvpbot faction add Red Bot1
/pvpbot faction remove Red Bot1
/pvpbot faction add-near Red 30          # Add all bots within 30 blocks
/pvpbot faction add-all Red              # Add all existing bots
```

### 检查信息
```
/pvpbot faction list              # List all factions
/pvpbot faction info Red          # Show members and enemies
```

## 敌对关系

将派系设置为敌人。机器人会自动攻击敌方派系成员。

```
/pvpbot faction hostile Red Blue
/pvpbot faction hostile Red Neutral false
```

**注意：** 敌人总是相互的——将红色设置为与蓝色敌对也会使蓝色与红色敌对。

## 协调行动

＃＃＃ 攻击
所有派系成员攻击一个目标：
```
/pvpbot faction attack Red GreenPlayer
```

### 给予物品
向所有派系成员提供物品：
```
/pvpbot faction give Red diamond_sword 1
```

### 赠送套件
为所有成员配备套件：
```
/pvpbot faction kit give-kit Red MyKit
```

### 赠送随机套件
为派系成员配备随机加权套件：
```
/pvpbot faction kit give-kit-random Red warrior 60% archer 30% mage 10%
```

每个成员根据体重分配获得一套装备。

### 传送
逐渐传送整个阵营（每 100 毫秒 5 个机器人）：
```
/pvpbot faction tp Red 100 64 -200
/pvpbot faction tp Red ~ ~ ~
/pvpbot faction tp Red PlayerName
```

支持绝对坐标、相对坐标`~`，或按名称定位玩家/机器人。

### 路径跟踪
所有成员都遵循一条路径：
```
/pvpbot faction path start Red MyPath
/pvpbot faction path stop Red
```

＃＃ 设置

|设置|默认|描述 |
|---------|---------|-------------|
|派别 |真实 |启用派系系统 |
|友军火力 |假 |允许攻击自己的派系成员 |
