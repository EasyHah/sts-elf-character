package elfprototype.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;

/** No backlash at the end of this turn (read by Lumen.backlash). */
public class NoBacklashPower extends ElfPower {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("NoBacklash");

    public NoBacklashPower(AbstractCreature owner, int amount) {
        super("NoBacklash", "Steady", owner, amount, PowerType.BUFF);
    }

    @Override
    public void atStartOfTurn() {
        removeSelf();
    }
}
