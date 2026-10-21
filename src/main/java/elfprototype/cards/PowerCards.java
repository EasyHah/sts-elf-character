package elfprototype.cards;

import basemod.abstracts.CustomCard;
import com.megacrit.cardcrawl.actions.common.ApplyPowerAction;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.CardCrawlGame;
import com.megacrit.cardcrawl.localization.CardStrings;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.powers.AbstractPower;
import elfprototype.ElfPrototypeMod;
import elfprototype.character.ElfEnums;
import elfprototype.powers.AfterglowPower;
import elfprototype.powers.BacklashEchoPower;
import elfprototype.powers.BloodbladesPower;
import elfprototype.powers.ComposurePower;
import elfprototype.powers.CreepingCursePower;
import elfprototype.powers.CursedHeartPower;
import elfprototype.powers.EvernightPower;
import elfprototype.powers.LightOathPower;
import elfprototype.powers.MoonAwakeningPower;
import elfprototype.powers.RadiancePower;
import elfprototype.powers.RecollectionPower;
import elfprototype.powers.ResonancePower;
import elfprototype.powers.VigilPower;

import java.util.function.BiFunction;

/** Power cards (single face, cannot be eroded). Design v4 §4.2 / §4.3. */
public final class PowerCards {
    private PowerCards() {
    }

    /**
     * One power card: applies the power with amount = magicNumber.
     * Upgrade: magic +magicUp, or cost -> upCost (>= 0), or Innate (UPGRADE_DESCRIPTION shows it).
     */
    public abstract static class ElfPowerCard extends CustomCard {
        private final int magicUp;
        private final int upCost;
        private final boolean innateUp;
        private final BiFunction<AbstractPlayer, Integer, AbstractPower> power;

        protected ElfPowerCard(String simpleId, int cost, CardRarity rarity, int magic, int magicUp, int upCost,
                               boolean innateUp, BiFunction<AbstractPlayer, Integer, AbstractPower> power) {
            super(ElfPrototypeMod.makeID(simpleId), strings(simpleId).NAME,
                    ElfPrototypeMod.MOD_ID + "/images/cards/power/" + simpleId + ".png", cost,
                    strings(simpleId).DESCRIPTION, CardType.POWER, ElfEnums.ELF_GREEN, rarity, CardTarget.SELF);
            this.baseMagicNumber = this.magicNumber = magic;
            this.magicUp = magicUp;
            this.upCost = upCost;
            this.innateUp = innateUp;
            this.power = power;
        }

        private static CardStrings strings(String simpleId) {
            return CardCrawlGame.languagePack.getCardStrings(ElfPrototypeMod.makeID(simpleId));
        }

        @Override
        public void upgrade() {
            if (upgraded) return;
            upgradeName();
            if (magicUp != 0) upgradeMagicNumber(magicUp);
            if (upCost >= 0) upgradeBaseCost(upCost);
            if (innateUp) isInnate = true;
            CardStrings s = CardCrawlGame.languagePack.getCardStrings(cardID);
            if (s.UPGRADE_DESCRIPTION != null) {
                rawDescription = s.UPGRADE_DESCRIPTION;
                initializeDescription();
            }
        }

        @Override
        public void use(AbstractPlayer p, AbstractMonster m) {
            addToBot(new ApplyPowerAction(p, p, power.apply(p, magicNumber), magicNumber));
        }
    }

    // ------------------------------------------------------------------ uncommon

    public static class Resonance extends ElfPowerCard {
        public Resonance() {
            super("Resonance", 1, CardRarity.UNCOMMON, 1, 0, -1, true, ResonancePower::new);
        }
    }

    public static class Recollection extends ElfPowerCard {
        public Recollection() {
            super("Recollection", 1, CardRarity.UNCOMMON, 3, 2, -1, false, RecollectionPower::new);
        }
    }

    public static class Bloodblades extends ElfPowerCard {
        public Bloodblades() {
            super("Bloodblades", 1, CardRarity.UNCOMMON, 1, 1, -1, false, BloodbladesPower::new);
        }
    }

    public static class Radiance extends ElfPowerCard {
        public Radiance() {
            super("Radiance", 1, CardRarity.UNCOMMON, 1, 0, -1, true, RadiancePower::new);
        }
    }

    public static class BacklashEcho extends ElfPowerCard {
        public BacklashEcho() {
            super("BacklashEcho", 1, CardRarity.UNCOMMON, 2, 1, -1, false, BacklashEchoPower::new);
        }
    }

    public static class Composure extends ElfPowerCard {
        public Composure() {
            super("Composure", 1, CardRarity.UNCOMMON, 3, 1, -1, false, ComposurePower::new);
        }
    }

    public static class CreepingCurse extends ElfPowerCard {
        public CreepingCurse() {
            super("CreepingCurse", 1, CardRarity.UNCOMMON, 1, 1, -1, false, CreepingCursePower::new);
        }
    }

    public static class Vigil extends ElfPowerCard {
        public Vigil() {
            super("Vigil", 1, CardRarity.UNCOMMON, 3, 2, -1, false, VigilPower::new);
        }
    }

    // ------------------------------------------------------------------ rare

    public static class MoonAwakening extends ElfPowerCard {
        public MoonAwakening() {
            super("MoonAwakening", 3, CardRarity.RARE, 1, 0, 2, false, MoonAwakeningPower::new);
        }
    }

    public static class Afterglow extends ElfPowerCard {
        public Afterglow() {
            super("Afterglow", 2, CardRarity.RARE, 1, 0, 1, false, AfterglowPower::new);
        }
    }

    public static class Evernight extends ElfPowerCard {
        public Evernight() {
            super("Evernight", 3, CardRarity.RARE, 1, 0, 2, false, EvernightPower::new);
        }
    }

    public static class CursedHeart extends ElfPowerCard {
        public CursedHeart() {
            super("CursedHeart", 1, CardRarity.RARE, 1, 0, -1, true, CursedHeartPower::new);
        }
    }

    public static class LightOath extends ElfPowerCard {
        public LightOath() {
            super("LightOath", 2, CardRarity.RARE, 1, 0, 1, false, LightOathPower::new);
        }
    }
}
