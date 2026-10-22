"""Source of truth for the elf's text. Writes src/main/resources/elfprototype/localization/{zhs,eng}/*.json.

Card text: !D! damage, !B! block, !M! magic (hit count on multi-hit faces), {M2} second number, [E] energy.
Chinese: keywords and numbers are separated by spaces so the game finds them.
Dual cards: (id, light name, light text, cursed name, cursed text, light upgraded text or None, cursed upgraded text or None)

Usage: python tools/build_strings.py
"""
import json
import os

ROOT = os.path.join(os.path.dirname(os.path.dirname(os.path.abspath(__file__))),
                    "src", "main", "resources", "elfprototype", "localization")
P = "elfprototype:"

HITS_ZH = "造成 !D! 点伤害 !M! 次。"
HIT_ZH = "造成 !D! 点伤害。"
HITS_EN = "Deal !D! damage !M! times."
HIT_EN = "Deal !D! damage."

DUAL_ZH = [
    ("Strike_Elf", "打击", HIT_ZH, "蚀·打击", HITS_ZH, None, None),
    ("Defend_Elf", "防御", "获得 !B! 点 格挡 。", "蚀·防御", "获得 !B! 点 格挡 。 NL 对所有敌人造成 !D! 点伤害。", None, None),
    ("CurseWhisper", "咒之低语", "侵蚀 !M! 张其他手牌。 NL 抽 {M2} 张牌。", "蚀·咒之低语", "侵蚀 !M! 张其他手牌。", None,
     "侵蚀 !M! 张其他手牌。 NL 抽 {M2} 张牌。"),
    ("SingleEdge", "单刃斩", HIT_ZH + " NL 若本回合已 翻面 ，抽 1 张牌。", "蚀·单刃斩", HITS_ZH, None, None),
    ("ThicketArrow", "穿林箭", HIT_ZH, "影刺", HITS_ZH, None, None),
    ("QuickArrow", "快箭", HIT_ZH + " NL 抽 1 张牌。", "疾刃", HITS_ZH, None, None),
    ("ScatterShot", "散射", "对所有敌人造成 !D! 点伤害。", "旋刃", "对所有敌人造成 !D! 点伤害 !M! 次。", None, None),
    ("SnareShot", "绊索箭", HIT_ZH + " NL 给予 {M2} 层 虚弱 。", "断筋", HITS_ZH + " NL 给予 {M2} 层 易伤 。", None, None),
    ("ChargedShot", "蓄力射击", HIT_ZH + " NL 本牌的 咒鸣 加成翻倍。", "暗月斩", HITS_ZH, None, None),
    ("CorruptingEdge", "侵蚀之刃", HIT_ZH + " NL 侵蚀 1 张其他手牌。", "蚀·侵蚀之刃", HITS_ZH + " NL 侵蚀 1 张其他手牌。", None, None),
    ("GladeThrust", "林间突刺", HIT_ZH + " NL 获得 !B! 点 格挡 。", "影突", HITS_ZH, None, None),
    ("Riposte", "反手刺", HIT_ZH + " NL 封印 1 张手牌。", "蚀·反手刺", HITS_ZH + " NL 封印 1 张其他手牌。", None, None),
    ("EmberShot", "余烬箭", HIT_ZH + " NL 本牌的 咒鸣 加成 ×3 。", "余烬刃", HITS_ZH, None, None),
    ("Recoil", "弓弦回拉", HIT_ZH + " NL 若手中有蚀面牌，抽 1 张牌。", "回旋刃", HITS_ZH + " NL 抽 1 张牌。", None, None),
    ("LeafWard", "林叶护持", "获得 !B! 点 格挡 。", "影甲", "获得 !B! 点 格挡 。 NL 侵蚀 1 张其他手牌。", None, None),
    ("HoldBreath", "屏息", "获得 !B! 点 格挡 。 NL 抽 1 张牌。", "蚀·屏息", "抽 !M! 张牌。", None, None),
    ("SealingRite", "封印术", "获得 !B! 点 格挡 。 NL 封印 2 张手牌。", "蚀·封印术", "封印 !M! 张其他手牌。 NL 抽 2 张牌。", None, None),
    ("Invite", "引咒", "侵蚀 2 张其他手牌。 NL 抽 1 张牌。", "蚀·引咒", "侵蚀 2 张其他手牌。 NL 抽 2 张牌。", None, None),
    ("Moonstep", "月光步", "获得 !B! 点 格挡 。 NL 若本回合已 翻面 ，抽 1 张牌。", "夜步", "获得 !B! 点 格挡 。 NL 抽 1 张牌。", None, None),
    ("PurifyingLight", "净化之光", "封印 手中所有蚀面牌，每张获得 !M! 点 格挡 。", "蚀·净化之光",
     "封印 手中其他所有蚀面牌，每张抽 1 张牌。", None, None),
    ("CurseWard", "诅咒之盾", "获得 !B! 点 格挡 。 NL 手中每有 1 张蚀面牌，多 !M! 点。", "蚀·诅咒之盾",
     "手中每有 1 张其他蚀面牌，获得 !M! 点 格挡 。 NL （共 !B! 点）", None, None),
    ("Premonition", "预感", "查看抽牌堆顶 !M! 张牌，将其中 1 张放入手牌。", "窥视",
     "查看抽牌堆顶 !M! 张牌，将其中 1 张放入手牌并 侵蚀 它。", None, None),
    ("Nock", "张弓", "本回合下一张光面攻击牌伤害 +!M! 。", "蓄势", "本回合下一张蚀面攻击牌每段伤害 +!M! 。", None, None),
    ("Unbind", "解咒", "封印 !M! 张手牌。 NL 抽 {M2} 张牌。", "蚀·解咒", "封印 !M! 张其他手牌。", None,
     "封印 !M! 张其他手牌。 NL 抽 {M2} 张牌。"),
    ("TwinMoon", "双月斩", HIT_ZH, "蚀·双月斩", HITS_ZH, None, None),
    ("Skim", "掠影", HIT_ZH + " NL 侵蚀 1 张其他手牌。", "蚀·掠影", HITS_ZH + " NL 侵蚀 1 张其他手牌。", None, None),
    ("Heartseeker", "穿心箭", HIT_ZH + " NL 若目标有 虚弱 ，多造成 !M! 点。", "裂心", HITS_ZH + " NL 给予 {M2} 层 易伤 。", None, None),
    ("Harvest", "收割", HIT_ZH + " NL 若击杀，获得 [E] 。", "蚀·收割", HITS_ZH + " NL 每段击杀抽 1 张牌。", None, None),
    ("Volley", "箭雨", "对所有敌人造成 !D! 点伤害。", "刃舞", "对所有敌人造成 !D! 点伤害 !M! 次。", None, None),
    ("MirrorEdge", "镜刃", HIT_ZH + " NL 封印 1 张手牌，获得 !B! 点 格挡 。", "蚀·镜刃", HITS_ZH + " NL 封印 2 张其他手牌。", None, None),
    ("Leech", "噬血刃", HIT_ZH, "蚀·噬血刃", HITS_ZH + " NL 每段造成伤害时回复 1 点生命。", None, None),
    ("Chain", "连珠", HITS_ZH + " NL 本回合打出过蚀面牌时多 1 次。", "连斩", HITS_ZH + " NL 本回合打出过光面牌时多 2 次。", None, None),
    ("Sunder", "破甲箭", "移除目标所有 格挡 ，造成 !D! 点伤害。", "碎甲", HITS_ZH, None, None),
    ("Shatter", "碎咒", "对所有敌人造成 !D! 点伤害（ 咒鸣 ×2 ）。", "蚀·碎咒", "对所有敌人造成 !D! 点伤害 !M! 次。",
     "对所有敌人造成 !D! 点伤害（ 咒鸣 ×3 ）。", None),
    ("Pursuit", "追影", HIT_ZH + " NL 本回合 咒鸣 达到 4 时费用为 0 。", "蚀·追影", HITS_ZH + " NL 本回合 咒鸣 达到 4 时费用为 0 。", None, None),
    ("Release", "解放", "封印 手中所有蚀面牌，每张对所有敌人造成 !M! 点伤害。", "蚀·解放",
     "封印 手中其他所有蚀面牌，每张对所有敌人造成 !M! 点伤害。", None, None),
    ("DarkPact", "暗契", "失去 !M! 点生命，获得 [E] 。 NL 消耗 。", "蚀·暗契", "失去 !M! 点生命，获得 [E] [E] 。 NL 消耗 。", None, None),
    ("Spread", "蔓延", "侵蚀 手中所有其他牌。", "蚀·蔓延", "侵蚀 手中所有其他牌。 NL 抽 2 张牌。", None, None),
    ("Mirror", "镜像", "复制 1 张手牌放入手牌，复制品是光面并带有 消耗 。", "蚀·镜像",
     "复制 1 张手牌放入手牌，复制品带有 消耗 ，并 侵蚀 它。", None, None),
    ("CurseBarrier", "咒之屏障", "获得 !B! 点 格挡 。", "蚀·咒之屏障", "获得 !B! 点 格挡 。 NL 侵蚀 2 张其他手牌。", None, None),
    ("Rewind", "回溯", "将弃牌堆中 1 张牌放入手牌。", "蚀·回溯", "将弃牌堆中 1 张牌放入手牌并 侵蚀 它。", None, None),
    ("ForestBreath", "林间呼吸", "抽 !M! 张牌。", "蚀·林间呼吸", "抽 !M! 张牌，并 侵蚀 抽到的牌。", None, None),
    ("Turnabout", "转念", "侵蚀 1 张其他手牌，再 封印 1 张手牌（可以是同一张）。", "蚀·转念",
     "侵蚀 2 张其他手牌，再 封印 2 张手牌。", "侵蚀 1 张其他手牌，再 封印 1 张手牌（可以是同一张）。 NL 抽 {M2} 张牌。", None),
    ("Insight", "灵视", "获得 !B! 点 格挡 （ 咒鸣 ×2 ）。", "蚀·灵视", "抽 咒鸣 的一半（向下取整）张牌。",
     "获得 !B! 点 格挡 （ 咒鸣 ×3 ）。", None),
    ("Offering", "献咒", "消耗 1 张手牌，获得 [E] ；若是诅咒牌，改为获得 [E] [E] 。", "蚀·献咒", "消耗 1 张手牌，抽 2 张牌。", None, None),
    ("BowGuard", "弓之守", "获得 !B! 点 格挡 。 NL 下回合获得 4 点 格挡 。", "蚀·弓之守",
     "获得 !B! 点 格挡 ，抽 1 张牌。 NL 下回合获得 4 点 格挡 。", None, None),
    ("Bind", "咒缚", "给予 !M! 层 虚弱 。", "蚀·咒缚", "给予 !M! 层 易伤 。 NL 侵蚀 1 张其他手牌。", None, None),
    ("Steady", "定神", "获得 !B! 点 格挡 。 NL 本回合结束时不触发 反噬 。", "蚀·定神", "抽 !M! 张牌。 NL 本回合结束时不触发 反噬 。", None, None),
    ("ThousandBlades", "千刃", "造成 !D! 点伤害（ 咒鸣 ×3 ）。", "蚀·千刃", "造成 !D! 点伤害 !M! 次（次数等于 咒鸣 ）。",
     "造成 !D! 点伤害（ 咒鸣 ×4 ）。", None),
    ("Starfall", "星落之箭", HIT_ZH + " NL 本回合 咒鸣 达到 5 时费用 -2 。", "坠星", HITS_ZH + " NL 本回合 咒鸣 达到 5 时费用 -2 。", None, None),
    ("Devour", "噬咒", "封印 手中所有蚀面牌，每张使本牌伤害 +6 ，然后造成 !D! 点伤害。", "蚀·噬咒",
     "侵蚀 手中所有其他牌，每张使本牌多 1 段，然后造成 !D! 点伤害 !M! 次。", None, None),
    ("Echo", "回响", HIT_ZH + " NL 本回合下一张蚀面牌打出两次。", "蚀·回响", HITS_ZH + " NL 本回合下一张光面牌打出两次。", None, None),
    ("Execution", "处刑", HIT_ZH + " NL 若击杀，本回合 咒鸣 翻倍。", "蚀·处刑", HITS_ZH + " NL 若击杀，获得 [E] [E] 。", None, None),
    ("TwinSoul", "双生", "本回合，你的光面牌打出后自动 侵蚀 并回到手牌。 NL 消耗 。", "蚀·双生",
     "本回合，你的光面牌打出后自动 侵蚀 并回到手牌。 NL 抽 2 张牌。 NL 消耗 。", None, None),
    ("Overflow", "咒满", "获得 !B! 点 格挡 （ 咒鸣 ×4 ）。", "蚀·咒满", "咒鸣 翻倍。 NL 消耗 。", "获得 !B! 点 格挡 （ 咒鸣 ×5 ）。", None),
    ("TimeCurse", "时之咒", "抽 5 张牌。 NL 消耗 。", "蚀·时之咒", "抽 5 张牌，并 侵蚀 抽到的牌。 NL 消耗 。", None, None),
    ("GreatSeal", "大封印", "获得 !B! 点 格挡 ， 封印 手中所有蚀面牌。", "蚀·大封印",
     "获得 !B! 点 格挡 ， 封印 手中其他所有蚀面牌，每张获得 [E] （最多 3 点）。", None, None),
    ("Avatar", "化身", "本回合 咒鸣 +!M! 。 NL 消耗 。", "蚀·化身", "失去 5 点生命，本回合 咒鸣 +!M! 。 NL 消耗 。", None, None),
    ("WorldtreeVow", "世界树之誓", "获得 !B! 点 格挡 。 NL 下回合获得 [E] 。", "蚀·世界树之誓",
     "获得 !B! 点 格挡 。 NL 下回合获得 [E] [E] 并多抽 2 张牌。", None, None),
]

