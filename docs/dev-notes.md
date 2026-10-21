# 开发笔记：PNG 动画播放器、测试外观与调试命令

**仅测试用**。把**一个**原版角色（默认：铁甲战士 IRONCLAD）在战斗中的身体绘制替换为精灵 PNG 逐帧动画。
卡牌、伤害、受击、死亡、复活、战斗结束全部沿用原版流程，本模组只改“画什么”。
不改怪物、不改其他角色、不替换游戏本体 JAR、不动存档。

- mod ID：`elfprototype`　版本：0.1.0　日志前缀：`[ElfPrototype]`（log4j logger 名 `ElfPrototype`）
- 依赖：ModTheSpire 3.30.x、BaseMod 5.56.0（运行时依赖仅 `basemod`）。**不依赖 StSLib**（没用到，未引入）。
- 资源命名空间：`elfprototype/…`（来自 `elf-windows-resource-pack-v1.zip`，原样复制）

## 1. 本机路径配置

路径不写在 pom 里。复制示例并按本机修改：

```
copy .mvn\maven.config.example .mvn\maven.config
```

```
-Dsts.home=D:/SteamLibrary/steamapps/common/SlayTheSpire          ← 含 desktop-1.0.jar 的游戏目录
-Dsts.workshop=D:/SteamLibrary/steamapps/workshop/content/646570  ← 创意工坊目录（含 1605060445\ModTheSpire.jar、1605833019\BaseMod.jar）
-gs
.mvn/global-settings.xml
```

最后两行让 Maven 使用工程内的空全局设置：本机 `C:\Program Files\Maven\apache-maven-3.9.8\conf\settings.xml`
有 XML 语法错误（`<mirrors>` 重复开标签，且镜像是已失效的 http 地址），不加这两行 Maven 无法启动。
修好该文件后可删掉这两行。`.mvn/maven.config` 已在 `.gitignore` 中。

## 2. 构建（只输出到 target，不碰游戏目录）

在工程根目录执行（`-gs` 是相对路径，必须在根目录运行）：

```
mvn package
```

产物：`target\ElfPrototypeMod.jar`。需要 JDK ≥ 9（用 `--release 8` 产出 Java 8 字节码，游戏自带 JRE 是 1.8.0_144）。
本机实际使用：Maven 3.9.8 + JDK 11.0.24（`JAVA_HOME=C:\Program Files\Java\jdk-11`）。
游戏本体、MTS、BaseMod 是 `system` 作用域的编译依赖，**不会**打进 JAR。

## 3. 显式部署 / 移除

```
powershell -ExecutionPolicy Bypass -File deploy.ps1          # 复制到 <sts.home>\mods\ElfPrototypeMod.jar 并打印 SHA256
powershell -ExecutionPolicy Bypass -File deploy.ps1 -Remove  # 从 mods 移除
```

## 4. 启动

先启动并登录 Steam。任选其一：

- Steam 库 → 杀戮尖塔 → 开始游戏 → 选 “Play With Mods” → 在 ModTheSpire 启动器勾选 **BaseMod** 与 **Elf Prototype (Test Skin)** → Play
- 脚本（用游戏自带 JRE，跳过启动器，stdout 存到 `logs\run-*.log`）：

```
powershell -ExecutionPolicy Bypass -File run-mts.ps1                    # basemod,elfprototype
powershell -ExecutionPolicy Bypass -File run-mts.ps1 -Mods basemod      # 基线：只有 BaseMod
powershell -ExecutionPolicy Bypass -File run-mts.ps1 -Vanilla           # 原版，无加载器
```

## 5. 测试角色、开关

| 项 | 默认 | 修改方式 |
|---|---|---|
| 是否启用外观 | 开 | 主菜单 Mods → Elf Prototype → 勾选框；或控制台 `elf on` / `elf off`（即时生效，原角色立即恢复） |
| 目标角色 | `IRONCLAD` | 控制台 `elf target THE_SILENT`（取值为 `AbstractPlayer.PlayerClass` 名） |
| 缩放 | 1.0 × `Settings.scale` | `elf scale 0.9` |
| 调试叠加层 | 关 | Mods 面板勾选框；或 `elf debug` |
| 攻击箭矢特效 | 开 | `elf arrows` 切换 |

配置文件路径在启动日志里打印（`[ElfPrototype] config file: …`）。

彻底关闭：在 ModTheSpire 启动器取消勾选本模组，或 `deploy.ps1 -Remove`。无需重装游戏、无需删存档。

## 6. 调试控制台（BaseMod，按 `` ` `` 键打开）

