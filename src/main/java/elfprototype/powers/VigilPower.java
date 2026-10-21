package elfprototype.powers;

import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;

/** Each manual seal: gain amount block. */
public class VigilPower extends ElfPower implements elfprototype.mechanics.SealListener {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("Vigil");

    public VigilPower(AbstractCreature owner, int amount) {
        super("Vigil", "Vigil", owner, amount, PowerType.BUFF);
    }

    @Override
    public void onSeal(AbstractCard card, boolean auto) {
        if (auto) return;
        flash();
        bot(new GainBlockAction(owner, owner, amount));
    }
}