DUAL_EN = {
    "Strike_Elf": ("Strike", HIT_EN, "Cursed Strike", HITS_EN),
    "Defend_Elf": ("Defend", "Gain !B! Block.", "Cursed Defend", "Gain !B! Block. NL Deal !D! damage to ALL enemies."),
    "CurseWhisper": ("Curse Whisper", "Erode !M! other card. NL Draw {M2} card(s).", "Cursed Whisper",
                     "Erode !M! other cards.", None, "Erode !M! other cards. NL Draw {M2} card."),
    "SingleEdge": ("Single Edge", HIT_EN + " NL If you flipped a card this turn, draw 1 card.", "Cursed Edge", HITS_EN),
    "ThicketArrow": ("Thicket Arrow", HIT_EN, "Shadow Stab", HITS_EN),
    "QuickArrow": ("Quick Arrow", HIT_EN + " NL Draw 1 card.", "Quick Blades", HITS_EN),
    "ScatterShot": ("Scatter Shot", "Deal !D! damage to ALL enemies.", "Whirling Blades",
                    "Deal !D! damage to ALL enemies !M! times."),
    "SnareShot": ("Snare Shot", HIT_EN + " NL Apply {M2} Weak.", "Hamstring", HITS_EN + " NL Apply {M2} Vulnerable."),
    "ChargedShot": ("Charged Shot", HIT_EN + " NL Chime bonus is doubled on this card.", "Dark Moon Slash", HITS_EN),
    "CorruptingEdge": ("Corrupting Edge", HIT_EN + " NL Erode 1 other card.", "Cursed Corrupting Edge",
                       HITS_EN + " NL Erode 1 other card."),
    "GladeThrust": ("Glade Thrust", HIT_EN + " NL Gain !B! Block.", "Shadow Thrust", HITS_EN),
    "Riposte": ("Riposte", HIT_EN + " NL Seal 1 card.", "Cursed Riposte", HITS_EN + " NL Seal 1 other card."),
    "EmberShot": ("Ember Shot", HIT_EN + " NL Chime bonus x3 on this card.", "Ember Blades", HITS_EN),
    "Recoil": ("Recoil", HIT_EN + " NL If a cursed card is in your hand, draw 1 card.", "Boomerang Blades",
               HITS_EN + " NL Draw 1 card."),
    "LeafWard": ("Leaf Ward", "Gain !B! Block.", "Shadow Plate", "Gain !B! Block. NL Erode 1 other card."),
    "HoldBreath": ("Hold Breath", "Gain !B! Block. NL Draw 1 card.", "Cursed Breath", "Draw !M! cards."),
    "SealingRite": ("Sealing Rite", "Gain !B! Block. NL Seal 2 cards.", "Cursed Rite",
                    "Seal !M! other cards. NL Draw 2 cards."),
    "Invite": ("Invite", "Erode 2 other cards. NL Draw 1 card.", "Cursed Invite", "Erode 2 other cards. NL Draw 2 cards."),
    "Moonstep": ("Moonstep", "Gain !B! Block. NL If you flipped a card this turn, draw 1 card.", "Nightstep",
                 "Gain !B! Block. NL Draw 1 card."),
    "PurifyingLight": ("Purifying Light", "Seal every cursed card in your hand. Gain !M! Block for each.",
                       "Cursed Light", "Seal every other cursed card in your hand. Draw 1 card for each."),
    "CurseWard": ("Curse Ward", "Gain !B! Block. NL +!M! for each cursed card in your hand.", "Cursed Ward",
                  "Gain !M! Block for each other cursed card in your hand. NL (Total !B!)"),
    "Premonition": ("Premonition", "Look at the top !M! cards of your draw pile. Put 1 into your hand.", "Glimpse",
                    "Look at the top !M! cards of your draw pile. Put 1 into your hand and Erode it."),
    "Nock": ("Nock", "Your next light attack this turn deals !M! more damage.", "Bide",
             "Your next cursed attack this turn deals !M! more damage per hit."),
    "Unbind": ("Unbind", "Seal !M! card(s). NL Draw {M2} card(s).", "Cursed Unbind", "Seal !M! other cards.", None,
               "Seal !M! other cards. NL Draw {M2} card."),
    "TwinMoon": ("Twin Moon", HIT_EN, "Cursed Twin Moon", HITS_EN),
    "Skim": ("Skim", HIT_EN + " NL Erode 1 other card.", "Cursed Skim", HITS_EN + " NL Erode 1 other card."),
    "Heartseeker": ("Heartseeker", HIT_EN + " NL Deals !M! more damage to a Weak enemy.", "Heartsplit",
                    HITS_EN + " NL Apply {M2} Vulnerable."),
    "Harvest": ("Harvest", HIT_EN + " NL If this kills, gain [E] .", "Cursed Harvest",
                HITS_EN + " NL Draw 1 card for each killing hit."),
    "Volley": ("Volley", "Deal !D! damage to ALL enemies.", "Blade Dance", "Deal !D! damage to ALL enemies !M! times."),
    "MirrorEdge": ("Mirror Edge", HIT_EN + " NL Seal 1 card. Gain !B! Block.", "Cursed Mirror Edge",
                   HITS_EN + " NL Seal 2 other cards."),
    "Leech": ("Leech", HIT_EN, "Cursed Leech", HITS_EN + " NL Heal 1 HP for each hit that deals damage."),
    "Chain": ("Chain Shot", HITS_EN + " NL +1 hit if you played a cursed card this turn.", "Chain Slash",
              HITS_EN + " NL +2 hits if you played a light card this turn."),
    "Sunder": ("Sunder", "Remove all Block from the enemy. Deal !D! damage.", "Shatter Armor", HITS_EN),
    "Shatter": ("Shatter", "Deal !D! damage to ALL enemies (Chime x2).", "Cursed Shatter",
                "Deal !D! damage to ALL enemies !M! times.", "Deal !D! damage to ALL enemies (Chime x3)."),
    "Pursuit": ("Pursuit", HIT_EN + " NL Costs 0 once Chime is 4 or more.", "Cursed Pursuit",
                HITS_EN + " NL Costs 0 once Chime is 4 or more."),
    "Release": ("Release", "Seal every cursed card in your hand. Deal !M! damage to ALL enemies for each.",
                "Cursed Release", "Seal every other cursed card in your hand. Deal !M! damage to ALL enemies for each."),
    "DarkPact": ("Dark Pact", "Lose !M! HP. Gain [E] . NL Exhaust.", "Cursed Pact", "Lose !M! HP. Gain [E] [E] . NL Exhaust."),
    "Spread": ("Spread", "Erode every other card in your hand.", "Cursed Spread",
               "Erode every other card in your hand. NL Draw 2 cards."),
    "Mirror": ("Mirror", "Add a copy of a card in your hand to your hand. The copy is light and has Exhaust.",
               "Cursed Mirror", "Add a copy of a card in your hand to your hand. The copy has Exhaust and is Eroded."),
    "CurseBarrier": ("Curse Barrier", "Gain !B! Block.", "Cursed Barrier", "Gain !B! Block. NL Erode 2 other cards."),
    "Rewind": ("Rewind", "Put a card from your discard pile into your hand.", "Cursed Rewind",
               "Put a card from your discard pile into your hand and Erode it."),
    "ForestBreath": ("Forest Breath", "Draw !M! cards.", "Cursed Breath of the Forest", "Draw !M! cards and Erode them."),
    "Turnabout": ("Turnabout", "Erode 1 other card, then Seal 1 card (it may be the same one).", "Cursed Turnabout",
                  "Erode 2 other cards, then Seal 2 cards.",
                  "Erode 1 other card, then Seal 1 card (it may be the same one). NL Draw {M2} card."),
    "Insight": ("Insight", "Gain !B! Block (Chime x2).", "Cursed Insight", "Draw cards equal to half your Chime.",
                "Gain !B! Block (Chime x3)."),
    "Offering": ("Offering", "Exhaust a card in your hand. Gain [E] , or [E] [E] if it was a Curse.", "Cursed Offering",
                 "Exhaust a card in your hand. Draw 2 cards."),
    "BowGuard": ("Bow Guard", "Gain !B! Block. NL Next turn, gain 4 Block.", "Cursed Guard",
                 "Gain !B! Block. Draw 1 card. NL Next turn, gain 4 Block."),
    "Bind": ("Bind", "Apply !M! Weak.", "Cursed Bind", "Apply !M! Vulnerable. NL Erode 1 other card."),
    "Steady": ("Steady", "Gain !B! Block. NL No Backlash at the end of this turn.", "Cursed Steady",
               "Draw !M! cards. NL No Backlash at the end of this turn."),
    "ThousandBlades": ("Thousand Blades", "Deal !D! damage (Chime x3).", "Cursed Thousand Blades",
                       "Deal !D! damage !M! times (hits = Chime).", "Deal !D! damage (Chime x4)."),
    "Starfall": ("Starfall", HIT_EN + " NL Costs 2 less once Chime is 5 or more.", "Falling Star",
                 HITS_EN + " NL Costs 2 less once Chime is 5 or more."),
    "Devour": ("Devour", "Seal every cursed card in your hand, +6 damage for each, then deal !D! damage.",
               "Cursed Devour", "Erode every other card in your hand, +1 hit for each, then deal !D! damage !M! times."),
    "Echo": ("Echo", HIT_EN + " NL This turn, your next cursed card is played twice.", "Cursed Echo",
             HITS_EN + " NL This turn, your next light card is played twice."),
    "Execution": ("Execution", HIT_EN + " NL If this kills, double your Chime.", "Cursed Execution",
                  HITS_EN + " NL If this kills, gain [E] [E] ."),
    "TwinSoul": ("Twin Soul", "This turn, light cards Erode themselves after being played and return to your hand. NL Exhaust.",
                 "Cursed Twin Soul",
                 "This turn, light cards Erode themselves after being played and return to your hand. NL Draw 2 cards. NL Exhaust."),
    "Overflow": ("Overflow", "Gain !B! Block (Chime x4).", "Cursed Overflow", "Double your Chime. NL Exhaust.",
                 "Gain !B! Block (Chime x5)."),
    "TimeCurse": ("Time Curse", "Draw 5 cards. NL Exhaust.", "Cursed Time", "Draw 5 cards and Erode them. NL Exhaust."),
    "GreatSeal": ("Great Seal", "Gain !B! Block. Seal every cursed card in your hand.", "Cursed Seal",
                  "Gain !B! Block. Seal every other cursed card in your hand, gain [E] for each (max 3)."),
    "Avatar": ("Avatar", "Gain !M! Chime. NL Exhaust.", "Cursed Avatar", "Lose 5 HP. Gain !M! Chime. NL Exhaust."),
    "WorldtreeVow": ("Worldtree Vow", "Gain !B! Block. NL Next turn, gain [E] .", "Cursed Vow",
                     "Gain !B! Block. NL Next turn, gain [E] [E] and draw 2 more cards."),
}

