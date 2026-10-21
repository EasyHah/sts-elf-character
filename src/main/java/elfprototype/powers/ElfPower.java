package elfprototype.powers;

import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.TextureAtlas;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.RemoveSpecificPowerAction;
import com.megacrit.cardcrawl.core.AbstractCreature;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.powers.AbstractPower;
import elfprototype.ElfPrototypeMod;

import java.util.HashMap;
import java.util.Map;

/**
 * Base for the elf's powers: id = mod id + simple id, icon = images/powers/(large/)iconKey.png,
 * description = DESCRIPTIONS[0] with "{N}" replaced by the amount ([1] is used instead when amount is 1, if present).
 */
public abstract class ElfPower extends AbstractPower {
    private static final Map<String, Texture[]> ICONS = new HashMap<>();

    protected ElfPower(String simpleId, String iconKey, AbstractCreature owner, int amount, PowerType type) {
        this.ID = ElfPrototypeMod.makeID(simpleId);
        this.name = strings().NAME;
        this.owner = owner;
        this.amount = amount;
        this.type = type;
        Texture[] t = ICONS.computeIfAbsent(iconKey, k -> new Texture[]{
                ImageMaster.loadImage(ElfPrototypeMod.MOD_ID + "/images/powers/" + k + ".png"),
                ImageMaster.loadImage(ElfPrototypeMod.MOD_ID + "/images/powers/large/" + k + ".png")});
        this.region48 = new TextureAtlas.AtlasRegion(t[0], 0, 0, t[0].getWidth(), t[0].getHeight());
        this.region128 = new TextureAtlas.AtlasRegion(t[1], 0, 0, t[1].getWidth(), t[1].getHeight());
        updateDescription();
    }

    protected PowerStrings strings() {
        return CardCrawlGame.languagePack.getPowerStrings(ID);
    }

    @Override
    public void updateDescription() {
        String[] d = strings().DESCRIPTIONS;
        String t = amount == 1 && d.length > 1 ? d[1] : d[0];
        description = t.replace("{N}", String.valueOf(amount));
    }

    protected void removeSelf() {
        addToBot(new RemoveSpecificPowerAction(owner, owner, this));
    }

    protected void bot(AbstractGameAction a) {
        addToBot(a);
    }
}
