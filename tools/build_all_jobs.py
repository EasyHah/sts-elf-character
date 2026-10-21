"""Build the remaining art (B2-B5 of art-production-plan.md) as one job file: docs/art-pipeline/art-all-jobs.json.

Reuses the B1 blocks and lessons from build_b1_jobs.py (same workflow, identity, cursed-face edit wording):
  round 1: light faces of 53 dual cards, 12 power-card arts, 17 power icons, 8 relics (4 candidates each)
  round 2: the 53 cursed faces, edited from the chosen light face (3 candidates each)
Remote output folder: /workspace/sts1-art-pipeline/elf-art/<ID>/

Usage: python tools/build_all_jobs.py
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import build_b1_jobs as b1  # noqa: E402

OUT_ROOT = "/workspace/sts1-art-pipeline/elf-art"
LIGHT_CANDIDATES = 4
CURSED_CANDIDATES = 3
BASE_SEED = 40000000

IDENTITY = dict(b1.IDENTITY)
IDENTITY["dark"] = ("Preserve the SAME adult female elf from <image1> in her cursed dark form: recognizable face and "
                    "cheek marking, pointed ears, violet hood, dark leather and steel armor, purple cloak, braid with "
                    "a violet sheen, exactly TWO curved silver blades, one in each hand. Coherent deep violet / black "
                    "/ cold silver palette with a magenta curse glow. No bow, no other weapons, no additional person.")

# Dual cards: id, card type, light-face form, light scene, cursed scene (cursed = edit of the chosen light face).
DUAL = [
    # ---- common (ChargedShot and LeafWard already have both faces)
    ("ThicketArrow", "attack", "light",
     "she fires ONE arrow that threads through a narrow gap between dense birch trunks toward screen RIGHT, "
     "leaves bursting where it passes, dappled green forest.",
     "the two blades thrust forward together through the gap between the trunks, short violet streaks of light "
     "at the tips."),
    ("QuickArrow", "attack", "light",
     "a quick snap shot from the hip, the arrow already far ahead as a thin golden line toward screen RIGHT, her "
     "braid swinging, wind-swept meadow at the forest edge.",
     "a quick double slash from the hip, two short violet streaks of light, her braid swinging."),
    ("ScatterShot", "attack", "light",
     "she looses a fan of five arrows spreading across the sky toward screen RIGHT, autumn hillside above the "
     "forest.",
     "she spins with the blades, one wide circular violet streak of light sweeping around her."),
    ("SnareShot", "attack", "light",
     "a low kneeling shot aimed at an unseen enemy's legs, a glowing green vine trailing behind the arrow, roots "
     "of an ancient oak.",
     "a low kneeling slash at leg height, a violet streak of light along the ground, thorny dark roots."),
    ("CorruptingEdge", "attack", "intermediate",
     "a single curved saber stroke toward screen RIGHT, a thin violet stain spreading along the blade from the "
     "hilt, ruined stone arch in a misty forest.",
     "violet corruption covers both blades, a crackling violet streak of light along the stroke."),
    ("GladeThrust", "attack", "light",
     "she drives forward with the bow held across her body as a guard, a curved shield of green leaf-light on her "
     "forearm, sunlit fern glade.",
     "she lunges with rapid stabs, a row of four short violet streaks of light at the blade tips."),
    ("Riposte", "attack", "intermediate",
     "a backhand counter-strike with the single saber, a golden seal sigil of light flashing on the blade as it "
     "parries, stone ruins in the forest at dusk.",
     "a backhand counter with the blades, a golden seal sigil of light flashing where they meet."),
    ("EmberShot", "attack", "light",
     "she aims an arrow whose head glows like a smouldering ember, tiny sparks drifting, burnt clearing with "
     "charred stumps under a dim sky.",
     "both blade edges glow like smouldering embers, sparks drifting, short ember-violet streaks of light."),
    ("Recoil", "attack", "light",
     "she leaps backward while firing, the bowstring snapping forward, a golden arrow streak toward screen RIGHT, "
     "rocky forest stream.",
     "she leaps backward slashing, one curved violet streak of light looping like a returning boomerang."),
    ("HoldBreath", "skill", "light",
     "close shot, she holds her breath with an arrow nocked and the bow half drawn, perfectly still, a single "
     "drifting leaf frozen in the air, misty dawn forest.",
     "she holds still with the blades lowered, eyes closed, a faint violet glow on the edges, the frozen leaf now "
     "dark."),
    ("SealingRite", "skill", "light",
     "she draws a glowing golden seal circle of leaves in the air with two fingers, two small violet wisps being "
     "pulled into it, moonlit stone shrine in the forest.",
     "the seal circle now glows violet and gold around her crossed blades."),
    ("Invite", "skill", "intermediate",
     "she opens her free palm and lets two threads of violet smoke rise from it, face calm, eyes half closed, "
     "abandoned shrine with broken lanterns.",
     "the violet smoke rises from the blades and coils around her arms, eyes glowing violet."),
    ("Moonstep", "skill", "light",
     "a light sidestep across moonlit stepping stones, faint silver footprints of light behind her, night river "
     "in the forest.",
     "a shadowy sidestep, violet footprints of light behind her, blades held low."),
    ("PurifyingLight", "skill", "light",
     "she raises her palm and warm golden light washes over drifting violet smoke, dissolving it into golden "
     "motes, forest chapel ruins with sunbeams.",
     "the violet smoke is drawn into her blades instead, the edges glowing gold and violet."),
    ("CurseWard", "skill", "intermediate",
     "a shield of overlapping violet and gold shards forms around her raised forearm, the single saber held low, "
     "cracked marble courtyard overgrown with ivy.",
     "the shards are all violet now, forming a jagged shield between her blades."),
    ("Premonition", "skill", "light",
     "she gazes into a floating disc of still water that shows three faint blank glowing reflections, quiet "
     "forest pond at twilight.",
     "the water disc turns violet and the reflections darken; she watches with glowing eyes."),
    ("Nock", "skill", "light",
     "close detail of her hands nocking a single arrow, golden light gathering at the arrowhead, blurred forest "
     "bokeh behind.",
     "close detail of her hands tightening their grip on the two blade hilts, violet light gathering along the "
     "edges."),
    ("Unbind", "skill", "light",
     "she pulls a glowing golden thread out of a knot of violet chains around her own wrist, the chains breaking "
     "into light, ivy-covered ruin at sunrise.",
     "the chains snap and their violet light flows into her blades."),
    # ---- uncommon attacks
    ("TwinMoon", "attack", "light",
     "she fires two arrows at once toward screen RIGHT under two pale moons in the night sky, silver-lit cliff "
     "above the forest.",
     "a heavy diagonal slash, two crescent-shaped violet streaks of light echoing the two moons."),
    ("Skim", "attack", "intermediate",
     "a swift low skimming cut with the single saber across a still lake surface, a spray of water and a thin "
     "violet ripple, misty lake at dawn.",
     "two swift low cuts skimming the water, two thin violet ripples of light."),
    ("Heartseeker", "attack", "light",
     "a precise aimed shot, a small heart-shaped golden glint at the arrowhead, extreme focus along the shaft, "
     "dark pine forest.",
     "a precise double thrust, violet streaks of light converging to a single point."),
    ("Harvest", "attack", "light",
     "she shoots across a golden autumn field of tall grass, the arrow reaping a line through the stalks, late "
     "afternoon sun.",
     "a wide reaping sweep through the tall grass, one violet crescent streak of light."),
    ("Volley", "attack", "light",
     "she shoots high into the sky and a rain of golden arrows falls across a distant valley, dramatic clouds over "
     "the forest.",
     "a whirling blade dance, a ring of violet streaks of light around her; the arrow rain is gone."),
    ("MirrorEdge", "attack", "intermediate",
     "her single saber reflects her face like a mirror, a golden seal glint on the blade, hall of broken mirrors "
     "in a ruined forest temple.",
     "the blades reflect her glowing violet eyes, a golden seal glint on each blade."),
    ("Leech", "attack", "light",
     "she fires an arrow that leaves a thin trail of green life-light flowing back toward her, swamp of glowing "
     "moss at dusk.",
     "three quick slashes, thin crimson-violet threads of light flowing back from the blades into her hands."),
    ("Chain", "attack", "light",
     "she draws a second arrow while the first is still in flight, two golden streaks in a row, windy cliff path.",
     "two chained slashes, a zigzag violet streak of light linking them."),
    ("Sunder", "attack", "light",
     "a heavy armor-piercing arrow with a broad steel head strikes a shattered iron shield half-buried in the "
     "ground, sparks flying, battlefield ruins in the forest.",
     "the blades come down on the iron shield and split it, violet sparks."),
    ("Shatter", "attack", "intermediate",
     "she slams the saber point into the ground and a ring of violet glass-like shards bursts outward, cracked "
     "stone plaza in the forest at night.",
     "she drives the blades into the ground, a larger ring of violet shards bursts outward."),
    ("Pursuit", "attack", "light",
     "she runs and shoots at the same time, leaping over a fallen log, a golden arrow streak toward screen RIGHT, "
     "dense forest trail.",
     "she runs and slashes, a long violet streak of light trailing behind her."),
    ("Release", "attack", "intermediate",
     "she throws her free arm open and several violet wisps burst outward from her, turning into golden light as "
     "they fly, starry forest clearing.",
     "the wisps stay violet and burst outward like a nova around her blades."),
    # ---- uncommon skills
    ("DarkPact", "skill", "intermediate",
     "a single drop of glowing violet light falls from her open palm onto a dark altar stone, which lights up, "
     "ruined altar in a dark forest. Non-graphic, no blood.",
     "the altar blazes violet; she holds the blades crossed above it."),
    ("Spread", "skill", "intermediate",
     "violet veins of curse light creep outward across the ground from her feet, withering the grass, her saber "
     "lowered, dying grove.",
     "the violet veins climb the trees around her, blades raised."),
    ("Mirror", "skill", "intermediate",
     "she holds up a small round mirror of moonlight that reflects a glowing leaf, and a second identical glowing "
     "leaf appears beside the first, quiet moonlit glade.",
     "the mirror light turns violet and the copied leaf turns black, her blades held low."),
    ("CurseBarrier", "skill", "light",
     "she plants her bow upright and a tall dome of golden-green light rises around her, rain falling on the "
     "dome, stormy forest.",
     "the dome is violet and cracked, she stands inside with the blades crossed."),
    ("Rewind", "skill", "light",
     "a fallen arrow floats back up from the ground into her open hand along a golden spiral of light, clearing "
     "at dusk.",
     "a violet spiral of light curls back into her hands around the blades."),
    ("ForestBreath", "skill", "light",
     "she breathes in deeply, eyes closed, and a stream of glowing green leaves flows toward her from the forest, "
     "ancient sunlit grove.",
     "the stream of leaves turns black-violet as it reaches her."),
    ("Turnabout", "skill", "intermediate",
     "she turns sharply mid-step, half of her cloak glowing gold and the other half violet, a swirl of gold and "
     "violet light around her, crossroads of two forest paths.",
     "the swirl of light is fully violet now as she turns with the blades drawn."),
    ("Insight", "skill", "light",
     "close portrait, her eyes glow faintly gold as faint constellations of light appear in the air around her, "
     "night forest under stars.",
     "her eyes glow violet and the constellations turn violet."),
    ("Offering", "skill", "intermediate",
     "she offers a small cracked violet crystal on her open palm to the glowing roots of an ancient tree, which "
     "absorb its light, tree hollow lit by fireflies.",
     "the crystal's violet energy flows into her blades instead."),
    ("BowGuard", "skill", "light",
     "she holds her longbow horizontally in front of her like a barrier, the string glowing and spreading into a "
     "wall of golden threads, foggy forest bridge.",
     "she holds the blades horizontally in front of her, a wall of violet threads of light between them."),
    ("Bind", "skill", "intermediate",
     "violet chains of light shoot from her outstretched free hand toward an unseen enemy on the right, "
     "overgrown dungeon entrance in the forest.",
     "violet chains of light shoot from the blades toward the right."),
    ("Steady", "skill", "light",
     "she kneels on one knee with the bow resting across her thighs, eyes closed in meditation, a calm golden "
     "aura, mossy riverside at sunset.",
     "she kneels with the blades resting across her thighs, a calm violet aura."),
    # ---- rare attacks
    ("ThousandBlades", "attack", "intermediate",
     "countless faint violet and gold streaks of light fan out around her single saber like a blooming flower, "
     "night storm in the forest. The streaks are light only, not blades.",
     "the fan of light streaks around her blades is fully violet."),
    ("Starfall", "attack", "light",
     "she aims straight up at the night sky and a huge golden falling star streaks down toward screen RIGHT, "
     "mountain ridge above the forest.",
     "she drops from the sky with the blades raised, a violet falling-star streak of light behind her."),
    ("Devour", "attack", "intermediate",
     "violet curse smoke from all around is sucked into the tip of her single saber, which glows white-hot, "
     "swirling vortex in a dark grove.",
     "the vortex of smoke is pulled into her blades."),
    ("Echo", "attack", "light",
     "the twang of her bowstring sends a visible golden ripple of sound through the air with an arrow inside it, "
     "echoing canyon with tall cliffs.",
     "a slash sends a violet ripple of sound through the canyon."),
    ("Execution", "attack", "light",
     "a single decisive shot at dawn, a long golden streak and a burst of light at the far right edge, foggy "
     "battlefield with broken banners.",
     "a single decisive downward strike, a vertical violet streak of light splitting the fog."),
    # ---- rare skills
    ("TwinSoul", "skill", "intermediate",
     "her shadow on the ground glows violet and holds a different pose than her, as if alive, moonlit forest "
     "clearing. The shadow is flat on the ground, not a second person.",
     "the shadow on the ground blazes violet beneath her."),
    ("Overflow", "skill", "intermediate",
     "violet and gold light overflows from a cracked crystal vessel in her free hand, spilling across the ground "
     "like liquid, ruined fountain in a forest.",
     "the overflowing light is all violet and pours over her blades."),
    ("TimeCurse", "skill", "intermediate",
     "a giant translucent ring of light like a clock face without numbers floats behind her, leaves frozen in the "
     "air, twilight forest.",
     "the ring of light turns violet-black and spins, the frozen leaves crumbling."),
    ("GreatSeal", "skill", "light",
     "she stands at the center of an enormous golden seal circle of leaves and light on the ground, beams rising "
     "around her, ancient stone circle in the forest.",
     "the giant seal circle is cracked, violet light bursting from the cracks around her."),
    ("Avatar", "skill", "intermediate",
     "she floats slightly above the ground wrapped in a towering aura of violet and gold flames, hair and cloak "
     "rising, storm clouds over the forest.",
     "the towering aura is pure violet, her eyes blazing, blades spread wide."),
    ("WorldtreeVow", "skill", "light",
     "she kneels before the colossal glowing World Tree as its golden roots rise to shield her, amber light, vast "
     "ancient forest.",
     "the World Tree's roots shield her in violet light; she kneels with the blades planted in the ground."),
]

# Power-card art (single face): id, form, scene.
POWERS = [
    ("Resonance", "intermediate", "she stands still as rings of violet and gold light resonate outward from her "
     "chest in perfect circles, crystal cavern with glowing pools."),
    ("Recollection", "intermediate", "memories float around her as translucent leaves of light, each holding a "
     "faint glowing image, autumn grove at dusk."),
    ("Bloodblades", "dark", "crimson-violet light runs along the edges of her two curved blades as she raises "
     "them crossed before her face, blood-red moon over a dead forest."),
    ("Radiance", "light", "golden radiance pours from her raised bow like a sunrise, forest mist burning away, "
     "mountain overlook at dawn."),
    ("BacklashEcho", "dark", "violet shockwaves of light burst from her body as she clenches her blades in pain "
     "and anger, cracked earth in a dark forest."),
    ("Composure", "light", "she sits calmly on a branch of a huge tree, bow across her lap, golden fireflies "
     "around her, peaceful night forest."),
    ("CreepingCurse", "dark", "violet curse veins creep up her arms from her two blades while she watches them "
     "calmly, overgrown graveyard in a dark forest."),
    ("Vigil", "light", "she keeps watch on a high stone tower above the forest at night, bow in hand, a golden "
     "lantern glowing beside her, starry sky."),
    ("Afterglow", "intermediate", "long violet and golden afterglow streaks linger in the air around her saber at "
     "sunset, a glowing lake reflecting the sky."),
    ("Evernight", "dark", "she stands under an eternal night sky with a violet eclipse, blades lowered, stars "
     "burning violet over an endless dark forest."),
    ("CursedHeart", "dark", "a violet heart-shaped crack of light shines through her chest armor as she grips "
     "her blades, storm-lit ruins."),
    ("LightOath", "light", "she swears an oath with one hand raised and the bow held to her heart, a column of "
     "golden light from above, sacred grove of white trees."),
]

# Power icons: id, palette reference (light | dark), subject.
ICONS = [
    ("Resonance", "light", "a small golden tuning fork with two bold violet ripple rings around it"),
    ("Recollection", "light", "a single glowing green leaf inside a circular arrow of golden light"),
    ("Bloodblades", "dark", "two crossed curved silver blades with crimson-violet glowing edges"),
    ("Radiance", "light", "a radiant golden sun disc with short thick rays"),
    ("BacklashEcho", "dark", "a cracked violet heart sending out one bold shockwave ring"),
    ("Composure", "light", "a smooth round river stone with one small green leaf resting on top"),
    ("CreepingCurse", "dark", "a thorny violet vine curling around a small silver blade"),
    ("Vigil", "light", "a small golden lantern with a warm flame"),
    ("MoonAwakening", "light", "a crescent moon cradling a single green leaf"),
    ("Afterglow", "light", "a half sun sinking behind a violet horizon line"),
    ("Evernight", "dark", "a black-violet eclipse disc with a thin glowing violet rim"),
    ("CursedHeart", "dark", "a dark heart with one bold glowing violet crack"),
    ("LightOath", "light", "a golden laurel wreath around a small bright star"),
    ("Nock", "light", "a single golden arrowhead pointing right"),
    ("Steady", "light", "a smooth river stone balanced on another stone, a small green leaf beside them"),
    ("Echo", "light", "two concentric golden sound rings around a small arrowhead"),
    ("TwinSoul", "dark", "two overlapping circles, one gold and one violet, with no symbols inside"),
]

# Relics: id, palette, subject. The chosen CrackedSeal is heart-shaped, so WholeSeal matches it.
RELICS = [
    ("WholeSeal", "light", "a HEART-SHAPED ivory-heartwood seal medallion with an antique gold rim and a simple "
     "engraved leaf crest, perfectly intact with no crack, soft warm golden glow. No chain, cord or stand."),
    ("DuskGauntlet", "dark", "a single dark leather and steel gauntlet with glowing violet engraved vein lines"),
    ("SilverleafBrooch", "light", "a silver brooch shaped like a single leaf with a small green gem"),
    ("MirrorEarring", "dark", "a single crescent-shaped silver earring holding a small round mirror that reflects "
     "violet light"),
    ("MossCharm", "light", "a small round river-stone charm wrapped in soft green moss with one tiny white flower"),
    ("EclipseStone", "dark", "a round polished black stone showing a violet eclipse with a thin golden ring"),
    ("Whetstone_Elf", "dark", "a rectangular grey whetstone with a thin glowing violet edge"),
    ("CursedCrown", "dark", "a thin dark silver circlet crown with violet thorny spikes and one glowing violet gem"),
]


def light_prompt(form, scene):
    return " ".join([IDENTITY[form], "Horizontal card illustration, " + scene, b1.STYLE])


def contained(form, size):
    return {"image_1": {"path": b1.DESIGN.format("elf" if form == "light" else form),
                        "prepare": f"contain in {size[0]}x{size[1]} white canvas"}}


def build():
    jobs = []
    seed = BASE_SEED

    def add(job, n):
        nonlocal seed
        job["seeds"] = [seed + k for k in range(n)]
        job["candidates"] = n
        job["out_dir"] = f"{OUT_ROOT}/{job['id']}"
        seed += 100
        jobs.append(job)

    for cid, ctype, form, light, cursed in DUAL:
        add({"id": cid, "round": 1, "kind": "card", "card_type": ctype, "face": form, "size": [1024, 768],
             "images": contained(form, (1024, 768)), "prompt": light_prompt(form, light),
             "runtime": b1.runtime("card", cid, ctype)}, LIGHT_CANDIDATES)
    for pid, form, scene in POWERS:
        add({"id": pid + "_card", "round": 1, "kind": "card", "card_type": "power", "face": form,
             "size": [1024, 768], "images": contained(form, (1024, 768)), "prompt": light_prompt(form, scene),
             "runtime": b1.runtime("card", pid, "power")}, LIGHT_CANDIDATES)
    for iid, pal, subject in ICONS:
        add({"id": iid + "_icon", "round": 1, "kind": "power_icon", "card_type": None, "face": None,
             "size": [1024, 1024], "images": contained("elf" if pal == "light" else "dark", (1024, 1024)),
             "prompt": b1.ICON + " Subject: " + subject + ". Must read clearly at 32 pixels; very few shapes.",
             "post": "BiRefNet matting (birefnet.safetensors)", "runtime": b1.runtime("power_icon", iid, None)},
            LIGHT_CANDIDATES)
    for rid, pal, subject in RELICS:
        add({"id": rid, "round": 1, "kind": "relic", "card_type": None, "face": None, "size": [1024, 1024],
             "images": contained("elf" if pal == "light" else "dark", (1024, 1024)),
             "prompt": b1.ICON + " Subject: " + subject + ".",
             "post": "BiRefNet matting (birefnet.safetensors)", "runtime": b1.runtime("relic", rid, None)},
            LIGHT_CANDIDATES)
    for cid, ctype, form, light, cursed in DUAL:
        add({"id": cid + "_cursed", "round": 2, "kind": "card", "card_type": ctype, "face": "dark",
             "size": [1024, 768], "depends_on": cid,
             "images": {"image_1": {"path": f"{OUT_ROOT}/{cid}/chosen.png", "prepare": "as is (1024x768)"}},
             "prompt": " ".join([b1.DARK_EDIT, cursed, b1.STYLE]),
             "runtime": b1.runtime("card", cid + "_cursed", ctype)}, CURSED_CANDIDATES)
    return jobs


def main():
    # "elf" is how contained() names the light design folder; map it for icons too.
    jobs = build()
    for j in jobs:
        img = j["images"]["image_1"]
        img["path"] = img["path"].replace("/designs/light/", "/designs/elf/")
    doc = {"batch": "B2-B5", "design": "cards-relics-design.md v4", "plan": "art-production-plan.md",
           "workflow": b1.WORKFLOW, "jobs": jobs,
           "counts": {r: sum(len(j["seeds"]) for j in jobs if j["round"] == r) for r in (1, 2)}}
    path = os.path.join(b1.DOCS, "art-all-jobs.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(doc, f, ensure_ascii=False, indent=2)
    print(len(jobs), "jobs,", doc["counts"], "images ->", path)


if __name__ == "__main__":
    main()