# Power cards: id -> (zh name, zh text, zh upgraded text or None, en name, en text, en upgraded or None)
POWER_CARDS = {
    "Resonance": ("共鸣", "每回合 咒鸣 第一次达到 4 时，获得 [E] 。", "固有 。 NL 每回合 咒鸣 第一次达到 4 时，获得 [E] 。",
                  "Resonance", "The first time Chime reaches 4 each turn, gain [E] .",
                  "Innate. NL The first time Chime reaches 4 each turn, gain [E] ."),
    "Recollection": ("回忆", "每回合前 !M! 次 封印 （包括自动封印），各抽 1 张牌。", None,
                     "Recollection", "The first !M! times you Seal a card each turn, draw 1 card.", None),
    "Bloodblades": ("暗血", "你的蚀面攻击每段伤害 +!M! 。", None,
                    "Bloodblades", "Your cursed attacks deal !M! more damage per hit.", None),
    "Radiance": ("光辉", "光面牌获得 格挡 时， 咒鸣 加成翻倍。", "固有 。 NL 光面牌获得 格挡 时， 咒鸣 加成翻倍。",
                 "Radiance", "Light cards get double Chime on Block.", "Innate. NL Light cards get double Chime on Block."),
    "BacklashEcho": ("血之回响", "反噬 失去生命时，对所有敌人造成失去生命 ×!M! 的伤害。", None,
                     "Backlash Echo", "When Backlash costs HP, deal HP lost x!M! damage to ALL enemies.", None),
    "Composure": ("静心", "回合结束时，手中每张光面牌给你 !M! 点 格挡 。", None,
                  "Composure", "At the end of your turn, gain !M! Block for each light card in your hand.", None),
    "CreepingCurse": ("蔓延之咒", "每回合抽牌后， 侵蚀 手中最左边 !M! 张光面牌。", None,
                      "Creeping Curse", "After drawing each turn, Erode the leftmost !M! light card(s) in your hand.", None),
    "Vigil": ("守望", "每次手动 封印 1 张牌，获得 !M! 点 格挡 。", None,
              "Vigil", "Whenever you Seal a card yourself, gain !M! Block.", None),
    "MoonAwakening": ("月辉觉醒", "每次 封印 （包括自动封印），抽 1 张牌并获得 [E] 。", None,
                      "Moon Awakening", "Whenever you Seal a card (including automatically), draw 1 card and gain [E] .", None),
    "Afterglow": ("余韵", "回合结束时， 咒鸣 只清除一半（向下取整）。", None,
                  "Afterglow", "At the end of your turn, Chime is only halved.", None),
    "Evernight": ("永夜", "你的蚀面攻击多造成 1 段。", None,
                  "Evernight", "Your cursed attacks hit 1 more time.", None),
    "CursedHeart": ("咒之心", "每次 反噬 ，获得 1 点 力量 。", "固有 。 NL 每次 反噬 ，获得 1 点 力量 。",
                    "Cursed Heart", "Whenever you Backlash, gain 1 Strength.", "Innate. NL Whenever you Backlash, gain 1 Strength."),
    "LightOath": ("光之誓", "光面牌的伤害和 格挡 ， 咒鸣 加成翻倍。", None,
                  "Light Oath", "Light cards get double Chime on damage and Block.", None),
}

