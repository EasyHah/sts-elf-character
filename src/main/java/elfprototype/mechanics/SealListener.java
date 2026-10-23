package elfprototype.mechanics;

import com.megacrit.cardcrawl.cards.AbstractCard;

/** Implemented by relics / powers that react to the elf's rules. Called inside actions, right after the event. */
public interface SealListener {
    default void onErode(AbstractCard card) {
    }

    /** @param auto true when Cracked / Whole Seal sealed a played cursed face on its way to the discard pile. */
    default void onSeal(AbstractCard card, boolean auto) {
    }

    /** Chime changed this turn (always an increase). */
    default void onChimeChanged(int before, int after) {
    }

    /** A cursed face in hand at end of turn made the player lose hp. */
    default void onBacklash(int hpLost) {
    }
}
