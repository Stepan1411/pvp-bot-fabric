# 路径系统

创建供机器人遵循的路径点路径。每个世界的路径保存在`config/pvpbot/worlds/<worldname>/paths.json`。

## 命令

### 创建路径
```
/pvpbot bot-management path create MyPath
```

### 添加航点
站在所需位置并运行：
```
/pvpbot bot-management path add-point MyPath
```
路径可视化（粒子）自动启用。

### 管理航点
```
/pvpbot bot-management path remove-point MyPath      # Remove last
/pvpbot bot-management path remove-point MyPath 0    # Remove by index
/pvpbot bot-management path clear MyPath             # Clear all points
/pvpbot bot-management path info MyPath              # List all points
/pvpbot bot-management path list                     # List all paths
```

### 遵循路径
```
/pvpbot bot-management path start Bot1 MyPath
/pvpbot bot-management path stop Bot1
/pvpbot bot-management path start-near MyPath 20     # Start for bots within 20 blocks
/pvpbot bot-management path stop-all MyPath          # Stop all bots on path
```

＃＃＃ 分配
沿路径点均匀分布机器人：
```
/pvpbot bot-management path distribute MyPath
```

### 步行类型
```
/pvpbot bot-management path walk-type MyPath bhop    # Bunny hop (default)
/pvpbot bot-management path walk-type MyPath sprint  # Sprint
/pvpbot bot-management path walk-type MyPath walk    # Walk
```

### 循环模式
```
/pvpbot bot-management path loop MyPath true
/pvpbot bot-management path loop MyPath false
```

在循环模式下，机器人在最后反转方向（乒乓球）。在非循环模式下，机器人从头开始。

### 可视化
```
/pvpbot bot-management path show MyPath true
/pvpbot bot-management path show MyPath false
```

将路径点显示为粒子（WAX_ON + 绿色灰尘线）。

## 派系路径

一次控制一个派系中的所有机器人：
```
/pvpbot faction path start RedFaction MyPath
/pvpbot faction path stop RedFaction
```

＃＃ 特性

|物业 |选项|默认|
|----------|---------|---------|
|步行类型| bhop / 冲刺 / 步行 |博普|
|循环|真/假|假 |
|攻击|真/假|真实 |
|积分 |变量 (Vec3d) | - |
