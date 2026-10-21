# 联调记录 — 2026-10-20（Windows，首轮）

## 环境（实查）

| 项 | 值 |
|---|---|
| Steam 库 | `C:\Program Files (x86)\Steam`（根）、`D:\SteamLibrary`（游戏所在） |
| 游戏 | `D:\SteamLibrary\steamapps\common\SlayTheSpire`，desktop-1.0.jar 版本 `2022-12-18` |
| 游戏运行时 | 自带 `jre\bin\java.exe` 1.8.0_144 |
| ModTheSpire | 3.30.3，`…\workshop\content\646570\1605060445\ModTheSpire.jar` |
| BaseMod | 5.56.0，`…\646570\1605833019\BaseMod.jar` |
| StSLib | 有（`1609158507`），本模组未使用 |
| 编译 | Maven 3.9.8 + JDK 11.0.24，`--release 8`（另有 JDK 21 未用） |
| 显卡 | NVIDIA RTX 3060 Laptop、AMD Radeon 核显、**GameViewer Virtual Display Adapter** |
| 资源包 | SHA256 `E66F58A8…1B00`，与交接值一致；解压到 `D:\StSDev\ElfWindowsResources`（原样保留） |
| 测试前备份 | 已备份游戏的 saves 和 preferences 目录 |

