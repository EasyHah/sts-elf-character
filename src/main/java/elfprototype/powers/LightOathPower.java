package elfprototype.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;

/** Light faces get double chime on damage and block (read by ChimePower). */
public class LightOathPower extends ElfPower {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("LightOath");

    public LightOathPower(AbstractCreature owner, int amount) {
        super("LightOath", "LightOath", owner, amount, PowerType.BUFF);
    }
}
