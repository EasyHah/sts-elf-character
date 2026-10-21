package elfprototype.mechanics;

import basemod.BaseMod;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.CardGroup;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.function.Consumer;

/**
 * Pick 1 card from the discard pile, or from the top N cards of the draw pile, and put it into hand
 * (Rewind, Premonition). The callback runs on the card once it is in hand.
 */
public class PickPileAction extends AbstractGameAction {
    private final boolean fromDraw;
    private final int look;
    private final String prompt;
    private final Consumer<AbstractCard> onPick;
    private CardGroup source;

    private PickPileAction(boolean fromDraw, int look, String prompt, Consumer<AbstractCard> onPick) {
        this.fromDraw = fromDraw;
        this.look = look;
        this.prompt = prompt;
        this.onPick = onPick;
        this.actionType = ActionType.CARD_MANIPULATION;
        this.duration = this.startDuration = 0.25f;
    }

    public static PickPileAction fromDiscard(String prompt, Consumer<AbstractCard> onPick) {
        return new PickPileAction(false, 0, prompt, onPick);
    }

    public static PickPileAction fromDrawTop(int look, String prompt, Consumer<AbstractCard> onPick) {
        return new PickPileAction(true, look, prompt, onPick);
    }

    @Override
    public void update() {
        AbstractPlayer p = AbstractDungeon.player;
        if (duration == startDuration) {
            if (p.hand.size() >= BaseMod.MAX_HAND_SIZE) {
                p.createHandIsFullDialog();
                isDone = true;
                return;
            }
            CardGroup pile = fromDraw ? p.drawPile : p.discardPile;
            CardGroup view = new CardGroup(CardGroup.CardGroupType.UNSPECIFIED);
            if (fromDraw) {
                // The top of the draw pile is the end of the list.
                for (int i = 0; i < look && i < pile.size(); i++) view.addToTop(pile.group.get(pile.size() - 1 - i));
            } else {
                view.group.addAll(pile.group);
            }
            if (view.isEmpty()) {
                isDone = true;
                return;
            }
            source = pile;
            if (view.size() == 1) {
                take(view.getTopCard());
                isDone = true;
                return;
            }
            AbstractDungeon.gridSelectScreen.open(view, 1, prompt, false);
            tickDuration();
            return;
        }
        if (!AbstractDungeon.gridSelectScreen.selectedCards.isEmpty()) {
            AbstractCard c = AbstractDungeon.gridSelectScreen.selectedCards.get(0);
            AbstractDungeon.gridSelectScreen.selectedCards.clear();
            take(c);
            isDone = true;
        }
        tickDuration();
    }

    private void take(AbstractCard c) {
        AbstractPlayer p = AbstractDungeon.player;
        source.removeCard(c);
        c.unhover();
        c.lighten(true);
        c.setAngle(0f);
        c.drawScale = 0.12f;
        c.targetDrawScale = 0.75f;
        p.hand.addToHand(c);
        p.hand.refreshHandLayout();
        onPick.accept(c);
        p.hand.applyPowers();
    }
}