# Powers: id -> (zh name, zh text, en name, en text). {N} = amount.
POWERS = {
    "Chime": None,  # three-part text, written below
    "Resonance": ("共鸣", "每回合 #y咒鸣 第一次达到 #b4 时，获得 #b{N} 点能量。", "Resonance",
                  "The first time #yChime reaches #b4 each turn, gain #b{N} energy."),
    "Recollection": ("回忆", "每回合前 #b{N} 次 #y封印 各抽 #b1 张牌。", "Recollection",
                     "The first #b{N} Seals each turn draw #b1 card each."),
    "Bloodblades": ("暗血", "蚀面攻击每段伤害 +#b{N} 。", "Bloodblades", "Cursed attacks deal #b{N} more damage per hit."),
    "Radiance": ("光辉", "光面牌获得 #y格挡 时， #y咒鸣 加成翻倍。", "Radiance", "Light cards get double #yChime on #yBlock."),
    "BacklashEcho": ("血之回响", "#y反噬 失去生命时，对所有敌人造成失去生命 ×#b{N} 的伤害。", "Backlash Echo",
                     "When #yBacklash costs HP, deal HP lost x#b{N} damage to ALL enemies."),
    "Composure": ("静心", "回合结束时，手中每张光面牌给你 #b{N} 点 #y格挡 。", "Composure",
                  "At the end of your turn, gain #b{N} #yBlock for each light card in your hand."),
    "CreepingCurse": ("蔓延之咒", "每回合抽牌后， #y侵蚀 手中最左边 #b{N} 张光面牌。", "Creeping Curse",
                      "After drawing each turn, #yErode the leftmost #b{N} light card(s) in your hand."),
    "Vigil": ("守望", "每次手动 #y封印 1 张牌，获得 #b{N} 点 #y格挡 。", "Vigil",
              "Whenever you #ySeal a card yourself, gain #b{N} #yBlock."),
    "MoonAwakening": ("月辉觉醒", "每次 #y封印 ，抽 #b{N} 张牌并获得 #b{N} 点能量。", "Moon Awakening",
                      "Whenever you #ySeal a card, draw #b{N} card(s) and gain #b{N} energy."),
    "Afterglow": ("余韵", "回合结束时， #y咒鸣 只清除一半。", "Afterglow", "At the end of your turn, #yChime is only halved."),
    "Evernight": ("永夜", "蚀面攻击多造成 #b{N} 段。", "Evernight", "Cursed attacks hit #b{N} more time(s)."),
    "CursedHeart": ("咒之心", "每次 #y反噬 ，获得 #b{N} 点 #y力量 。", "Cursed Heart",
                    "Whenever you #yBacklash, gain #b{N} #yStrength."),
    "LightOath": ("光之誓", "光面牌的伤害和 #y格挡 ， #y咒鸣 加成翻倍。", "Light Oath",
                  "Light cards get double #yChime on damage and #yBlock."),
    "TwinSoul": ("双生", "本回合，光面牌打出后自动 #y侵蚀 并回到手牌。", "Twin Soul",
                 "This turn, light cards #yErode themselves after being played and return to your hand."),
    "NoBacklash": ("定神", "本回合结束时不触发 #y反噬 。", "Steady", "No #yBacklash at the end of this turn."),
    "NockLight": ("张弓", "本回合下一张光面攻击牌伤害 +#b{N} 。", "Nock", "Your next light attack this turn deals #b{N} more damage."),
    "NockCursed": ("蓄势", "本回合下一张蚀面攻击牌每段伤害 +#b{N} 。", "Bide",
                   "Your next cursed attack this turn deals #b{N} more damage per hit."),
    "EchoLight": ("回响·光", "本回合接下来 #b{N} 张光面牌打出两次。", "Echo (light)",
                  "This turn, your next #b{N} light card(s) are played twice."),
    "EchoCursed": ("回响·蚀", "本回合接下来 #b{N} 张蚀面牌打出两次。", "Echo (cursed)",
                   "This turn, your next #b{N} cursed card(s) are played twice."),
}

