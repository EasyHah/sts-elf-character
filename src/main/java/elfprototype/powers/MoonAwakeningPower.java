package elfprototype.powers;

import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;

/** Each seal (incl. auto seal): draw amount and gain amount energy. */
public class MoonAwakeningPower extends ElfPower implements elfprototype.mechanics.SealListener {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("MoonAwakening");

    public MoonAwakeningPower(AbstractCreature owner, int amount) {
        super("MoonAwakening", "MoonAwakening", owner, amount, PowerType.BUFF);
    }

    @Override
    public void onSeal(AbstractCard card, boolean auto) {
        flash();
        bot(new DrawCardAction(owner, amount));
        bot(new GainEnergyAction(amount));
    }
}
