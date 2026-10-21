package elfprototype.skin;

import com.badlogic.gdx.Gdx;
import com.badlogic.gdx.graphics.Color;
import com.badlogic.gdx.graphics.GL20;
import com.badlogic.gdx.graphics.Texture;
import com.badlogic.gdx.graphics.g2d.SpriteBatch;
import com.megacrit.cardcrawl.cards.AbstractCard;
import com.megacrit.cardcrawl.characters.AbstractPlayer;
import com.megacrit.cardcrawl.core.Settings;
import com.megacrit.cardcrawl.dungeons.AbstractDungeon;
import com.megacrit.cardcrawl.helpers.FontHelper;
import com.megacrit.cardcrawl.helpers.ImageMaster;
import com.megacrit.cardcrawl.monsters.AbstractMonster;
import com.megacrit.cardcrawl.rooms.AbstractRoom;
import elfprototype.ElfConfig;
import elfprototype.ElfPrototypeMod;
import elfprototype.anim.ClipData;
import elfprototype.anim.ClipJson;
import elfprototype.anim.ElfAnimator;
import elfprototype.cards.DualFaceCard;
import elfprototype.character.ElfEnums;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Game-side facade: decides whether the elf skin applies to a player, keeps animation state in sync
 * with real game state, and draws full frames anchored at the foot pivot (canvas size per clip).
 * All texture work happens on the GL thread (called from render / postInitialize).
 */
public final class ElfSkin {
    public static final String[] ACTIONS = {"idle", "attack", "cast", "guard", "hit", "death", "victory",
            ElfAnimator.CURSED_IDLE, "attack_cursed"};
    private static final String ANIM_ROOT = "elfprototype/images/character/animations/";
    /** Old frame fades out over this long when the drawn form changes (elf / intermediate / dark). */
    private static final float FORM_FADE_SECONDS = 0.125f;
    public static final String ARROW_IMG = "elfprototype/images/effects/arrow.png";
    /** Arrow spawn point relative to the foot pivot, in canvas px (x right, y up); from attack frame 4's bow/arrow layer. */
    private static final float ARROW_DX = 150f;
    private static final float ARROW_DY_UP = 228f;

    private static ElfAnimator animator;
    private static final Map<String, Texture[]> textures = new LinkedHashMap<>();
    private static boolean loadFailed;
    private static int texturesCreated;

    private static AbstractPlayer syncedPlayer;
    private static AbstractRoom syncedRoom;
    private static long lastUpdateFrame = -1;
    private static AbstractMonster pendingArrowTarget;
    private static final Deque<Integer> hpBeforeDamage = new ArrayDeque<>();

    private static ClipData lastClip;
    private static Texture lastTex;
    private static ClipData fadeClip;
    private static Texture fadeTex;
    private static float fadeLeft;

    private ElfSkin() {
    }

    /** elf/<action>/ for the base form; the cursed variants live under their own form folder. */
    private static String clipDir(String action) {
        if (ElfAnimator.CURSED_IDLE.equals(action)) return ANIM_ROOT + "intermediate/idle/";
        if ("attack_cursed".equals(action)) return ANIM_ROOT + "dark/attack/";
        return ANIM_ROOT + "elf/" + action + "/";
    }

    // ---------------------------------------------------------------- loading

