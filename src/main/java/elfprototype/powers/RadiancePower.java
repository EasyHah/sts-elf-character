package elfprototype.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;

/** Light faces get double chime on block (read by ChimePower). */
public class RadiancePower extends ElfPower {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("Radiance");

    public RadiancePower(AbstractCreature owner, int amount) {
        super("Radiance", "Radiance", owner, amount, PowerType.BUFF);
    }
}
