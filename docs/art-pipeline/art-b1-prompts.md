# B1 起始包：执行方案与逐张提示词

由 `tools/build_b1_jobs.py` 生成，请勿手改；改提示词请改脚本后重新生成。机器可读版本：`art-b1-jobs.json`。

## 远端内置流程核对（2026-10-20，经 SSH 只读检查）

| 需求 | 结论 | 依据 |
|---|---|---|
| 带角色参考出新画面 | **支持** | 三张演示卡就是这么出的：`elf_production.py` 把设定图放进白底横幅画布作 `<image1>`，再写场景 |
| 以光面为输入改出蚀面 | **支持**，第 1 轮已验证：只用光面一张图、造型写在文字里才能保住构图 | `TextEncodeQwenImage21` 最多 16 张输入图；已有“保持身份与构图、只改姿势”的改图先例 |
| 多图参考（光面 + 暗精灵设定） | 支持，但**蚀面不用** | 第 1 轮试过：加入暗精灵全身设定图会把近景拉成全身远景 |
| 尺寸可控 | **支持**，规则是输出跟第一张图走 | 卡图沿用演示卡母图尺寸 1024×768；图标 1024×1024 |
| 种子与候选数 | **支持** | KSampler 固定种子，每张候选单独提交 |
| 抠图 | **支持** | `models/background_removal/` 有 birefnet、birefnet-matting |
| 降噪强度式图生图 | 不需要 | 改图靠指令完成，降噪恒为 1.0；原计划的强度扫描取消 |

模型与参数沿用内置流程：`qwen_image_2.1_int8_convrot` + `qwen3vl_8b_int8_convrot` + `qwen_image_2.1_vae_bf16`，25 步、CFG 3.0、euler / simple，`resolution=0`，负面提示词留空（与演示卡一致）。演示卡出图时显存峰值约 15 GB，**需要切回有卡模式**才能出图。

## 执行步骤

1. 切回有卡模式，启动工作台（工作台 7860、ComfyUI 8188）。
2. **第 1 轮（8 项，无依赖）**：4 张光面、2 张演示卡蚀面、裂纹封印、咒鸣图标，每项 4 张候选。演示卡蚀面同时用来验证改图能否在换装的同时保住构图：保不住就先调提示词，再出第 2 轮。
3. 我把候选拉回本地，出审阅拼图（候选并排，附 250×190 和 32 px 缩略图），你逐项选定。
4. **第 2 轮（4 项）**：以选定光面为 `<image1>` 出起始 4 张牌的蚀面，再审阅一次。
5. 导入：缩放、遗物描边、32 px 小图、放入 `src/main/resources`，检查后记录到 `TEST-RECORD.md`。

所有输出写到远端 `/workspace/sts1-art-pipeline/elf-b1/<ID>/`，每个候选保存 `workflow_api.json`、`prompt.txt` 和结果图；不覆盖已有的 `elf-production-v1`。

## Strike_Elf（第 1 轮 · card · light）

- 尺寸 1024×768，候选 4 张，种子 20261020–20261023
- `image_1`：`/workspace/character-pipeline-h3/character_studio/runs/qwen_final/designs/elf/clean/ready_qwen.png`（contain in 1024x768 white canvas）

```
Preserve the SAME adult female light elf from <image1>: recognizable face and cheek marking, blue eyes, pointed ears, long black braid, ivory hood, forest green cape and fully covered fitted tunic, gold segmented armor, ONE elegant gold longbow. Coherent pale ivory / forest green / warm gold palette. No additional person, no sword, no duplicated bow. Horizontal card illustration, right-facing waist-up profile at the moment of release: ONE arrow has just left the longbow toward screen RIGHT, the string still trembling, a thin golden streak trailing the arrow, sunlit forest clearing. Painterly 2D fantasy game card illustration, horizontal composition, soft brush texture, strong readable silhouette, ONE clear focal action that stays readable when shrunk to 250x190, face and hands inside the central 75 percent, uncluttered background. The painted environment background fills the entire frame edge to edge; no white, black or empty background. No text, labels, logos, watermark, frame, card border or interface. Only one character.
```

