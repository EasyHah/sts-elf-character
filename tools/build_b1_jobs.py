"""Build the B1 art batch: docs/art-pipeline/art-b1-jobs.json (for the remote pipeline) and docs/art-pipeline/art-b1-prompts.md (review).

Matches the remote's built-in Qwen Image 2.1 workflow (checked 2026-10-20 over SSH, see art-b1-prompts.md):
  - one node TextEncodeQwenImage21 does both text-to-image and edit; prompts address inputs as <image1>, <image2>
  - output size follows the FIRST input image (resolution=0 keeps it as is, multiples of 32)
  - light faces: image1 = the form's design reference contained in a white 1024x768 canvas
    (same method elf_production.py highres() used for the three demo cards)
  - cursed faces: image1 = the chosen light-face card (1024x768) ONLY; the dark design is described in text.
    Round-1 test: adding the dark design sheet as image2 pulled the shot out to full body; text-only kept the crop.
  - every scene names a concrete environment: with only "white canvas + effect" the background stayed blank
  - relic / icon: image1 = a palette reference contained in 1024x1024 white, then BiRefNet matting

Usage: python tools/build_b1_jobs.py
"""
import json
import os

PROJECT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DOCS = os.path.join(PROJECT, "docs", "art-pipeline")

# Remote paths (all verified to exist on the server).
DESIGN = "/workspace/character-pipeline-h3/character_studio/runs/qwen_final/designs/{}/clean/ready_qwen.png"
DEMO_MASTER = "/workspace/sts1-art-pipeline/elf-production-v1/{}_opaque_source.png"  # 1024x768 opaque
OUT_ROOT = "/workspace/sts1-art-pipeline/elf-b1"

WORKFLOW = {
    "based_on": "sts1-art-pipeline/production-tools/elf_production.py highres() graph",
    "unet": "qwen_image_2.1_int8_convrot.safetensors",
    "clip": ["qwen3vl_8b_int8_convrot.safetensors", "qwen_image"],
    "vae": "qwen_image_2.1_vae_bf16.safetensors",
    "encoder": "TextEncodeQwenImage21", "resolution": 0, "negative_prompt": "",
    "sampler": {"steps": 25, "cfg": 3.0, "sampler_name": "euler", "scheduler": "simple", "denoise": 1.0},
    "latent": "encoder output #2 (size of image1)",
    "comfy": "http://127.0.0.1:8188 (engine ComfyUI of WalkingwithAI-Studio)",
}

STYLE = ("Painterly 2D fantasy game card illustration, horizontal composition, soft brush texture, strong readable "
         "silhouette, ONE clear focal action that stays readable when shrunk to 250x190, face and hands inside the "
         "central 75 percent, uncluttered background. The painted environment background fills the entire frame "
         "edge to edge; no white, black or empty background. No text, labels, logos, watermark, frame, card border "
         "or interface. Only one character.")

# Identity blocks. LIGHT is the template's own identity string used for the three demo cards.
IDENTITY = {
    "light": ("Preserve the SAME adult female light elf from <image1>: recognizable face and cheek marking, blue "
              "eyes, pointed ears, long black braid, ivory hood, forest green cape and fully covered fitted tunic, "
              "gold segmented armor, ONE elegant gold longbow. Coherent pale ivory / forest green / warm gold "
              "palette. No additional person, no sword, no duplicated bow."),
    "intermediate": ("Preserve the SAME adult female elf from <image1> in her half-cursed form: recognizable face "
                     "and cheek marking, pointed ears, long braid, white hood with violet edges, pale ivory armor, "
                     "ONE single curved saber. Coherent ivory / pale violet / steel palette. No bow, no second "
                     "blade, no additional person."),
}
DARK_EDIT = ("Edit <image1>. Keep EXACTLY the same camera, framing, composition, pose, body position and background "
             "layout of <image1>. Same crop and scale as <image1>: her head, shoulders and hands stay at the same "
             "position and size in the frame; do NOT zoom out, do NOT show more of her body than <image1> shows. "
             "Turn the elf into her cursed dark side: violet hood, dark leather and steel armor, purple cloak, braid "
             "with a violet sheen; her face, cheek marking, ears and braid stay recognisably the same person. "
             "Remove the bow and arrows completely; do not turn them into anything. She holds exactly TWO curved "
             "silver blades in total, one gripped in each hand; there are no other blades anywhere in the image, "
             "none floating, none on her back or belt. Shift the palette to deep violet, black, cold silver and a "
             "magenta curse glow, darken the background toward dusk, add thin dark curse smoke.")
