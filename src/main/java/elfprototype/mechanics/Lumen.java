package elfprototype.mechanics;

import com.badlogic.gdx.graphics.Color;
import com.megacrit.cardcrawl.actions.GameActionManager;
import com.megacrit.cardcrawl.actions.common.LoseHPAction;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.powers.AbstractPower;
import com.megacrit.cardcrawl.relics.AbstractRelic;
import elfprototype.cards.DualFaceCard;
import elfprototype.powers.EvernightPower;
import elfprototype.powers.NoBacklashPower;

import java.util.ArrayList;
import java.util.List;

/**
 * Core rules of design v4: erode (light -> cursed), seal (cursed -> light), chime (flips this turn), backlash.
 * Every flip goes through here so chime, listeners and card numbers stay in sync. Call only from inside actions.
 */
public final class Lumen {
    public static final Color CURSE_COLOR = new Color(0.62f, 0.25f, 0.95f, 1f);
    public static final Color SEAL_COLOR = new Color(1f, 0.85f, 0.35f, 1f);
    /** HP lost per cursed face left in hand at end of turn. */
    public static final int BACKLASH = 2;

    private static int playedTurn = -1;
    private static boolean playedLight;
    private static boolean playedCursed;

    private Lumen() {
    }

    /** Chime = number of flips (erode, seal, auto seal) this turn, plus direct chime gains. */
    public static int chime() {
        AbstractPlayer p = AbstractDungeon.player;
        if (p == null) return 0;
        AbstractPower pw = p.getPower(ChimePower.POWER_ID);
        return pw == null ? 0 : pw.amount;
    }

    public static boolean canErode(AbstractCard c) {
        return c instanceof DualFaceCard && !((DualFaceCard) c).isCursed();
    }

    public static boolean canSeal(AbstractCard c) {
        return c instanceof DualFaceCard && ((DualFaceCard) c).isCursed();
    }

    public static boolean isCursed(AbstractCard c) {
        return canSeal(c);
    }

    public static boolean isCursedAttack(AbstractCard c) {
        return isCursed(c) && c.type == AbstractCard.CardType.ATTACK;
    }

    public static boolean isLight(AbstractCard c) {
        return canErode(c);
    }

    public static void erode(AbstractCard c) {
        if (!canErode(c)) return;
        ((DualFaceCard) c).setCursed(true);
        c.superFlash(CURSE_COLOR.cpy());
        addChime(1);
        for (SealListener l : listeners()) l.onErode(c);
    }

    public static void seal(AbstractCard c, boolean auto) {
        if (!canSeal(c)) return;
        ((DualFaceCard) c).setCursed(false);
        if (!auto) c.superFlash(SEAL_COLOR.cpy());
        addChime(1);
        for (SealListener l : listeners()) l.onSeal(c, auto);
    }

    /** Erodes the leftmost light cards in hand (CreepingCurse, DuskGauntlet, CursedCrown). Returns how many. */
    public static int erodeLeftmost(int n) {
        int done = 0;
        for (AbstractCard c : new ArrayList<>(AbstractDungeon.player.hand.group)) {
            if (done >= n) break;
            if (canErode(c)) {
                erode(c);
                done++;
            }
        }
        return done;
    }

    /** Seals every cursed card in hand. Returns how many were sealed. */
    public static int sealAllInHand() {
        int n = 0;
        for (AbstractCard c : new ArrayList<>(AbstractDungeon.player.hand.group)) {
            if (canSeal(c)) {
                seal(c, false);
                n++;
            }
        }
        return n;
    }

    /** Erodes every light card in hand. Returns how many were eroded. */
    public static int erodeAllInHand() {
        int n = 0;
        for (AbstractCard c : new ArrayList<>(AbstractDungeon.player.hand.group)) {
            if (canErode(c)) {
                erode(c);
                n++;
            }
        }
        return n;
    }

    public static int countInHand(boolean cursed, AbstractCard except) {
        int n = 0;
        for (AbstractCard c : AbstractDungeon.player.hand.group) {
            if (c != except && (cursed ? canSeal(c) : canErode(c))) n++;
        }
        return n;
    }

    /**
     * Called from DualFaceCard.use() for a cursed face: it turns light again after it resolves and then goes to the
     * discard pile like any other card (rule changed 2026-10-23: it used to return to hand, which let two cards that
     * erode each other loop for free).
     */
    public static void queueAutoSeal(DualFaceCard c) {
        AbstractDungeon.actionManager.addToBottom(new SealSelfAction(c));
    }

    /** Raises chime by n right away (flips, Avatar, Overflow, Execution). */
    public static void addChime(int n) {
        if (n <= 0) return;
        AbstractPlayer p = AbstractDungeon.player;
        int before = chime();
        AbstractPower pw = p.getPower(ChimePower.POWER_ID);
        if (pw == null) {
            pw = new ChimePower(p, n);
            p.powers.add(pw);
            pw.onInitialApplication();
        } else {
            pw.stackPower(n);
        }
        pw.flashWithoutSound();
        AbstractDungeon.onModifyPower(); // re-applies powers to the hand so card numbers update now
        for (SealListener l : listeners()) l.onChimeChanged(before, before + n);
    }

    /** Extra hits for cursed attacks (Evernight). */
    public static int extraHits(AbstractCard c) {
        if (!isCursedAttack(c)) return 0;
        AbstractPower pw = AbstractDungeon.player == null ? null : AbstractDungeon.player.getPower(EvernightPower.POWER_ID);
        return pw == null ? 0 : pw.amount;
    }

    /** Backlash for one cursed face left in hand at end of turn. */
    public static void backlash() {
        AbstractPlayer p = AbstractDungeon.player;
        if (p.hasPower(NoBacklashPower.POWER_ID)) return;
        AbstractDungeon.actionManager.addToTop(new LoseHPAction(p, p, BACKLASH));
        for (SealListener l : listeners()) l.onBacklash(BACKLASH);
    }

    /** Remembers which faces were played this turn (Chain). */
    public static void notePlayed(boolean cursed) {
        syncTurn();
        if (cursed) playedCursed = true;
        else playedLight = true;
    }

    public static boolean playedThisTurn(boolean cursed) {
        syncTurn();
        return cursed ? playedCursed : playedLight;
    }

    private static void syncTurn() {
        if (playedTurn != GameActionManager.turn) {
            playedTurn = GameActionManager.turn;
            playedLight = playedCursed = false;
        }
    }

    private static List<SealListener> listeners() {
        List<SealListener> out = new ArrayList<>();
        AbstractPlayer p = AbstractDungeon.player;
        for (AbstractRelic r : p.relics) if (r instanceof SealListener) out.add((SealListener) r);
        for (AbstractPower pw : new ArrayList<>(p.powers)) if (pw instanceof SealListener) out.add((SealListener) pw);
        return out;
    }
}
