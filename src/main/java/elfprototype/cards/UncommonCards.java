package elfprototype.cards;

import basemod.BaseMod;
import com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.ExhaustSpecificCardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.actions.common.RemoveAllBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.NextTurnBlockPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;
import com.megacrit.cardcrawl.vfx.cardManip.ShowCardAndAddToHandEffect;
import elfprototype.mechanics.DamageThenAction;
import elfprototype.mechanics.Do;
import elfprototype.mechanics.Lumen;
import elfprototype.mechanics.PickHandAction;
import elfprototype.mechanics.PickPileAction;
import elfprototype.powers.NoBacklashPower;

import static elfprototype.cards.CommonCards.uiText;

/** Uncommon pool (design v4 §4.2), attacks and skills. Power cards are in PowerCards. */
public final class UncommonCards {
    private UncommonCards() {
    }

    // ------------------------------------------------------------------ attacks

    /** 2 cost. 14 (18) / 3 (4) x5. */
    public static class TwinMoon extends DualFaceCard {
        public TwinMoon() {
            super("TwinMoon", 2, CardType.ATTACK, CardRarity.UNCOMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(14, 4),
                    CardTarget.ENEMY, FaceStats.of().damage(3, 1).hits(5, 0));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_HEAVY);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
        }
    }

    /** 0 cost. 4 (6), erode 1 / 2 (3) x2, erode 1. */
    public static class Skim extends DualFaceCard {
        public Skim() {
            super("Skim", 0, CardType.ATTACK, CardRarity.UNCOMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(4, 2),
                    CardTarget.ENEMY, FaceStats.of().damage(2, 1).hits(2, 0));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_DIAGONAL);
            erode(1);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
            erode(1);
        }
    }

    /** 2 cost. 16 (20), +6 (8) vs Weak / 5 (6) x3, Vulnerable 2. */
    public static class Heartseeker extends DualFaceCard {
        public Heartseeker() {
            super("Heartseeker", 2, CardType.ATTACK, CardRarity.UNCOMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(16, 4).magic(6, 2),
                    CardTarget.ENEMY, FaceStats.of().damage(5, 1).hits(3, 0).magic2(2, 0));
        }

        @Override
        protected int bonusDamage(AbstractMonster target) {
            return !isCursed() && target != null && target.hasPower(WeakPower.POWER_ID) ? magicNumber : 0;
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_HEAVY);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
            addToBot(new ApplyPowerAction(m, p, new VulnerablePower(m, m2(), false), m2()));
        }
    }

    /** 8 (11), kill: gain 1 energy / 3 (4) x3, each killing hit draws 1. */
    public static class Harvest extends DualFaceCard {
        public Harvest() {
            super("Harvest", 1, CardType.ATTACK, CardRarity.UNCOMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(8, 3),
                    CardTarget.ENEMY, FaceStats.of().damage(3, 1).hits(3, 0));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            addToBot(new DamageThenAction(m, new DamageInfo(p, damage, damageTypeForTurn), AttackEffect.SLASH_HEAVY,
                    (killed, dealt) -> {
                        if (killed) AbstractDungeon.actionManager.addToTop(new GainEnergyAction(1));
                    }));
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            for (int i = 0; i < magicNumber; i++) {
                addToBot(new DamageThenAction(m, new DamageInfo(p, damage, damageTypeForTurn),
                        AttackEffect.SLASH_HORIZONTAL, (killed, dealt) -> {
                            if (killed) AbstractDungeon.actionManager.addToTop(new DrawCardAction(p, 1));
                        }));
            }
        }
    }

    /** 2 cost, ALL: 11 (15) / 3 (4) x3. */
    public static class Volley extends DualFaceCard {
        public Volley() {
            super("Volley", 2, CardType.ATTACK, CardRarity.UNCOMMON,
                    CardTarget.ALL_ENEMY, FaceStats.of().damage(11, 4).aoe(),
                    CardTarget.ALL_ENEMY, FaceStats.of().damage(3, 1).hits(3, 0).aoe());
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hitAll(AttackEffect.SLASH_DIAGONAL);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hitsAll(AttackEffect.SLASH_HORIZONTAL);
        }
    }

    /** 7 (10), seal 1, block 4 (6) / 3 (4) x2, seal 2. */
    public static class MirrorEdge extends DualFaceCard {
        public MirrorEdge() {
            super("MirrorEdge", 1, CardType.ATTACK, CardRarity.UNCOMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(7, 3).block(4, 2),
                    CardTarget.ENEMY, FaceStats.of().damage(3, 1).hits(2, 0));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_DIAGONAL);
            seal(1);
            gainBlock();
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
            seal(2);
        }
    }

    /** 7 (10) / 3 (4) x3, heal 1 per hit that deals damage. */
    public static class Leech extends DualFaceCard {
        public Leech() {
            super("Leech", 1, CardType.ATTACK, CardRarity.UNCOMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(7, 3),
                    CardTarget.ENEMY, FaceStats.of().damage(3, 1).hits(3, 0));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_DIAGONAL);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            for (int i = 0; i < magicNumber; i++) {
                addToBot(new DamageThenAction(m, new DamageInfo(p, damage, damageTypeForTurn),
                        AttackEffect.SLASH_HORIZONTAL, (killed, dealt) -> {
                            if (dealt > 0) AbstractDungeon.actionManager.addToTop(new HealAction(p, p, 1));
                        }));
            }
        }
    }

    /** 5 (7), one more hit if a cursed face was played this turn / 2 (3) x2, two more if a light face was. */
    public static class Chain extends DualFaceCard {
        public Chain() {
            super("Chain", 1, CardType.ATTACK, CardRarity.UNCOMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(5, 2).hits(1, 0),
                    CardTarget.ENEMY, FaceStats.of().damage(2, 1).hits(2, 0));
        }

        @Override
        protected int bonusHits() {
            return isCursed() ? (Lumen.playedThisTurn(false) ? 2 : 0) : (Lumen.playedThisTurn(true) ? 1 : 0);
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_DIAGONAL);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
        }

        @Override
        public void triggerOnGlowCheck() {
            glowColor = bonusHits() > 0 ? GOLD_BORDER_GLOW_COLOR.cpy() : BLUE_BORDER_GLOW_COLOR.cpy();
        }
    }

    /** 2 cost. Remove all Block, 12 (16) / 4 (5) x4. */
    public static class Sunder extends DualFaceCard {
        public Sunder() {
            super("Sunder", 2, CardType.ATTACK, CardRarity.UNCOMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(12, 4),
                    CardTarget.ENEMY, FaceStats.of().damage(4, 1).hits(4, 0));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            addToBot(new RemoveAllBlockAction(m, p));
            hit(m, AttackEffect.SLASH_HEAVY);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
        }
    }

    /** ALL: chime x2 (x3) damage / 1 (2) x3. */
    public static class Shatter extends DualFaceCard {
        public Shatter() {
            super("Shatter", 1, CardType.ATTACK, CardRarity.UNCOMMON,
                    CardTarget.ALL_ENEMY, FaceStats.of().damage(0, 0).aoe(),
                    CardTarget.ALL_ENEMY, FaceStats.of().damage(1, 1).hits(3, 0).aoe());
        }

        @Override
        public int chimeDamageMultiplier() {
            return isCursed() ? 1 : (upgraded ? 3 : 2);
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hitAll(AttackEffect.BLUNT_HEAVY);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hitsAll(AttackEffect.SLASH_HORIZONTAL);
        }
    }

    /** 8 (11) / 3 (4) x2; costs 0 once chime is 4 or more. */
    public static class Pursuit extends DualFaceCard {
        public Pursuit() {
            super("Pursuit", 1, CardType.ATTACK, CardRarity.UNCOMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(8, 3),
                    CardTarget.ENEMY, FaceStats.of().damage(3, 1).hits(2, 0));
        }

        @Override
        protected int conditionalCost() {
            return Lumen.chime() >= 4 ? 0 : -1;
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_DIAGONAL);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
        }
    }

    /** ALL: seal every cursed card in hand, 3 (4) / 5 (7) damage to ALL enemies per card sealed. */
    public static class Release extends DualFaceCard {
        public Release() {
            super("Release", 1, CardType.ATTACK, CardRarity.UNCOMMON,
                    CardTarget.ALL_ENEMY, FaceStats.of().magic(3, 1),
                    CardTarget.ALL_ENEMY, FaceStats.of().magic(5, 2));
        }

        private void release(AbstractPlayer p) {
            int per = magicNumber;
            addToBot(new Do(() -> {
                int n = Lumen.sealAllInHand();
                for (int i = 0; i < n; i++) {
                    AbstractDungeon.actionManager.addToTop(new DamageAllEnemiesAction(p,
                            DamageInfo.createDamageMatrix(per, true), DamageInfo.DamageType.THORNS,
                            AttackEffect.FIRE));
                }
            }));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            release(p);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            release(p);
        }
    }

    // ------------------------------------------------------------------ skills

    /** 0 cost, exhaust. Lose 3 (2) hp, gain 1 / 2 energy. */
    public static class DarkPact extends DualFaceCard {
        public DarkPact() {
            super("DarkPact", 0, CardType.SKILL, CardRarity.UNCOMMON,
                    CardTarget.SELF, FaceStats.of().magic(3, -1).exhaust(),
                    CardTarget.SELF, FaceStats.of().magic(3, -1).exhaust());
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            addToBot(new LoseHPAction(p, p, magicNumber));
            addToBot(new GainEnergyAction(1));
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            addToBot(new LoseHPAction(p, p, magicNumber));
            addToBot(new GainEnergyAction(2));
        }
    }

    /** 1 (0) cost. Erode every other card in hand / same, draw 2. */
    public static class Spread extends DualFaceCard {
        public Spread() {
            super("Spread", 1, CardType.SKILL, CardRarity.UNCOMMON,
                    CardTarget.SELF, FaceStats.of(), CardTarget.SELF, FaceStats.of());
        }

        @Override
        protected void onUpgrade() {
            upgradeBothCosts(0);
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            addToBot(new Do(Lumen::erodeAllInHand));
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            addToBot(new Do(Lumen::erodeAllInHand));
            draw(2);
        }
    }

    /** 1 (0) cost. Copy a card in hand (light face, exhausts) / same, and erode the copy. */
    public static class Mirror extends DualFaceCard {
        public Mirror() {
            super("Mirror", 1, CardType.SKILL, CardRarity.UNCOMMON,
                    CardTarget.SELF, FaceStats.of(), CardTarget.SELF, FaceStats.of());
        }

        @Override
        protected void onUpgrade() {
            upgradeBothCosts(0);
        }

        private void copy(boolean erodeCopy) {
            addToBot(new PickHandAction(uiText(5), 1, c -> true, c -> {
                if (AbstractDungeon.player.hand.size() >= BaseMod.MAX_HAND_SIZE) return;
                AbstractCard copy = c.makeStatEquivalentCopy();
                copy.exhaust = true;
                if (copy instanceof DualFaceCard) ((DualFaceCard) copy).alwaysExhaust = true;
                if (erodeCopy) Lumen.erode(copy);
                AbstractDungeon.effectList.add(new ShowCardAndAddToHandEffect(copy));
            }));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            copy(false);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            copy(true);
        }
    }

    /** 2 cost. Block 14 (18) / block 7 (10), erode 2. */
    public static class CurseBarrier extends DualFaceCard {
        public CurseBarrier() {
            super("CurseBarrier", 2, CardType.SKILL, CardRarity.UNCOMMON,
                    CardTarget.SELF, FaceStats.of().block(14, 4),
                    CardTarget.SELF, FaceStats.of().block(7, 3));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
            erode(2);
        }
    }

    /** 1 (0) cost. Put a card from the discard pile into hand / same, and erode it. */
    public static class Rewind extends DualFaceCard {
        public Rewind() {
            super("Rewind", 1, CardType.SKILL, CardRarity.UNCOMMON,
                    CardTarget.SELF, FaceStats.of(), CardTarget.SELF, FaceStats.of());
        }

        @Override
        protected void onUpgrade() {
            upgradeBothCosts(0);
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            addToBot(PickPileAction.fromDiscard(uiText(4), c -> { }));
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            addToBot(PickPileAction.fromDiscard(uiText(4), Lumen::erode));
        }
    }

    /** Draw 3 (4) / draw 2 (3) and erode the drawn cards. */
    public static class ForestBreath extends DualFaceCard {
        public ForestBreath() {
            super("ForestBreath", 1, CardType.SKILL, CardRarity.UNCOMMON,
                    CardTarget.SELF, FaceStats.of().magic(3, 1),
                    CardTarget.SELF, FaceStats.of().magic(2, 1));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            draw(magicNumber);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            addToBot(new DrawCardAction(magicNumber, new Do(() -> {
                for (AbstractCard c : DrawCardAction.drawnCards) Lumen.erode(c);
            })));
        }
    }

    /** 0 cost. Erode 1, then seal 1 (may be the same card), (draw 1 when upgraded) / erode 2, then seal 2. */
    public static class Turnabout extends DualFaceCard {
        public Turnabout() {
            super("Turnabout", 0, CardType.SKILL, CardRarity.UNCOMMON,
                    CardTarget.SELF, FaceStats.of().magic(1, 0).magic2(0, 1),
                    CardTarget.SELF, FaceStats.of().magic(2, 0));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            erode(magicNumber);
            seal(magicNumber);
            draw(m2());
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            erode(magicNumber);
            seal(magicNumber);
        }
    }

    /** Block = chime x2 (x3) / draw chime / 2; cursed face costs 0 when upgraded. */
    public static class Insight extends DualFaceCard {
        public Insight() {
            super("Insight", 1, CardType.SKILL, CardRarity.UNCOMMON,
                    CardTarget.SELF, FaceStats.of().block(0, 0),
                    CardTarget.SELF, FaceStats.of().cost(1, -1));
        }

        @Override
        public int chimeBlockMultiplier() {
            return isCursed() ? 1 : (upgraded ? 3 : 2);
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            addToBot(new Do(() -> {
                int n = Lumen.chime() / 2;
                if (n > 0) AbstractDungeon.actionManager.addToTop(new DrawCardAction(p, n));
            }));
        }
    }

    /** 1 (0) cost. Exhaust a card in hand: gain 1 energy (2 if it was a Curse) / exhaust a card in hand, draw 2. */
    public static class Offering extends DualFaceCard {
        public Offering() {
            super("Offering", 1, CardType.SKILL, CardRarity.UNCOMMON,
                    CardTarget.SELF, FaceStats.of(), CardTarget.SELF, FaceStats.of());
        }

        @Override
        protected void onUpgrade() {
            upgradeBothCosts(0);
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            addToBot(new PickHandAction(uiText(6), 1, c -> true, c -> {
                AbstractDungeon.actionManager.addToTop(new GainEnergyAction(c.type == CardType.CURSE ? 2 : 1));
                AbstractDungeon.actionManager.addToTop(new ExhaustSpecificCardAction(c, p.hand));
            }));
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            addToBot(new PickHandAction(uiText(6), 1, c -> true, c -> {
                AbstractDungeon.actionManager.addToTop(new DrawCardAction(p, 2));
                AbstractDungeon.actionManager.addToTop(new ExhaustSpecificCardAction(c, p.hand));
            }));
        }
    }

    /** Block 8 (11), 4 block next turn / block 4 (6), draw 1, 4 block next turn. */
    public static class BowGuard extends DualFaceCard {
        public BowGuard() {
            super("BowGuard", 1, CardType.SKILL, CardRarity.UNCOMMON,
                    CardTarget.SELF, FaceStats.of().block(8, 3),
                    CardTarget.SELF, FaceStats.of().block(4, 2));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
            addToBot(new ApplyPowerAction(p, p, new NextTurnBlockPower(p, 4), 4));
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
            draw(1);
            addToBot(new ApplyPowerAction(p, p, new NextTurnBlockPower(p, 4), 4));
        }
    }

    /** Weak 2 (3) / Vulnerable 2 (3), erode 1. */
    public static class Bind extends DualFaceCard {
        public Bind() {
            super("Bind", 1, CardType.SKILL, CardRarity.UNCOMMON,
                    CardTarget.ENEMY, FaceStats.of().magic(2, 1),
                    CardTarget.ENEMY, FaceStats.of().magic(2, 1));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            addToBot(new ApplyPowerAction(m, p, new WeakPower(m, magicNumber, false), magicNumber));
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            addToBot(new ApplyPowerAction(m, p, new VulnerablePower(m, magicNumber, false), magicNumber));
            erode(1);
        }
    }

    /** Block 6 (9) / draw 2 (3); no backlash at the end of this turn. */
    public static class Steady extends DualFaceCard {
        public Steady() {
            super("Steady", 1, CardType.SKILL, CardRarity.UNCOMMON,
                    CardTarget.SELF, FaceStats.of().block(6, 3),
                    CardTarget.SELF, FaceStats.of().magic(2, 1));
        }

        private void noBacklash(AbstractPlayer p) {
            if (!p.hasPower(NoBacklashPower.POWER_ID)) {
                addToBot(new ApplyPowerAction(p, p, new NoBacklashPower(p, 1), 1));
            }
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
            noBacklash(p);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            draw(magicNumber);
            noBacklash(p);
        }
    }
}
