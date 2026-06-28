# 套件

保存和加载机器人的设备预设。套件全局保存在`config/pvpbot/kits.json`。

## 创建套件

1.为你的角色配备所需的物品
2. 运行创建命令：

```
/pvpbot kit create-kit warrior
/pvpbot kit create-kit archer
```

套件将所有 41 个库存槽位（快捷栏、主库存、装甲、副手）保存为 NBT 数据。

## 应用套件

### 对于单个机器人或玩家
```
/pvpbot kit give-kit Bot1 warrior
```

### 至半径内的机器人
为半径内的所有机器人提供一个套件（默认值：10）：
```
/pvpbot kit give-kit-near warrior 15
```

### 半径内机器人的随机加权套件
根据半径内的机器人的权重提供随机套件：
```
/pvpbot kit give-kit-near-random 15 warrior 60% archer 30% mage 10%
```

15 个区块内的每个机器人都会根据重量分布获得一套套件。

### 对于整个派系
```
/pvpbot faction kit give-kit Red warrior
```

### 派别随机加权套件
```
/pvpbot faction kit give-kit-random Red warrior 60% archer 30% mage 10%
```

## 管理套件
```
/pvpbot kit kits                  # List all kits
/pvpbot kit delete-kit warrior    # Delete a kit
```

## 注释

- 套件在应用前完全清除目标的库存
- `give-kit-near`和`give-kit-near-random`需要一个玩家执行者
- `give-kit-random`和`give-kit-near-random`从控制台或播放器工作
- 套件名称不区分大小写
- 套件是全球性的（所有世界共享）
- 支持的物品包括任何具有完整 NBT 数据（结界、伤害等）的 Minecraft 物品
