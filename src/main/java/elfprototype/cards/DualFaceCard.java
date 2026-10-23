package elfprototype.cards;

import basemod.abstracts.CustomCard;
import basemod.helpers.TooltipInfo;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DamageAction;
import com.megacrit.cardcrawl.actions.common.DamageAllEnemiesAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.cards.DamageInfo;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import elfprototype.ElfPrototypeMod;
import elfprototype.character.ElfEnums;
import elfprototype.mechanics.FlipHandAction;
import elfprototype.mechanics.Lumen;

import java.util.Collections;
import java.util.List;

/**
 * A card with a light face (bow, guard) and a cursed face (twin blades). Cards start light every combat:
 * combat piles are fresh copies of the master deck and copies never inherit the cursed face.
 *
 * Strings: NAME / DESCRIPTION = light face, EXTENDED_DESCRIPTION[0] = cursed name, [1] = cursed text,
 * optional [2] = cursed text after upgrade; UPGRADE_DESCRIPTION = light text after upgrade (optional).
 * "{M2}" in a text is replaced by the face's magic2 value.
 *
 * Subclasses can add per-card rules through bonusDamage / bonusBlock / bonusHits / chime multipliers; those
 * are folded into the numbers shown on the card, so the description can keep using !D! / !B! / !M!.
 */
public abstract class DualFaceCard extends CustomCard {
    private static final String IMG = ElfPrototypeMod.MOD_ID + "/images/";

    protected final CardStrings strings;
    private final String simpleId;
    private final CardTarget lightTarget;
    private final CardTarget cursedTarget;
    /** Cost of faces without their own cost; changed by upgradeBothCosts. */
    private int cardCost;
    private FaceStats light;
    private FaceStats cursed;
    private boolean cursedFace;
    /** Exhaust regardless of face (copies made by Mirror). */
    public boolean alwaysExhaust;
    private boolean[] lightUpgrades = new boolean[3];
    private boolean[] cursedUpgrades = new boolean[3];
    /** True while a hover preview (the other face) is being built, so the preview gets no preview of its own. */
    private static boolean buildingPreview;

    protected DualFaceCard(String simpleId, int cost, CardType type, CardRarity rarity,
                           CardTarget lightTarget, FaceStats light, CardTarget cursedTarget, FaceStats cursed) {
        super(ElfPrototypeMod.makeID(simpleId), "", cardImage(simpleId, type, false), cost, "", type,
                ElfEnums.ELF_GREEN, rarity, lightTarget);
        this.strings = CardCrawlGame.languagePack.getCardStrings(cardID);
        this.simpleId = simpleId;
        this.lightTarget = lightTarget;
        this.cursedTarget = cursedTarget;
        this.cardCost = cost;
        this.light = light;
        this.cursed = cursed;
        applyFace();
    }

    private static String cardImage(String simpleId, CardType type, boolean cursed) {
        return IMG + "cards/" + type.name().toLowerCase() + "/" + simpleId + (cursed ? "_cursed" : "") + ".png";
    }

    public boolean isCursed() {
        return cursedFace;
    }

    protected FaceStats face() {
        return cursedFace ? cursed : light;
    }

    /** Only Lumen flips faces (so chime and listeners follow); copies use forceFace. */
    public void setCursed(boolean on) {
        if (cursedFace == on) return;
        cursedFace = on;
        applyFace();
        if (AbstractDungeon.player != null && AbstractDungeon.currMapNode != null) applyPowers();
    }

    /** Sets the face of a temporary copy (double plays) without counting as a flip. */
    public void forceFace(boolean on) {
        setCursed(on);
    }

    private void applyFace() {
        FaceStats f = face();
        String text;
        if (cursedFace) {
            String[] ext = strings.EXTENDED_DESCRIPTION;
            name = ext[0];
            text = upgraded && ext.length > 2 && !ext[2].isEmpty() ? ext[2] : ext[1];
        } else {
            name = strings.NAME;
            text = upgraded && strings.UPGRADE_DESCRIPTION != null ? strings.UPGRADE_DESCRIPTION : strings.DESCRIPTION;
        }
        if (upgraded) name += "+";
        rawDescription = text.replace("{M2}", String.valueOf(f.magic2));
        target = cursedFace ? cursedTarget : lightTarget;
        baseDamage = damage = f.damage;
        baseBlock = block = f.block;
        baseMagicNumber = magicNumber = f.magic;
        isMultiDamage = f.aoe;
        exhaust = f.exhaust || alwaysExhaust;
        int newCost = f.cost >= 0 ? f.cost : cardCost;
        if (cost != newCost) {
            cost = newCost;
            costForTurn = newCost;
        }
        boolean[] ups = cursedFace ? cursedUpgrades : lightUpgrades;
        upgradedDamage = upgraded && ups[0];
        upgradedBlock = upgraded && ups[1];
        upgradedMagicNumber = upgraded && ups[2];

        textureImg = cardImage(simpleId, type, cursedFace);
        loadCardImage(textureImg);
        String bg = IMG + "character/" + (cursedFace ? "cardback_cursed" : "cardback") + "/";
        String kind = type.name().toLowerCase();
        setBackgroundTexture(bg + "bg_" + kind + ".png", bg + "bg_" + kind + "_p.png");
        setOrbTexture(bg + "energy_orb.png", bg + "energy_orb_p.png");
        initializeTitle();
        initializeDescription();
        if (!buildingPreview) refreshOtherFacePreview();
    }

