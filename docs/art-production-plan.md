# 美术资源生成计划（配合设计稿 v4）

日期：2026-10-20
依据：`cards-relics-design.md` v4（光面 / 蚀面 · 咒鸣），资源包 v1 现有素材，项目内现有文件的实际尺寸。

分工：
- **远端**（你的出图流程）：Qwen 出图，H3 出视频，BiRefNet 抠图。
- **本地**（我在 Windows 上做）：缩放、描边、命名、放入 `src/main/resources`、尺寸与透明度检查、审阅拼图、游戏内接入。

---

## 1. 需求总表

| 类别 | 需要 | 已有（可直接用） | 还要出图 |
|---|---|---|---|
| 双面卡卡图（攻击 / 技能） | 59 张 × 2 面 = 118 | 蓄力射击光面、林叶护持光面 | **116** |
| 能力牌卡图（单面） | 13 | 月辉觉醒 | **12** |
| 能力图标（大 84 + 小 32） | 13 张能力牌 + 咒鸣 + 4 个临时效果 = 18 | 无（只有占位） | **18** |
| 遗物（128 + 描边 + 大图 256） | 10 | 世界树的祝福 | **9** |
| 卡框 / 能量球 | 光面、蚀面各一套 | 光面原有；蚀面已由调色脚本生成 | 0（可选：正式重画蚀面卡框） |
| 战斗动画 | 精灵 7 动作 + 中间态待机 + 暗精灵双刃 | 全部已接入 | 0（可选补充见 B6） |
| 选人图、肩像、结局图 | 各 1 | 已有（精灵形态） | 0（可选：带诅咒元素的版本） |
| 翻面特效 | 侵蚀烟雾、封印金纹 | 无 | 0：先用游戏自带粒子调色，不出图 |
| 药水 | 世界树之酿 | 已有 | 0 |

**正式出图合计约 155 张**：卡图 128，能力图标 18，遗物 9。

---

## 2. 规格

| 资源 | 母图（远端输出） | 游戏文件（本地生成） | 背景 | 放置路径 |
|---|---|---|---|---|
| 卡图 | 1024×768（与演示卡母图相同） | `<ID>_p.png` 500×380，`<ID>.png` 250×190 | 不透明，满画面 | `images/cards/<attack|skill|power>/` |
| 蚀面卡图 | 1024×768 | `<ID>_cursed_p.png`，`<ID>_cursed.png` | 不透明 | 同上 |
| 能力图标 | 512×512 | `large/<ID>.png` 84×84，`<ID>.png` 32×32 | **透明** | `images/powers/` |
| 遗物 | 1024×1024 | `<ID>.png` 128，`<ID>Outline.png` 128，`large/<ID>.png` 256 | **透明** | `images/relics/` |

通用要求：sRGB，PNG，straight alpha。画面里不要文字、字母、符文、数字、边框或界面元素。

**卡图构图**：游戏里卡图被裁成 250×190，主体要放在画面中央约 70% 的范围内，缩到 250 宽时仍能一眼看清人物在做什么。

**遗物描边**：不出图，本地按透明度膨胀生成，做法与现有 `WorldtreeBlessingOutline.png` 一致。

**能力图标**：只出一张大图，32 px 小图本地缩放。图形要粗、简单，缩到 32 px 后轮廓还能分辨。

---

## 3. 统一风格

### 3.1 通用风格段（所有卡图共用，放在提示词开头）

> Painterly 2D fantasy card illustration for a roguelike deckbuilder, landscape 1000x760, soft brush texture, strong readable silhouette, single clear focal action that stays readable when shrunk to 250x190, cinematic but uncluttered background, no text, no letters, no runes, no numbers, no frame, no border, no UI.

### 3.2 人物段

光面和蚀面是**同一个人**，脸、发型和体型保持一致。

| 面 | 人物段 | 参考图 |
|---|---|---|
| **光面** | the light elf ranger: white hood, ivory-and-gold plate armor, emerald cloak, long black braid, golden longbow; palette forest emerald, ivory, antique gold, warm amber light | `optional/duality/designs/elf/clean/ready_qwen.png`，以及三张演示卡 |
| **中间态** | the same elf half-taken by her curse: white hood with violet edges, pale armor, single curved saber; palette ivory, pale violet, steel | `designs/intermediate/clean/ready_qwen.png` |
| **蚀面** | the same woman as her cursed dark side: violet hood, dark leather and steel armor, purple cloak, braid with violet sheen, twin curved silver blades; palette deep violet, black, cold silver, magenta curse glow; dark curse smoke curling around her | `designs/dark/clean/ready_qwen.png` |

