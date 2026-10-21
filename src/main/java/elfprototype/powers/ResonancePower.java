package elfprototype.powers;

import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.core.AbstractCreature;

/** The first time chime reaches 4 each turn, gain amount energy. */
public class ResonancePower extends ElfPower implements elfprototype.mechanics.SealListener {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("Resonance");
    private boolean fired;

    public ResonancePower(AbstractCreature owner, int amount) {
        super("Resonance", "Resonance", owner, amount, PowerType.BUFF);
    }

    @Override
    public void atStartOfTurn() {
        fired = false;
    }

    @Override
    public void onChimeChanged(int before, int after) {
        if (!fired && before < 4 && after >= 4) {
            fired = true;
            flash();
            bot(new GainEnergyAction(amount));
        }
    }
}
