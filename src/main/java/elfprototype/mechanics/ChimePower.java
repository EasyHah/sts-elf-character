package elfprototype.mechanics;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import elfprototype.ElfPrototypeMod;
import elfprototype.cards.DualFaceCard;
import elfprototype.powers.AfterglowPower;
import elfprototype.powers.LightOathPower;
import elfprototype.powers.RadiancePower;

/**
 * Chime: flips this turn. Elf cards get +amount damage per hit and +amount block, times the card's own chime
 * multiplier (ChargedShot, EmberShot, Shatter, ...); Light Oath doubles it on light faces, Radiance doubles
 * light-face block. Removed at the end of the player's turn (halved instead with Afterglow).
 */
public class ChimePower extends AbstractPower {
    public static final String POWER_ID = ElfPrototypeMod.makeID("Chime");
    private static Texture small;
    private static Texture large;

    public ChimePower(AbstractCreature owner, int amount) {
        PowerStrings s = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
        this.name = s.NAME;
        this.ID = POWER_ID;
        this.owner = owner;
        this.amount = amount;
        this.type = PowerType.BUFF;
        if (small == null) {
            small = ImageMaster.loadImage(ElfPrototypeMod.MOD_ID + "/images/powers/Chime.png");
            large = ImageMaster.loadImage(ElfPrototypeMod.MOD_ID + "/images/powers/large/Chime.png");
        }
        this.region48 = new TextureAtlas.AtlasRegion(small, 0, 0, small.getWidth(), small.getHeight());
        this.region128 = new TextureAtlas.AtlasRegion(large, 0, 0, large.getWidth(), large.getHeight());
        updateDescription();
    }

    private boolean ownerHas(String id) {
        return owner.hasPower(id);
    }

    @Override
    public float atDamageGive(float damage, DamageInfo.DamageType type, AbstractCard card) {
        if (type != DamageInfo.DamageType.NORMAL || !(card instanceof DualFaceCard)) return damage;
        DualFaceCard c = (DualFaceCard) card;
        int mult = c.chimeDamageMultiplier();
        if (!c.isCursed() && ownerHas(LightOathPower.POWER_ID)) mult *= 2;
        return damage + amount * mult;
    }

    @Override
    public float modifyBlock(float block, AbstractCard card) {
        if (!(card instanceof DualFaceCard)) return block;
        DualFaceCard c = (DualFaceCard) card;
        int mult = c.chimeBlockMultiplier();
        if (!c.isCursed() && ownerHas(LightOathPower.POWER_ID)) mult *= 2;
        if (!c.isCursed() && ownerHas(RadiancePower.POWER_ID)) mult *= 2;
        return block + amount * mult;
    }

    @Override
    public void atEndOfTurn(boolean isPlayer) {
        if (!isPlayer) return;
        int keep = ownerHas(AfterglowPower.POWER_ID) ? amount / 2 : 0;
        if (keep > 0) {
            amount = keep;
            updateDescription();
            flash();
        } else {
            addToBot(new RemoveSpecificPowerAction(owner, owner, this));
        }
    }

    @Override
    public void updateDescription() {
        PowerStrings s = CardCrawlGame.languagePack.getPowerStrings(POWER_ID);
        description = s.DESCRIPTIONS[0] + amount + s.DESCRIPTIONS[1] + amount + s.DESCRIPTIONS[2];
    }
}
