# NeoServerStats Placeholder

纯服务端的 Minecraft 1.21.1 NeoForge mod，通过 **Forge PlaceholderAPI** 暴露服务器与玩家运行时统计。

---

## 环境要求

| 组件 | 版本 |
| --- | --- |
| Minecraft | `1.21.1` |
| NeoForge | `21.1.x`（开发与验证使用 `21.1.250`） |
| Java | `21` |
| Forge PlaceholderAPI | `2.1.0`（NeoForge 1.21.1），**必需** |

**仅服务端**：入口标注 `@Mod(dist = Dist.DEDICATED_SERVER)`，所有依赖声明为 `side="SERVER"`，客户端不加载也无需安装。

## 安装

1. 服务端安装 NeoForge `21.1.x`（Minecraft `1.21.1`）。
2. 将 `ForgePlaceholderAPI-NeoForge-2.1.0-1.21.1.jar` 放入 `mods/`（必需依赖）。
3. 将本 mod 的 jar 放入 `mods/`。
4. 启动服务器。

---

## 占位符语法

```text
%<命名空间>_<键>%
```

* **命名空间大小写敏感**（是 Forge PlaceholderAPI 正则中的字面量）：只有 `%server_...%` 与 `%player_...%` 有效，`%SERVER_...%` 完全不匹配。
* **键大小写不敏感**：`%player_name%` 与 `%player_NAME%` 均可解析。
* 可嵌入长文本：`Running Minecraft %server_version%` → `Running Minecraft 1.21.1`。
* 本 mod 未实现的占位符保持原样，不会被清空。
* **无玩家上下文时，所有 `%player_*%` 一律返回 `N/A`**，不抛异常、不随机取在线玩家、不用玩家列表第一个兜底。

---

## 服务器占位符（14 个）

| 占位符 | 说明 | 示例输出 |
| --- | --- | --- |
| `%server_version%` | Minecraft 版本 | `1.21.1` |
| `%server_online%` | 当前在线人数 | `12` |
| `%server_max_players%` | 配置的人数上限 | `20` |
| `%server_players_list%` | 在线玩家名，逗号分隔；无人在线为空串 | `Alice, Bob, Steve` |
| `%server_uptime%` | 本次实例已运行时长 | `4h 23m` |
| `%server_motd%` | MOTD 纯文本（已剥离 `§` 颜色代码） | `NeoServerStats Dev Server` |
| `%server_tps%` | 平滑 TPS，范围 `0.00 ~ 20.00` | `19.98` |
| `%server_mspt%` | 每 tick 平均毫秒（原版 100 tick 滚动窗口） | `12.43` |
| `%server_cpu_process%` | JVM 进程 CPU 占用 | `37.4%` |
| `%server_cpu_system%` | 宿主系统 CPU 占用 | `61.8%` |
| `%server_memory_used%` | JVM 堆已用 | `3.42 GiB` |
| `%server_memory_committed%` | JVM 堆已提交 | `4.00 GiB` |
| `%server_memory_max%` | JVM 最大堆（未定义则 `N/A`） | `11.84 GiB` |
| `%server_memory_percent%` | 已用堆占最大堆百分比 | `42.7%` |

## 玩家占位符（13 个）

以下占位符均需玩家上下文，否则返回 `N/A`。

| 占位符 | 说明 | 示例输出 |
| --- | --- | --- |
| `%player_name%` | 玩家账号名 | `Alice` |
| `%player_uuid%` | UUID（标准带连字符格式） | `069a79f4-44e9-4726-a5be-fca90e38aaf5` |
| `%player_ping%` | 延迟毫秒数（与原版玩家列表一致） | `43` |
| `%player_dimension%` | 所在维度 | `minecraft:overworld` |
| `%player_x%` / `%player_y%` / `%player_z%` | 坐标，固定两位小数 | `273.50` |
| `%player_deaths%` | 原版死亡统计 | `27` |
| `%player_playtime%` | 原版累计游戏时长，可读格式 | `4d 12h 35m` |
| `%player_playtime_ticks%` | 原版累计时长原始 ticks | `7812345` |
| `%player_playtime_seconds%` | 累计时长秒数 | `390617` |
| `%player_playtime_hours%` | 累计时长小时数，两位小数 | `108.58` |
| `%player_session_time%` | 本次登录会话时长 | `2h 17m 33s` |

---

## 格式与语义说明

### 时长格式

`%server_uptime%` 与 `%player_playtime%`：

| 时长 | 输出 |
| --- | --- |
| 不足 1 分钟 | `42s` |
| 不足 1 小时 | `18m 07s` |
| 不足 1 天 | `4h 23m` |
| 1 天及以上 | `3d 08h 15m` |

`%player_session_time%`（始终带秒）：`33s`、`17m 33s`、`2h 17m 33s`、`1d 02h 17m 33s`。

`%server_uptime%` 使用墙上时钟（`System.nanoTime()` 单调时钟）而非 tick 数，卡顿不影响真实时长；从不持久化，重启归零。

### TPS / MSPT

