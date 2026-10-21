package elfprototype.powers;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;

/** When backlash costs hp, deal hp lost x amount damage to ALL enemies. */
public class BacklashEchoPower extends ElfPower implements elfprototype.mechanics.SealListener {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("BacklashEcho");

    public BacklashEchoPower(AbstractCreature owner, int amount) {
        super("BacklashEcho", "BacklashEcho", owner, amount, PowerType.BUFF);
    }

    @Override
    public void onBacklash(int hpLost) {
        flash();
        bot(new DamageAllEnemiesAction(owner, DamageInfo.createDamageMatrix(hpLost * amount, true),
                DamageInfo.DamageType.THORNS, AbstractGameAction.AttackEffect.FIRE));
    }
}
