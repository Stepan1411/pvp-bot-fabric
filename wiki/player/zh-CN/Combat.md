#⚔️战斗系统

PVP Bot 具有先进的战斗人工智能，可以使用不同的武器和战术。

---

## 🗡️ 武器类型

### 近战战斗
- **剑** - 快速攻击，良好的伤害
- **斧头** - 速度较慢，但​​可以打破护盾
- 当敌人靠近时，机器人会自动切换到近战模式

### 远程战斗
- **弓** - 绘制并释放箭头
- **十字弓** - 加载和消防螺栓
- 机器人保持最佳距离（8-20 个街区）

### 狼牙棒战斗
- **狼牙棒+风能冲锋** - 跳跃攻击造成巨大伤害
- 机器人利用风能发射到空中
- 毁灭性的坠落攻击

---

## 🎯 定位

### 复仇模式
当机器人受到伤害时，它会自动瞄准攻击者。
```mcfunction
/pvpbot settings revenge true
```

### 自动定位
机器人会自动搜索视野范围内的敌人。
```mcfunction
/pvpbot settings autotarget true
```

### 手动目标
强制机器人攻击特定目标。
```mcfunction
/pvpbot attack BotName TargetName
```

### 目标过滤器
选择机器人可以定位的目标：
```mcfunction
/pvpbot settings targetplayers true   # Target players
/pvpbot settings targetmobs true      # Target hostile mobs
/pvpbot settings targetbots true      # Target other bots
```

---

## 🛡️ 防御

### 自动屏蔽
当敌人攻击时，机器人会自动举起护盾。
```mcfunction
/pvpbot settings autoshield true
```

### 破盾
机器人使用斧头来禁用敌人的护盾。
```mcfunction
/pvpbot settings shieldbreak true
```

### 自动图腾
机器人会随时保存不死图腾。
```mcfunction
/pvpbot settings autototem true
```

---

## 🍎 治愈

### 自动吃
机器人在以下情况下吃食物：
- 健康状况不佳 (< 30%)
- 饥饿低于阈值

```mcfunction
/pvpbot settings autoeat true
/pvpbot settings minhunger 14
```

### 自动药水
机器人自动使用药水：
- **治疗药水** - 当HP较低时（飞溅或饮用）
- **力量药水** - 进入战斗时
- **速度药水** - 进入战斗时
- **抗火药水** - 进入战斗时

战斗开始时，所有增益药水都会立即投入。当效果到期时（剩余 < 5 秒），机器人会重新应用增益。

```mcfunction
/pvpbot settings autopotion true
```

### 撤退
当生命值较低时，机器人会在进食/治疗时撤退。
如果机器人没有食物（战斗至死），则无法撤退。

```mcfunction
/pvpbot settings retreat true
/pvpbot settings retreathp 0.3  # 30% HP
```

---

## 💥 热门歌曲

机器人可以通过跳跃攻击来进行致命一击。
```mcfunction
/pvpbot settings criticals true
```

---

## 🕸️ 蛛网战术

机器人可以战略性地使用蜘蛛网：
- **撤退时** - 将蜘蛛网放置在追逐敌人的下方以减慢他们的速度
- **在近战战斗中** - 将蜘蛛网放置在冲锋的敌人下方

```mcfunction
/pvpbot settings cobweb true
```

---

## ⚙️ 战斗设置

|设置|范围 |默认 |描述 |
|--------|--------|---------|------------|
| `战斗` |真/假|真实 |启用战斗 |
| ‘复仇’ |真/假|真实 |攻击攻击你的人 |
| `自动目标` |真/假|假 |自动寻找敌人 |
| `批评者` |真/假|真实 |严重打击 |
| `远程` |真/假|真实 |使用弓|
| `狼牙棒` |真/假|真实 |使用狼牙棒|
| `矛` |真/假|假 |使用长矛（越野车）|
| `自动药剂` |真/假|真实 |自动使用药水 |
| `蜘蛛网` |真/假|真实 |使用蜘蛛网 |
| `撤退` |真/假|真实 | HP低时撤退|
| `撤退` | 0.1-0.9| 0.3 | 0.3 HP % 撤退 |
| `攻击冷却时间` | 1-40 | 1-40 10 | 10攻击之间的滴答声|
| `近战范围` | 2-6 | 2-6 3.5 | 3.5近战攻击距离|
| `视距` | 5-128 | 5-128 64 | 64目标搜索范围|
