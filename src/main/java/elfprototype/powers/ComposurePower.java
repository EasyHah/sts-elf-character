package elfprototype.powers;

import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import elfprototype.mechanics.Lumen;

/** End of turn: gain amount block for each light face in hand. */
public class ComposurePower extends ElfPower {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("Composure");

    public ComposurePower(AbstractCreature owner, int amount) {
        super("Composure", "Composure", owner, amount, PowerType.BUFF);
    }

    @Override
    public void atEndOfTurnPreEndTurnCards(boolean isPlayer) {
        if (!isPlayer) return;
        int n = Lumen.countInHand(false, null);
        if (n > 0) {
            flash();
            bot(new GainBlockAction(owner, owner, n * amount));
        }
    }
}
