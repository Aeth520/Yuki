<div align="center">

# Yuki

**一款基于 PacketEvents 的高性能 Minecraft 反作弊插件**

[English](./README_EN.md) | 简体中文

</div>

## 简介

Yuki 是一款基于 GrimAC 二次开发的 Minecraft 反作弊插件，融合了 Matrix、Vulcan、Medusa、Karhu、Hawk、GrimAC、Raven 等多种反作弊的检测理念。插件采用预测引擎架构，通过模拟客户端移动来精确检测各类作弊行为。

## 特性

- **预测引擎**：完整模拟客户端移动、物理、流体、载具等行为
- **丰富检查**：覆盖移动、战斗、玩家、搭建、聊天、MultiActions 等六大类共 200+ 项检查
- **多版本支持**：支持 1.13 - 1.21+ 服务端，兼容 ViaVersion 协议转换
- **多语言**：内置中文和英文两种语言包
- **平台抽象**：平台无关的 API 层（AntiCheatUser、EventBus），便于扩展到 Velocity 等代理端
- **存储抽象**：支持 SQLite、MySQL、H2、MongoDB，可通过 BackendRegistry 注册自定义后端
- **Feature Flags**：运行时动态开关各类检查和功能
- **Discord Webhook**：违规警报推送至 Discord
- **诊断 Dump**：导出插件运行状态为 JSON，便于排障
- **性能监控**：实时 TPS、MSPT、检查耗时统计

## 环境要求

| 依赖 | 版本 |
|------|------|
| Java | 21+ |
| Minecraft | 1.13+ |
| 服务端 | Paper / Spigot（推荐 Paper） |
| PacketEvents | 内置（2.13.0） |

## 安装

