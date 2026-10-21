package elfprototype.relics;

import basemod.abstracts.CustomRelic;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.actions.common.DrawCardAction;
import com.megacrit.cardcrawl.actions.common.GainBlockAction;
import com.megacrit.cardcrawl.actions.common.GainEnergyAction;
import com.megacrit.cardcrawl.actions.common.HealAction;
import com.megacrit.cardcrawl.actions.common.RelicAboveCreatureAction;
import com.megacrit.cardcrawl.actions.utility.UseCardAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import elfprototype.ElfPrototypeMod;
import elfprototype.mechanics.Do;
import elfprototype.mechanics.Lumen;
import elfprototype.mechanics.SealListener;

/** Elf relics other than the starter (design v4 §5). Images: relics/<id>.png, <id>Outline.png, large/<id>.png. */
public final class ElfRelics {
    private ElfRelics() {
    }

    private static final String IMG = ElfPrototypeMod.MOD_ID + "/images/relics/";

    /** Shared image loading and helpers. */
    public abstract static class ElfRelic extends CustomRelic {
        protected ElfRelic(String simpleId, RelicTier tier, LandingSound sound) {
            super(ElfPrototypeMod.makeID(simpleId), ImageMaster.loadImage(IMG + simpleId + ".png"),
                    ImageMaster.loadImage(IMG + simpleId + "Outline.png"), tier, sound);
            largeImg = ImageMaster.loadImage(IMG + "large/" + simpleId + ".png");
        }

        @Override
        public String getUpdatedDescription() {
            return DESCRIPTIONS[0];
        }

        protected AbstractPlayer p() {
            return AbstractDungeon.player;
        }

        protected void show() {
            flash();
            addToBot(new RelicAboveCreatureAction(p(), this));
        }

        protected void bot(AbstractGameAction a) {
            addToBot(a);
        }

        protected static boolean inCombat() {
            return AbstractDungeon.currMapNode != null && AbstractDungeon.getCurrRoom() != null
                    && AbstractDungeon.getCurrRoom().phase == AbstractRoom.RoomPhase.COMBAT;
        }
    }

    /** Boss, replaces Cracked Seal: the first 2 seals each turn draw 1 card each. */
    public static class WholeSeal extends ElfRelic implements SealListener {
        public static final String ID = ElfPrototypeMod.makeID("WholeSeal");

        public WholeSeal() {
            super("WholeSeal", RelicTier.BOSS, LandingSound.MAGICAL);
        }

        @Override
        public boolean canSpawn() {
            return AbstractDungeon.player.hasRelic(CrackedSeal.ID);
        }

        @Override
        public void obtain() {
            AbstractPlayer p = AbstractDungeon.player;
            for (int i = 0; i < p.relics.size(); i++) {
                if (p.relics.get(i).relicId.equals(CrackedSeal.ID)) {
                    instantObtain(p, i, true);
                    return;
                }
            }
            super.obtain();
        }

        @Override
        public void atTurnStart() {
            counter = 0;
        }

        @Override
        public void onSeal(AbstractCard card, boolean auto) {
            if (counter >= 2) return;
            counter++;
            show();
            bot(new DrawCardAction(p(), 1));
        }

        @Override
        public void onVictory() {
            counter = -1;
        }
    }

    /** Common: after the first draw of each combat, erode the leftmost card in hand. */
    public static class DuskGauntlet extends ElfRelic {
        private boolean firstTurn;

        public DuskGauntlet() {
            super("DuskGauntlet", RelicTier.COMMON, LandingSound.HEAVY);
        }

        @Override
        public void atPreBattle() {
            firstTurn = true;
        }

        @Override
        public void atTurnStartPostDraw() {
            if (!firstTurn) return;
            firstTurn = false;
            show();
            bot(new Do(() -> Lumen.erodeLeftmost(1)));
        }
    }

    /** Common: the first manual seal each turn gives 4 Block. */
    public static class SilverleafBrooch extends ElfRelic implements SealListener {
        public SilverleafBrooch() {
            super("SilverleafBrooch", RelicTier.COMMON, LandingSound.CLINK);
        }

