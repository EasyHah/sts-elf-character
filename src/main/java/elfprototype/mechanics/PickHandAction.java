package elfprototype.mechanics;

import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Predicate;

/**
 * Pick N cards from hand that match a filter (all of them without a prompt if there are N or fewer).
 * The chosen cards are put back into hand before the callback runs, so the callback may exhaust or copy them.
 */
public class PickHandAction extends AbstractGameAction {
    private final String prompt;
    private final Predicate<AbstractCard> filter;
    private final Consumer<AbstractCard> onPick;
    private final List<AbstractCard> hidden = new ArrayList<>();

    public PickHandAction(String prompt, int amount, Predicate<AbstractCard> filter, Consumer<AbstractCard> onPick) {
        this.prompt = prompt;
        this.amount = amount;
        this.filter = filter;
        this.onPick = onPick;
        this.actionType = ActionType.CARD_MANIPULATION;
        this.duration = this.startDuration = 0.25f;
    }

    @Override
    public void update() {
        AbstractPlayer p = AbstractDungeon.player;
        if (duration == startDuration) {
            List<AbstractCard> ok = new ArrayList<>();
            for (AbstractCard c : p.hand.group) {
                if (filter.test(c)) ok.add(c);
                else hidden.add(c);
            }
            if (amount <= 0 || ok.isEmpty()) {
                isDone = true;
                return;
            }
            if (ok.size() <= amount) {
                hidden.clear();
                for (AbstractCard c : ok) onPick.accept(c);
                isDone = true;
                return;
            }
            p.hand.group.removeAll(hidden);
            AbstractDungeon.handCardSelectScreen.open(prompt, amount, false, false);
            tickDuration();
            return;
        }
        if (!AbstractDungeon.handCardSelectScreen.wereCardsRetrieved) {
            List<AbstractCard> picked = new ArrayList<>(AbstractDungeon.handCardSelectScreen.selectedCards.group);
            for (AbstractCard c : picked) p.hand.addToTop(c);
            for (AbstractCard c : hidden) p.hand.addToTop(c);
            AbstractDungeon.handCardSelectScreen.wereCardsRetrieved = true;
            AbstractDungeon.handCardSelectScreen.selectedCards.group.clear();
            for (AbstractCard c : picked) onPick.accept(c);
            p.hand.refreshHandLayout();
            p.hand.applyPowers();
            isDone = true;
        }
        tickDuration();
    }
}
