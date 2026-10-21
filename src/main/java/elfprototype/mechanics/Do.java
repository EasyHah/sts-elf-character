package elfprototype.mechanics;

import com.megacrit.cardcrawl.actions.AbstractGameAction;

/** Runs a piece of code when the action queue reaches it, so it sees the state left by the actions before it. */
public class Do extends AbstractGameAction {
    private final Runnable body;

    public Do(Runnable body) {
        this.body = body;
        this.actionType = ActionType.SPECIAL;
    }

    @Override
    public void update() {
        body.run();
        isDone = true;
    }
}