    /** Loads clip metadata and all frame textures once per process. Safe to call repeatedly. */
    public static boolean ensureLoaded() {
        if (animator != null) return true;
        if (loadFailed) return false;
        long t0 = System.currentTimeMillis();
        Map<String, ClipData> clips = new LinkedHashMap<>();
        try {
            for (String a : ACTIONS) {
                String dir = clipDir(a);
                String json = Gdx.files.internal(dir + "animation.json").readString("UTF-8");
                ClipData c = ClipJson.parse(json);
                if (!a.equals(c.action)) throw new IllegalStateException(dir + "animation.json declares action " + c.action);
                Texture[] frames = new Texture[c.frameCount];
                for (int i = 0; i < c.frameCount; i++) {
                    Texture t = new Texture(Gdx.files.internal(dir + c.frameFiles.get(i)));
                    t.setFilter(Texture.TextureFilter.Linear, Texture.TextureFilter.Linear);
                    if (t.getWidth() != c.canvasW || t.getHeight() != c.canvasH) {
                        throw new IllegalStateException(a + " frame " + i + " is " + t.getWidth() + "x" + t.getHeight()
                                + ", metadata canvas is " + c.canvasW + "x" + c.canvasH);
                    }
                    frames[i] = t;
                    texturesCreated++;
                }
                textures.put(a, frames);
                clips.put(a, c);
            }
        } catch (Exception e) {
            ElfPrototypeMod.logger.error("[ElfPrototype] loading frames failed; skin disabled, vanilla rendering kept", e);
            for (Texture[] arr : textures.values()) for (Texture t : arr) if (t != null) t.dispose();
            textures.clear();
            loadFailed = true;
            return false;
        }
        animator = new ElfAnimator(clips);
        animator.setListener(ElfSkin::onClipEvent);
        int total = 0;
        long bytes = 0;
        StringBuilder sb = new StringBuilder();
        for (ClipData c : clips.values()) {
            total += c.frameCount;
            bytes += (long) c.frameCount * c.canvasW * c.canvasH * 4L;
            sb.append(' ').append(c.action).append('=').append(c.frameCount).append('@').append(c.fps)
                    .append(c.loop ? "/loop" : "/" + c.endAction);
        }
        ElfPrototypeMod.logger.info("[ElfPrototype] loaded " + total + " frames (" + texturesCreated
                + " textures created, ~" + (bytes / (1024 * 1024)) + " MiB RGBA) in "
                + (System.currentTimeMillis() - t0) + " ms:" + sb);
        return true;
    }

    // ---------------------------------------------------------------- applicability

    /** The elf character always; a vanilla character only as the switchable test skin. */
    public static boolean appliesTo(AbstractPlayer p) {
        if (p == null || p != AbstractDungeon.player || p.chosenClass == null) return false;
        if (p.chosenClass == ElfEnums.ELF) return ensureLoaded();
        return ElfConfig.enabled() && p.chosenClass.name().equalsIgnoreCase(ElfConfig.target()) && ensureLoaded();
    }

    // ---------------------------------------------------------------- state sync

    /** Mirrors real game state into the animator once per rendered frame. */
    private static void sync(AbstractPlayer p) {
        AbstractRoom room = AbstractDungeon.getCurrRoom();
        if (p != syncedPlayer) {
            syncedPlayer = p;
            syncedRoom = room;
            animator.setCursedIdle(false);
            animator.reset();
            log("reset: new player instance (" + p.chosenClass + ")");
        } else if (room != syncedRoom) {
            syncedRoom = room;
            if (!p.isDead) {
                animator.setCursedIdle(false); // hand is empty between rooms
                animator.reset();
                log("reset: room changed");
            }
        }
        if (p.chosenClass == ElfEnums.ELF && room != null && room.phase == AbstractRoom.RoomPhase.COMBAT) {
            boolean cursedInHand = false;
            for (AbstractCard c : p.hand.group) {
                if (c instanceof DualFaceCard && ((DualFaceCard) c).isCursed()) {
                    cursedInHand = true;
                    break;
                }
            }
            setCursedInHand(cursedInHand);
        }
        if (p.isDead && !animator.isDeathLocked()) {
            // isDead is only set after Fairy in a Bottle / Lizard Tail / Mark of the Bloom checks fail.
            if (animator.onRealDeath()) log("REAL death -> death clip (locked, holds last frame)");
        } else if (!p.isDead && animator.isDeathLocked()) {
            animator.onRevived();
            log("player no longer dead -> death lock cleared, idle");
        }
    }

    public static void onBattleStart() {
        AbstractPlayer p = AbstractDungeon.player;
        if (!appliesTo(p)) return;
        syncedPlayer = p;
        syncedRoom = AbstractDungeon.getCurrRoom();
        animator.setCursedIdle(false);
        animator.reset();
        log("reset: battle start");
    }

    /**
     * For the card layer: true while any cursed-face card sits in hand (backlash risk), which shows the
     * intermediate-form idle. Call whenever the hand changes; repeated calls with the same value are free.
     */
    public static void setCursedInHand(boolean on) {
        if (!ensureLoaded() || animator.isCursedIdle() == on) return;
        animator.setCursedIdle(on);
        log("cursed in hand = " + on + " -> " + animator.currentAction());
    }

    public static boolean isCursedInHand() {
        return animator != null && animator.isCursedIdle();
    }

    /** For the card layer: a cursed face was played; draws the dark-form dual-blade attack (no arrow). */
    public static void onCursedFacePlayed(AbstractPlayer p, AbstractCard c) {
        if (!appliesTo(p)) return;
        trigger("attack_cursed", "cursed face " + c.cardID);
    }

    // ---------------------------------------------------------------- game triggers (from patches)