用中间态的牌：单刃斩的光面（单刃是“半诅咒”的武器），以及卡面上同时涉及侵蚀和封印的牌，比如转念、镜像。

### 3.3 双面卡的出图方法

两面要像同一个镜头的“光 / 暗”版本，玩家翻面时一眼认得出是同一张牌：

1. **先出光面**，选定一张。
2. **蚀面以光面成图为输入图改图**：`<image1>` = 光面卡图，`<image2>` = 暗精灵设定图；保留镜头、构图和人物站位，把人物换成暗精灵，武器换成双刃，色调换成紫黑，背景压暗并加上诅咒烟雾。

2026-10-20 已核对远端：Qwen Image 2.1 的 `TextEncodeQwenImage21` 同时负责文生图和指令改图，支持多张输入图，输出尺寸跟第一张图走；改图靠指令完成，不用降噪强度。B1 第 1 轮用两张演示卡的蚀面验证换装后构图是否保得住。详见 `art-pipeline/art-b1-prompts.md`。

### 3.4 遗物和能力图标

沿用 `worldtree-design.json` 已验证的物件图标写法：

> Create ONE isolated painterly 2D fantasy roguelike inventory icon, designed to remain readable at 64px. Broad simple shapes, strong dark warm-brown contour, restrained shading, crisp silhouette. PURE WHITE background, no cast shadow, no environment, no platform, no frame, no text, letters, runes, labels, numbers, hands, people or duplicate object. Entire object comfortably within the central 72% of the canvas.

调色：封印类用象牙白、古金、翠绿；诅咒类用深紫、黑、冷银、品红光。出图后用 BiRefNet 抠成透明。

---

## 4. 分批计划

每一批：远端出图 → 交给我导入 → 我出审阅拼图 → 你确认 → 进游戏。**前一批确认了风格，下一批才开始**，避免大批返工。

| 批次 | 内容 | 张数 | 目的 | 前置 |
|---|---|---|---|---|
| **B0**（本地，已完成） | 蚀面卡框、能量球、占位卡图、双刃攻击、中间态待机 | — | 先有能跑的表现 | — |
| **B1 起始包** | 起始 4 种牌的两面（8）、蓄力射击和林叶护持的蚀面（2）、裂纹封印遗物（1）、咒鸣图标（1） | 12 | 定风格：光暗两面的一致性、改图能否保住构图、图标可读性 | 无 |
| **B2 机制图标与遗物** | 剩余 17 个能力图标、其余 8 件遗物 | 25 | 让所有能力和遗物有正式图 | B1 图标风格确认 |
| **B3 普通卡** | 普通卡 18 张 × 2 面（2 张演示卡已在 B1 处理） | 36 | 第一层就能见到的牌 | B1 双面方法确认 |
| **B4 罕见卡** | 双面 24 × 2 = 48，能力牌 8 | 56 | 中期构筑的牌 | B3 |
| **B5 稀有卡** | 双面 11 × 2 = 22，能力牌 4（月辉觉醒已有） | 26 | 收尾 | B4 |
| **B6 可选润色** | 暗精灵的施法 / 格挡 / 受击动作（H3）；正式蚀面卡框；带诅咒元素的选人图；封印特效贴图 | 视需要 | 表现升级 | 机制在游戏里玩顺之后 |

建议 B1 完成后先把机制做进游戏，玩顺了再开始 B3。卡池还可能调整，提前画完 116 张卡图返工风险太大。

---

## 5. B1 起始包：可直接提交的提示词

每条提示词 = 通用风格段（§3.1）+ 人物段（§3.2）+ 下面的画面描述。蚀面一律以对应光面成图为输入图改图（§3.3）。

