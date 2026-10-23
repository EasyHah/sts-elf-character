package elfprototype.cards;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.DamageRandomEnemyAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.EnergizedPower;
import com.megacrit.cardcrawl.powers.NextTurnBlockPower;
import com.megacrit.cardcrawl.powers.VulnerablePower;
import com.megacrit.cardcrawl.powers.WeakPower;
import elfprototype.mechanics.Do;
import elfprototype.mechanics.FlipHandAction;
import elfprototype.mechanics.Lumen;
import elfprototype.powers.NextAttackPower;

/**
 * A card's seal effect (design v5): runs whenever the card is sealed (flipped from cursed back to light).
 * Always weaker than the card's cursed face, free and instant. Seal damage is fixed (THORNS type: no strength,
 * no chime). The text comes from UIStrings "elfprototype:Seal" TEXT[kind.ordinal()] with {S} = amount.
 */
public final class Seal {
    public enum Kind {
        NONE, DAMAGE_RANDOM, DAMAGE_ALL, BLOCK, DRAW, CHIME, ENERGY, WEAK_RANDOM, VULN_RANDOM, NEXT_ATTACK,
        NEXT_LIGHT_ATTACK, HEAL, NEXT_TURN_BLOCK, NEXT_TURN_ENERGY, STRIP_BLOCK_RANDOM, ERODE, DAMAGE_RANDOM_TWICE,
        DAMAGE_CHIME_RANDOM, DAMAGE_CHIME_ALL, BLOCK_CHIME
    }

    public static final Seal NONE = new Seal(Kind.NONE, 0, 0);

    public final Kind kind;
    private final int base;
    private final int up;

    private Seal(Kind kind, int base, int up) {
        this.kind = kind;
        this.base = base;
        this.up = up;
    }

    public static Seal of(Kind kind, int base, int up) {
        return new Seal(kind, base, up);
    }

    public static Seal of(Kind kind, int base) {
        return new Seal(kind, base, 0);
    }

    public int amount(boolean upgraded) {
        return base + (upgraded ? up : 0);
    }

    private static void bot(AbstractGameAction a) {
        AbstractDungeon.actionManager.addToBottom(a);
    }

    private static AbstractMonster randomEnemy() {
        return AbstractDungeon.getMonsters().getRandomMonster(null, true, AbstractDungeon.cardRandomRng);
    }

    /** Queues the effect. Called from inside an action (Lumen.seal), so everything goes to the bottom. */
    public void run(AbstractPlayer p, boolean upgraded) {
        int n = amount(upgraded);
        switch (kind) {
            case NONE:
                return;
            case DAMAGE_RANDOM:
                bot(new DamageRandomEnemyAction(new DamageInfo(p, n, DamageInfo.DamageType.THORNS),
                        AbstractGameAction.AttackEffect.SLASH_DIAGONAL));
                return;
            case DAMAGE_RANDOM_TWICE:
                for (int i = 0; i < 2; i++) {
                    bot(new DamageRandomEnemyAction(new DamageInfo(p, n, DamageInfo.DamageType.THORNS),
                            AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                }
                return;
            case DAMAGE_ALL:
                bot(new DamageAllEnemiesAction(p, DamageInfo.createDamageMatrix(n, true), DamageInfo.DamageType.THORNS,
                        AbstractGameAction.AttackEffect.SLASH_HORIZONTAL));
                return;
            case DAMAGE_CHIME_RANDOM:
                bot(new Do(() -> AbstractDungeon.actionManager.addToTop(new DamageRandomEnemyAction(
                        new DamageInfo(p, Lumen.chime(), DamageInfo.DamageType.THORNS),
                        AbstractGameAction.AttackEffect.SLASH_HEAVY))));
                return;
            case DAMAGE_CHIME_ALL:
                bot(new Do(() -> AbstractDungeon.actionManager.addToTop(new DamageAllEnemiesAction(p,
                        DamageInfo.createDamageMatrix(Lumen.chime(), true), DamageInfo.DamageType.THORNS,
                        AbstractGameAction.AttackEffect.BLUNT_HEAVY))));
                return;
            case BLOCK:
                bot(new GainBlockAction(p, p, n));
                return;
            case BLOCK_CHIME:
                bot(new Do(() -> AbstractDungeon.actionManager.addToTop(new GainBlockAction(p, p, Lumen.chime()))));
                return;
            case DRAW:
                bot(new DrawCardAction(p, n));
                return;
            case CHIME:
                bot(new Do(() -> Lumen.addChime(n)));
                return;
            case ENERGY:
                bot(new GainEnergyAction(n));
                return;
            case WEAK_RANDOM:
                bot(new Do(() -> {
                    AbstractMonster m = randomEnemy();
                    if (m != null) AbstractDungeon.actionManager.addToTop(new ApplyPowerAction(m, p, new WeakPower(m, n, false), n));
                }));
                return;
            case VULN_RANDOM:
                bot(new Do(() -> {
                    AbstractMonster m = randomEnemy();
                    if (m != null) AbstractDungeon.actionManager.addToTop(new ApplyPowerAction(m, p, new VulnerablePower(m, n, false), n));
                }));
                return;
            case STRIP_BLOCK_RANDOM:
                bot(new Do(() -> {
                    AbstractMonster m = randomEnemy();
                    if (m != null) m.loseBlock(n);
                }));
                return;
            case NEXT_ATTACK:
                bot(new ApplyPowerAction(p, p, new NextAttackPower(p, n, false), n));
                return;
            case NEXT_LIGHT_ATTACK:
                bot(new ApplyPowerAction(p, p, new NextAttackPower(p, n, true), n));
                return;
            case HEAL:
                bot(new HealAction(p, p, n));
                return;
            case NEXT_TURN_BLOCK:
                bot(new ApplyPowerAction(p, p, new NextTurnBlockPower(p, n), n));
                return;
            case NEXT_TURN_ENERGY:
                bot(new ApplyPowerAction(p, p, new EnergizedPower(p, n), n));
                return;
            case ERODE:
                bot(FlipHandAction.erode(n));
                return;
            default:
                throw new IllegalStateException("unhandled seal kind " + kind);
        }
    }
}
