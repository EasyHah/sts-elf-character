package elfprototype.powers;

import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import elfprototype.mechanics.Lumen;

/**
 * Nock (light): the next light-face attack this turn deals +amount damage.
 * Nock (cursed): the next cursed-face attack this turn deals +amount damage per hit. Removed at end of turn.
 */
public class NockPower extends ElfPower {
    public static final String LIGHT_ID = elfprototype.ElfPrototypeMod.makeID("NockLight");
    public static final String CURSED_ID = elfprototype.ElfPrototypeMod.makeID("NockCursed");
    private final boolean cursed;

    public NockPower(AbstractCreature owner, int amount, boolean cursed) {
        super(cursed ? "NockCursed" : "NockLight", "Nock", owner, amount, PowerType.BUFF);
        this.cursed = cursed;
    }

    private boolean matches(AbstractCard c) {
        return c != null && c.type == AbstractCard.CardType.ATTACK
                && (cursed ? Lumen.isCursed(c) : Lumen.isLight(c));
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