交付：cards/attack/Strike_Elf_p.png (500x380)，cards/attack/Strike_Elf.png (250x190)

## Defend_Elf（第 1 轮 · card · light）

- 尺寸 1024×768，候选 4 张，种子 20261120–20261123
- `image_1`：`/workspace/character-pipeline-h3/character_studio/runs/qwen_final/designs/elf/clean/ready_qwen.png`（contain in 1024x768 white canvas）

```
Preserve the SAME adult female light elf from <image1>: recognizable face and cheek marking, blue eyes, pointed ears, long black braid, ivory hood, forest green cape and fully covered fitted tunic, gold segmented armor, ONE elegant gold longbow. Coherent pale ivory / forest green / warm gold palette. No additional person, no sword, no duplicated bow. Horizontal card illustration, low guarded stance, bow held low in one hand, the other forearm raised; a translucent emerald barrier of overlapping leaves blooms in front of her, falling leaves, standing in a sunlit forest glade with tall trees, mossy ground and soft morning haze behind her. Painterly 2D fantasy game card illustration, horizontal composition, soft brush texture, strong readable silhouette, ONE clear focal action that stays readable when shrunk to 250x190, face and hands inside the central 75 percent, uncluttered background. The painted environment background fills the entire frame edge to edge; no white, black or empty background. No text, labels, logos, watermark, frame, card border or interface. Only one character.
```

交付：cards/skill/Defend_Elf_p.png (500x380)，cards/skill/Defend_Elf.png (250x190)

## CurseWhisper（第 1 轮 · card · light）

- 尺寸 1024×768，候选 4 张，种子 20261220–20261223
- `image_1`：`/workspace/character-pipeline-h3/character_studio/runs/qwen_final/designs/elf/clean/ready_qwen.png`（contain in 1024x768 white canvas）

```
Preserve the SAME adult female light elf from <image1>: recognizable face and cheek marking, blue eyes, pointed ears, long black braid, ivory hood, forest green cape and fully covered fitted tunic, gold segmented armor, ONE elegant gold longbow. Coherent pale ivory / forest green / warm gold palette. No additional person, no sword, no duplicated bow. Horizontal card illustration, close shot, eyes closed, one hand pressed to her chest; a faint thread of violet smoke slips out between her fingers like a whisper, cool moonlit forest. The violet thread is the focal point. Painterly 2D fantasy game card illustration, horizontal composition, soft brush texture, strong readable silhouette, ONE clear focal action that stays readable when shrunk to 250x190, face and hands inside the central 75 percent, uncluttered background. The painted environment background fills the entire frame edge to edge; no white, black or empty background. No text, labels, logos, watermark, frame, card border or interface. Only one character.
```

交付：cards/skill/CurseWhisper_p.png (500x380)，cards/skill/CurseWhisper.png (250x190)

## SingleEdge（第 1 轮 · card · intermediate）

- 尺寸 1024×768，候选 4 张，种子 20261320–20261323
- `image_1`：`/workspace/character-pipeline-h3/character_studio/runs/qwen_final/designs/intermediate/clean/ready_qwen.png`（contain in 1024x768 white canvas）

```
Preserve the SAME adult female elf from <image1> in her half-cursed form: recognizable face and cheek marking, pointed ears, long braid, white hood with violet edges, pale ivory armor, ONE single curved saber. Coherent ivory / pale violet / steel palette. No bow, no second blade, no additional person. Horizontal card illustration, a precise forward thrust of the single curved saber toward screen RIGHT, long lunge, a pale violet glint at the blade tip, misty grove. Painterly 2D fantasy game card illustration, horizontal composition, soft brush texture, strong readable silhouette, ONE clear focal action that stays readable when shrunk to 250x190, face and hands inside the central 75 percent, uncluttered background. The painted environment background fills the entire frame edge to edge; no white, black or empty background. No text, labels, logos, watermark, frame, card border or interface. Only one character.
```

交付：cards/attack/SingleEdge_p.png (500x380)，cards/attack/SingleEdge.png (250x190)

## ChargedShot_cursed（第 1 轮 · card · dark）

