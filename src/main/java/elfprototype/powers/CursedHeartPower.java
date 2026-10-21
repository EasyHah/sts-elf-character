package elfprototype.powers;

import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.powers.StrengthPower;

/** Each backlash: gain amount Strength. */
public class CursedHeartPower extends ElfPower implements elfprototype.mechanics.SealListener {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("CursedHeart");

    public CursedHeartPower(AbstractCreature owner, int amount) {
        super("CursedHeart", "CursedHeart", owner, amount, PowerType.BUFF);
    }

    @Override
    public void onBacklash(int hpLost) {
        flash();
        bot(new ApplyPowerAction(owner, owner, new StrengthPower(owner, amount), amount));
    }
}