# Relics: id -> (zh name, zh flavor, zh text, en name, en flavor, en text)
RELICS = {
    "CrackedSeal": ("裂纹封印", "封印还在，只是已经裂开了。", "每回合第一次 #y封印 牌时，抽 #b1 张牌。",
                    "Cracked Seal", "Still sealed. Just cracked.", "The first time you #ySeal a card each turn, draw #b1 card."),
    "WholeSeal": ("完整封印", "裂纹被金线缝合，比原来更牢。", "替换 #y裂纹封印 。每回合前 #b2 次 #y封印 牌时，各抽 #b1 张牌。",
                  "Whole Seal", "The crack is mended with gold.",
                  "Replaces #yCracked #ySeal. The first #b2 times you #ySeal a card each turn, draw #b1 card."),
    "DuskGauntlet": ("暗纹护手", "护手上的纹路在黑暗里会自己发光。", "每场战斗第一回合抽牌后， #y侵蚀 手中最左边的 #b1 张牌。",
                     "Dusk Gauntlet", "Its veins glow on their own in the dark.",
                     "After your first draw each combat, #yErode the leftmost card in your hand."),
    "SilverleafBrooch": ("银叶胸针", "一片永不凋零的叶子。", "每回合第一次手动 #y封印 牌时，获得 #b4 点 #y格挡 。",
                         "Silverleaf Brooch", "A leaf that never withers.",
                         "The first time you #ySeal a card yourself each turn, gain #b4 #yBlock."),
    "MirrorEarring": ("镜之耳坠", "镜子里的她，总是先动手。", "每回合打出的第一张蚀面牌，每段伤害 +#b3 。",
                      "Mirror Earring", "Her reflection always strikes first.",
                      "The first cursed card you play each turn deals #b3 more damage per hit."),
    "MossCharm": ("苔藓护符", "安静、潮湿、可靠。", "回合结束时，若手中没有蚀面牌，获得 #b4 点 #y格挡 。",
                  "Moss Charm", "Quiet, damp, dependable.",
                  "At the end of your turn, if no cursed card is in your hand, gain #b4 #yBlock."),
    "WorldtreeBlessing": ("世界树的祝福", "你不是独自在抵御风暴。",
                          "战斗开始时，获得 #b6 点 #y格挡 。每场战斗第一次主动使用药水后，恢复 #b3 点生命。",
                          "Worldtree Blessing", "You do not face the storm alone.",
                          "At the start of combat, gain #b6 #yBlock. The first time you use a potion each combat, heal #b3 HP."),
    "EclipseStone": ("月蚀石", "光与暗重叠的一瞬间。", "每回合 #y咒鸣 第一次达到 #b6 时，获得 #b1 点能量并抽 #b1 张牌。",
                     "Eclipse Stone", "The moment light and dark overlap.",
                     "The first time #yChime reaches #b6 each turn, gain #b1 energy and draw #b1 card."),
    "Whetstone_Elf": ("磨刃石", "刀越磨越亮，诅咒也是。", "蚀面攻击每段伤害 +#b1 。",
                      "Whetstone", "The sharper the blade, the brighter the curse.",
                      "Cursed attacks deal #b1 more damage per hit."),
    "CursedCrown": ("诅咒王冠", "戴上它，力量与诅咒一同涌来。", "每回合开始时获得 #b1 点能量。每回合抽牌后， #y侵蚀 手中最左边的 #b1 张牌。",
                    "Cursed Crown", "Power and curse arrive together.",
                    "Gain #b1 energy at the start of each turn. After drawing each turn, #yErode the leftmost card in your hand."),
}