    /**
     * Hovering a card shows the other face next to it (the game's cardsToPreview, like the vanilla Shiv preview),
     * with the same upgrade state. Rebuilt whenever this card's face or upgrade changes.
     */
    private void refreshOtherFacePreview() {
        buildingPreview = true;
        try {
            DualFaceCard other = (DualFaceCard) makeCopy();
            if (upgraded) other.upgrade();
            other.forceFace(!cursedFace);
            cardsToPreview = other;
        } finally {
            buildingPreview = false;
        }
    }

    @Override
    public void upgrade() {
        if (upgraded) return;
        lightUpgrades = new boolean[]{light.damageUpgrades(), light.blockUpgrades(), light.magicUpgrades()};
        cursedUpgrades = new boolean[]{cursed.damageUpgrades(), cursed.blockUpgrades(), cursed.magicUpgrades()};
        light = light.upgraded();
        cursed = cursed.upgraded();
        upgradeName(); // sets upgraded / timesUpgraded; applyFace rebuilds the name of the current face
        onUpgrade();
        applyFace();
    }

    /** Extra upgrade effects (Innate, card-wide cost change, ...). Runs before the face is rebuilt. */
    protected void onUpgrade() {
    }

    /** Use for upgrades that lower the cost of both faces. */
    protected void upgradeBothCosts(int newCost) {
        cardCost = newCost;
        upgradedCost = true;
    }

    // ------------------------------------------------------------------ per-card rules

    public int chimeDamageMultiplier() {
        return 1;
    }

    public int chimeBlockMultiplier() {
        return 1;
    }

    /** Added to base damage while computing the shown number. */
    protected int bonusDamage(AbstractMonster target) {
        return 0;
    }

    protected int bonusBlock() {
        return 0;
    }

    /** Added to the hit count of a hits() face. */
    protected int bonusHits() {
        return 0;
    }

    /** Cost override for this turn (Pursuit, Starfall); -1 = none. */
    protected int conditionalCost() {
        return -1;
    }

    @Override
    public void applyPowers() {
        int realDamage = baseDamage;
        int realBlock = baseBlock;
        if (baseDamage >= 0) baseDamage += bonusDamage(null);
        if (baseBlock >= 0) baseBlock += bonusBlock();
        super.applyPowers();
        baseDamage = realDamage;
        baseBlock = realBlock;
        isDamageModified = damage != baseDamage;
        isBlockModified = block != baseBlock;
        refreshHitsAndCost();
    }

    @Override
    public void calculateCardDamage(AbstractMonster mo) {
        int realDamage = baseDamage;
        int realBlock = baseBlock;
        if (baseDamage >= 0) baseDamage += bonusDamage(mo);
        if (baseBlock >= 0) baseBlock += bonusBlock();
        super.calculateCardDamage(mo);
        baseDamage = realDamage;
        baseBlock = realBlock;
        isDamageModified = damage != baseDamage;
        isBlockModified = block != baseBlock;
        refreshHitsAndCost();
    }

    private void refreshHitsAndCost() {
        if (face().hits) {
            magicNumber = Math.max(0, baseMagicNumber + bonusHits() + Lumen.extraHits(this));
            isMagicNumberModified = magicNumber != baseMagicNumber;
        }
        int c = conditionalCost();
        if (c >= 0 && costForTurn > c) setCostForTurn(c);
    }

    // ------------------------------------------------------------------ play

    @Override
    public final void use(AbstractPlayer p, AbstractMonster m) {
        Lumen.notePlayed(cursedFace);
        if (cursedFace) {
            useCursed(p, m);
            if (!purgeOnUse) Lumen.queueAutoSeal(this); // temporary double-play copies just vanish
        } else {
            useLight(p, m);
        }
    }

    protected abstract void useLight(AbstractPlayer p, AbstractMonster m);

    protected abstract void useCursed(AbstractPlayer p, AbstractMonster m);

    /** Backlash: a cursed face still in hand when the turn ends. */
    @Override
    public void triggerOnEndOfPlayerTurn() {
        super.triggerOnEndOfPlayerTurn();
        if (cursedFace) Lumen.backlash();
    }

    @Override
    public List<TooltipInfo> getCustomTooltips() {
        if (!cursedFace) return Collections.emptyList();
        UIStrings ui = CardCrawlGame.languagePack.getUIString(ElfPrototypeMod.makeID("Lumen"));
        return Collections.singletonList(new TooltipInfo(ui.TEXT[2], ui.TEXT[3]));
    }

    // ------------------------------------------------------------------ helpers for subclasses

    protected void hit(AbstractMonster m, AbstractGameAction.AttackEffect fx) {
        addToBot(new DamageAction(m, new DamageInfo(AbstractDungeon.player, damage, damageTypeForTurn), fx));
    }

    /** magicNumber hits on one target. */
    protected void hits(AbstractMonster m, AbstractGameAction.AttackEffect fx) {
        for (int i = 0; i < magicNumber; i++) hit(m, fx);
    }

    protected void hitAll(AbstractGameAction.AttackEffect fx) {
        addToBot(new DamageAllEnemiesAction(AbstractDungeon.player, multiDamage, damageTypeForTurn, fx));
    }

    protected void hitsAll(AbstractGameAction.AttackEffect fx) {
        for (int i = 0; i < magicNumber; i++) hitAll(fx);
    }

    protected void gainBlock() {
        addToBot(new GainBlockAction(AbstractDungeon.player, AbstractDungeon.player, block));
    }

    protected void draw(int n) {
        if (n > 0) addToBot(new DrawCardAction(AbstractDungeon.player, n));
    }

    protected void erode(int n) {
        if (n > 0) addToBot(FlipHandAction.erode(n));
    }

    protected void seal(int n) {
        if (n > 0) addToBot(FlipHandAction.seal(n));
    }

    protected int m2() {
        return face().magic2;
    }
}
