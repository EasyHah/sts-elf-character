package elfprototype.powers;

import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import elfprototype.mechanics.Lumen;

/**
 * From seal effects: the next attack this turn (or the next light-face attack) deals +amount damage.
 * Unlike Nock it adds once to a single-hit card and to every hit of a multi-hit card the same way.
 */
public class NextAttackPower extends ElfPower {
    public static final String ANY_ID = elfprototype.ElfPrototypeMod.makeID("NextAttack");
    public static final String LIGHT_ID = elfprototype.ElfPrototypeMod.makeID("NextLightAttack");
    private final boolean lightOnly;

    public NextAttackPower(AbstractCreature owner, int amount, boolean lightOnly) {
        super(lightOnly ? "NextLightAttack" : "NextAttack", "Nock", owner, amount, PowerType.BUFF);
        this.lightOnly = lightOnly;
    }

    private boolean matches(AbstractCard c) {
        return c != null && c.type == AbstractCard.CardType.ATTACK && (!lightOnly || Lumen.isLight(c));
    }

    @Override
    public float atDamageGive(float damage, DamageInfo.DamageType type, AbstractCard card) {
        return type == DamageInfo.DamageType.NORMAL && matches(card) ? damage + amount : damage;
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (matches(card)) {
            flash();
            removeSelf();
        }
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer) removeSelf();
    }
}