CHAR = {
    "zhs": {"NAMES": ["精灵", "被诅咒的游侠"],
            "TEXT": ["诅咒是她身上的另一面。 NL 放出诅咒，让同一张牌用两次；在反噬之前，把它封回去。",
                     "你举起长弓，诅咒在你耳边低语……", "那个吸血鬼注视着你身上的诅咒，似乎觉得你已经是“同类”了。"]},
    "eng": {"NAMES": ["The Elf", "the Cursed Ranger"],
            "TEXT": ["The curse is her other side. NL Let it out to play a card twice, then seal it away before it backlashes.",
                     "You raise your bow. The curse whispers...",
                     "The vampire eyes the curse on you, as if you were already one of its kind."]},
}

UI = {
    "zhs": ["侵蚀", "封印", "蚀面",
            "费用至少为 #b1 。打出后自动 #y封印 并回到手牌。回合结束时若仍在手牌中，失去 #b2 点生命（ #y反噬 ）。",
            "选择一张牌放入手牌", "复制", "消耗"],
    "eng": ["to Erode", "to Seal", "Cursed face",
            "Costs at least #b1. After it is played it #ySeals itself and returns to your hand. If it is still in your hand at end of turn, lose #b2 HP ( #yBacklash ).",
            "Choose a card to put into your hand", "to copy", "to Exhaust"],
}