# Round-2 review (2026-10-20): candidates showed 3-4 blades. Causes: effects worded as blade afterimages/arcs,
# "twin blades" repeated, the bow turned into blades, empty negative prompt. Effects are now light streaks only.
NEG_DARK = ("extra blades, third blade, more than two blades, floating weapons, duplicated weapons, weapon "
            "afterimages, extra hands, extra arms, bow, arrows")

ICON = ("Use <image1> only as a palette and material reference. Draw ONE isolated painterly 2D fantasy roguelike "
        "inventory icon, readable at 64 pixels: broad simple shapes, strong dark warm-brown contour, restrained "
        "shading, crisp silhouette, object within the central 72 percent. PURE WHITE background, no shadow, no "
        "platform, no frame, no text, letters, runes, numbers, hands, people or duplicate object.")

CANDIDATES = 4
BASE_SEED = 20261020

# id, round, kind, card_type, identity ("light" | "intermediate" | "dark"), scene, source of image1/image2
ASSETS = [
    ("Strike_Elf", 1, "card", "attack", "light",
     "Horizontal card illustration, right-facing waist-up profile at the moment of release: ONE arrow has just "
     "left the longbow toward screen RIGHT, the string still trembling, a thin golden streak trailing the arrow, "
     "sunlit forest clearing.", None),
    ("Defend_Elf", 1, "card", "skill", "light",
     "Horizontal card illustration, low guarded stance, bow held low in one hand, the other forearm raised; a "
     "translucent emerald barrier of overlapping leaves blooms in front of her, falling leaves, standing in a "
     "sunlit forest glade with tall trees, mossy ground and soft morning haze behind her.", None),
    ("CurseWhisper", 1, "card", "skill", "light",
     "Horizontal card illustration, close shot, eyes closed, one hand pressed to her chest; a faint thread of "
     "violet smoke slips out between her fingers like a whisper, cool moonlit forest. The violet thread is the "
     "focal point.", None),
    ("SingleEdge", 1, "card", "attack", "intermediate",
     "Horizontal card illustration, a precise forward thrust of the single curved saber toward screen RIGHT, "
     "long lunge, a pale violet glint at the blade tip, misty grove.", None),
    ("ChargedShot_cursed", 1, "card", "attack", "dark",
     "In the same stance she holds the two blades drawn back ready to strike, a crescent moon behind her, "
     "thin violet light streaks trailing the blades (light only, not more blades).", {"demo": "ChargedShot"}),
    ("LeafWard_cursed", 1, "card", "skill", "dark",
     "In the same pose with one hand raised, the glowing leaves in her palm have turned into violet-black thorn "
     "shards that lock together into a plate of armor on her forearm.", {"demo": "LeafWard"}),
    ("CrackedSeal", 1, "relic", None, None,
     "Subject: a ROUND ivory-heartwood SEAL MEDALLION with an antique gold rim and a simple engraved leaf crest, "
     "split by ONE bold jagged crack across the center; soft violet curse light leaks from the crack. No chain, "
     "cord or stand.", {"palette": "light"}),
    ("Chime", 1, "power_icon", None, None,
     "Subject: a small cracked gold seal stamped with a simple leaf at the center surrounded by THREE bold concentric violet ripple "
     "rings, like a struck bell made of light. Must read clearly at 32 pixels; very few shapes, high contrast.",
     {"palette": "dark"}),
    ("Strike_Elf_cursed", 2, "card", "attack", "dark",
     "With the arm still extended to the right, a sweeping slash of the forward blade leaves ONE violet "
     "X-shaped streak of light in the air (light only, not blades).", {"b1": "Strike_Elf"}),
    ("Defend_Elf_cursed", 2, "card", "skill", "dark",
     "The two blades are crossed in front of her chest; a ring-shaped violet shockwave of light bursts outward "
     "from the crossing point, dark petals thrown back.", {"b1": "Defend_Elf"}),
    ("CurseWhisper_cursed", 2, "card", "skill", "dark",
     "Her eyes are open and glowing violet; the smoke now pours freely from her chest and coils up her arm, the "
     "moonlight turned magenta. Her hands stay where they are in <image1>; one blade rests in each hand.", {"b1": "CurseWhisper"}),
    ("SingleEdge_cursed", 2, "card", "attack", "dark",
     "The single saber is replaced by the two blades, one in each hand, in the same lunge toward screen RIGHT; "
     "one violet streak of light trails the forward blade (light only, not another blade), sparks at the tip.", {"b1": "SingleEdge"}),
]


