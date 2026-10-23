package elfprototype.powers;

import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;

/** Each erode draws amount; each seal gains amount energy (v5: erode gives cards, seal gives energy). */
public class MoonAwakeningPower extends ElfPower implements elfprototype.mechanics.SealListener {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("MoonAwakening");

    public MoonAwakeningPower(AbstractCreature owner, int amount) {
        super("MoonAwakening", "MoonAwakening", owner, amount, PowerType.BUFF);
    }

    @Override
    public void onErode(AbstractCard card) {
        flash();
        bot(new DrawCardAction(owner, amount));
    }

    @Override
    public void onSeal(AbstractCard card, boolean auto) {
        flash();
        bot(new GainEnergyAction(amount));
    }
}
