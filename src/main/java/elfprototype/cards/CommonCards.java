package elfprototype.cards;

import com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;
import elfprototype.ElfPrototypeMod;
import elfprototype.mechanics.Do;
import elfprototype.mechanics.Lumen;
import elfprototype.mechanics.PickPileAction;
import elfprototype.powers.NockPower;

/** Common pool (design v4 §4.1). Light face / cursed face numbers follow the design table, upgrade in brackets. */
public final class CommonCards {
    private CommonCards() {
    }

    static String uiText(int i) {
        return CardCrawlGame.languagePack.getUIString(ElfPrototypeMod.makeID("Lumen")).TEXT[i];
    }

    /** 9 (12) / 3 (4) x3. */
    public static class ThicketArrow extends DualFaceCard {
        public ThicketArrow() {
            super("ThicketArrow", 1, CardType.ATTACK, CardRarity.COMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(9, 3),
                    CardTarget.ENEMY, FaceStats.of().damage(3, 1).hits(3, 0));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_DIAGONAL);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
        }
    }

    /** 0 cost. 3 (5), draw 1 / 2 (3) x2. */
    public static class QuickArrow extends DualFaceCard {
        public QuickArrow() {
            super("QuickArrow", 0, CardType.ATTACK, CardRarity.COMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(3, 2),
                    CardTarget.ENEMY, FaceStats.of().damage(2, 1).hits(2, 0));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_DIAGONAL);
            draw(1);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
        }
    }

    /** ALL: 6 (9) / 2 (3) x3. */
    public static class ScatterShot extends DualFaceCard {
        public ScatterShot() {
            super("ScatterShot", 1, CardType.ATTACK, CardRarity.COMMON,
                    CardTarget.ALL_ENEMY, FaceStats.of().damage(6, 3).aoe(),
                    CardTarget.ALL_ENEMY, FaceStats.of().damage(2, 1).hits(3, 0).aoe());
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hitAll(AttackEffect.SLASH_DIAGONAL);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hitsAll(AttackEffect.SLASH_HORIZONTAL);
        }
    }

    /** 7 (9), Weak 1 (2) / 3 (4) x2, Vulnerable 1 (2). magic2 = debuff stacks. */
    public static class SnareShot extends DualFaceCard {
        public SnareShot() {
            super("SnareShot", 1, CardType.ATTACK, CardRarity.COMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(7, 2).magic2(1, 1),
                    CardTarget.ENEMY, FaceStats.of().damage(3, 1).hits(2, 0).magic2(1, 1));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_DIAGONAL);
            addToBot(new ApplyPowerAction(m, p, new WeakPower(m, m2(), false), m2()));
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
            addToBot(new ApplyPowerAction(m, p, new VulnerablePower(m, m2(), false), m2()));
        }
    }

    /** 2 cost. 12 (16), double chime bonus / 5 (6) x3. */
    public static class ChargedShot extends DualFaceCard {
        public ChargedShot() {
            super("ChargedShot", 2, CardType.ATTACK, CardRarity.COMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(12, 4),
                    CardTarget.ENEMY, FaceStats.of().damage(5, 1).hits(3, 0));
        }

        @Override
        public int chimeDamageMultiplier() {
            return isCursed() ? 1 : 2;
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_HEAVY);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
        }
    }

    /** 6 (9), erode 1 / 3 (4) x2, erode 1. */
    public static class CorruptingEdge extends DualFaceCard {
        public CorruptingEdge() {
            super("CorruptingEdge", 1, CardType.ATTACK, CardRarity.COMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(6, 3),
                    CardTarget.ENEMY, FaceStats.of().damage(3, 1).hits(2, 0));
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

    /** 5 (7) + 5 (7) block / 2 (3) x4. */
    public static class GladeThrust extends DualFaceCard {
        public GladeThrust() {
            super("GladeThrust", 1, CardType.ATTACK, CardRarity.COMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(5, 2).block(5, 2),
                    CardTarget.ENEMY, FaceStats.of().damage(2, 1).hits(4, 0));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_DIAGONAL);
            gainBlock();
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_VERTICAL);
        }
    }

    /** 6 (9), seal 1 / 4 (5) x2, seal 1. */
    public static class Riposte extends DualFaceCard {
        public Riposte() {
            super("Riposte", 1, CardType.ATTACK, CardRarity.COMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(6, 3),
                    CardTarget.ENEMY, FaceStats.of().damage(4, 1).hits(2, 0));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_DIAGONAL);
            seal(1);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
            seal(1);
        }
    }

    /** 4 (6), triple chime bonus / 1 (2) x4. */
    public static class EmberShot extends DualFaceCard {
        public EmberShot() {
            super("EmberShot", 1, CardType.ATTACK, CardRarity.COMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(4, 2),
                    CardTarget.ENEMY, FaceStats.of().damage(1, 1).hits(4, 0));
        }

        @Override
        public int chimeDamageMultiplier() {
            return isCursed() ? 1 : 3;
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.FIRE);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.FIRE);
        }
    }

    /** 7 (10), draw 1 if a cursed face is in hand / 3 (4) x2, draw 1. */
    public static class Recoil extends DualFaceCard {
        public Recoil() {
            super("Recoil", 1, CardType.ATTACK, CardRarity.COMMON,
                    CardTarget.ENEMY, FaceStats.of().damage(7, 3),
                    CardTarget.ENEMY, FaceStats.of().damage(3, 1).hits(2, 0));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_DIAGONAL);
            if (Lumen.countInHand(true, this) > 0) draw(1);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
            draw(1);
        }

        @Override
        public void triggerOnGlowCheck() {
            glowColor = !isCursed() && Lumen.countInHand(true, this) > 0
                    ? GOLD_BORDER_GLOW_COLOR.cpy() : BLUE_BORDER_GLOW_COLOR.cpy();
        }
    }

    /** Block 8 (11) / block 4 (6), erode 1. */
    public static class LeafWard extends DualFaceCard {
        public LeafWard() {
            super("LeafWard", 1, CardType.SKILL, CardRarity.COMMON,
                    CardTarget.SELF, FaceStats.of().block(8, 3),
                    CardTarget.SELF, FaceStats.of().block(4, 2));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
            erode(1);
        }
    }

    /** Block 6 (9), draw 1 / draw 2 (3). */
    public static class HoldBreath extends DualFaceCard {
        public HoldBreath() {
            super("HoldBreath", 1, CardType.SKILL, CardRarity.COMMON,
                    CardTarget.SELF, FaceStats.of().block(6, 3),
                    CardTarget.SELF, FaceStats.of().magic(2, 1));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
            draw(1);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            draw(magicNumber);
        }
    }

    /** Block 5 (8), seal 2 / seal 2 (3), draw 2. */
    public static class SealingRite extends DualFaceCard {
        public SealingRite() {
            super("SealingRite", 1, CardType.SKILL, CardRarity.COMMON,
                    CardTarget.SELF, FaceStats.of().block(5, 3),
                    CardTarget.SELF, FaceStats.of().magic(2, 1));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
            seal(2);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            seal(magicNumber);
            draw(2);
        }
    }

    /** 1 (0) cost. Erode 2, draw 1 / erode 2, draw 2. */
    public static class Invite extends DualFaceCard {
        public Invite() {
            super("Invite", 1, CardType.SKILL, CardRarity.COMMON,
                    CardTarget.SELF, FaceStats.of(),
                    CardTarget.SELF, FaceStats.of());
        }

        @Override
        protected void onUpgrade() {
            upgradeBothCosts(0);
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            erode(2);
            draw(1);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            erode(2);
            draw(2);
        }
    }

    /** 0 cost. Block 3 (5), draw 1 if flipped this turn / block 2 (4), draw 1. */
    public static class Moonstep extends DualFaceCard {
        public Moonstep() {
            super("Moonstep", 0, CardType.SKILL, CardRarity.COMMON,
                    CardTarget.SELF, FaceStats.of().block(3, 2),
                    CardTarget.SELF, FaceStats.of().block(2, 2));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
            if (Lumen.chime() > 0) draw(1);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
            draw(1);
        }

        @Override
        public void triggerOnGlowCheck() {
            glowColor = !isCursed() && Lumen.chime() > 0 ? GOLD_BORDER_GLOW_COLOR.cpy() : BLUE_BORDER_GLOW_COLOR.cpy();
        }
    }

    /** Seal all cursed in hand, 3 (4) block each / seal all others, draw 1 each; cursed face costs 0 when upgraded. */
    public static class PurifyingLight extends DualFaceCard {
        public PurifyingLight() {
            super("PurifyingLight", 1, CardType.SKILL, CardRarity.COMMON,
                    CardTarget.SELF, FaceStats.of().magic(3, 1),
                    CardTarget.SELF, FaceStats.of().cost(1, -1));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            int per = magicNumber;
            addToBot(new Do(() -> {
                int n = Lumen.sealAllInHand();
                if (n > 0) addToTop(new GainBlockAction(p, p, n * per));
            }));
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            addToBot(new Do(() -> {
                int n = Lumen.sealAllInHand();
                if (n > 0) addToTop(new DrawCardAction(p, n));
            }));
        }
    }

    /** Block 7 (10) + 3 (4) per cursed card in hand / block 4 (5) per other cursed card in hand. */
    public static class CurseWard extends DualFaceCard {
        public CurseWard() {
            super("CurseWard", 1, CardType.SKILL, CardRarity.COMMON,
                    CardTarget.SELF, FaceStats.of().block(7, 3).magic(3, 1),
                    CardTarget.SELF, FaceStats.of().block(0, 0).magic(4, 1));
        }

        @Override
        protected int bonusBlock() {
            return magicNumber * Lumen.countInHand(true, this);
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
        }
    }

    /** Look at the top 3 (4) cards of the draw pile, put 1 into hand / same, then erode it. */
    public static class Premonition extends DualFaceCard {
        public Premonition() {
            super("Premonition", 1, CardType.SKILL, CardRarity.COMMON,
                    CardTarget.SELF, FaceStats.of().magic(3, 1),
                    CardTarget.SELF, FaceStats.of().magic(3, 1));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            addToBot(PickPileAction.fromDrawTop(magicNumber, uiText(4), c -> { }));
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            addToBot(PickPileAction.fromDrawTop(magicNumber, uiText(4), Lumen::erode));
        }
    }

    /** 0 cost. Next light attack this turn +5 (8) / next cursed attack this turn +2 (3) per hit. */
    public static class Nock extends DualFaceCard {
        public Nock() {
            super("Nock", 0, CardType.SKILL, CardRarity.COMMON,
                    CardTarget.SELF, FaceStats.of().magic(5, 3),
                    CardTarget.SELF, FaceStats.of().magic(2, 1));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            addToBot(new ApplyPowerAction(p, p, new NockPower(p, magicNumber, false), magicNumber));
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            addToBot(new ApplyPowerAction(p, p, new NockPower(p, magicNumber, true), magicNumber));
        }
    }

    /** 0 cost. Seal 1, draw 1 (2) / seal 2, (draw 1 when upgraded). magic = seal, magic2 = draw. */
    public static class Unbind extends DualFaceCard {
        public Unbind() {
            super("Unbind", 0, CardType.SKILL, CardRarity.COMMON,
                    CardTarget.SELF, FaceStats.of().magic(1, 0).magic2(1, 1),
                    CardTarget.SELF, FaceStats.of().magic(2, 0).magic2(0, 1));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            seal(magicNumber);
            draw(m2());
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            seal(magicNumber);
            draw(m2());
        }
    }
}
