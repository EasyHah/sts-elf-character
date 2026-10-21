package elfprototype.powers;

import com.megacrit.cardcrawl.core.AbstractCreature;
import elfprototype.mechanics.Do;
import elfprototype.mechanics.Lumen;

/** After the start-of-turn draw, erode the leftmost amount light cards in hand. */
public class CreepingCursePower extends ElfPower {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("CreepingCurse");

    public CreepingCursePower(AbstractCreature owner, int amount) {
        super("CreepingCurse", "CreepingCurse", owner, amount, PowerType.BUFF);
    }

    @Override
    public void atStartOfTurnPostDraw() {
        flash();
        bot(new Do(() -> Lumen.erodeLeftmost(amount)));
    }
}
