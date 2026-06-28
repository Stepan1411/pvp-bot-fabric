# 命令

所有命令都使用`/pvpbot`前缀。

## 机器人管理

|命令 |描述 |
|---------|-------------|
| `/pvpbot spawn [name]`|生成一个机器人（如果省略，则为随机名称）|
| `/pvpbot remove <name>`|删除特定机器人 |
| `/pvpbot removeall`|删除所有机器人 |
| `/pvpbot reload`|重新加载所有配置（设置、套件、路径、机器人）|
| `/pvpbot bot-management list`|列出所有活跃的机器人 |
| `/pvpbot bot-management inventory <botname>`|查看机器人库存和统计数据 |
| `/pvpbot bot-management mass-spawn <1-50>`|产生多个机器人 |

## 战斗控制

|命令 |描述 |
|---------|-------------|
| `/pvpbot bot-management attack <botname> <target>`|强制机器人攻击目标 |
| `/pvpbot bot-management stop-attack <botname>`|停止攻击 |

＃＃ 设置

|命令 |描述 |
|---------|-------------|
| `/pvpbot settings`|列出所有当前设置 |
| `/pvpbot settings <name>`|查看特定设置 |
| `/pvpbot settings <name> <value>`|设定设定值 |

请参阅[设置]（设置）了解所有可用选项。

## 路径

|命令 |描述 |
|---------|-------------|
| `/pvpbot bot-management path create <name>`|创建新路径 |
| `/pvpbot bot-management path delete <name>`|删除路径 |
| `/pvpbot bot-management path add-point <name>`|添加当前位置作为航点 |
| `/pvpbot bot-management path remove-point <name> [index]`|删除点（最后一个或按索引）|
| `/pvpbot bot-management path clear <name>`|清除所有点 |
| `/pvpbot bot-management path loop <name> <true/false>`|切换循环 |
| `/pvpbot bot-management path start <bot> <path>`|按照路径启动机器人 |
| `/pvpbot bot-management path stop <bot>`|阻止机器人遵循路径 |
| `/pvpbot bot-management path list`|列出所有路径 |
| `/pvpbot bot-management path show <name> <true/false>`|切换路径可视化 |
| `/pvpbot bot-management path info <name>`|查看路径详细信息 |
| `/pvpbot bot-management path distribute <path>`|沿路径均匀分布机器人 |
| `/pvpbot bot-management path start-near <path> <radius>`|附近机器人的起始路径 |
| `/pvpbot bot-management path stop-all <path>`|停止路径上的所有机器人 |
| `/pvpbot bot-management path walk-type <name> <type>`|设置步行类型（bhop/sprint/walk）|

详细使用方法请参见[Paths](路径)。

## 套件

|命令 |描述 |
|---------|-------------|
| `/pvpbot kit create-kit <name>`|将您的库存保存为套件 |
| `/pvpbot kit delete-kit <name>`|删除套件 |
| `/pvpbot kit give-kit <player> <kitname>`|向玩家/机器人提供套件 |
| `/pvpbot kit kits`|列出所有套件 |
| `/pvpbot kit give-kit-near <kitname> [radius]`|为半径内的机器人提供套件（默认值：10）|
| `/pvpbot kit give-kit-near-random <radius> <kit1> <w1>% [<kit2> <w2>% ...]`|为半径内的机器人提供随机加权套件 |

详细使用方法请参见【套件】（套件）。

## 派系

|命令 |描述 |
|---------|-------------|
| `/pvpbot faction list`|列出所有派别 |
| `/pvpbot faction create <name>`|创建派系|
| `/pvpbot faction delete <name>`|删除派别 |
| `/pvpbot faction add <faction> <player>`|将玩家/机器人添加到派系 |
| `/pvpbot faction remove <faction> <player>`|从派系中删除玩家/机器人 |
| `/pvpbot faction hostile <f1> <f2> [true/false]`|设定敌对关系 |
| `/pvpbot faction info <name>`|查看派系信息 |
| `/pvpbot faction add-near <faction> <radius>`|将附近的机器人添加到派系 |
| `/pvpbot faction add-all <faction>`|将所有机器人添加到派系 |
| `/pvpbot faction give <faction> <item>`|向所有成员赠送物品 |
| `/pvpbot faction attack <faction> <target>`|全员攻击目标|
| `/pvpbot faction path start <faction> <path>`|所有成员都遵循路径 |
| `/pvpbot faction path stop <faction>`|停止路径上的所有成员 |
| `/pvpbot faction tp <faction> <x y z\|player>`|逐渐传送整个派系|

### 派系套件命令

|命令 |描述 |
|---------|-------------|
| `/pvpbot faction kit give-kit <faction> <kitname>`|向所有会员赠送套件 |
| `/pvpbot faction kit give-kit-random <faction> <kit1> <w1>% [<kit2> <w2>% ...]`|向派系成员提供随机加权套件 |

详细使用方法请参见【派系】（派系）。
