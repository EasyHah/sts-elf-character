package elfprototype.cards;

import com.megacrit.cardcrawl.cards.AbstractCard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/** Every elf card (starter + pool), for registration. Order = design doc order. */
public final class ElfCards {
    private ElfCards() {
    }

    public static List<AbstractCard> all() {
        return new ArrayList<>(Arrays.asList(
                // starter
                new StrikeElf(), new DefendElf(), new CurseWhisper(), new SingleEdge(),
                // common
                new CommonCards.ThicketArrow(), new CommonCards.QuickArrow(), new CommonCards.ScatterShot(),
                new CommonCards.SnareShot(), new CommonCards.ChargedShot(), new CommonCards.CorruptingEdge(),
                new CommonCards.GladeThrust(), new CommonCards.Riposte(), new CommonCards.EmberShot(),
                new CommonCards.Recoil(), new CommonCards.LeafWard(), new CommonCards.HoldBreath(),
                new CommonCards.SealingRite(), new CommonCards.Invite(), new CommonCards.Moonstep(),
                new CommonCards.PurifyingLight(), new CommonCards.CurseWard(), new CommonCards.Premonition(),
                new CommonCards.Nock(), new CommonCards.Unbind(),
                // uncommon
                new PowerCards.Resonance(), new PowerCards.Recollection(), new PowerCards.Bloodblades(),
                new PowerCards.Radiance(), new PowerCards.BacklashEcho(), new PowerCards.Composure(),
                new PowerCards.CreepingCurse(), new PowerCards.Vigil(),
                new UncommonCards.TwinMoon(), new UncommonCards.Skim(), new UncommonCards.Heartseeker(),
                new UncommonCards.Harvest(), new UncommonCards.Volley(), new UncommonCards.MirrorEdge(),
                new UncommonCards.Leech(), new UncommonCards.Chain(), new UncommonCards.Sunder(),
                new UncommonCards.Shatter(), new UncommonCards.Pursuit(), new UncommonCards.Release(),
                new UncommonCards.DarkPact(), new UncommonCards.Spread(), new UncommonCards.Mirror(),
                new UncommonCards.CurseBarrier(), new UncommonCards.Rewind(), new UncommonCards.ForestBreath(),
                new UncommonCards.Turnabout(), new UncommonCards.Insight(), new UncommonCards.Offering(),
                new UncommonCards.BowGuard(), new UncommonCards.Bind(), new UncommonCards.Steady(),
                // rare
                new PowerCards.MoonAwakening(), new PowerCards.Afterglow(), new PowerCards.Evernight(),
                new PowerCards.CursedHeart(), new PowerCards.LightOath(),
                new RareCards.ThousandBlades(), new RareCards.Starfall(), new RareCards.Devour(),
                new RareCards.Echo(), new RareCards.Execution(),
                new RareCards.TwinSoul(), new RareCards.Overflow(), new RareCards.TimeCurse(),
                new RareCards.GreatSeal(), new RareCards.Avatar(), new RareCards.WorldtreeVow()));
    }
}
