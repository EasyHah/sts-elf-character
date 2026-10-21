package elfprototype.anim;

import java.util.Map;

/**
 * Pure (no GL) frame-clip state machine. Time advances only by the delta passed to {@link #update(float)},
 * so playback speed follows the clip fps regardless of display refresh rate.
 *
 * Policy:
 * - Any non-death request interrupts the current clip and restarts the requested one from frame 0
 *   (consecutive hits restart hit; attack during hit replaces it and vice versa).
 * - Non-looping clips with end_action "hold" keep their last frame; others switch to end_action (idle).
 * - A REAL_DEATH request locks the animator on death; only {@link #reset()} / {@link #onRevived()} unlock it.
 *   Debug previews of death do not lock and can be interrupted by normal requests.
 * - Each clip event fires at most once per play, even if one update skips past several frames.
 * - "idle" resolves to the cursed (intermediate form) idle while {@link #setCursedIdle(boolean)} is on,
 *   both when requested directly and when a clip ends with end_action "idle".
 */
public final class ElfAnimator {
    public static final String IDLE = "idle";
    public static final String CURSED_IDLE = "idle_cursed";
    public static final String DEATH = "death";
    private static final int MAX_TRANSITIONS_PER_UPDATE = 8;

    public enum Source { GAME, DEBUG, REAL_DEATH }

    public interface Listener {
        void onClipEvent(ClipData clip, ClipData.Event event);
    }

    private final Map<String, ClipData> clips;
    private Listener listener;

    private ClipData current;
    private float time;
    private boolean[] fired;
    private boolean deathLocked;
    private boolean cursedIdle;
    private long playCount;

    public ElfAnimator(Map<String, ClipData> clips) {
        if (!clips.containsKey(IDLE)) throw new IllegalArgumentException("idle clip is required");
        this.clips = clips;
        start(clips.get(IDLE));
    }

    public void setListener(Listener listener) {
        this.listener = listener;
    }

    /** Back to looping idle; clears the death lock. Used for new combat, room change, new run, revive. */
    public void reset() {
        deathLocked = false;
        start(idleClip());
    }

    /**
     * Cursed cards in hand -> intermediate idle. Switches immediately if an idle is showing;
     * a running action clip finishes first and then returns to the matching idle.
     */
    public void setCursedIdle(boolean on) {
        if (cursedIdle == on) return;
        cursedIdle = on;
        if (!deathLocked && isIdle()) start(idleClip());
    }

    public boolean isCursedIdle() {
        return cursedIdle;
    }

    private boolean isIdle() {
        return IDLE.equals(current.action) || CURSED_IDLE.equals(current.action);
    }

    private ClipData idleClip() {
        ClipData c = cursedIdle ? clips.get(CURSED_IDLE) : null;
        return c != null ? c : clips.get(IDLE);
    }

    private ClipData resolve(String action) {
        return IDLE.equals(action) ? idleClip() : clips.get(action);
    }

    /** @return true if the clip was started. */
    public boolean play(String action, Source source) {
        ClipData c = resolve(action);
        if (c == null) return false;
        if (deathLocked && source != Source.REAL_DEATH) return false;
        if (source == Source.REAL_DEATH) {
            if (deathLocked) return false; // final death triggers once
            deathLocked = true;
        }
        start(c);
        return true;
    }

    /** Call once the game has finally decided the player is dead (after revive checks). */
    public boolean onRealDeath() {
        return play(DEATH, Source.REAL_DEATH);
    }

    /** Call if the player is no longer dead after a death lock (revive / new run). */
    public void onRevived() {
        if (deathLocked) reset();
    }

    public void update(float dt) {
        if (!(dt > 0f)) { // also rejects NaN
            fireDue();
            return;
        }
        time += dt;
        for (int guard = 0; guard < MAX_TRANSITIONS_PER_UPDATE; guard++) {
            fireDue();
            float dur = current.duration();
            if (time < dur) return;
            if (current.loop) {
                time -= dur;
                fired = new boolean[current.events.size()];
                continue;
            }
            if (current.holdsLastFrame()) {
                time = dur;
                return;
            }
            float leftover = time - dur;
            ClipData next = resolve(current.endAction);
            start(next != null ? next : idleClip());
            time = leftover;
        }
        // Pathological dt: settle at the start of whatever clip we ended on.
        time = 0f;
    }

    private void start(ClipData c) {
        current = c;
        time = 0f;
        fired = new boolean[c.events.size()];
        playCount++;
    }

    private void fireDue() {
        for (int i = 0; i < fired.length; i++) {
            ClipData.Event e = current.events.get(i);
            if (!fired[i] && time + 1e-6f >= e.timeSeconds) {
                fired[i] = true;
                if (listener != null) listener.onClipEvent(current, e);
            }
        }
    }

    public int frameIndex() {
        int f = (int) Math.floor(time * current.fps + 1e-4f);
        if (current.loop) f %= current.frameCount;
        return Math.max(0, Math.min(f, current.frameCount - 1));
    }

    public ClipData current() {
        return current;
    }

    public String currentAction() {
        return current.action;
    }

    public boolean isDeathLocked() {
        return deathLocked;
    }

    public boolean isHoldingLastFrame() {
        return current.holdsLastFrame() && time >= current.duration();
    }

    public float time() {
        return time;
    }

    /** Increments on every clip start; lets tests/logs detect restarts. */
    public long playCount() {
        return playCount;
    }

    public Map<String, ClipData> clips() {
        return clips;
    }
}