CHIME = {
    "zhs": ("咒鸣", ["本回合已翻面 #b", " 次。精灵卡牌每段伤害和获得的 #y格挡 +#b", " 。回合结束时清空。"]),
    "eng": ("Chime", ["Flipped #b", " time(s) this turn. Elf cards deal + #b", " damage per hit and gain that much more #yBlock. Resets at end of turn."]),
}

KEYWORDS = {
    "zhs": [
        {"PROPER_NAME": "侵蚀", "NAMES": ["侵蚀"], "DESCRIPTION": "将光面牌翻成蚀面。蚀面更强、费用至少为 #b1 ，打出后自动 #y封印 并回到手牌；回合结束仍在手中会 #y反噬 。"},
        {"PROPER_NAME": "封印", "NAMES": ["封印"], "DESCRIPTION": "将蚀面牌翻回光面。"},
        {"PROPER_NAME": "翻面", "NAMES": ["翻面"], "DESCRIPTION": "#y侵蚀 或 #y封印 一张牌。每次翻面使本回合 #y咒鸣 +1。"},
        {"PROPER_NAME": "咒鸣", "NAMES": ["咒鸣"], "DESCRIPTION": "本回合翻面的次数。精灵卡牌每段伤害和获得的 #y格挡 加上 咒鸣 。"},
        {"PROPER_NAME": "反噬", "NAMES": ["反噬"], "DESCRIPTION": "回合结束时，手中每张蚀面牌使你失去 #b2 点生命。"}],
    "eng": [
        {"PROPER_NAME": "Erode", "NAMES": ["erode", "eroded"], "DESCRIPTION": "Flip a light card to its cursed face. Cursed faces are stronger, cost at least #b1, #ySeal themselves after being played and return to hand, and #yBacklash if still in hand at end of turn."},
        {"PROPER_NAME": "Seal", "NAMES": ["seal", "sealed"], "DESCRIPTION": "Flip a cursed card back to its light face."},
        {"PROPER_NAME": "Flip", "NAMES": ["flip", "flipped"], "DESCRIPTION": "#yErode or #ySeal a card. Each flip adds 1 #yChime this turn."},
        {"PROPER_NAME": "Chime", "NAMES": ["chime"], "DESCRIPTION": "Flips this turn. Elf cards deal + Chime damage per hit and gain + Chime #yBlock."},
        {"PROPER_NAME": "Backlash", "NAMES": ["backlash"], "DESCRIPTION": "At end of turn, lose #b2 HP for each cursed card in your hand."}],
}