def runtime(kind, aid, ctype):
    if kind == "card":
        return [f"cards/{ctype}/{aid}_p.png (500x380)", f"cards/{ctype}/{aid}.png (250x190)"]
    if kind == "relic":
        return [f"relics/{aid}.png (128)", f"relics/{aid}Outline.png (128, local)", f"relics/large/{aid}.png (256)"]
    return [f"powers/large/{aid}.png (84)", f"powers/{aid}.png (32, local)"]


def build_job(i, a):
    aid, rnd, kind, ctype, ident, scene, src = a
    job = {"id": aid, "round": rnd, "kind": kind, "card_type": ctype, "face": ident,
           "candidates": CANDIDATES, "seeds": [BASE_SEED + i * 100 + k for k in range(CANDIDATES)],
           "out_dir": f"{OUT_ROOT}/{aid}", "runtime": runtime(kind, aid, ctype)}
    if kind == "card" and ident in IDENTITY:
        job["size"] = [1024, 768]
        job["images"] = {"image_1": {"path": DESIGN.format("elf" if ident == "light" else ident),
                                     "prepare": "contain in 1024x768 white canvas"}}
        job["prompt"] = " ".join([IDENTITY[ident], scene, STYLE])
    elif kind == "card":
        if "demo" in src:
            # LeafWard's 1024x768 master has white side bars; ChargedShot's is full bleed.
            prep = "crop content to 4:3, resize 1024x768" if src["demo"] == "LeafWard" else "as is (1024x768)"
            first = {"path": DEMO_MASTER.format(src["demo"]), "prepare": prep}
        else:
            first = {"path": f"{OUT_ROOT}/{src['b1']}/chosen.png", "prepare": "as is (1024x768)"}
            job["depends_on"] = src["b1"]
        job["size"] = [1024, 768]
        job["images"] = {"image_1": first}
        job["prompt"] = " ".join([DARK_EDIT, scene, STYLE])
        # NEG_DARK is kept for reference only: the A/B test showed it sometimes removes the second blade too.
    else:
        job["size"] = [1024, 1024]
        job["images"] = {"image_1": {"path": DESIGN.format("elf" if src["palette"] == "light" else "dark"),
                                     "prepare": "contain in 1024x1024 white canvas"}}
        job["prompt"] = ICON + " " + scene
        job["post"] = "BiRefNet matting (models/background_removal/birefnet.safetensors) -> transparent PNG"
    return job


