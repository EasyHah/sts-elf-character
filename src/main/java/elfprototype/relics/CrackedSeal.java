package elfprototype.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import elfprototype.ElfPrototypeMod;
import elfprototype.mechanics.SealListener;

/** Starter relic: the first seal each turn (manual or automatic) draws 1 card. */
public class CrackedSeal extends CustomRelic implements SealListener {
    public static final String ID = ElfPrototypeMod.makeID("CrackedSeal");
    private static final String IMG = ElfPrototypeMod.MOD_ID + "/images/relics/";

    public CrackedSeal() {
        super(ID, ImageMaster.loadImage(IMG + "CrackedSeal.png"), ImageMaster.loadImage(IMG + "CrackedSealOutline.png"),
                RelicTier.STARTER, LandingSound.FLAT);
        largeImg = ImageMaster.loadImage(IMG + "large/CrackedSeal.png");
    }

    @Override
    public void atTurnStart() {
        grayscale = false;
    }

    @Override
    public void onSeal(AbstractCard card, boolean auto) {
        if (grayscale) return; // already used this turn
        grayscale = true;
        flash();
        addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        addToBot(new DrawCardAction(AbstractDungeon.player, 1));
    }

    @Override
    public void onVictory() {
        grayscale = false;
    }

    @Override
    public String getUpdatedDescription() {
        return DESCRIPTIONS[0];
    }
}
