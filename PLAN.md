# NeoServerStats Placeholder — 0.2.0 实施计划

## 目标与固定环境

提供 Player / Server 常用占位符，保持 eCloud Player 2.0.9、Server 2.7.3 的键名和默认格式，并内置 NeoForge TAB / EasyBot 适配。只实现 README 列出的 74 个变量（30 server、44 player），不是完整 Bukkit PlaceholderAPI 或 eCloud JAR 加载器。

固定 Java 21、Minecraft 1.21.1、NeoForge 21.1.250，元数据 NeoForge 范围 `[21.1.250,21.2)`。标准专用服 mod JAR，mod id `neoserverstats_placeholder`，版本 `0.2.0`。必需依赖 Forge PlaceholderAPI 2.1.0 的 NeoForge 1.21.1 发行物；不引入 Bukkit、数据库、网络查询、客户端代码或新 mixin，不打包依赖 JAR。

## 兼容契约

- 其他模组须调用对应解析接口；相同变量名不构成跨 PAPI 实现的自动兼容。
- Forge PlaceholderAPI 使用真实 2.1.0 API 注册两个 namespace；消费者使用 `%server_key%` / `%player_key%`。
- TAB 5.2.1 的 `1.20.5 - 1.21.1` 发行物使用公开 registerServerPlaceholder / registerPlayerPlaceholder，在 ServerStarted 注册、TabLoadEvent 后恢复，刷新间隔 server 1000 ms / player 250 ms。
- EasyBot 0.3.3 的 `mc1.21+5` 发行物使用 IPlaceholderHandler 注册 player / server，仅移除已实现键的公开预映射。两个前缀注册成功前，处理器保持不可用；遇到前缀冲突，不删除任何预映射。
- 可选适配按 mod 元数据精确检查版本，其他版本跳过并告警一次。API 注册失败停用对应适配，FPAPI 核心继续运行。
- TAB 的公开 API 会覆盖同名注册，也自动创建未知变量默认实现；它没有公开 API 区分这两种提供器。因此本目录变量按明确所有权覆盖，不能承诺识别任意第三方同名冲突；README 明确要求避免争用。无需反射或内部 API 猜测。

## 变量范围与格式

精确列表、示例和兼容差异以 README 表格为契约。保留旧 27 个键并新增常用 Player 身份/位置/生命/状态/经验/延迟、Server 基础信息/堆/历史 TPS 键，以及 tps_current，合计 74。

- x/y/z 向下取整，负数也一致；x_long/y_long/z_long 为原始小数。
- ram_* 输出整数 MiB，free = committed - used，total = committed；memory_* 保留单位和百分比。
- tps 为彩色 1/5/15 分钟列表；tps_current 为两位小数单值，与 mspt 共用原版最近 100 tick 计时。
- 历史 TPS 使用 60/300/900 秒墙钟窗口，预热按已有时长，上限 20；固定 901 个秒级桶，边界按重叠比例插值。按默认整数舍入显示 .0；颜色按原始数值 >18 绿、>16 黄、其他红。
- uptime 为本次实例非零 w d h m s，零值 0s；不改变旧累计 playtime 格式。
- 布尔值 yes/no；ping >100 红、>50 黄、其他绿。
- world 为存档名和 _nether / _the_end 后缀；自定义维度资源 ID。world_type 为 Overworld / Nether / The End，其他 N/A。原版 biome 大写注册名，自定义 ID；capitalized 对原版分词首字母大写。
- 只支持真实在线玩家上下文，缺失/退出返回 N/A，不按姓名读取离线文件。未知键 null，交给消费者保留原样或其他提供器。
- deaths / playtime 只读原版统计；当前经验、原版 totalExperience、等级进度及升级总需求分别暴露。

首版排除装备、权限、离线历史、首次/最后登录、累计独立玩家、实体/区块总数、任意日期与倒计时。

## 实现结构

1. PlaceholderCatalog：唯一变量目录；PlaceholderSnapshots：原子发布不可变字符串映射和在线上下文映射。
2. RuntimeSnapshotService：服务器线程采集；玩家每 5 tick，全局/堆/CPU 约每秒。解析无 MC 状态访问或主线程等待。
3. PlayerSnapshotCollector / PlayerStatistics / ExperienceCalculator：玩家值、只读统计和经验规则；PlayerSessionTracker：UUID 登录时间内存表，退出移除、定期清理、停服清空。
4. MemoryMetrics / CpuMetrics / ServerPerformanceMetrics / RollingTpsMetrics：明确 JVM 堆、JDK 可见 CPU、原版计时与独立墙钟吞吐。计算集中在采集阶段。
5. CompatibilityFormatter / 原有 formatter：格式集中管理。
6. PlaceholderRegistrar / ForgePlaceholderApiCompat / TabCompat / EasyBotCompat：三入口共用快照。OptionalIntegrations 检查版本后才加载可选类。
7. Gradle compileOnly 编译可选发行物；下载任务固定官方 URL / SHA-256；运行参数显式添加可选 localRuntime。无 JAR 打包或提交。

## 执行和验收

按共用解析服务 → 简单变量 → 可选适配 → 历史 TPS → 完整验证推进，每阶段先编译。

- wrapper clean build；Java 21；检查发布 JAR 不包含依赖、验证模组或调试产物。
- 专用服四组合：FPAPI、FPAPI + TAB、FPAPI + EasyBot、三者同时安装。
- 74 个变量在线解析；无上下文/退出/重连/重启；负坐标、跨维度、经验等级边界、原版死亡/累计时长、未知变量。
- 真实 TAB 页眉页脚网络包与 reload；EasyBot 自身 replacePlaceholders 比对，预映射接管、缺失上下文、未知键。
- 模拟完整 60/300/900 秒窗口及预热/环绕/停顿；颜色边界；CPU 缓存/不可用回退；堆语义；并发读取。
- README 精确变量、格式迁移、版本、兼容差异；PLAN / AGENTS 纳入版本控制；详实验证记录见 VALIDATION.md。

## 当前进度

代码、依赖配置、四种专用服组合、故障隔离、自动测试和发布 JAR 检查均已完成；最终 wrapper clean build 通过。按 VALIDATION.md 的实际版本、路径与公开限制声明兼容。