| # | 文件 ID | 面 | 人物 | 画面描述 |
|---|---|---|---|---|
| 1 | `Strike_Elf` | 光 | 光面 | Mid-release of a single arrow toward an unseen foe on the right, bowstring still trembling, a thin golden streak trailing the arrow, sunlit forest clearing. |
| 2 | `Strike_Elf_cursed` | 蚀 | 蚀面 | Same framing: she lunges right with two crossing slashes of her twin blades, two violet slash trails forming an X, the forest behind dimmed to dusk. |
| 3 | `Defend_Elf` | 光 | 光面 | Low guarded stance, forearm raised, a translucent emerald barrier of overlapping leaves blooming in front of her, falling leaves. |
| 4 | `Defend_Elf_cursed` | 蚀 | 蚀面 | Same framing: blades crossed in front of her chest, a ring-shaped violet shockwave bursting outward from the crossed blades, dust and dark petals thrown back. |
| 5 | `CurseWhisper` | 光 | 光面 | Close shot, eyes closed, one hand pressed to her chest; a faint thread of violet smoke slips out between her fingers like a whisper, cool moonlit forest. |
| 6 | `CurseWhisper_cursed` | 蚀 | 蚀面 | Same framing: eyes open and glowing violet, the smoke now pours freely from her chest and coils up her arm, the moonlight turned magenta. |
| 7 | `SingleEdge` | 光 | 中间态 | A precise forward thrust of a single curved saber, body in a long lunge, a pale violet glint at the blade tip, misty grove. |
| 8 | `SingleEdge_cursed` | 蚀 | 蚀面 | Same framing: three rapid strikes shown as three overlapping violet afterimages of her twin blades, sparks at the point of impact. |
| 9 | `ChargedShot_cursed` | 蚀 | 蚀面 | 以现有 `ChargedShot_p.png` 为输入图：same framing, she leaps with both blades raised under a crescent moon, three violet arcs cutting the air. |
| 10 | `LeafWard_cursed` | 蚀 | 蚀面 | 以现有 `LeafWard_p.png` 为输入图：same pose, the glowing leaves in her hand have turned into violet-black thorn shards that lock together into a plate of armor on her forearm. |
| 11 | `CrackedSeal`（遗物） | — | — | 物件图标段（§3.4）+ Subject: a ROUND ivory-heartwood SEAL MEDALLION with an antique gold rim and a simple engraved leaf crest, split by ONE bold jagged crack across the center; soft violet curse light leaks from the crack. Readable at 64px; no chain, no cord, no stand. |
| 12 | `Chime`（能力图标） | — | — | 物件图标段（§3.4）+ Subject: a small cracked gold seal glyph at the center surrounded by THREE bold concentric violet ripple rings, like a struck bell made of light. Must read clearly at 32px; very simple shapes. |

---

## 6. 验收标准

| 检查项 | 怎么查 | 谁 |
|---|---|---|
| 尺寸、透明度、文件名 | 导入脚本自动检查，不合格的列出来 | 我 |
| 缩小后可读 | 拼图里同时放 250×190 和 32 px 的缩略图 | 我出图，你看 |
| 光暗两面是同一镜头 | 拼图里两面左右并排；脚本计算两面缩略灰度图的结构相似度，低于阈值的标出 | 我出图，你看 |
| 人物一致 | 兜帽、黑色长辫、尖耳在两面都在；蚀面武器一定是双刃（单刃斩光面例外） | 你 |
| 无文字、无边框 | 人工看 | 你 |
| 游戏内效果 | 卡框里的裁切位置、蚀面紫框上的对比度 | 进游戏后一起看 |

---

## 7. 交接方式

- **你交给我**：每批一个文件夹，里面是母图，文件名用上表的 ID（如 `Strike_Elf.png`、`Strike_Elf_cursed.png`、`CrackedSeal.png`）。放在 `D:\StSDev\art-inbox\B1\` 这类目录即可。遗物和图标请交抠图后的透明版。
- **我来做**：写 `tools/import_art.py` 完成缩放、描边、32 px 小图、命名、放入 `src/main/resources`、检查和审阅拼图，并在 `TEST-RECORD.md` 记录每批结果。
- **原包不动**：资源包里的文件保持原样，所有新图只新增，不覆盖。替换占位图时保留旧文件，直到你确认新图。

---

## 8. 已确认（2026-10-20）

1. 远端支持指令改图和多图参考（见 §3.3）。
2. B1 按内置 `elf_production.py` 的出图方式直接提交到工作台自带的 ComfyUI，输出写到 `/workspace/sts1-art-pipeline/elf-b1/`，不改动已有阶段和 `elf-production-v1`。
3. 先出 B1 的 12 张，确认风格后再继续。