```
elf status              当前动作/帧号/死亡锁/纹理数
elf play <idle|attack|cast|guard|hit|death|victory>   单独预览一个动作
elf play attack_cursed  预览暗精灵双刃攻击（蚀面打出时的动作，无箭矢）
elf play idle_cursed    预览中间态待机
elf cursed              切换“手里有蚀面牌”：开=中间态待机，关=精灵待机（卡牌层接入前的手动开关）
elf reset               回到 idle（清除死亡锁；若玩家仍是死亡状态，下一帧会重新锁定）
elf debug               显示 动作名 [形态] / 帧号 / 画布框 / 红色十字=脚底锚点
```

形态切换（精灵 ↔ 中间态 ↔ 暗精灵）时，旧画面在 0.125 秒内淡出，不会硬切。
卡牌层接入后调用 `ElfSkin.setCursedInHand(boolean)` 和 `ElfSkin.onCursedFacePlayed(...)`，不再需要 `elf cursed`。

注意：`elf play death` 只是**预览**，不加死亡锁、可被打断，**不等于**真实死亡分支通过验收。

## 7. 实现说明（按本地 desktop-1.0.jar 12-18-2022 字节码核对）

| 表现 | 挂钩 | 策略 |
|---|---|---|
| 身体绘制 | `AbstractPlayer.render` 内两处：`renderPlayerImage(sb)`（骨骼）与 `sb.draw(img,…)`（`atlas==null` 或死亡后 `renderCorpse`） | 仅目标角色替换为精灵帧；血条、能量球、姿态、Hitbox 原样 |
| idle | 进入战斗（`OnStartBattleSubscriber`）、换房间、新玩家实例（新开一局）时重置 | 循环 |
| attack | `AbstractPlayer.useCard` 前缀，ATTACK 牌 | **每打出一张牌播一次**；多段攻击不按段数重播；伤害仍由动作队列结算。frame 4 `visual_hit` 发射一支纯视觉箭矢（每次播放只触发一次，跳帧也只一次）。目标：所选怪物，否则遍历第一个存活怪物（**不**调用 `getRandomMonster`，不消耗游戏 RNG） |
| guard / cast | 同上：有格挡的 SKILL → guard；其他 SKILL 和 POWER → cast | 播完回 idle |
| hit | `AbstractPlayer.damage` 前/后缀，比较 HP：下降且未死亡 | 连续受击从第 0 帧重播；hit 与 attack 互相打断（后到者优先）；播完回 idle，不会卡帧 |
| death | 每帧轮询 `player.isDead` | 原版只在 Mark of the Bloom / 妖精瓶 / 蜥蜴尾巴都不生效后才置 `isDead`，因此 HP 暂时为 0 但被复活时**不会**触发死亡。只触发一次、锁定、停末帧；之后的 hit/attack/调试 play 都被忽略。`isDead` 变回 false 或进入新局时解锁 |
| victory | `AbstractPlayer.onVictory` 后缀 | 按其 animation.json：`end_action=hold`，停末帧，换房间/下一场战斗时回 idle |

- 绘制：完整 512×512 帧，脚底轴点 (256,448)，`s = Settings.scale × scale`；
  `drawX = (player.drawX+animX) − 256·s`，`drawY = (player.drawY+animY) − (512−448)·s`，一次转换、一次缩放。
  `flipHorizontal` 时绕脚底镜像（轴点 x 用 512−256），箭矢发射点/方向同时镜像。
- 时间：`Gdx.graphics.getDeltaTime()` 推进，按 animation.json 的 fps，不按显示器帧率；同一渲染帧只推进一次。
- 混合/颜色：绘制前设 `GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA`（straight alpha），颜色乘角色 tint；绘制后恢复原混合函数与颜色。
- 纹理：PostInitialize 时一次性加载 144 张（约 144 MiB 显存 RGBA），进程内缓存、不随战斗重复分配。加载失败则记录错误并回退原版绘制，不崩溃。
  未使用 atlas/page_*.png（第一版按交接要求读完整帧）。

## 8. 自动化检查

`mvn package` 会运行 `src/test/java/elfprototype/ElfAnimatorTest.java`（12 项，读取工程内**真实** animation.json）：
元数据（144 帧、512 画布、轴点、fps、事件帧）、idle 在 144Hz 步长下仍按 24fps 循环、attack 回 idle 且事件只触发一次、
大步长跳帧事件仍只一次、重复攻击每次播放各触发一次、连续受击不粘连、hit 打断 attack、真实死亡只触发一次并停末帧且屏蔽后续请求、
复活/重置解锁、调试死亡预览不加锁、victory 遵循元数据、0/NaN/负 delta 忽略。

这些是**逻辑检查**，不代表游戏内表现。`tools/check_pivot.py` 是素材离线检查（`python -I tools/check_pivot.py src/main/resources`）。