    public static void onCardUsed(AbstractPlayer p, AbstractCard c, AbstractMonster m) {
        if (!appliesTo(p)) return;
        String action;
        if (c.type == AbstractCard.CardType.ATTACK) {
            // useCard runs before the face seals itself, so a cursed attack still reads as cursed here.
            action = c instanceof DualFaceCard && ((DualFaceCard) c).isCursed() ? "attack_cursed" : "attack";
        } else if (c.type == AbstractCard.CardType.SKILL && c.baseBlock > 0) {
            action = "guard";
        } else if (c.type == AbstractCard.CardType.SKILL || c.type == AbstractCard.CardType.POWER) {
            action = "cast";
        } else {
            return; // status / curse: no visual
        }
        pendingArrowTarget = m;
        trigger(action, "card " + c.cardID + " (" + c.type + ")");
    }

    public static void beforeDamage(AbstractPlayer p) {
        hpBeforeDamage.push(p.currentHealth);
    }

    public static void afterDamage(AbstractPlayer p) {
        Integer before = hpBeforeDamage.poll();
        if (before == null || !appliesTo(p)) return;
        if (p.currentHealth < before && !p.isDead) {
            trigger("hit", "hp " + before + " -> " + p.currentHealth);
        }
        // Death is not triggered here: sync() polls isDead so every death path is covered exactly once.
    }

    public static void onVictory(AbstractPlayer p) {
        if (!appliesTo(p) || p.isDead) return;
        trigger("victory", "onVictory");
    }

    private static void trigger(String action, String why) {
        boolean ok = animator.play(action, ElfAnimator.Source.GAME);
        log((ok ? "play " : "ignored (death locked) ") + action + " <- " + why);
    }

    /** Debug console entry. Death preview here does NOT lock and is not the real-death path. */
    public static String debugPlay(String action) {
        if (!ensureLoaded()) return "frames not loaded (see log)";
        if (!animator.clips().containsKey(action)) return "unknown action " + action;
        boolean ok = animator.play(action, ElfAnimator.Source.DEBUG);
        log("debug play " + action + (ok ? "" : " ignored: real death lock active"));
        return ok ? "playing " + action : "ignored: real death lock active (use 'elf reset')";
    }

    public static void debugReset() {
        if (ensureLoaded()) {
            animator.reset();
            log("debug reset -> idle");
        }
    }

    public static String status() {
        if (animator == null) return "not loaded; enabled=" + ElfConfig.enabled() + " target=" + ElfConfig.target();
        return "enabled=" + ElfConfig.enabled() + " target=" + ElfConfig.target() + " scale=" + ElfConfig.scale()
                + " action=" + animator.currentAction() + " frame=" + animator.frameIndex() + "/" + animator.current().frameCount
                + " deathLocked=" + animator.isDeathLocked() + " cursedInHand=" + animator.isCursedIdle()
                + " textures=" + texturesCreated
                + " appliesNow=" + (AbstractDungeon.player != null && appliesTo(AbstractDungeon.player));
    }

    private static void onClipEvent(ClipData clip, ClipData.Event e) {
        log("event " + clip.action + "." + e.name + " @frame " + e.frameIndex);
        if ("attack".equals(clip.action) && "visual_hit".equals(e.name) && ElfConfig.arrows()) {
            spawnArrow();
        }
    }

    // ---------------------------------------------------------------- rendering

    /** Replaces the vanilla body draw (skeleton or corpse image) for the target player. */
    public static void render(AbstractPlayer p, SpriteBatch sb) {
        long frame = Gdx.graphics.getFrameId();
        ClipData c;
        Texture tex;
        if (frame != lastUpdateFrame) { // advance at most once per rendered frame
            lastUpdateFrame = frame;
            float dt = Gdx.graphics.getDeltaTime();
            sync(p);
            animator.update(dt);
            fadeLeft = Math.max(0f, fadeLeft - dt);
            c = animator.current();
            tex = textures.get(c.action)[animator.frameIndex()];
            if (lastClip != null && !lastClip.form.equals(c.form)) {
                fadeClip = lastClip;
                fadeTex = lastTex;
                fadeLeft = FORM_FADE_SECONDS;
            }
            lastClip = c;
            lastTex = tex;
        } else {
            c = lastClip;
            tex = lastTex;
        }

        float s = Settings.scale * ElfConfig.scale();
        float footX = p.drawX + p.animX;
        float footY = p.drawY + p.animY;
        boolean flip = p.flipHorizontal;
        Color base = p.tint != null ? p.tint.color : Color.WHITE;

        Color prevColor = sb.getColor().cpy();
        int prevSrc = sb.getBlendSrcFunc();
        int prevDst = sb.getBlendDstFunc();
        sb.setBlendFunction(GL20.GL_SRC_ALPHA, GL20.GL_ONE_MINUS_SRC_ALPHA); // straight alpha
        drawFrame(sb, c, tex, footX, footY, s, flip, base, 1f);
        if (fadeLeft > 0f && fadeTex != null) { // previous form dissolves on top of the new one
            drawFrame(sb, fadeClip, fadeTex, footX, footY, s, flip, base, fadeLeft / FORM_FADE_SECONDS);
        }

        if (ElfConfig.debugOverlay()) {
            float pivotX = flip ? c.canvasW - c.pivotX : c.pivotX;
            renderOverlay(sb, c, footX, footY, footX - pivotX * s, footY - (c.canvasH - c.pivotY) * s, s);
        }

        sb.setBlendFunction(prevSrc, prevDst);
        sb.setColor(prevColor);
    }