- 尺寸 1024×768，候选 4 张，种子 20261420–20261423
- `image_1`：`/workspace/sts1-art-pipeline/elf-production-v1/ChargedShot_opaque_source.png`（as is (1024x768)）

```
Edit <image1>. Keep EXACTLY the same camera, framing, composition, pose, body position and background layout of <image1>. Same crop and scale as <image1>: her head, shoulders and hands stay at the same position and size in the frame; do NOT zoom out, do NOT show more of her body than <image1> shows. Turn the elf into her cursed dark side: violet hood, dark leather and steel armor, purple cloak, braid with a violet sheen; her face, cheek marking, ears and braid stay recognisably the same person. Remove the bow and arrows completely; do not turn them into anything. She holds exactly TWO curved silver blades in total, one gripped in each hand; there are no other blades anywhere in the image, none floating, none on her back or belt. Shift the palette to deep violet, black, cold silver and a magenta curse glow, darken the background toward dusk, add thin dark curse smoke. In the same stance she holds the two blades drawn back ready to strike, a crescent moon behind her, thin violet light streaks trailing the blades (light only, not more blades). Painterly 2D fantasy game card illustration, horizontal composition, soft brush texture, strong readable silhouette, ONE clear focal action that stays readable when shrunk to 250x190, face and hands inside the central 75 percent, uncluttered background. The painted environment background fills the entire frame edge to edge; no white, black or empty background. No text, labels, logos, watermark, frame, card border or interface. Only one character.
```

交付：cards/attack/ChargedShot_cursed_p.png (500x380)，cards/attack/ChargedShot_cursed.png (250x190)

## LeafWard_cursed（第 1 轮 · card · dark）

- 尺寸 1024×768，候选 4 张，种子 20261520–20261523
- `image_1`：`/workspace/sts1-art-pipeline/elf-production-v1/LeafWard_opaque_source.png`（crop content to 4:3, resize 1024x768）

```
Edit <image1>. Keep EXACTLY the same camera, framing, composition, pose, body position and background layout of <image1>. Same crop and scale as <image1>: her head, shoulders and hands stay at the same position and size in the frame; do NOT zoom out, do NOT show more of her body than <image1> shows. Turn the elf into her cursed dark side: violet hood, dark leather and steel armor, purple cloak, braid with a violet sheen; her face, cheek marking, ears and braid stay recognisably the same person. Remove the bow and arrows completely; do not turn them into anything. She holds exactly TWO curved silver blades in total, one gripped in each hand; there are no other blades anywhere in the image, none floating, none on her back or belt. Shift the palette to deep violet, black, cold silver and a magenta curse glow, darken the background toward dusk, add thin dark curse smoke. In the same pose with one hand raised, the glowing leaves in her palm have turned into violet-black thorn shards that lock together into a plate of armor on her forearm. Painterly 2D fantasy game card illustration, horizontal composition, soft brush texture, strong readable silhouette, ONE clear focal action that stays readable when shrunk to 250x190, face and hands inside the central 75 percent, uncluttered background. The painted environment background fills the entire frame edge to edge; no white, black or empty background. No text, labels, logos, watermark, frame, card border or interface. Only one character.
```

交付：cards/skill/LeafWard_cursed_p.png (500x380)，cards/skill/LeafWard_cursed.png (250x190)

## CrackedSeal（第 1 轮 · relic）

- 尺寸 1024×1024，候选 4 张，种子 20261620–20261623
- `image_1`：`/workspace/character-pipeline-h3/character_studio/runs/qwen_final/designs/elf/clean/ready_qwen.png`（contain in 1024x1024 white canvas）
- 后处理：BiRefNet matting (models/background_removal/birefnet.safetensors) -> transparent PNG

```
Use <image1> only as a palette and material reference. Draw ONE isolated painterly 2D fantasy roguelike inventory icon, readable at 64 pixels: broad simple shapes, strong dark warm-brown contour, restrained shading, crisp silhouette, object within the central 72 percent. PURE WHITE background, no shadow, no platform, no frame, no text, letters, runes, numbers, hands, people or duplicate object. Subject: a ROUND ivory-heartwood SEAL MEDALLION with an antique gold rim and a simple engraved leaf crest, split by ONE bold jagged crack across the center; soft violet curse light leaks from the crack. No chain, cord or stand.
```

