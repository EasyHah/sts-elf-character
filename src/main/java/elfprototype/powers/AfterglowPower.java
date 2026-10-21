package elfprototype.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;

/** Chime is only halved at end of turn (read by ChimePower). */
public class AfterglowPower extends ElfPower {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("Afterglow");

    public AfterglowPower(AbstractCreature owner, int amount) {
        super("Afterglow", "Afterglow", owner, amount, PowerType.BUFF);
    }
}
