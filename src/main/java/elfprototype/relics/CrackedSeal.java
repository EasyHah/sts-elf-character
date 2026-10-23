package elfprototype.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import elfprototype.ElfPrototypeMod;
import elfprototype.mechanics.Lumen;
import elfprototype.mechanics.SealSelfAction;

/** Starter relic (v5): the first cursed face played each turn seals itself on the way to the discard pile, running its seal effect. */
public class CrackedSeal extends CustomRelic {
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
    public void onUseCard(AbstractCard card, UseCardAction action) {
        if (grayscale || card.purgeOnUse || !Lumen.isCursed(card)) return; // already used this turn
        grayscale = true;
        flash();
        addToBot(new RelicAboveCreatureAction(AbstractDungeon.player, this));
        addToBot(new SealSelfAction(card));
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
