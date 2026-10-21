package elfprototype.powers;

import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;

/** The first amount seals (incl. auto seal) each turn draw 1 card each. */
public class RecollectionPower extends ElfPower implements elfprototype.mechanics.SealListener {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("Recollection");
    private int used;

    public RecollectionPower(AbstractCreature owner, int amount) {
        super("Recollection", "Recollection", owner, amount, PowerType.BUFF);
    }

    @Override
    public void atStartOfTurn() {
        used = 0;
    }

    @Override
    public void onSeal(AbstractCard card, boolean auto) {
        if (used < amount) {
            used++;
            flash();
            bot(new DrawCardAction(owner, 1));
        }
    }
}
