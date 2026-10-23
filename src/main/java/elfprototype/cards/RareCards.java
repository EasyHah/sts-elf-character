package elfprototype.cards;

import com.megacrit.cardcrawl.actions.AbstractGameAction.AttackEffect;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.DrawCardNextTurnPower;
import com.megacrit.cardcrawl.powers.EnergizedPower;
import elfprototype.mechanics.DamageThenAction;
import elfprototype.mechanics.Do;
import elfprototype.mechanics.Lumen;
import elfprototype.powers.EchoPower;
import elfprototype.powers.TwinSoulPower;

/** Rare pool (design v4 §4.3), attacks and skills. Power cards are in PowerCards. */
public final class RareCards {
    private RareCards() {
    }

    // ------------------------------------------------------------------ attacks

    /** 2 cost. Damage = chime x3 (x4) / 1 (2) damage, hits = chime. */
    public static class ThousandBlades extends DualFaceCard {
        public ThousandBlades() {
            super("ThousandBlades", 2, CardType.ATTACK, CardRarity.RARE,
                    CardTarget.ENEMY, FaceStats.of().damage(0, 0),
                    CardTarget.ENEMY, FaceStats.of().damage(1, 1).hits(0, 0));
            setSeal(Seal.of(Seal.Kind.DAMAGE_CHIME_RANDOM, 0, 0));
        }

        @Override
        public int chimeDamageMultiplier() {
            return isCursed() ? 1 : (upgraded ? 4 : 3);
        }