交付：relics/CrackedSeal.png (128)，relics/CrackedSealOutline.png (128, local)，relics/large/CrackedSeal.png (256)

## Chime（第 1 轮 · power_icon）

- 尺寸 1024×1024，候选 4 张，种子 20261720–20261723
- `image_1`：`/workspace/character-pipeline-h3/character_studio/runs/qwen_final/designs/dark/clean/ready_qwen.png`（contain in 1024x1024 white canvas）
- 后处理：BiRefNet matting (models/background_removal/birefnet.safetensors) -> transparent PNG

```
Use <image1> only as a palette and material reference. Draw ONE isolated painterly 2D fantasy roguelike inventory icon, readable at 64 pixels: broad simple shapes, strong dark warm-brown contour, restrained shading, crisp silhouette, object within the central 72 percent. PURE WHITE background, no shadow, no platform, no frame, no text, letters, runes, numbers, hands, people or duplicate object. Subject: a small cracked gold seal stamped with a simple leaf at the center surrounded by THREE bold concentric violet ripple rings, like a struck bell made of light. Must read clearly at 32 pixels; very few shapes, high contrast.
```

交付：powers/large/Chime.png (84)，powers/Chime.png (32, local)

## Strike_Elf_cursed（第 2 轮 · card · dark）

- 尺寸 1024×768，候选 4 张，种子 20261820–20261823
- `image_1`：`/workspace/sts1-art-pipeline/elf-b1/Strike_Elf/chosen.png`（as is (1024x768)）
- 依赖：先选定 `Strike_Elf`

```
Edit <image1>. Keep EXACTLY the same camera, framing, composition, pose, body position and background layout of <image1>. Same crop and scale as <image1>: her head, shoulders and hands stay at the same position and size in the frame; do NOT zoom out, do NOT show more of her body than <image1> shows. Turn the elf into her cursed dark side: violet hood, dark leather and steel armor, purple cloak, braid with a violet sheen; her face, cheek marking, ears and braid stay recognisably the same person. Remove the bow and arrows completely; do not turn them into anything. She holds exactly TWO curved silver blades in total, one gripped in each hand; there are no other blades anywhere in the image, none floating, none on her back or belt. Shift the palette to deep violet, black, cold silver and a magenta curse glow, darken the background toward dusk, add thin dark curse smoke. With the arm still extended to the right, a sweeping slash of the forward blade leaves ONE violet X-shaped streak of light in the air (light only, not blades). Painterly 2D fantasy game card illustration, horizontal composition, soft brush texture, strong readable silhouette, ONE clear focal action that stays readable when shrunk to 250x190, face and hands inside the central 75 percent, uncluttered background. The painted environment background fills the entire frame edge to edge; no white, black or empty background. No text, labels, logos, watermark, frame, card border or interface. Only one character.
```

交付：cards/attack/Strike_Elf_cursed_p.png (500x380)，cards/attack/Strike_Elf_cursed.png (250x190)

## Defend_Elf_cursed（第 2 轮 · card · dark）

- 尺寸 1024×768，候选 4 张，种子 20261920–20261923
- `image_1`：`/workspace/sts1-art-pipeline/elf-b1/Defend_Elf/chosen.png`（as is (1024x768)）
- 依赖：先选定 `Defend_Elf`

```
Edit <image1>. Keep EXACTLY the same camera, framing, composition, pose, body position and background layout of <image1>. Same crop and scale as <image1>: her head, shoulders and hands stay at the same position and size in the frame; do NOT zoom out, do NOT show more of her body than <image1> shows. Turn the elf into her cursed dark side: violet hood, dark leather and steel armor, purple cloak, braid with a violet sheen; her face, cheek marking, ears and braid stay recognisably the same person. Remove the bow and arrows completely; do not turn them into anything. She holds exactly TWO curved silver blades in total, one gripped in each hand; there are no other blades anywhere in the image, none floating, none on her back or belt. Shift the palette to deep violet, black, cold silver and a magenta curse glow, darken the background toward dusk, add thin dark curse smoke. The two blades are crossed in front of her chest; a ring-shaped violet shockwave of light bursts outward from the crossing point, dark petals thrown back. Painterly 2D fantasy game card illustration, horizontal composition, soft brush texture, strong readable silhouette, ONE clear focal action that stays readable when shrunk to 250x190, face and hands inside the central 75 percent, uncluttered background. The painted environment background fills the entire frame edge to edge; no white, black or empty background. No text, labels, logos, watermark, frame, card border or interface. Only one character.
```

