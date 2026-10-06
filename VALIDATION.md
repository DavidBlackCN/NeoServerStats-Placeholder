# 0.2.2 验证记录

日期：2026-10-06。Java 21 / Minecraft 1.21.1，新增 TAB 5.5.0 适配，保留 TAB 5.2.1 与 EasyBot 0.3.3 `mc1.21+5`。

## 发行物与 API

检查 [TAB 5.5.0 官方发行物](https://www.curseforge.com/minecraft/mc-mods/tab/files/7659430)，文件为 `TAB v5.5.0 1.20.5 - 1.21.1.jar`，4025747 字节，SHA-256：`abadbe8cf11b0736fdf3cdbac22a0518590acd118813983e99a6d16e9b94d985`。JAR 内 NeoForge mod 元数据为 `tab=5.5.0`，Minecraft 范围包括 1.21.1。

用实际 JAR 的 javap 核对 PlaceholderManager、EventBus、TabPlayer、TabLoadEvent；所用公开签名与 5.2.1 一致。实际 NeoForgePlatform 对未知变量仍注册默认实现，NeoForgeTAB 在 ServerStarting 初始化、ServerStopping 卸载，因此继续使用公开 API，在 ServerStarted 注册并监听 TabLoadEvent 恢复。新增精确允许版本 5.5.0，注册日志显示实际数量；其他未知版本仍跳过。

编译继续使用最低支持版本的 5.2.1 API；`-PwithTab=true` 默认选择固定 SHA-256 的 5.5.0，`-Ptab_version=5.2.1` 可回归旧版。依赖和开发验证工具不进入发布 JAR。

## 专用服验证

均将 21.1.249 编译的同一个 0.2.2 JAR 放入 `mods/`，关闭开发源码绑定，使用真实 Minecraft 1.21.1 协议玩家。

| NeoForge / 安装组合（均含本 mod） | 实际结果 |
| --- | --- |
| 21.1.249：仅 FPAPI | 启动、74 项在线解析和停服通过；未安装可选消费方安全 |
| 21.1.249：FPAPI + TAB 5.5.0 | 74 项注册、250 / 1000 ms 间隔、全部 74 项实际页眉页脚网络值、reload 前后输出及停服通过 |
| 21.1.249：FPAPI + EasyBot | 74 项自身解析入口与 FPAPI 比对、预映射接管、缺失上下文、未知键及停服通过 |
| 21.1.249：FPAPI + TAB 5.5.0 + EasyBot | 两个消费方同时工作；74 项比对与网络值、TAB reload 和停服通过 |
| 21.1.249：FPAPI + TAB 5.2.1 | 同一 JAR 的旧版回归通过；74 项注册与网络值、reload 和停服通过 |
| 21.1.250：FPAPI + TAB 5.5.0 + EasyBot | 同一份 21.1.249 编译的发布 JAR 回归通过；74 项消费入口比对和网络值、reload、停服正常；server_build 返回 21.1.250 |

TAB 使用 5.5.0 的 `header-footer.designs.default` 配置，包含用户报告的 `%server_tps_5_colored%`、`%server_ram_used%`、`%server_ram_max%`。刷新后的实际网络输出示例：

```text
NSS TPS: §e17.0 RAM: 409MB / 12128MB
```

注册和重新注册发生在 TAB 生命周期事件中；验证等待刷新后的完整输出，允许启动预热及重载期间的短暂中间包。EasyBot 沿用本地 ignoreError=true 验证自身解析入口；远端机器人消息和图形客户端截图仍未验证。

## 构建与发布

wrapper `clean build` 和 `clean build -Pneo_version=21.1.250` 均通过；每次执行 8 个 JUnit 测试方法，零失败/错误。两版编译生成的全部 32 个模组类逐字节一致。发布元数据为 0.2.2、NeoForge `[21.1.249,21.2)`，class major 65；JAR 不含依赖、第三方类、测试/验证模组、调试产物或 mixin。

产物 `build/libs/neoserverstats-placeholder-neoforge-1.21.1-0.2.2.jar`，54192 字节，SHA-256：`5c0587e0cf3d3318fd430a3398b64acc076fd07d26dbbf4181fe44a87a11010b`。原始 API 检查输出、协议客户端、验证模组及日志仅留在被忽略的开发目录；验证用服务器配置恢复。

---

# 0.2.1 验证记录（历史）

日期：2026-10-06。沿用下方 Java 21、Minecraft 1.21.1 和精确消费方发行物；新增 NeoForge 21.1.249，保留 21.1.250 兼容。

## 兼容改动

默认编译基线从 21.1.250 下移到 21.1.249；发布范围独立配置为 `[21.1.249,21.2)`。`-Pneo_version=21.1.250` 仅选择构建/运行环境，不会提高发布元数据的最低版本。占位符实现和依赖发行物保持原有行为，版本升为 0.2.1。GitHub Actions 增加两版构建矩阵。

## 本次实际验证

| 环境 | 结果 |
| --- | --- |
| 21.1.249：wrapper `clean build` | 成功；8 个测试方法，零失败/错误 |
| 21.1.250：wrapper `clean build -Pneo_version=21.1.250` | 成功；8 个测试方法，零失败/错误；元数据仍为 `[21.1.249,21.2)` |
| 21.1.249：FPAPI / FPAPI + TAB / FPAPI + EasyBot / 三者同时安装 | 四种专用服启动、在线玩家 74 个变量解析及正常停服均通过 |
| 21.1.249：TAB / EasyBot 消费入口 | TAB 检查全部注册和刷新间隔，reload 后重新验证；收到实际页眉页脚网络包。EasyBot 自身入口逐项与 FPAPI 比对一致，已支持的预映射移除、无玩家上下文返回 N/A、未知键保持原样 |
| 21.1.249：发布 JAR + FPAPI | 关闭开发源码绑定，将最终 JAR 放入 `mods/`；专用服启动、全部 74 项在线解析、server_build=21.1.249 和正常停服通过 |
| 21.1.250：21.1.249 编译的发布 JAR + FPAPI + TAB + EasyBot | 将同一个 JAR 放入 `mods/` 并关闭开发源码绑定；启动、74 项消费入口比对、TAB reload、页眉页脚网络包和停服均通过；server_build 正确返回 21.1.250 |

两版编译生成的 32 个模组类逐字节一致。最终再次执行默认 21.1.249 的 wrapper `clean build`。发布 JAR 为 Java class major 65，版本 0.2.1、NeoForge 范围 `[21.1.249,21.2)`，不含依赖 JAR、第三方类、测试/验证模组或 mixin。大小：53999 字节；SHA-256：`1b4e135bba650cb653c543531b2f6e39114baf5fc53f566acdef53a148dead66`。

本次复验主要覆盖新增加载器版本的构建、加载、核心变量和可选适配；下方 0.2.0 的详细语义与故障隔离验证作为历史记录保留。远端 EasyBot 机器人消息、图形客户端显示及其他 NeoForge 版本未验证；本地 EasyBot 仍使用 `ignoreError=true` 测试解析入口。

---

# 0.2.0 验证记录（历史）

日期：2026-10-06。环境：Windows 11、Eclipse Temurin Java 21.0.10、Minecraft 1.21.1、NeoForge 21.1.250、Gradle wrapper 9.2.1。仅支持专用服。

## 精确依赖

| 组件 | 验证发行物 |
| --- | --- |
| Forge PlaceholderAPI | com.envyful.papi:neo21:2.1.0；实际 mod 元数据为 2.0.5 |
| TAB | 5.2.1，CurseForge 文件 6556999，`TAB v5.2.1 1.20.5 - 1.21.1.jar` |
| EasyBot | 0.3.3，`easybot-neoforge-0.3.3+mc1.21+5.jar` |

下载地址和 SHA-256 固定于 `gradle/compat-dependencies.gradle`。通过发行 JAR 的类/字节码检查真实 API，而非旧版本示例。

## 专用服组合

| 安装组合（均包含本 mod） | 实际结果 |
| --- | --- |
| FPAPI | 启动、30 server / 44 player 注册、在线 74 项逐一解析成功；可选类缺失不会崩溃 |
| FPAPI + TAB | 启动、74 项注册、250 / 1000 ms 刷新间隔检查、真实页眉页脚网络包、reload 后恢复注册 |
| FPAPI + EasyBot | 启动，EasyBot 自身 replacePlaceholders 入口逐项与 FPAPI 比对 74 项一致 |
| FPAPI + TAB + EasyBot | 启动，两适配同时生效，74 项比对及 TAB reload；最终停服生命周期复验 |

测试玩家使用本地 Minecraft 1.21.1 协议连接专用服，是真实在线 ServerPlayer。使用控制台/RCON 与仅用于开发的验证模组调用真实消费接口。验证模组、测试客户端、日志、下载 JAR 全部留在被忽略的 run / .devtools / build 目录，不进入源码主集或发布 JAR。

TAB 实际发出的页眉页脚示例（客户端网络包中的字符串）：

```text
NSS NssTester | -1 | 20.0 | world_nether
NSS NeoForge | 1 | §a20.0§7, §a20.0§7, §a20.0 | 338
```

TAB 最初停服复验发现注册表可能先被 TAB 销毁的问题；实现已改为停用回调和解绑事件监听，由 TAB 清理其注册表，并重新进行联合启动/reload/停服验证。

## 变量与数据

- 在线 74 项均可解析；EasyBot 输入错误的 playerName 时，player_name 仍来自传入的真实在线玩家。
- EasyBot 支持键对应的预映射全部移除；44 个玩家键缺少上下文均返回 N/A；未知 player 键保持原样。
- FPAPI 控制台上下文返回玩家 N/A；本 mod 的 evaluatePlaceholders 包装器直接传 null 的 44 项也安全返回 N/A。
- 负坐标 -0.1 / -12.9 输出 -1 / -13；对应 _long 输出原始值。
- 主世界、下界、末地实际切换后，world / world_type / dimension 结果一致；自定义维度分支经代码审查，未安装自定义维度数据包实测。
- 等级 17、16 当前等级经验点：exp=0.34042552、current_exp=410、total_exp=0、exp_to_level=47，确认没有混用。另用自动测试验证 0 / 15 / 16 / 17 / 30 / 31 / 32 级边界。
- 实际死亡与自动重生后 deaths 从 0 变 1；停止时原版 stats 文件也为 deaths=1。只读取原版数据，不另存计数。
- 退出后 server_online=0、列表为空；重连和服务器重启后 session_time 重新计时，累计 playtime_ticks 持续增长，原版 stats 文件保留数据。
- JVM 堆：ram_total=committed、ram_free=committed-used，全部整数 MiB；截断可差 1 MiB；memory_* 保留单位与百分比。
- 原版 /tick rate 5 后 tps_current=5.00，历史窗口分别为不同数值（例如 14.0 / 17.0 / 17.0）；恢复 20 后 current=20.00。Windows 的进程/运行环境 CPU 实际采样有效。

## 自动测试

wrapper test / clean build 执行 JUnit：8 个测试方法，零失败。包括负坐标、布尔/时长/颜色边界、自定义 biome ID 和 N/A 格式、经验等级边界、快照不可变性、异步读与发布并发、会话退出/残留清理/重连、CPU 不可用/NaN/无穷/缓存。

历史 TPS 测试用受控时钟序列模拟 2000 秒，包含真实 60 / 300 / 900 秒独立窗口、启动预热、20 → 10 TPS 切换、半秒窗口边界插值、环绕、长停顿、重启及显示上限。15 分钟完整时间行为通过模拟验证，未在真实专用服连续施加载荷运行 15 分钟。

## 故障隔离

- 测试模组提前占用 EasyBot 的 server 前缀，迫使第二个注册失败：原预映射表完整保留，部分 player 处理器未激活，FPAPI 正常；null 上下文也通过验证。
- 使用官方 TAB JAR 仅修改元数据版本为 0.0.0 的测试副本：TAB 本身加载，但本 mod 的 TAB 适配跳过并仅告警一次；FPAPI 正常。此副本不能代表对其他实际 TAB 版本的兼容。
- TAB 公开注册接口不会报告同名提供器来源，会覆盖注册；无法保证对任意第三方同名提供器的冲突检测。此限制和目录所有权已写入 README / PLAN。

## 公开验证边界

EasyBot 未连接远端主程序，默认 ignoreError=false 会在登录检查时拒绝测试玩家。本地测试使用 EasyBot 自带 ignoreError=true 允许登录，验证其真实占位符解析入口；远端机器人、群聊消息、账号绑定/黑白名单等外部链路没有验证，也不由本 mod 实现。EasyBot 自己的数据库和网络行为不属于本 mod 的依赖或采集实现。

测试客户端对 NeoForge 的部分附加配方包存在解析提示，但登录、移动、经验、死亡重生和 TAB 页眉页脚接收实际成功；未进行图形客户端截图验证。颜色阈值通过边界测试与实际 § 代码包验证。

## 发布检查

最终 wrapper clean build 成功（8 个测试方法、零失败）；JAR 检查通过：只包含本 mod 类与 NeoForge 元数据；Java class major 65；不包含 FPAPI / TAB / EasyBot / Bukkit、测试类、验证模组、依赖 JAR 或 mixin 配置。产物路径见 README。

发布 JAR 大小：53997 字节；SHA-256：`7f5fb55e6d6d6a7351fe95784d7078e6a43d249d80b2c4ff7c1b2db0840a5f23`。
