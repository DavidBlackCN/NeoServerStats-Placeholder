# NeoServerStats Placeholder 0.2.1

Minecraft 1.21.1 NeoForge 专用服占位符 mod，内置 30 个服务器变量与 44 个玩家变量。变量名参考 [Player 2.0.9](https://ecloud.placeholderapi.com/expansions/player/) 和 [Server 2.7.3](https://ecloud.placeholderapi.com/expansions/server/)，在 NeoForge 上重新实现。

## 安装与兼容性

| 组件 | 要求 |
| --- | --- |
| Java | 21 |
| Minecraft | 1.21.1 |
| NeoForge | `[21.1.249,21.2)`，默认编译基线 21.1.249；验证 21.1.249 / 21.1.250 |
| [Forge PlaceholderAPI](https://github.com/EnvyWare/ForgePlaceholderAPI) | 必需，NeoForge 1.21.1 的 2.1.0 发行物 |
| [TAB](https://www.curseforge.com/minecraft/mc-mods/tab/files/6556999) | 可选，5.2.1 的 `1.20.5 - 1.21.1` 发行物 |
| [EasyBot](https://files.inectar.cn/easybot_mods/neoforge/0.3.3) | 可选，0.3.3 的 `mc1.21+5` 发行物 |

将 FPAPI 与本 mod 的 JAR 放入服务器 `mods/`，需要消费方时再添加上述 TAB / EasyBot JAR。依赖不打包在本 mod 中，不安装 Bukkit 的 eCloud 扩展 JAR。客户端不需要安装本 mod；只支持专用服。

调用 Forge PlaceholderAPI 的模组可以使用注册变量；NeoForge TAB 和 EasyBot 使用本 mod 的独立适配。其他 PAPI 实现仍须接入对应接口，名称相同不代表自动兼容。

TAB 在服务器启动后通过公开 API 注册，`/tab reload` 后通过 `TabLoadEvent` 恢复。玩家刷新间隔 250 ms，服务器 1000 ms。EasyBot 在初始化阶段注册 `player` / `server` 处理器，仅移除本 mod 支持变量对应的公开预映射，使结果与 FPAPI 一致。未知键返回 null，交还 EasyBot；EasyBot 自己已有的其他映射仍可能解析原生变量。

适配按 mod 元数据精确检查 `tab=5.2.1` / `easybot=0.3.3`；构建依赖通过 SHA-256 固定发行物。其他版本跳过适配并告警一次。EasyBot 前缀冲突或 API 注册异常停用对应适配，FPAPI 核心继续工作。TAB 公开注册 API 会替换同名变量，包括未知变量的默认实现；请勿再安装争用这些名称的 TAB 提供器。

FPAPI 2.1.0 发行物自身元数据仍写着 `2.0.5`，因此元数据依赖范围使用 `[2.0,)`；其他 FPAPI 发行物尚未验证。

## 使用规则

语法为 `%server_<key>%` / `%player_<key>%`。FPAPI 命名空间必须小写，键大小写不敏感；跨三个消费方统一使用表中小写写法。

已实现玩家变量在无在线玩家上下文或退出后统一返回 `N/A`，包括 `player_online`。不会按姓名读取离线文件或选择其他在线玩家。启动尚无有效采样时 TPS 为 `N/A`。本 mod 不识别的变量不展开。

布尔值为 `yes` / `no`。彩色 ping：大于 100 红色，大于 50 黄色，其他绿色。TPS 颜色按未舍入值：大于 18 绿色，大于 16 黄色，其他红色；显示上限 20，默认舍入显示如 `20.0`。颜色为 Minecraft `§a` / `§e` / `§c`。

## 服务器变量（30）

| 精确变量 | 含义与格式 | 示例 |
| --- | --- | --- |
| `%server_version%` | Minecraft 版本 | `1.21.1` |
| `%server_name%` | eCloud 默认服务器名称，固定值 | `A Minecraft Server` |
| `%server_variant%` | 服务端实现 | `NeoForge` |
| `%server_build%` | NeoForge 构建版本 | `21.1.250` |
| `%server_version_full%` | Minecraft 与 NeoForge 版本组合 | `1.21.1-21.1.250` |
| `%server_online%` | 在线人数 | `1` |
| `%server_max_players%` | 人数上限 | `20` |
| `%server_players_list%` | 在线账户名，逗号和空格分隔；无人时为空串 | `Alice, Bob` |
| `%server_motd%` | MOTD 纯文本，去除 § 颜色代码 | `A Minecraft Server` |
| `%server_has_whitelist%` | 是否启用白名单 | `yes` |
| `%server_uptime%` | 本次服务器实例时长，非零 w d h m s 单位 | `1w 2d 3h 4m 5s` |
| `%server_memory_used%` | JVM 堆已用，带二进制单位 | `512.00 MiB` |
| `%server_memory_committed%` | JVM 堆已提交，带二进制单位 | `1.00 GiB` |
| `%server_memory_max%` | JVM 堆上限；未定义时 N/A | `2.00 GiB` |
| `%server_memory_percent%` | 堆已用 / 堆上限，一位小数百分比 | `25.0%` |
| `%server_ram_used%` | 堆已用，整数 MiB，无单位 | `512` |
| `%server_ram_free%` | 堆已提交减已用，整数 MiB，无单位 | `512` |
| `%server_ram_total%` | 堆已提交，整数 MiB，无单位 | `1024` |
| `%server_ram_max%` | 堆上限，整数 MiB；未定义时 N/A | `2048` |
| `%server_mspt%` | 原版最近 100 tick 平均工作耗时，毫秒，两位小数 | `12.43` |
| `%server_tps_current%` | 与 MSPT 同源的当前 TPS，两位小数，上限 20 | `20.00` |
| `%server_tps%` | 1 / 5 / 15 分钟彩色 TPS 列表 | `§a20.0§7, §a20.0§7, §a20.0` |
| `%server_cpu_process%` | JVM 进程 CPU，一位小数百分比；不可用时 N/A | `3.2%` |
| `%server_cpu_system%` | JDK 提供的运行环境 CPU，一位小数百分比；不可用时 N/A | `14.5%` |
| `%server_tps_1%` | 1 分钟墙钟 TPS；四舍五入到整数，保留 .0 | `20.0` |
| `%server_tps_1_colored%` | 1 分钟 TPS，带 § 颜色 | `§a20.0` |
| `%server_tps_5%` | 5 分钟墙钟 TPS；四舍五入到整数，保留 .0 | `20.0` |
| `%server_tps_5_colored%` | 5 分钟 TPS，带 § 颜色 | `§a20.0` |
| `%server_tps_15%` | 15 分钟墙钟 TPS；四舍五入到整数，保留 .0 | `20.0` |
| `%server_tps_15_colored%` | 15 分钟 TPS，带 § 颜色 | `§a20.0` |

## 玩家变量（44）

| 精确变量 | 含义与格式 | 示例 |
| --- | --- | --- |
| `%player_name%` | 账户名 | `Alice` |
| `%player_uuid%` | UUID | `cdb42808-ae2c-3717-9ace-2818b37bc3b7` |
| `%player_displayname%` | 显示名纯文本 | `Alice` |
| `%player_list_name%` | Tab 列表名纯文本；为空时使用账户名 | `Alice` |
| `%player_ping%` | 连接延迟，整数毫秒 | `50` |
| `%player_colored_ping%` | 带 § 颜色的延迟 | `§a50` |
| `%player_dimension%` | 维度资源 ID | `minecraft:overworld` |
| `%player_world%` | 存档名加原版维度后缀；自定义维度输出资源 ID | `world_nether` |
| `%player_world_type%` | Overworld / Nether / The End；自定义维度 N/A | `Nether` |
| `%player_yaw%` | 水平视角，原始浮点角度 | `90.0` |
| `%player_pitch%` | 垂直视角，原始浮点角度 | `0.0` |
| `%player_biome%` | 原版生物群系大写名称；自定义为资源 ID | `SNOWY_PLAINS` |
| `%player_biome_capitalized%` | 原版生物群系分词首字母大写；自定义 ID 原样 | `Snowy Plains` |
| `%player_health%` | 当前生命值，原始小数 | `20.0` |
| `%player_health_rounded%` | 当前生命值四舍五入整数 | `20` |
| `%player_max_health%` | 最大生命值，原始小数 | `20.0` |
| `%player_max_health_rounded%` | 最大生命值四舍五入整数 | `20` |
| `%player_food_level%` | 饥饿值，整数 | `20` |
| `%player_saturation%` | 饱和度，原始浮点值 | `5.0` |
| `%player_gamemode%` | 大写游戏模式名称 | `SURVIVAL` |
| `%player_online%` | 有效在线上下文 yes；无上下文或退出后 N/A | `yes` |
| `%player_allow_flight%` | 是否允许飞行 | `no` |
| `%player_is_flying%` | 是否正在飞行 | `no` |
| `%player_is_sneaking%` | 是否潜行 | `no` |
| `%player_is_sprinting%` | 是否疾跑 | `no` |
| `%player_is_sleeping%` | 是否睡眠 | `no` |
| `%player_is_inside_vehicle%` | 是否乘坐实体 | `no` |
| `%player_level%` | 经验等级 | `17` |
| `%player_exp%` | 当前等级进度，0～1 原始浮点数 | `0.34042552` |
| `%player_current_exp%` | 按等级及进度计算的当前经验总量 | `410` |
| `%player_total_exp%` | 原版 totalExperience 字段，与当前经验不同 | `0` |
| `%player_exp_to_level%` | 当前等级升一级需要的完整经验量，不是剩余量 | `47` |
| `%player_deaths%` | 原版 DEATHS 统计 | `0` |
| `%player_playtime%` | 原版 PLAY_TIME，保留 0.1.0 可读格式 | `1h 30m` |
| `%player_playtime_ticks%` | 原版累计游戏 tick | `108000` |
| `%player_playtime_seconds%` | 累计 tick / 20，向下取整 | `5400` |
| `%player_playtime_hours%` | 累计 tick / 72000，两位小数 | `1.50` |
| `%player_session_time%` | 本次连接时长；秒或 Xm Ys / Xh Ym，重连重置 | `2m 15s` |
| `%player_x%` | 向下取整方块坐标 | `-1` |
| `%player_x_long%` | 原始 double 小数坐标 | `-0.1` |
| `%player_y%` | 向下取整方块坐标 | `90` |
| `%player_y_long%` | 原始 double 小数坐标 | `90.0` |
| `%player_z%` | 向下取整方块坐标 | `-13` |
| `%player_z_long%` | 原始 double 小数坐标 | `-12.9` |

## 数据来源与兼容差异

Minecraft 状态只在服务器线程采集为不可变快照；玩家每 5 tick、全局/堆/CPU 约每秒更新。低 TPS 时玩家刷新相应变慢。解析线程只读取字符串快照，不等待主线程、不访问 Minecraft 状态、不采样 CPU、不进行文件或网络 I/O。登录/退出即时更新，会话残留定期清理，停服清空。

`server_tps_current` / `server_mspt` 共享原版最近 100 tick 计时；正常 20 TPS 的等待时间不属于 MSPT。历史 TPS 使用墙钟 60 / 300 / 900 秒窗口，预热期按已有时长计算。固定 901 个秒级桶记录 tick 完成数，窗口边界按该秒重叠比例插值，具有最多一秒的时间分辨率；与 Bukkit 实现可能有细微差异。

CPU 来自 Java 21 `com.sun.management.OperatingSystemMXBean`；进程值按接口提供的可用处理器范围归一化，系统值指 JVM 可见操作环境，容器中不保证代表整个物理宿主机。负值、NaN、无穷或无接口时返回 `N/A`。堆指标来自 `MemoryMXBean`；`ram_free` 不是系统可用内存，整数 MiB 各自截断可能使 `ram_used + ram_free` 比 `ram_total` 少 1。

死亡与累计时长只读取原版统计，不另存计数、不修改数据。会话用内存中的 UUID 与登录时间，按墙钟计算，不持久化。原版命令改等级未必同步 `totalExperience`，因此 `current_exp` 与 `total_exp` 可以不同。

NeoForge 没有 Bukkit 的服务器名、构建名与世界类型接口：`server_name` 保持 eCloud 默认名称，`variant` / `build` / `version_full` 输出 NeoForge 信息。显示名和 Tab 名为 Component 纯文本，不序列化颜色。`world` 为存档名和 `_nether` / `_the_end` 后缀；自定义维度输出资源 ID、`world_type=N/A`。原版 biome 使用 Minecraft 注册名，无法保证与所有 Bukkit 枚举一致。uptime 从本次 ServerStarted 开始，零值 `0s` 是明确兼容差异。

首版不实现装备、权限节点、离线历史、首次/最后登录、累计独立玩家数、实体/区块总数、任意日期格式、倒计时及 eCloud 其他变量。本 mod 保持它们原样；其他提供器可能自行处理。

## 从 0.1.0 迁移

- `%player_x/y/z%`（三个独立键）改为向下取整整数；小数改用对应 `_long`。
- `%server_tps%` 改为彩色三窗口列表；原两位小数单值改用 `%server_tps_current%`。
- `%server_uptime%` 改为非零 `w d h m s` 单位，零值 `0s`。
- `%server_memory_*%` 和累计玩家时长保留格式；新增 `%server_ram_*%` 为整数 MiB，无单位。

## 示例与调试

TAB 的 header/footer 等文本可以写：

```yaml
header:
  - '玩家：%player_name%  生命：%player_health%  坐标：%player_x%, %player_y%, %player_z%'
footer:
  - '在线：%server_online%/%server_max_players%  TPS：%server_tps%'
  - '堆内存：%server_ram_used%/%server_ram_max% MiB'
```

EasyBot 支持占位符的配置文本使用相同变量；玩家值必须由消费方传入真实在线 ServerPlayer。只有姓名不构成玩家上下文。

OP（权限等级 2）调试命令：

```text
/neoserverstats placeholder %server_tps% | %server_mspt% | %player_name% | %player_current_exp%
```

控制台无玩家显示 N/A；可用 `execute as <在线玩家> run neoserverstats placeholder ...` 指定上下文。`/placeholderapi` 仅列出已注册命名空间。启动记录注册数量和一次解析自检，不逐次记录解析。

## 构建与开发

```powershell
.\gradlew.bat clean build
.\gradlew.bat runServer
.\gradlew.bat runServer -PwithTab=true
.\gradlew.bat runServer -PwithEasyBot=true
.\gradlew.bat runServer -PwithTab=true -PwithEasyBot=true
.\gradlew.bat clean build -Pneo_version=21.1.250
.\gradlew.bat runServer -Pneo_version=21.1.250
```

Unix 使用 `./gradlew`。首次构建从 EnvyWare Maven 获取 FPAPI API，从官方发行地址下载精确 TAB / EasyBot API JAR 到 `build/compat/` 并校验 SHA-256。二者为 compileOnly，仅显式开发运行参数添加 localRuntime。不提交下载二进制，不 shade，不发布依赖 JAR。JUnit 仅用于开发测试。

产物：`build/libs/neoserverstats-placeholder-neoforge-1.21.1-0.2.1.jar`。默认使用 NeoForge 21.1.249 编译；`-Pneo_version` 可选择验证版本，不改变发布 JAR 声明的支持范围。GitHub Actions 分别在 21.1.249 / 21.1.250 上构建与执行测试。源码固定 UTF-8；Gradle 使用 COMPAT 解决 Windows 中文路径 worker 参数编码问题，见 [Gradle 已知问题](https://github.com/gradle/gradle/issues/30391)。

## 验证

详见 [VALIDATION.md](VALIDATION.md)，设计范围见 [PLAN.md](PLAN.md)。只声明记录中实际验证过的版本与路径，不声明对其他版本、远端机器人消息或离线玩家的兼容。

## 许可证

MIT，见 [LICENSE](LICENSE)。
