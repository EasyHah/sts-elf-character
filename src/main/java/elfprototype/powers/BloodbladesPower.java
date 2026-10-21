package elfprototype.powers;

import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import elfprototype.mechanics.Lumen;

/** Cursed attacks deal +amount damage per hit. */
public class BloodbladesPower extends ElfPower {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("Bloodblades");

    public BloodbladesPower(AbstractCreature owner, int amount) {
        super("Bloodblades", "Bloodblades", owner, amount, PowerType.BUFF);
    }

    @Override
    public float atDamageGive(float damage, DamageInfo.DamageType type, AbstractCard card) {
        return type == DamageInfo.DamageType.NORMAL && Lumen.isCursedAttack(card) ? damage + amount : damage;
    }
}
