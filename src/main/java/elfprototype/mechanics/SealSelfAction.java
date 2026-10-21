package elfprototype.mechanics;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;

/** Queued by a cursed face's use(): turns it light again before UseCardAction moves it back to hand. */
public class SealSelfAction extends AbstractGameAction {
    private final AbstractCard card;

    public SealSelfAction(AbstractCard card) {
        this.card = card;
        this.actionType = ActionType.SPECIAL;
    }

    @Override
    public void update() {
        Lumen.seal(card, true);
        isDone = true;
    }
}