def card_entry(name, text, cname, ctext, up=None, cup=None):
    e = {"NAME": name, "DESCRIPTION": text, "EXTENDED_DESCRIPTION": [cname, ctext] + ([cup] if cup else [])}
    if up:
        e["UPGRADE_DESCRIPTION"] = up
    return e


def build(lang):
    zh = lang == "zhs"
    cards = {}
    for row in DUAL_ZH:
        cid = row[0]
        if zh:
            cards[P + cid] = card_entry(row[1], row[2], row[3], row[4], row[5], row[6])
        else:
            e = DUAL_EN[cid]
            cards[P + cid] = card_entry(e[0], e[1], e[2], e[3], e[4] if len(e) > 4 else None, e[5] if len(e) > 5 else None)
    for cid, t in POWER_CARDS.items():
        name, text, up = (t[0], t[1], t[2]) if zh else (t[3], t[4], t[5])
        cards[P + cid] = {"NAME": name, "DESCRIPTION": text}
        if up:
            cards[P + cid]["UPGRADE_DESCRIPTION"] = up
    powers = {}
    for pid, t in POWERS.items():
        if pid == "Chime":
            n, d = CHIME[lang]
            powers[P + pid] = {"NAME": n, "DESCRIPTIONS": d}
        else:
            powers[P + pid] = {"NAME": t[0] if zh else t[2], "DESCRIPTIONS": [t[1] if zh else t[3]]}
    relics = {}
    for rid, t in RELICS.items():
        name, flavor, text = (t[0], t[1], t[2]) if zh else (t[3], t[4], t[5])
        relics[P + rid] = {"NAME": name, "FLAVOR": flavor, "DESCRIPTIONS": [text]}
    out = {
        "CardStrings.json": cards,
        "PowerStrings.json": powers,
        "RelicStrings.json": relics,
        "CharacterStrings.json": {P + "Elf": CHAR[lang]},
        "UIStrings.json": {P + "Lumen": {"TEXT": UI[lang]}},
        "Keywords.json": KEYWORDS[lang],
    }
    os.makedirs(os.path.join(ROOT, lang), exist_ok=True)
    for f, v in out.items():
        with open(os.path.join(ROOT, lang, f), "w", encoding="utf-8") as fh:
            json.dump(v, fh, ensure_ascii=False, indent=2)
    return len(cards), len(powers), len(relics)


if __name__ == "__main__":
    for lang in ("zhs", "eng"):
        print(lang, "cards/powers/relics:", build(lang))
