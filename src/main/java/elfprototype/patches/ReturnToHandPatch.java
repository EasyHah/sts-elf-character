package elfprototype.patches;

import basemod.ReflectionHacks;
import com.evacipated.cardcrawl.modthespire.lib.SpirePatch;
import com.evacipated.cardcrawl.modthespire.lib.SpirePostfixPatch;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import elfprototype.cards.DualFaceCard;

/**
 * A cursed face sets AbstractCard.returnToHand so UseCardAction puts it back in hand. The flag lives on the card,
 * so it is cleared once that UseCardAction is done; otherwise the light face would also bounce back next time.
 */
@SpirePatch(clz = UseCardAction.class, method = "update")
public class ReturnToHandPatch {
    @SpirePostfixPatch
    public static void postfix(UseCardAction __instance) {
        if (!__instance.isDone) return;
        AbstractCard c = ReflectionHacks.getPrivate(__instance, UseCardAction.class, "targetCard");
        if (c instanceof DualFaceCard) c.returnToHand = false;
    }
}