交付：cards/skill/Defend_Elf_cursed_p.png (500x380)，cards/skill/Defend_Elf_cursed.png (250x190)

## CurseWhisper_cursed（第 2 轮 · card · dark）

- 尺寸 1024×768，候选 4 张，种子 20262020–20262023
- `image_1`：`/workspace/sts1-art-pipeline/elf-b1/CurseWhisper/chosen.png`（as is (1024x768)）
- 依赖：先选定 `CurseWhisper`

```
Edit <image1>. Keep EXACTLY the same camera, framing, composition, pose, body position and background layout of <image1>. Same crop and scale as <image1>: her head, shoulders and hands stay at the same position and size in the frame; do NOT zoom out, do NOT show more of her body than <image1> shows. Turn the elf into her cursed dark side: violet hood, dark leather and steel armor, purple cloak, braid with a violet sheen; her face, cheek marking, ears and braid stay recognisably the same person. Remove the bow and arrows completely; do not turn them into anything. She holds exactly TWO curved silver blades in total, one gripped in each hand; there are no other blades anywhere in the image, none floating, none on her back or belt. Shift the palette to deep violet, black, cold silver and a magenta curse glow, darken the background toward dusk, add thin dark curse smoke. Her eyes are open and glowing violet; the smoke now pours freely from her chest and coils up her arm, the moonlight turned magenta. Her hands stay where they are in <image1>; one blade rests in each hand. Painterly 2D fantasy game card illustration, horizontal composition, soft brush texture, strong readable silhouette, ONE clear focal action that stays readable when shrunk to 250x190, face and hands inside the central 75 percent, uncluttered background. The painted environment background fills the entire frame edge to edge; no white, black or empty background. No text, labels, logos, watermark, frame, card border or interface. Only one character.
```

交付：cards/skill/CurseWhisper_cursed_p.png (500x380)，cards/skill/CurseWhisper_cursed.png (250x190)

## SingleEdge_cursed（第 2 轮 · card · dark）

- 尺寸 1024×768，候选 4 张，种子 20262120–20262123
- `image_1`：`/workspace/sts1-art-pipeline/elf-b1/SingleEdge/chosen.png`（as is (1024x768)）
- 依赖：先选定 `SingleEdge`

```
Edit <image1>. Keep EXACTLY the same camera, framing, composition, pose, body position and background layout of <image1>. Same crop and scale as <image1>: her head, shoulders and hands stay at the same position and size in the frame; do NOT zoom out, do NOT show more of her body than <image1> shows. Turn the elf into her cursed dark side: violet hood, dark leather and steel armor, purple cloak, braid with a violet sheen; her face, cheek marking, ears and braid stay recognisably the same person. Remove the bow and arrows completely; do not turn them into anything. She holds exactly TWO curved silver blades in total, one gripped in each hand; there are no other blades anywhere in the image, none floating, none on her back or belt. Shift the palette to deep violet, black, cold silver and a magenta curse glow, darken the background toward dusk, add thin dark curse smoke. The single saber is replaced by the two blades, one in each hand, in the same lunge toward screen RIGHT; one violet streak of light trails the forward blade (light only, not another blade), sparks at the tip. Painterly 2D fantasy game card illustration, horizontal composition, soft brush texture, strong readable silhouette, ONE clear focal action that stays readable when shrunk to 250x190, face and hands inside the central 75 percent, uncluttered background. The painted environment background fills the entire frame edge to edge; no white, black or empty background. No text, labels, logos, watermark, frame, card border or interface. Only one character.
```

交付：cards/attack/SingleEdge_cursed_p.png (500x380)，cards/attack/SingleEdge_cursed.png (250x190)

