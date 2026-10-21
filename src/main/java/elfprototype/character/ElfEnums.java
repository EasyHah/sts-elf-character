package elfprototype.character;

import com.evacipated.cardcrawl.modthespire.lib.SpireEnum;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.helpers.CardLibrary;

/** Enum values added by ModTheSpire. The library type must share the card color's name. */
public final class ElfEnums {
    @SpireEnum
    public static AbstractPlayer.PlayerClass ELF;

    @SpireEnum(name = "ELF_GREEN")
    public static AbstractCard.CardColor ELF_GREEN;

    @SpireEnum(name = "ELF_GREEN")
    public static CardLibrary.LibraryType ELF_GREEN_LIBRARY;

    private ElfEnums() {
    }
}