def prompts_md(jobs):
    L = ["# B1 起始包：执行方案与逐张提示词", "",
         "由 `tools/build_b1_jobs.py` 生成，请勿手改；改提示词请改脚本后重新生成。机器可读版本：`art-b1-jobs.json`。", "",
         "## 远端内置流程核对（2026-10-20，经 SSH 只读检查）", "",
         "| 需求 | 结论 | 依据 |", "|---|---|---|",
         "| 带角色参考出新画面 | **支持** | 三张演示卡就是这么出的：`elf_production.py` 把设定图放进白底横幅画布作 `<image1>`，再写场景 |",
         "| 以光面为输入改出蚀面 | **支持**，第 1 轮已验证：只用光面一张图、造型写在文字里才能保住构图 | `TextEncodeQwenImage21` 最多 16 张输入图；已有“保持身份与构图、只改姿势”的改图先例 |",
         "| 多图参考（光面 + 暗精灵设定） | 支持，但**蚀面不用** | 第 1 轮试过：加入暗精灵全身设定图会把近景拉成全身远景 |",
         "| 尺寸可控 | **支持**，规则是输出跟第一张图走 | 卡图沿用演示卡母图尺寸 1024×768；图标 1024×1024 |",
         "| 种子与候选数 | **支持** | KSampler 固定种子，每张候选单独提交 |",
         "| 抠图 | **支持** | `models/background_removal/` 有 birefnet、birefnet-matting |",
         "| 降噪强度式图生图 | 不需要 | 改图靠指令完成，降噪恒为 1.0；原计划的强度扫描取消 |", "",
         "模型与参数沿用内置流程：`qwen_image_2.1_int8_convrot` + `qwen3vl_8b_int8_convrot` + `qwen_image_2.1_vae_bf16`，"
         "25 步、CFG 3.0、euler / simple，`resolution=0`，负面提示词留空（与演示卡一致）。"
         "演示卡出图时显存峰值约 15 GB，**需要切回有卡模式**才能出图。", "",
         "## 执行步骤", "",
         "1. 切回有卡模式，启动工作台（工作台 7860、ComfyUI 8188）。",
         "2. **第 1 轮（8 项，无依赖）**：4 张光面、2 张演示卡蚀面、裂纹封印、咒鸣图标，每项 4 张候选。"
         "演示卡蚀面同时用来验证改图能否在换装的同时保住构图：保不住就先调提示词，再出第 2 轮。",
         "3. 我把候选拉回本地，出审阅拼图（候选并排，附 250×190 和 32 px 缩略图），你逐项选定。",
         "4. **第 2 轮（4 项）**：以选定光面为 `<image1>` 出起始 4 张牌的蚀面，再审阅一次。",
         "5. 导入：缩放、遗物描边、32 px 小图、放入 `src/main/resources`，检查后记录到 `TEST-RECORD.md`。", "",
         "所有输出写到远端 `" + OUT_ROOT + "/<ID>/`，每个候选保存 `workflow_api.json`、`prompt.txt` 和结果图；不覆盖已有的 `elf-production-v1`。", ""]
    for j in jobs:
        L.append(f"## {j['id']}（第 {j['round']} 轮 · {j['kind']}" + (f" · {j['face']}" if j['face'] else "") + "）")
        L.append("")
        L.append(f"- 尺寸 {j['size'][0]}×{j['size'][1]}，候选 {j['candidates']} 张，种子 {j['seeds'][0]}–{j['seeds'][-1]}")
        for k, v in j["images"].items():
            L.append(f"- `{k}`：`{v['path']}`（{v['prepare']}）")
        if j.get("depends_on"):
            L.append(f"- 依赖：先选定 `{j['depends_on']}`")
        if j.get("post"):
            L.append(f"- 后处理：{j['post']}")
        L += ["", "```", j["prompt"], "```", "", "交付：" + "，".join(j["runtime"]), ""]
    return "\n".join(L) + "\n"


def main():
    jobs = [build_job(i, a) for i, a in enumerate(ASSETS)]
    doc = {"batch": "B1", "design": "cards-relics-design.md v4", "plan": "art-production-plan.md",
           "workflow": WORKFLOW,
           "rounds": {"1": "light faces, demo-card cursed faces (edit check), relic, icon",
                      "2": "cursed faces of the four starter cards from the chosen round-1 light faces"},
           "jobs": jobs}
    with open(os.path.join(DOCS, "art-b1-jobs.json"), "w", encoding="utf-8") as f:
        json.dump(doc, f, ensure_ascii=False, indent=2)
    with open(os.path.join(DOCS, "art-b1-prompts.md"), "w", encoding="utf-8") as f:
        f.write(prompts_md(jobs))
    print(len(jobs), "jobs ->", "docs/art-pipeline/art-b1-jobs.json, docs/art-pipeline/art-b1-prompts.md")


if __name__ == "__main__":
    main()