        @Override
        protected int bonusHits() {
            return Lumen.chime();
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_HEAVY);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
        }
    }

    /** 3 cost. 30 (36) / 6 (7) x5; costs 2 less once chime is 5 or more. */
    public static class Starfall extends DualFaceCard {
        public Starfall() {
            super("Starfall", 3, CardType.ATTACK, CardRarity.RARE,
                    CardTarget.ENEMY, FaceStats.of().damage(25, 5),
                    CardTarget.ENEMY, FaceStats.of().damage(6, 1).hits(5, 0));
            setSeal(Seal.of(Seal.Kind.DAMAGE_RANDOM, 10, 2));
        }

        /** Light: 25 (30) + chime x5, so the light face grows with chime too. */
        @Override
        public int chimeDamageMultiplier() {
            return isCursed() ? 1 : 5;
        }

        @Override
        protected int conditionalCost() {
            return Lumen.chime() >= 5 ? Math.max(0, cost - 2) : -1;
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_HEAVY);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
        }
    }

    /**
     * 2 cost. Seal every cursed card in hand, +6 damage for each, then deal 8 (12) /
     * erode every other card in hand, one more hit for each, then 3 (4) x2.
     */
    public static class Devour extends DualFaceCard {
        public Devour() {
            super("Devour", 2, CardType.ATTACK, CardRarity.RARE,
                    CardTarget.ENEMY, FaceStats.of().damage(8, 4),
                    CardTarget.ENEMY, FaceStats.of().damage(3, 1).hits(2, 0));
            setSeal(Seal.of(Seal.Kind.DAMAGE_RANDOM, 6, 2));
        }

        @Override
        protected int bonusDamage(AbstractMonster target) {
            return isCursed() ? 0 : 6 * Lumen.countInHand(true, this);
        }

        @Override
        protected int bonusHits() {
            return isCursed() ? Lumen.countInHand(false, this) : 0;
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            addToBot(new Do(Lumen::sealAllInHand));
            hit(m, AttackEffect.SLASH_HEAVY);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            addToBot(new Do(Lumen::erodeAllInHand));
            hits(m, AttackEffect.SLASH_HORIZONTAL);
        }
    }

    /** 1 (0) cost. 6, the next cursed face this turn is played twice / 2 x3, the next light face this turn is played twice. */
    public static class Echo extends DualFaceCard {
        public Echo() {
            super("Echo", 1, CardType.ATTACK, CardRarity.RARE,
                    CardTarget.ENEMY, FaceStats.of().damage(6, 0),
                    CardTarget.ENEMY, FaceStats.of().damage(2, 0).hits(3, 0));
            setSeal(Seal.of(Seal.Kind.NEXT_ATTACK, 5, 1));
        }

        @Override
        protected void onUpgrade() {
            upgradeBothCosts(0);
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            hit(m, AttackEffect.SLASH_DIAGONAL);
            addToBot(new ApplyPowerAction(p, p, new EchoPower(p, 1, true), 1));
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            hits(m, AttackEffect.SLASH_HORIZONTAL);
            addToBot(new ApplyPowerAction(p, p, new EchoPower(p, 1, false), 1));
        }
    }

    /** 2 cost. 20 (26), kill: double chime / 5 (6) x4, kill: gain 2 energy (once). */
    public static class Execution extends DualFaceCard {
        public Execution() {
            super("Execution", 2, CardType.ATTACK, CardRarity.RARE,
                    CardTarget.ENEMY, FaceStats.of().damage(20, 6).exhaust(),
                    CardTarget.ENEMY, FaceStats.of().damage(5, 1).hits(4, 0).exhaust());
            setSeal(Seal.of(Seal.Kind.DAMAGE_RANDOM, 8, 2));
        }

        /** The only kill card (v5). Light: kill gives 3 energy. */
        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            addToBot(new DamageThenAction(m, new DamageInfo(p, damage, damageTypeForTurn), AttackEffect.SLASH_HEAVY,
                    (killed, dealt) -> {
                        if (killed) AbstractDungeon.actionManager.addToTop(new GainEnergyAction(3));
                    }));
        }

        /** Cursed: the first killing hit draws 3 and doubles chime. */
        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            boolean[] paid = {false};
            for (int i = 0; i < magicNumber; i++) {
                addToBot(new DamageThenAction(m, new DamageInfo(p, damage, damageTypeForTurn),
                        AttackEffect.SLASH_HORIZONTAL, (killed, dealt) -> {
                            if (killed && !paid[0]) {
                                paid[0] = true;
                                Lumen.addChime(Lumen.chime());
                                AbstractDungeon.actionManager.addToTop(new DrawCardAction(p, 3));
                            }
                        }));
            }
        }
    }

    // ------------------------------------------------------------------ skills

    /** 1 (0) cost, exhaust. This turn light faces erode themselves after play and return to hand / same, draw 2. */
    public static class TwinSoul extends DualFaceCard {
        public TwinSoul() {
            super("TwinSoul", 1, CardType.SKILL, CardRarity.RARE,
                    CardTarget.SELF, FaceStats.of().exhaust(), CardTarget.SELF, FaceStats.of().exhaust());
            setSeal(Seal.of(Seal.Kind.DRAW, 1, 0));
        }

        @Override
        protected void onUpgrade() {
            upgradeBothCosts(0);
        }

        private void twin(AbstractPlayer p) {
            if (!p.hasPower(TwinSoulPower.POWER_ID)) {
                addToBot(new ApplyPowerAction(p, p, new TwinSoulPower(p, 1), 1));
            }
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            twin(p);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            twin(p);
            draw(2);
        }
    }

    /** 2 cost. Block = chime x4 (x5) / double chime, exhaust; cursed face costs 1 when upgraded. */
    public static class Overflow extends DualFaceCard {
        public Overflow() {
            super("Overflow", 2, CardType.SKILL, CardRarity.RARE,
                    CardTarget.SELF, FaceStats.of().block(0, 0),
                    CardTarget.SELF, FaceStats.of().cost(2, -1).exhaust());
            setSeal(Seal.of(Seal.Kind.CHIME, 2, 0));
        }

        @Override
        public int chimeBlockMultiplier() {
            return isCursed() ? 1 : (upgraded ? 5 : 4);
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            addToBot(new Do(() -> Lumen.addChime(Lumen.chime())));
        }
    }

    /** 3 (2) cost, exhaust. Draw 5 / draw 5 and erode them. */
    public static class TimeCurse extends DualFaceCard {
        public TimeCurse() {
            super("TimeCurse", 3, CardType.SKILL, CardRarity.RARE,
                    CardTarget.SELF, FaceStats.of().exhaust(), CardTarget.SELF, FaceStats.of().exhaust());
            setSeal(Seal.of(Seal.Kind.DRAW, 2, 0));
        }

        @Override
        protected void onUpgrade() {
            upgradeBothCosts(2);
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            draw(5);
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            addToBot(new DrawCardAction(5, new Do(() -> {
                for (AbstractCard c : DrawCardAction.drawnCards) Lumen.erode(c);
            })));
        }
    }

    /** Block 15 (20), seal every cursed card in hand / block 10 (14), same, gain 1 energy each (max 3). */
    public static class GreatSeal extends DualFaceCard {
        public GreatSeal() {
            super("GreatSeal", 1, CardType.SKILL, CardRarity.RARE,
                    CardTarget.SELF, FaceStats.of(),
                    CardTarget.SELF, FaceStats.of().block(10, 4).magic(3, 1));
            setSeal(Seal.of(Seal.Kind.BLOCK, 5, 1));
        }

        @Override
        protected void onUpgrade() {
            upgradeBothCosts(0);
        }

        /** Light: seal every cursed card in hand, each seal effect runs twice (payoff of the seal deck). */
        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            addToBot(new Do(() -> Lumen.sealAllInHand(2)));
        }

        /** Cursed: block, and cleanse up to 3 (4) cursed cards in the discard pile (no seal effect). */
        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
            int n = magicNumber;
            addToBot(new Do(() -> Lumen.cleanseDiscard(n)));
        }
    }

    /** Exhaust. Chime +5 (+7) / chime +8 (+11), lose 5 hp. */
    public static class Avatar extends DualFaceCard {
        public Avatar() {
            super("Avatar", 1, CardType.SKILL, CardRarity.RARE,
                    CardTarget.SELF, FaceStats.of().magic(5, 2).exhaust(),
                    CardTarget.SELF, FaceStats.of().magic(8, 3).exhaust());
            setSeal(Seal.of(Seal.Kind.CHIME, 2, 0));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            int n = magicNumber;
            addToBot(new Do(() -> Lumen.addChime(n)));
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            int n = magicNumber;
            addToBot(new LoseHPAction(p, p, 5));
            addToBot(new Do(() -> Lumen.addChime(n)));
        }
    }

    /** 2 cost. Block 20 (25), 1 energy next turn / block 10 (14), 2 energy and draw 2 next turn. */
    public static class WorldtreeVow extends DualFaceCard {
        public WorldtreeVow() {
            super("WorldtreeVow", 2, CardType.SKILL, CardRarity.RARE,
                    CardTarget.SELF, FaceStats.of().block(30, 5),
                    CardTarget.SELF, FaceStats.of().block(10, 4));
            setSeal(Seal.of(Seal.Kind.NEXT_TURN_ENERGY, 1, 0));
        }

        protected void useLight(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
            addToBot(new ApplyPowerAction(p, p, new EnergizedPower(p, 2), 2));
        }

        protected void useCursed(AbstractPlayer p, AbstractMonster m) {
            gainBlock();
            addToBot(new ApplyPowerAction(p, p, new EnergizedPower(p, 2), 2));
            addToBot(new ApplyPowerAction(p, p, new DrawCardNextTurnPower(p, 2), 2));
        }
    }
}
