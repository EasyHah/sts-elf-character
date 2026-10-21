package elfprototype.anim;

import java.util.Collections;
import java.util.List;

/** Immutable description of one PNG frame clip, read from the pack's custom animation.json. */
public final class ClipData {
    public static final String END_HOLD = "hold";

    public static final class Event {
        public final String name;
        public final int frameIndex;
        public final float timeSeconds;

        public Event(String name, int frameIndex, float timeSeconds) {
            this.name = name;
            this.frameIndex = frameIndex;
            this.timeSeconds = timeSeconds;
        }
    }

    public final String action;
    public final int frameCount;
    /** Character form drawn by this clip ("elf", "intermediate", "dark"); a form change is crossfaded. */
    public final String form;
    public final float fps;
    public final boolean loop;
    /** "hold" = keep last frame, otherwise the action to switch to (normally "idle"). Ignored when loop. */
    public final String endAction;
    public final int canvasW;
    public final int canvasH;
    /** Foot pivot, top-left origin, in canvas pixels. */
    public final int pivotX;
    public final int pivotY;
    public final List<Event> events;
    /** Frame file paths relative to the clip directory, ordered by frame index. */
    public final List<String> frameFiles;

    public ClipData(String action, String form, int frameCount, float fps, boolean loop, String endAction,
                    int canvasW, int canvasH, int pivotX, int pivotY,
                    List<Event> events, List<String> frameFiles) {
        if (frameCount <= 0) throw new IllegalArgumentException(action + ": frameCount must be > 0");
        if (fps <= 0) throw new IllegalArgumentException(action + ": fps must be > 0");
        if (frameFiles.size() != frameCount) {
            throw new IllegalArgumentException(action + ": " + frameFiles.size() + " frame files for frameCount " + frameCount);
        }
        this.action = action;
        this.form = form == null ? "elf" : form;
        this.frameCount = frameCount;
        this.fps = fps;
        this.loop = loop;
        this.endAction = endAction == null ? "idle" : endAction;
        this.canvasW = canvasW;
        this.canvasH = canvasH;
        this.pivotX = pivotX;
        this.pivotY = pivotY;
        this.events = Collections.unmodifiableList(events);
        this.frameFiles = Collections.unmodifiableList(frameFiles);
    }

    public float duration() {
        return frameCount / fps;
    }

    public boolean holdsLastFrame() {
        return !loop && END_HOLD.equals(endAction);
    }
}
