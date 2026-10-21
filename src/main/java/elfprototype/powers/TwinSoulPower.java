package elfprototype.powers;

import basemod.BaseMod;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import elfprototype.mechanics.Do;
import elfprototype.mechanics.Lumen;

/** This turn, light faces erode themselves after being played and return to hand. */
public class TwinSoulPower extends ElfPower {
    public static final String POWER_ID = elfprototype.ElfPrototypeMod.makeID("TwinSoul");

    public TwinSoulPower(AbstractCreature owner, int amount) {
        super("TwinSoul", "TwinSoul", owner, amount, PowerType.BUFF);
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.purgeOnUse || !Lumen.isLight(card)) return;
        card.returnToHand = AbstractDungeon.player.hand.size() < BaseMod.MAX_HAND_SIZE;
        bot(new Do(() -> Lumen.erode(card)));
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer) removeSelf();
    }
}
