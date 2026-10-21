package elfprototype.mechanics;

import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.UIStrings;
import elfprototype.ElfPrototypeMod;

/**
 * Erode N / Seal N: the player picks N eligible cards from hand (all of them if there are N or fewer).
 * The card being played is in limbo, not in hand, so "another card" holds automatically.
 */
public final class FlipHandAction {
    private FlipHandAction() {
    }

    private static UIStrings text() {
        return CardCrawlGame.languagePack.getUIString(ElfPrototypeMod.makeID("Lumen"));
    }

    public static PickHandAction erode(int amount) {
        return new PickHandAction(text().TEXT[0], amount, Lumen::canErode, Lumen::erode);
    }

    public static PickHandAction seal(int amount) {
        return new PickHandAction(text().TEXT[1], amount, Lumen::canSeal, c -> Lumen.seal(c, false));
    }
}