测试前确认没有进行中的铁甲战士存档，所以默认测试角色设为 IRONCLAD 不会覆盖进度。
测试局死亡会在 `runs\2_IRONCLAD\` 新增一条历史并更新统计，属正常游戏写入。

## 执行与结果

| # | 命令 | 结果 |
|---|---|---|
| 1 | `Get-FileHash` 资源 ZIP | 一致 |
| 2 | `mvn package` | **退出码 0**；12/12 测试通过；`target\ElfPrototypeMod.jar` |
| 3 | JAR 检查 | ModTheSpire.json 已正确展开；219 张 PNG 与原包**逐字节一致**；无 `src/main/resources` 前缀；无游戏/BaseMod 类；字节码 major 52（Java 8） |
| 4 | `run-mts.ps1 -Vanilla`（沙箱内、沙箱外各一次） | **失败**：`GdxRuntimeException: OpenGL is not supported by the video driver` / `LWJGLException: Pixel format not accelerated`（`logs\01-vanilla.log.err`）。原版本体都无法创建 GL 窗口，与模组无关 |
| 5 | MTS `--skip-launcher --mods basemod,elfprototype --out-jar` | **成功**：识别 `elfprototype (0.1.0)` 与 `basemod (5.56.0)`；补丁注入并编译通过；日志 `patched AbstractPlayer.render -> corpse/img draw (#1)` / `renderPlayerImage (#2)`（`logs\02-mts-outjar.log`）。stderr 只有 `Could not connect to Steam`（当时 Steam 未运行） |
| 6 | `javap` 补丁后的 AbstractPlayer（`logs\desktop-1.0-patched.jar`） | render 两处 `ElfSkin.appliesTo/render`、useCard 前缀、damage 前/后缀、onVictory 后缀均存在 |
| 7 | `tools/check_pivot.py` | 7 个动作每帧最低不透明行 = 447（紧贴轴点 448）；attack 个别帧到 463、death 到 456（脚底以下有部件，疑为弓梢/披风，需游戏内看） |
| 8 | `deploy.ps1` | 已部署到 `…\SlayTheSpire\mods\ElfPrototypeMod.jar`，SHA256 `1CA005C3…80FF` |

**结论：编译成功 ✔；MTS 识别与补丁注入成功 ✔；浏览器预览本轮未执行；游戏内实测 ✘ 未进行（本机当前显示环境无法创建 OpenGL 窗口）。**

## 阻塞项：OpenGL 窗口创建失败（需要用户处理）

`Pixel format not accelerated` 通常出现在通过远程桌面/虚拟显示器访问时。本机有 GameViewer 虚拟显示适配器，
推测当前会话的输出走的是这块虚拟显卡。请任选其一后，自己先确认**原版**能进主菜单：

1. 在本机物理屏幕前操作（断开 GameViewer 远程），或在 GameViewer 里切换到使用物理显卡/物理显示器的模式；
2. 或在 Windows 显示设置中把笔记本内屏设为主显示器 / 暂时禁用 “GameViewer Virtual Display Adapter”；
3. 若仍失败，在 NVIDIA 控制面板把 `SlayTheSpire\jre\bin\java.exe` 设为“高性能 NVIDIA 处理器”。

## 请用户执行的游戏内验证步骤（用 `run-mts.ps1` 启动时日志自动存到 `logs\run-*.log`，回传该文件和截图）

0. 启动 Steam。`powershell -ExecutionPolicy Bypass -File run-mts.ps1 -Mods basemod` → 能进主菜单即“仅基础依赖”通过，退出。
1. `powershell -ExecutionPolicy Bypass -File run-mts.ps1` → 主菜单。日志里应有
   `[ElfPrototype] loaded 144 frames (144 textures created, ~144 MiB RGBA) in … ms`，且无 `ERROR`。
2. 开始 → 标准模式 → **铁甲战士** → 出发；涅奥事件随便选，进第一层地图的普通怪房间。
   - 预期：战斗中原铁甲战士位置显示精灵、循环待机；血条/能量/格挡图标照常。按 `` ` `` 输入 `elf debug`，红十字应在脚底。截图。
3. 打出“打击”：播放攻击一次→回待机，第 4 帧附近射出一支箭；伤害数字正常、只结算一次。日志 `play attack <- card Strike_R` 与 `event attack.visual_hit` 各一次。
   打出“防御” → guard；打出“痛击” → attack。
4. 结束回合，怪物攻击：hit 一次→回待机。连续多次受击（多段攻击怪）不应卡在 hit。
5. `elf play cast` / `guard` / `victory` / `death` 各预览一次（death 预览后再打一张牌应能恢复正常）。
6. 拖动窗口大小或在设置里改分辨率：精灵仍站在原角色位置。
7. 打赢 → victory 停末帧；进入下一场战斗 → 重置为待机。重复进 3~4 场战斗后 `elf status` 的 `textures=` 应仍为 144。
8. 复活分支：控制台 `relic add` + Tab 选 Lizard Tail，再 `hp lose 999`
   → 预期**不**播放死亡、不出现 `REAL death` 日志，HP 恢复 50%。（妖精瓶：`potion` + Tab 放入 FairyPotion 后同测。）
9. 真实死亡：`hp lose 999`（无复活手段）→ 播放 death 并停末帧，日志 `REAL death -> death clip` 只出现一次；
   之后 `elf play idle` 应提示 `ignored: real death lock active`。记录死亡界面是否遮挡、遮挡时机。
10. 回主菜单新开一局 → 待机正常（`reset: new player instance`）。
11. `elf off` → 立刻恢复原铁甲战士骨骼动画；`elf on` 恢复精灵。退出后在启动器取消勾选本模组 → 原版正常。

## 验收清单

- [x] Maven 构建退出码 0，产出 JAR，含模组元数据与实际 PNG，资源路径无重复前缀
- [~] ModTheSpire 识别测试模组、补丁注入成功 ✔；进入主菜单与战斗 —— **未验证**（GL 窗口无法创建）
- [ ] 透明边缘、缩放、脚底位置在实际战斗中正常 —— 未验证
- [~] 待机/攻击回退/连续受击/打断/死亡锁 —— 逻辑单测通过；游戏内未验证
- [ ] 死亡调试预览 与 真实死亡、复活分支、新战斗重置 —— 未验证（逻辑单测已分开覆盖）
- [ ] 分辨率/窗口变化、多次战斗后纹理不增长 —— 未验证（设计上 144 张只加载一次）
- [ ] 关闭模组恢复正常游戏 —— 未验证（设计上 `elf off` / 取消勾选 / `deploy.ps1 -Remove` 均可）

## 完成 / 待验证 / 未实现

**已完成（代码 + 构建 + 逻辑测试）**：工程骨架、本地路径配置、显式部署/启动脚本；资源复制；完整帧 PNG 播放器
（idle/attack/cast/guard/hit/death/victory 七个动作全部接入）；脚底锚点绘制与镜像；delta-time 推进；纹理一次加载缓存；
SpriteBatch 状态恢复；攻击/受击/真实死亡/胜利挂钩；死亡锁与复活/新战斗/换房间/新局重置；箭矢视觉事件（每次播放一次）；
`elf` 控制台命令、Mods 面板开关、调试叠加层。

**待验证（全部需要游戏内）**：上方清单中未勾选的项；cast/guard 的触发规则（有格挡 SKILL→guard，其他 SKILL/POWER→cast）
是本轮自定的临时表现规则，不是已接受需求；缩放 1.0 是否与原角色体型匹配；箭矢发射点 (+150, +228 px) 是否对准弓。

**未实现**：
- 三张演示卡（蓄力射击、林叶护持、月辉觉醒）及其机制——费用/效果未定稿，本轮未写
- 肩像、选人图、能量球、卡框等 UI 接入（需独立角色才用得上）
- 世界树之酿（药水）、世界树的祝福（遗物）——仅有 PNG 与设计文档，无游戏实现
- optional/duality 三形态候选——未接入，未混用
- 其他死亡动画——只有一套“跪倒败北”；未做多死亡片段选择
- atlas/page_*.png 图集加载（显存优化）——未做，当前读完整帧

## 第二轮：三形态表现素材（2026-10-20）

依据设计：取消全局形态，三种形态只做表现。手里没有蚀面牌时显示精灵待机，有蚀面牌时显示中间态待机，打出蚀面时播放暗精灵双刃攻击。

| 项 | 结果 |
|---|---|
| 蚀面卡框 / 能量球 | `tools/make_cursed_art.py` 由现有绿金卡框调色生成，`images/character/cardback_cursed/`（新增，未覆盖原文件） |
| 蚀面占位卡图 | 3 张演示卡和 default 的 `*_cursed[_p].png`（新增）；仅占位，正式图待远端出图 |
| 对齐 | 三形态候选与基础精灵同身高（319 / 318 px），只平移：暗 (+27, +10)，中间态 (+53, +10)，脚底都在 (256, 448) |
| 暗精灵双刃攻击 | 用户选定候选 C（源帧 42–62 中的 12 帧，0.5 秒，命中帧 5 = 0.21 秒）；画布 576×512（右侧加宽 64 px 保留刀尖），`animations/dark/attack/` |
| 中间态待机 | 源帧 0–36 隔帧正放加倒放，36 帧 / 12fps / 3 秒循环，`animations/intermediate/idle/` |
| 代码 | ClipData 增加 `form`；动画机增加“蚀面在手”待机切换；形态变化时 0.125 秒淡出；`elf cursed` 手动开关 |
| `mvn package` | **退出码 0**；18/18 单测通过（原 12 项 + 新 6 项）；字节码 major 52 |
| 显存估算 | 完整帧约 193.5 MiB（原 144） |
| 部署 | **本轮未部署**到游戏 `mods\`（用户已决定暂缓游戏内测试） |
| 游戏内 | **未验证**：淡出效果、双刃攻击位置与节奏、中间态待机观感 |

审阅用预览在 `art-review\`：`dark_attack_compare.gif`、`dark_attack_filmstrips.png`、`intermediate_idle.gif`、`cursed_preview.png`、`alignment_overlay.png`。
重新生成：`python tools/cut_form_clips.py <ElfWindowsResources 目录>`（只写预览）；加 `--export C_snap` 写运行时片段（目标目录已存在时拒绝覆盖）。

## 第三轮：美术 B1 起始包（2026-10-20）

远端：RTX 4090，工作台自带的 ComfyUI + Qwen Image 2.1（25 步、CFG 3.0、负面提示词为空），输出在 `/workspace/sts1-art-pipeline/elf-b1/`。

| 项 | 结果 |
|---|---|
| 出图 | 第 1 轮 32 张；修正试验 60 张；第 2 轮 16 张；刀数修正 16 张。每张约 9–13 秒，显存峰值约 23 GB |
| 选定 | 12 张，用户逐项挑选，记录在远端 `elf-b1/selection.json`；本地 `art-inbox\B1\final\`，哈希与远端一致 |
| 导入 | `tools/import_art.py` 生成 25 个文件：卡图 500×380 / 250×190，遗物 128 / 256 / 描边，图标 84 / 32；清单见 `art-review/b1_import.json` |
| 替换 | 蓄力射击、林叶护持的蚀面调色占位图被正式图替换，旧文件备份在 `art-review/replaced/20261020-174706/` |
| `mvn package` | 退出码 0，18/18 单测通过，新资源已打进 JAR |
| 游戏内 | **未验证**：独立角色和卡牌代码尚未实现，这些图暂时还没有地方显示 |

出图经验（已写入 `tools/build_b1_jobs.py`）：
- 蚀面只用光面一张图改图，暗精灵造型写在文字里。加入暗精灵全身设定图，会把近景拉成全身远景。
- 每张卡的画面描述必须写具体场景，否则会沿用白底画布。
- 刀光写成光痕，并写明"全图只有两把刀"。负面提示词不加：加了"多余的刀"反而会去掉第二把刀。

## 第四轮：美术 B2–B5 全量（2026-10-20）

用户授权“一次性出完、能决定的由我决定”。远端输出 `/workspace/sts1-art-pipeline/elf-art/`，任务清单 `docs/art-pipeline/art-all-jobs.json`（`tools/build_all_jobs.py` 生成）。

| 项 | 结果 |
|---|---|
| 第 1 轮 | 53 张双面卡光面、12 张能力牌卡图、17 个能力图标、8 件遗物，每项 4 候选，共 360 张，0 报错 |
| 补出 | 蔓延（背景空白）4 张；守望、余韵图标（误画成人物，去掉参考图重出）8 张 |
| 第 2 轮 | 53 张蚀面，以选定光面为输入改图，每项 3 候选，共 159 张，0 报错 |
| 选图 | `tools/auto_pick.py` 初排 + 逐张目检；人工改选 13 处（多刀、画面跑题、图标画成人物），选择记录 `docs/art-pipeline/art-all-selection-r1.json` / `-r2.json` |
| 抠图 | 25 个图标/遗物用 birefnet |
| 导入 | 294 个文件；设计稿 82 个 ID 全部有图（`art-review/all_cards_overview.png`、`all_icons_overview.png`） |
| `mvn package` | 退出码 0，18/18；JAR 约 77 MB |
| 游戏内 | **未验证**（卡牌代码未写） |

注：`art-review/replaced/20261020-193414/` 是二次导入时备份的本轮自己生成的文件，可删除。

## 第五轮：独立角色 + 核心机制 + 起始牌组（2026-10-20）

| 项 | 实现 |
|---|---|
| 角色 | `character/ElfCharacter`（CustomPlayer，70 生命，99 金币，3 能量，抽 5），卡牌颜色 `ELF_GREEN`，选人按钮、立绘、能量球、卡框用资源包现有素材；战斗身体仍由 ElfSkin 画（精灵角色始终启用，不受测试皮肤开关影响） |
| 双面卡 | `cards/DualFaceCard`：两面各自的名称、描述、数值、目标、卡图和卡框（蚀面用 `cardback_cursed/`）；升级同时作用于两面；战斗用的牌是牌组副本，副本不继承蚀面，所以每场战斗都从光面开始 |
| 侵蚀 / 封印 | `mechanics/FlipHandAction`：从手牌选 N 张（不足 N 张时全部翻面），打出中的牌不在手牌里，所以天然满足“其他牌” |
| 自动封印 | 蚀面打出时设置 `returnToHand` 并排一个 `SealSelfAction`，在伤害结算后、回手之前翻回光面；`patches/ReturnToHandPatch` 在动作结束后清掉标记。手牌满 10 张时改为进弃牌堆 |
| 咒鸣 | `mechanics/ChimePower`：每次翻面 +1，我方精灵卡牌每段伤害和获得的格挡 + 层数（卡面数字实时变化），回合结束移除 |
| 反噬 | `DualFaceCard.triggerOnEndOfPlayerTurn`：回合结束时手中每张蚀面失去 2 生命（不受格挡影响） |
| 起始牌组 | 打击 ×4、防御 ×4、咒之低语、单刃斩，数值按设计稿 v4 |
| 起始遗物 | 裂纹封印：每回合第一次封印（含自动封印）抽 1 张 |
| 动画 | 手中有蚀面 → 中间态待机；打出蚀面攻击 → 暗精灵双刃斩 |
| 文本 | 简体中文和英文（`localization/zhs`、`eng`），关键词：侵蚀、封印、翻面、咒鸣、反噬；蚀面卡另有悬浮提示 |
| `mvn package` | 退出码 0；22/22 单测（新增 4 项：面数值与升级、两种语言的文本齐全、卡图与遗物图齐全） |
| ModTheSpire 补丁 | `--out-jar` 注入成功：`ELF` 枚举存在，`UseCardAction.update` 含 `ReturnToHandPatch`，渲染补丁两处（`logs/03-mts-outjar-character.log`，补丁后 JAR 移到 `logs/desktop-1.0-patched-character.jar`）。进程写完后未自动退出，已手动结束 |
| 部署 | 已覆盖部署到 `mods\ElfPrototypeMod.jar`（SHA256 前缀 50fe57bf） |
| 游戏内 | **未验证**（用户暂缓可视化测试） |

### 游戏内验证步骤（以后执行）
1. 选人界面出现“精灵”，按钮、立绘、介绍、起始遗物正常；开局牌组 10 张。
2. 打出 咒之低语：弹出选择框选 1 张（手里只有 1 张可选时直接翻）；被选牌变成紫色卡框和蚀面卡图，名字带“蚀·”；角色变为中间态待机；右上出现 咒鸣 1。
3. 手牌中光面牌的伤害 / 格挡数字随 咒鸣 增加。
4. 打出 蚀·打击：播放双刃斩，结算两段伤害，然后翻回光面回到手牌；咒鸣 +1；裂纹封印闪光并抽 1 张（每回合仅一次）。
5. 手里留一张蚀面结束回合：失去 2 生命；下回合 咒鸣 归零。
6. 单刃斩在本回合翻过面后金色发光，打出抽 1 张。
7. 手牌 10 张时打出蚀面：进弃牌堆，不回手；下一次打出它的光面不应再回手。
8. 升级（篝火或 `upgrade` 命令）后，两面数值都按设计稿提高。

## 第六轮：完整卡池与遗物（2026-10-20）

| 项 | 结果 |
|---|---|
| 卡牌 | 72 张全部实现：起始 4 + 普通 20 + 罕见 32（8 张能力牌）+ 稀有 16（5 张能力牌），数值按设计稿 v4 §4。代码在 `cards/CommonCards`、`UncommonCards`、`RareCards`、`PowerCards`，注册清单 `cards/ElfCards` |
| 能力 | 19 个：13 张能力牌各一个，另有张弓（光 / 蚀）、回响（光 / 蚀）、双生、定神（本回合不反噬） |
| 遗物 | 10 件：裂纹封印、完整封印（Boss，替换裂纹封印）、暗纹护手、银叶胸针、镜之耳坠、苔藓护符、世界树的祝福、月蚀石、磨刃石、诅咒王冠 |
| 机制扩展 | 每面可单独设费用 / 消耗 / 段数；按条件加伤加格挡、加段数、改费用；咒鸣倍率（蓄力射击 ×2、余烬箭 ×3、碎咒、千刃、灵视、咒满）；光之誓、光辉、余韵读咒鸣；永夜加段数；反噬、咒鸣变化、侵蚀、封印都有监听接口 |
| 通用动作 | 选手牌（`PickHandAction`）、选抽牌堆顶 / 弃牌堆（`PickPileAction`）、击杀回调（`DamageThenAction`）、`Do` |
| 文本 | `tools/build_strings.py` 统一生成中英文：卡牌 72、能力 20（含咒鸣）、遗物 10 |
| `mvn package` | 退出码 0，26/26 单测（新增：卡牌数 72 且全部注册；两种语言文本齐全；72 张牌的卡图、19 个能力图标、10 件遗物三种图都存在） |
| ModTheSpire 补丁 | 注入成功（`logs/04-mts-outjar-pool.log`），已部署（SHA256 前缀 77768018） |
| 游戏内 | **未验证** |

实现时对设计稿的取舍：
- 诅咒之盾、噬咒等“按手牌数量加数值”的牌，把加成直接算进卡面数字。
- 回响的“打出两次”用复制品打出：复制品不触发自动封印、不回手。
- 净化之光、灵视、咒满的“升级后费用降低”只作用于设计稿标注的那一面。
- 解放、血之回响的伤害是固定值，不吃力量和咒鸣。

## 回退

- 启动器里取消勾选 “Elf Prototype (Test Skin)”，或 `powershell -ExecutionPolicy Bypass -File deploy.ps1 -Remove`
- 游戏内即时关闭：`elf off`
- 本模组不写存档；测试前备份的 saves / preferences 可按需复制回游戏目录
