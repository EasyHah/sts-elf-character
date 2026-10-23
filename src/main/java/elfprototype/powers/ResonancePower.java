package elfprototype.powers;

import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.core.AbstractCreature;

/** Every time chime reaches a multiple of 3 (3, 6, 9, ...), gain amount energy. No per-turn limit (v5). */
public class ResonancePower extends ElfPower implements elfprototype.mechanics.SealListener {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("Resonance");

    public ResonancePower(AbstractCreature owner, int amount) {
        super("Resonance", "Resonance", owner, amount, PowerType.BUFF);
    }

    @Override
    public void onChimeChanged(int before, int after) {
        int crossed = after / 3 - before / 3;
        if (crossed > 0) {
            flash();
            bot(new GainEnergyAction(amount * crossed));
        }
    }
}