    private static void drawFrame(SpriteBatch sb, ClipData c, Texture tex, float footX, float footY, float s,
                                  boolean flip, Color base, float alpha) {
        float pivotX = flip ? c.canvasW - c.pivotX : c.pivotX;
        float x0 = footX - pivotX * s;
        float y0 = footY - (c.canvasH - c.pivotY) * s;
        sb.setColor(base.r, base.g, base.b, base.a * alpha);
        sb.draw(tex, x0, y0, c.canvasW * s, c.canvasH * s, 0, 0, c.canvasW, c.canvasH, flip, false);
    }

    private static void renderOverlay(SpriteBatch sb, ClipData c, float footX, float footY, float x0, float y0, float s) {
        Texture w = ImageMaster.WHITE_SQUARE_IMG;
        float t = Math.max(1f, 2f * Settings.scale);
        float cw = c.canvasW * s, ch = c.canvasH * s;
        sb.setColor(new Color(0.3f, 0.8f, 1f, 0.6f)); // canvas bounds
        sb.draw(w, x0, y0, cw, t);
        sb.draw(w, x0, y0 + ch - t, cw, t);
        sb.draw(w, x0, y0, t, ch);
        sb.draw(w, x0 + cw - t, y0, t, ch);
        sb.setColor(Color.RED); // foot pivot cross
        float arm = 20f * Settings.scale;
        sb.draw(w, footX - arm, footY - t / 2f, arm * 2f, t);
        sb.draw(w, footX - t / 2f, footY - arm, t, arm * 2f);
        String text = c.action + " [" + c.form + "]  f" + animator.frameIndex() + "/" + (c.frameCount - 1)
                + (animator.isDeathLocked() ? "  [REAL DEATH LOCK]" : "")
                + (animator.isHoldingLastFrame() ? "  [hold]" : "")
                + "\nfoot (" + (int) footX + "," + (int) footY + ")  s=" + String.format("%.3f", s);
        FontHelper.renderFontLeftTopAligned(sb, FontHelper.tipBodyFont, text, x0, y0 + ch + 40f * Settings.scale, Color.YELLOW);
    }

    private static void spawnArrow() {
        AbstractPlayer p = AbstractDungeon.player;
        if (p == null || AbstractDungeon.effectList == null) return;
        float s = Settings.scale * ElfConfig.scale();
        boolean flip = p.flipHorizontal;
        float sx = p.drawX + p.animX + (flip ? -ARROW_DX : ARROW_DX) * s;
        float sy = p.drawY + p.animY + ARROW_DY_UP * s;
        AbstractMonster target = pendingArrowTarget;
        if (target == null || target.isDeadOrEscaped()) target = firstLivingMonster();
        float tx, ty;
        if (target != null) {
            tx = target.hb.cX;
            ty = target.hb.cY;
        } else {
            tx = sx + (flip ? -600f : 600f) * Settings.scale;
            ty = sy;
        }
        AbstractDungeon.effectList.add(new ElfArrowEffect(sx, sy, tx, ty, s));
    }

    private static AbstractMonster firstLivingMonster() {
        // Deliberately not getRandomMonster(): must not consume game RNG.
        if (AbstractDungeon.getMonsters() == null) return null;
        for (AbstractMonster m : AbstractDungeon.getMonsters().monsters) {
            if (!m.isDeadOrEscaped()) return m;
        }
        return null;
    }

    private static void log(String msg) {
        ElfPrototypeMod.logger.info("[ElfPrototype] " + msg);
    }
}
