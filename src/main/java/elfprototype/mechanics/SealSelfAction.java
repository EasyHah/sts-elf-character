package elfprototype.mechanics;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;

/** Queued by Cracked Seal / Whole Seal when a cursed face is played: seals it (runs its seal effect) before UseCardAction puts it in the discard pile. */
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
