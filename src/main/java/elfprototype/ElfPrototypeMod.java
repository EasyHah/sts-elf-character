package elfprototype;

import basemod.BaseMod;
import basemod.ModLabeledToggleButton;
import basemod.ModPanel;
import basemod.devcommands.ConsoleCommand;
import basemod.interfaces.EditCardsSubscriber;
import basemod.interfaces.EditCharactersSubscriber;
import basemod.interfaces.EditKeywordsSubscriber;
import basemod.interfaces.EditRelicsSubscriber;
import basemod.interfaces.EditStringsSubscriber;
import basemod.interfaces.OnStartBattleSubscriber;
import basemod.interfaces.PostInitializeSubscriber;
import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Texture;
import com.google.gson.Gson;
import com.evacipated.cardcrawl.modthespire.lib.SpireInitializer;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.localization.CharacterStrings;
import com.megacrit.cardcrawl.localization.PowerStrings;
import com.megacrit.cardcrawl.localization.RelicStrings;
import com.megacrit.cardcrawl.localization.UIStrings;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import com.megacrit.cardcrawl.unlock.UnlockTracker;
import elfprototype.cards.ElfCards;
import elfprototype.character.ElfCharacter;
import elfprototype.character.ElfEnums;
import elfprototype.mechanics.ChimePower;
import elfprototype.relics.CrackedSeal;
import elfprototype.relics.ElfRelics;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import elfprototype.skin.ElfSkin;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.nio.charset.StandardCharsets;

@SpireInitializer
public class ElfPrototypeMod implements PostInitializeSubscriber, OnStartBattleSubscriber, EditCharactersSubscriber,
        EditCardsSubscriber, EditRelicsSubscriber, EditStringsSubscriber, EditKeywordsSubscriber {
    public static final String MOD_ID = "elfprototype";
    public static final Logger logger = LogManager.getLogger("ElfPrototype");
    private static final String CARDBACK = MOD_ID + "/images/character/cardback/";

    public static String makeID(String id) {
        return MOD_ID + ":" + id;
    }

    public static void initialize() {
        new ElfPrototypeMod();
    }

    public ElfPrototypeMod() {
        BaseMod.subscribe(this);
        ElfConfig.load();
        BaseMod.addColor(ElfEnums.ELF_GREEN, ElfCharacter.CARD_COLOR,
                CARDBACK + "bg_attack.png", CARDBACK + "bg_skill.png", CARDBACK + "bg_power.png",
                CARDBACK + "energy_orb.png",
                CARDBACK + "bg_attack_p.png", CARDBACK + "bg_skill_p.png", CARDBACK + "bg_power_p.png",
                CARDBACK + "energy_orb_p.png", CARDBACK + "small_orb.png");
        logger.info("[ElfPrototype] init: test skin enabled=" + ElfConfig.enabled() + " target=" + ElfConfig.target()
                + " scale=" + ElfConfig.scale());
    }

    private static String lang() {
        return Settings.language == Settings.GameLanguage.ZHS ? "zhs" : "eng";
    }

    private static String strings(String file) {
        return MOD_ID + "/localization/" + lang() + "/" + file;
    }

    @Override
    public void receiveEditStrings() {
        BaseMod.loadCustomStringsFile(CharacterStrings.class, strings("CharacterStrings.json"));
        BaseMod.loadCustomStringsFile(CardStrings.class, strings("CardStrings.json"));
        BaseMod.loadCustomStringsFile(RelicStrings.class, strings("RelicStrings.json"));
        BaseMod.loadCustomStringsFile(PowerStrings.class, strings("PowerStrings.json"));
        BaseMod.loadCustomStringsFile(UIStrings.class, strings("UIStrings.json"));
    }

    private static final class Keyword {
        String PROPER_NAME;
        String[] NAMES;
        String DESCRIPTION;
    }

    @Override
    public void receiveEditKeywords() {
        String json = Gdx.files.internal(strings("Keywords.json")).readString(StandardCharsets.UTF_8.name());
        for (Keyword k : new Gson().fromJson(json, Keyword[].class)) {
            BaseMod.addKeyword(k.PROPER_NAME, k.NAMES, k.DESCRIPTION);
        }
    }

    @Override
    public void receiveEditCharacters() {
        BaseMod.addCharacter(new ElfCharacter(ElfCharacter.ID), ElfCharacter.BUTTON, ElfCharacter.PORTRAIT,
                ElfEnums.ELF);
    }

    @Override
    public void receiveEditCards() {
        for (AbstractCard c : ElfCards.all()) {
            BaseMod.addCard(c);
            UnlockTracker.unlockCard(c.cardID);
        }
    }

    @Override
    public void receiveEditRelics() {
        BaseMod.addRelicToCustomPool(new CrackedSeal(), ElfEnums.ELF_GREEN);
        for (AbstractRelic r : new AbstractRelic[]{new ElfRelics.WholeSeal(), new ElfRelics.DuskGauntlet(),
                new ElfRelics.SilverleafBrooch(), new ElfRelics.MirrorEarring(), new ElfRelics.MossCharm(),
                new ElfRelics.WorldtreeBlessing(), new ElfRelics.EclipseStone(), new ElfRelics.Whetstone(),
                new ElfRelics.CursedCrown()}) {
            BaseMod.addRelicToCustomPool(r, ElfEnums.ELF_GREEN);
            UnlockTracker.markRelicAsSeen(r.relicId);
        }
    }

    @Override
    public void receivePostInitialize() {
        ConsoleCommand.addCommand("elf", ElfCommand.class);
        BaseMod.addPower(ChimePower.class, ChimePower.POWER_ID);

        ModPanel panel = new ModPanel();
        panel.addUIElement(new ModLabeledToggleButton("Test skin on a vanilla character (target: " + ElfConfig.target() + ")",
                350f, 700f, Settings.CREAM_COLOR, FontHelper.charDescFont, ElfConfig.enabled(), panel,
                l -> { }, b -> ElfConfig.setBool(ElfConfig.ENABLED, b.enabled)));
        panel.addUIElement(new ModLabeledToggleButton("Debug overlay (action / frame / foot pivot)",
                350f, 650f, Settings.CREAM_COLOR, FontHelper.charDescFont, ElfConfig.debugOverlay(), panel,
                l -> { }, b -> ElfConfig.setBool(ElfConfig.DEBUG_OVERLAY, b.enabled)));
        Texture badge = ImageMaster.loadImage(MOD_ID + "/images/badge.png");
        BaseMod.registerModBadge(badge, "Elf Prototype", "ElfPrototype",
                "Elf character prototype (design v4: light / cursed faces, chime). Keeps the vanilla test skin.", panel);

        ElfSkin.ensureLoaded(); // preload once on the GL thread
    }

    @Override
    public void receiveOnBattleStart(AbstractRoom room) {
        ElfSkin.onBattleStart();
    }
}