* 数据源是原版已维护的 tick 计时：`MinecraftServer#getAverageTickTimeNanos()`，即**最近 100 tick 的滚动平均**。本 mod 不添加 tick 监听器、不用 mixin、不自行采样。
* `%server_tps% = min(上限, 1000 / MSPT)`，因此两者**天然自洽**：50ms/tick 恰好 20.00 TPS，100ms/tick 为 10.00 TPS。
* 上限取配置的 tick rate（`/tick rate`，默认 20）并截断到 20。管理员执行 `/tick rate 5` 时会如实报告 `5.00`，而不是误导性的 `20.00`。
* 显示值始终截断在 `0.00 ~ 20.00`。
* 首个 tick 完成前两者返回 `N/A`（仅服务器启动瞬间）。

### CPU

* 数据源为 JDK 自带的 `com.sun.management.OperatingSystemMXBean`，**不引入 OSHI、不读 `/proc`、不调用系统命令**。
* `%server_cpu_process%` 按 JDK 定义**跨全部处理器归一化**：`100%` 表示所有核心都在跑 JVM 线程。`37.4%` 指占整机 CPU 能力的 37.4%，不是单核的 37.4%。
* `%server_cpu_system%` 是整台宿主机的占用。**容器环境下反映宿主机而非容器配额**，这是 JDK 的行为。
* 采样结果**缓存 1 秒**，同一秒内重复解析返回同一数值；缓存仅在有占位符被解析时才惰性刷新，**没有后台线程**。
* JVM 不提供该管理 bean 时两个占位符均返回 `N/A`，且只在启动时告警一次，不会刷屏。

### 内存

`%server_memory_*%` 报告的是 **JVM 堆**（`MemoryMXBean` / `MemoryUsage`），按需采样：

* `used` 是堆内已用，不是「系统空闲内存」。
* `committed` 是已提交给进程的堆，不是宿主机总内存。
* `max` 是 JVM 将尝试使用的最大堆；`percent = used / max * 100`。

**它们都不是宿主机/容器/系统级内存数据。**

### 回退为 `N/A` 的情况

无玩家上下文的所有 `%player_*%`；需要服务器但服务器实例不存在的 `%server_*%`；首个 tick 完成前的 TPS/MSPT；平台无法提供时的 CPU 指标；JVM 未定义最大堆时的 `memory_max` / `memory_percent`；玩家无活动会话时的 `session_time`。

回退字面量只有一处（`PlaceholderFallback`），无玩家上下文的规则只有一处（`PlayerPlaceholder#parseObject`）。

---

## 管理 / 调试命令

```text
/neoserverstats placeholder <text>
```

需要权限等级 2（管理员）。按真实消费方（ForgeMenus 等）的完全相同路径求值：

```text
/neoserverstats placeholder %server_online%/%server_max_players%
> %server_online%/%server_max_players% -> 0/20
```

由玩家执行时使用该玩家的上下文，`%player_*%` 可正常解析；由控制台或 RCON 执行时无玩家上下文，`%player_*%` 返回 `N/A`。

Forge PlaceholderAPI 自带的 `/placeholderapi` 只能列出命名空间描述、**无法求值**，因此本命令是确认占位符是否真正解析的官方手段。

---

## 构建与开发运行

使用项目自带的 Gradle wrapper，无需全局 Gradle。

```powershell
.\gradlew.bat clean build
.\gradlew.bat runServer
```

开发服务器运行在 `run/`（已被 git 忽略），首次运行需接受 EULA：

```text
run/eula.txt  ->  eula=true
```

Forge PlaceholderAPI 通过 `localRuntime` 自动加入开发运行，无需手动拷贝 jar。

---

## 代码结构

```text
src/main/java/io/github/davidblackcn/neoserverstatsplaceholder/
├── NeoServerStatsPlaceholder.java      入口、生命周期接线、启动自检
├── compat/ForgePlaceholderApiCompat    唯一接触 Forge PlaceholderAPI 的类
├── placeholder/
│   ├── PlaceholderRegistrar            唯一的注册点
│   ├── PlaceholderFallback             唯一的 "N/A" 字面量
│   ├── KeyedPlaceholder                基类：两条路径共用的键匹配
│   ├── ServerPlaceholder               基类：%server_*%（两条路径都应答）
│   ├── ServerInstancePlaceholder       追加「无服务器实例 → N/A」
│   ├── PlayerPlaceholder               基类：%player_*%（无上下文 → N/A）
│   └── Server*/Player*Placeholder      14 + 13 个具体占位符
├── metrics/                            运行时长、内存、TPS/MSPT、CPU 采样
├── player/                             原版统计读取、内存会话跟踪
├── format/                             时长、坐标、内存单位、数值/百分比格式
└── command/PlaceholderDebugCommand     调试命令
```

新增占位符 = 新增一个 `ServerPlaceholder` 或 `PlayerPlaceholder` 子类 + 在 `PlaceholderRegistrar` 中加一行注册。

---

## 许可证

[MIT](LICENSE)
