package elfprototype.powers;

import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import elfprototype.mechanics.SealListener;

/** Penance (uncommon power, v5): the first seal each turn gains amount energy. */
public class PenancePower extends ElfPower implements SealListener {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("Penance");
    private boolean used;

    public PenancePower(AbstractCreature owner, int amount) {
        super("Penance", "Penance", owner, amount, PowerType.BUFF);
    }

    @Override
    public void atStartOfTurn() {
        used = false;
    }

    @Override
    public void onSeal(AbstractCard card, boolean auto) {
        if (used) return;
        used = true;
        flash();
        bot(new GainEnergyAction(amount));
    }
}
