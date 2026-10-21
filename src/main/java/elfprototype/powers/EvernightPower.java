package elfprototype.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;

/** Cursed attacks hit amount extra times (read by Lumen.extraHits). */
public class EvernightPower extends ElfPower {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("Evernight");

    public EvernightPower(AbstractCreature owner, int amount) {
        super("Evernight", "Evernight", owner, amount, PowerType.BUFF);
    }
}