1. 从 [Releases](https://github.com/Aeth520/Yuki/releases) 下载最新 `Yuki-<version>.jar`
2. 将 jar 文件放入服务器的 `plugins/` 目录
3. 启动服务器，插件会自动生成配置文件
4. 根据需要修改 `plugins/Yuki/` 下的配置文件

## 命令

主命令：`/yuki`（别名 `/yk`）

| 子命令 | 说明 | 权限 |
|--------|------|------|
| `help` | 查看帮助 | `yuki.commands` |
| `reload` | 重载配置 | `yuki.commands` |
| `alerts` | 切换警报显示 | `yuki.commands.alert` |
| `spectate` / `spec` | 旁观玩家 | `yuki.commands.spectate` |
| `stopspectate` / `stopspec` | 停止旁观 | `yuki.commands.stopspectating` |
| `freeze` | 冻结玩家 | `yuki.commands.freeze` |
| `unfreeze` | 解冻玩家 | `yuki.commands.freeze` |
| `setback` | 强制回弹玩家 | `yuki.commands.setback` |
| `mitigate` | 缓解措施管理 | `yuki.commands.mitigate` |
| `verbose` | 详细信息切换 | `yuki.commands` |
| `perf` / `benchmark` | 性能监控统计 | `yuki.commands` |
| `debug` | 调试模式 | `yuki.commands` |
| `profile` | 玩家档案 | `yuki.commands` |
| `log` / `logs` | 违规日志查询 | `yuki.commands` |
| `history` / `hist` | 历史记录 | `yuki.commands` |
| `dump` | 导出诊断 Dump | `yuki.commands.dump` |
| `features` | 功能开关管理 | `yuki.commands.features` |
| `discordtest` | 测试 Discord Webhook | `yuki.commands` |
| `crash` | 崩溃玩家客户端 | `yuki.commands` |
| `decrypt` | 解密操作 | `yuki.commands.decrypt` |

## 权限

| 权限节点 | 说明 | 默认 |
|----------|------|------|
| `yuki.staff` | 员工权限（包含下列所有子权限） | op |
| `yuki.commands` | 基础命令权限 | op |
| `yuki.exempt` | 豁免所有检查 | op |
| `yuki.exempt.cancel` | 豁免事件取消 | op |
| `yuki.exempt.setback` | 豁免回弹 | op |
| `yuki.exempt.highpingkick` | 豁免高延迟踢出 | op |
| `yuki.antiplugin` | 插件列表伪装 | op |
| `yuki.brand` | 查看客户端品牌 | op |

## 检查类别

### 移动检查（Movement）
- **Elytra**（鞘翅）：A-K，检测飞行作弊
- **GroundSpoof**（地面欺骗）：A-C
- **NoSlow**（无减速）：A-G
- **Sprint**（疾跑）：A
- **Vehicle**（载具）：Fly A-B、NoSaddle A-B
- **MovementValidation**（移动验证）

### 战斗检查（Combat）
- **Aim**（瞄准）：A-W（23 项），含统计分析 Analysis A-H
- **KillAura**（杀戮光环）：A-M
- **Reach**（攻击距离）：A-E
- **Velocity**（击退）：A-F
- **AutoBlock**（自动格挡）：A-G

### 玩家检查（Player）
- **AutoClicker**（自动点击）：A-T
- **BadPackets**（异常数据包）：A-AA
- **Scaffold**（自动搭桥）：A-K
- **Timer**（计时器）：A-AA
- **Crash**（崩溃）：A-M
- **Inventory**（背包）：A-N
- **PingSpoof**（延迟欺骗）：A-F
- **Baritone**（路径寻找）：A-D
- **FastBreak** / **FastPlace** / **FarBreak** 等破坏/放置检查
- **MultiActions**（多动作）：A-G
- **Exploit**（漏洞利用）：A-G

### 杂项检查（Misc）
- **Chat**（聊天）：A-D
- **Spam**（刷屏）：A-B
- **Client**（客户端）：A
- **GhostBlock**（幽灵方块）缓解
- **Visual**（视觉）：装备/元数据隐藏

## 配置

配置文件位于 `plugins/Yuki/` 目录下：

| 文件 | 说明 |
|------|------|
| `settings.yml` | 主配置（语言、输出、功能开关、缓解措施等） |
| `check.yml` | 检查参数配置 |
| `punishments.yml` | 惩罚配置 |
| `messages.yml` | 消息文本 |
| `database.yml` | 数据库配置 |
| `models/analysis-h.json` | AnalysisH ML 模型权重（外部加载） |

### 数据库配置

```yaml
# database.yml
data-type: sqlite  # sqlite | mysql | h2 | mongodb
```

- **SQLite**：默认，无需额外配置
- **MySQL/MariaDB**：配置 host/port/database/username/password
- **H2**：嵌入式数据库
- **MongoDB**：文档型数据库

### 功能开关

```yaml
# settings.yml
features:
  checks:
    movement: true
    combat: true
    scaffold: true
    # ...
  performance:
    monitor: true
  discord:
    webhook: false
  dump:
    diagnostic: true
```

也可通过 `/yuki features` 命令运行时切换。

## 可选 Hook 插件

| 插件 | 功能 |
|------|------|
| ViaVersion / ViaBackwards / ViaRewind | 多版本协议支持 |
| Geyser-Spigot | 基岩版玩家支持 |
| Floodgate | 基岩版账号管理 |
| PlaceholderAPI | 变量替换 |
| MythicMobs | 怪物兼容 |
| MyPet | 宠物兼容 |

## 构建

```bash
git clone https://github.com/Aeth520/Yuki.git
cd Yuki
./gradlew build
```

构建产物：`build/libs/Yuki-<version>.jar`

### 自定义数据库驱动打包

默认仅打包 SQLite 驱动，如需其他驱动：

```bash
./gradlew build -PdbDrivers=mysql,mongodb
```

## 技术架构

- **Java 21** + **Gradle**（Shadow 插件）
- **PacketEvents 2.13.0**：数据包监听与协议处理
- **Kyori Adventure**：文本组件处理
- **ORMLite**：ORM 映射
- **HikariCP**：连接池
- **Manifold**：编译期处理器
- **Lombok**：代码简化

## 致谢

- [GrimAC](https://github.com/GrimAnticheat/Grim) - 预测引擎基础架构
- [PacketEvents](https://github.com/retrooper/packetevents) - 数据包事件框架
- Matrix / Vulcan / Medusa / Karhu / Hawk / Raven - 检测理念参考

## 许可证

本项目仅供学习和研究使用。

## 链接

- [GitHub 仓库](https://github.com/Aeth520/Yuki)
- [问题反馈](https://github.com/Aeth520/Yuki/issues)
