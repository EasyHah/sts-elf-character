package elfprototype.powers;

import com.megacrit.cardcrawl.actions.common.ReducePowerAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardQueueItem;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import elfprototype.cards.DualFaceCard;
import elfprototype.mechanics.Lumen;

/**
 * Echo: this turn the next amount card(s) with the given face are played a second time (copy, same face,
 * purged after use, so it does not seal or return). Same pattern as the vanilla Double Tap power.
 */
public class EchoPower extends ElfPower {
    public static final String LIGHT_ID = elfprototype.ElfPrototypeMod.makeID("EchoLight");
    public static final String CURSED_ID = elfprototype.ElfPrototypeMod.makeID("EchoCursed");
    private final boolean cursed;

    public EchoPower(AbstractCreature owner, int amount, boolean cursed) {
        super(cursed ? "EchoCursed" : "EchoLight", "Echo", owner, amount, PowerType.BUFF);
        this.cursed = cursed;
    }

    @Override
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (card.purgeOnUse || !(cursed ? Lumen.isCursed(card) : Lumen.isLight(card))) return;
        flash();
        AbstractMonster m = action.target instanceof AbstractMonster ? (AbstractMonster) action.target : null;
        AbstractCard tmp = card.makeSameInstanceOf();
        ((DualFaceCard) tmp).forceFace(cursed);
        AbstractDungeon.player.limbo.addToBottom(tmp);
        tmp.current_x = card.current_x;
        tmp.current_y = card.current_y;
        tmp.target_x = Settings.WIDTH / 2.0f - 300.0f * Settings.scale;
        tmp.target_y = Settings.HEIGHT / 2.0f;
        if (m != null) tmp.calculateCardDamage(m);
        tmp.purgeOnUse = true;
        AbstractDungeon.actionManager.addCardQueueItem(new CardQueueItem(tmp, m, card.energyOnUse, true, true), true);
        addToBot(new ReducePowerAction(owner, owner, this, 1));
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (isPlayer) removeSelf();
    }
}