        @Override
        public void atTurnStart() {
            grayscale = false;
        }

        @Override
        public void onSeal(AbstractCard card, boolean auto) {
            if (auto || grayscale) return;
            grayscale = true;
            show();
            bot(new GainBlockAction(p(), p(), 4));
        }

        @Override
        public void onVictory() {
            grayscale = false;
        }
    }

    /** Uncommon: the first cursed face played each turn deals +3 damage per hit. */
    public static class MirrorEarring extends ElfRelic {
        public MirrorEarring() {
            super("MirrorEarring", RelicTier.UNCOMMON, LandingSound.CLINK);
        }

        @Override
        public void atTurnStart() {
            grayscale = false;
        }

        @Override
        public float atDamageModify(float damage, AbstractCard c) {
            return !grayscale && Lumen.isCursed(c) ? damage + 3 : damage;
        }

        @Override
        public void onUseCard(AbstractCard card, UseCardAction action) {
            if (!grayscale && Lumen.isCursed(card)) {
                grayscale = true;
                flash();
            }
        }

        @Override
        public void onVictory() {
            grayscale = false;
        }
    }

    /** Uncommon: end of turn with no cursed face in hand: gain 4 Block. */
    public static class MossCharm extends ElfRelic {
        public MossCharm() {
            super("MossCharm", RelicTier.UNCOMMON, LandingSound.FLAT);
        }

        @Override
        public void onPlayerEndTurn() {
            if (Lumen.countInHand(true, null) == 0) {
                show();
                bot(new GainBlockAction(p(), p(), 4));
            }
        }
    }

    /** Rare (RE01): 6 Block at the start of combat; the first potion used each combat heals 3. */
    public static class WorldtreeBlessing extends ElfRelic {
        public WorldtreeBlessing() {
            super("WorldtreeBlessing", RelicTier.RARE, LandingSound.MAGICAL);
        }

        @Override
        public void atPreBattle() {
            grayscale = false;
        }

        @Override
        public void atBattleStart() {
            show();
            bot(new GainBlockAction(p(), p(), 6));
        }

        @Override
        public void onUsePotion() {
            if (grayscale || !inCombat()) return;
            grayscale = true; // mark first, then heal (no re-trigger)
            show();
            bot(new HealAction(p(), p(), 3));
        }

        @Override
        public void onVictory() {
            grayscale = false;
        }
    }

    /** Rare: the first time chime reaches 6 each turn, gain 1 energy and draw 1. */
    public static class EclipseStone extends ElfRelic implements SealListener {
        public EclipseStone() {
            super("EclipseStone", RelicTier.RARE, LandingSound.SOLID);
        }

        @Override
        public void atTurnStart() {
            grayscale = false;
        }

        @Override
        public void onChimeChanged(int before, int after) {
            if (grayscale || before >= 6 || after < 6) return;
            grayscale = true;
            show();
            bot(new GainEnergyAction(1));
            bot(new DrawCardAction(p(), 1));
        }

        @Override
        public void onVictory() {
            grayscale = false;
        }
    }

    /** Shop: cursed attacks deal +1 damage per hit. */
    public static class Whetstone extends ElfRelic {
        public Whetstone() {
            super("Whetstone_Elf", RelicTier.SHOP, LandingSound.SOLID);
        }

        @Override
        public float atDamageModify(float damage, AbstractCard c) {
            return Lumen.isCursedAttack(c) ? damage + 1 : damage;
        }
    }

    /** Boss: +1 energy each turn; after the turn's draw, erode the leftmost card in hand. */
    public static class CursedCrown extends ElfRelic {
        public CursedCrown() {
            super("CursedCrown", RelicTier.BOSS, LandingSound.HEAVY);
        }

        @Override
        public void onEquip() {
            AbstractDungeon.player.energy.energyMaster++;
        }

        @Override
        public void onUnequip() {
            AbstractDungeon.player.energy.energyMaster--;
        }

        @Override
        public void atTurnStartPostDraw() {
            show();
            bot(new Do(() -> Lumen.erodeLeftmost(1)));
        }
    }
}
