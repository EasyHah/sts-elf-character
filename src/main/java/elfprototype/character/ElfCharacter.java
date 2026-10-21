package elfprototype.character;

import basemod.abstracts.CustomPlayer;
import basemod.animations.AbstractAnimation;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.g2d.BitmapFont;
import com.megacrit.cardcrawl.actions.AbstractGameAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.core.EnergyManager;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ScreenShake;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.screens.CharSelectInfo;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import elfprototype.ElfPrototypeMod;
import elfprototype.cards.CurseWhisper;
import elfprototype.cards.DefendElf;
import elfprototype.cards.SingleEdge;
import elfprototype.cards.StrikeElf;
import elfprototype.relics.CrackedSeal;

import java.util.ArrayList;

/**
 * The elf (design v4). The battle body is drawn by ElfSkin (PNG frame animations, patched into
 * AbstractPlayer.render), so the BaseMod animation slot gets an empty animation.
 */
public class ElfCharacter extends CustomPlayer {
    public static final String ID = ElfPrototypeMod.makeID("Elf");
    public static final Color CARD_COLOR = new Color(0.36f, 0.62f, 0.38f, 1f);
    public static final int MAX_HP = 70;
    private static final String CHAR = ElfPrototypeMod.MOD_ID + "/images/character/";
    private static final String ORB = CHAR + "energyorb/";
    /** BaseMod CustomEnergyOrb: N energy layers, the base layer, then N empty-energy layers. */
    private static final String[] ORB_TEXTURES = {
            ORB + "layer1.png", ORB + "layer2.png", ORB + "layer3.png", ORB + "layer4.png", ORB + "layer5.png",
            ORB + "cover.png",
            ORB + "layer1d.png", ORB + "layer2d.png", ORB + "layer3d.png", ORB + "layer4d.png", ORB + "layer5d.png"};
    private static final float[] LAYER_SPEED = {-40f, -32f, 20f, -20f, 0f};
    public static final String BUTTON = CHAR + "select/button.png";
    public static final String PORTRAIT = CHAR + "select/portrait.png";

    private static CharacterStrings strings() {
        return CardCrawlGame.languagePack.getCharacterString(ID);
    }

    /** Placeholder for the BaseMod animation slot; ElfSkin draws the body. */
    private static final class NoAnimation extends AbstractAnimation {
        @Override
        public Type type() {
            return Type.NONE;
        }
    }

    public ElfCharacter(String name) {
        super(name, ElfEnums.ELF, ORB_TEXTURES, ORB + "vfx.png", LAYER_SPEED, new NoAnimation());
        initializeClass(CHAR + "image.png", CHAR + "shoulder2.png", CHAR + "shoulder.png", CHAR + "corpse.png",
                getLoadout(), 20f, -10f, 200f, 300f, new EnergyManager(3));
        dialogX = drawX + 0f * Settings.scale;
        dialogY = drawY + 240f * Settings.scale;
    }

    @Override
    public ArrayList<String> getStartingDeck() {
        ArrayList<String> deck = new ArrayList<>();
        for (int i = 0; i < 4; i++) deck.add(ElfPrototypeMod.makeID(StrikeElf.SIMPLE_ID));
        for (int i = 0; i < 4; i++) deck.add(ElfPrototypeMod.makeID(DefendElf.SIMPLE_ID));
        deck.add(ElfPrototypeMod.makeID(CurseWhisper.SIMPLE_ID));
        deck.add(ElfPrototypeMod.makeID(SingleEdge.SIMPLE_ID));
        return deck;
    }

    @Override
    public ArrayList<String> getStartingRelics() {
        ArrayList<String> relics = new ArrayList<>();
        relics.add(CrackedSeal.ID);
        UnlockTracker.markRelicAsSeen(CrackedSeal.ID);
        return relics;
    }

    @Override
    public CharSelectInfo getLoadout() {
        CharacterStrings s = strings();
        return new CharSelectInfo(s.NAMES[0], s.TEXT[0], MAX_HP, MAX_HP, 0, 99, 5, this,
                getStartingRelics(), getStartingDeck(), false);
    }

    @Override
    public String getTitle(AbstractPlayer.PlayerClass playerClass) {
        return strings().NAMES[1];
    }

    @Override
    public AbstractCard.CardColor getCardColor() {
        return ElfEnums.ELF_GREEN;
    }

    @Override
    public Color getCardRenderColor() {
        return CARD_COLOR.cpy();
    }

    @Override
    public Color getCardTrailColor() {
        return CARD_COLOR.cpy();
    }

    @Override
    public Color getSlashAttackColor() {
        return CARD_COLOR.cpy();
    }

    @Override
    public AbstractCard getStartCardForEvent() {
        return new CurseWhisper();
    }

    @Override
    public int getAscensionMaxHPLoss() {
        return 4;
    }

    @Override
    public BitmapFont getEnergyNumFont() {
        return FontHelper.energyNumFontGreen;
    }

    @Override
    public void doCharSelectScreenSelectEffect() {
        CardCrawlGame.sound.playA("ATTACK_DAGGER_2", 1.25f);
        CardCrawlGame.screenShake.shake(ScreenShake.ShakeIntensity.LOW, ScreenShake.ShakeDur.SHORT, false);
    }

    @Override
    public String getCustomModeCharacterButtonSoundKey() {
        return "ATTACK_DAGGER_2";
    }

    @Override
    public String getLocalizedCharacterName() {
        return strings().NAMES[0];
    }

    @Override
    public AbstractPlayer newInstance() {
        return new ElfCharacter(name);
    }

    @Override
    public String getSpireHeartText() {
        return strings().TEXT[1];
    }

    @Override
    public AbstractGameAction.AttackEffect[] getSpireHeartSlashEffect() {
        return new AbstractGameAction.AttackEffect[]{AbstractGameAction.AttackEffect.SLASH_HEAVY,
                AbstractGameAction.AttackEffect.SLASH_DIAGONAL, AbstractGameAction.AttackEffect.SLASH_HORIZONTAL};
    }

    @Override
    public String getVampireText() {
        return strings().TEXT[2];
    }
}
